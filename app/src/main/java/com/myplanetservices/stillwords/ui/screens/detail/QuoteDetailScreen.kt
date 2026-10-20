package com.myplanetservices.stillwords.ui.screens.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import kotlinx.coroutines.launch
import com.myplanetservices.stillwords.ui.components.AddToCollectionBottomSheet
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.myplanetservices.stillwords.data.model.Quote
import com.myplanetservices.stillwords.data.repository.BackgroundRepository
import com.myplanetservices.stillwords.ui.components.AutoShrinkQuoteText
import com.myplanetservices.stillwords.ui.components.PrimaryButton
import com.myplanetservices.stillwords.ui.components.ShareFormatBottomSheet
import com.myplanetservices.stillwords.ui.theme.*
import com.myplanetservices.stillwords.util.QuoteFormatUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(
    viewModel: QuoteDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDesigner: (String) -> Unit,
    onNavigateToOtherQuote: (String) -> Unit,
    onNavigateToSeeAll: ((category: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val backgroundRepo = remember(context) { BackgroundRepository(context) }
    val quote = state.quote

    var showShareSheet by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showCollectionSheet by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val reflectionBringIntoView = remember { BringIntoViewRequester() }

    BackHandler {
        onNavigateBack()
    }

    if (quote == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        return
    }

    val photoPath = remember(quote.id, quote.category) {
        backgroundRepo.getBackgroundAssetPath(quote.id, quote.category)
    }
    var hasImageError by remember(photoPath) { mutableStateOf(false) }

    if (showShareSheet) {
        ShareFormatBottomSheet(
            quoteText = quote.text,
            author = quote.author,
            category = quote.category,
            quoteId = quote.id,
            cardStyle = state.cardStyle,
            photoAssetPath = photoPath,
            onDismissRequest = { showShareSheet = false }
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink)
            .testTag("quote_detail_screen")
    ) {
        // Layer 1: Full-bleed Photo Background
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

        // Layer 2: Full-screen Scrim Gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(FullScreenPhotoScrim)
        )

        // Layer 3: Scrollable Content with Top & Bottom padding
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Header: Back · Category · More
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Ivory,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = quote.category.uppercase(),
                    style = LabelOverlineStyle,
                    color = PhotoCategoryAccent
                )

                Box {
                    IconButton(
                        onClick = { showMoreMenu = true },
                        modifier = Modifier.testTag("detail_more_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Ivory,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = { showMoreMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Share Card") },
                            onClick = {
                                showMoreMenu = false
                                showShareSheet = true
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.Share, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Copy Quote Text") },
                            onClick = {
                                showMoreMenu = false
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Stillwords Quote", "“${quote.text}” — ${quote.author}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Quote copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.ContentCopy, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Add to Collection") },
                            onClick = {
                                showMoreMenu = false
                                viewModel.setCollectionSheetOpen(true)
                            },
                            leadingIcon = {
                                Icon(Icons.Outlined.BookmarkAdd, contentDescription = null)
                            }
                        )
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(top = 28.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. Quote Text & Author & Classic Source Caption
                item(key = "quote_and_author_block") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AutoShrinkQuoteText(
                            text = quote.text,
                            initialFontSize = 32.sp,
                            minFontSize = 16.sp,
                            color = Ivory,
                            lineHeightMultiplier = 1.24f,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "— ${quote.author}",
                                fontFamily = NewsreaderFontFamily,
                                fontStyle = FontStyle.Italic,
                                fontSize = 15.sp,
                                color = PhotoAuthorIvory
                            )

                            Text(
                                text = "Photo · Pexels",
                                style = CaptionStyle.copy(fontSize = 11.sp),
                                color = Color(0x99F3EDE3)
                            )
                        }

                        // For classic quotes, show the source under author as a small caption
                        val classicSource = remember(quote) {
                            QuoteFormatUtil.formatClassicQuoteSource(quote)
                        }
                        if (classicSource != null) {
                            Text(
                                text = classicSource,
                                style = CaptionStyle.copy(fontSize = 12.sp),
                                color = Color(0xB3F3EDE3)
                            )
                        }
                    }
                }

                // 2. 5 Round 48 dp Action Buttons: Save / Collect / Copy / Share / Restyle
                item(key = "action_buttons_row") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        // 1. Save (Heart)
                        DetailActionButton(
                            icon = if (quote.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                            label = if (quote.isFavorite) "Saved" else "Save",
                            isSelected = quote.isFavorite,
                            accentColor = Brass,
                            onClick = {
                                coroutineScope.launch {
                                    if (!quote.isFavorite) {
                                        viewModel.toggleFavorite()
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Saved",
                                            actionLabel = "Add to collection",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            showCollectionSheet = true
                                        }
                                    } else {
                                        viewModel.toggleFavorite()
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Removed from Saved",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.toggleFavorite()
                                        }
                                    }
                                }
                            }
                        )

                        // 2. Collect
                        DetailActionButton(
                            icon = Icons.Outlined.BookmarkAdd,
                            label = "Collect",
                            onClick = { showCollectionSheet = true }
                        )

                        // 3. Copy
                        DetailActionButton(
                            icon = Icons.Outlined.ContentCopy,
                            label = "Copy",
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Stillwords Quote", "“${quote.text}” — ${quote.author}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Quote copied", Toast.LENGTH_SHORT).show()
                            }
                        )

                        // 4. Share
                        DetailActionButton(
                            icon = Icons.Outlined.Share,
                            label = "Share",
                            onClick = { showShareSheet = true }
                        )

                        // 5. Restyle (Brass-filled)
                        DetailActionButton(
                            icon = Icons.Outlined.Edit,
                            label = "Restyle",
                            isRestyle = true,
                            onClick = { onNavigateToDesigner(quote.id) }
                        )
                    }
                }

                // 3. Reflection Note ("Take a moment" / write your private reflection)
                item(key = "reflection_card") {
                    Card(
                        shape = GroupShape,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Create,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Take a moment",
                                        style = SectionTitleStyle,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                if (!state.isReflectionEditing && state.reflectionText.isNotBlank()) {
                                    TextButton(onClick = { viewModel.setReflectionEditing(true) }) {
                                        Text(
                                            text = "Edit",
                                            style = ButtonSecondaryStyle,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "What does this quote mean to you today?",
                                style = CaptionStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            if (state.isReflectionEditing || state.reflectionText.isBlank()) {
                                OutlinedTextField(
                                    value = state.reflectionText,
                                    onValueChange = { viewModel.onReflectionTextChanged(it) },
                                    placeholder = {
                                        Text(
                                            "Write your private reflection here...",
                                            style = BodyStyle,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .bringIntoViewRequester(reflectionBringIntoView)
                                        .onFocusEvent {
                                            if (it.isFocused) {
                                                coroutineScope.launch {
                                                    reflectionBringIntoView.bringIntoView()
                                                }
                                            }
                                        }
                                        .testTag("reflection_input"),
                                    shape = RoundedCornerShape(14.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                                    ),
                                    minLines = 3,
                                    maxLines = 6
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    PrimaryButton(
                                        text = "Save Reflection",
                                        height = 44.dp,
                                        modifier = Modifier.widthIn(max = 180.dp),
                                        onClick = {
                                            viewModel.saveReflection()
                                            Toast.makeText(context, "Reflection saved", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.background,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = state.reflectionText,
                                        style = BodyStyle,
                                        color = MaterialTheme.colorScheme.onBackground,
                                        modifier = Modifier.padding(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. "More on {topic}" Horizontal Mini Cards Row
                if (state.relatedQuotes.isNotEmpty()) {
                    item(key = "more_on_topic_section") {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "More on ${quote.category.lowercase()}",
                                    style = SectionTitleStyle,
                                    color = Ivory
                                )

                                if (onNavigateToSeeAll != null) {
                                    Text(
                                        text = "See all",
                                        style = ButtonSecondaryStyle,
                                        color = Brass,
                                        modifier = Modifier
                                            .clickable { onNavigateToSeeAll(quote.category) }
                                            .padding(4.dp)
                                    )
                                }
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                itemsIndexed(
                                    state.relatedQuotes,
                                    key = { index, relQuote -> "rel_${relQuote.id}_$index" }
                                ) { _, relQuote ->
                                    val relPhoto = remember(relQuote.id, relQuote.category) {
                                        backgroundRepo.getBackgroundAssetPath(relQuote.id, relQuote.category)
                                    }
                                    DetailMiniCard(
                                        quoteText = relQuote.text,
                                        photoPath = relPhoto,
                                        onClick = { onNavigateToOtherQuote(relQuote.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add to Collection Bottom Sheet
        if (showCollectionSheet) {
            AddToCollectionBottomSheet(
                quoteId = quote.id,
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
                .padding(bottom = 16.dp)
        )
    }
}

@Composable
private fun DetailActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    isRestyle: Boolean = false,
    accentColor: Color = Brass
) {
    val circleBg = when {
        isRestyle -> accentColor
        isSelected -> BrassAlpha12
        else -> Color(0x1AF3EDE3)
    }
    val circleBorder = when {
        isRestyle -> accentColor
        isSelected -> accentColor
        else -> Color(0x29F3EDE3)
    }
    val iconTint = when {
        isRestyle -> Ink
        isSelected -> accentColor
        else -> Ivory
    }
    val labelColor = when {
        isSelected -> accentColor
        else -> Ivory
    }

    Column(
        modifier = modifier
            .width(56.dp)
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(circleBg)
                .border(1.dp, circleBorder, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(22.dp)
            )
        }

        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = FigtreeFontFamily,
            fontWeight = FontWeight.Medium,
            color = labelColor,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}

@Composable
private fun DetailMiniCard(
    quoteText: String,
    photoPath: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    com.myplanetservices.stillwords.ui.components.MiniQuoteCard(
        quoteText = quoteText,
        photoPath = photoPath,
        onClick = onClick,
        modifier = modifier
    )
}
