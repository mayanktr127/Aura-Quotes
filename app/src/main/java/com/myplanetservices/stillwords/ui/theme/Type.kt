package com.myplanetservices.stillwords.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.myplanetservices.stillwords.R

// ============================================================================
// STILLWORDS OFL FONT FAMILIES — docs/DESIGN_SPEC.md Sec. 3
// ============================================================================

val NewsreaderFontFamily = FontFamily(
    Font(R.font.newsreader_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.newsreader_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.newsreader_medium, FontWeight.Medium, FontStyle.Normal)
)

val FigtreeFontFamily = FontFamily(
    Font(R.font.figtree_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.figtree_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.figtree_semibold, FontWeight.SemiBold, FontStyle.Normal)
)

// Legacy alias for existing Compose usages
val SerifFontFamily = NewsreaderFontFamily
val SansFontFamily = FigtreeFontFamily

// ============================================================================
// STILLWORDS TYPOGRAPHY SCALE — docs/DESIGN_SPEC.md Sec. 3
// ============================================================================

val QuoteLargeStyle = TextStyle(
    fontFamily = NewsreaderFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 34.sp,
    lineHeight = 41.sp
)

val QuoteMediumStyle = TextStyle(
    fontFamily = NewsreaderFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 27.sp,
    lineHeight = 35.sp
)

val QuoteSmallStyle = TextStyle(
    fontFamily = NewsreaderFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp,
    lineHeight = 21.sp
)

val ScreenTitleStyle = TextStyle(
    fontFamily = NewsreaderFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 30.sp,
    lineHeight = 34.sp,
    letterSpacing = (-0.3).sp
)

val ScreenTitlePushedStyle = TextStyle(
    fontFamily = NewsreaderFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 26.sp,
    lineHeight = 30.sp,
    letterSpacing = (-0.3).sp
)

val OnboardingTitleStyle = TextStyle(
    fontFamily = NewsreaderFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 36.sp,
    lineHeight = 40.sp
)

val SectionTitleStyle = TextStyle(
    fontFamily = NewsreaderFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 20.sp,
    lineHeight = 26.sp
)

val AuthorStyle = TextStyle(
    fontFamily = NewsreaderFontFamily,
    fontStyle = FontStyle.Italic,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 20.sp
)

val BodyStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp
)

val BodySmallStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp,
    lineHeight = 21.sp
)

val ButtonPrimaryStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp,
    lineHeight = 20.sp
)

val ButtonSecondaryStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 15.sp,
    lineHeight = 20.sp
)

val ChipStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 18.sp
)

val LabelOverlineStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 2.sp
)

val TabLabelStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 14.sp
)

val CaptionStyle = TextStyle(
    fontFamily = FigtreeFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 12.sp,
    lineHeight = 16.sp
)

// ============================================================================
// MATERIAL 3 TYPOGRAPHY BINDING
// ============================================================================

val Typography = Typography(
    displayLarge = QuoteLargeStyle,
    displayMedium = QuoteMediumStyle,
    displaySmall = QuoteSmallStyle,
    headlineLarge = ScreenTitleStyle,
    headlineMedium = ScreenTitlePushedStyle,
    headlineSmall = SectionTitleStyle,
    titleLarge = SectionTitleStyle,
    titleMedium = ButtonPrimaryStyle,
    titleSmall = ChipStyle,
    bodyLarge = BodyStyle,
    bodyMedium = BodySmallStyle,
    bodySmall = CaptionStyle,
    labelLarge = ButtonPrimaryStyle,
    labelMedium = ChipStyle,
    labelSmall = LabelOverlineStyle
)
