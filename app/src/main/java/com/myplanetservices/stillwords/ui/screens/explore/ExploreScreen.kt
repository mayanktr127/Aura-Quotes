package com.myplanetservices.stillwords.ui.screens.explore

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.myplanetservices.stillwords.data.model.BackgroundTheme
import com.myplanetservices.stillwords.data.repository.BackgroundRepository
import com.myplanetservices.stillwords.ui.components.*
import com.myplanetservices.stillwords.ui.theme.*

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToDesigner: (String?) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToRead: (String?, List<String>?) -> Unit = { _, _ -> },
    onNavigateToFavorites: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val backgroundRepo = remember(context) { BackgroundRepository(context) }
    var isShowingAllTopics by remember { mutableStateOf(false) }
    var isShowingMyQuotes by remember { mutableStateOf(false) }

    // Predictive back handling
    if (state.selectedFilterType != null) {
        BackHandler {
            viewModel.clearFilter()
        }
    } else if (isShowingAllTopics) {
        BackHandler {
            isShowingAllTopics = false
        }
    } else if (isShowingMyQuotes) {
        BackHandler {
            isShowingMyQuotes = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        if (isShowingAllTopics) {
            // ================================================================
            // FULL TOPIC LIST (ALL 22 TOPICS IN 2-COLUMN GRID)
            // ================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("all_topics_screen")
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { isShowingAllTopics = false },
                        modifier = Modifier.testTag("all_topics_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Explore",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "All Topics",
                            style = ScreenTitlePushedStyle,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "${state.categories.size} topics",
                            style = CaptionStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 96.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        items = state.categories,
                        key = { "all_topic_$it" },
                        contentType = { "topic_tile" }
                    ) { category ->
                        val photoPath = remember(category) {
                            backgroundRepo.getCategoryThumbnailAssetPath(category)
                        }
                        TopicTile(
                            topicName = category,
                            photoPath = photoPath,
                            onClick = {
                                onNavigateToRead(null, listOf(category))
                            }
                        )
                    }
                }
            }
        } else if (isShowingMyQuotes) {
            // ================================================================
            // MY QUOTES VIEW (USER CREATED QUOTES - HISTORY ROW STYLE)
            // ================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("my_quotes_screen")
            ) {
                // Top App Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { isShowingMyQuotes = false },
                            modifier = Modifier.testTag("my_quotes_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Explore",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "My Quotes",
                                style = ScreenTitlePushedStyle,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${state.userQuotes.size} ${if (state.userQuotes.size == 1) "quote" else "quotes"}",
                                style = CaptionStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Create quote "+" action button
                    IconButton(
                        onClick = { onNavigateToDesigner(null) },
                        modifier = Modifier.testTag("create_quote_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Create a quote",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                if (state.userQuotes.isEmpty()) {
                    EmptyState(
                        title = "No quotes yet",
                        description = "Quotes you create will appear here. Write your own thoughtful words and customize their style.",
                        icon = Icons.Outlined.Edit,
                        actionLabel = "Create a quote",
                        onActionClick = { onNavigateToDesigner(null) }
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("my_quotes_list"),
                        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp)
                    ) {
                        items(
                            items = state.userQuotes,
                            key = { "my_quote_${it.id}" },
                            contentType = { "history_row" }
                        ) { quote ->
                            val bgTheme = remember(quote.category) { BackgroundTheme.fromCategory(quote.category) }
                            val photoPath = remember(bgTheme, quote.id) {
                                backgroundRepo.getBackgroundAssetPath(quote.id, quote.category)
                            }

                            Column(
                                modifier = Modifier
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
                                    // 56x72 Photo Thumbnail (matching History row style)
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
                                            modifier = Modifier
                                                .size(width = 56.dp, height = 72.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(width = 56.dp, height = 72.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(BrassAlpha12),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.Edit,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
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
        } else if (state.selectedFilterType != null && state.selectedFilterValue != null) {
            // ================================================================
            // FILTERED QUOTES VIEW
            // ================================================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("explore_filtered_screen"),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                item(key = "filtered_header", contentType = "header") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(
                                onClick = { viewModel.clearFilter() },
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.surface, CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = state.selectedFilterValue ?: "",
                                    style = SectionTitleStyle.copy(fontSize = 22.sp),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "${state.filteredQuotes.size} quotes found",
                                    style = CaptionStyle,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        TextButton(onClick = { viewModel.clearFilter() }) {
                            Text("Clear", color = MaterialTheme.colorScheme.primary, style = ChipStyle)
                        }
                    }
                }

                if (state.filteredQuotes.isEmpty()) {
                    item(key = "empty_filtered", contentType = "empty") {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No quotes available in this category yet.",
                                style = BodyStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(
                        items = state.filteredQuotes,
                        key = { "filtered_${it.id}" },
                        contentType = { "quote_card" }
                    ) { quote ->
                        Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                            QuoteCard(
                                quote = quote,
                                cardStyle = quote.defaultStyle,
                                isHero = false,
                                modifier = Modifier.fillMaxWidth(),
                                onFavoriteToggle = { viewModel.toggleFavorite(quote.id) },
                                onOpenDesigner = { onNavigateToDesigner(quote.id) },
                                onClick = { onNavigateToDetail(quote.id) }
                            )
                        }
                    }
                }
            }
        } else {
            // ================================================================
            // EXPLORE MAIN SCREEN (docs/DESIGN_SPEC.md Screen 7 & Explore.dc.html)
            // ================================================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("explore_screen"),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // 1. Title & Search Input Field (tapping opens SearchScreen)
                item(key = "search_header", contentType = "header") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Explore",
                            style = ScreenTitleStyle,
                            color = MaterialTheme.colorScheme.onBackground
                        )

                        // Search Field Box matching Explore.dc.html
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clip(InputShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline, InputShape)
                                .clickable { onNavigateToSearch() }
                                .padding(horizontal = 16.dp)
                                .testTag("explore_search_bar"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Search words, topics or authors",
                                style = BodyStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 2. QuickTiles 2x2 Grid (Favourites, Collections, My quotes, Recently read)
                item(key = "quick_tiles", contentType = "quick_tiles") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuickTile(
                                label = "Favourites",
                                icon = Icons.Outlined.FavoriteBorder,
                                onClick = { onNavigateToFavorites(0) },
                                modifier = Modifier.weight(1f)
                            )
                            QuickTile(
                                label = "Collections",
                                icon = Icons.Outlined.Folder,
                                onClick = { onNavigateToFavorites(1) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            QuickTile(
                                label = "My quotes",
                                icon = Icons.Outlined.Edit,
                                onClick = { isShowingMyQuotes = true },
                                modifier = Modifier.weight(1f)
                            )
                            QuickTile(
                                label = "Recently read",
                                icon = Icons.Outlined.History,
                                onClick = { onNavigateToFavorites(2) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                // 3. Topics Grid (4 Featured TopicTiles with real photos + "All 22" link)
                item(key = "topics_section", contentType = "topics") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(
                            title = "Topics",
                            actionLabel = "All ${state.categories.size.coerceAtLeast(22)}",
                            onActionClick = { isShowingAllTopics = true }
                        )

                        val topics = state.featuredTopics.take(4)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Row 1
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                topics.getOrNull(0)?.let { topic ->
                                    val photo = remember(topic) { backgroundRepo.getCategoryThumbnailAssetPath(topic) }
                                    TopicTile(
                                        topicName = topic,
                                        photoPath = photo,
                                        onClick = { onNavigateToRead(null, listOf(topic)) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                topics.getOrNull(1)?.let { topic ->
                                    val photo = remember(topic) { backgroundRepo.getCategoryThumbnailAssetPath(topic) }
                                    TopicTile(
                                        topicName = topic,
                                        photoPath = photo,
                                        onClick = { onNavigateToRead(null, listOf(topic)) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            // Row 2
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                topics.getOrNull(2)?.let { topic ->
                                    val photo = remember(topic) { backgroundRepo.getCategoryThumbnailAssetPath(topic) }
                                    TopicTile(
                                        topicName = topic,
                                        photoPath = photo,
                                        onClick = { onNavigateToRead(null, listOf(topic)) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                topics.getOrNull(3)?.let { topic ->
                                    val photo = remember(topic) { backgroundRepo.getCategoryThumbnailAssetPath(topic) }
                                    TopicTile(
                                        topicName = topic,
                                        photoPath = photo,
                                        onClick = { onNavigateToRead(null, listOf(topic)) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Moments Section (Horizontal chips with approved category mapping)
                item(key = "moments_section", contentType = "moments") {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SectionHeader(title = "Moments")

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(
                                items = state.moments,
                                key = { "moment_$it" },
                                contentType = { "moment_chip" }
                            ) { moment ->
                                StillwordsChip(
                                    text = moment,
                                    selected = false,
                                    onClick = {
                                        val mapped = ExploreUiState.MOMENT_MAPPING[moment] ?: listOf(moment)
                                        onNavigateToRead(null, mapped)
                                    }
                                )
                            }
                        }
                    }
                }

                // 5. Authors & Philosophers Section
                if (state.authors.isNotEmpty()) {
                    item(key = "authors_section", contentType = "authors") {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            SectionHeader(title = "Authors & Philosophers")

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(
                                    items = state.authors,
                                    key = { "author_${it.name}" },
                                    contentType = { "author_card" }
                                ) { author ->
                                    val shape = CardShape
                                    Box(
                                        modifier = Modifier
                                            .width(160.dp)
                                            .clip(shape)
                                            .background(MaterialTheme.colorScheme.surface)
                                            .border(1.dp, MaterialTheme.colorScheme.outline, shape)
                                            .clickable {
                                                val firstQuoteId = state.allQuotes.firstOrNull {
                                                    it.author.equals(author.name, ignoreCase = true)
                                                }?.id
                                                if (firstQuoteId != null) {
                                                    onNavigateToRead(firstQuoteId, null)
                                                } else {
                                                    viewModel.selectAuthor(author.name)
                                                }
                                            }
                                            .padding(14.dp)
                                    ) {
                                        Column {
                                            Box(
                                                modifier = Modifier
                                                    .size(36.dp)
                                                    .clip(CircleShape)
                                                    .background(BrassAlpha12),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Outlined.Person,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(10.dp))
                                            Text(
                                                text = author.name,
                                                style = BodyStyle.copy(fontWeight = FontWeight.SemiBold),
                                                color = MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${author.quoteCount} quotes",
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
