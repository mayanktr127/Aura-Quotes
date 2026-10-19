package com.myplanetservices.stillwords.ui.screens.read

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.myplanetservices.stillwords.data.model.Quote
import com.myplanetservices.stillwords.data.repository.BackgroundRepository
import com.myplanetservices.stillwords.ui.components.AutoShrinkQuoteText
import com.myplanetservices.stillwords.ui.components.ShareFormatBottomSheet
import com.myplanetservices.stillwords.ui.theme.*

@Composable
fun ReadScreen(
    viewModel: ReadViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDesigner: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val backgroundRepo = remember(context) { BackgroundRepository(context) }
    val imageLoader = remember(context) { ImageLoader(context) }

    var activeShareQuote by remember { mutableStateOf<Quote?>(null) }

    BackHandler {
        onNavigateBack()
    }

    if (activeShareQuote != null) {
        val q = activeShareQuote!!
        ShareFormatBottomSheet(
            quoteText = q.text,
            author = q.author,
            category = q.category,
            quoteId = q.id,
            photoAssetPath = backgroundRepo.getBackgroundAssetPath(q.id, q.category),
            onDismissRequest = { activeShareQuote = null }
        )
    }

    if (state.isLoading || state.quotes.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Ink),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(color = Brass)
            } else {
                Text(
                    text = "No quotes available",
                    style = BodyStyle,
                    color = Ivory
                )
            }
        }
        return
    }

    val quotes = state.quotes
    val initialPage = state.initialIndex.coerceIn(0, quotes.size - 1)
    val pagerState = rememberPagerState(
        initialPage = initialPage,
        pageCount = { quotes.size }
    )

    // Notify settled page for history recording and swipe hint dismissal
    LaunchedEffect(pagerState.settledPage) {
        viewModel.onPageSettled(pagerState.settledPage)
    }

    // Preload next and previous images only
    LaunchedEffect(pagerState.currentPage, quotes) {
        val nextIdx = pagerState.currentPage + 1
        val prevIdx = pagerState.currentPage - 1
        if (nextIdx in quotes.indices) {
            val nextPhoto = backgroundRepo.getBackgroundAssetPath(quotes[nextIdx].id, quotes[nextIdx].category)
            if (nextPhoto != null) {
                imageLoader.enqueue(
                    ImageRequest.Builder(context)
                        .data(backgroundRepo.getCoilUri(nextPhoto))
                        .memoryCacheKey(nextPhoto)
                        .diskCacheKey(nextPhoto)
                        .precision(Precision.INEXACT)
                        .scale(Scale.FILL)
                        .build()
                )
            }
        }
        if (prevIdx in quotes.indices) {
            val prevPhoto = backgroundRepo.getBackgroundAssetPath(quotes[prevIdx].id, quotes[prevIdx].category)
            if (prevPhoto != null) {
                imageLoader.enqueue(
                    ImageRequest.Builder(context)
                        .data(backgroundRepo.getCoilUri(prevPhoto))
                        .memoryCacheKey(prevPhoto)
                        .diskCacheKey(prevPhoto)
                        .precision(Precision.INEXACT)
                        .scale(Scale.FILL)
                        .build()
                )
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink)
            .testTag("read_screen")
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            key = { index -> quotes.getOrNull(index)?.id ?: index }
        ) { page ->
            val quote = quotes.getOrNull(page) ?: return@VerticalPager
            val photoPath = remember(quote.id, quote.category) {
                backgroundRepo.getBackgroundAssetPath(quote.id, quote.category)
            }

            ReadQuotePage(
                quote = quote,
                pageIndex = page,
                totalPages = quotes.size,
                photoPath = photoPath,
                isSettled = pagerState.settledPage == page,
                showSwipeHint = state.showSwipeHint && page == 0,
                onClose = onNavigateBack,
                onFavoriteToggle = { viewModel.toggleFavorite(quote.id) },
                onShare = { activeShareQuote = quote },
                onDownload = { activeShareQuote = quote },
                onEdit = { onNavigateToDesigner(quote.id) }
            )
        }
    }
}

