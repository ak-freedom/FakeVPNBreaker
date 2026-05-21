package com.akfreedom.fakevpnbreaker.logging

import com.akfreedom.fakevpnbreaker.settings.TriggerActionState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DiagnosticCatalogTest {
    @Test
    fun triggerActionLogValueUsesSafeCategories() {
        assertEquals("expected", DiagnosticCatalog.triggerActionLogValue(TriggerActionState.Expected))
        assertEquals("missing", DiagnosticCatalog.triggerActionLogValue(TriggerActionState.Missing))
        assertEquals("unsupported", DiagnosticCatalog.triggerActionLogValue(TriggerActionState.Unsupported))
    }

    @Test
    fun diagnosticsDoNotIncludeExternalValues() {
        val unsafeValues = listOf(
            "token-123",
            "com.example.UNTRUSTED_ACTION",
            "content://downloads/private/VPN_OFF.macro",
            """{"m_extra1Value":"token-123"}""",
            "raw exception message with uri content://downloads/private",
        )
        val messages = DiagnosticCode.entries.map { code ->
            DiagnosticCatalog.message(code, exceptionClass = "SecurityException").text
        }

        messages.forEach { message ->
            unsafeValues.forEach { unsafeValue ->
                assertFalse("Diagnostic leaked unsafe value in: $message", message.contains(unsafeValue))
            }
        }
    }

    @Test
    fun knownRecoveryStatesUseExpectedSeverity() {
        assertEquals(EventSeverity.Warn, DiagnosticCatalog.message(DiagnosticCode.MissingVpnPermission).severity)
        assertEquals(EventSeverity.Warn, DiagnosticCatalog.message(DiagnosticCode.InvalidTriggerToken).severity)
        assertEquals(EventSeverity.Error, DiagnosticCatalog.message(DiagnosticCode.ForegroundStartFailed).severity)
        assertEquals(EventSeverity.Error, DiagnosticCatalog.message(DiagnosticCode.VpnEstablishFailed).severity)
        assertEquals(EventSeverity.Error, DiagnosticCatalog.message(DiagnosticCode.MacroTemplateFailed).severity)
        assertEquals(EventSeverity.Error, DiagnosticCatalog.message(DiagnosticCode.MacroWriteFailed).severity)
        assertEquals(EventSeverity.Warn, DiagnosticCatalog.message(DiagnosticCode.RepeatedStart).severity)
    }
}
