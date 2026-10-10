// Modified for Nrfr Next (2026); see NOTICE and CREDITS.md for provenance and changes.
package com.github.nrfr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.nrfr.ui.components.*

@Composable
fun PermissionScreen(phoneReady: Boolean, onRetry: () -> Unit, onShowAbout: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
    Box(Modifier.fillMaxSize().safeDrawingPadding(), contentAlignment = Alignment.Center) {
        Column(Modifier.widthIn(max = 560.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)) {
            BrandTitle()
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = MaterialTheme.shapes.large) {
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("准备好，再开始", style = MaterialTheme.typography.headlineMedium)
                    Text("连接 Shizuku 并允许读取 SIM 状态，即可查看和设置地区。",
                        style = MaterialTheme.typography.bodyLarge)
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                SectionHeading("01  电话状态权限", if (phoneReady) "已允许 · 用于读取已激活的 SIM" else "用于显示 SIM 卡列表，不读取联系人或通话记录")
                SectionHeading("02  Shizuku 授权", "先启动 Shizuku，再单独授权 SIM 地区助手。其他版本的授权不会自动继承。")
            }
            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                Text(if (phoneReady) "重新检查并授权" else "允许读取 SIM 状态")
            }
            AttributionFooter(onShowAbout)
        }
    }
    }
}
