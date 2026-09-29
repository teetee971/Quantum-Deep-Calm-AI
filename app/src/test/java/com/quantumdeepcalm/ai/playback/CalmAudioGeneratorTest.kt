package com.quantumdeepcalm.ai.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalmAudioGeneratorTest {

    @Test
    fun generatedWavHasExpectedContainerAndSize() {
        val bytes = CalmAudioGenerator.generateWavBytes()

        assertEquals(CalmAudioGenerator.expectedByteSize, bytes.size)
        assertEquals("RIFF", bytes.copyOfRange(0, 4).toString(Charsets.US_ASCII))
        assertEquals("WAVE", bytes.copyOfRange(8, 12).toString(Charsets.US_ASCII))
        assertEquals("fmt ", bytes.copyOfRange(12, 16).toString(Charsets.US_ASCII))
        assertEquals("data", bytes.copyOfRange(36, 40).toString(Charsets.US_ASCII))
    }

    @Test
    fun generatedWavContainsAudibleNonZeroSamples() {
        val bytes = CalmAudioGenerator.generateWavBytes()
        assertTrue(bytes.drop(44).any { it.toInt() != 0 })
    }

    @Test
    fun expectedContentRejectsSameSizeCorruption() {
        val corrupted = CalmAudioGenerator.generateWavBytes()
        corrupted[corrupted.lastIndex] = (corrupted.last().toInt() xor 0x01).toByte()

        assertEquals(CalmAudioGenerator.expectedByteSize, corrupted.size)
        assertFalse(CalmAudioGenerator.hasExpectedContent(corrupted))
    }

    @Test
    fun expectedContentAcceptsFreshGeneration() {
        assertTrue(
            CalmAudioGenerator.hasExpectedContent(
                CalmAudioGenerator.generateWavBytes(),
            ),
        )
    }
}
