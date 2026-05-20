package com.akfreedom.fakevpnbreaker.settings

object TriggerToken {
    const val ACTION_BREAK_VPN = "com.akfreedom.fakevpnbreaker.BREAK_VPN"
    const val EXTRA_TRIGGER_TOKEN = "com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN"

    fun validate(action: String?, provided: String?, expectedProvider: () -> String): TriggerValidationResult =
        when {
            action != ACTION_BREAK_VPN -> TriggerValidationResult.UnsupportedAction
            else -> validateToken(provided, expectedProvider())
        }

    fun validate(action: String?, provided: String?, expected: String): TriggerValidationResult =
        when {
            action != ACTION_BREAK_VPN -> TriggerValidationResult.UnsupportedAction
            else -> validateToken(provided, expected)
        }

    fun isValid(provided: String?, expected: String): Boolean =
        validate(ACTION_BREAK_VPN, provided, expected) == TriggerValidationResult.Accepted

    private fun validateToken(provided: String?, expected: String): TriggerValidationResult =
        if (expected.isNotBlank() && provided == expected) {
            TriggerValidationResult.Accepted
        } else {
            TriggerValidationResult.InvalidToken
        }
}

enum class TriggerValidationResult {
    Accepted,
    UnsupportedAction,
    InvalidToken,
}
