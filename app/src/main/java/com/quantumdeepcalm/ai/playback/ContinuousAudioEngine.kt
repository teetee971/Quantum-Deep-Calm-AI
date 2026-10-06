package com.quantumdeepcalm.ai.playback

import android.content.Context
import com.quantumdeepcalm.ai.SessionAudioProfile
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

internal data class ContinuousAudioLayer(
    val frequencyHz: Double,
    val level: Double,
    val modulationDepth: Double,
    val modulationPeriodSeconds: Double,
    val phaseRadians: Double,
) {
    init {
        require(frequencyHz.isFinite() && frequencyHz > 0.0) { "Layer frequency must be finite and positive." }
        require(level.isFinite() && level in 0.0..1.0) { "Layer level must be finite and between 0 and 1." }
        require(modulationDepth.isFinite() && modulationDepth in 0.0..0.8) {
            "Layer modulation depth must be finite and between 0 and 0.8."
        }
        require(modulationPeriodSeconds.isFinite() && modulationPeriodSeconds > 0.0) {
            "Layer modulation period must be finite and positive."
        }
        require(phaseRadians.isFinite()) { "Layer phase must be finite." }
    }
}

internal data class ContinuousAudioMix(
    val layers: List<ContinuousAudioLayer>,
    val masterGain: Double,
) {
    init {
        require(layers.size >= 2) { "A continuous mix requires at least two layers." }
        require(masterGain.isFinite() && masterGain in 0.0..0.9) {
            "Master gain must be finite and leave clipping headroom."
        }
        require(layers.sumOf { it.level } <= 1.0 + 1e-9) {
            "Combined layer levels must not exceed unity."
        }
        require(layers.any { it.level > 0.0 }) { "At least one audio layer must be audible." }
    }
}

internal object ContinuousAudioEngine {
    const val SAMPLE_RATE_HZ = 12_000
    const val DEFAULT_DURATION_SECONDS = 180

    private const val CHANNELS = 1
    private const val BITS_PER_SAMPLE = 16
    private const val WAV_HEADER_SIZE = 44
    private const val ENGINE_VERSION = 1
    private const val MASTER_GAIN = 0.78
    private const val MAX_RENDER_SECONDS = 60 * 30
    private val safeSessionId = Regex("^[a-z0-9-]+$")

    fun buildMix(profile: SessionAudioProfile): ContinuousAudioMix {
        val modulation = profile.envelopeDepth.coerceIn(0.0, 0.8)
        return ContinuousAudioMix(
            layers = listOf(
                ContinuousAudioLayer(
                    frequencyHz = profile.primaryHz,
                    level = 0.36,
                    modulationDepth = modulation * 0.40,
                    modulationPeriodSeconds = 17.0,
                    phaseRadians = 0.0,
                ),
                ContinuousAudioLayer(
                    frequencyHz = profile.secondaryHz,
                    level = 0.26,
                    modulationDepth = modulation * 0.55,
                    modulationPeriodSeconds = 29.0,
                    phaseRadians = PI / 3.0,
                ),
                ContinuousAudioLayer(
                    frequencyHz = profile.accentHz,
                    level = 0.20,
                    modulationDepth = modulation * 0.70,
                    modulationPeriodSeconds = 41.0,
                    phaseRadians = PI / 2.0,
                ),
                ContinuousAudioLayer(
                    frequencyHz = profile.accentHz * 2.0,
                    level = 0.18,
                    modulationDepth = modulation * 0.50,
                    modulationPeriodSeconds = 53.0,
                    phaseRadians = 3.0 * PI / 4.0,
                ),
            ),
            masterGain = MASTER_GAIN,
        )
    }

    fun expectedByteSize(durationSeconds: Int = DEFAULT_DURATION_SECONDS): Int {
        require(durationSeconds in 1..MAX_RENDER_SECONDS) { "Unsupported render duration." }
        return WAV_HEADER_SIZE +
            SAMPLE_RATE_HZ * durationSeconds * CHANNELS * (BITS_PER_SAMPLE / 8)
    }

    fun renderWavBytes(
        profile: SessionAudioProfile,
        durationSeconds: Int = DEFAULT_DURATION_SECONDS,
    ): ByteArray = renderWavBytes(
        mix = buildMix(profile),
        durationSeconds = durationSeconds,
    )

