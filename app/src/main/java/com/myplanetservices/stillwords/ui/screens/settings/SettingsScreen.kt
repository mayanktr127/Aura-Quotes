package com.myplanetservices.stillwords.ui.screens.settings

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.myplanetservices.stillwords.BuildConfig
import com.myplanetservices.stillwords.data.model.PhotoCreditsLoader
import com.myplanetservices.stillwords.ui.components.PrimaryButton
import com.myplanetservices.stillwords.ui.components.SecondaryButton
import com.myplanetservices.stillwords.ui.components.SegmentedControl
import com.myplanetservices.stillwords.ui.components.SettingsGroup
import com.myplanetservices.stillwords.ui.components.SettingsRow
import com.myplanetservices.stillwords.ui.components.StillwordsSwitch
import com.myplanetservices.stillwords.ui.components.StillwordsTextButton
import com.myplanetservices.stillwords.ui.theme.*
import java.util.Locale

private val AllSettingsCategories = listOf(
    "Motivation", "Success", "Discipline", "Fitness", "Work",
    "Courage", "Confidence", "Leadership", "Growth", "Peace",
    "Mindfulness", "Healing", "Life", "Gratitude", "Self-Love",
    "Love", "Relationships", "Friendship", "Happiness", "Wisdom",
    "Creativity", "Study"
)

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: (() -> Unit)? = null,
    onOpenOnboarding: () -> Unit,
    onNavigateToPhotoCredits: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val prefs by viewModel.preferences.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val creditsData = remember(context) { PhotoCreditsLoader.load(context) }
    val creditsCount = creditsData.credits.size

    var showTimeDialog by remember { mutableStateOf(false) }
    var showTopicsSheet by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    // Android 13+ Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.setDailyReminder(true, prefs.reminderHour, prefs.reminderMinute)
            Toast.makeText(context, "Daily reminder enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    val selectedThemeIndex = when (prefs.themeMode) {
        "LIGHT" -> 2
        "DARK" -> 1
        else -> 0
    }

    val formattedReminderTime = remember(prefs.reminderHour, prefs.reminderMinute) {
        val h = prefs.reminderHour
        val m = prefs.reminderMinute
        val amPm = if (h < 12) "AM" else "PM"
        val displayH = when {
            h == 0 -> 12
            h > 12 -> h - 12
            else -> h
        }
        String.format(Locale.US, "%d:%02d %s", displayH, m, amPm)
    }

    val topicsSummary = remember(prefs.selectedCategories) {
        val list = prefs.selectedCategories.toList()
        when {
            list.isEmpty() -> "None"
            list.size == 1 -> list[0]
            list.size == 2 -> "${list[0]}, ${list[1]}"
            else -> "${list[0]}, ${list[1]} +${list.size - 2}"
        }
    }

    // Quote text size float: 0 = Small, 1 = Medium, 2 = Large
    val currentSizeFloat = when (prefs.quoteTextSize) {
        "SMALL" -> 0f
        "LARGE" -> 2f
        else -> 1f
    }
    var sliderValue by remember(currentSizeFloat) { mutableFloatStateOf(currentSizeFloat) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("settings_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Pushed Screen Header (docs/DESIGN_SPEC.md Sec. 6 Screen 15 & Settings.dc.html)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onNavigateBack != null) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("settings_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                            contentDescription = "Back to Today",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Text(
                    text = "Settings",
                    style = ScreenTitlePushedStyle,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            // Main Content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // ============================================================
                // GROUP 1: DAILY QUOTE
                // ============================================================
                item(key = "group_daily_quote") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "DAILY QUOTE",
                            style = LabelOverlineStyle,
                            color = MaterialTheme.colorScheme.primary
                        )

                        SettingsGroup {
                            // Row 1: Morning reminder
                            SettingsRow(
                                title = "Morning reminder",
                                subtitle = "A gentle note with today's quote",
                                showDivider = true,
                                trailingContent = {
                                    StillwordsSwitch(
                                        checked = prefs.dailyReminderEnabled,
                                        onCheckedChange = { enable ->
                                            if (enable) {
                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                    val granted = ContextCompat.checkSelfPermission(
                                                        context,
                                                        Manifest.permission.POST_NOTIFICATIONS
                                                    ) == PackageManager.PERMISSION_GRANTED
                                                    if (granted) {
                                                        viewModel.setDailyReminder(true, prefs.reminderHour, prefs.reminderMinute)
                                                    } else {
                                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                                    }
                                                } else {
                                                    viewModel.setDailyReminder(true, prefs.reminderHour, prefs.reminderMinute)
                                                }
                                            } else {
                                                viewModel.setDailyReminder(false, prefs.reminderHour, prefs.reminderMinute)
                                            }
                                        }
                                    )
                                }
                            )

                            // Row 2: Reminder time
                            SettingsRow(
                                title = "Reminder time",
                                valueText = if (prefs.dailyReminderEnabled) formattedReminderTime else "Off",
                                showChevron = true,
                                showDivider = true,
                                onClick = {
                                    showTimeDialog = true
                                }
                            )

                            // Row 3: Topics I like
                            SettingsRow(
                                title = "Topics I like",
                                valueText = topicsSummary,
                                showChevron = true,
                                showDivider = false,
                                onClick = {
                                    showTopicsSheet = true
                                }
                            )
                        }
                    }
                }

                // ============================================================
                // GROUP 2: LOOK
                // ============================================================
                item(key = "group_look") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "LOOK",
                            style = LabelOverlineStyle,
                            color = MaterialTheme.colorScheme.primary
                        )

                        SettingsGroup {
                            // Row 1: Theme Segmented Control
                            SettingsRow(
                                title = "Theme",
                                showDivider = true,
                                trailingContent = {
                                    SegmentedControl(
                                        items = listOf("System", "Dark", "Light"),
                                        selectedIndex = selectedThemeIndex,
                                        onItemSelected = { index ->
                                            when (index) {
                                                0 -> viewModel.setThemeMode("SYSTEM")
                                                1 -> viewModel.setThemeMode("DARK")
                                                2 -> viewModel.setThemeMode("LIGHT")
                                            }
                                        },
                                        modifier = Modifier.width(200.dp)
                                    )
                                }
                            )

                            // Row 2: Quote text size Slider
                            val sizeLabel = when (prefs.quoteTextSize) {
                                "SMALL" -> "Small"
                                "LARGE" -> "Large"
                                else -> "Medium"
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Quote text size",
                                        style = BodyStyle.copy(color = MaterialTheme.colorScheme.onBackground)
                                    )
                                    Text(
                                        text = sizeLabel,
                                        style = BodySmallStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "Aa",
                                        fontFamily = NewsreaderFontFamily,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Slider(
                                        value = sliderValue,
                                        onValueChange = { sliderValue = it },
                                        onValueChangeFinished = {
                                            val nearest = when {
                                                sliderValue < 0.5f -> "SMALL"
                                                sliderValue > 1.5f -> "LARGE"
                                                else -> "MEDIUM"
                                            }
                                            viewModel.setQuoteTextSize(nearest)
                                        },
                                        valueRange = 0f..2f,
                                        steps = 1,
                                        colors = SliderDefaults.colors(
                                            thumbColor = MaterialTheme.colorScheme.onBackground,
                                            activeTrackColor = MaterialTheme.colorScheme.primary,
                                            inactiveTrackColor = MaterialTheme.colorScheme.outline
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )

                                    Text(
                                        text = "Aa",
                                        fontFamily = NewsreaderFontFamily,
                                        fontSize = 22.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                // ============================================================
                // GROUP 3: ABOUT
                // ============================================================
                item(key = "group_about") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "ABOUT",
                            style = LabelOverlineStyle,
                            color = MaterialTheme.colorScheme.primary
                        )

                        SettingsGroup {
                            // Row 1: Rate Stillwords
                            SettingsRow(
                                title = "Rate Stillwords",
                                showChevron = true,
                                showDivider = true,
                                onClick = {
                                    try {
                                        val intent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("market://details?id=${context.packageName}")
                                        ).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        val webIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                                        ).apply {
                                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                        }
                                        context.startActivity(webIntent)
                                    }
                                }
                            )

                            // Row 2: Share with a friend
                            SettingsRow(
                                title = "Share with a friend",
                                showChevron = true,
                                showDivider = true,
                                onClick = {
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Stillwords — Words for the quiet moments.\nhttps://play.google.com/store/apps/details?id=${context.packageName}"
                                        )
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, "Share Stillwords")
                                    context.startActivity(shareIntent)
                                }
                            )

                            // Row 3: Photo credits
                            SettingsRow(
                                title = "Photo credits",
                                valueText = "$creditsCount Pexels photos",
                                showChevron = true,
                                showDivider = true,
                                onClick = onNavigateToPhotoCredits
                            )

                            // Row 4: Privacy policy
                            SettingsRow(
                                title = "Privacy policy",
                                showChevron = true,
                                showDivider = false,
                                onClick = {
                                    showPrivacyDialog = true
                                }
                            )
                        }
                    }
                }

                // ============================================================
                // GROUP 4: DATA & HISTORY
                // ============================================================
                item(key = "group_data") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "DATA",
                            style = LabelOverlineStyle,
                            color = MaterialTheme.colorScheme.primary
                        )

                        SettingsGroup {
                            // Row 1: Replay intro tour
                            SettingsRow(
                                title = "Replay intro tour",
                                showChevron = true,
                                showDivider = true,
                                onClick = onOpenOnboarding
                            )

                            // Row 2: Clear reading history
                            SettingsRow(
                                title = "Clear reading history",
                                showChevron = true,
                                showDivider = false,
                                onClick = {
                                    showClearHistoryDialog = true
                                }
                            )
                        }
                    }
                }

                // ============================================================
                // FOOTER: VERSION
                // ============================================================
                item(key = "settings_footer") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Stillwords · version ${BuildConfig.VERSION_NAME}",
                            fontFamily = NewsreaderFontFamily,
                            fontStyle = FontStyle.Italic,
                            fontSize = 13.sp,
                            color = StillwordsTheme.colors.muted,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Time Picker Dialog
    if (showTimeDialog) {
        ReminderTimePickerDialog(
            initialHour = prefs.reminderHour,
            initialMinute = prefs.reminderMinute,
            onDismiss = { showTimeDialog = false },
            onConfirm = { hour, minute ->
                viewModel.setDailyReminder(true, hour, minute)
                showTimeDialog = false
                Toast.makeText(context, "Reminder time updated", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Topics I Like Bottom Sheet
    if (showTopicsSheet) {
        TopicsSelectionBottomSheet(
            selectedCategories = prefs.selectedCategories,
            onDismiss = { showTopicsSheet = false },
            onSave = { updatedCategories ->
                viewModel.setSelectedCategories(updatedCategories)
                showTopicsSheet = false
            }
        )
    }

    // Clear History Dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = {
                Text(
                    text = "Clear Reading History?",
                    fontFamily = NewsreaderFontFamily,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Text(
                    text = "This will remove all recently read quotes from your history. Saved quotes and collections are kept.",
                    fontFamily = FigtreeFontFamily,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearHistoryDialog = false
                        Toast.makeText(context, "Reading history cleared", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Clear All", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(
                    text = "Privacy Policy",
                    fontFamily = NewsreaderFontFamily,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Stillwords is designed to be quiet, personal, and completely respectful of your privacy.\n\n" +
                                "• 100% Offline by default: your bookmarks, favorite quotes, and reading history never leave your device.\n\n" +
                                "• No accounts: you never need to sign in, register, or provide any personal details.\n\n" +
                                "• Minimal permissions: notifications are used only for your scheduled daily reflection.\n\n" +
                                "Stillwords does not track you, sell data, or transmit your reading habits to any third party.",
                        fontFamily = FigtreeFontFamily,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

// ============================================================================
// REMINDER TIME PICKER DIALOG
// ============================================================================
@Composable
private fun ReminderTimePickerDialog(
    initialHour: Int,
    initialMinute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var hour by remember { mutableIntStateOf(initialHour) }
    var minute by remember { mutableIntStateOf(initialMinute) }

    val presets = listOf(
        Triple("Early morning", 6, 30),
        Triple("Morning", 7, 30),
        Triple("Lunch break", 13, 0),
        Triple("Evening", 20, 0),
        Triple("Before sleep", 22, 0)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Reminder Time",
                fontFamily = NewsreaderFontFamily,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "Choose when today's quote should find you:",
                    fontFamily = FigtreeFontFamily,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Quick Presets
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { (name, h, m) ->
                        val isSelected = hour == h && minute == m
                        val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        val bgColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                                .clickable {
                                    hour = h
                                    minute = m
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = name,
                                fontFamily = FigtreeFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground
                            )
                            val amPm = if (h < 12) "AM" else "PM"
                            val displayH = when {
                                h == 0 -> 12
                                h > 12 -> h - 12
                                else -> h
                            }
                            Text(
                                text = String.format(Locale.US, "%d:%02d %s", displayH, m, amPm),
                                fontFamily = FigtreeFontFamily,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(hour, minute) }) {
                Text("Save", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    )
}

// ============================================================================
// TOPICS SELECTION BOTTOM SHEET
// ============================================================================
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TopicsSelectionBottomSheet(
    selectedCategories: Set<String>,
    onDismiss: () -> Unit,
    onSave: (Set<String>) -> Unit
) {
    val currentSelected = remember { mutableStateListOf(*selectedCategories.toTypedArray()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.outline)
            )
        },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp, top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Topics I like",
                    fontFamily = NewsreaderFontFamily,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Select 3 or more topics to personalize your daily quotes feed.",
                    fontFamily = FigtreeFontFamily,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 340.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    AllSettingsCategories.forEach { category ->
                        val isSelected = currentSelected.contains(category)
                        val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        val containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                        val textColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onBackground

                        Box(
                            modifier = Modifier
                                .height(40.dp)
                                .clip(CircleShape)
                                .background(containerColor)
                                .border(1.dp, borderColor, CircleShape)
                                .clickable {
                                    if (isSelected) {
                                        if (currentSelected.size > 1) {
                                            currentSelected.remove(category)
                                        }
                                    } else {
                                        currentSelected.add(category)
                                    }
                                }
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = category,
                                fontFamily = FigtreeFontFamily,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                                color = textColor
                            )
                        }
                    }
                }
            }

            PrimaryButton(
                text = "Save changes (${currentSelected.size} selected)",
                onClick = { onSave(currentSelected.toSet()) },
                enabled = currentSelected.size >= 1,
                height = 54.dp
            )
        }
    }
}
