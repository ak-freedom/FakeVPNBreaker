package com.akfreedom.fakevpnbreaker

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.Spinner
import android.widget.TextView
import com.akfreedom.fakevpnbreaker.logging.EventLogRepository
import com.akfreedom.fakevpnbreaker.logging.EventSeverity
import com.akfreedom.fakevpnbreaker.macrodroid.MacroTemplateRenderResult
import com.akfreedom.fakevpnbreaker.macrodroid.MacroTemplateRenderer
import com.akfreedom.fakevpnbreaker.settings.BreakDuration
import com.akfreedom.fakevpnbreaker.settings.RoutingMode
import com.akfreedom.fakevpnbreaker.settings.SettingsRepository
import com.akfreedom.fakevpnbreaker.settings.TriggerToken
import com.akfreedom.fakevpnbreaker.vpn.VpnBreakController
import java.io.IOException
import java.nio.charset.StandardCharsets

class MainActivity : Activity() {
    private lateinit var settingsRepository: SettingsRepository
    private lateinit var eventLogRepository: EventLogRepository
    private lateinit var vpnBreakController: VpnBreakController
    private lateinit var statusText: TextView
    private lateinit var logText: TextView
    private lateinit var triggerActionText: TextView
    private lateinit var triggerTokenText: TextView
    private lateinit var durationSpinner: Spinner
    private lateinit var routingSpinner: Spinner
    private lateinit var closeAfterTriggerCheck: CheckBox
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pendingLogRefreshes = mutableListOf<Runnable>()
    private var bindingSettings = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        eventLogRepository = EventLogRepository(this)
        settingsRepository = SettingsRepository(this, eventLogRepository)
        vpnBreakController = VpnBreakController(this, eventLogRepository)

        statusText = findViewById(R.id.statusText)
        logText = findViewById(R.id.logText)
        triggerActionText = findViewById(R.id.triggerActionText)
        triggerTokenText = findViewById(R.id.triggerTokenText)
        durationSpinner = findViewById(R.id.durationSpinner)
        routingSpinner = findViewById(R.id.routingSpinner)
        closeAfterTriggerCheck = findViewById(R.id.closeAfterTriggerCheck)

