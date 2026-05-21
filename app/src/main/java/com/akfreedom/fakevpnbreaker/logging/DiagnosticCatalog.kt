package com.akfreedom.fakevpnbreaker.logging

import com.akfreedom.fakevpnbreaker.settings.TriggerActionState

object DiagnosticCatalog {
    fun message(code: DiagnosticCode, exceptionClass: String? = null): DiagnosticMessage {
        val safeException = exceptionClass?.takeIf { it.isNotBlank() }?.let { " exception=$it" }.orEmpty()
        return when (code) {
            DiagnosticCode.BroadcastAccepted -> DiagnosticMessage(
                EventSeverity.Info,
                "Broadcast trigger accepted; starting dummy VPN if permission is already granted",
            )
            DiagnosticCode.ActivityAccepted -> DiagnosticMessage(
                EventSeverity.Info,
                "Activity trigger accepted; continuing with VPN permission or service delegation",
            )
            DiagnosticCode.MissingVpnPermission -> DiagnosticMessage(
                EventSeverity.Warn,
                "VPN permission missing; open FakeVpnBreaker and grant VPN permission before triggering again",
            )
            DiagnosticCode.VpnPermissionRequestStarted -> DiagnosticMessage(
                EventSeverity.Info,
                "VPN permission request started; waiting for Android consent",
            )
            DiagnosticCode.VpnPermissionGranted -> DiagnosticMessage(
                EventSeverity.Info,
                "VPN permission granted; dummy VPN can be started",
            )
            DiagnosticCode.VpnPermissionDenied -> DiagnosticMessage(
                EventSeverity.Warn,
                "VPN permission still missing after consent flow; grant permission before running again",
            )
            DiagnosticCode.VpnPermissionLaunchFailed -> DiagnosticMessage(
                EventSeverity.Error,
                "VPN permission screen could not be opened; retry from FakeVpnBreaker$safeException",
            )
            DiagnosticCode.InvalidTriggerToken -> DiagnosticMessage(
                EventSeverity.Warn,
                "Trigger rejected: missing or invalid token; update the MacroDroid extra from FakeVpnBreaker",
            )
            DiagnosticCode.MissingTriggerAction -> DiagnosticMessage(
                EventSeverity.Warn,
                "Trigger rejected: missing action; update the MacroDroid action value",
            )
            DiagnosticCode.UnsupportedTriggerAction -> DiagnosticMessage(
                EventSeverity.Warn,
                "Trigger rejected: unsupported action; update the MacroDroid action value",
            )
            DiagnosticCode.ForegroundStartFailed -> DiagnosticMessage(
                EventSeverity.Error,
                "Foreground VPN service could not start; try the Activity fallback$safeException",
            )
            DiagnosticCode.ServiceStartFailed -> DiagnosticMessage(
                EventSeverity.Error,
                "VPN break service start failed; try the Activity fallback$safeException",
            )
            DiagnosticCode.ServiceDelegated -> DiagnosticMessage(
                EventSeverity.Info,
                "VPN break service start delegated",
            )
            DiagnosticCode.VpnEstablishFailed -> DiagnosticMessage(
                EventSeverity.Error,
                "Dummy VPN could not be established; retry the break action$safeException",
            )
            DiagnosticCode.MacroTemplateFailed -> DiagnosticMessage(
                EventSeverity.Error,
                "MacroDroid macro export failed: bundled template could not be prepared",
            )
            DiagnosticCode.MacroPickerLaunchFailed -> DiagnosticMessage(
                EventSeverity.Error,
                "MacroDroid document picker could not be opened; retry export from FakeVpnBreaker$safeException",
            )
            DiagnosticCode.MacroSaveCancelled -> DiagnosticMessage(
                EventSeverity.Info,
                "MacroDroid macro export cancelled",
            )
            DiagnosticCode.MacroMissingUri -> DiagnosticMessage(
                EventSeverity.Warn,
                "MacroDroid macro export did not return a document; choose a save location and retry",
            )
            DiagnosticCode.MacroOutputUnavailable -> DiagnosticMessage(
                EventSeverity.Warn,
                "MacroDroid macro export target is unavailable; choose another save location",
            )
            DiagnosticCode.MacroWriteFailed -> DiagnosticMessage(
                EventSeverity.Error,
                "MacroDroid macro export failed while writing the document$safeException",
            )
            DiagnosticCode.MacroPermissionDenied -> DiagnosticMessage(
                EventSeverity.Error,
                "MacroDroid macro export permission denied; choose a writable location",
            )
            DiagnosticCode.RepeatedStart -> DiagnosticMessage(
                EventSeverity.Warn,
                "Repeated start received; replacing the active dummy VPN session",
            )
        }
    }

    fun triggerActionLogValue(actionState: TriggerActionState): String =
        when (actionState) {
            TriggerActionState.Expected -> "expected"
            TriggerActionState.Missing -> "missing"
            TriggerActionState.Unsupported -> "unsupported"
        }
}

enum class DiagnosticCode {
    BroadcastAccepted,
    ActivityAccepted,
    MissingVpnPermission,
    VpnPermissionRequestStarted,
    VpnPermissionGranted,
    VpnPermissionDenied,
    VpnPermissionLaunchFailed,
    InvalidTriggerToken,
    MissingTriggerAction,
    UnsupportedTriggerAction,
    ForegroundStartFailed,
    ServiceStartFailed,
    ServiceDelegated,
    VpnEstablishFailed,
    MacroTemplateFailed,
    MacroPickerLaunchFailed,
    MacroSaveCancelled,
    MacroMissingUri,
    MacroOutputUnavailable,
    MacroWriteFailed,
    MacroPermissionDenied,
    RepeatedStart,
}

data class DiagnosticMessage(
    val severity: EventSeverity,
    val text: String,
)
