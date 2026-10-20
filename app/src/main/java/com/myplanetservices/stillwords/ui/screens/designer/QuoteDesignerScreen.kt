package com.myplanetservices.stillwords.ui.screens.designer

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusEvent
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.myplanetservices.stillwords.data.model.BackgroundTheme
import com.myplanetservices.stillwords.data.model.CardAspectRatio
import com.myplanetservices.stillwords.data.model.CardStyle
import com.myplanetservices.stillwords.data.model.PhotoFilter
import com.myplanetservices.stillwords.data.model.QuoteFont
import com.myplanetservices.stillwords.data.repository.BackgroundRepository
import com.myplanetservices.stillwords.ui.components.*
import com.myplanetservices.stillwords.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun QuoteDesignerScreen(
    viewModel: QuoteDesignerViewModel,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val backgroundRepo = remember(context) { BackgroundRepository(context) }

    var selectedToolTab by remember { mutableStateOf(0) } // 0: Backgrounds, 1: Typography, 2: Edit Text
    var showUnlockSheet by remember { mutableStateOf(false) }
    var pendingLockedLookName by remember { mutableStateOf("Premium Look") }
    var pendingLookAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Format choices: 1:1, 4:5 Post, 9:16 Story (16:9 dropped per spec)
    val formatList = remember {
        listOf(
            CardAspectRatio.SQUARE_1_1,
            CardAspectRatio.PORTRAIT_4_5,
            CardAspectRatio.STORY_9_16
        )
    }
    val formatLabels = remember { listOf("1:1", "4:5 Post", "9:16 Story") }
    val currentFormatIndex = formatList.indexOf(state.aspectRatio).let { if (it >= 0) it else 1 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Create",
                        style = ScreenTitleStyle.copy(fontSize = 24.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("designer_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                actions = {
                    // Save as User Quote
                    IconButton(
                        onClick = {
                            viewModel.saveAsUserQuote()
                            Toast.makeText(context, "Saved to your quotes library", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("designer_save_quote_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkAdd,
                            contentDescription = "Save Quote",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Save to Gallery
                    IconButton(
                        onClick = {
                            viewModel.exportImage(context) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("designer_export_image_button")
                    ) {
                        if (state.isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = "Download Image",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    // Share
                    IconButton(
                        onClick = {
                            viewModel.shareImage(context) { success ->
                                if (!success) {
                                    Toast.makeText(context, "Could not share image", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.testTag("designer_share_image_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (!WindowInsets.isImeVisible) {
                // Footer: Save to gallery (Secondary) + Share (Primary) — docs/DESIGN_SPEC.md Screen 13 & Designer.dc.html
                Surface(
                    color = MaterialTheme.colorScheme.background,
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SecondaryButton(
                            text = "Save to gallery",
                            onClick = {
                                viewModel.exportImage(context) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            height = 52.dp,
                            modifier = Modifier.weight(1f)
                        )

                        PrimaryButton(
                            text = "Share",
                            onClick = {
                                viewModel.shareImage(context) { success ->
                                    if (!success) {
                                        Toast.makeText(context, "Could not share image", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            height = 52.dp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ====================================================================
            // 1. LIVE PREVIEW CARD — docs/DESIGN_SPEC.md Screen 13 & Designer.dc.html
            // Sizing: 264 x 330 (4:5 Post), 264 x 264 (1:1), 204 x 362 (9:16 Story)
            // ====================================================================
            val cardShape = RoundedCornerShape(22.dp)
            val isPhoto = state.selectedPhotoPath != null

            val (cardWidth, cardHeight) = when (state.aspectRatio) {
                CardAspectRatio.SQUARE_1_1 -> 264.dp to 264.dp
                CardAspectRatio.PORTRAIT_4_5 -> 264.dp to 330.dp
                CardAspectRatio.STORY_9_16 -> 204.dp to 362.dp
                CardAspectRatio.LANDSCAPE_16_9 -> 264.dp to 330.dp
            }

            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .size(width = cardWidth, height = cardHeight)
                    .shadow(4.dp, cardShape)
                    .clip(cardShape)
                    .background(
                        if (isPhoto) MaterialTheme.colorScheme.surface
                        else state.cardStyle.backgroundColor
                    )
                    .border(1.dp, MaterialTheme.colorScheme.outline, cardShape)
                    .testTag("designer_canvas_preview"),
                contentAlignment = Alignment.Center
            ) {
                // Background Photo + Filter
                if (isPhoto && state.selectedPhotoPath != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(backgroundRepo.getCoilUri(state.selectedPhotoPath!!))
                            .memoryCacheKey(state.selectedPhotoPath)
                            .diskCacheKey(state.selectedPhotoPath)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .precision(Precision.INEXACT)
                            .scale(Scale.FILL)
                            .crossfade(false)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        colorFilter = state.selectedFilter.composeColorFilter,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Scrim gradient
                    val scrimTop = if (state.selectedFilter == PhotoFilter.MIST) state.selectedFilter.scrimTop else Color(0x330F0E0C)
                    val scrimBottom = if (state.selectedFilter == PhotoFilter.MIST) state.selectedFilter.scrimBottom else Color(0xA60F0E0C)
                    val scrimBrush = remember(scrimTop, scrimBottom) {
                        Brush.verticalGradient(
                            colors = listOf(scrimTop, Color(0x660F0E0C), scrimBottom)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(scrimBrush)
                    )
                } else if (state.cardStyle.gradient != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(state.cardStyle.gradient!!)
                    )
                }

                // Inner luxury gold border frame inset
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f), RoundedCornerShape(14.dp))
                )

                // Card text content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Category Overline
                    Text(
                        text = "“  ${state.category.uppercase()}  ”",
                        style = LabelOverlineStyle.copy(
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Centered quote and author
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val formattedQuote = if (state.quoteFont.isAllCaps) state.quoteText.uppercase() else state.quoteText
                        val quoteFontSize = when {
                            state.quoteText.length > 150 -> 14.sp
                            state.quoteText.length > 100 -> 16.sp
                            state.quoteText.length > 60  -> 18.sp
                            else -> 20.sp
                        }

                        Text(
                            text = formattedQuote,
                            fontFamily = state.quoteFont.fontFamily,
                            fontStyle = state.quoteFont.fontStyle,
                            fontWeight = state.quoteFont.fontWeight,
                            fontSize = quoteFontSize,
                            lineHeight = (quoteFontSize.value * 1.3f).sp,
                            color = if (isPhoto) PhotoTextIvory else state.cardStyle.textColor,
                            textAlign = TextAlign.Center,
                            softWrap = true
                        )

                        Text(
                            text = "— ${state.author}",
                            fontFamily = NewsreaderFontFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 13.sp,
                            color = if (isPhoto) PhotoAuthorIvory else state.cardStyle.authorColor,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Watermark / Wordmark
                    if (state.showWatermark) {
                        Text(
                            text = "STILLWORDS",
                            style = LabelOverlineStyle.copy(
                                fontSize = 8.sp,
                                letterSpacing = 2.5.sp,
                                color = if (isPhoto) Color(0x8CF3EDE3) else state.cardStyle.authorColor.copy(alpha = 0.55f)
                            ),
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // ====================================================================
            // 2. FORMAT SEGMENTED CONTROL — docs/DESIGN_SPEC.md Screen 13
            // 1:1, 4:5 Post, 9:16 Story
            // ====================================================================
            SegmentedControl(
                items = formatLabels,
                selectedIndex = currentFormatIndex,
                onItemSelected = { index ->
                    viewModel.onAspectRatioSelected(formatList[index])
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ====================================================================
            // 3. TOOL SECTIONS (Backgrounds, Typography, Edit Text)
            // ====================================================================
            SegmentedControl(
                items = listOf("Backgrounds", "Typography", "Edit Text"),
                selectedIndex = selectedToolTab,
                onItemSelected = { selectedToolTab = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedToolTab) {
                0 -> {
                    // TAB 0: BACKGROUNDS & FILTERS
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Section Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Backgrounds",
                                style = SectionTitleStyle.copy(fontSize = 18.sp),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "Photos · Ink · Text · Font",
                                style = CaptionStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // 4-column Backgrounds Grid (8 tiles: 7 photo themes + 1 Plain)
                        val themeTiles = listOf(
                            BackgroundTheme.STILL_WATER,
                            BackgroundTheme.MOUNTAINS,
                            BackgroundTheme.GOLDEN_HOUR,
                            BackgroundTheme.NIGHT_SKY,
                            BackgroundTheme.FOREST_BOTANICAL, // Premium
                            null, // Plain / Ink
                            BackgroundTheme.SUNRISE_ROADS,    // Premium
                            BackgroundTheme.QUIET_SPACES      // Premium
                        )

                        val rows = themeTiles.chunked(4)
                        rows.forEach { rowTiles ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowTiles.forEach { theme ->
                                    val isPlainTile = theme == null
                                    val isSelected = if (isPlainTile) {
                                        state.selectedPhotoPath == null
                                    } else {
                                        state.selectedPhotoPath != null &&
                                                backgroundRepo.getBackgroundAssetPath("preview", theme?.displayName ?: "")?.startsWith("backgrounds/${theme?.id}") == true ||
                                                state.selectedPhotoPath?.startsWith("backgrounds/${theme?.id}") == true
                                    }

                                    val isLocked = (theme?.isPremium == true) && !state.isPremiumUnlocked
                                    val tileShape = RoundedCornerShape(12.dp)

                                    val tileBorderModifier = if (isSelected) {
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, tileShape)
                                    } else {
                                        Modifier.border(1.dp, MaterialTheme.colorScheme.outline, tileShape)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(72.dp)
                                            .clip(tileShape)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .then(tileBorderModifier)
                                            .clickable {
                                                if (isLocked) {
                                                    pendingLockedLookName = theme?.displayName ?: "Premium Look"
                                                    pendingLookAction = {
                                                        val path = theme?.let { backgroundRepo.getThumbnailAssetPath(it) }
                                                        viewModel.onPhotoSelected(path)
                                                    }
                                                    showUnlockSheet = true
                                                } else {
                                                    if (isPlainTile) {
                                                        viewModel.onPhotoSelected(null)
                                                    } else {
                                                        val path = theme?.let { backgroundRepo.getThumbnailAssetPath(it) }
                                                        viewModel.onPhotoSelected(path)
                                                    }
                                                }
                                            }
                                    ) {
                                        if (isPlainTile) {
                                            // Plain / Ink tile
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(MaterialTheme.colorScheme.surface),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Outlined.FormatColorFill,
                                                        contentDescription = "Plain Color",
                                                        tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(4.dp))
                                                    Text(
                                                        text = "Ink",
                                                        style = ChipStyle.copy(fontSize = 10.sp),
                                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        } else {
                                            // Photo theme tile with cached 160 px thumbnail
                                            val thumbPath = remember(theme) { theme?.let { backgroundRepo.getThumbnailAssetPath(it) } }
                                            val thumbBitmap = remember(thumbPath) {
                                                thumbPath?.let { backgroundRepo.getThumbnailBitmap(it, 160) }
                                            }

                                            if (thumbBitmap != null) {
                                                Image(
                                                    bitmap = thumbBitmap.asImageBitmap(),
                                                    contentDescription = theme?.displayName,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            }

                                            // Scrim over thumbnail
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .background(Color(0x400F0E0C))
                                            )

                                            // Subtitle
                                            Text(
                                                text = theme?.displayName?.split(" ")?.firstOrNull() ?: "",
                                                style = ChipStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                                color = Color.White,
                                                modifier = Modifier
                                                    .align(Alignment.BottomCenter)
                                                    .padding(bottom = 4.dp),
                                                maxLines = 1
                                            )
                                        }

                                        // Lock badge if premium and locked
                                        if (isLocked) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(6.dp)
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xB30F0E0C)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Lock,
                                                    contentDescription = "Locked",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Photo Filters Row (shown only when a photo background is selected; hidden for plain)
                        if (state.selectedPhotoPath != null) {
                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Filters",
                                style = SectionTitleStyle.copy(fontSize = 18.sp),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(PhotoFilter.entries) { filter ->
                                    val isSelected = filter == state.selectedFilter
                                    val isLocked = filter.isPremium && !state.isPremiumUnlocked
                                    val shape = RoundedCornerShape(12.dp)

                                    val filterBorderModifier = if (isSelected) {
                                        Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
                                    } else {
                                        Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
                                    }

                                    Box(
                                        modifier = Modifier
                                            .size(width = 68.dp, height = 72.dp)
                                            .clip(shape)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .then(filterBorderModifier)
                                            .clickable {
                                                if (isLocked) {
                                                    pendingLockedLookName = "${filter.displayName} Filter"
                                                    pendingLookAction = {
                                                        viewModel.onFilterSelected(filter)
                                                    }
                                                    showUnlockSheet = true
                                                } else {
                                                    viewModel.onFilterSelected(filter)
                                                }
                                            }
                                    ) {
                                        val filterThumb = remember(state.selectedPhotoPath) {
                                            state.selectedPhotoPath?.let { backgroundRepo.getThumbnailBitmap(it, 160) }
                                        }
                                        if (filterThumb != null) {
                                            Image(
                                                bitmap = filterThumb.asImageBitmap(),
                                                contentDescription = filter.displayName,
                                                contentScale = ContentScale.Crop,
                                                colorFilter = filter.composeColorFilter,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        }

                                        // Scrim
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(Color(0x400F0E0C))
                                        )

                                        // Label
                                        Text(
                                            text = filter.displayName,
                                            style = ChipStyle.copy(fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                                            color = Color.White,
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(bottom = 4.dp),
                                            maxLines = 1
                                        )

                                        // Lock badge
                                        if (isLocked) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .padding(4.dp)
                                                    .size(20.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xB30F0E0C)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Lock,
                                                    contentDescription = "Locked",
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            // Plain background selected: Show Card Styles
                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Card Styles",
                                style = SectionTitleStyle.copy(fontSize = 18.sp),
                                color = MaterialTheme.colorScheme.onBackground
                            )

                            StyleSelectorCarousel(
                                selectedStyle = state.cardStyle,
                                onStyleSelected = { viewModel.onStyleSelected(it) }
                            )
                        }

                        // Dashed "Watch a short video..." button — docs/DESIGN_SPEC.md Screen 13 & Designer.dc.html
                        if (!state.isPremiumUnlocked) {
                            Spacer(modifier = Modifier.height(6.dp))

                            val strokeColor = Color(0xFF8A6A30)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                                    .drawBehind {
                                        drawRoundRect(
                                            color = strokeColor,
                                            style = Stroke(
                                                width = 1.dp.toPx(),
                                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                                            ),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(14.dp.toPx())
                                        )
                                    }
                                    .clickable {
                                        pendingLockedLookName = "All Looks"
                                        pendingLookAction = null
                                        showUnlockSheet = true
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Watch a short video to unlock premium looks for 24 hours",
                                    style = ChipStyle.copy(
                                        color = Color(0xFFE8D3A8),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                1 -> {
                    // TAB 1: TYPOGRAPHY & WATERMARK
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Curated Font Styles",
                            style = SectionTitleStyle.copy(fontSize = 18.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(QuoteFont.entries) { font ->
                                val isSelected = font == state.quoteFont
                                val shape = RoundedCornerShape(14.dp)
                                val borderStroke = if (isSelected) {
                                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, shape)
                                } else {
                                    Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(shape)
                                        .background(
                                            if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surface
                                        )
                                        .then(borderStroke)
                                        .clickable { viewModel.onFontSelected(font) }
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "Aa",
                                            fontFamily = font.fontFamily,
                                            fontStyle = font.fontStyle,
                                            fontWeight = font.fontWeight,
                                            fontSize = 20.sp,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onBackground
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = font.displayName,
                                            style = CaptionStyle,
                                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        // Watermark setting
                        SettingsGroup {
                            SettingsRow(
                                title = "Include Stillwords watermark",
                                subtitle = "Shows subtle STILLWORDS wordmark at bottom",
                                trailingContent = {
                                    Switch(
                                        checked = state.showWatermark,
                                        onCheckedChange = { viewModel.toggleWatermark(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                                            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            uncheckedTrackColor = MaterialTheme.colorScheme.outline
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                2 -> {
                    // TAB 2: EDIT TEXT
                    val quoteBringIntoView = remember { BringIntoViewRequester() }
                    val authorBringIntoView = remember { BringIntoViewRequester() }
                    val categoryBringIntoView = remember { BringIntoViewRequester() }
                    val textScope = rememberCoroutineScope()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = state.quoteText,
                            onValueChange = { viewModel.onQuoteTextChanged(it) },
                            label = { Text("Quote Words") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(quoteBringIntoView)
                                .onFocusEvent {
                                    if (it.isFocused) {
                                        textScope.launch { quoteBringIntoView.bringIntoView() }
                                    }
                                }
                                .testTag("designer_quote_input"),
                            shape = RoundedCornerShape(16.dp),
                            minLines = 3,
                            maxLines = 6,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        OutlinedTextField(
                            value = state.author,
                            onValueChange = { viewModel.onAuthorChanged(it) },
                            label = { Text("Author / Speaker") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(authorBringIntoView)
                                .onFocusEvent {
                                    if (it.isFocused) {
                                        textScope.launch { authorBringIntoView.bringIntoView() }
                                    }
                                }
                                .testTag("designer_author_input"),
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        OutlinedTextField(
                            value = state.category,
                            onValueChange = { viewModel.onCategoryChanged(it) },
                            label = { Text("Category / Topic") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .bringIntoViewRequester(categoryBringIntoView)
                                .onFocusEvent {
                                    if (it.isFocused) {
                                        textScope.launch { categoryBringIntoView.bringIntoView() }
                                    }
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )

                        PrimaryButton(
                            text = "Save as User Quote",
                            icon = Icons.Outlined.BookmarkAdd,
                            onClick = {
                                viewModel.saveAsUserQuote()
                                Toast.makeText(context, "Saved to your quotes library", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    // UnlockSheet dialog
    if (showUnlockSheet) {
        UnlockSheet(
            lockedLookName = pendingLockedLookName,
            isUnlocked = state.isPremiumUnlocked,
            expiryTimestamp = state.premiumExpiresAt,
            onWatchVideo = {
                viewModel.unlockPremiumFor24Hours()
            },
            onDismiss = {
                showUnlockSheet = false
            },
            onApplyLook = {
                pendingLookAction?.invoke()
                showUnlockSheet = false
            }
        )
    }
}
