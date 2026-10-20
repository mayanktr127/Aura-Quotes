package com.myplanetservices.stillwords.ui.screens.favorites

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch
import com.myplanetservices.stillwords.data.model.BackgroundTheme
import com.myplanetservices.stillwords.data.repository.BackgroundRepository
import com.myplanetservices.stillwords.ui.components.*
import com.myplanetservices.stillwords.ui.theme.*

@Composable
fun FavoritesScreen(
    viewModel: FavoritesViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToDesigner: (String?) -> Unit,
    onNavigateToCollection: (Long) -> Unit = {},
    onNavigateToRead: (String?, List<String>?) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val backgroundRepo = remember(context) { BackgroundRepository(context) }

    var newColName by remember { mutableStateOf("") }
    var newColDesc by remember { mutableStateOf("") }
    val colNameBringIntoView = remember { BringIntoViewRequester() }
    val colDescBringIntoView = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()

    // Dialog: Create Collection
    if (state.isCreateCollectionDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.setCreateCollectionDialogOpen(false) },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.imePadding(),
            properties = DialogProperties(decorFitsSystemWindows = false),
            title = {
                Text(
                    text = "New Collection",
                    style = SectionTitleStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = newColName,
                        onValueChange = { newColName = it },
                        label = { Text("Name", style = CaptionStyle) },
                        placeholder = { Text("e.g. Morning anchors", style = BodyStyle) },
                        singleLine = true,
                        shape = InputShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(colNameBringIntoView)
                            .onFocusEvent {
                                if (it.isFocused) {
                                    coroutineScope.launch {
                                        colNameBringIntoView.bringIntoView()
                                    }
                                }
                            }
                            .testTag("collection_name_input")
                    )

                    OutlinedTextField(
                        value = newColDesc,
                        onValueChange = { newColDesc = it },
                        label = { Text("Description (optional)", style = CaptionStyle) },
                        placeholder = { Text("e.g. Daily reminders for stillness", style = BodyStyle) },
                        maxLines = 2,
                        shape = InputShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .bringIntoViewRequester(colDescBringIntoView)
                            .onFocusEvent {
                                if (it.isFocused) {
                                    coroutineScope.launch {
                                        colDescBringIntoView.bringIntoView()
                                    }
                                }
                            }
                    )
                }
            },
            confirmButton = {
                PrimaryButton(
                    text = "Create",
                    onClick = {
                        if (newColName.isNotBlank()) {
                            viewModel.createCollection(newColName, newColDesc)
                            newColName = ""
                            newColDesc = ""
                        }
                    },
                    modifier = Modifier.width(110.dp),
                    height = 44.dp
                )
            },
            dismissButton = {
                StillwordsTextButton(
                    text = "Cancel",
                    onClick = {
                        viewModel.setCreateCollectionDialogOpen(false)
                        newColName = ""
                        newColDesc = ""
                    }
                )
            }
        )
    }

    // Dialog: Clear History Confirmation
    if (state.isClearHistoryDialogOpen) {
        AlertDialog(
            onDismissRequest = { viewModel.setClearHistoryDialogOpen(false) },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Clear History?",
                    style = SectionTitleStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "This will remove all recently read quotes from your history. Saved quotes will not be affected.",
                    style = BodyStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                PrimaryButton(
                    text = "Clear All",
                    onClick = { viewModel.clearHistory() },
                    modifier = Modifier.width(110.dp),
                    height = 44.dp
                )
            },
            dismissButton = {
                StillwordsTextButton(
                    text = "Cancel",
                    onClick = { viewModel.setClearHistoryDialogOpen(false) }
                )
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // ====================================================================
        // TOP HEADER: TITLE & NEW COLLECTION "+" (Saved.dc.html lines 31-34)
        // ====================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Saved",
                style = ScreenTitleStyle,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (state.selectedTab == 2 && state.historyItems.isNotEmpty()) {
                    TextButton(onClick = { viewModel.setClearHistoryDialogOpen(true) }) {
                        Text(
                            text = "Clear",
                            style = ChipStyle,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // New Collection round "+" button
                IconButton(
                    onClick = { viewModel.setCreateCollectionDialogOpen(true) },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Transparent)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .testTag("create_collection_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = "New collection",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // ====================================================================
        // SEGMENTED CONTROL (Saved.dc.html lines 36-40)
        // Quotes · {n} | Collections · {n} | History
        // ====================================================================
        val quoteCount = state.savedQuotes.size
        val colCount = state.collections.size
        val tabTitles = listOf(
            if (quoteCount > 0) "Quotes · $quoteCount" else "Quotes",
            if (colCount > 0) "Collections · $colCount" else "Collections",
            "History"
        )

        SegmentedControl(
            items = tabTitles,
            selectedIndex = state.selectedTab,
            onItemSelected = { viewModel.selectTab(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ====================================================================
        // TAB CONTENT
        // ====================================================================
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 0.dp)
        ) {
            when (state.selectedTab) {
                // ------------------------------------------------------------
                // TAB 0: SAVED QUOTES (STAGGERED 2-COLUMN GRID)
                // ------------------------------------------------------------
                0 -> {
                    if (state.savedQuotes.isEmpty()) {
                        SavedEmptyState(
                            onStartReading = { onNavigateToRead(null, null) }
                        )
                    } else {
                        LazyVerticalStaggeredGrid(
                            columns = StaggeredGridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("saved_quotes_grid"),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalItemSpacing = 10.dp
                        ) {
                            items(
                                items = state.savedQuotes,
                                key = { "saved_${it.id}" },
                                contentType = { "quote_tile" }
                            ) { quote ->
                                val bgTheme = remember(quote.category) { BackgroundTheme.fromCategory(quote.category) }
                                val photoPath = remember(bgTheme, quote.id) {
                                    backgroundRepo.getBackgroundAssetPath(quote.id, quote.category)
                                }
                                QuoteTile(
                                    category = quote.category,
                                    quoteText = quote.text,
                                    modifier = Modifier.animateItem(),
                                    photoPath = photoPath,
                                    onClick = { onNavigateToDetail(quote.id) }
                                )
                            }
                        }
                    }
                }

                // ------------------------------------------------------------
                // TAB 1: COLLECTIONS
                // ------------------------------------------------------------
                1 -> {
                    if (state.collections.isEmpty()) {
                        EmptyState(
                            title = "No collections yet",
                            description = "Group your favourite quotes into curated collections for any mood or moment.",
                            icon = Icons.Outlined.Folder,
                            actionLabel = "New collection",
                            onActionClick = { viewModel.setCreateCollectionDialogOpen(true) }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("collections_list"),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(
                                items = state.collections,
                                key = { "col_${it.id}" },
                                contentType = { "collection_card" }
                            ) { collection ->
                                val firstQuote = collection.previewQuotes.firstOrNull()
                                val photoPath = remember(firstQuote) {
                                    firstQuote?.let { backgroundRepo.getBackgroundAssetPath(it.id, it.category) }
                                }

                                Row(
                                    modifier = Modifier
                                        .animateItem()
                                        .fillMaxWidth()
                                        .clip(CardShape)
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, CardShape)
                                        .clickable { onNavigateToCollection(collection.id) }
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    // 48x60 Cover Thumbnail
                                    if (photoPath != null) {
                                        AsyncImage(
                                            model = ImageRequest.Builder(context)
                                                .data(backgroundRepo.getCoilUri(photoPath))
                                                .memoryCacheKey(photoPath)
                                                .precision(Precision.INEXACT)
                                                .scale(Scale.FILL)
                                                .crossfade(300)
                                                .build(),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .size(width = 48.dp, height = 60.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 48.dp, height = 60.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(BrassAlpha12),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Folder,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }

                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = collection.name,
                                            style = SectionTitleStyle,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (collection.description.isNotBlank()) {
                                            Text(
                                                text = collection.description,
                                                style = CaptionStyle,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Text(
                                            text = "${collection.quoteCount} ${if (collection.quoteCount == 1) "quote" else "quotes"}",
                                            style = LabelOverlineStyle.copy(fontSize = 10.sp),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Outlined.ChevronRight,
                                        contentDescription = "Open",
                                        tint = StillwordsTheme.colors.muted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // ------------------------------------------------------------
                // TAB 2: HISTORY (STABLE KEYS USING HistoryItem.id)
                // ------------------------------------------------------------
                2 -> {
                    if (state.historyItems.isEmpty()) {
                        EmptyState(
                            title = "No history yet",
                            description = "Quotes you explore or read will be kept here for easy return.",
                            icon = Icons.Outlined.History,
                            actionLabel = "Start reading",
                            onActionClick = { onNavigateToRead(null, null) }
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("history_list"),
                            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp)
                        ) {
                            items(
                                items = state.historyItems,
                                // CRITICAL RULE 6: stable, strictly unique key derived from HistoryEntity.id
                                key = { "hist_${it.id}" },
                                contentType = { "history_row" }
                            ) { historyItem ->
                                val quote = historyItem.quote
                                val bgTheme = remember(quote.category) { BackgroundTheme.fromCategory(quote.category) }
                                val photoPath = remember(bgTheme, quote.id) {
                                    backgroundRepo.getBackgroundAssetPath(quote.id, quote.category)
                                }

                                Column(
                                    modifier = Modifier
                                        .animateItem()
                                        .fillMaxWidth()
                                        .clickable { onNavigateToDetail(quote.id) }
                                ) {
                                    HorizontalDivider(
                                        thickness = 1.dp,
                                        color = MaterialTheme.colorScheme.outline
                                    )

                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 14.dp),
                                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        // 56x72 Photo Thumbnail
                                        if (photoPath != null) {
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data(backgroundRepo.getCoilUri(photoPath))
                                                    .memoryCacheKey(photoPath)
                                                    .precision(Precision.INEXACT)
                                                    .scale(Scale.FILL)
                                                    .crossfade(300)
                                                    .build(),
                                                contentDescription = null,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .size(width = 56.dp, height = 72.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                            )
                                        } else {
                                            Box(
                                                modifier = Modifier
                                                    .size(width = 56.dp, height = 72.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(MaterialTheme.colorScheme.surface)
                                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                            )
                                        }

                                        Column(
                                            modifier = Modifier.weight(1f),
                                            verticalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = quote.text,
                                                style = QuoteSmallStyle.copy(fontSize = 17.sp, lineHeight = 23.sp),
                                                color = MaterialTheme.colorScheme.onBackground,
                                                maxLines = 3,
                                                overflow = TextOverflow.Ellipsis
                                            )

                                            Text(
                                                text = "${quote.category} · ${quote.author}",
                                                style = CaptionStyle,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Empty state for Saved tab matching EmptySaved.dc.html lines 32-42:
 * Stacked cards illustration (one solid surface rotated -8deg, one dashed rotated 6deg with heart),
 * Title "Nothing saved yet", body, and PrimaryButton "Start reading".
 */
@Composable
private fun SavedEmptyState(
    onStartReading: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Stacked Cards Illustration (EmptySaved.dc.html lines 33-36)
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(170.dp),
            contentAlignment = Alignment.Center
        ) {
            // Card 1: Solid surface card rotated -8deg
            Box(
                modifier = Modifier
                    .offset(x = (-12).dp, y = 10.dp)
                    .size(width = 104.dp, height = 136.dp)
                    .rotate(-8f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(16.dp))
            )

            // Card 2: Dashed card rotated +6deg with heart icon
            val emptyBorder = StillwordsTheme.colors.emptyDotBorder
            Box(
                modifier = Modifier
                    .offset(x = 12.dp, y = (-6).dp)
                    .size(width = 104.dp, height = 136.dp)
                    .rotate(6f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawRoundRect(
                        color = emptyBorder,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16.dp.toPx()),
                        style = Stroke(
                            width = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )
                    )
                }

                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(22.dp))

        // Title & Description
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "Nothing saved yet",
                style = ScreenTitlePushedStyle.copy(fontSize = 24.sp),
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                text = "When a quote stays with you, tap the heart. It will wait for you here.",
                style = BodyStyle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.widthIn(max = 270.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // PrimaryButton "Start reading"
        PrimaryButton(
            text = "Start reading",
            onClick = onStartReading,
            modifier = Modifier.widthIn(max = 220.dp)
        )
    }
}
