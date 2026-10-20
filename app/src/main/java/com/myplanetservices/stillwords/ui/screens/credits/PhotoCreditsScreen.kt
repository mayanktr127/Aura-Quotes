package com.myplanetservices.stillwords.ui.screens.credits

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Scale
import com.myplanetservices.stillwords.data.model.PhotoCreditItem
import com.myplanetservices.stillwords.data.model.PhotoCreditsLoader
import com.myplanetservices.stillwords.ui.components.SectionHeader
import com.myplanetservices.stillwords.ui.theme.*

@Composable
fun PhotoCreditsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val creditsData = remember(context) { PhotoCreditsLoader.load(context) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("photo_credits_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Header: Pushed Screen Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("credits_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Photo Credits",
                    style = ScreenTitlePushedStyle,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pexels & AI Disclosure Banner Card
                item(key = "disclosure_banner") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(CardShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CardShape)
                            .padding(18.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CameraAlt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Photography & Background Art",
                                    fontFamily = FigtreeFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            if (creditsData.hasAiEntries && creditsData.statement.isNotBlank()) {
                                Text(
                                    text = creditsData.statement,
                                    fontFamily = FigtreeFontFamily,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Text(
                                text = "All photography is sourced under the free Pexels license with sincere gratitude to the photographers for capturing quiet, contemplative imagery.",
                                fontFamily = FigtreeFontFamily,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Section Header
                item(key = "photographers_header") {
                    SectionHeader(
                        title = "Contributing Photographers (${creditsData.credits.size})"
                    )
                }

                // Photographers List
                items(
                    items = creditsData.credits,
                    key = { "${it.theme}_${it.file}" }
                ) { credit ->
                    PhotoCreditCard(
                        credit = credit,
                        onOpenUrl = {
                            try {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(credit.sourceUrl)).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                // Ignore if browser is unavailable
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun PhotoCreditCard(
    credit: PhotoCreditItem,
    onOpenUrl: () -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val thumbWidthPx = remember(density) { density.run { 60.dp.roundToPx() } }
    val thumbHeightPx = remember(density) { density.run { 75.dp.roundToPx() } }
    val photoAssetPath = remember(credit.theme, credit.file) {
        "file:///android_asset/backgrounds/${credit.theme}/${credit.file}"
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, CardShape)
            .clickable(onClick = onOpenUrl)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Thumbnail Preview (display-size inexact Coil cache)
            val thumbShape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .size(width = 60.dp, height = 75.dp)
                    .clip(thumbShape)
                    .border(1.dp, MaterialTheme.colorScheme.outline, thumbShape)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoAssetPath)
                        .memoryCacheKey(photoAssetPath)
                        .size(thumbWidthPx, thumbHeightPx)
                        .precision(Precision.INEXACT)
                        .scale(Scale.FILL)
                        .crossfade(false)
                        .build(),
                    contentDescription = credit.photographer,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Info Column
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = credit.photographer,
                    fontFamily = NewsreaderFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = credit.theme.replace("-", " ").uppercase(),
                        fontFamily = FigtreeFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Tappable Link Icon
            IconButton(
                onClick = onOpenUrl,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = "View on Pexels",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
