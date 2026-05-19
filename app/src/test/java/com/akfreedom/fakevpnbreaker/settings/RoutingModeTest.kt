package com.akfreedom.fakevpnbreaker.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class RoutingModeTest {
    @Test
    fun invalidNameFallsBackToDefault() {
        assertEquals(RoutingMode.Default, RoutingMode.fromName("invalid"))
        assertEquals(RoutingMode.Default, RoutingMode.fromName(null))
    }

    @Test
    fun validNameIsParsed() {
        assertEquals(RoutingMode.FullTakeover, RoutingMode.fromName("FullTakeover"))
        assertEquals(RoutingMode.LocalOnly, RoutingMode.fromName("LocalOnly"))
    }
}
