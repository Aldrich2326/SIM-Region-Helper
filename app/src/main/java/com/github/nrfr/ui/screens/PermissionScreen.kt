// Modified for SIM Region Helper (2026): authorization status and explicit recovery actions.
package com.github.nrfr.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.github.nrfr.compat.*
import com.github.nrfr.ui.components.*

@Composable
fun PermissionScreen(
    state: AuthorizationState, requesting: Boolean = false,
    onRefresh: () -> Unit = {}, onPhoneRequest: () -> Unit = {}, onPhoneSettings: () -> Unit = {},
    onShizukuRequest: () -> Unit = {}, onOpenShizuku: () -> Unit = {}, onCopy: () -> Unit = {},
    onContinue: () -> Unit = {}, onShowAbout: () -> Unit = {}
) {
    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(Modifier.safeDrawingPadding(), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 560.dp).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                BrandTitle()
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("授权状态", style = MaterialTheme.typography.headlineMedium)
                        Text(if (state.ready) "所需权限已就绪" else "请完成下方授权；两种权限可分别申请。")
                        Text("前台自动刷新 · 不修改国家码", style = MaterialTheme.typography.bodySmall)
                    }
                }
                val connected = state.shizuku != ShizukuAccess.DISCONNECTED && state.shizuku != ShizukuAccess.ERROR
                StatusCard("Shizuku 连接", when (state.shizuku) {
                    ShizukuAccess.DISCONNECTED -> "未连接"
                    ShizukuAccess.ERROR -> "检测失败"
                    else -> "已连接"
                }, "未连接时，请先确认 Shizuku 正在运行，并与本应用安装在同一用户空间。")
                StatusCard("Shizuku 应用授权", when (state.shizuku) {
                    ShizukuAccess.GRANTED -> "已授权"
                    ShizukuAccess.NOT_GRANTED -> "未授权"
                    ShizukuAccess.BLOCKED -> "已拒绝再次申请"
                    else -> "尚无法确认"
                }, "授权需要由本应用发起。若已拒绝再次申请，请打开 Shizuku 管理授权。")
                Button(onClick = onShizukuRequest,
                    enabled = connected && state.shizuku == ShizukuAccess.NOT_GRANTED && !requesting,
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text(if (requesting) "等待授权弹窗，请确认" else "申请 Shizuku 授权")
                }
                OutlinedButton(onClick = onOpenShizuku, modifier = Modifier.fillMaxWidth()) { Text("打开 Shizuku") }
                StatusCard("电话状态权限", if (state.phoneAllowed) "已允许" else "未允许",
                    "用于显示已激活的 SIM，不读取联系人或通话记录。")
                OutlinedButton(onClick = onPhoneRequest, enabled = !state.phoneAllowed,
                    modifier = Modifier.fillMaxWidth()) { Text("申请电话状态权限") }
                TextButton(onClick = onPhoneSettings, modifier = Modifier.fillMaxWidth()) { Text("打开本应用权限设置") }
                if (state.detail.isNotBlank()) Text(state.detail, color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium)
                OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) { Text("重新检测授权") }
                TextButton(onClick = onCopy, modifier = Modifier.fillMaxWidth()) { Text("复制当前授权诊断") }
                if (state.ready) Button(onClick = onContinue, modifier = Modifier.fillMaxWidth()) { Text("进入 SIM 设置") }
                Text("授权通过只表示权限就绪，不代表国家码写入一定成功。", style = MaterialTheme.typography.bodySmall)
                AttributionFooter(onShowAbout)
            }
        }
    }
}

@Composable
private fun StatusCard(title: String, status: String, detail: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.fillMaxWidth().padding(20.dp).semantics { liveRegion = LiveRegionMode.Polite },
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            LabelPill(status)
            Text(detail, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
