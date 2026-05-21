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
        assertEquals(
            TriggerValidationResult.InvalidToken,
            TriggerToken.validate(TriggerToken.ACTION_BREAK_VPN, "token-123", " "),
        )
        assertEquals(
            TriggerValidationResult.InvalidToken,
            TriggerToken.validate(TriggerToken.ACTION_BREAK_VPN, "token-123", "\n"),
        )
        assertEquals(
            TriggerValidationResult.InvalidToken,
            TriggerToken.validate(TriggerToken.ACTION_BREAK_VPN, null) { "token-123" },
        )
    }

    @Test
    fun unsupportedActionIsRejectedBeforeTokenCheck() {
        var expectedTokenWasRead = false

        assertEquals(
            TriggerValidationResult.UnsupportedAction,
            TriggerToken.validate("unsupported", "token-123") {
                expectedTokenWasRead = true
                "token-123"
            },
        )
        assertFalse(expectedTokenWasRead)
    }

    @Test
    fun missingActionIsRejectedBeforeTokenCheck() {
        var expectedTokenWasRead = false

        assertEquals(
            TriggerValidationResult.MissingAction,
            TriggerToken.validate(null, "token-123", "token-123"),
        )
        assertEquals(
            TriggerValidationResult.MissingAction,
            TriggerToken.validate(null, "token-123") {
                expectedTokenWasRead = true
                "token-123"
            },
        )
        assertFalse(expectedTokenWasRead)
    }

    @Test
    fun actionMatchingIsExact() {
        listOf(
            "",
            " ",
            TriggerToken.ACTION_BREAK_VPN.lowercase(),
            "${TriggerToken.ACTION_BREAK_VPN} ",
            " ${TriggerToken.ACTION_BREAK_VPN}",
        ).forEach { action ->
            assertEquals(
                TriggerValidationResult.UnsupportedAction,
                TriggerToken.validate(action, "token-123", "token-123"),
            )
        }
    }

    @Test
    fun classifiesActionsWithoutReturningRawExternalValue() {
        assertEquals(TriggerActionState.Expected, TriggerToken.classifyAction(TriggerToken.ACTION_BREAK_VPN))
        assertEquals(TriggerActionState.Missing, TriggerToken.classifyAction(null))
        assertEquals(TriggerActionState.Unsupported, TriggerToken.classifyAction("untrusted-action"))
    }
}
