package com.myplanetservices.stillwords.ui.components

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.myplanetservices.stillwords.data.repository.BackgroundRepository
import com.myplanetservices.stillwords.ui.theme.*

// ============================================================================
// 1. PRIMARY BUTTON — docs/DESIGN_SPEC.md Sec. 5
// 52–56 dp tall, radius 16, accent fill (Brass), onPrimary text Figtree 600
// ============================================================================
@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 54.dp,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .height(height),
        shape = ButtonShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 6.dp)
            )
        }
        Text(
            text = text,
            style = ButtonPrimaryStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ============================================================================
// 2. SECONDARY BUTTON — docs/DESIGN_SPEC.md Sec. 5
// Same size, transparent, 1 dp outline, main text colour (Figtree 500)
// ============================================================================
@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 54.dp,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .height(height),
        shape = ButtonShape,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 6.dp)
            )
        }
        Text(
            text = text,
            style = ButtonSecondaryStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ============================================================================
// 3. TEXT BUTTON — docs/DESIGN_SPEC.md Sec. 5
// 48 dp tall, secondary text colour, no border ("Skip", "Not now")
// ============================================================================
@Composable
fun StillwordsTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .heightIn(min = 48.dp)
            .height(48.dp),
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        ),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            style = ButtonSecondaryStyle,
            textAlign = TextAlign.Center
        )
    }
}

// ============================================================================
// 4. CHIP — docs/DESIGN_SPEC.md Sec. 5
// 40–42 dp tall, horizontal padding 16, fully round.
// Off: outline border, main text.
// On: accent border, accent 12% fill, accent-text colour.
// ============================================================================
@Composable
fun StillwordsChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    icon: ImageVector? = null
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    val backgroundColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val textColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    Box(
        modifier = modifier
            .heightIn(min = 40.dp)
            .height(40.dp)
            .clip(ChipShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, ChipShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Text(
                text = text,
                style = ChipStyle,
                color = textColor,
                maxLines = 1
            )
        }
    }
}

// ============================================================================
// 5. SEGMENTED CONTROL — docs/DESIGN_SPEC.md Sec. 5
// Surface fill, outline border, radius 14, 4 dp inner padding.
// Selected segment: outline-colour fill + main text; others transparent + secondary text.
// ============================================================================
@Composable
fun SegmentedControl(
    items: List<String>,
    selectedIndex: Int,
    onItemSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(SegmentedControlShape)
            .background(containerColor)
            .border(1.dp, MaterialTheme.colorScheme.outline, SegmentedControlShape)
            .padding(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEachIndexed { index, title ->
                val isSelected = index == selectedIndex
                val segmentBackground = if (isSelected) MaterialTheme.colorScheme.outline else Color.Transparent
                val segmentTextColor = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(34.dp)
                        .clip(SegmentedItemShape)
                        .background(segmentBackground)
                        .clickable { onItemSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        style = ChipStyle.copy(fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium),
                        color = segmentTextColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// ============================================================================
// 6. SETTINGS GROUP & ROW — docs/DESIGN_SPEC.md Sec. 5
// Surface card, radius 18, rows >= 54 dp, hairline between rows,
// value text right-aligned in secondary colour, chevron in muted colour.
// ============================================================================
@Composable
fun SettingsGroup(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(GroupShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, GroupShape)
    ) {
        content()
    }
}

@Composable
fun SettingsRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    valueText: String? = null,
    showChevron: Boolean = false,
    showDivider: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 54.dp)
                .clickable(enabled = onClick != null, onClick = { onClick?.invoke() })
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = title,
                    style = BodyStyle.copy(color = MaterialTheme.colorScheme.onBackground)
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = CaptionStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            if (valueText != null) {
                Text(
                    text = valueText,
                    style = BodySmallStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (trailingContent != null) {
                Box(modifier = Modifier.padding(start = 12.dp)) {
                    trailingContent()
                }
            } else if (showChevron) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                    contentDescription = null,
                    tint = StillwordsTheme.colors.muted,
                    modifier = Modifier
                        .size(18.dp)
                        .padding(start = 6.dp)
                )
            }
        }

        if (showDivider) {
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

// ============================================================================
// 7. STILLWORDS SWITCH — docs/DESIGN_SPEC.md Sec. 5
// 48 x 28 dp, accent track when on, knob 22 dp (Ink on Brass when on;
// secondary colour on outline track when off)
// ============================================================================
@Composable
fun StillwordsSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        animationSpec = tween(durationMillis = 150),
        label = "switchTrackColor"
    )

    val knobColor by animateColorAsState(
        targetValue = if (checked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 150),
        label = "switchKnobColor"
    )

    val knobOffset by animateDpAsState(
        targetValue = if (checked) 20.dp else 0.dp,
        animationSpec = tween(durationMillis = 150),
        label = "switchKnobOffset"
    )

    Box(
        modifier = modifier
            .width(48.dp)
            .height(28.dp)
            .clip(CircleShape)
            .background(trackColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                role = Role.Switch
            ) { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = knobOffset)
                .size(22.dp)
                .clip(CircleShape)
                .background(knobColor)
        )
    }
}

