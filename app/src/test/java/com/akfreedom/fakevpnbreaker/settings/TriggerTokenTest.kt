package com.akfreedom.fakevpnbreaker.settings

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TriggerTokenTest {
    @Test
    fun expectedActionAndMatchingTokenIsAccepted() {
        assertTrue(TriggerToken.isValid("token-123", "token-123"))
        assertEquals(
            TriggerValidationResult.Accepted,
            TriggerToken.validate(
                TriggerToken.ACTION_BREAK_VPN,
                "token-123",
                "token-123",
            ),
        )
    }

    @Test
    fun missingWrongOrBlankExpectedTokenIsRejected() {
        assertFalse(TriggerToken.isValid(null, "token-123"))
        assertFalse(TriggerToken.isValid("", "token-123"))
        assertFalse(TriggerToken.isValid("wrong", "token-123"))
        assertFalse(TriggerToken.isValid("token-123", ""))
        assertEquals(
            TriggerValidationResult.InvalidToken,
            TriggerToken.validate(TriggerToken.ACTION_BREAK_VPN, null, "token-123"),
        )
        assertEquals(
            TriggerValidationResult.InvalidToken,
            TriggerToken.validate(TriggerToken.ACTION_BREAK_VPN, "", "token-123"),
        )
        assertEquals(
            TriggerValidationResult.InvalidToken,
            TriggerToken.validate(TriggerToken.ACTION_BREAK_VPN, "wrong", "token-123"),
        )
        assertEquals(
            TriggerValidationResult.InvalidToken,
            TriggerToken.validate(TriggerToken.ACTION_BREAK_VPN, "token-123", ""),
        )
    }

    @Test
    fun unsupportedActionIsRejectedBeforeTokenCheck() {
        var expectedTokenWasRead = false

        assertEquals(
            TriggerValidationResult.UnsupportedAction,
            TriggerToken.validate("unsupported", "token-123", "token-123"),
        )
        assertEquals(
            TriggerValidationResult.UnsupportedAction,
            TriggerToken.validate(null, "token-123") {
                expectedTokenWasRead = true
                "token-123"
            },
        )
        assertFalse(expectedTokenWasRead)
    }
}
