package com.akfreedom.fakevpnbreaker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.akfreedom.fakevpnbreaker.logging.DiagnosticCatalog
import com.akfreedom.fakevpnbreaker.logging.DiagnosticCode
import com.akfreedom.fakevpnbreaker.logging.EventLogRepository
import com.akfreedom.fakevpnbreaker.logging.EventSeverity
import com.akfreedom.fakevpnbreaker.settings.SettingsRepository
import com.akfreedom.fakevpnbreaker.settings.TriggerToken
import com.akfreedom.fakevpnbreaker.settings.TriggerValidationResult
import com.akfreedom.fakevpnbreaker.vpn.VpnBreakController

class TriggerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val eventLogRepository = EventLogRepository(context)
        val settingsRepository = SettingsRepository(context, eventLogRepository)
        val vpnBreakController = VpnBreakController(context, eventLogRepository)
        val action = intent?.action
        val actionState = TriggerToken.classifyAction(action)

        eventLogRepository.append(
            EventSeverity.Debug,
            "Broadcast trigger received: actionState=${DiagnosticCatalog.triggerActionLogValue(actionState)}",
        )
        when (
            TriggerToken.validate(
                action,
                intent?.getStringExtra(TriggerToken.EXTRA_TRIGGER_TOKEN),
            ) {
                settingsRepository.getTriggerToken()
            }
        ) {
            TriggerValidationResult.Accepted -> handleAcceptedTrigger(eventLogRepository, vpnBreakController)
            TriggerValidationResult.MissingAction -> {
                eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.MissingTriggerAction))
            }
            TriggerValidationResult.UnsupportedAction -> {
                eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.UnsupportedTriggerAction))
            }
            TriggerValidationResult.InvalidToken -> {
                eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.InvalidTriggerToken))
            }
        }
    }

    private fun handleAcceptedTrigger(
        eventLogRepository: EventLogRepository,
        vpnBreakController: VpnBreakController,
    ) {
        eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.BroadcastAccepted))
        val hasPermission = vpnBreakController.hasVpnPermission()
        eventLogRepository.append(EventSeverity.Debug, "Broadcast trigger VPN permission granted=$hasPermission")
        if (!hasPermission) {
            eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.MissingVpnPermission))
            return
        }

        val started = vpnBreakController.startBreakService()
        if (started) {
            eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.ServiceDelegated))
        } else {
            eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.ServiceStartFailed))
        }
    }
}
