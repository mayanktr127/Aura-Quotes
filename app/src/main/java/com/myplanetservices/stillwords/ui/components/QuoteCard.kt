package com.myplanetservices.stillwords.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.myplanetservices.stillwords.data.model.CardStyle
import com.myplanetservices.stillwords.data.model.Quote
import com.myplanetservices.stillwords.data.repository.BackgroundRepository

@Composable
fun QuoteCard(
    quote: Quote,
    modifier: Modifier = Modifier,
    cardStyle: CardStyle = quote.defaultStyle,
    photoAssetPath: String? = null,
    usePhotoBackground: Boolean = true,
    isHero: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    onShare: () -> Unit = {},
    onOpenDesigner: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val backgroundRepo = remember(context) { BackgroundRepository(context) }
    val effectivePhotoPath = if (usePhotoBackground) {
        photoAssetPath ?: remember(quote.id, quote.category) {
            backgroundRepo.getBackgroundAssetPath(quote.id, quote.category)
        }
    } else null

    var hasImageError by remember(effectivePhotoPath) { mutableStateOf(false) }
    val isPhotoActive = effectivePhotoPath != null && !hasImageError

    var isLiked by remember(quote.isFavorite) { mutableStateOf(quote.isFavorite) }

    // Heart scale animation on favorite
    val heartScale by animateFloatAsState(
        targetValue = if (isLiked) 1.25f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "heartScale"
    )

    val shape = RoundedCornerShape(28.dp)

    // Palette resolution: photo mode uses high-contrast warm white & gold (WCAG AA > 4.5:1), fallback uses cardStyle
    val textColor = if (isPhotoActive) Color(0xFFFBF7EE) else cardStyle.textColor
    val authorColor = if (isPhotoActive) Color(0xFFD4C8B0) else cardStyle.authorColor
    val accentColor = if (isPhotoActive) Color(0xFFE5A93C) else cardStyle.accentColor

    val backgroundModifier = if (isPhotoActive) {
        Modifier.background(Color(0xFF141210), shape = shape)
    } else if (cardStyle.gradient != null) {
        Modifier.background(cardStyle.gradient, shape = shape)
    } else {
        Modifier.background(cardStyle.backgroundColor, shape = shape)
    }

    val borderModifier = if (isPhotoActive) {
        Modifier.border(1.2.dp, Color(0x33E5A93C), shape = shape)
    } else if (cardStyle.borderColor != Color.Transparent) {
        Modifier.border(1.2.dp, cardStyle.borderColor, shape = shape)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .testTag("quote_card_${quote.id}")
            .shadow(
                elevation = if (isHero) 12.dp else 4.dp,
                shape = shape,
                spotColor = if (isPhotoActive || cardStyle.isDark) Color(0x66000000) else Color(0x22000000)
            )
            .then(backgroundModifier)
            .then(borderModifier)
            .clip(shape)
            .clickable(onClick = onClick)
    ) {
        // Layer 1: Photo background (Coil with memory caching)
        if (isPhotoActive) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(backgroundRepo.getCoilUri(effectivePhotoPath!!))
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape),
                onError = { hasImageError = true }
            )

            // Layer 2: Dark gradient scrim (lighter at top, darker at bottom, ensures WCAG AA 4.5:1 contrast)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(shape)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0x66000000), // 40% black at top
                                Color(0x99000000), // 60% black at center
                                Color(0xDD000000)  // 87% black at bottom
                            )
                        )
                    )
            )
        }

        // Layer 3: Foreground content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isHero) 24.dp else 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Category Badge & Style Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip / Flame
                val badgeBg = if (isPhotoActive) Color(0x55000000) else cardStyle.authorColor.copy(alpha = 0.15f)
                val badgeBorder = if (isPhotoActive) Modifier.border(1.dp, Color(0x33E5A93C), RoundedCornerShape(12.dp)) else Modifier
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .then(badgeBorder)
                        .background(badgeBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = quote.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = textColor.copy(alpha = 0.9f)
                    )
                }

                // Favorite heart top right
                val favBg = if (isPhotoActive) Color(0x55000000) else cardStyle.authorColor.copy(alpha = 0.12f)
                val favBorder = if (isPhotoActive) Modifier.border(1.dp, Color(0x22FFFFFF), CircleShape) else Modifier
                IconButton(
                    onClick = {
                        isLiked = !isLiked
                        onFavoriteToggle()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .then(favBorder)
                        .background(favBg, CircleShape)
                        .testTag("favorite_button_${quote.id}")
                ) {
                    Icon(
                        imageVector = if (quote.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (quote.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (quote.isFavorite) Color(0xFFEF4444) else textColor.copy(alpha = 0.85f),
                        modifier = Modifier
                            .size(18.dp)
                            .scale(heartScale)
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isHero) 24.dp else 16.dp))

            // Quotation mark icon
            Icon(
                imageVector = Icons.Default.FormatQuote,
                contentDescription = null,
                tint = accentColor.copy(alpha = if (isPhotoActive) 0.85f else 0.6f),
                modifier = Modifier.size(if (isHero) 34.dp else 26.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic Font Sizing: longer quotes auto-shrink down to a sensible minimum size
            // Scale compensation ensures text at fontScale up to 1.3x remains perfectly proportioned
            val density = LocalDensity.current
            val fontScale = density.fontScale.coerceIn(1.0f, 1.3f)
            val scaleCompensation = 1.0f / (1.0f + (fontScale - 1.0f) * 0.5f)
            val baseSp = when {
                quote.text.length > 180 -> if (isHero) 16f else 13.5f
                quote.text.length > 130 -> if (isHero) 18.5f else 15f
                quote.text.length > 80  -> if (isHero) 21.5f else 17.5f
                quote.text.length > 40  -> if (isHero) 25f else 20.5f
                else                    -> if (isHero) 29f else 23.5f
            }
            val fontSize = (baseSp * scaleCompensation).coerceAtLeast(12.5f).sp
            val lineHeight = (fontSize.value * 1.38f).sp

            // Quote Text: Never truncated with "...", never hidden or capped by maxLines
            Text(
                text = quote.text,
                fontSize = fontSize,
                lineHeight = lineHeight,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = textColor,
                softWrap = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Author text
            Text(
                text = "— ${quote.author}",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontStyle = FontStyle.Italic,
                    fontFamily = FontFamily.Serif
                ),
                color = authorColor,
                textAlign = TextAlign.Center
            )

            if (quote.isUserCreated) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Personal Reflection",
                    style = MaterialTheme.typography.labelSmall,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(if (isHero) 24.dp else 16.dp))

            // Bottom Actions Bar
            val barBg = if (isPhotoActive) Color(0x55000000) else cardStyle.authorColor.copy(alpha = 0.08f)
            val barBorder = if (isPhotoActive) Modifier.border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(16.dp)) else Modifier
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(barBorder)
                    .background(barBg, RoundedCornerShape(16.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Share
                IconButton(
                    onClick = onShare,
                    modifier = Modifier.testTag("share_button_${quote.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Quote",
                        tint = textColor.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Copy to Clipboard
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Stillwords Quote", "“${quote.text}” — ${quote.author}")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Quote copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("copy_button_${quote.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy Quote",
                        tint = textColor.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Open in Designer / Style
                IconButton(
                    onClick = onOpenDesigner,
                    modifier = Modifier.testTag("designer_button_${quote.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Palette,
                        contentDescription = "Style in Creator",
                        tint = accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

