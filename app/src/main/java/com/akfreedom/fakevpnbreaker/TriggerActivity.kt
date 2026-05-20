package com.akfreedom.fakevpnbreaker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.akfreedom.fakevpnbreaker.logging.EventLogRepository
import com.akfreedom.fakevpnbreaker.logging.EventSeverity
import com.akfreedom.fakevpnbreaker.settings.SettingsRepository
import com.akfreedom.fakevpnbreaker.settings.TriggerToken
import com.akfreedom.fakevpnbreaker.settings.TriggerValidationResult
import com.akfreedom.fakevpnbreaker.vpn.VpnBreakController

class TriggerActivity : Activity() {
    private lateinit var eventLogRepository: EventLogRepository
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var vpnBreakController: VpnBreakController

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
        eventLogRepository.append(EventSeverity.Info, "[FIX:trigger-single-task] TriggerActivity received replacement intent")
        processTriggerIntent(intent)
    }

    private fun processTriggerIntent(triggerIntent: Intent?) {
        val action = triggerIntent?.action
        eventLogRepository.append(EventSeverity.Info, "Trigger received: ${action ?: "missing action"}")
        when (
            TriggerToken.validate(
                action,
                triggerIntent?.getStringExtra(TriggerToken.EXTRA_TRIGGER_TOKEN),
            ) {
                settingsRepository.getTriggerToken()
            }
        ) {
            TriggerValidationResult.Accepted -> handleTrigger()
            TriggerValidationResult.UnsupportedAction -> {
                eventLogRepository.append(EventSeverity.Warn, "Unsupported trigger action")
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

        if (vpnBreakController.hasVpnPermission()) {
            eventLogRepository.append(EventSeverity.Info, "VPN permission granted from trigger")
            vpnBreakController.startBreakService()
        } else {
            eventLogRepository.append(EventSeverity.Warn, "VPN permission missing after trigger consent flow")
        }
        finishIfConfigured()
    }

    private fun handleTrigger() {
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

        eventLogRepository.append(EventSeverity.Info, "Trigger requesting VPN permission")
        vpnBreakController.requestPermission(this, REQUEST_VPN_PERMISSION)
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

    private companion object {
        const val REQUEST_VPN_PERMISSION = 41
    }
}