// ============================================================================
// 8. PHOTO QUOTE CARD — docs/DESIGN_SPEC.md Sec. 5 & PHASE 3.5 SAFE DECODE
// Photo + scrim + category label (top-left) + "Photo · Pexels" (top-right) +
// quote (Newsreader 27 auto-shrink) + author + action row.
// ============================================================================
@Composable
fun PhotoQuoteCard(
    quoteText: String,
    author: String,
    category: String,
    photoPath: String?,
    modifier: Modifier = Modifier,
    credit: String = "Photo · Pexels",
    isFavorite: Boolean = false,
    onFavoriteToggle: () -> Unit = {},
    onShare: () -> Unit = {},
    onDownload: () -> Unit = {},
    onEdit: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val backgroundRepo = remember(context) { BackgroundRepository(context) }
    var hasImageError by remember(photoPath) { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(440.dp)
            .clip(HeroCardShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, HeroCardShape)
            .clickable(onClick = onClick)
    ) {
        // Photo background with Phase 3.5 display-size memory cache optimization
        if (photoPath != null && !hasImageError) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(backgroundRepo.getCoilUri(photoPath))
                    .memoryCacheKey(photoPath)
                    .diskCacheKey(photoPath)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .precision(Precision.INEXACT)
                    .scale(Scale.FILL)
                    .crossfade(false)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                onError = { hasImageError = true }
            )
        }

        // Top -> Bottom Scrim Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(HeroCardScrim)
        )

        // Card Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Category & Credit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QUOTE OF THE DAY · ${category.uppercase()}",
                    style = LabelOverlineStyle,
                    color = PhotoCategoryAccent
                )
                Text(
                    text = credit,
                    style = CaptionStyle,
                    color = PhotoCreditIvory
                )
            }

            // Quote Text (Auto-shrinks from 27sp down to 16sp floor) & Author
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                AutoShrinkQuoteText(
                    text = quoteText,
                    initialFontSize = 27.sp,
                    minFontSize = 16.sp,
                    color = PhotoTextIvory,
                    lineHeightMultiplier = 1.28f
                )

                Text(
                    text = "— $author",
                    style = AuthorStyle,
                    color = PhotoAuthorIvory
                )

                // Action Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .offset(x = (-10).dp),
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Save to favorites",
                            tint = if (isFavorite) MaterialTheme.colorScheme.primary else StillwordsTheme.colors.onPhotoText,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = StillwordsTheme.colors.onPhotoText,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onDownload,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.FileDownload,
                            contentDescription = "Download image",
                            tint = StillwordsTheme.colors.onPhotoText,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Edit design",
                            tint = StillwordsTheme.colors.onPhotoText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

// Auto-shrink text composable for quote display — docs/DESIGN_SPEC.md Sec. 1 (Rule 7 & 20)
// No quote is ever truncated (no maxLines + ellipsis). Auto-shrinks down to 16sp floor.
@Composable
fun AutoShrinkQuoteText(
    text: String,
    initialFontSize: TextUnit,
    minFontSize: TextUnit = 16.sp,
    color: Color = MaterialTheme.colorScheme.onBackground,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    lineHeightMultiplier: Float = 1.25f
) {
    val density = LocalDensity.current
    val fontScale = density.fontScale.coerceIn(1.0f, 1.5f)
    val scaleCompensation = 1.0f / (1.0f + (fontScale - 1.0f) * 0.5f)

    val lengthFactor = when {
        text.length > 140 -> 0.72f
        text.length > 100 -> 0.80f
        text.length > 70  -> 0.88f
        text.length > 45  -> 0.94f
        else -> 1.0f
    }

    val computedStartSp = (initialFontSize.value * lengthFactor * scaleCompensation)
        .coerceAtLeast(minFontSize.value)

    var currentFontSize by remember(text, initialFontSize, fontScale) {
        mutableStateOf(computedStartSp.sp)
    }

    Text(
        text = text,
        fontSize = currentFontSize,
        fontFamily = NewsreaderFontFamily,
        fontWeight = FontWeight.Normal,
        lineHeight = (currentFontSize.value * lineHeightMultiplier).sp,
        color = color,
        textAlign = textAlign,
        softWrap = true,
        overflow = TextOverflow.Clip,
        onTextLayout = { textLayoutResult ->
            if (textLayoutResult.hasVisualOverflow && currentFontSize.value > minFontSize.value) {
                currentFontSize = (currentFontSize.value - 1f).coerceAtLeast(minFontSize.value).sp
            }
        },
        modifier = modifier
    )
}

