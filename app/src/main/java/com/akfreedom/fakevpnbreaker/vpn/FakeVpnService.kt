package com.akfreedom.fakevpnbreaker.vpn

import android.app.Service
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.ParcelFileDescriptor
import com.akfreedom.fakevpnbreaker.logging.DiagnosticCatalog
import com.akfreedom.fakevpnbreaker.logging.DiagnosticCode
import com.akfreedom.fakevpnbreaker.logging.EventLogRepository
import com.akfreedom.fakevpnbreaker.logging.EventSeverity
import com.akfreedom.fakevpnbreaker.settings.RoutingMode
import com.akfreedom.fakevpnbreaker.settings.SettingsRepository

class FakeVpnService : VpnService() {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var vpnInterface: ParcelFileDescriptor? = null
    private var cleanupRunnable: Runnable? = null
    private var foregroundStarted = false
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var eventLogRepository: EventLogRepository

    override fun onCreate() {
        super.onCreate()
        eventLogRepository = EventLogRepository(this)
        settingsRepository = SettingsRepository(this, eventLogRepository)
        eventLogRepository.append(EventSeverity.Debug, "FakeVpnService created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        eventLogRepository.append(EventSeverity.Debug, "FakeVpnService onStartCommand startId=$startId")
        val foregroundStartSucceeded = runCatching {
            startForeground(VpnNotification.NOTIFICATION_ID, VpnNotification.create(this))
        }.onFailure { error ->
            eventLogRepository.append(
                DiagnosticCatalog.message(DiagnosticCode.ForegroundStartFailed, error.javaClass.simpleName),
            )
        }.isSuccess

        if (!foregroundStartSucceeded) {
            cleanup("Foreground start failed")
            return Service.START_NOT_STICKY
        }

        foregroundStarted = true
        startDummySession()
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        eventLogRepository.append(EventSeverity.Debug, "FakeVpnService onDestroy cleanup requested")
        cleanup("Service destroyed")
        super.onDestroy()
    }

    private fun startDummySession() {
        val previousInterface = vpnInterface
        if (previousInterface != null) {
            eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.RepeatedStart))
            cleanup("Repeated start cleanup", stopService = false)
        }

        val duration = settingsRepository.getBreakDuration()
        val routingMode = settingsRepository.getRoutingMode()
        eventLogRepository.append(
            EventSeverity.Debug,
            "Starting dummy VPN session duration=${duration.label}, routingMode=${routingMode.name}",
        )

        runCatching {
            val builder = Builder()
                .setSession("FakeVpnBreaker")
                .setMtu(1500)
                .addAddress(DUMMY_ADDRESS, DUMMY_PREFIX_LENGTH)
                .setBlocking(false)

            if (routingMode == RoutingMode.FullTakeover) {
                builder.addRoute(DEFAULT_ROUTE, DEFAULT_ROUTE_PREFIX_LENGTH)
            }

            val establishedInterface = builder.establish()
            if (establishedInterface == null) {
                eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.VpnEstablishFailed))
                cleanup("VPN establish failed")
                return
            }

            vpnInterface = establishedInterface
            eventLogRepository.append(EventSeverity.Info, "Dummy VPN established")
            scheduleCleanup(duration.millis)
        }.onFailure { error ->
            eventLogRepository.append(
                DiagnosticCatalog.message(DiagnosticCode.VpnEstablishFailed, error.javaClass.simpleName),
            )
            cleanup("VPN start exception")
        }
    }

    private fun scheduleCleanup(durationMillis: Long) {
        eventLogRepository.append(EventSeverity.Debug, "Scheduling dummy VPN cleanup in ${durationMillis}ms")
        val runnable = Runnable { cleanup("Hold duration elapsed") }
        cleanupRunnable = runnable
        mainHandler.postDelayed(runnable, durationMillis)
    }

    private fun cleanup(reason: String, stopService: Boolean = true) {
        cleanupRunnable?.let(mainHandler::removeCallbacks)
        cleanupRunnable = null

        runCatching {
            vpnInterface?.close()
        }.onFailure { error ->
            eventLogRepository.append(EventSeverity.Error, "Failed to close VPN interface: ${error.javaClass.simpleName}")
        }
        if (vpnInterface != null) {
            eventLogRepository.append(EventSeverity.Info, "Dummy VPN closed: $reason")
        }
        vpnInterface = null

        if (!stopService) {
            eventLogRepository.append(EventSeverity.Debug, "[FIX:repeated-start] Previous dummy VPN session closed without stopping service")
            return
        }

        if (foregroundStarted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(STOP_FOREGROUND_REMOVE)
            } else {
                @Suppress("DEPRECATION")
                stopForeground(true)
            }
            foregroundStarted = false
        }
        eventLogRepository.append(EventSeverity.Info, "FakeVpnService stopped")
        stopSelf()
    }

    private companion object {
        const val DUMMY_ADDRESS = "10.255.0.1"
        const val DUMMY_PREFIX_LENGTH = 32
        const val DEFAULT_ROUTE = "0.0.0.0"
        const val DEFAULT_ROUTE_PREFIX_LENGTH = 0
    }
}
