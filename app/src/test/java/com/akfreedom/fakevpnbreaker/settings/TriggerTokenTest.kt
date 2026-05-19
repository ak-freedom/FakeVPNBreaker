package com.akfreedom.fakevpnbreaker.settings

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TriggerTokenTest {
    @Test
    fun matchingTokenIsValid() {
        assertTrue(TriggerToken.isValid("token-123", "token-123"))
    }

    @Test
    fun missingOrWrongTokenIsRejected() {
        assertFalse(TriggerToken.isValid(null, "token-123"))
        assertFalse(TriggerToken.isValid("", "token-123"))
        assertFalse(TriggerToken.isValid("wrong", "token-123"))
        assertFalse(TriggerToken.isValid("token-123", ""))
    }
}
