package com.akfreedom.fakevpnbreaker.vpn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import com.akfreedom.fakevpnbreaker.logging.DiagnosticCatalog
import com.akfreedom.fakevpnbreaker.logging.DiagnosticCode
import com.akfreedom.fakevpnbreaker.logging.EventLogRepository
import com.akfreedom.fakevpnbreaker.logging.EventSeverity

class VpnBreakController(
    private val context: Context,
    private val eventLogRepository: EventLogRepository = EventLogRepository(context),
) {
    fun prepareVpnIntent(): Intent? {
        val prepareIntent = VpnService.prepare(context)
        val result = if (prepareIntent == null) "granted" else "missing"
        eventLogRepository.append(EventSeverity.Debug, "VPN permission check result=$result")
        return prepareIntent
    }

    fun hasVpnPermission(): Boolean = prepareVpnIntent() == null

    fun requestPermission(activity: Activity, requestCode: Int): VpnPermissionRequestResult {
        val prepareIntent = prepareVpnIntent()
        if (prepareIntent == null) {
            eventLogRepository.append(EventSeverity.Info, "VPN permission already granted")
            return VpnPermissionRequestResult.AlreadyGranted
        }

        eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.VpnPermissionRequestStarted))
        return runCatching {
            activity.startActivityForResult(prepareIntent, requestCode)
            VpnPermissionRequestResult.Started
        }.getOrElse { error ->
            eventLogRepository.append(
                DiagnosticCatalog.message(
                    DiagnosticCode.VpnPermissionLaunchFailed,
                    error.javaClass.simpleName,
                ),
            )
            VpnPermissionRequestResult.LaunchFailed
        }
    }

    fun startBreakService(): Boolean {
        eventLogRepository.append(EventSeverity.Debug, "Creating FakeVpnService start intent")
        val intent = Intent(context, FakeVpnService::class.java)
        return runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
            eventLogRepository.append(EventSeverity.Info, "VPN break service start requested")
            true
        }.getOrElse { error ->
            eventLogRepository.append(
                DiagnosticCatalog.message(DiagnosticCode.ServiceStartFailed, error.javaClass.simpleName),
            )
            false
        }
    }
}

enum class VpnPermissionRequestResult {
    AlreadyGranted,
    Started,
    LaunchFailed,
}
