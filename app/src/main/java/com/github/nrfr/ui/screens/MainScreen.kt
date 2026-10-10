// Modified for Nrfr Next (2026); see NOTICE and CREDITS.md for provenance and changes.
package com.github.nrfr.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.github.nrfr.compat.CountryOverrideCoordinator
import com.github.nrfr.compat.DeviceDiagnostics
import com.github.nrfr.data.CountryPresets
import com.github.nrfr.manager.CarrierConfigManager
import com.github.nrfr.model.SimCardInfo
import com.github.nrfr.ui.components.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun MainScreen(onShowAbout: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboardManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var selectedSubId by rememberSaveable { mutableIntStateOf(-1) }
    var selectedCountryCode by rememberSaveable { mutableStateOf("") }
    var customCountryCode by rememberSaveable { mutableStateOf(false) }
    var showCountrySheet by rememberSaveable { mutableStateOf(false) }
    var refreshTrigger by remember { mutableIntStateOf(0) }
    var isBusy by remember { mutableStateOf(false) }
    var lastResult by rememberSaveable { mutableStateOf("") }
    var resultIsError by rememberSaveable { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshTrigger++
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    val simCards = remember(context, refreshTrigger) { CarrierConfigManager.getSimCards(context) }
    val selectedSim = simCards.find { it.subId == selectedSubId }
        ?: simCards.find { it.isDefaultData } ?: simCards.firstOrNull()
    val restoreTarget = selectedSim?.let {
        CountryOverrideCoordinator.restoreTarget(context, it.subId, it.operatorNumeric)
    }
    val restoreAvailable = selectedSim?.let {
        it.isReady && restoreTarget != null && (CountryOverrideCoordinator.hasSnapshot(context, it.subId)
            || !it.countryIso.equals(restoreTarget, ignoreCase = true))
    } == true

    fun operate(name: String, task: () -> String) {
        if (isBusy) return
        isBusy = true
        lastResult = ""
        scope.launch {
            val result = runCatching { withContext(Dispatchers.IO) { task() } }
            resultIsError = result.isFailure
            lastResult = result.getOrElse {
                val detail = DeviceDiagnostics.failure(it)
                DeviceDiagnostics.save(context, "$name failed", detail)
                "$name 未确认：$detail"
            }
            refreshTrigger++
            isBusy = false
        }
    }

    MainScreenContent(
        simCards = simCards, selectedSim = selectedSim,
        countryCode = selectedCountryCode, customCountry = customCountryCode,
        restoreAvailable = restoreAvailable, busy = isBusy,
        result = lastResult, resultIsError = resultIsError,
        onShowAbout = onShowAbout,
        onSelectSim = { sim ->
            selectedSubId = sim.subId
            selectedCountryCode = ""
            customCountryCode = false
            lastResult = ""
        },
        onChooseCountry = { showCountrySheet = true },
        onCountryInput = { value ->
            if (value.length <= 2 && value.all { it in 'A'..'Z' || it in 'a'..'z' })
                selectedCountryCode = value.uppercase(Locale.ROOT)
        },
        onApply = {
            selectedSim?.let { sim ->
                val target = selectedCountryCode
                operate("保存") {
                    CarrierConfigManager.setCountry(context, sim.subId, target)
                    "已核验：SIM ${sim.slot} 当前国家码为 $target"
                }
            }
        },
        onRestore = {
            selectedSim?.let { sim ->
                operate("还原") {
                    CarrierConfigManager.restoreCountry(context, sim.subId)
                    "已核验：SIM ${sim.slot} 国家码已还原"
                }
            }
        },
        onDiagnose = { operate("检查") { CountryOverrideCoordinator.diagnose(context) } },
        onCopyDiagnostics = {
            clipboard.setText(AnnotatedString(DeviceDiagnostics.latest(context)))
            Toast.makeText(context, "诊断结果已复制", Toast.LENGTH_SHORT).show()
        }
    )
    if (showCountrySheet) {
        CountryPicker(selectedCountryCode, onDismiss = { showCountrySheet = false }) { code, custom ->
            selectedCountryCode = code
            customCountryCode = custom
            showCountrySheet = false
        }
    }
}

/** Pure presentation: previews and UI tests never invoke a telephony service. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MainScreenContent(
    simCards: List<SimCardInfo>, selectedSim: SimCardInfo?, countryCode: String,
    customCountry: Boolean = false, restoreAvailable: Boolean = false, busy: Boolean = false,
    result: String = "", resultIsError: Boolean = false,
    onShowAbout: () -> Unit = {}, onSelectSim: (SimCardInfo) -> Unit = {},
    onChooseCountry: () -> Unit = {}, onCountryInput: (String) -> Unit = {},
    onApply: () -> Unit = {}, onRestore: () -> Unit = {}, onDiagnose: () -> Unit = {},
    onCopyDiagnostics: () -> Unit = {}
) {
    val colors = MaterialTheme.colorScheme
    Scaffold(topBar = {
        TopAppBar(title = { BrandTitle() }, actions = {
            TextButton(onClick = onShowAbout, enabled = !busy) {
                Icon(Icons.Default.Info, null, Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("开源")
            }
        }, colors = TopAppBarDefaults.topAppBarColors(containerColor = colors.background))
    }) { insets ->
        Box(Modifier.fillMaxSize().padding(insets), contentAlignment = Alignment.TopCenter) {
            Column(
                Modifier.widthIn(max = 640.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Card(colors = CardDefaults.cardColors(containerColor = colors.primaryContainer),
                    shape = MaterialTheme.shapes.large) {
                    Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("SIM 地区", style = MaterialTheme.typography.headlineLarge,
                            color = colors.onPrimaryContainer)
                        Text("让应用读取你选择的国家或地区", style = MaterialTheme.typography.bodyLarge,
                            color = colors.onPrimaryContainer)
                        Spacer(Modifier.height(2.dp))
                        LabelPill("免 Root · Shizuku 已连接")
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeading("选择 SIM", "每张卡的地区独立设置")
                    if (simCards.isEmpty()) {
                        Surface(shape = MaterialTheme.shapes.medium, color = colors.surface) {
                            Text("未读取到已激活的 SIM。请检查电话权限与 SIM 状态。",
                                Modifier.fillMaxWidth().padding(20.dp), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    val fontScale = LocalDensity.current.fontScale
                    BoxWithConstraints {
                        if (maxWidth >= 320.dp && fontScale < 1.3f) {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                simCards.chunked(2).forEach { pair ->
                                    Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                        pair.forEach { sim -> SimCardTile(sim, sim.subId == selectedSim?.subId,
                                            !busy, Modifier.weight(1f).fillMaxHeight()) { onSelectSim(sim) } }
                                        if (pair.size == 1 && simCards.size > 1) Spacer(Modifier.weight(1f))
                                    }
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                simCards.forEach { sim -> SimCardTile(sim, sim.subId == selectedSim?.subId,
                                    !busy, Modifier.fillMaxWidth()) { onSelectSim(sim) } }
                            }
                        }
                    }
                }
                Card(colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = MaterialTheme.shapes.large) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        SectionHeading("设置地区", selectedSim?.let { "将应用到 SIM ${it.slot}" } ?: "请先插入并选择 SIM")
                        Surface(onClick = onChooseCountry, enabled = selectedSim != null && !busy,
                            shape = MaterialTheme.shapes.medium,
                            color = colors.surfaceContainerLow, border = BorderStroke(1.dp, colors.outlineVariant)) {
                            Row(Modifier.fillMaxWidth().heightIn(min = 80.dp).padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                CountryBadge(countryCode)
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text(countryName(countryCode), style = MaterialTheme.typography.titleMedium)
                                    Text(if (countryCode.isEmpty()) "点击选择国家或地区" else "国家码 $countryCode",
                                        style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                                }
                                Icon(Icons.Default.ArrowForward, null, Modifier.size(20.dp), tint = colors.primary)
                            }
                        }
                        if (customCountry) {
                            val focus = LocalFocusManager.current
                            OutlinedTextField(value = countryCode, onValueChange = onCountryInput,
                                label = { Text("自定义两位国家码") }, supportingText = { Text("例如 US、JP、SG") },
                                enabled = !busy, singleLine = true, modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }))
                        }
                        Text("仅调整系统报告的 SIM 地区。实体 SIM、真实运营商与 IP 地址保持不变。",
                            style = MaterialTheme.typography.bodyMedium, color = colors.onSurfaceVariant)
                        if (selectedSim != null && !selectedSim.isReady) {
                            Text("这张 SIM 尚未就绪，请稍后再试。", color = colors.error,
                                style = MaterialTheme.typography.bodyMedium)
                        }
                        val sameCountry = selectedSim?.countryIso.equals(countryCode, ignoreCase = true)
                        Button(onClick = onApply, enabled = selectedSim?.isReady == true && !busy &&
                            countryCode.matches(Regex("[A-Z]{2}")) && !sameCountry,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)) {
                            Text(if (busy) "正在处理，请稍候" else if (sameCountry && countryCode.isNotEmpty())
                                "与当前地区相同" else selectedSim?.let { "应用到 SIM ${it.slot}" } ?: "应用地区")
                        }
                        TextButton(onClick = onRestore, enabled = restoreAvailable && !busy,
                            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("还原所选 SIM 地区") }
                        if (busy) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
                if (result.isNotEmpty()) {
                    Surface(shape = MaterialTheme.shapes.medium,
                        color = if (resultIsError) colors.errorContainer else colors.secondaryContainer,
                        contentColor = if (resultIsError) colors.onErrorContainer else colors.onSecondaryContainer) {
                        Column(Modifier.fillMaxWidth().padding(20.dp).semantics { liveRegion = LiveRegionMode.Polite },
                            verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (resultIsError) "需要处理" else "操作结果", style = MaterialTheme.typography.titleSmall)
                            Text(result, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionHeading("环境与诊断", "检查只读取环境信息，不修改国家码")
                    OutlinedButton(onClick = onDiagnose, enabled = !busy,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("检查环境") }
                    TextButton(onClick = onCopyDiagnostics, enabled = !busy,
                        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) { Text("复制诊断") }
                    Text("设置为临时覆盖，重启或 SIM 刷新后可能失效。不同系统的兼容情况见关于页。",
                        style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
                }
                AttributionFooter(onShowAbout, enabled = !busy)
            }
        }
    }
}

@Composable
private fun SimCardTile(sim: SimCardInfo, selected: Boolean, enabled: Boolean,
    modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    Surface(modifier = modifier.selectable(selected = selected, enabled = enabled, role = Role.RadioButton,
        onClick = onClick), shape = MaterialTheme.shapes.medium,
        color = if (selected) colors.secondaryContainer else colors.surface,
        border = BorderStroke(1.dp, if (selected) colors.primary else colors.outlineVariant)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("SIM ${sim.slot}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                RadioButton(selected = selected, onClick = null, enabled = enabled, modifier = Modifier.size(24.dp))
            }
            Text(sim.countryIso.ifBlank { "—" }, style = MaterialTheme.typography.headlineLarge, color = colors.primary)
            Text(sim.carrierName.ifBlank { "未知运营商" }, style = MaterialTheme.typography.bodyMedium)
            Text(if (sim.isReady) "SIM 已就绪" else "SIM 未就绪",
                style = MaterialTheme.typography.bodySmall, color = colors.onSurfaceVariant)
            if (sim.isDefaultData) LabelPill("默认数据卡", emphasized = selected)
        }
    }
}

@Composable
private fun CountryBadge(code: String) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
        Box(Modifier.sizeIn(minWidth = 48.dp, minHeight = 48.dp).padding(8.dp), contentAlignment = Alignment.Center) {
            Text(code.ifBlank { "—" }, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun countryName(code: String) = CountryPresets.countries.find { it.code == code }?.name
    ?: if (code.isEmpty()) "选择目标地区" else "自定义地区"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun CountryPicker(selectedCode: String, onDismiss: () -> Unit,
    onSelect: (String, Boolean) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    val countries = remember(query) { CountryPresets.countries.filter {
        it.name.contains(query.trim(), ignoreCase = true) || it.code.contains(query.trim(), ignoreCase = true)
    } }
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("选择国家或地区", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, "关闭地区选择") }
            }
            OutlinedTextField(value = query, onValueChange = { query = it }, singleLine = true,
                label = { Text("搜索名称或国家码") }, leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier.fillMaxWidth())
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 480.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                item {
                    TextButton(onClick = { onSelect("", true) }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                        Text("输入自定义国家码")
                    }
                }
                if (countries.isEmpty()) item {
                    Text("没有匹配的地区，可输入自定义国家码。", Modifier.padding(vertical = 20.dp),
                        style = MaterialTheme.typography.bodyMedium)
                }
                items(countries, key = { it.code }) { country ->
                    Surface(onClick = { onSelect(country.code, false) }, shape = MaterialTheme.shapes.small,
                        color = if (country.code == selectedCode) MaterialTheme.colorScheme.secondaryContainer
                            else MaterialTheme.colorScheme.surface) {
                        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            CountryBadge(country.code)
                            Text(country.name, Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
                            if (country.code == selectedCode) Icon(Icons.Default.Check, "当前选中的地区")
                        }
                    }
                }
            }
        }
    }
}
