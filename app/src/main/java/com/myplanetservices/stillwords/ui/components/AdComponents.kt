package com.myplanetservices.stillwords.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.myplanetservices.stillwords.ui.theme.*

// ============================================================================
// 17. AD NATIVE CARD (Phase 5 Wiring, Phase 4 Layout & Preview)
// docs/DESIGN_SPEC.md Sec. 6 (Screen 17) & AdNative.dc.html
// ============================================================================
@Composable
fun AdNativeCard(
    modifier: Modifier = Modifier,
    advertiserName: String = "[Advertiser name]",
    ratingPrice: String = "[Store rating · Price]",
    headline: String = "[Headline, up to 25 characters]",
    bodyText: String = "[Body text, up to 90 characters, shown on two lines at most.]",
    callToAction: String = "[Call to action]",
    onClose: () -> Unit = {},
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Bar: Close (X) + SPONSORED overline
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Text(
                    text = "SPONSORED",
                    style = LabelOverlineStyle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.width(44.dp))
            }

            // Article Card Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(HeroCardShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(1.dp, MaterialTheme.colorScheme.outline, HeroCardShape)
                    .clickable(onClick = onClick)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Header Row: App Icon + Advertiser info + "Ad" badge + AdChoices
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // 40dp App Icon
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.outline)
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ad",
                                fontFamily = FigtreeFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Advertiser Name & Rating/Price
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Text(
                                text = advertiserName,
                                fontFamily = FigtreeFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onBackground,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = ratingPrice,
                                fontFamily = FigtreeFontFamily,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // "Ad" Badge
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ) {
                            Text(
                                text = "Ad",
                                fontFamily = FigtreeFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // AdChoices Icon
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = "AdChoices",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }

                    // Media View Placeholder (16:9 to 1:1, ~300dp)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(280.dp)
                            .background(
                                if (MaterialTheme.colorScheme.background == Ink) Color(0xFF1F1C19)
                                else Color(0xFFE8E2D8)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                RoundedCornerShape(0.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "[Media view · image or video, 16:9 to 1:1]",
                            fontFamily = FigtreeFontFamily,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(16.dp)
                        )
                    }

                    // Headline & Body text
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = headline,
                            fontFamily = FigtreeFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 17.sp,
                            lineHeight = 22.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = bodyText,
                            fontFamily = FigtreeFontFamily,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Outlined Accent Call To Action Button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(bottom = 16.dp)
                    ) {
                        Button(
                            onClick = onClick,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = callToAction,
                                fontFamily = FigtreeFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            // Bottom hint: Swipe up to keep reading
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Swipe up to keep reading",
                    fontFamily = FigtreeFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                )
            }
        }
    }
}

// ============================================================================
// 18. AD BANNER SLOT (Phase 5 Wiring, Phase 4 Layout & Preview)
// docs/DESIGN_SPEC.md Sec. 6 (Screen 18) & AdBanner.dc.html
// ============================================================================
@Composable
fun AdBannerSlot(
    modifier: Modifier = Modifier,
    placeholderText: String = "[Anchored adaptive banner · full width × ~60]"
) {
    val isDark = MaterialTheme.colorScheme.background == Ink
    val bannerBackground = if (isDark) Color(0xFF151311) else Linen

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(62.dp)
            .background(bannerBackground)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "Ad" Badge
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Text(
                    text = "Ad",
                    fontFamily = FigtreeFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 10.sp,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Inner Banner Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = placeholderText,
                    fontFamily = FigtreeFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

// ============================================================================
// COMPOSE PREVIEWS (Dark & Light)
// ============================================================================

@Preview(name = "AdNativeCard Dark", showBackground = true)
@Composable
private fun PreviewAdNativeCardDark() {
    StillwordsTheme(darkTheme = true) {
        AdNativeCard()
    }
}

@Preview(name = "AdNativeCard Light", showBackground = true)
@Composable
private fun PreviewAdNativeCardLight() {
    StillwordsTheme(darkTheme = false) {
        AdNativeCard()
    }
}

@Preview(name = "AdBannerSlot Dark", showBackground = true)
@Composable
private fun PreviewAdBannerSlotDark() {
    StillwordsTheme(darkTheme = true) {
        AdBannerSlot()
    }
}

@Preview(name = "AdBannerSlot Light", showBackground = true)
@Composable
private fun PreviewAdBannerSlotLight() {
    StillwordsTheme(darkTheme = false) {
        AdBannerSlot()
    }
}
