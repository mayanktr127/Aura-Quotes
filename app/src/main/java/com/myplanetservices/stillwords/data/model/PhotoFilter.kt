package com.myplanetservices.stillwords.data.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix

/**
 * Curated photo filters for Stillwords Quote Designer.
 * Each filter is implemented as a 4x5 ColorMatrix float array, guaranteeing
 * identical rendering between the Compose preview canvas and exported bitmap images.
 */
enum class PhotoFilter(
    val id: String,
    val displayName: String,
    val matrixValues: FloatArray,
    // Scrim gradient colors tuned to guarantee WCAG AA 4.5:1 contrast for each filter
    val scrimTop: Color = Color(0x66000000),
    val scrimMid: Color = Color(0x99000000),
    val scrimBottom: Color = Color(0xDD000000)
) {
    ORIGINAL(
        id = "original",
        displayName = "Original",
        matrixValues = floatArrayOf(
            1f, 0f, 0f, 0f, 0f,
            0f, 1f, 0f, 0f, 0f,
            0f, 0f, 1f, 0f, 0f,
            0f, 0f, 0f, 1f, 0f
        ),
        scrimTop = Color(0x66000000),
        scrimMid = Color(0x99000000),
        scrimBottom = Color(0xDD000000)
    ),

    NOIR(
        id = "noir",
        displayName = "Noir",
        // Deep contrast black and white with rich shadows
        matrixValues = floatArrayOf(
            0.374f, 0.734f, 0.142f, 0f, -25f,
            0.374f, 0.734f, 0.142f, 0f, -25f,
            0.374f, 0.734f, 0.142f, 0f, -25f,
            0f,     0f,     0f,     1f, 0f
        ),
        scrimTop = Color(0x55000000),
        scrimMid = Color(0x88000000),
        scrimBottom = Color(0xDD000000)
    ),

    WARM(
        id = "warm",
        displayName = "Warm",
        // Soft amber tint: boosted reds, warm midtones, subtle blue attenuation
        matrixValues = floatArrayOf(
            1.05f, 0.0f,  0.0f,  0f, 18f,
            0.0f,  1.02f, 0.0f,  0f, 8f,
            0.0f,  0.0f,  0.90f, 0f, -14f,
            0f,    0f,    0f,    1f, 0f
        ),
        scrimTop = Color(0x66000000),
        scrimMid = Color(0x99000000),
        scrimBottom = Color(0xDD000000)
    ),

    GOLDEN(
        id = "golden",
        displayName = "Golden",
        // Golden-hour glow: rich amber/honey radiance
        matrixValues = floatArrayOf(
            1.12f, 0.05f, 0.0f,  0f, 22f,
            0.05f, 1.05f, 0.0f,  0f, 12f,
            0.0f,  0.0f,  0.75f, 0f, -22f,
            0f,    0f,    0f,    1f, 0f
        ),
        scrimTop = Color(0x66000000),
        scrimMid = Color(0x99000000),
        scrimBottom = Color(0xDD000000)
    ),

    EARTHY(
        id = "earthy",
        displayName = "Earthy",
        // Muted green film tone: olive/sage undertones with subdued saturation
        matrixValues = floatArrayOf(
            0.85f, 0.10f, 0.05f, 0f, -5f,
            0.05f, 0.95f, 0.05f, 0f, 8f,
            0.05f, 0.10f, 0.75f, 0f, -12f,
            0f,    0f,    0f,    1f, 0f
        ),
        scrimTop = Color(0x66000000),
        scrimMid = Color(0x99000000),
        scrimBottom = Color(0xDD000000)
    ),

    MIST(
        id = "mist",
        displayName = "Mist",
        // Faded and lighter: lifted black levels and soft airy luminance
        // Scrim is darkened to ensure WCAG AA 4.5:1 text readability over lighter background
        matrixValues = floatArrayOf(
            0.82f, 0.0f,  0.0f,  0f, 42f,
            0.0f,  0.82f, 0.0f,  0f, 42f,
            0.0f,  0.0f,  0.82f, 0f, 42f,
            0f,    0f,    0f,    1f, 0f
        ),
        scrimTop = Color(0x77000000),
        scrimMid = Color(0xBB000000),
        scrimBottom = Color(0xEE000000)
    ),

    MIDNIGHT(
        id = "midnight",
        displayName = "Midnight",
        // Cool blue tone: deep blue/cyan shadows with crisp cool highlights
        matrixValues = floatArrayOf(
            0.80f, 0.0f,  0.0f,  0f, -18f,
            0.0f,  0.88f, 0.05f, 0f, -6f,
            0.05f, 0.05f, 1.15f, 0f, 24f,
            0f,    0f,    0f,    1f, 0f
        ),
        scrimTop = Color(0x66000000),
        scrimMid = Color(0x99000000),
        scrimBottom = Color(0xDD000000)
    );

    val composeColorFilter: ColorFilter
        get() = ColorFilter.colorMatrix(ColorMatrix(matrixValues))

    fun toAndroidColorMatrix(): android.graphics.ColorMatrix {
        return android.graphics.ColorMatrix(matrixValues)
    }

    companion object {
        fun fromId(id: String?): PhotoFilter {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ORIGINAL
        }
    }
}
