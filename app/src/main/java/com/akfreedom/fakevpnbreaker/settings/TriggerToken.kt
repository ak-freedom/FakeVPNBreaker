package com.akfreedom.fakevpnbreaker.settings

object TriggerToken {
    const val ACTION_BREAK_VPN = "com.akfreedom.fakevpnbreaker.BREAK_VPN"
    const val EXTRA_TRIGGER_TOKEN = "com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN"

    fun validate(action: String?, provided: String?, expectedProvider: () -> String): TriggerValidationResult =
        when (classifyAction(action)) {
            TriggerActionState.Expected -> validateToken(provided, expectedProvider())
            TriggerActionState.Missing -> TriggerValidationResult.MissingAction
            TriggerActionState.Unsupported -> TriggerValidationResult.UnsupportedAction
        }

    fun validate(action: String?, provided: String?, expected: String): TriggerValidationResult =
        when (classifyAction(action)) {
            TriggerActionState.Expected -> validateToken(provided, expected)
            TriggerActionState.Missing -> TriggerValidationResult.MissingAction
            TriggerActionState.Unsupported -> TriggerValidationResult.UnsupportedAction
        }

    fun isValid(provided: String?, expected: String): Boolean =
        validate(ACTION_BREAK_VPN, provided, expected) == TriggerValidationResult.Accepted

    fun classifyAction(action: String?): TriggerActionState =
        when {
            action == null -> TriggerActionState.Missing
            action == ACTION_BREAK_VPN -> TriggerActionState.Expected
            else -> TriggerActionState.Unsupported
        }

    private fun validateToken(provided: String?, expected: String): TriggerValidationResult =
        if (expected.isNotBlank() && provided == expected) {
            TriggerValidationResult.Accepted
        } else {
            TriggerValidationResult.InvalidToken
        }
}

enum class TriggerValidationResult {
    Accepted,
    MissingAction,
    UnsupportedAction,
    InvalidToken,
}

enum class TriggerActionState {
    Expected,
    Missing,
    Unsupported,
}
