package com.myplanetservices.stillwords.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myplanetservices.stillwords.data.model.Quote
import com.myplanetservices.stillwords.data.repository.BackgroundRepository
import com.myplanetservices.stillwords.ui.components.AddToCollectionBottomSheet
import com.myplanetservices.stillwords.ui.components.PhotoQuoteCard
import com.myplanetservices.stillwords.ui.components.ShareFormatBottomSheet
import com.myplanetservices.stillwords.ui.components.StillwordsChip
import com.myplanetservices.stillwords.ui.components.WeekDots
import com.myplanetservices.stillwords.ui.theme.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToRead: (initialQuoteId: String?, categories: List<String>?) -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToDesigner: (String?) -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToCategory: (String) -> Unit,
    onNavigateToSaved: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val backgroundRepo = remember(context) { BackgroundRepository(context) }

    var shareQuote by remember { mutableStateOf<Quote?>(null) }
    var showCollectionSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    if (shareQuote != null) {
        val q = shareQuote!!
        ShareFormatBottomSheet(
            quoteText = q.text,
            author = q.author,
            category = q.category,
            quoteId = q.id,
            photoAssetPath = backgroundRepo.getBackgroundAssetPath(q.id, q.category),
            onDismissRequest = { shareQuote = null }
        )
    }

    val cal = remember { Calendar.getInstance() }
    val dayOfYear = remember { cal.get(Calendar.DAY_OF_YEAR) }
    val formattedDate = remember {
        SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
    }
    val datelineText = remember(dayOfYear, formattedDate) {
        "No. $dayOfYear · $formattedDate"
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .testTag("home_screen")
        ) {
        // Top Header — docs/DESIGN_SPEC.md Sec. 6 (Screen 4) & Main.dc.html
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = datelineText,
                    style = LabelOverlineStyle,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = state.greeting,
                    style = ScreenTitleStyle,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateToSearch,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("home_search_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }

                IconButton(
                    onClick = onNavigateToSettings,
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 1. PhotoQuoteCard with Quote of the Day
            item(key = "hero_quote_card") {
                state.quoteOfTheDay?.let { qotd ->
                    val photoPath = remember(qotd.id, qotd.category) {
                        backgroundRepo.getBackgroundAssetPath(qotd.id, qotd.category)
                    }

                    PhotoQuoteCard(
                        quoteText = qotd.text,
                        author = qotd.author,
                        category = qotd.category,
                        photoPath = photoPath,
                        isFavorite = qotd.isFavorite,
                        onFavoriteToggle = {
                            coroutineScope.launch {
                                if (!qotd.isFavorite) {
                                    viewModel.toggleFavorite(qotd.id)
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Saved",
                                        actionLabel = "Add to collection",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        showCollectionSheet = true
                                    }
                                } else {
                                    viewModel.toggleFavorite(qotd.id)
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Removed from Saved",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        viewModel.toggleFavorite(qotd.id)
                                    }
                                }
                            }
                        },
                        onShare = { shareQuote = qotd },
                        onDownload = { shareQuote = qotd },
                        onEdit = { onNavigateToDesigner(qotd.id) },
                        onClick = { onNavigateToRead(qotd.id, null) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 2. Week Card with WeekDots ("{n} quiet days")
            item(key = "week_stillness_card") {
                WeekDots(
                    daysCompleted = state.quietDaysThisWeek,
                    title = "${state.quietDaysThisWeek} quiet days",
                    subtitle = "Your week of stillness",
                    dayStatuses = state.weekDayStatuses,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 3. "For this moment" Mood Chips
            item(key = "for_this_moment_section") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "For this moment",
                        style = SectionTitleStyle,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    val moodMappings = remember {
                        listOf(
                            "Calm" to listOf("Peace", "Mindfulness", "Healing", "Gratitude"),
                            "Focused" to listOf("Discipline", "Study", "Work", "Wisdom"),
                            "Hopeful" to listOf("Growth", "Life", "Happiness", "Creativity"),
                            "Need a lift" to listOf("Motivation", "Courage", "Self-Love", "Confidence")
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        moodMappings.forEach { (moodName, categories) ->
                            StillwordsChip(
                                text = moodName,
                                selected = false,
                                onClick = {
                                    onNavigateToRead(null, categories)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showCollectionSheet && state.quoteOfTheDay != null) {
        AddToCollectionBottomSheet(
            quoteId = state.quoteOfTheDay!!.id,
            quoteRepository = viewModel.repository,
            onDismissRequest = { showCollectionSheet = false },
            snackbarHostState = snackbarHostState
        )
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .navigationBarsPadding()
            .imePadding()
            .padding(bottom = 8.dp)
    )
}
}
