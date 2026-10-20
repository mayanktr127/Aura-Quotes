package com.myplanetservices.stillwords.ui.screens.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.myplanetservices.stillwords.data.preferences.UserPreferencesRepository
import com.myplanetservices.stillwords.notification.DailyReminderManager
import com.myplanetservices.stillwords.ui.components.PrimaryButton
import com.myplanetservices.stillwords.ui.components.StillwordsTextButton
import com.myplanetservices.stillwords.ui.theme.*
import kotlinx.coroutines.launch

private val RealCategories = listOf(
    "Motivation", "Success", "Discipline", "Fitness", "Work",
    "Courage", "Confidence", "Leadership", "Growth", "Peace",
    "Mindfulness", "Healing", "Life", "Gratitude", "Self-Love",
    "Love", "Relationships", "Friendship", "Happiness", "Wisdom",
    "Creativity", "Study"
)

private data class ReminderSlot(
    val name: String,
    val timeLabel: String,
    val hour: Int,
    val minute: Int
)

private val ReminderSlots = listOf(
    ReminderSlot("Early morning", "6:30 AM", 6, 30),
    ReminderSlot("Morning", "7:30 AM", 7, 30),
    ReminderSlot("Lunch break", "1:00 PM", 13, 0),
    ReminderSlot("Before sleep", "10:00 PM", 22, 0)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingScreen(
    preferencesRepository: UserPreferencesRepository,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
    initialStep: Int = 0
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableIntStateOf(initialStep) }

    // Selected categories for Step 2 (default 4 selected matching Onboarding2.dc.html)
    val selectedTopics = remember {
        mutableStateListOf("Peace", "Courage", "Gratitude", "Study")
    }

    // Selected reminder slot for Step 3 (default 1 = Morning 7:30 AM)
    var selectedSlotIndex by remember { mutableIntStateOf(1) }
    val selectedSlot = ReminderSlots[selectedSlotIndex]

    val reminderManager = remember(context) { DailyReminderManager(context) }

    fun finishOnboarding(enableReminder: Boolean) {
        coroutineScope.launch {
            preferencesRepository.setSelectedCategories(selectedTopics.toSet())
            preferencesRepository.setDailyReminder(enableReminder, selectedSlot.hour, selectedSlot.minute)
            if (enableReminder) {
                reminderManager.scheduleDailyReminder(selectedSlot.hour, selectedSlot.minute)
            } else {
                reminderManager.cancelDailyReminder()
            }
            preferencesRepository.setOnboardingCompleted(true)
            onComplete()
        }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        finishOnboarding(enableReminder = isGranted)
    }

    BackHandler(enabled = currentStep > 0) {
        currentStep--
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Ink)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("onboarding_screen")
    ) {
        when (currentStep) {
            0 -> {
                // ============================================================
                // STEP 1: WELCOME (docs/DESIGN_SPEC.md Screen 1 & Welcome.dc.html)
                // ============================================================
                WelcomeStep(
                    onBegin = { currentStep = 1 },
                    onSkip = {
                        coroutineScope.launch {
                            preferencesRepository.setOnboardingCompleted(true)
                            onComplete()
                        }
                    }
                )
            }

            1 -> {
                // ============================================================
                // STEP 2: TOPICS (docs/DESIGN_SPEC.md Screen 2 & Onboarding2.dc.html)
                // ============================================================
                TopicsStep(
                    selectedTopics = selectedTopics,
                    onToggleTopic = { topic ->
                        if (selectedTopics.contains(topic)) {
                            selectedTopics.remove(topic)
                        } else {
                            selectedTopics.add(topic)
                        }
                    },
                    onBack = { currentStep = 0 },
                    onSkip = {
                        coroutineScope.launch {
                            preferencesRepository.setSelectedCategories(selectedTopics.toSet())
                            preferencesRepository.setOnboardingCompleted(true)
                            onComplete()
                        }
                    },
                    onContinue = {
                        coroutineScope.launch {
                            preferencesRepository.setSelectedCategories(selectedTopics.toSet())
                            currentStep = 2
                        }
                    }
                )
            }

            2 -> {
                // ============================================================
                // STEP 3: REMINDER (docs/DESIGN_SPEC.md Screen 3 & Onboarding3.dc.html)
                // ============================================================
                ReminderStep(
                    selectedSlotIndex = selectedSlotIndex,
                    onSelectSlot = { selectedSlotIndex = it },
                    onBack = { currentStep = 1 },
                    onTurnOn = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val permissionCheck = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                                finishOnboarding(enableReminder = true)
                            } else {
                                notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        } else {
                            finishOnboarding(enableReminder = true)
                        }
                    },
                    onNotNow = {
                        finishOnboarding(enableReminder = false)
                    }
                )
            }
        }
    }
}

