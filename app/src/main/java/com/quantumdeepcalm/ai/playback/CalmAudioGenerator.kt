package com.quantumdeepcalm.ai.playback

import android.content.Context
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

    val expectedByteSize: Int
        get() = WAV_HEADER_SIZE + SAMPLE_RATE_HZ * DURATION_SECONDS * (BITS_PER_SAMPLE / 8)

    fun generateWavBytes(): ByteArray {
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
            val slowEnvelope = 0.72 + 0.28 * sin(
                (2.0 * PI * timeSeconds / DURATION_SECONDS.toDouble()) - (PI / 2.0),
            )
            val tone =
                0.58 * sin(2.0 * PI * 110.0 * timeSeconds) +
                0.27 * sin(2.0 * PI * 165.0 * timeSeconds) +
                0.15 * sin(2.0 * PI * 220.0 * timeSeconds)
            val normalized = (0.09 * slowEnvelope * tone).coerceIn(-1.0, 1.0)
            buffer.putShort((normalized * Short.MAX_VALUE).toInt().toShort())
        }

        return buffer.array()
    }

    fun ensureGeneratedFile(context: Context): File {
        val target = File(context.filesDir, "calm_ambience_v1.wav")
        if (target.exists() && target.length() == expectedByteSize.toLong()) {
            return target
        }

        val bytes = generateWavBytes()
        val temporary = File(context.filesDir, "calm_ambience_v1.wav.tmp")
        temporary.writeBytes(bytes)

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