        setupDurationSpinner()
        setupRoutingSpinner()
        setupActions()
        bindSettings()
        refreshUi()
    }

    override fun onResume() {
        super.onResume()
        eventLogRepository.append(EventSeverity.Debug, "MainActivity refresh onResume")
        refreshUi()
    }

    override fun onPause() {
        super.onPause()
        clearPendingLogRefreshes()
    }

    @Deprecated("Deprecated in Android framework; kept to avoid AndroidX dependency.")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_VPN_PERMISSION -> handleVpnPermissionResult()
            REQUEST_CREATE_MACRO_DOCUMENT -> handleMacroDocumentResult(resultCode, data)
        }
    }

    private fun setupDurationSpinner() {
        durationSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            BreakDuration.entries.map { it.label },
        )
        durationSpinner.onItemSelectedListener = SimpleItemSelectedListener {
            if (bindingSettings) return@SimpleItemSelectedListener
            settingsRepository.setBreakDuration(BreakDuration.entries[durationSpinner.selectedItemPosition])
            refreshUi()
        }
    }

    private fun setupRoutingSpinner() {
        routingSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            RoutingMode.entries.map { it.label },
        )
        routingSpinner.onItemSelectedListener = SimpleItemSelectedListener {
            if (bindingSettings) return@SimpleItemSelectedListener
            settingsRepository.setRoutingMode(RoutingMode.entries[routingSpinner.selectedItemPosition])
            refreshUi()
        }
    }

    private fun setupActions() {
        findViewById<Button>(R.id.permissionButton).setOnClickListener {
            vpnBreakController.requestPermission(this, REQUEST_VPN_PERMISSION)
            refreshUi()
        }

        findViewById<Button>(R.id.testButton).setOnClickListener {
            eventLogRepository.append(EventSeverity.Info, "Manual test launch requested")
            if (vpnBreakController.hasVpnPermission()) {
                if (vpnBreakController.startBreakService()) {
                    scheduleServiceLogRefreshes()
                }
            } else {
                eventLogRepository.append(EventSeverity.Warn, "Manual test requires VPN permission")
                vpnBreakController.requestPermission(this, REQUEST_VPN_PERMISSION)
            }
            refreshUi()
        }

        closeAfterTriggerCheck.setOnCheckedChangeListener { _, isChecked ->
            if (bindingSettings) return@setOnCheckedChangeListener
            settingsRepository.setCloseAfterTrigger(isChecked)
            refreshUi()
        }

        findViewById<Button>(R.id.clearLogsButton).setOnClickListener {
            eventLogRepository.clear()
            refreshUi()
        }

        findViewById<Button>(R.id.copyTriggerTokenButton).setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("FakeVpnBreaker trigger token", settingsRepository.getTriggerToken()))
            eventLogRepository.append(EventSeverity.Info, "[FIX:trigger-auth] Trigger token copied for MacroDroid setup")
            refreshUi()
        }

        findViewById<Button>(R.id.saveMacroButton).setOnClickListener {
            requestMacroDocumentSave()
        }
    }

    private fun bindSettings() {
        bindingSettings = true
        durationSpinner.setSelection(BreakDuration.entries.indexOf(settingsRepository.getBreakDuration()))
        routingSpinner.setSelection(RoutingMode.entries.indexOf(settingsRepository.getRoutingMode()))
        closeAfterTriggerCheck.isChecked = settingsRepository.shouldCloseAfterTrigger()
        bindingSettings = false
    }

    private fun refreshUi() {
        val hasPermission = vpnBreakController.hasVpnPermission()
        statusText.setText(if (hasPermission) R.string.status_permission_granted else R.string.status_permission_missing)
        triggerActionText.text = getString(R.string.trigger_action_value, TriggerToken.ACTION_BREAK_VPN)
        triggerTokenText.text = getString(R.string.trigger_token_value, settingsRepository.getTriggerToken())
        logText.text = eventLogRepository.formatForDisplay()
    }

    private fun refreshLogs() {
        logText.text = eventLogRepository.formatForDisplay()
    }

    private fun handleVpnPermissionResult() {
        if (vpnBreakController.hasVpnPermission()) {
            eventLogRepository.append(EventSeverity.Info, "VPN permission granted")
        } else {
            eventLogRepository.append(EventSeverity.Warn, "VPN permission missing after consent flow")
        }
        refreshUi()
    }

    private fun requestMacroDocumentSave() {
        eventLogRepository.append(EventSeverity.Info, "MacroDroid macro save requested")
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = MACRO_DOCUMENT_MIME_TYPE
            putExtra(Intent.EXTRA_TITLE, MACRO_DOCUMENT_NAME)
        }
        startActivityForResult(intent, REQUEST_CREATE_MACRO_DOCUMENT)
        refreshUi()
    }

    private fun handleMacroDocumentResult(resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) {
            eventLogRepository.append(EventSeverity.Info, "MacroDroid macro save cancelled")
            refreshUi()
            return
        }

        val uri = data?.data
        if (uri == null) {
            eventLogRepository.append(EventSeverity.Warn, "MacroDroid macro save failed: missing document URI")
            refreshUi()
            return
        }

        saveMacroDocument(uri)
        refreshUi()
    }

    private fun saveMacroDocument(uri: Uri) {
        val template = try {
            readMacroTemplate()
        } catch (exception: IOException) {
            eventLogRepository.append(EventSeverity.Error, "MacroDroid macro save failed: template read error")
            return
        }

        val rendered = MacroTemplateRenderer.render(template, settingsRepository.getTriggerToken())
        if (rendered is MacroTemplateRenderResult.Failure) {
            eventLogRepository.append(EventSeverity.Error, "MacroDroid macro save failed: ${rendered.message}")
            return
        }

        val content = (rendered as MacroTemplateRenderResult.Success).content
        try {
            val outputStream = contentResolver.openOutputStream(uri)
            if (outputStream == null) {
                eventLogRepository.append(EventSeverity.Warn, "MacroDroid macro save failed: document output stream unavailable")
                return
            }
            outputStream.use {
                it.write(content.toByteArray(StandardCharsets.UTF_8))
            }
            eventLogRepository.append(EventSeverity.Info, "MacroDroid macro saved")
        } catch (exception: IOException) {
            eventLogRepository.append(EventSeverity.Error, "MacroDroid macro save failed: document write error")
        } catch (exception: SecurityException) {
            eventLogRepository.append(EventSeverity.Error, "MacroDroid macro save failed: document permission denied")
        }
    }

    private fun readMacroTemplate(): String =
        assets.open(MACRO_TEMPLATE_ASSET).bufferedReader(StandardCharsets.UTF_8).use { reader ->
            reader.readText()
        }

    private fun scheduleServiceLogRefreshes() {
        clearPendingLogRefreshes()
        listOf(250L, 1_250L, 2_500L).forEach { delayMillis ->
            val runnable = Runnable { refreshLogs() }
            pendingLogRefreshes += runnable
            mainHandler.postDelayed(runnable, delayMillis)
        }
    }

    private fun clearPendingLogRefreshes() {
        pendingLogRefreshes.forEach(mainHandler::removeCallbacks)
        pendingLogRefreshes.clear()
    }

    private companion object {
        const val REQUEST_VPN_PERMISSION = 40
        const val REQUEST_CREATE_MACRO_DOCUMENT = 41
        const val MACRO_DOCUMENT_NAME = "VPN_OFF.macro"
        const val MACRO_DOCUMENT_MIME_TYPE = "application/octet-stream"
        const val MACRO_TEMPLATE_ASSET = "macrodroid/VPN_OFF.template.macro"
    }
}
