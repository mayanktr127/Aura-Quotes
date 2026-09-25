package com.example.ui.screens.designer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.CardAspectRatio
import com.example.data.model.CardStyle
import com.example.data.model.QuoteFont
import com.example.ui.components.StyleSelectorCarousel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDesignerScreen(
    viewModel: QuoteDesignerViewModel,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var selectedToolTab by remember { mutableStateOf(0) } // 0: Styles & Ratio, 1: Typography, 2: Text

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quote Designer",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                },
                actions = {
                    // Save as User Quote
                    IconButton(
                        onClick = {
                            viewModel.saveAsUserQuote()
                            Toast.makeText(context, "Saved to your quotes library", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("designer_save_quote_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.BookmarkAdd,
                            contentDescription = "Save Quote",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Share
                    IconButton(
                        onClick = {
                            viewModel.shareImage(context) { success ->
                                if (!success) {
                                    Toast.makeText(context, "Failed to share image", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.testTag("designer_share_image_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Card",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // Export / Save Image
                    IconButton(
                        onClick = {
                            viewModel.exportImage(context) { success, msg ->
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("designer_export_image_button")
                    ) {
                        if (state.isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.tertiary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Download,
                                contentDescription = "Export Image",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Preview Canvas Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                val cardShape = RoundedCornerShape(24.dp)
                val cardStyle = state.cardStyle

                val bgMod = if (cardStyle.gradient != null) {
                    Modifier.background(cardStyle.gradient, cardShape)
                } else {
                    Modifier.background(cardStyle.backgroundColor, cardShape)
                }

                val borderMod = if (cardStyle.borderColor != Color.Transparent) {
                    Modifier.border(1.2.dp, cardStyle.borderColor, cardShape)
                } else Modifier

                // Aspect ratio sizing
                val canvasModifier = when (state.aspectRatio) {
                    CardAspectRatio.SQUARE_1_1 -> Modifier.fillMaxWidth().aspectRatio(1f)
                    CardAspectRatio.PORTRAIT_4_5 -> Modifier.fillMaxWidth(0.85f).aspectRatio(4f / 5f)
                    CardAspectRatio.STORY_9_16 -> Modifier.fillMaxWidth(0.72f).aspectRatio(9f / 16f)
                    CardAspectRatio.LANDSCAPE_16_9 -> Modifier.fillMaxWidth().aspectRatio(16f / 9f)
                }

                Box(
                    modifier = canvasModifier
                        .shadow(12.dp, cardShape, spotColor = Color(0x40000000))
                        .then(bgMod)
                        .then(borderMod)
                        .clip(cardShape)
                        .padding(20.dp)
                        .testTag("designer_canvas_preview"),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Top category
                        Text(
                            text = "“  ${state.category.uppercase()}  ”",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = cardStyle.authorColor,
                            textAlign = TextAlign.Center
                        )

                        // Center quote text
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
                        ) {
                            val displayText = if (state.quoteFont.isAllCaps) state.quoteText.uppercase() else state.quoteText
                            val quoteFontSize = when (state.aspectRatio) {
                                CardAspectRatio.STORY_9_16 -> if (displayText.length > 100) 18.sp else 22.sp
                                CardAspectRatio.LANDSCAPE_16_9 -> if (displayText.length > 100) 16.sp else 20.sp
                                else -> if (displayText.length > 100) 19.sp else 24.sp
                            }

                            Text(
                                text = displayText,
                                fontSize = quoteFontSize,
                                lineHeight = (quoteFontSize.value * 1.35).sp,
                                fontFamily = state.quoteFont.fontFamily,
                                fontStyle = state.quoteFont.fontStyle,
                                fontWeight = state.quoteFont.fontWeight,
                                letterSpacing = state.quoteFont.letterSpacing.sp,
                                textAlign = TextAlign.Center,
                                color = cardStyle.textColor
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "— ${state.author}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    fontFamily = FontFamily.Serif
                                ),
                                color = cardStyle.authorColor,
                                textAlign = TextAlign.Center
                            )
                        }

                        // Bottom watermark
                        if (state.showWatermark) {
                            Text(
                                text = "AURA • DAILY INSPIRATION",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    letterSpacing = 1.5.sp
                                ),
                                color = cardStyle.authorColor.copy(alpha = 0.6f)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            // Customization Tool Selector Tabs
            TabRow(
                selectedTabIndex = selectedToolTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedToolTab == 0,
                    onClick = { selectedToolTab = 0 },
                    text = { Text("Style & Ratio") }
                )
                Tab(
                    selected = selectedToolTab == 1,
                    onClick = { selectedToolTab = 1 },
                    text = { Text("Typography") }
                )
                Tab(
                    selected = selectedToolTab == 2,
                    onClick = { selectedToolTab = 2 },
                    text = { Text("Edit Text") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // TAB 0: Style & Aspect Ratio
            if (selectedToolTab == 0) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Aspect ratio presets (Instagram post, story/wallpaper, etc.)
                    Text(
                        text = "Format & Aspect Ratio",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CardAspectRatio.values().forEach { ratio ->
                            val isSelected = ratio == state.aspectRatio
                            val shape = RoundedCornerShape(14.dp)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(shape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { viewModel.onAspectRatioSelected(ratio) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = ratio.label,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = ratio.sublabel.split("/").firstOrNull()?.trim() ?: "",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    // Style Carousel
                    StyleSelectorCarousel(
                        selectedStyle = state.cardStyle,
                        onStyleSelected = { viewModel.onStyleSelected(it) }
                    )
                }
            }

            // TAB 1: Typography & Options
            if (selectedToolTab == 1) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Curated Font Styles",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(QuoteFont.values()) { font ->
                            val isSelected = font == state.quoteFont
                            val shape = RoundedCornerShape(16.dp)
                            Box(
                                modifier = Modifier
                                    .clip(shape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.tertiary
                                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = shape
                                    )
                                    .clickable { viewModel.onFontSelected(font) }
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "Aa",
                                        fontFamily = font.fontFamily,
                                        fontStyle = font.fontStyle,
                                        fontWeight = font.fontWeight,
                                        fontSize = 18.sp,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = font.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Include Aura watermark",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Switch(
                            checked = state.showWatermark,
                            onCheckedChange = { viewModel.toggleWatermark(it) }
                        )
                    }
                }
            }

            // TAB 2: Edit Text
            if (selectedToolTab == 2) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    OutlinedTextField(
                        value = state.quoteText,
                        onValueChange = { viewModel.onQuoteTextChanged(it) },
                        label = { Text("Quote Words") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("designer_quote_input"),
                        shape = RoundedCornerShape(16.dp),
                        minLines = 3,
                        maxLines = 6
                    )

                    OutlinedTextField(
                        value = state.author,
                        onValueChange = { viewModel.onAuthorChanged(it) },
                        label = { Text("Author / Speaker") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("designer_author_input"),
                        shape = RoundedCornerShape(16.dp)
                    )

                    OutlinedTextField(
                        value = state.category,
                        onValueChange = { viewModel.onCategoryChanged(it) },
                        label = { Text("Category / Topic") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary Bottom Actions (Export & Share)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.exportImage(context) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Outlined.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to Photos")
                }

                Button(
                    onClick = {
                        viewModel.shareImage(context) { success ->
                            if (!success) {
                                Toast.makeText(context, "Could not share image", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = Color.Black
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Card")
                }
            }
        }
    }
}
