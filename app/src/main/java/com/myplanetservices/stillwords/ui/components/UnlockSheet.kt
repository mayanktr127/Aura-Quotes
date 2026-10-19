package com.myplanetservices.stillwords.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplanetservices.stillwords.BuildConfig
import com.myplanetservices.stillwords.data.model.BackgroundTheme
import com.myplanetservices.stillwords.data.repository.BackgroundRepository
import com.myplanetservices.stillwords.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnlockSheet(
    lockedLookName: String,
    isUnlocked: Boolean,
    onWatchVideo: () -> Unit,
    onDismiss: () -> Unit,
    onApplyLook: () -> Unit,
    modifier: Modifier = Modifier,
    expiryTimestamp: Long = 0L
) {
    val context = LocalContext.current
    val backgroundRepo = remember(context) { BackgroundRepository(context) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        scrimColor = Color(0x9E080706),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF4A443D))
            )
        },
        modifier = modifier.testTag("unlock_sheet")
    ) {
        AnimatedContent(
            targetState = isUnlocked,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "UnlockSheetStateTransition"
        ) { unlocked ->
            if (unlocked) {
                // Success State — docs/DESIGN_SPEC.md Screen 14 & UnlockSheet.dc.html
                val formattedExpiry = remember(expiryTimestamp) {
                    val targetTime = if (expiryTimestamp > 0L) expiryTimestamp else (System.currentTimeMillis() + 24 * 60 * 60 * 1000L)
                    val sdf = SimpleDateFormat("h:mm a 'tomorrow'", Locale.getDefault())
                    sdf.format(Date(targetTime))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 34.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    Spacer(modifier = Modifier.height(4.dp))

                    // Accent check circle (64 x 64 dp)
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                            .border(1.dp, MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Premium looks unlocked",
                            style = ScreenTitleStyle.copy(fontSize = 26.sp),
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Enjoy them until $formattedExpiry.",
                            style = BodyStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                            textAlign = TextAlign.Center
                        )
                    }

                    PrimaryButton(
                        text = "Use $lockedLookName",
                        onClick = onApplyLook,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("unlock_apply_button")
                    )
                }
            } else {
                // Locked State — docs/DESIGN_SPEC.md Screen 14 & UnlockSheet.dc.html
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 34.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header text
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "PREMIUM LOOK · ${lockedLookName.uppercase()}",
                            style = LabelOverlineStyle.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = "Unlock every premium look for 24 hours",
                            style = ScreenTitleStyle.copy(fontSize = 26.sp, lineHeight = 32.sp),
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Watch one short video and use all locked backgrounds, fonts and frames until this time tomorrow.",
                            style = BodyStyle.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 21.sp
                            )
                        )
                    }

                    // Look preview thumbnails row (64 x 80 dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val previewThemes = listOf(
                            BackgroundTheme.FOREST_BOTANICAL,
                            BackgroundTheme.SUNRISE_ROADS,
                            BackgroundTheme.QUIET_SPACES
                        )

                        previewThemes.forEach { theme ->
                            val thumbPath = remember(theme) { backgroundRepo.getThumbnailAssetPath(theme) }
                            val thumbBitmap = remember(thumbPath) {
                                thumbPath?.let { backgroundRepo.getThumbnailBitmap(it, 160) }
                            }

                            Box(
                                modifier = Modifier
                                    .size(width = 64.dp, height = 80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surface)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            ) {
                                if (thumbBitmap != null) {
                                    Image(
                                        bitmap = thumbBitmap.asImageBitmap(),
                                        contentDescription = theme.displayName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }

                        // "+ more" swatch
                        Box(
                            modifier = Modifier
                                .size(width = 64.dp, height = 80.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.background)
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+ more",
                                style = ChipStyle.copy(fontSize = 13.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Actions
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Per requirement 3:
                        // The "Watch video" button shows the success state only in debug builds; in release it is hidden until Phase 5.
                        if (BuildConfig.DEBUG) {
                            PrimaryButton(
                                text = "Watch video",
                                icon = Icons.Default.PlayArrow,
                                onClick = onWatchVideo,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("unlock_watch_video_button")
                            )
                            Text(
                                text = "Ad · about 30 seconds",
                                style = CaptionStyle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        StillwordsTextButton(
                            text = "Not now",
                            onClick = onDismiss,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("unlock_not_now_button")
                        )
                    }

                    Text(
                        text = "If no video is available, try again in a little while. Your design is kept.",
                        style = CaptionStyle.copy(fontSize = 11.sp, lineHeight = 16.sp),
                        color = StillwordsTheme.colors.muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
