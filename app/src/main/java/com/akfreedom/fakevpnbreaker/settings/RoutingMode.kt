package com.akfreedom.fakevpnbreaker.settings

enum class RoutingMode {
    FullTakeover,
    LocalOnly;

    companion object {
        val Default = FullTakeover

        fun fromName(value: String?): RoutingMode = entries.firstOrNull { it.name == value } ?: Default
    }
}
