package com.myplanetservices.stillwords.ui.screens.search

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.myplanetservices.stillwords.data.model.BackgroundTheme
import com.myplanetservices.stillwords.data.repository.BackgroundRepository
import com.myplanetservices.stillwords.ui.components.SecondaryButton
import com.myplanetservices.stillwords.ui.components.StillwordsChip
import com.myplanetservices.stillwords.ui.theme.*

@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToDesigner: (String?) -> Unit,
    onNavigateToRead: (String?, List<String>?) -> Unit = { _, _ -> },
    onNavigateToExplore: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val backgroundRepo = remember(context) { BackgroundRepository(context) }
    val focusRequester = remember { FocusRequester() }
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    var queryText by rememberSaveable { mutableStateOf(state.query) }

    LaunchedEffect(state.query) {
        if (queryText != state.query) {
            queryText = state.query
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // ====================================================================
        // 1. TOP HEADER & SEARCH INPUT (Search.dc.html lines 28-36)
        // ====================================================================
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(44.dp)
                    .testTag("search_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Search Bar Input Container
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(InputShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.primary, InputShape)
                    .padding(start = 14.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (queryText.isEmpty()) {
                        Text(
                            text = "Search words, topics or authors",
                            style = BodyStyle.copy(fontSize = 15.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    BasicTextField(
                        value = queryText,
                        onValueChange = {
                            queryText = it
                            viewModel.onQueryChanged(it)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onFocusEvent {
                                if (it.isFocused) {
                                    coroutineScope.launch {
                                        bringIntoViewRequester.bringIntoView()
                                    }
                                }
                            }
                            .testTag("search_input_field"),
                        textStyle = BodyStyle.copy(
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        ),
                        singleLine = true,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() })
                    )
                }

                if (queryText.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            queryText = ""
                            viewModel.clearQuery()
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Clear search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // ====================================================================
        // 2. FILTER CHIPS (All, Quotes, Topics, Authors - Search.dc.html 39-43)
        // ====================================================================
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                items = SearchFilter.entries,
                key = { "filter_${it.name}" },
                contentType = { "filter_chip" }
            ) { filter ->
                val selected = state.selectedFilter == filter
                Box(
                    modifier = Modifier
                        .height(36.dp)
                        .clip(ChipShape)
                        .background(if (selected) BrassAlpha12 else Color.Transparent)
                        .border(
                            1.dp,
                            if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                            ChipShape
                        )
                        .clickable { viewModel.onFilterSelected(filter) }
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.label,
                        style = ChipStyle,
                        color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ====================================================================
        // 3. SEARCH RESULTS OR EMPTY STATES
        // ====================================================================
        val searchViewMode = remember(state.query.isBlank(), state.results.isEmpty(), state.matchingTopics.isEmpty()) {
            when {
                state.query.isBlank() -> 0
                state.results.isEmpty() && state.matchingTopics.isEmpty() -> 1
                else -> 2
            }
        }

        AnimatedContent(
            targetState = searchViewMode,
            transitionSpec = {
                fadeIn(animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing)) togetherWith
                    fadeOut(animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing))
            },
            label = "search_crossfade"
        ) { mode ->
            when (mode) {
                0 -> {
                    // Initial Empty Query State: suggestions
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("search_suggestions_list"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item(key = "try_searching_label") {
                    Text(
                        text = "TRY SEARCHING",
                        style = LabelOverlineStyle,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                item(key = "suggestions_row") {
                    val initialSuggestions = listOf(
                        "Peace", "Courage", "Mindfulness", "Wisdom",
                        "Marcus Aurelius", "Seneca", "Rumi", "Gratitude", "Discipline"
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(
                                items = initialSuggestions,
                                key = { "init_sugg_$it" },
                                contentType = { "suggestion_chip" }
                            ) { suggestion ->
                                StillwordsChip(
                                    text = suggestion,
                                    selected = false,
                                    onClick = { viewModel.onSuggestionSelected(suggestion) }
                                )
                            }
                        }
                    }
                }
            }
        }

        1 -> {
            // ================================================================
            // NO RESULTS STATE (EmptySearch.dc.html lines 31-52)
            // ================================================================
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .testTag("search_no_results"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(28.dp)
            ) {
                Spacer(modifier = Modifier.height(28.dp))

                // Dashed Circle Icon Container
                val emptyBorderColor = StillwordsTheme.colors.emptyDotBorder
                val brassColor = MaterialTheme.colorScheme.primary

                Box(
                    modifier = Modifier.size(96.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = emptyBorderColor,
                            style = Stroke(
                                width = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                            )
                        )
                    }
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = brassColor,
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Title & Subtitle
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "No quotes for “${state.query.trim()}”",
                        style = ScreenTitlePushedStyle.copy(fontSize = 24.sp),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "Try a shorter word, a feeling, or one of the topics below.",
                        style = BodyStyle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.widthIn(max = 280.dp)
                    )
                }

                // "TRY INSTEAD" Chips
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "TRY INSTEAD",
                        style = LabelOverlineStyle,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(
                                items = state.suggestions,
                                key = { "sugg_$it" },
                                contentType = { "suggestion_chip" }
                            ) { category ->
                                StillwordsChip(
                                    text = category,
                                    selected = false,
                                    onClick = { viewModel.onSuggestionSelected(category) }
                                )
                            }
                        }
                    }
                }

                // "Browse all topics" SecondaryButton
                SecondaryButton(
                    text = "Browse all topics",
                    icon = Icons.Outlined.Explore,
                    onClick = onNavigateToExplore,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        else -> {
            // ================================================================
            // RESULTS LIST (Search.dc.html lines 45-57)
            // ================================================================
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("search_results_list"),
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                // Summary count overline (e.g. "24 QUOTES · 2 TOPICS")
                item(key = "results_count_summary") {
                    val countLabel = buildString {
                        if (state.totalQuotesCount > 0) {
                            append("${state.totalQuotesCount} ${if (state.totalQuotesCount == 1) "QUOTE" else "QUOTES"}")
                        }
                        if (state.totalTopicsCount > 0) {
                            if (isNotEmpty()) append(" · ")
                            append("${state.totalTopicsCount} ${if (state.totalTopicsCount == 1) "TOPIC" else "TOPICS"}")
                        }
                    }
                    Text(
                        text = countLabel.uppercase(),
                        style = LabelOverlineStyle,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }

                // Matching Topics row (Search.dc.html lines 47-50)
                if (state.matchingTopics.isNotEmpty()) {
                    item(key = "matching_topics_row") {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            items(state.matchingTopics, key = { "match_topic_$it" }) { topic ->
                                val photoPath = remember(topic) { backgroundRepo.getCategoryThumbnailAssetPath(topic) }

                                Row(
                                    modifier = Modifier
                                        .height(56.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                        .clickable { onNavigateToRead(null, listOf(topic)) }
                                        .padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
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
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer)
                                        )
                                    }

                                    Text(
                                        text = topic,
                                        style = BodyStyle.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // Result Rows with hairline dividers & highlighted query (Search.dc.html lines 52-57)
                items(
                    items = state.results,
                    key = { "search_res_${it.id}" },
                    contentType = { "search_result_row" }
                ) { quote ->
                    val photoPath = remember(quote.category) { backgroundRepo.getCategoryThumbnailAssetPath(quote.category) }

                    Column(
                        modifier = Modifier
                            .animateItem()
                            .fillMaxWidth()
                            .clickable {
                                viewModel.recordHistory(quote.id)
                                onNavigateToDetail(quote.id)
                            }
                    ) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = MaterialTheme.colorScheme.outline
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
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

                            // Quote Text with highlight + Subtitle
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                HighlightedText(
                                    text = quote.text,
                                    query = state.query,
                                    style = QuoteSmallStyle.copy(fontSize = 17.sp, lineHeight = 23.sp),
                                    highlightColor = BrassAlpha22
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

/**
 * Renders text with the search query highlighted with [highlightColor] background
 * matching <mark> in Search.dc.html.
 */
@Composable
fun HighlightedText(
    text: String,
    query: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    highlightColor: Color = BrassAlpha22,
    textColor: Color = MaterialTheme.colorScheme.onBackground
) {
    val trimmed = query.trim()
    val annotatedString = remember(text, trimmed, highlightColor, textColor) {
        buildAnnotatedString {
            if (trimmed.isEmpty()) {
                append(text)
                return@buildAnnotatedString
            }
            var startIndex = 0
            while (startIndex < text.length) {
                val matchIndex = text.indexOf(trimmed, startIndex, ignoreCase = true)
                if (matchIndex == -1) {
                    append(text.substring(startIndex))
                    break
                }
                if (matchIndex > startIndex) {
                    append(text.substring(startIndex, matchIndex))
                }
                val endIndex = matchIndex + trimmed.length
                withStyle(SpanStyle(background = highlightColor)) {
                    append(text.substring(matchIndex, endIndex))
                }
                startIndex = endIndex
            }
        }
    }
    Text(
        text = annotatedString,
        style = style,
        color = textColor,
        modifier = modifier
    )
}
