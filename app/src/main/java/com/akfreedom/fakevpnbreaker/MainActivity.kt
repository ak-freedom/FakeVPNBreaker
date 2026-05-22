package com.akfreedom.fakevpnbreaker

import android.app.Activity
import android.app.AlertDialog
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
import com.akfreedom.fakevpnbreaker.logging.DiagnosticCatalog
import com.akfreedom.fakevpnbreaker.logging.DiagnosticCode
import com.akfreedom.fakevpnbreaker.logging.EventLogRepository
import com.akfreedom.fakevpnbreaker.logging.EventSeverity
import com.akfreedom.fakevpnbreaker.macrodroid.MacroTemplateRenderResult
import com.akfreedom.fakevpnbreaker.macrodroid.MacroTemplateRenderer
import com.akfreedom.fakevpnbreaker.settings.AppLanguage
import com.akfreedom.fakevpnbreaker.settings.AppLanguageContext
import com.akfreedom.fakevpnbreaker.settings.AppLanguageStorage
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
    private lateinit var versionText: TextView
    private lateinit var statusText: TextView
    private lateinit var logText: TextView
    private lateinit var triggerActionText: TextView
    private lateinit var triggerTokenText: TextView
    private lateinit var languageSpinner: Spinner
    private lateinit var durationSpinner: Spinner
    private lateinit var routingSpinner: Spinner
    private lateinit var closeAfterTriggerCheck: CheckBox
    private val mainHandler = Handler(Looper.getMainLooper())
    private val pendingLogRefreshes = mutableListOf<Runnable>()
    private var bindingSettings = false

    override fun attachBaseContext(newBase: Context) {
        val language = AppLanguageStorage.get(newBase)
        super.attachBaseContext(AppLanguageContext.wrap(newBase, language))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        eventLogRepository = EventLogRepository(this)
        settingsRepository = SettingsRepository(this, eventLogRepository)
        vpnBreakController = VpnBreakController(this, eventLogRepository)

        statusText = findViewById(R.id.statusText)
        versionText = findViewById(R.id.versionText)
        logText = findViewById(R.id.logText)
        triggerActionText = findViewById(R.id.triggerActionText)
        triggerTokenText = findViewById(R.id.triggerTokenText)
        languageSpinner = findViewById(R.id.languageSpinner)
        durationSpinner = findViewById(R.id.durationSpinner)
        routingSpinner = findViewById(R.id.routingSpinner)
        closeAfterTriggerCheck = findViewById(R.id.closeAfterTriggerCheck)

        setupLanguageSpinner()
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

    private fun setupLanguageSpinner() {
        languageSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            AppLanguage.entries.map { languageLabel(it) },
        )
        languageSpinner.onItemSelectedListener = SimpleItemSelectedListener {
            if (bindingSettings) return@SimpleItemSelectedListener
            val selectedLanguage = AppLanguage.entries[languageSpinner.selectedItemPosition]
            if (selectedLanguage == settingsRepository.getAppLanguage()) return@SimpleItemSelectedListener
            settingsRepository.setAppLanguage(selectedLanguage)
            recreate()
        }
    }

    private fun setupDurationSpinner() {
        durationSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            BreakDuration.entries.map { durationLabel(it) },
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
            RoutingMode.entries.map { routingModeLabel(it) },
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
                } else {
                    eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.ServiceStartFailed))
                }
            } else {
                eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.MissingVpnPermission))
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

        findViewById<Button>(R.id.durationHelpButton).setOnClickListener {
            showHelpDialog(R.string.duration_help_title, R.string.duration_help_message)
        }

        findViewById<Button>(R.id.routingHelpButton).setOnClickListener {
            showHelpDialog(R.string.routing_help_title, R.string.routing_help_message)
        }

        findViewById<Button>(R.id.closeAfterTriggerHelpButton).setOnClickListener {
            showHelpDialog(R.string.close_after_trigger_help_title, R.string.close_after_trigger_help_message)
        }

        findViewById<Button>(R.id.triggerHelpButton).setOnClickListener {
            showHelpDialog(R.string.trigger_help_title, R.string.trigger_help_message)
        }
    }

    private fun bindSettings() {
        bindingSettings = true
        languageSpinner.setSelection(AppLanguage.entries.indexOf(settingsRepository.getAppLanguage()))
        durationSpinner.setSelection(BreakDuration.entries.indexOf(settingsRepository.getBreakDuration()))
        routingSpinner.setSelection(RoutingMode.entries.indexOf(settingsRepository.getRoutingMode()))
        closeAfterTriggerCheck.isChecked = settingsRepository.shouldCloseAfterTrigger()
        bindingSettings = false
    }

    private fun refreshUi() {
        val hasPermission = vpnBreakController.hasVpnPermission()
        versionText.text = getString(R.string.version_label, currentVersionName())
        statusText.setText(if (hasPermission) R.string.status_permission_granted else R.string.status_permission_missing)
        triggerActionText.text = getString(R.string.trigger_action_value, TriggerToken.ACTION_BREAK_VPN)
        triggerTokenText.text = getString(R.string.trigger_token_value, settingsRepository.getTriggerToken())
        logText.text = eventLogRepository.formatForDisplay(getString(R.string.no_events_yet))
    }

    private fun currentVersionName(): String =
        runCatching {
            packageManager.getPackageInfo(packageName, 0).versionName ?: UNKNOWN_VERSION
        }.getOrDefault(UNKNOWN_VERSION)

    private fun refreshLogs() {
        logText.text = eventLogRepository.formatForDisplay(getString(R.string.no_events_yet))
    }

    private fun languageLabel(language: AppLanguage): String =
        when (language) {
            AppLanguage.English -> getString(R.string.language_english)
            AppLanguage.Russian -> getString(R.string.language_russian)
        }

    private fun durationLabel(duration: BreakDuration): String = "${duration.millis} ms"

    private fun routingModeLabel(mode: RoutingMode): String =
        when (mode) {
            RoutingMode.FullTakeover -> getString(R.string.routing_full_takeover)
            RoutingMode.LocalOnly -> getString(R.string.routing_local_only)
        }

    private fun showHelpDialog(titleResId: Int, messageResId: Int) {
        AlertDialog.Builder(this)
            .setTitle(titleResId)
            .setMessage(messageResId)
            .setPositiveButton(R.string.help_dialog_ok, null)
            .show()
    }

    private fun handleVpnPermissionResult() {
        if (vpnBreakController.hasVpnPermission()) {
            eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.VpnPermissionGranted))
        } else {
            eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.VpnPermissionDenied))
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
        runCatching {
            startActivityForResult(intent, REQUEST_CREATE_MACRO_DOCUMENT)
        }.onFailure { error ->
            eventLogRepository.append(
                DiagnosticCatalog.message(
                    DiagnosticCode.MacroPickerLaunchFailed,
                    error.javaClass.simpleName,
                ),
            )
        }
        refreshUi()
    }

    private fun handleMacroDocumentResult(resultCode: Int, data: Intent?) {
        if (resultCode != RESULT_OK) {
            eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.MacroSaveCancelled))
            refreshUi()
            return
        }

        val uri = data?.data
        if (uri == null) {
            eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.MacroMissingUri))
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
            eventLogRepository.append(
                DiagnosticCatalog.message(DiagnosticCode.MacroTemplateFailed, exception.javaClass.simpleName),
            )
            return
        }

        val rendered = MacroTemplateRenderer.render(template, settingsRepository.getTriggerToken())
        if (rendered is MacroTemplateRenderResult.Failure) {
            eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.MacroTemplateFailed))
            return
        }

        val content = (rendered as MacroTemplateRenderResult.Success).content
        try {
            val outputStream = contentResolver.openOutputStream(uri)
            if (outputStream == null) {
                eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.MacroOutputUnavailable))
                return
            }
            outputStream.use {
                it.write(content.toByteArray(StandardCharsets.UTF_8))
            }
            eventLogRepository.append(EventSeverity.Info, "MacroDroid macro saved")
        } catch (exception: IOException) {
            eventLogRepository.append(
                DiagnosticCatalog.message(DiagnosticCode.MacroWriteFailed, exception.javaClass.simpleName),
            )
        } catch (exception: SecurityException) {
            eventLogRepository.append(DiagnosticCatalog.message(DiagnosticCode.MacroPermissionDenied))
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
        const val UNKNOWN_VERSION = "unknown"
    }
}
