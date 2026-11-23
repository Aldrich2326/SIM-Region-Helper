// Modified for SIM Region Helper (2026): explicit, independent authorization checks.
package com.github.nrfr

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.github.nrfr.compat.*
import com.github.nrfr.ui.screens.*
import com.github.nrfr.ui.theme.NrfrTheme
import rikka.shizuku.Shizuku
import moe.shizuku.server.IShizukuService

class MainActivity : ComponentActivity() {
    private var authorization by mutableStateOf(AuthorizationState())
    private var requesting by mutableStateOf(false)
    private var requestError = ""
    private val handler = Handler(Looper.getMainLooper())
    private val gateway = object : AuthorizationGateway {
        override fun phoneAllowed() = checkSelfPermission(Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
        override fun binderAlive() = Shizuku.pingBinder()
        // The SDK convenience methods cache positive grants/rationale. Query the live
        // service's generated AIDL interface so revocation does not reuse a cached grant.
        override fun permissionGranted() = liveService().checkSelfPermission()
        override fun requestBlocked() = liveService().shouldShowRequestPermissionRationale()
    }
    private fun liveService(): IShizukuService = IShizukuService.Stub.asInterface(
        Shizuku.getBinder() ?: throw IllegalStateException("Shizuku 已断开"))
    private val poll = object : Runnable {
        override fun run() { refreshAuthorization(); handler.postDelayed(this, 2000) }
    }
    private val phonePermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        refreshAuthorization()
    }
    private val permissionListener = Shizuku.OnRequestPermissionResultListener { _, _ ->
        runOnUiThread { requesting = false; refreshAuthorization() }
    }
    private val binderListener = Shizuku.OnBinderReceivedListener {
        runOnUiThread { refreshAuthorization() }
    }
    private val deadListener = Shizuku.OnBinderDeadListener {
        runOnUiThread { requesting = false; refreshAuthorization() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Register before inspecting; sticky delivery also covers an already-received Binder.
        Shizuku.addRequestPermissionResultListener(permissionListener)
        Shizuku.addBinderDeadListener(deadListener)
        Shizuku.addBinderReceivedListenerSticky(binderListener)
        refreshAuthorization()
        setContent {
            NrfrTheme {
                var about by rememberSaveable { mutableStateOf(false) }
                var permissions by rememberSaveable { mutableStateOf(false) }
                BackHandler(enabled = about || (permissions && authorization.ready)) {
                    if (about) about = false else permissions = false
                }
                when {
                    about -> AboutScreen { about = false }
                    authorization.ready && !permissions -> MainScreen(
                        onShowAbout = { about = true }, onShowPermissions = { permissions = true })
                    else -> PermissionScreen(
                        state = authorization, requesting = requesting,
                        onRefresh = { requesting = false; refreshAuthorization() },
                        onPhoneRequest = { phonePermission.launch(Manifest.permission.READ_PHONE_STATE) },
                        onPhoneSettings = { openIntent(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))) },
                        onShizukuRequest = { requestShizuku() },
                        onOpenShizuku = { openShizuku() },
                        onCopy = { copyAuthorization() },
                        onContinue = { permissions = false }, onShowAbout = { about = true }
                    )
                }
            }
        }
    }

    private fun refreshAuthorization() {
        authorization = AuthorizationProbe.inspect(gateway)
        if (authorization.shizuku == ShizukuAccess.GRANTED) requestError = ""
        if (authorization.detail.isEmpty() && requestError.isNotEmpty())
            authorization = authorization.copy(detail = requestError)
        if (authorization.shizuku != ShizukuAccess.NOT_GRANTED) requesting = false
    }

    private fun requestShizuku() {
        refreshAuthorization()
        if (requesting || authorization.shizuku != ShizukuAccess.NOT_GRANTED) return
        requestError = ""
        requesting = true
        try { Shizuku.requestPermission(42) }
        catch (e: Exception) {
            requesting = false
            requestError = "授权请求失败：${e.javaClass.simpleName}: ${e.message.orEmpty()}"
            authorization = authorization.copy(detail = requestError)
        }
    }

    private fun openShizuku() {
        val intent = packageManager.getLaunchIntentForPackage("moe.shizuku.privileged.api")
        if (intent != null) openIntent(intent)
        else Toast.makeText(this, "未找到 Shizuku，请确认已安装在同一用户空间。", Toast.LENGTH_LONG).show()
    }

    private fun openIntent(intent: Intent) {
        runCatching { startActivity(intent) }.onFailure {
            Toast.makeText(this, "无法打开，请从系统设置或桌面手动进入。", Toast.LENGTH_LONG).show()
        }
    }

    private fun copyAuthorization() {
        refreshAuthorization()
        val report = "${DeviceDiagnostics.header()}\nPackage: $packageName\nPhone permission: ${authorization.phoneAllowed}" +
            "\nShizuku: ${authorization.shizuku}\n${authorization.detail}\nAuthorization inspection only; no SIM writes."
        getSystemService(android.content.ClipboardManager::class.java)
            .setPrimaryClip(android.content.ClipData.newPlainText("授权诊断", report))
        Toast.makeText(this, "当前授权诊断已复制", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() { super.onStart(); handler.post(poll) }
    override fun onResume() { super.onResume(); refreshAuthorization() }
    override fun onStop() { handler.removeCallbacks(poll); super.onStop() }
    override fun onDestroy() {
        handler.removeCallbacks(poll)
        Shizuku.removeRequestPermissionResultListener(permissionListener)
        Shizuku.removeBinderReceivedListener(binderListener)
        Shizuku.removeBinderDeadListener(deadListener)
        super.onDestroy()
    }
}
