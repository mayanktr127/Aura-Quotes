package com.myplanetservices.stillwords.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myplanetservices.stillwords.data.model.CardAspectRatio
import com.myplanetservices.stillwords.data.model.CardStyle
import com.myplanetservices.stillwords.data.model.PhotoFilter
import com.myplanetservices.stillwords.data.model.QuoteFont
import com.myplanetservices.stillwords.data.preferences.UserPreferencesRepository
import com.myplanetservices.stillwords.data.repository.BackgroundRepository
import com.myplanetservices.stillwords.util.ImageExportUtil
import kotlinx.coroutines.launch

/**
 * Bottom sheet allowing users to choose an export format ("Post 4:5", "Story 9:16", "Square 1:1")
 * before sharing or saving a quote card. Remembers the user's last chosen format in DataStore.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareFormatBottomSheet(
    quoteText: String,
    author: String,
    category: String,
    quoteId: String? = null,
    cardStyle: CardStyle = CardStyle.NOIR_STILLWORDS,
    photoAssetPath: String? = null,
    photoFilter: PhotoFilter = PhotoFilter.ORIGINAL,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesRepository = remember { UserPreferencesRepository(context.applicationContext) }
    val userPrefs by preferencesRepository.userPreferencesFlow.collectAsStateWithLifecycle(initialValue = null)

    val backgroundRepo = remember { BackgroundRepository(context.applicationContext) }
    val effectivePhoto = photoAssetPath ?: if (quoteId != null) {
        backgroundRepo.getBackgroundAssetPath(quoteId, category)
    } else null

    var selectedFormat by remember {
        mutableStateOf(
            when (userPrefs?.defaultAspectRatio) {
                "STORY_9_16" -> CardAspectRatio.STORY_9_16
                "SQUARE_1_1" -> CardAspectRatio.SQUARE_1_1
                else -> CardAspectRatio.PORTRAIT_4_5
            }
        )
    }

    // Sync with remembered preference once loaded if not changed by user
    LaunchedEffect(userPrefs?.defaultAspectRatio) {
        userPrefs?.defaultAspectRatio?.let { savedRatio ->
            val matchingRatio = when (savedRatio) {
                "STORY_9_16" -> CardAspectRatio.STORY_9_16
                "SQUARE_1_1" -> CardAspectRatio.SQUARE_1_1
                else -> CardAspectRatio.PORTRAIT_4_5
            }
            selectedFormat = matchingRatio
        }
    }

    var isExporting by remember { mutableStateOf(false) }

    val formatOptions = listOf(
        Triple(CardAspectRatio.PORTRAIT_4_5, "Post 4:5", "Feed"),
        Triple(CardAspectRatio.STORY_9_16, "Story 9:16", "Story"),
        Triple(CardAspectRatio.SQUARE_1_1, "Square 1:1", "Classic")
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Export Quote Card",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "“${quoteText.take(70)}${if (quoteText.length > 70) "…" else ""}”",
                    style = MaterialTheme.typography.bodySmall.copy(fontStyle = FontStyle.Italic),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Format Options: Post 4:5, Story 9:16, Square 1:1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                formatOptions.forEach { (ratio, label, sublabel) ->
                    val isSelected = ratio == selectedFormat
                    val shape = RoundedCornerShape(16.dp)
                    val borderColor = if (isSelected) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(shape)
                            .background(bgColor)
                            .border(if (isSelected) 2.dp else 1.dp, borderColor, shape)
                            .clickable {
                                selectedFormat = ratio
                                scope.launch {
                                    preferencesRepository.setDefaultAspectRatio(ratio.name)
                                }
                            }
                            .padding(vertical = 12.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .background(MaterialTheme.colorScheme.tertiary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(18.dp))
                            }

                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = sublabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Action Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Share Card Button
                Button(
                    onClick = {
                        if (!isExporting) {
                            isExporting = true
                            scope.launch {
                                try {
                                    preferencesRepository.setDefaultAspectRatio(selectedFormat.name)
                                    val bitmap = ImageExportUtil.createQuoteBitmap(
                                        context = context,
                                        quoteText = quoteText,
                                        author = author,
                                        category = category,
                                        cardStyle = cardStyle,
                                        quoteFont = QuoteFont.EDITORIAL_SERIF,
                                        aspectRatio = selectedFormat,
                                        showWatermark = true,
                                        photoAssetPath = effectivePhoto,
                                        photoFilter = photoFilter
                                    )
                                    val uri = ImageExportUtil.cacheBitmapForSharing(context, bitmap)
                                    isExporting = false
                                    onDismissRequest()
                                    if (uri != null) {
                                        ImageExportUtil.shareImage(context, uri, quoteText, author)
                                    } else {
                                        ImageExportUtil.shareText(context, quoteText, author)
                                    }
                                } catch (e: Exception) {
                                    isExporting = false
                                    onDismissRequest()
                                    ImageExportUtil.shareText(context, quoteText, author)
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = !isExporting
                ) {
                    if (isExporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Share Card",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                // Save to Photos Button
                OutlinedButton(
                    onClick = {
                        if (!isExporting) {
                            isExporting = true
                            scope.launch {
                                try {
                                    preferencesRepository.setDefaultAspectRatio(selectedFormat.name)
                                    val bitmap = ImageExportUtil.createQuoteBitmap(
                                        context = context,
                                        quoteText = quoteText,
                                        author = author,
                                        category = category,
                                        cardStyle = cardStyle,
                                        quoteFont = QuoteFont.EDITORIAL_SERIF,
                                        aspectRatio = selectedFormat,
                                        showWatermark = true,
                                        photoAssetPath = effectivePhoto,
                                        photoFilter = photoFilter
                                    )
                                    val uri = ImageExportUtil.saveBitmapToGallery(context, bitmap)
                                    isExporting = false
                                    onDismissRequest()
                                    if (uri != null) {
                                        Toast.makeText(context, "Quote saved to Pictures/Stillwords", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Could not save to photos", Toast.LENGTH_SHORT).show()
                                    }
                                } catch (e: Exception) {
                                    isExporting = false
                                    onDismissRequest()
                                    Toast.makeText(context, "Failed to save: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    ),
                    enabled = !isExporting
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Download,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save to Photos",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
