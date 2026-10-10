// Modified for Nrfr Next (2026); see NOTICE and CREDITS.md for provenance and changes.
package com.github.nrfr.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.Density
import androidx.test.core.app.ApplicationProvider
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage
import com.github.nrfr.model.SimCardInfo
import com.github.nrfr.ui.screens.AboutScreen
import com.github.nrfr.ui.screens.MainScreenContent
import com.github.nrfr.ui.screens.PermissionScreen
import com.github.nrfr.ui.theme.NrfrTheme
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/** Synthetic SIM data only: these tests cannot call the permission bridge or write a SIM. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w393dp-h852dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class InterfaceTest {
    @get:Rule val compose = createComposeRule()
    private val sim = SimCardInfo(1, 1, "示例运营商", "CN", "46000", "CN", false, true)
    private val dataSim = sim.copy(slot = 2, subId = 2, isDefaultData = true)

    @Test fun regionSelectionNeverAppliesUntilExplicitButtonPress() {
        var writes = 0
        var selections = 0
        compose.setContent { NrfrTheme {
            MainScreenContent(listOf(sim, dataSim), dataSim, "US", onApply = { writes++ },
                onChooseCountry = { selections++ })
        } }
        compose.onNodeWithText("美国").performScrollTo().performClick()
        assertEquals(1, selections)
        assertEquals(0, writes)
        compose.onNodeWithText("应用到 SIM 2", substring = false).performScrollTo().performClick()
        assertEquals(1, writes)
    }

    @Test fun noSimPreventsApply() {
        compose.setContent { NrfrTheme { MainScreenContent(emptyList(), null, "US") } }
        compose.onNodeWithText("应用地区").performScrollTo().assertIsNotEnabled()
    }

    @Test fun notReadySimPreventsApply() {
        val notReady = sim.copy(isReady = false)
        compose.setContent { NrfrTheme { MainScreenContent(listOf(notReady), notReady, "US") } }
        compose.onNodeWithText("应用到 SIM 1", substring = false).performScrollTo().assertIsNotEnabled()
    }

    @Test fun unchangedCountryPreventsRedundantWrite() {
        compose.setContent { NrfrTheme { MainScreenContent(listOf(sim), sim, "CN") } }
        compose.onNodeWithText("与当前地区相同").performScrollTo().assertIsNotEnabled()
    }

    @Test fun busyStateDisablesApplyAndRestore() {
        compose.setContent { NrfrTheme {
            MainScreenContent(listOf(sim), sim, "US", restoreAvailable = true, busy = true)
        } }
        compose.onNodeWithText("正在处理，请稍候").performScrollTo().assertIsNotEnabled()
        compose.onNodeWithText("还原所选 SIM 地区").performScrollTo().assertIsNotEnabled()
    }

    @Test fun sourceAttributionAndOfflineLicensesRemainAccessible() {
        compose.setContent { NrfrTheme { AboutScreen {} } }
        listOf("原作者 · Ackites", "兼容改版 · JHR-deve", "兼容层来源 · Ritel-T", "本版维护 · Aldrich2326")
            .forEach { compose.onNodeWithText(it).performScrollTo().assertIsDisplayed() }
        compose.onNodeWithText("Apache License 2.0").performScrollTo().performClick()
        compose.onNodeWithText("TERMS AND CONDITIONS FOR USE", substring = true).assertExists()
        compose.onNodeWithContentDescription("关闭许可全文").performClick()
        compose.onNodeWithText("MIT License").performScrollTo().performClick()
        compose.onNodeWithText("Permission is hereby granted", substring = true).assertExists()
        val context = ApplicationProvider.getApplicationContext<Application>()
        assertTrue(context.assets.open("NOTICE").bufferedReader().use { it.readText() }.contains(ProjectInfo.ORIGINAL))
        assertTrue(context.assets.open("LICENSE-SamsungRegionOverride-MIT.txt").bufferedReader()
            .use { it.readText() }.contains("Copyright"))
    }

    @Test fun lightPortraitPreview() = preview("main-light", dark = false)
    @Test fun darkPortraitPreview() = preview("main-dark", dark = true)

    @Test @Config(qualifiers = "w375dp-h812dp-xhdpi")
    fun smallPhonePreview() = preview("main-small", dark = false)

    @Test @Config(qualifiers = "w852dp-h393dp-land-xhdpi")
    fun landscapePreview() = preview("main-landscape", dark = false)

    @Test @Config(qualifiers = "w800dp-h1100dp-xhdpi")
    fun tabletPreview() = preview("main-tablet", dark = false)

    @Test fun largestFontPreview() = preview("main-large-font", dark = true, fontScale = 2f)

    @Test fun aboutPreview() {
        compose.setContent { NrfrTheme { AboutScreen {} } }
        compose.onNodeWithText("原作者 · Ackites").performScrollTo().assertIsDisplayed()
        screenshot("about-sources")
    }

    @Test fun permissionPreviewIncludesOriginalAuthor() {
        compose.setContent { NrfrTheme { PermissionScreen(false, {}, {}) } }
        compose.onNodeWithText("源自 Ackites / Nrfr").assertExists()
        screenshot("permission")
    }

    private fun preview(name: String, dark: Boolean, fontScale: Float = 1f) {
        compose.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale)) {
                NrfrTheme(darkTheme = dark) { MainScreenContent(listOf(sim, dataSim), dataSim, "US") }
            }
        }
        screenshot(name)
        // Ensure the primary control remains reachable at each width/orientation/font scale.
        compose.onNodeWithText("应用到 SIM 2", substring = false).performScrollTo().assertIsDisplayed().assertIsEnabled()
    }

    private fun screenshot(name: String) {
        val output = File("build/ui-previews").apply { mkdirs() }
        // Draw the Android view directly: PixelCopy's redraw callback requires a device
        // frame scheduler and times out in this Robolectric/Compose combination.
        val bitmap = compose.runOnIdle {
            val activity = ActivityLifecycleMonitorRegistry.getInstance()
                .getActivitiesInStage(Stage.RESUMED).single()
            val view = activity.findViewById<View>(android.R.id.content)
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888).also {
                view.draw(Canvas(it))
            }
        }
        File(output, "$name.png").outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
    }
}
