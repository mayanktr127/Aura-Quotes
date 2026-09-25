package com.example.data.model

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.theme.*

enum class CardStyle(
    val title: String,
    val subtitle: String,
    val backgroundColor: Color,
    val textColor: Color,
    val authorColor: Color,
    val accentColor: Color,
    val borderColor: Color = Color.Transparent,
    val isDark: Boolean = false,
    val gradient: Brush? = null
) {
    NOIR_AURA(
        title = "Noir Aura",
        subtitle = "Obsidian & Gold",
        backgroundColor = CardNoirBg,
        textColor = Color(0xFFFBF7EE),
        authorColor = Color(0xFFC7BFAF),
        accentColor = CardAmberGold,
        borderColor = CardNoirBorder,
        isDark = true,
        gradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF1E1A16), Color(0xFF110F0D))
        )
    ),
    WARM_LINEN(
        title = "Warm Linen",
        subtitle = "Natural Organic",
        backgroundColor = CardLinenBg,
        textColor = CardLinenText,
        authorColor = Color(0xFF786A5E),
        accentColor = Color(0xFFA86745),
        borderColor = Color(0xFFE2D6CB),
        isDark = false
    ),
    ETHEREAL_DAWN(
        title = "Ethereal Dawn",
        subtitle = "Lavender Peach",
        backgroundColor = Color(0xFFEDE4F0),
        textColor = CardDawnText,
        authorColor = Color(0xFF6B5B75),
        accentColor = Color(0xFF8B6FA8),
        borderColor = Color(0x30FFFFFF),
        isDark = false,
        gradient = Brush.linearGradient(
            colors = listOf(CardDawnStart, CardDawnEnd)
        )
    ),
    EARTHY_SAGE(
        title = "Earthy Sage",
        subtitle = "Forest Olive",
        backgroundColor = CardSageBg,
        textColor = CardSageText,
        authorColor = Color(0xFFD4C8B0),
        accentColor = Color(0xFFE5A93C),
        borderColor = Color(0x30FFFFFF),
        isDark = true,
        gradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF455442), Color(0xFF333E31))
        )
    ),
    GOLDEN_OCHRE(
        title = "Golden Ochre",
        subtitle = "Warm Amber Sun",
        backgroundColor = CardOchreBg,
        textColor = CardOchreText,
        authorColor = Color(0xFF3B2A10),
        accentColor = Color(0xFF1A1208),
        borderColor = Color(0x20000000),
        isDark = false,
        gradient = Brush.verticalGradient(
            colors = listOf(Color(0xFFDF9742), Color(0xFFC77E2A))
        )
    ),
    DARK_CINEMATIC(
        title = "Cinematic Night",
        subtitle = "Twilight Slate",
        backgroundColor = Color(0xFF10141D),
        textColor = Color(0xFFF1F5F9),
        authorColor = Color(0xFF94A3B8),
        accentColor = Color(0xFF60A5FA),
        borderColor = Color(0xFF1E293B),
        isDark = true,
        gradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF192231), Color(0xFF0C1017))
        )
    ),
    BOTANICAL_LEAF(
        title = "Botanical Sand",
        subtitle = "Earthy Clay",
        backgroundColor = Color(0xFFEFE8DF),
        textColor = Color(0xFF28231D),
        authorColor = Color(0xFF685E54),
        accentColor = Color(0xFF5A6953),
        borderColor = Color(0xFFDED3C4),
        isDark = false
    ),
    PAPER_PARCHMENT(
        title = "Parchment Book",
        subtitle = "Classic Editorial",
        backgroundColor = CardParchmentBg,
        textColor = Color(0xFF2B221B),
        authorColor = Color(0xFF6E5B4B),
        accentColor = Color(0xFF8B4513),
        borderColor = Color(0xFFDFD1BC),
        isDark = false
    ),
    MINIMAL_STUDIO(
        title = "Pure Minimal",
        subtitle = "Clean Contrast",
        backgroundColor = Color(0xFFFAFAFA),
        textColor = Color(0xFF09090B),
        authorColor = Color(0xFF71717A),
        accentColor = Color(0xFF18181B),
        borderColor = Color(0xFFE4E4E7),
        isDark = false
    ),
    MIDNIGHT_GLASS(
        title = "Midnight Glass",
        subtitle = "Violet Dream",
        backgroundColor = Color(0xFF151022),
        textColor = Color(0xFFF8FAFC),
        authorColor = Color(0xFFC4B5FD),
        accentColor = Color(0xFFA78BFA),
        borderColor = Color(0xFF31264E),
        isDark = true,
        gradient = Brush.verticalGradient(
            colors = listOf(Color(0xFF201736), Color(0xFF100C1B))
        )
    )
}

enum class QuoteFont(
    val displayName: String,
    val fontFamily: FontFamily,
    val fontWeight: FontWeight,
    val fontStyle: FontStyle,
    val letterSpacing: Float = 0f,
    val isAllCaps: Boolean = false
) {
    EDITORIAL_SERIF(
        displayName = "Editorial Serif",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Normal
    ),
    ELEGANT_ITALIC(
        displayName = "Elegant Italic",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Italic
    ),
    MODERN_SANS(
        displayName = "Modern Sans",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontStyle = FontStyle.Normal
    ),
    BOLD_DISPLAY(
        displayName = "Bold Display",
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Bold,
        fontStyle = FontStyle.Normal,
        letterSpacing = 0.5f,
        isAllCaps = true
    ),
    MINIMALIST(
        displayName = "Minimalist",
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Light,
        fontStyle = FontStyle.Normal,
        letterSpacing = 1.2f
    ),
    MONOSPACE_POETIC(
        displayName = "Typewriter",
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Normal,
        fontStyle = FontStyle.Normal
    )
}

enum class CardAspectRatio(
    val label: String,
    val sublabel: String,
    val ratio: Float,
    val widthFactor: Float,
    val heightFactor: Float
) {
    SQUARE_1_1("1:1", "Square / Post", 1f, 1f, 1f),
    PORTRAIT_4_5("4:5", "Portrait", 0.8f, 4f, 5f),
    STORY_9_16("9:16", "Story / Wallpaper", 9f / 16f, 9f, 16f),
    LANDSCAPE_16_9("16:9", "Landscape", 16f / 9f, 16f, 9f)
}
