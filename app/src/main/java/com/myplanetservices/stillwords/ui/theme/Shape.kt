package com.myplanetservices.stillwords.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// ============================================================================
// STILLWORDS SHAPES & CORNER RADII — docs/DESIGN_SPEC.md Sec. 4
// ============================================================================
// - Corner radius: hero/photo cards 28, cards and tiles 18, list groups 18,
//   small cards 16, buttons 16, inputs 14, segmented control 14 (inner 10),
//   chips fully round, bottom sheet top corners 28.

val HeroCardCornerRadius = 28.dp
val CardCornerRadius = 18.dp
val TileCornerRadius = 18.dp
val GroupCornerRadius = 18.dp
val SmallCardCornerRadius = 16.dp
val ButtonCornerRadius = 16.dp
val InputCornerRadius = 14.dp
val SegmentedControlCornerRadius = 14.dp
val SegmentedItemCornerRadius = 10.dp
val BottomSheetCornerRadius = 28.dp

val HeroCardShape = RoundedCornerShape(HeroCardCornerRadius)
val CardShape = RoundedCornerShape(CardCornerRadius)
val TileCardShape = RoundedCornerShape(TileCornerRadius)
val GroupShape = RoundedCornerShape(GroupCornerRadius)
val SmallCardShape = RoundedCornerShape(SmallCardCornerRadius)
val ButtonShape = RoundedCornerShape(ButtonCornerRadius)
val InputShape = RoundedCornerShape(InputCornerRadius)
val SegmentedControlShape = RoundedCornerShape(SegmentedControlCornerRadius)
val SegmentedItemShape = RoundedCornerShape(SegmentedItemCornerRadius)
val ChipShape = CircleShape
val BottomSheetShape = RoundedCornerShape(topStart = BottomSheetCornerRadius, topEnd = BottomSheetCornerRadius)

val StillwordsShapes = Shapes(
    extraSmall = SegmentedItemShape,
    small = InputShape,
    medium = ButtonShape,
    large = CardShape,
    extraLarge = HeroCardShape
)