    fun renderWavBytes(
        mix: ContinuousAudioMix,
        durationSeconds: Int = DEFAULT_DURATION_SECONDS,
    ): ByteArray {
        require(durationSeconds in 1..MAX_RENDER_SECONDS) { "Unsupported render duration." }

        val sampleCount = SAMPLE_RATE_HZ * durationSeconds
        val dataSize = sampleCount * CHANNELS * (BITS_PER_SAMPLE / 8)
        val buffer = ByteBuffer
            .allocate(WAV_HEADER_SIZE + dataSize)
            .order(ByteOrder.LITTLE_ENDIAN)

        writeWavHeader(buffer = buffer, dataSize = dataSize)

        for (index in 0 until sampleCount) {
            val timeSeconds = index.toDouble() / SAMPLE_RATE_HZ.toDouble()
            var mixed = 0.0

            for (layer in mix.layers) {
                if (layer.level == 0.0) {
                    continue
                }
                val modulationPhase = (2.0 * PI * timeSeconds / layer.modulationPeriodSeconds) +
                    layer.phaseRadians
                val modulation = (1.0 - layer.modulationDepth) +
                    layer.modulationDepth * (0.5 + 0.5 * sin(modulationPhase))
                mixed += layer.level * modulation * sin(
                    2.0 * PI * layer.frequencyHz * timeSeconds + layer.phaseRadians,
                )
            }

            val edgeFade = edgeFade(
                timeSeconds = timeSeconds,
                durationSeconds = durationSeconds.toDouble(),
            )
            val normalized = (mix.masterGain * edgeFade * mixed).coerceIn(-0.90, 0.90)
            buffer.putShort((normalized * Short.MAX_VALUE).toInt().toShort())
        }

        return buffer.array()
    }

    @Synchronized
    fun ensureGeneratedFile(
        context: Context,
        sessionId: String,
        profile: SessionAudioProfile,
    ): File {
        require(safeSessionId.matches(sessionId)) { "Invalid session id." }

        val expectedSize = expectedByteSize()
        val target = File(
            context.filesDir,
            "qdc_${sessionId}_continuous_v${ENGINE_VERSION}.wav",
        )

        if (isValidCachedWav(target, expectedSize)) {
            return target
        }

        val rendered = renderWavBytes(profile = profile)
        check(rendered.size == expectedSize) { "Rendered audio size does not match the WAV contract." }

        val temporary = File(context.filesDir, "${target.name}.tmp")
        temporary.writeBytes(rendered)

        if (target.exists() && !target.delete()) {
            temporary.delete()
            error("Unable to replace stale generated audio.")
        }

        if (!temporary.renameTo(target)) {
            temporary.copyTo(target, overwrite = true)
            temporary.delete()
        }

        check(isValidCachedWav(target, expectedSize)) { "Generated audio file failed integrity validation." }
        return target
    }

    private fun writeWavHeader(buffer: ByteBuffer, dataSize: Int) {
        buffer.put("RIFF".toByteArray(Charsets.US_ASCII))
        buffer.putInt(36 + dataSize)
        buffer.put("WAVE".toByteArray(Charsets.US_ASCII))
        buffer.put("fmt ".toByteArray(Charsets.US_ASCII))
        buffer.putInt(16)
        buffer.putShort(1.toShort())
        buffer.putShort(CHANNELS.toShort())
        buffer.putInt(SAMPLE_RATE_HZ)
        buffer.putInt(SAMPLE_RATE_HZ * CHANNELS * (BITS_PER_SAMPLE / 8))
        buffer.putShort((CHANNELS * (BITS_PER_SAMPLE / 8)).toShort())
        buffer.putShort(BITS_PER_SAMPLE.toShort())
        buffer.put("data".toByteArray(Charsets.US_ASCII))
        buffer.putInt(dataSize)
    }

    private fun edgeFade(timeSeconds: Double, durationSeconds: Double): Double {
        val fadeSeconds = minOf(4.0, durationSeconds / 8.0).coerceAtLeast(0.05)
        val fadeIn = (timeSeconds / fadeSeconds).coerceIn(0.0, 1.0)
        val fadeOut = ((durationSeconds - timeSeconds) / fadeSeconds).coerceIn(0.0, 1.0)
        return minOf(fadeIn, fadeOut)
    }

    private fun isValidCachedWav(file: File, expectedSize: Int): Boolean {
        if (!file.isFile || file.length() != expectedSize.toLong()) {
            return false
        }

        val header = ByteArray(WAV_HEADER_SIZE)
        val bytesRead = runCatching {
            file.inputStream().use { input -> input.read(header) }
        }.getOrDefault(-1)
        if (bytesRead != WAV_HEADER_SIZE) {
            return false
        }

        if (!header.copyOfRange(0, 4).contentEquals("RIFF".toByteArray(Charsets.US_ASCII))) {
            return false
        }
        if (!header.copyOfRange(8, 12).contentEquals("WAVE".toByteArray(Charsets.US_ASCII))) {
            return false
        }
        if (!header.copyOfRange(12, 16).contentEquals("fmt ".toByteArray(Charsets.US_ASCII))) {
            return false
        }
        if (!header.copyOfRange(36, 40).contentEquals("data".toByteArray(Charsets.US_ASCII))) {
            return false
        }

        val declaredDataSize = ByteBuffer
            .wrap(header, 40, 4)
            .order(ByteOrder.LITTLE_ENDIAN)
            .int
        return declaredDataSize == expectedSize - WAV_HEADER_SIZE
    }
}
