package com.myplanetservices.stillwords.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.myplanetservices.stillwords.data.model.BackgroundTheme
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * Repository that dynamically lists background photo files per theme from assets
 * and deterministically assigns a background photo to each quote based on quote ID.
 *
 * Guarantees:
 * 1. No hardcoded file names — files are discovered dynamically from assets/backgrounds/<theme>/.
 * 2. Deterministic assignment — the same quote always displays the exact same photo.
 * 3. Thread-safe process-wide caching of asset lists and downsampled thumbnails.
 */
class BackgroundRepository(private val context: Context) {

    companion object {
        private val themeFilesCache = ConcurrentHashMap<String, List<String>>()
        private val thumbnailCache = ConcurrentHashMap<String, Bitmap>()
    }

    /**
     * Decodes and caches a small downsampled Bitmap (target size ~160px) for thumbnails.
     * Uses inSampleSize to minimize memory and decode overhead.
     */
    fun getThumbnailBitmap(assetPath: String, targetSize: Int = 160): Bitmap? {
        return thumbnailCache.getOrPut("$assetPath@$targetSize") {
            try {
                val options = BitmapFactory.Options().apply {
                    inJustDecodeBounds = true
                }
                context.assets.open(assetPath).use { stream ->
                    BitmapFactory.decodeStream(stream, null, options)
                }
                val width = options.outWidth
                val height = options.outHeight
                if (width <= 0 || height <= 0) return null

                var sampleSize = 1
                while ((width / (sampleSize * 2)) >= targetSize && (height / (sampleSize * 2)) >= targetSize) {
                    sampleSize *= 2
                }

                val decodeOptions = BitmapFactory.Options().apply {
                    inSampleSize = sampleSize
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                }
                val decoded = context.assets.open(assetPath).use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOptions)
                } ?: return null

                val targetHeight = (targetSize * (height.toFloat() / width.toFloat())).toInt().coerceAtLeast(1)
                val scaled = Bitmap.createScaledBitmap(decoded, targetSize, targetHeight, true)
                if (scaled != decoded) {
                    decoded.recycle()
                }
                scaled
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Returns all WebP background asset file names under backgrounds/<theme.id>/.
     * Results are cached in memory so asset listing occurs at most once per theme.
     */
    fun getBackgroundFiles(theme: BackgroundTheme): List<String> {
        return themeFilesCache.getOrPut(theme.id) {
            try {
                val list = context.assets.list("backgrounds/${theme.id}")
                list?.filter { it.endsWith(".webp", ignoreCase = true) }
                    ?.sorted()
                    ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * Deterministically resolves the background asset path for a quote.
     * Format: "backgrounds/<theme-id>/<filename>.webp"
     * Returns null if the category has no photo theme or no background files exist.
     */
    fun getBackgroundAssetPath(quoteId: String, category: String): String? {
        val theme = BackgroundTheme.fromCategory(category) ?: return null
        val files = getBackgroundFiles(theme)
        if (files.isEmpty()) return null

        val deterministicHash = abs(quoteId.hashCode())
        val selectedIndex = deterministicHash % files.size
        return "backgrounds/${theme.id}/${files[selectedIndex]}"
    }

    /**
     * Converts a relative asset path (e.g. "backgrounds/mountains/mountains-01.webp")
     * into a Coil-compatible asset URI ("file:///android_asset/...").
     */
    fun getCoilUri(assetPath: String): String {
        return "file:///android_asset/$assetPath"
    }

    /**
     * Returns the first background photo asset path for a theme (used as thumbnail in Designer).
     */
    fun getThumbnailAssetPath(theme: BackgroundTheme): String? {
        val files = getBackgroundFiles(theme)
        return files.firstOrNull()?.let { "backgrounds/${theme.id}/$it" }
    }

    /**
     * Returns thumbnail asset paths for all available themes.
     */
    fun getAllThemeThumbnails(): List<Pair<BackgroundTheme, String>> {
        return BackgroundTheme.entries.mapNotNull { theme ->
            getThumbnailAssetPath(theme)?.let { path -> theme to path }
        }
    }
}
