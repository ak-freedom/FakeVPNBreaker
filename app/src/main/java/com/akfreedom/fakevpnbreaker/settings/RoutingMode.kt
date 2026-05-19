package com.akfreedom.fakevpnbreaker.settings

enum class RoutingMode(val label: String) {
    FullTakeover("Full takeover"),
    LocalOnly("Local only");

    companion object {
        val Default = FullTakeover

        fun fromName(value: String?): RoutingMode = entries.firstOrNull { it.name == value } ?: Default
    }
}
