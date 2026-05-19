package com.akfreedom.fakevpnbreaker.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class BreakDurationTest {
    @Test
    fun fromMillisAcceptsOnlyAllowedValues() {
        assertEquals(BreakDuration.Ms300, BreakDuration.fromMillis(300))
        assertEquals(BreakDuration.Ms500, BreakDuration.fromMillis(500))
        assertEquals(BreakDuration.Ms1000, BreakDuration.fromMillis(1000))
        assertEquals(BreakDuration.Ms2000, BreakDuration.fromMillis(2000))
        assertEquals(BreakDuration.Ms5000, BreakDuration.fromMillis(5000))
    }

    @Test
    fun invalidMillisFallsBackToDefault() {
        assertEquals(BreakDuration.Default, BreakDuration.fromMillis(42))
    }

    @Test
    fun invalidNameFallsBackToDefault() {
        assertEquals(BreakDuration.Default, BreakDuration.fromName("invalid"))
        assertEquals(BreakDuration.Default, BreakDuration.fromName(null))
    }
}
