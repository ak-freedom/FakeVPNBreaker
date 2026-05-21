package com.akfreedom.fakevpnbreaker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.akfreedom.fakevpnbreaker.logging.DiagnosticCatalog
import com.akfreedom.fakevpnbreaker.logging.DiagnosticCode
import com.akfreedom.fakevpnbreaker.logging.EventLogRepository
import com.akfreedom.fakevpnbreaker.logging.EventSeverity
import com.akfreedom.fakevpnbreaker.settings.SettingsRepository
import com.akfreedom.fakevpnbreaker.settings.TriggerToken
import com.akfreedom.fakevpnbreaker.settings.TriggerValidationResult
import com.akfreedom.fakevpnbreaker.vpn.VpnBreakController
import com.akfreedom.fakevpnbreaker.vpn.VpnPermissionRequestResult

class TriggerActivity : Activity() {
    private lateinit var eventLogRepository: EventLogRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var vpnBreakController: VpnBreakController
    private var permissionRequestInProgress = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        eventLogRepository = EventLogRepository(this)
        settingsRepository = SettingsRepository(this, eventLogRepository)
        vpnBreakController = VpnBreakController(this, eventLogRepository)

        processTriggerIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        val actionState = TriggerToken.classifyAction(intent?.action)
        eventLogRepository.append(
            EventSeverity.Debug,
            "TriggerActivity received replacement intent actionState=${DiagnosticCatalog.triggerActionLogValue(actionState)}",
        )
        processTriggerIntent(intent)
    }

    private fun processTriggerIntent(triggerIntent: Intent?) {
        val action = triggerIntent?.action
        val actionState = TriggerToken.classifyAction(action)
        eventLogRepository.append(
            EventSeverity.Debug,
            "TriggerActivity received trigger: actionState=${DiagnosticCatalog.triggerActionLogValue(actionState)}",
        )
        when (
            TriggerToken.validate(
                action,
                triggerIntent?.getStringExtra(TriggerToken.EXTRA_TRIGGER_TOKEN),
            ) {
                settingsRepository.getTriggerToken()
            }
        ) {
            TriggerValidationResult.Accepted -> handleTrigger()
            TriggerValidationResult.MissingAction -> {
                eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.MissingTriggerAction))
                finishTriggerActivity()
            }
            TriggerValidationResult.UnsupportedAction -> {
                eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.UnsupportedTriggerAction))
                finishTriggerActivity()
            }
            TriggerValidationResult.InvalidToken -> {
                eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.InvalidTriggerToken))
                finishTriggerActivity()
            }
        }
    }

    @Deprecated("Deprecated in Android framework; kept to avoid AndroidX dependency.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: android.content.Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_VPN_PERMISSION) return

        permissionRequestInProgress = false
        if (vpnBreakController.hasVpnPermission()) {
            eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.VpnPermissionGranted))
            val started = vpnBreakController.startBreakService()
            if (started) {
                eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.ServiceDelegated))
            } else {
                eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.ServiceStartFailed))
            }
        } else {
            eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.VpnPermissionDenied))
        }
        finishIfConfigured()
    }

    private fun handleTrigger() {
        eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.ActivityAccepted))
        if (vpnBreakController.hasVpnPermission()) {
            eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.VpnPermissionGranted))
            val started = vpnBreakController.startBreakService()
            if (started) {
                eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.ServiceDelegated))
            } else {
                eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.ServiceStartFailed))
            }
            finishIfConfigured()
            return
        }

        if (permissionRequestInProgress) {
            eventLogRepository.append(EventSeverity.Debug, "TriggerActivity ignored duplicate trigger while VPN permission request is in progress")
            return
        }

        permissionRequestInProgress = true
        when (vpnBreakController.requestPermission(this, REQUEST_VPN_PERMISSION)) {
            VpnPermissionRequestResult.Started -> Unit
            VpnPermissionRequestResult.AlreadyGranted -> {
                permissionRequestInProgress = false
                val started = vpnBreakController.startBreakService()
                if (started) {
                    eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.ServiceDelegated))
                } else {
                    eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.ServiceStartFailed))
                }
                finishIfConfigured()
            }
            VpnPermissionRequestResult.LaunchFailed -> {
                permissionRequestInProgress = false
                finishIfConfigured()
            }
        }
    }

    private fun finishIfConfigured() {
        if (settingsRepository.shouldCloseAfterTrigger()) {
            eventLogRepository.append(EventSeverity.Info, "TriggerActivity finished after trigger handling")
            finishTriggerActivity()
        }
    }

    private fun finishTriggerActivity() {
        finishAndRemoveTask()
    }

    private companion object {
        const val REQUEST_VPN_PERMISSION = 41
    }
}
