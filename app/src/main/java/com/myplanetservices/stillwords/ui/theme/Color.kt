package com.myplanetservices.stillwords.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// STILLWORDS DESIGN TOKENS (Ink & Brass Palette) — docs/DESIGN_SPEC.md Sec. 2
// ============================================================================

// Dark Theme (Default)
val Ink = Color(0xFF0F0E0C)                // Background
val Charcoal = Color(0xFF1A1816)           // Surface / Cards / Inputs / Groups
val HairlineDark = Color(0xFF2E2A26)       // Outline / Borders
val Ivory = Color(0xFFF3EDE3)              // OnBackground / Main Text
val Stone = Color(0xFFA69F94)              // Secondary Text
val Brass = Color(0xFFC8A46A)              // Primary / Accent
val AccentTintDark = Color(0xFFE8D3A8)     // Accent text on tinted chips / labels
val MutedDark = Color(0xFF6E675E)          // Chevrons, footnotes
val EmptyDotBorderDark = Color(0xFF4A443D) // Unfilled week dots border

// Light Theme
val Paper = Color(0xFFF7F3EC)              // Background
val Linen = Color(0xFFEFE9DF)              // Surface / Cards
val HairlineLight = Color(0xFFDCD3C4)      // Outline / Borders
val InkText = Color(0xFF1C1A17)            // OnBackground / Main Text
val Taupe = Color(0xFF6B645A)              // Secondary Text
val DeepBrass = Color(0xFF8A6A30)          // Primary / Accent
val AccentTintLight = Color(0xFF6E5426)    // Accent text on tinted chips
val MutedLight = Color(0xFF9A9184)         // Chevrons, footnotes
val EmptyDotBorderLight = Color(0xFFC9BFAE)// Unfilled week dots border

// Shared Accent Alpha
val BrassAlpha12 = Color(0x1FC8A46A)       // Selected chip fill dark (Brass at 12% alpha)
val DeepBrassAlpha12 = Color(0x1F8A6A30)   // Selected chip fill light (Deep brass at 12% alpha)
val BrassAlpha22 = Color(0x38C8A46A)       // Search highlight mark (Brass at 22% alpha - Search.dc.html line 23)

// On-Photo Colours (Same in both themes)
val PhotoTextIvory = Color(0xFFF3EDE3)
val PhotoAuthorIvory = Color(0xB8F3EDE3)   // Ivory at 72% alpha
val PhotoCategoryAccent = Color(0xFFE8D3A8)
val PhotoCreditIvory = Color(0x99F3EDE3)   // Ivory at 60% alpha

// Scrim Gradients over Photos (Ink at specified alpha top -> bottom)
val HeroCardScrim = Brush.verticalGradient(
    0.00f to Color(0x590F0E0C), // 35%
    0.35f to Color(0x0D0F0E0C), // 5%
    0.70f to Color(0x8C0F0E0C), // 55%
    1.00f to Color(0xEB0F0E0C)  // 92%
)

val FullScreenPhotoScrim = Brush.verticalGradient(
    0.00f to Color(0x8C0F0E0C), // 55%
    0.22f to Color(0x1A0F0E0C), // 10%
    0.55f to Color(0x400F0E0C), // 25%
    1.00f to Color(0xF20F0E0C)  // 95%
)

val TileCardScrim = Brush.verticalGradient(
    0.00f to Color(0x1A0F0E0C), // 10%
    1.00f to Color(0xC70F0E0C)  // 78%
)

// Legacy alias mappings for existing screen compatibility
val DarkBackground = Ink
val DarkSurface = Charcoal
val DarkSurfaceVariant = Charcoal
val DarkPrimary = Brass
val DarkOnPrimary = Ink
val DarkSecondary = Stone
val DarkTertiary = Brass
val DarkAccent = Brass
val DarkTextPrimary = Ivory
val DarkTextSecondary = Stone
val DarkTextTertiary = MutedDark
val DarkBorder = HairlineDark

val LightBackground = Paper
val LightSurface = Linen
val LightSurfaceVariant = Linen
val LightPrimary = DeepBrass
val LightOnPrimary = Paper
val LightSecondary = Taupe
val LightTertiary = DeepBrass
val LightAccent = DeepBrass
val LightTextPrimary = InkText
val LightTextSecondary = Taupe
val LightTextTertiary = MutedLight
val LightBorder = HairlineLight

// Preset Quote Style Colors (Preserved for QuoteStyle / Quote Designer backwards compatibility)
val CardNoirBg = Color(0xFF161412)
val CardNoirBorder = Color(0xFF38312A)
val CardLinenBg = Color(0xFFF5EFEB)
val CardLinenText = Color(0xFF2C2520)
val CardSageBg = Color(0xFF3B4738)
val CardSageText = Color(0xFFF5EEDB)
val CardOchreBg = Color(0xFFD48B38)
val CardOchreText = Color(0xFF1A1208)
val CardDawnStart = Color(0xFFDDD2E8)
val CardDawnEnd = Color(0xFFF8D8C8)
val CardDawnText = Color(0xFF25202B)
val CardMidnightBg = Color(0xFF0F172A)
val CardParchmentBg = Color(0xFFEDE3D2)
val CardAmberGold = Color(0xFFE5A93C)
