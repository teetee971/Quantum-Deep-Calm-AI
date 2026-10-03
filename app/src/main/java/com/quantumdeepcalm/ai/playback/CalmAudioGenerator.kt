package com.quantumdeepcalm.ai.playback

import android.content.Context
import com.quantumdeepcalm.ai.SessionAudioProfile
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

internal object CalmAudioGenerator {
    const val SAMPLE_RATE_HZ = 8_000
    const val DURATION_SECONDS = 8
    private const val CHANNELS = 1
    private const val BITS_PER_SAMPLE = 16
    private const val WAV_HEADER_SIZE = 44
    private val safeSessionId = Regex("^[a-z0-9-]+$")

    val expectedByteSize: Int
        get() = WAV_HEADER_SIZE + SAMPLE_RATE_HZ * DURATION_SECONDS * (BITS_PER_SAMPLE / 8)

    fun generateWavBytes(profile: SessionAudioProfile): ByteArray {
        val sampleCount = SAMPLE_RATE_HZ * DURATION_SECONDS
        val dataSize = sampleCount * (BITS_PER_SAMPLE / 8)
        val buffer = ByteBuffer
            .allocate(WAV_HEADER_SIZE + dataSize)
            .order(ByteOrder.LITTLE_ENDIAN)

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

        for (index in 0 until sampleCount) {
            val timeSeconds = index.toDouble() / SAMPLE_RATE_HZ.toDouble()
            val slowEnvelope = (1.0 - profile.envelopeDepth) + profile.envelopeDepth * sin(
                (2.0 * PI * timeSeconds / DURATION_SECONDS.toDouble()) - (PI / 2.0),
            )
            val tone =
                0.58 * sin(2.0 * PI * profile.primaryHz * timeSeconds) +
                0.27 * sin(2.0 * PI * profile.secondaryHz * timeSeconds) +
                0.15 * sin(2.0 * PI * profile.accentHz * timeSeconds)
            val normalized = (0.09 * slowEnvelope * tone).coerceIn(-1.0, 1.0)
            buffer.putShort((normalized * Short.MAX_VALUE).toInt().toShort())
        }

        return buffer.array()
    }

    fun hasExpectedContent(candidate: ByteArray, profile: SessionAudioProfile): Boolean {
        return candidate.size == expectedByteSize &&
            candidate.contentEquals(generateWavBytes(profile))
    }

    fun ensureGeneratedFile(
        context: Context,
        sessionId: String,
        profile: SessionAudioProfile,
    ): File {
        require(safeSessionId.matches(sessionId)) { "Invalid session id." }

        val target = File(context.filesDir, "qdc_${sessionId}_v2.wav")
        val expectedBytes = generateWavBytes(profile)

        if (target.exists() && target.length() == expectedBytes.size.toLong()) {
            val existingBytes = runCatching { target.readBytes() }.getOrNull()
            if (existingBytes != null && existingBytes.contentEquals(expectedBytes)) {
                return target
            }
        }

        val temporary = File(context.filesDir, "qdc_${sessionId}_v2.wav.tmp")
        temporary.writeBytes(expectedBytes)

        if (target.exists()) {
            temporary.copyTo(target, overwrite = true)
            temporary.delete()
            return target
        }

        if (!temporary.renameTo(target)) {
            temporary.copyTo(target, overwrite = true)
            temporary.delete()
        }

        return target
    }
}
