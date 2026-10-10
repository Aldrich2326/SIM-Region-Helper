// Modified for Nrfr Next (2026); see NOTICE and CREDITS.md for provenance and changes.
package com.github.nrfr

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.github.nrfr.ui.screens.AboutScreen
import com.github.nrfr.ui.screens.MainScreen
import com.github.nrfr.ui.screens.PermissionScreen
import com.github.nrfr.ui.theme.NrfrTheme
import rikka.shizuku.Shizuku

class MainActivity : ComponentActivity() {
    private var isShizukuReady by mutableStateOf(false)
    private var isPhoneReadReady by mutableStateOf(false)
    private val phonePermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isPhoneReadReady = granted
        if (granted) checkShizukuStatus()
    }
    private val shizukuPermissionListener = Shizuku.OnRequestPermissionResultListener {
        _, grantResult ->
        isShizukuReady = grantResult == PackageManager.PERMISSION_GRANTED
        if (!isShizukuReady) {
            Toast.makeText(this, "需要 Shizuku 权限才能运行", Toast.LENGTH_LONG).show()
        }
    }
    private val shizukuBinderListener = Shizuku.OnBinderReceivedListener {
        runOnUiThread { checkShizukuStatus() }
    }
    private val shizukuDeadListener = Shizuku.OnBinderDeadListener {
        runOnUiThread { isShizukuReady = false }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        isPhoneReadReady = checkSelfPermission(Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED
        if (!isPhoneReadReady) phonePermission.launch(Manifest.permission.READ_PHONE_STATE)
        else checkShizukuStatus()

        // 添加 Shizuku 权限监听器
        Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)

        // 添加 Shizuku 绑定监听器
        Shizuku.addBinderReceivedListener(shizukuBinderListener)
        Shizuku.addBinderDeadListener(shizukuDeadListener)

        setContent {
            NrfrTheme {
                var showAbout by rememberSaveable { mutableStateOf(false) }
                BackHandler(enabled = showAbout) { showAbout = false }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    if (showAbout) {
                        AboutScreen(onBack = { showAbout = false })
                    } else if (isShizukuReady && isPhoneReadReady) {
                        MainScreen(onShowAbout = { showAbout = true })
                    } else {
                        PermissionScreen(
                            phoneReady = isPhoneReadReady,
                            onRetry = {
                                if (!isPhoneReadReady) phonePermission.launch(Manifest.permission.READ_PHONE_STATE)
                                else checkShizukuStatus()
                            },
                            onShowAbout = { showAbout = true }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        isPhoneReadReady = checkSelfPermission(Manifest.permission.READ_PHONE_STATE) ==
            PackageManager.PERMISSION_GRANTED
        if (isPhoneReadReady) checkShizukuStatus(requestIfMissing = false)
    }

    private fun checkShizukuStatus(requestIfMissing: Boolean = true) {
        if (!isPhoneReadReady) return
        isShizukuReady = if (!Shizuku.pingBinder()) {
            Toast.makeText(this, "请先安装并启用 Shizuku", Toast.LENGTH_LONG).show()
            false
        } else {
            val hasPermission = runCatching {
                Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
            }.getOrDefault(false)
            if (!hasPermission && requestIfMissing) {
                Shizuku.requestPermission(0)
            }
            hasPermission
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
        Shizuku.removeBinderReceivedListener(shizukuBinderListener)
        Shizuku.removeBinderDeadListener(shizukuDeadListener)
    }
}