// ============================================================================
// 9. QUOTE TILE (Saved grid) — docs/DESIGN_SPEC.md Sec. 5
// Photo or Ink card, category label + quote, bottom-aligned, radius 18, gap 10
// ============================================================================
@Composable
fun QuoteTile(
    category: String,
    quoteText: String,
    modifier: Modifier = Modifier,
    photoPath: String? = null,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val backgroundRepo = remember(context) { BackgroundRepository(context) }
    val isPhoto = photoPath != null

    Box(
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 140.dp)
            .clip(TileCardShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, TileCardShape)
            .clickable(onClick = onClick)
    ) {
        if (isPhoto) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(backgroundRepo.getCoilUri(photoPath!!))
                    .memoryCacheKey(photoPath)
                    .precision(Precision.INEXACT)
                    .scale(Scale.FILL)
                    .crossfade(false)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .clip(TileCardShape)
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(TileCardShape)
                    .background(TileCardScrim)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomStart)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = category.uppercase(),
                style = LabelOverlineStyle.copy(fontSize = 11.sp, letterSpacing = 1.5.sp),
                color = if (isPhoto) PhotoCategoryAccent else MaterialTheme.colorScheme.primary
            )
            Text(
                text = quoteText,
                style = QuoteSmallStyle,
                color = if (isPhoto) PhotoTextIvory else MaterialTheme.colorScheme.onSurface,
                maxLines = 4,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ============================================================================
// 10. TOPIC TILE (Explore) — docs/DESIGN_SPEC.md Sec. 5
// 120 dp tall photo tile, topic name Newsreader 20 bottom-left
// ============================================================================
@Composable
fun TopicTile(
    topicName: String,
    modifier: Modifier = Modifier,
    photoPath: String? = null,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val backgroundRepo = remember(context) { BackgroundRepository(context) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(TileCardShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, TileCardShape)
            .clickable(onClick = onClick)
    ) {
        if (photoPath != null) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(backgroundRepo.getCoilUri(photoPath))
                    .memoryCacheKey(photoPath)
                    .precision(Precision.INEXACT)
                    .scale(Scale.FILL)
                    .crossfade(false)
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Overlay scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(TileCardScrim)
        )

        Text(
            text = topicName,
            style = SectionTitleStyle,
            color = PhotoTextIvory,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(14.dp)
        )
    }
}

// ============================================================================
// 11. QUICK TILE (Explore) — docs/DESIGN_SPEC.md Sec. 5
// 60 dp tall surface card, 34 dp icon box (accent 12% fill, accent icon) + label
// ============================================================================
@Composable
fun QuickTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(60.dp)
            .clip(SmallCardShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, SmallCardShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(BrassAlpha12),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = label,
            style = BodyStyle.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ============================================================================
// 12. WEEK DOTS — docs/DESIGN_SPEC.md Sec. 5 & Main.dc.html
// 7 dots for stillness habit tracking
// ============================================================================
@Composable
fun WeekDots(
    daysCompleted: Int,
    modifier: Modifier = Modifier,
    title: String = "$daysCompleted quiet days",
    subtitle: String = "Your week of stillness",
    dayStatuses: List<Boolean>? = null
) {
    val dayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(GroupShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, GroupShape)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = SectionTitleStyle.copy(fontSize = 18.sp),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = CaptionStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            dayLabels.forEachIndexed { index, label ->
                val isDone = dayStatuses?.getOrNull(index) ?: (index < daysCompleted)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .then(
                                if (isDone) {
                                    Modifier.background(MaterialTheme.colorScheme.primary)
                                } else {
                                    Modifier.border(
                                        1.dp,
                                        StillwordsTheme.colors.emptyDotBorder,
                                        CircleShape
                                    )
                                }
                            )
                    )
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontFamily = FigtreeFontFamily,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ============================================================================
// 13. SECTION HEADER — docs/DESIGN_SPEC.md Sec. 5
// Newsreader 20 section title with optional right action
// ============================================================================
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = SectionTitleStyle,
            color = MaterialTheme.colorScheme.onBackground
        )

        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                style = ChipStyle.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onActionClick)
                    .padding(vertical = 4.dp, horizontal = 6.dp)
            )
        }
    }
}

