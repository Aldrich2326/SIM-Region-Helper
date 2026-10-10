// Modified for Nrfr Next (2026); see NOTICE and CREDITS.md for provenance and changes.
package com.github.nrfr.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// The system font preserves Chinese glyph coverage and accessibility settings.
private fun text(size: Int, height: Int, weight: FontWeight = FontWeight.Normal) = TextStyle(
    fontFamily = FontFamily.Default, fontWeight = weight,
    fontSize = size.sp, lineHeight = height.sp, letterSpacing = 0.sp
)

val Typography = Typography(
    displaySmall = text(36, 44, FontWeight.SemiBold),
    headlineLarge = text(32, 40, FontWeight.SemiBold),
    headlineMedium = text(28, 36, FontWeight.SemiBold),
    headlineSmall = text(24, 32, FontWeight.SemiBold),
    titleLarge = text(22, 30, FontWeight.SemiBold),
    titleMedium = text(16, 24, FontWeight.SemiBold),
    titleSmall = text(14, 20, FontWeight.SemiBold),
    bodyLarge = text(16, 25), bodyMedium = text(14, 22), bodySmall = text(12, 19),
    labelLarge = text(14, 20, FontWeight.Medium),
    labelMedium = text(12, 18, FontWeight.Medium),
    labelSmall = text(12, 18, FontWeight.Medium)
)
