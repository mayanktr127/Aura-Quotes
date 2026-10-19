package com.myplanetservices.stillwords.ui.screens.collection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.myplanetservices.stillwords.ui.components.EmptyState
import com.myplanetservices.stillwords.ui.components.PrimaryButton
import com.myplanetservices.stillwords.ui.components.SecondaryButton
import com.myplanetservices.stillwords.ui.components.StillwordsTextButton
import com.myplanetservices.stillwords.ui.theme.*

@Composable
fun CollectionDetailScreen(
    viewModel: CollectionDetailViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToDesigner: (String?) -> Unit,
    onNavigateToRead: (String?, List<String>?) -> Unit = { _, _ -> },
    onNavigateToSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val backgroundRepo = remember(context) { BackgroundRepository(context) }

    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var renameText by remember { mutableStateOf("") }

    // Dialog: Delete Collection
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Delete Collection?",
                    style = SectionTitleStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "This will delete the collection. Quotes within it will still remain in Stillwords.",
                    style = BodyStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                PrimaryButton(
                    text = "Delete",
                    onClick = {
                        showDeleteConfirmDialog = false
                        viewModel.deleteCollection(onDeleted = onNavigateBack)
                    },
                    modifier = Modifier.width(110.dp),
                    height = 44.dp
                )
            },
            dismissButton = {
                StillwordsTextButton(
                    text = "Cancel",
                    onClick = { showDeleteConfirmDialog = false }
                )
            }
        )
    }

    // Dialog: Rename Collection
    if (showRenameDialog) {
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Rename Collection",
                    style = SectionTitleStyle,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    singleLine = true,
                    shape = InputShape,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                PrimaryButton(
                    text = "Save",
                    onClick = {
                        if (renameText.isNotBlank()) {
                            viewModel.renameCollection(renameText)
                            showRenameDialog = false
                        }
                    },
                    modifier = Modifier.width(110.dp),
                    height = 44.dp
                )
            },
            dismissButton = {
                StillwordsTextButton(
                    text = "Cancel",
                    onClick = { showRenameDialog = false }
                )
            }
        )
    }

    val firstQuote = state.quotes.firstOrNull()
    val headerPhotoPath = remember(firstQuote) {
        firstQuote?.let { backgroundRepo.getBackgroundAssetPath(it.id, it.category) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // ====================================================================
        // 1. PHOTO HEADER (260dp) — Collection.dc.html lines 24-37
        // ====================================================================
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Header Background Photo
            if (headerPhotoPath != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(backgroundRepo.getCoilUri(headerPhotoPath))
                        .memoryCacheKey(headerPhotoPath)
                        .precision(Precision.INEXACT)
                        .scale(Scale.FILL)
                        .crossfade(false)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Multi-stop scrim fading to background (Collection.dc.html line 25)
            val headerScrim = Brush.verticalGradient(
                0.00f to Color(0x730F0E0C), // 45% black at top
                0.35f to Color(0x000F0E0C), // transparent at 35%
                1.00f to MaterialTheme.colorScheme.background // 100% background at bottom
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(headerScrim)
            )

            // Top Bar: Back left, Rename & Delete right (Collection.dc.html lines 26-32)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier.testTag("collection_detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = PhotoTextIvory,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            renameText = state.collection?.name ?: ""
                            showRenameDialog = true
                        },
                        modifier = Modifier.testTag("collection_rename_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Edit,
                            contentDescription = "Rename collection",
                            tint = PhotoTextIvory,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { showDeleteConfirmDialog = true },
                        modifier = Modifier.testTag("collection_detail_delete_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete collection",
                            tint = PhotoTextIvory,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Bottom of Header: Overline + Collection Title (Collection.dc.html lines 33-36)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val count = state.quotes.size
                Text(
                    text = "COLLECTION · $count ${if (count == 1) "QUOTE" else "QUOTES"}",
                    style = LabelOverlineStyle,
                    color = PhotoCategoryAccent
                )
                Text(
                    text = state.collection?.name ?: "Collection",
                    style = ScreenTitleStyle.copy(fontSize = 32.sp),
                    color = PhotoTextIvory,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // ====================================================================
        // 2. MAIN BODY: ACTION BUTTONS & QUOTE ROWS (Collection.dc.html lines 39-51)
        // ====================================================================
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("collection_quotes_list"),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Action Buttons Row: Read all & Add quotes (Collection.dc.html lines 40-43)
            item(key = "collection_actions", contentType = "actions") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PrimaryButton(
                        text = "Read all",
                        icon = Icons.Outlined.PlayArrow,
                        onClick = {
                            if (state.quotes.isNotEmpty()) {
                                onNavigateToRead(state.quotes.first().id, null)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        height = 48.dp,
                        enabled = state.quotes.isNotEmpty()
                    )

                    SecondaryButton(
                        text = "Add quotes",
                        icon = Icons.Outlined.Add,
                        onClick = onNavigateToSearch,
                        modifier = Modifier.weight(1f),
                        height = 48.dp
                    )
                }
            }

            // Description card (if present)
            val desc = state.collection?.description
            if (!desc.isNullOrBlank()) {
                item(key = "collection_description", contentType = "description") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(GroupShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, GroupShape)
                            .padding(14.dp)
                    ) {
                        Text(
                            text = desc,
                            style = BodyStyle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Empty Collection State
            if (state.quotes.isEmpty() && !state.isLoading) {
                item(key = "empty_collection", contentType = "empty") {
                    EmptyState(
                        title = "No quotes yet",
                        description = "Add quotes to this collection from any quote card or search view.",
                        icon = Icons.Outlined.CollectionsBookmark,
                        actionLabel = "Find quotes",
                        onActionClick = onNavigateToSearch
                    )
                }
            }

            // Quote rows with photo thumbnail and drag handle grip (Collection.dc.html lines 45-50)
            items(
                items = state.quotes,
                key = { "col_quote_${it.id}" },
                contentType = { "collection_quote_row" }
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
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 48x60 Photo Thumb (Collection.dc.html line 18)
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
                                    .size(width = 48.dp, height = 60.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(width = 48.dp, height = 60.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            )
                        }

                        // Quote Text & Category
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = quote.text,
                                style = QuoteSmallStyle.copy(fontSize = 16.sp, lineHeight = 21.sp),
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = quote.category,
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
