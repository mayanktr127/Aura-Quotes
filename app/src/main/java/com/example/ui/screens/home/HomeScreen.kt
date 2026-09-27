package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.Quote
import com.example.ui.components.MoodChipRow
import com.example.ui.components.QuoteCard
import com.example.ui.components.StyleSelectorCarousel
import com.example.util.ImageExportUtil
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
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

    val todayDateFormatted = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Top App Bar / Greeting
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = state.greeting,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    letterSpacing = 0.2.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        Text(
                            text = state.subGreeting,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateToSearch,
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .testTag("home_search_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                                .testTag("home_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Section: Quote of the Day Hero
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "QUOTE OF THE DAY",
                        style = MaterialTheme.typography.labelSmall.copy(
                            letterSpacing = 2.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = todayDateFormatted,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                state.quoteOfTheDay?.let { qotd ->
                    QuoteCard(
                        quote = qotd,
                        cardStyle = state.heroStyle,
                        isHero = true,
                        onFavoriteToggle = { viewModel.toggleFavorite(qotd.id) },
                        onShare = { ImageExportUtil.shareText(context, qotd.text, qotd.author) },
                        onOpenDesigner = { onNavigateToDesigner(qotd.id) },
                        onClick = {
                            viewModel.recordHistory(qotd.id)
                            onNavigateToDetail(qotd.id)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Style Selector Carousel (Inspired by Image 1)
                StyleSelectorCarousel(
                    selectedStyle = state.heroStyle,
                    onStyleSelected = { viewModel.onHeroStyleSelected(it) }
                )
            }
        }

        // Section: "How are you feeling?" Mood Filter
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 12.dp)
            ) {
                Text(
                    text = "How are you feeling?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)
                )

                MoodChipRow(
                    moods = state.moods,
                    selectedMood = state.selectedMood,
                    onMoodSelected = { viewModel.onMoodSelected(it) },
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        // Section: Mood Quotes Carousel
        if (state.moodQuotes.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(state.moodQuotes, key = { it.id }) { quote ->
                        QuoteCard(
                            quote = quote,
                            cardStyle = quote.defaultStyle,
                            isHero = false,
                            modifier = Modifier.width(300.dp),
                            onFavoriteToggle = { viewModel.toggleFavorite(quote.id) },
                            onShare = { ImageExportUtil.shareText(context, quote.text, quote.author) },
                            onOpenDesigner = { onNavigateToDesigner(quote.id) },
                            onClick = {
                                viewModel.recordHistory(quote.id)
                                onNavigateToDetail(quote.id)
                            }
                        )
                    }
                }
            }
        }

        // Section: "What do you need right now?" Quick Needs
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 28.dp, bottom = 12.dp, start = 20.dp, end = 20.dp)
            ) {
                Text(
                    text = "What do you need right now?",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(10.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.categories) { category ->
                        val shape = RoundedCornerShape(16.dp)
                        Box(
                            modifier = Modifier
                                .clip(shape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onNavigateToCategory(category) }
                                .padding(horizontal = 14.dp, vertical = 9.dp)
                        ) {
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Section: "For You" Curated Recommendations
        if (state.forYouQuotes.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "For You",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        items(state.forYouQuotes, key = { "for_you_${it.id}" }) { quote ->
                            QuoteCard(
                                quote = quote,
                                cardStyle = quote.defaultStyle,
                                isHero = false,
                                modifier = Modifier.width(280.dp),
                                onFavoriteToggle = { viewModel.toggleFavorite(quote.id) },
                                onShare = { ImageExportUtil.shareText(context, quote.text, quote.author) },
                                onOpenDesigner = { onNavigateToDesigner(quote.id) },
                                onClick = {
                                    viewModel.recordHistory(quote.id)
                                    onNavigateToDetail(quote.id)
                                }
                            )
                        }
                    }
                }
            }
        }

        // Section: Saved Quotes Quick Access
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .clickable { onNavigateToSaved() }
                    .testTag("today_saved_quotes_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Your Saved Quotes",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Revisit your personal collection and reflections",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "View Saved",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Section: "Popular Today"
        if (state.popularQuotes.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp, start = 20.dp, end = 20.dp)
                ) {
                    Text(
                        text = "Popular Today",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            items(state.popularQuotes, key = { "popular_${it.id}" }) { quote ->
                Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                    QuoteCard(
                        quote = quote,
                        cardStyle = quote.defaultStyle,
                        isHero = false,
                        modifier = Modifier.fillMaxWidth(),
                        onFavoriteToggle = { viewModel.toggleFavorite(quote.id) },
                        onShare = { ImageExportUtil.shareText(context, quote.text, quote.author) },
                        onOpenDesigner = { onNavigateToDesigner(quote.id) },
                        onClick = {
                            viewModel.recordHistory(quote.id)
                            onNavigateToDetail(quote.id)
                        }
                    )
                }
            }
        }
    }
}
