package com.myplanetservices.auraquotes.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplanetservices.auraquotes.data.model.CardStyle
import com.myplanetservices.auraquotes.data.model.Quote

@Composable
fun QuoteCard(
    quote: Quote,
    modifier: Modifier = Modifier,
    cardStyle: CardStyle = quote.defaultStyle,
    isHero: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    onShare: () -> Unit = {},
    onOpenDesigner: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    var isLiked by remember(quote.isFavorite) { mutableStateOf(quote.isFavorite) }

    // Heart scale animation on favorite
    val heartScale by animateFloatAsState(
        targetValue = if (isLiked) 1.25f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "heartScale"
    )

    val shape = RoundedCornerShape(28.dp)

    val backgroundModifier = if (cardStyle.gradient != null) {
        Modifier.background(cardStyle.gradient, shape = shape)
    } else {
        Modifier.background(cardStyle.backgroundColor, shape = shape)
    }

    val borderModifier = if (cardStyle.borderColor != Color.Transparent) {
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
                spotColor = if (cardStyle.isDark) Color(0x66000000) else Color(0x22000000)
            )
            .then(backgroundModifier)
            .then(borderModifier)
            .clip(shape)
            .clickable(onClick = onClick)
            .padding(if (isHero) 28.dp else 22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row: Category Badge & Style Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Chip / Flame
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .background(cardStyle.authorColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 5.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = cardStyle.accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = quote.category.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 1.2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = cardStyle.textColor.copy(alpha = 0.85f)
                    )
                }

                // Favorite heart top right
                IconButton(
                    onClick = {
                        isLiked = !isLiked
                        onFavoriteToggle()
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(cardStyle.authorColor.copy(alpha = 0.12f), CircleShape)
                        .testTag("favorite_button_${quote.id}")
                ) {
                    Icon(
                        imageVector = if (quote.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (quote.isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (quote.isFavorite) Color(0xFFEF4444) else cardStyle.textColor.copy(alpha = 0.75f),
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
                tint = cardStyle.accentColor.copy(alpha = 0.6f),
                modifier = Modifier.size(if (isHero) 34.dp else 26.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Dynamic Font Sizing depending on quote length
            val fontSize = when {
                quote.text.length > 180 -> if (isHero) 20.sp else 16.sp
                quote.text.length > 100 -> if (isHero) 24.sp else 19.sp
                quote.text.length > 50 -> if (isHero) 28.sp else 22.sp
                else -> if (isHero) 32.sp else 24.sp
            }
            val lineHeight = (fontSize.value * 1.35).sp

            // Quote Text
            Text(
                text = quote.text,
                fontSize = fontSize,
                lineHeight = lineHeight,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center,
                color = cardStyle.textColor,
                maxLines = if (isHero) 8 else 5,
                overflow = TextOverflow.Ellipsis,
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
                color = cardStyle.authorColor,
                textAlign = TextAlign.Center
            )

            if (quote.isUserCreated) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Personal Reflection",
                    style = MaterialTheme.typography.labelSmall,
                    color = cardStyle.accentColor
                )
            }

            Spacer(modifier = Modifier.height(if (isHero) 24.dp else 16.dp))

            // Bottom Actions Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardStyle.authorColor.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
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
                        tint = cardStyle.textColor.copy(alpha = 0.8f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Copy to Clipboard
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = ClipData.newPlainText("Aura Quote", "“${quote.text}” — ${quote.author}")
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Quote copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("copy_button_${quote.id}")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy Quote",
                        tint = cardStyle.textColor.copy(alpha = 0.8f),
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
                        tint = cardStyle.accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
