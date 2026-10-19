package com.myplanetservices.stillwords.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myplanetservices.stillwords.data.model.QuoteCollection
import com.myplanetservices.stillwords.data.repository.QuoteRepository
import com.myplanetservices.stillwords.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddToCollectionBottomSheet(
    quoteId: String,
    quoteRepository: QuoteRepository,
    onDismissRequest: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val collections by quoteRepository.collectionsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val activeCollectionIds by quoteRepository.getCollectionIdsForQuote(quoteId).collectAsStateWithLifecycle(initialValue = emptyList())

    val coroutineScope = rememberCoroutineScope()
    val bringIntoViewRequester = remember { BringIntoViewRequester() }

    var isCreatingCollection by remember { mutableStateOf(false) }
    var newCollectionName by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        shape = BottomSheetShape,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(StillwordsTheme.colors.muted)
            )
        },
        modifier = modifier.testTag("add_to_collection_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .imePadding()
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add to Collection",
                style = SectionTitleStyle,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (collections.isEmpty() && !isCreatingCollection) {
                Text(
                    text = "No collections yet. Create your first one below.",
                    style = BodyStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 280.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(
                        items = collections,
                        key = { "sheet_col_${it.id}" }
                    ) { collection ->
                        val isContained = activeCollectionIds.contains(collection.id)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    if (isContained) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar(
                                                message = "Already in ${collection.name}",
                                                duration = SnackbarDuration.Short
                                            )
                                        }
                                    } else {
                                        coroutineScope.launch {
                                            quoteRepository.addQuoteToCollection(collection.id, quoteId)
                                            onDismissRequest()
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Added to ${collection.name}",
                                                actionLabel = "Undo",
                                                duration = SnackbarDuration.Short
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                quoteRepository.removeQuoteFromCollection(collection.id, quoteId)
                                            }
                                        }
                                    }
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = collection.name,
                                    style = BodyStyle,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${collection.quoteCount} ${if (collection.quoteCount == 1) "quote" else "quotes"}",
                                    style = CaptionStyle,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            if (isContained) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "In collection",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (!isCreatingCollection) {
                OutlinedButton(
                    onClick = { isCreatingCollection = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("sheet_new_collection_button"),
                    shape = ButtonShape,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
                    )
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "New collection",
                        style = ButtonSecondaryStyle,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCollectionName,
                        onValueChange = { newCollectionName = it },
                        placeholder = { Text("Collection name", style = BodyStyle) },
                        modifier = Modifier
                            .weight(1f)
                            .bringIntoViewRequester(bringIntoViewRequester)
                            .onFocusEvent {
                                if (it.isFocused) {
                                    coroutineScope.launch {
                                        bringIntoViewRequester.bringIntoView()
                                    }
                                }
                            }
                            .testTag("sheet_collection_name_input"),
                        shape = InputShape,
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    PrimaryButton(
                        text = "Create",
                        height = 50.dp,
                        modifier = Modifier.width(100.dp),
                        enabled = newCollectionName.isNotBlank(),
                        onClick = {
                            val name = newCollectionName.trim()
                            if (name.isNotBlank()) {
                                coroutineScope.launch {
                                    val newColId = quoteRepository.createCollection(name)
                                    quoteRepository.addQuoteToCollection(newColId, quoteId)
                                    onDismissRequest()
                                    val result = snackbarHostState.showSnackbar(
                                        message = "Added to $name",
                                        actionLabel = "Undo",
                                        duration = SnackbarDuration.Short
                                    )
                                    if (result == SnackbarResult.ActionPerformed) {
                                        quoteRepository.removeQuoteFromCollection(newColId, quoteId)
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}
