package com.akfreedom.fakevpnbreaker.settings

object TriggerToken {
    const val ACTION_BREAK_VPN = "com.akfreedom.fakevpnbreaker.BREAK_VPN"
    const val EXTRA_TRIGGER_TOKEN = "com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN"

    fun isValid(provided: String?, expected: String): Boolean = expected.isNotBlank() && provided == expected
}