// ============================================================================
// 14. EMPTY STATE — docs/DESIGN_SPEC.md Sec. 5 & EmptySaved.dc.html
// Title (Newsreader 24) + Description (Figtree 14) + CTA button
// ============================================================================
@Composable
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.BookmarkBorder,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(BrassAlpha12)
                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = ScreenTitlePushedStyle.copy(fontSize = 24.sp),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = description,
                style = BodySmallStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(0.85f)
            )
        }

        if (actionLabel != null && onActionClick != null) {
            PrimaryButton(
                text = actionLabel,
                onClick = onActionClick,
                modifier = Modifier.widthIn(max = 220.dp)
            )
        }
    }
}

// ============================================================================
// 15. BOTTOM SHEET WRAPPER — docs/DESIGN_SPEC.md Sec. 5
// Top radius 28, surface background, drag handle 40 x 4
// ============================================================================
@Composable
fun BottomSheetWrapper(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(BottomSheetShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, BottomSheetShape)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(StillwordsTheme.colors.muted)
        )

        Spacer(modifier = Modifier.height(18.dp))
        content()
    }
}

// ============================================================================
// PREVIEWS — DARK & LIGHT THEME
// ============================================================================

@Preview(name = "Buttons - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Buttons - Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ButtonsPreview() {
    StillwordsTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryButton(text = "Primary Button", onClick = {})
                SecondaryButton(text = "Secondary Button", onClick = {})
                StillwordsTextButton(text = "Text Button", onClick = {})
            }
        }
    }
}

@Preview(name = "Chips & Segments - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Chips & Segments - Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun ChipsAndSegmentsPreview() {
    StillwordsTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StillwordsChip(text = "Selected", selected = true, onClick = {})
                    StillwordsChip(text = "Unselected", selected = false, onClick = {})
                }
                SegmentedControl(
                    items = listOf("Quotes · 12", "Collections · 4", "History"),
                    selectedIndex = 0,
                    onItemSelected = {}
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StillwordsSwitch(checked = true, onCheckedChange = {})
                    StillwordsSwitch(checked = false, onCheckedChange = {})
                }
            }
        }
    }
}

@Preview(name = "Settings - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Settings - Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun SettingsPreview() {
    StillwordsTheme {
        Surface {
            Column(modifier = Modifier.padding(16.dp)) {
                SettingsGroup {
                    SettingsRow(
                        title = "Morning reminder",
                        subtitle = "A gentle note with today's quote",
                        trailingContent = { StillwordsSwitch(checked = true, onCheckedChange = {}) }
                    )
                    SettingsRow(
                        title = "Reminder time",
                        valueText = "7:30 AM",
                        showChevron = true
                    )
                    SettingsRow(
                        title = "Photo credits",
                        valueText = "84 Pexels photos",
                        showChevron = true,
                        showDivider = false
                    )
                }
            }
        }
    }
}

@Preview(name = "Tiles & Week - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Tiles & Week - Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun TilesAndWeekPreview() {
    StillwordsTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                WeekDots(daysCompleted = 5)
                SectionHeader(title = "Topics", actionLabel = "All 22", onActionClick = {})
                QuickTile(label = "Favourites", icon = Icons.Outlined.Favorite, onClick = {})
                TopicTile(topicName = "Peace")
                QuoteTile(
                    category = "Peace",
                    quoteText = "The quiet mind hears what the busy one misses."
                )
            }
        }
    }
}

@Preview(name = "Empty State - Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Preview(name = "Empty State - Light", uiMode = Configuration.UI_MODE_NIGHT_NO)
@Composable
private fun EmptyStatePreview() {
    StillwordsTheme {
        Surface {
            EmptyState(
                title = "Nothing saved yet",
                description = "When a quote stays with you, tap the heart. It will wait for you here.",
                actionLabel = "Start reading",
                onActionClick = {}
            )
        }
    }
}
