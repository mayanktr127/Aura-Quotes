package com.myplanetservices.stillwords.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class StillwordsCustomColors(
    val accentText: Color,
    val muted: Color,
    val emptyDotBorder: Color,
    val selectedChipFill: Color,
    val onPhotoText: Color = PhotoTextIvory,
    val onPhotoAuthor: Color = PhotoAuthorIvory,
    val onPhotoCategory: Color = PhotoCategoryAccent,
    val onPhotoCredit: Color = PhotoCreditIvory,
)

val LocalStillwordsCustomColors = staticCompositionLocalOf {
    StillwordsCustomColors(
        accentText = AccentTintDark,
        muted = MutedDark,
        emptyDotBorder = EmptyDotBorderDark,
        selectedChipFill = BrassAlpha12
    )
}

object StillwordsTheme {
    val colors: StillwordsCustomColors
        @Composable
        get() = LocalStillwordsCustomColors.current
}

private val DarkColorScheme = darkColorScheme(
    primary = Brass,
    onPrimary = Ink,
    primaryContainer = BrassAlpha12,
    onPrimaryContainer = AccentTintDark,
    secondary = Stone,
    onSecondary = Ink,
    secondaryContainer = Charcoal,
    onSecondaryContainer = Ivory,
    tertiary = Brass,
    onTertiary = Ink,
    background = Ink,
    onBackground = Ivory,
    surface = Charcoal,
    onSurface = Ivory,
    surfaceVariant = Charcoal,
    onSurfaceVariant = Stone,
    outline = HairlineDark,
    outlineVariant = EmptyDotBorderDark,
    scrim = MutedDark
)

private val LightColorScheme = lightColorScheme(
    primary = DeepBrass,
    onPrimary = Paper,
    primaryContainer = DeepBrassAlpha12,
    onPrimaryContainer = AccentTintLight,
    secondary = Taupe,
    onSecondary = Paper,
    secondaryContainer = Linen,
    onSecondaryContainer = InkText,
    tertiary = DeepBrass,
    onTertiary = Paper,
    background = Paper,
    onBackground = InkText,
    surface = Linen,
    onSurface = InkText,
    surfaceVariant = Linen,
    onSurfaceVariant = Taupe,
    outline = HairlineLight,
    outlineVariant = EmptyDotBorderLight,
    scrim = MutedLight
)

@Composable
fun StillwordsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val customColors = if (darkTheme) {
        StillwordsCustomColors(
            accentText = AccentTintDark,
            muted = MutedDark,
            emptyDotBorder = EmptyDotBorderDark,
            selectedChipFill = BrassAlpha12
        )
    } else {
        StillwordsCustomColors(
            accentText = AccentTintLight,
            muted = MutedLight,
            emptyDotBorder = EmptyDotBorderLight,
            selectedChipFill = DeepBrassAlpha12
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(
        LocalStillwordsCustomColors provides customColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = StillwordsShapes,
            content = content
        )
    }
}
