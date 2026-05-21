package com.akfreedom.fakevpnbreaker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.akfreedom.fakevpnbreaker.logging.EventLogRepository
import com.akfreedom.fakevpnbreaker.logging.EventSeverity
import com.akfreedom.fakevpnbreaker.settings.SettingsRepository
import com.akfreedom.fakevpnbreaker.settings.TriggerActionState
import com.akfreedom.fakevpnbreaker.settings.TriggerToken
import com.akfreedom.fakevpnbreaker.settings.TriggerValidationResult
import com.akfreedom.fakevpnbreaker.vpn.VpnBreakController

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
            "[FIX:trigger-single-task] TriggerActivity received replacement intent actionState=${actionState.logValue}",
        )
        processTriggerIntent(intent)
    }

    private fun processTriggerIntent(triggerIntent: Intent?) {
        val action = triggerIntent?.action
        val actionState = TriggerToken.classifyAction(action)
        eventLogRepository.append(EventSeverity.Debug, "TriggerActivity received trigger: actionState=${actionState.logValue}")
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
                eventLogRepository.append(EventSeverity.Warn, "TriggerActivity rejected trigger: missing action")
                finishTriggerActivity()
            }
            TriggerValidationResult.UnsupportedAction -> {
                eventLogRepository.append(EventSeverity.Warn, "TriggerActivity rejected trigger: unsupported action")
                finishTriggerActivity()
            }
            TriggerValidationResult.InvalidToken -> {
                eventLogRepository.append(EventSeverity.Warn, "[FIX:trigger-auth] Rejected trigger with missing or invalid token")
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
            eventLogRepository.append(EventSeverity.Info, "VPN permission granted from trigger")
            vpnBreakController.startBreakService()
        } else {
            eventLogRepository.append(EventSeverity.Warn, "VPN permission missing after trigger consent flow")
        }
        finishIfConfigured()
    }

    private fun handleTrigger() {
        eventLogRepository.append(EventSeverity.Info, "TriggerActivity accepted trigger")
        if (vpnBreakController.hasVpnPermission()) {
            eventLogRepository.append(EventSeverity.Info, "Trigger permission already granted")
            val started = vpnBreakController.startBreakService()
            if (started) {
                eventLogRepository.append(EventSeverity.Info, "Trigger delegated service start")
            } else {
                eventLogRepository.append(EventSeverity.Error, "Trigger failed to delegate service start")
            }
            finishIfConfigured()
            return
        }

        if (permissionRequestInProgress) {
            eventLogRepository.append(EventSeverity.Debug, "TriggerActivity ignored duplicate trigger while VPN permission request is in progress")
            return
        }

        permissionRequestInProgress = true
        eventLogRepository.append(EventSeverity.Info, "Trigger requesting VPN permission")
        val requestStarted = vpnBreakController.requestPermission(this, REQUEST_VPN_PERMISSION)
        if (!requestStarted) {
            permissionRequestInProgress = false
            eventLogRepository.append(EventSeverity.Warn, "Trigger permission request skipped before consent flow")
            finishIfConfigured()
        }
    }

    private fun finishIfConfigured() {
        if (settingsRepository.shouldCloseAfterTrigger()) {
            eventLogRepository.append(EventSeverity.Info, "TriggerActivity finished")
            finishTriggerActivity()
        }
    }

    private fun finishTriggerActivity() {
        finishAndRemoveTask()
    }

    private val TriggerActionState.logValue: String
        get() = when (this) {
            TriggerActionState.Expected -> "expected"
            TriggerActionState.Missing -> "missing"
            TriggerActionState.Unsupported -> "unsupported"
        }

    private companion object {
        const val REQUEST_VPN_PERMISSION = 41
    }
}
