// Modified for Nrfr Next (2026); see NOTICE and CREDITS.md for provenance and changes.
package com.github.nrfr.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.github.nrfr.BuildConfig
import com.github.nrfr.compat.DeviceDiagnostics
import com.github.nrfr.ui.ProjectInfo
import com.github.nrfr.ui.components.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AboutScreen(onBack: () -> Unit) {
    var license by rememberSaveable { mutableStateOf<String?>(null) }
    Scaffold(topBar = {
        TopAppBar(title = { Text("关于与开源") }, navigationIcon = {
            IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "返回主界面") }
        }, colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background))
    }) { insets ->
        Box(Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.TopCenter) {
            Column(Modifier.widthIn(max = 640.dp).fillMaxWidth().verticalScroll(rememberScrollState())
                .padding(20.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = MaterialTheme.shapes.large) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        BrandTitle()
                        Text("延续开源，让 SIM 地区设置更清晰。", style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            LabelPill(BuildConfig.VERSION_NAME)
                            LabelPill("非官方社区改版")
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeading("作者与致谢", "每一份改进，都有清楚的来源")
                    SourceCard("原作者 · Ackites", "Nrfr 的原始项目与核心功能", "Ackites / Nrfr", ProjectInfo.ORIGINAL, true)
                    SourceCard("兼容改版 · JHR-deve", "Android 17 权限桥与临时配置调用路径", "JHR-deve / Nrfr-Android17", ProjectInfo.COMPATIBILITY)
                    SourceCard("兼容层来源 · Ritel-T", "部分 instrumentation 与权限握手实现，使用 MIT 许可", "Ritel-T / SamsungRegionOverride", ProjectInfo.BRIDGE)
                    SourceCard("本版维护 · Aldrich2326", "界面整理、环境诊断与 Android 16 兼容改进", "Aldrich2326 / Nrfr-Next", ProjectInfo.REPOSITORY)
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeading("开源许可", "许可与来源声明随安装包提供，可离线阅读")
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(8.dp)) {
                            LicenseRow("Apache License 2.0", "Nrfr 主项目与本版修改", "LICENSE-Apache-2.0.txt") { license = it }
                            HorizontalDivider(Modifier.padding(horizontal = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            LicenseRow("MIT License", "SamsungRegionOverride 兼容层", "LICENSE-SamsungRegionOverride-MIT.txt") { license = it }
                            HorizontalDivider(Modifier.padding(horizontal = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)
                            LicenseRow("来源与修改声明", "原作者、改版链路及修改范围", "NOTICE") { license = it }
                        }
                    }
                    RepositoryButton("查看本版源码与发行版", ProjectInfo.REPOSITORY)
                }
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeading("使用范围")
                        Text("通过 Shizuku 调整系统向应用报告的 SIM 国家码；不修改实体 SIM、号码、真实运营商或 IP。设置为临时覆盖，重启或 SIM 刷新后可能失效。",
                            style = MaterialTheme.typography.bodyMedium)
                        Text("先前构建已完成 Android 16 真机启动与权限检查，用户反馈看起来可用。保存、还原和通话/数据影响尚未完成受控验证。其他设备兼容性需分别确认。",
                            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(DeviceDiagnostics.deviceSummary(), style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Text("本项目不是 Ackites 或其他上游作者的官方发行版，也不代表其认可本版修改。原版权与许可声明完整保留。",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
    license?.let { asset -> LicenseDialog(asset) { license = null } }
}

@Composable
private fun SourceCard(title: String, description: String, repository: String, url: String, original: Boolean = false) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = if (original) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            RepositoryButton(repository, url)
        }
    }
}

@Composable
private fun RepositoryButton(text: String, url: String) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    TextButton(onClick = {
        runCatching { uriHandler.openUri(url) }.onFailure {
            Toast.makeText(context, "未找到可打开链接的应用", Toast.LENGTH_SHORT).show()
        }
    }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text(text, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.width(8.dp))
        Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp))
    }
}

@Composable
private fun LicenseRow(title: String, subtitle: String, asset: String, onOpen: (String) -> Unit) {
    TextButton(onClick = { onOpen(asset) }, modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LicenseDialog(asset: String, onClose: () -> Unit) {
    val context = LocalContext.current
    val text = remember(asset) {
        runCatching { context.assets.open(asset).bufferedReader().use { it.readText() } }
            .getOrElse { "许可文件暂时无法读取，请在源码仓库查看。\n${ProjectInfo.REPOSITORY}" }
    }
    BackHandler(onBack = onClose)
    Dialog(onDismissRequest = onClose, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Scaffold(topBar = {
            TopAppBar(title = { Text("许可与声明") }, navigationIcon = {
                IconButton(onClick = onClose) { Icon(Icons.Default.ArrowBack, "关闭许可全文") }
            })
        }) { insets ->
            SelectionContainer(Modifier.fillMaxSize().padding(insets).verticalScroll(rememberScrollState())) {
                Text(text, Modifier.padding(20.dp), style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
