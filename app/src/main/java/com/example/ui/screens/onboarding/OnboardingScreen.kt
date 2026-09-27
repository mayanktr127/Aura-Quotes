package com.example.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    preferencesRepository: UserPreferencesRepository,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    var currentStep by remember { mutableStateOf(0) }

    val categories = listOf(
        "Motivation", "Confidence", "Gratitude", "Mindfulness",
        "Relationships", "Resilience", "Creativity", "Growth",
        "Wisdom", "Peace", "Discipline", "Self-Love"
    )
    val selectedInterests = remember { mutableStateListOf("Motivation", "Confidence", "Gratitude", "Mindfulness") }

    var selectedHour by remember { mutableStateOf(8) }
    var selectedMinute by remember { mutableStateOf(30) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E1A17),
                        Color(0xFF12100E)
                    )
                )
            )
            .padding(24.dp)
            .testTag("onboarding_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Step Indicator
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                (0..2).forEach { index ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .height(4.dp)
                            .width(if (index == currentStep) 32.dp else 12.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == currentStep) Color(0xFFE5A93C)
                                else Color(0x40FFFFFF)
                            )
                    )
                }
            }

            // Step Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (currentStep) {
                    0 -> {
                        // Step 1: Intro
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(Color(0x20E5A93C), CircleShape)
                                    .border(1.dp, Color(0x60E5A93C), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color(0xFFE5A93C),
                                    modifier = Modifier.size(36.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(32.dp))

                            Text(
                                text = "Words that stay with you.",
                                style = MaterialTheme.typography.displayMedium,
                                color = Color(0xFFFBF7EE),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Discover thoughts and timeless wisdom for every mood and moment of your day.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFFA8A29E),
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }

                    1 -> {
                        // Step 2: What inspires you?
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "What inspires you?",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color(0xFFFBF7EE),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Select topics to personalize your daily inspiration feed.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFA8A29E),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(28.dp))

                            // Topic Pills
                            val chunked = categories.chunked(2)
                            chunked.forEach { rowList ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowList.forEach { category ->
                                        val isSelected = selectedInterests.contains(category)
                                        val shape = RoundedCornerShape(16.dp)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(shape)
                                                .background(
                                                    if (isSelected) Color(0xFFE5A93C)
                                                    else Color(0xFF24201C)
                                                )
                                                .border(
                                                    1.dp,
                                                    if (isSelected) Color(0xFFE5A93C) else Color(0x30FFFFFF),
                                                    shape
                                                )
                                                .clickable {
                                                    if (isSelected) {
                                                        if (selectedInterests.size > 1) selectedInterests.remove(category)
                                                    } else {
                                                        selectedInterests.add(category)
                                                    }
                                                }
                                                .padding(vertical = 12.dp, horizontal = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = Color.Black,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                                Text(
                                                    text = category,
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = if (isSelected) Color.Black else Color(0xFFE7E1D8)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // Step 3: When should we inspire you?
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .background(Color(0x20E5A93C), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = Color(0xFFE5A93C),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            Text(
                                text = "When should we inspire you?",
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color(0xFFFBF7EE),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Choose a quiet moment for your daily reflection.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFA8A29E),
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(32.dp))

                            // Morning, Noon, Evening quick presets
                            listOf(
                                Triple("Morning Calm", "08:30 AM", Pair(8, 30)),
                                Triple("Midday Reset", "12:30 PM", Pair(12, 30)),
                                Triple("Evening Reflection", "08:30 PM", Pair(20, 30))
                            ).forEach { (title, timeLabel, timePair) ->
                                val isSelected = selectedHour == timePair.first && selectedMinute == timePair.second
                                val shape = RoundedCornerShape(18.dp)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clip(shape)
                                        .background(if (isSelected) Color(0xFF2E2721) else Color(0xFF1E1A17))
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) Color(0xFFE5A93C) else Color(0x20FFFFFF),
                                        shape = shape
                                    )
                                    .clickable {
                                        selectedHour = timePair.first
                                        selectedMinute = timePair.second
                                    }
                                    .padding(horizontal = 20.dp, vertical = 14.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = title,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                                color = Color(0xFFFBF7EE)
                                            )
                                            Text(
                                                text = timeLabel,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color(0xFFE5A93C)
                                            )
                                        }

                                        if (isSelected) {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = Color(0xFFE5A93C),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    TextButton(onClick = { currentStep-- }) {
                        Text("Back", color = Color(0xFFA8A29E))
                    }
                } else {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            preferencesRepository.setOnboardingCompleted(true)
                            onComplete()
                        }
                    }) {
                        Text("Skip", color = Color(0xFFA8A29E))
                    }
                }

                Button(
                    onClick = {
                        if (currentStep < 2) {
                            currentStep++
                        } else {
                            coroutineScope.launch {
                                preferencesRepository.setSelectedCategories(selectedInterests.toSet())
                                preferencesRepository.setDailyReminder(true, selectedHour, selectedMinute)
                                preferencesRepository.setOnboardingCompleted(true)
                                onComplete()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE5A93C),
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .height(52.dp)
                        .testTag("onboarding_next_button")
                ) {
                    Text(
                        text = if (currentStep == 2) "Get Started" else "Continue",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
