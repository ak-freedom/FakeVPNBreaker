package com.akfreedom.fakevpnbreaker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.akfreedom.fakevpnbreaker.logging.EventLogRepository
import com.akfreedom.fakevpnbreaker.logging.EventSeverity
import com.akfreedom.fakevpnbreaker.settings.SettingsRepository
import com.akfreedom.fakevpnbreaker.settings.TriggerActionState
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

        eventLogRepository.append(EventSeverity.Debug, "Broadcast trigger received: actionState=${actionState.logValue}")
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
                eventLogRepository.append(EventSeverity.Warn, "Broadcast trigger rejected: missing action")
            }
            TriggerValidationResult.UnsupportedAction -> {
                eventLogRepository.append(EventSeverity.Warn, "Broadcast trigger rejected: unsupported action")
            }
            TriggerValidationResult.InvalidToken -> {
                eventLogRepository.append(EventSeverity.Warn, "Broadcast trigger rejected: missing or invalid token")
            }
        }
    }

    private fun handleAcceptedTrigger(
        eventLogRepository: EventLogRepository,
        vpnBreakController: VpnBreakController,
    ) {
        eventLogRepository.append(EventSeverity.Info, "Broadcast trigger accepted")
        val hasPermission = vpnBreakController.hasVpnPermission()
        eventLogRepository.append(EventSeverity.Debug, "Broadcast trigger VPN permission granted=$hasPermission")
        if (!hasPermission) {
            eventLogRepository.append(EventSeverity.Warn, "Broadcast trigger ignored: VPN permission missing")
            return
        }

        val started = vpnBreakController.startBreakService()
        if (started) {
            eventLogRepository.append(EventSeverity.Info, "Broadcast trigger delegated service start")
        } else {
            eventLogRepository.append(EventSeverity.Error, "Broadcast trigger failed to start VPN service")
        }
    }

    private val TriggerActionState.logValue: String
        get() = when (this) {
            TriggerActionState.Expected -> "expected"
            TriggerActionState.Missing -> "missing"
            TriggerActionState.Unsupported -> "unsupported"
        }
}