// ============================================================================
// STEP 1: WELCOME
// ============================================================================
@Composable
private fun WelcomeStep(
    onBegin: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    0.0f to Color(0xFF0B0F1C),
                    0.48f to Color(0xFF1C2540),
                    0.66f to Color(0xFF5A5266),
                    0.74f to Color(0xFF8B6B58),
                    1.00f to Color(0xFF0F0E0C)
                )
            )
    ) {
        // Bottom fade overlay to #0F0E0C
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.55f)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Transparent,
                        0.45f to Color(0xFF0F0E0C),
                        1.0f to Color(0xFF0F0E0C)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .padding(bottom = 36.dp, top = 20.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.Start
        ) {
            // Logo Mark (Brass quotation marks + "Stillwords")
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Two Brass Quotation Marks
                androidx.compose.foundation.Canvas(modifier = Modifier.size(width = 28.dp, height = 22.dp)) {
                    drawQuoteMarks(color = Brass)
                }
                Text(
                    text = "Stillwords",
                    fontFamily = NewsreaderFontFamily,
                    fontSize = 20.sp,
                    letterSpacing = 0.5.sp,
                    color = Ivory
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            // Title
            Text(
                text = "Words for the quiet moments.",
                fontFamily = NewsreaderFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 40.sp,
                lineHeight = 44.sp,
                letterSpacing = (-0.5).sp,
                color = Ivory
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Body
            Text(
                text = "One thoughtful quote a day, with room to pause, save what stays with you, and share it beautifully.",
                fontFamily = FigtreeFontFamily,
                fontSize = 15.sp,
                lineHeight = 23.sp,
                color = Stone,
                modifier = Modifier.fillMaxWidth(0.92f)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // CTA Buttons
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PrimaryButton(
                    text = "Begin",
                    onClick = onBegin,
                    height = 56.dp,
                    modifier = Modifier.testTag("onboarding_next_button")
                )

                StillwordsTextButton(
                    text = "Skip for now",
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Step Indicator (Step 1 active)
            OnboardingStepIndicator(
                currentStep = 0,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

// ============================================================================
// STEP 2: TOPICS
// ============================================================================
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TopicsStep(
    selectedTopics: List<String>,
    onToggleTopic: (String) -> Unit,
    onBack: () -> Unit,
    onSkip: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val count = selectedTopics.size
    val isReady = count >= 3

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ink)
            .padding(vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("onboarding_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = Ivory,
                    modifier = Modifier.size(24.dp)
                )
            }

            OnboardingStepIndicator(currentStep = 1)

            TextButton(
                onClick = onSkip,
                modifier = Modifier.height(48.dp)
            ) {
                Text(
                    text = "Skip",
                    style = ChipStyle,
                    color = Stone
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Titles
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "STEP 2 OF 3",
                style = LabelOverlineStyle,
                color = Brass
            )
            Text(
                text = "What would you like to hear more of?",
                fontFamily = NewsreaderFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                lineHeight = 37.sp,
                letterSpacing = (-0.3).sp,
                color = Ivory
            )
            Text(
                text = "Pick three or more. Your daily quote will lean towards these.",
                fontFamily = FigtreeFontFamily,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = Stone
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 22 Real Categories Chips
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                RealCategories.forEach { category ->
                    val isSelected = selectedTopics.contains(category)
                    TopicSelectionChip(
                        label = category,
                        isSelected = isSelected,
                        onClick = { onToggleTopic(category) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Footer & Continue Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val countText = if (isReady) "$count topics chosen" else "Choose ${3 - count} more"
            Text(
                text = countText,
                fontFamily = FigtreeFontFamily,
                fontSize = 13.sp,
                color = Stone,
                textAlign = TextAlign.Center
            )

            PrimaryButton(
                text = "Continue",
                enabled = isReady,
                onClick = onContinue,
                height = 56.dp,
                modifier = Modifier.testTag("onboarding_continue_button")
            )
        }
    }
}

// ============================================================================
// STEP 3: REMINDER
// ============================================================================
@Composable
private fun ReminderStep(
    selectedSlotIndex: Int,
    onSelectSlot: (Int) -> Unit,
    onBack: () -> Unit,
    onTurnOn: () -> Unit,
    onNotNow: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedSlot = ReminderSlots[selectedSlotIndex]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Ink)
            .padding(vertical = 12.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("onboarding_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.KeyboardArrowLeft,
                    contentDescription = "Back",
                    tint = Ivory,
                    modifier = Modifier.size(24.dp)
                )
            }

            OnboardingStepIndicator(currentStep = 2)

            Spacer(modifier = Modifier.width(48.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Titles
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "STEP 3 OF 3",
                style = LabelOverlineStyle,
                color = Brass
            )
            Text(
                text = "When should today's quote find you?",
                fontFamily = NewsreaderFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 32.sp,
                lineHeight = 37.sp,
                letterSpacing = (-0.3).sp,
                color = Ivory
            )
            Text(
                text = "One quiet notification a day. Change or turn it off anytime in Settings.",
                fontFamily = FigtreeFontFamily,
                fontSize = 14.sp,
                lineHeight = 21.sp,
                color = Stone
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Dawn Preview Card (matches Onboarding3.dc.html)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .height(150.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.verticalGradient(
                        0.0f to Color(0xFF0F0E0C),
                        0.40f to Color(0xFF1C1A22),
                        1.00f to Color(0xFF4A3A3A)
                    )
                )
                .border(1.dp, HairlineDark, RoundedCornerShape(24.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = selectedSlot.timeLabel,
                    fontFamily = NewsreaderFontFamily,
                    fontSize = 52.sp,
                    letterSpacing = (-1).sp,
                    lineHeight = 54.sp,
                    color = Ivory
                )
                Text(
                    text = selectedSlot.name,
                    fontFamily = FigtreeFontFamily,
                    fontSize = 13.sp,
                    color = Ivory.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 2 x 2 Grid of slots
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val chunked = ReminderSlots.chunked(2)
            chunked.forEachIndexed { rowIndex, rowSlots ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowSlots.forEachIndexed { colIndex, slot ->
                        val globalIndex = rowIndex * 2 + colIndex
                        val isSelected = globalIndex == selectedSlotIndex
                        ReminderSlotCard(
                            slot = slot,
                            isSelected = isSelected,
                            onClick = { onSelectSlot(globalIndex) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Actions
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            PrimaryButton(
                text = "Turn on reminder",
                onClick = onTurnOn,
                height = 56.dp,
                modifier = Modifier.testTag("onboarding_turn_on_reminder")
            )

            StillwordsTextButton(
                text = "Not now",
                onClick = onNotNow,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ============================================================================
// HELPER COMPOSABLES
// ============================================================================

@Composable
private fun OnboardingStepIndicator(
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        (0..2).forEach { index ->
            val isActive = index == currentStep
            val width by animateDpAsState(
                targetValue = if (isActive) 22.dp else 8.dp,
                animationSpec = tween(200),
                label = "step_indicator_width"
            )
            val color by animateColorAsState(
                targetValue = if (isActive) Brass else HairlineDark,
                animationSpec = tween(200),
                label = "step_indicator_color"
            )
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
            )
        }
    }
}

@Composable
private fun TopicSelectionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Brass else HairlineDark
    val containerColor = if (isSelected) BrassAlpha12 else Color.Transparent
    val textColor = if (isSelected) AccentTintDark else Ivory

    Box(
        modifier = modifier
            .height(42.dp)
            .clip(CircleShape)
            .background(containerColor)
            .border(1.dp, borderColor, CircleShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = FigtreeFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = textColor
        )
    }
}

@Composable
private fun ReminderSlotCard(
    slot: ReminderSlot,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) Brass else HairlineDark
    val containerColor = if (isSelected) BrassAlpha12 else Charcoal
    val textColor = if (isSelected) AccentTintDark else Ivory

    Box(
        modifier = modifier
            .height(64.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = slot.name,
                fontFamily = FigtreeFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = textColor
            )
            Text(
                text = slot.timeLabel,
                fontFamily = FigtreeFontFamily,
                fontSize = 12.sp,
                color = textColor.copy(alpha = 0.75f)
            )
        }
    }
}

private fun DrawScope.drawQuoteMarks(color: Color) {
    // Vector path for twin opening quote marks: "“"
    val scale = size.height / 26f
    val path1 = Path().apply {
        moveTo(2f * scale, 24f * scale)
        lineTo(2f * scale, 14f * scale)
        cubicTo(2f * scale, 7f * scale, 6f * scale, 3f * scale, 13f * scale, 2f * scale)
        lineTo(13f * scale, 7f * scale)
        cubicTo(9f * scale, 8f * scale, 7f * scale, 10f * scale, 7f * scale, 14f * scale)
        lineTo(13f * scale, 14f * scale)
        lineTo(13f * scale, 24f * scale)
        close()
    }
    val path2 = Path().apply {
        moveTo(20f * scale, 24f * scale)
        lineTo(20f * scale, 14f * scale)
        cubicTo(20f * scale, 7f * scale, 24f * scale, 3f * scale, 31f * scale, 2f * scale)
        lineTo(31f * scale, 7f * scale)
        cubicTo(27f * scale, 8f * scale, 25f * scale, 10f * scale, 25f * scale, 14f * scale)
        lineTo(31f * scale, 14f * scale)
        lineTo(31f * scale, 24f * scale)
        close()
    }
    drawPath(path1, color)
    drawPath(path2, color)
}