@Composable
private fun ReadQuotePage(
    quote: Quote,
    pageIndex: Int,
    totalPages: Int,
    photoPath: String?,
    isSettled: Boolean,
    showSwipeHint: Boolean,
    onClose: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onShare: () -> Unit,
    onDownload: () -> Unit,
    onEdit: () -> Unit
) {
    val context = LocalContext.current
    val backgroundRepo = remember(context) { BackgroundRepository(context) }
    var hasImageError by remember(photoPath) { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Layer 1: Full-bleed Photo with caching
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

        // Layer 2: Full-screen Scrim Gradient (55% -> 10% -> 25% -> 95%)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FullScreenPhotoScrim)
        )

        // Layer 3: Foreground Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 16.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Row: Close (X) · {CATEGORY} · i OF n · Spacer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("read_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Ivory,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "${quote.category.uppercase()} · ${pageIndex + 1} OF $totalPages",
                    style = LabelOverlineStyle,
                    color = PhotoCategoryAccent
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            // Center: Brass Quotation Mark + Quote Text (Auto-shrink) + Author
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // Brass Quote Mark
                BrassQuoteMark(modifier = Modifier.size(width = 34.dp, height = 26.dp))

                // Text Fades In when Settled
                AnimatedVisibility(
                    visible = isSettled,
                    enter = fadeIn(animationSpec = tween(200)),
                    exit = fadeOut(animationSpec = tween(100))
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        AutoShrinkQuoteText(
                            text = quote.text,
                            initialFontSize = 34.sp,
                            minFontSize = 16.sp,
                            color = Ivory,
                            lineHeightMultiplier = 1.22f,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = "— ${quote.author}",
                            fontFamily = NewsreaderFontFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 16.sp,
                            color = PhotoAuthorIvory
                        )
                    }
                }
            }

            // Bottom Block: 4 Action Buttons + Swipe Hint + Credit
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                // 4 Round 52 dp Action Buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Save Button
                    ReadActionButton(
                        icon = if (quote.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (quote.isFavorite) "Saved" else "Save to favourites",
                        tint = if (quote.isFavorite) Brass else Ivory,
                        borderColor = if (quote.isFavorite) Brass else Color(0x38F3EDE3),
                        backgroundColor = if (quote.isFavorite) BrassAlpha12 else Color(0x590F0E0C),
                        onClick = onFavoriteToggle
                    )

                    // Share Button
                    ReadActionButton(
                        icon = Icons.Outlined.Share,
                        contentDescription = "Share",
                        onClick = onShare
                    )

                    // Download Button
                    ReadActionButton(
                        icon = Icons.Outlined.FileDownload,
                        contentDescription = "Download image",
                        onClick = onDownload
                    )

                    // Edit Button
                    ReadActionButton(
                        icon = Icons.Outlined.Edit,
                        contentDescription = "Edit design",
                        onClick = onEdit
                    )
                }

                // "Swipe up for the next quote" Hint (shown on first few uses)
                if (showSwipeHint) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowUp,
                            contentDescription = null,
                            tint = Color(0x8CF3EDE3),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Swipe up for the next quote",
                            style = CaptionStyle.copy(fontSize = 12.sp),
                            color = Color(0x8CF3EDE3),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Credit
                Text(
                    text = "Photo · Pexels",
                    style = CaptionStyle.copy(fontSize = 10.sp),
                    color = Color(0x73F3EDE3),
                    modifier = Modifier.align(Alignment.Start)
                )
            }
        }
    }
}

@Composable
private fun ReadActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = Ivory,
    borderColor: Color = Color(0x38F3EDE3),
    backgroundColor: Color = Color(0x590F0E0C)
) {
    Box(
        modifier = modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun BrassQuoteMark(modifier: Modifier = Modifier) {
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val scaleX = size.width / 34f
        val scaleY = size.height / 26f
        val path = androidx.compose.ui.graphics.Path().apply {
            // First quotation mark
            moveTo(2f * scaleX, 24f * scaleY)
            lineTo(2f * scaleX, 14f * scaleY)
            cubicTo(2f * scaleX, 7f * scaleY, 6f * scaleX, 3f * scaleY, 13f * scaleX, 2f * scaleY)
            lineTo(13f * scaleX, 7f * scaleY)
            cubicTo(9f * scaleX, 8f * scaleY, 7f * scaleX, 10f * scaleY, 7f * scaleX, 14f * scaleY)
            lineTo(13f * scaleX, 14f * scaleY)
            lineTo(13f * scaleX, 24f * scaleY)
            close()

            // Second quotation mark
            moveTo(20f * scaleX, 24f * scaleY)
            lineTo(20f * scaleX, 14f * scaleY)
            cubicTo(20f * scaleX, 7f * scaleY, 24f * scaleX, 3f * scaleY, 31f * scaleX, 2f * scaleY)
            lineTo(31f * scaleX, 7f * scaleY)
            cubicTo(27f * scaleX, 8f * scaleY, 25f * scaleX, 10f * scaleY, 25f * scaleX, 14f * scaleY)
            lineTo(31f * scaleX, 14f * scaleY)
            lineTo(31f * scaleX, 24f * scaleY)
            close()
        }
        drawPath(path = path, color = Brass)
    }
}
