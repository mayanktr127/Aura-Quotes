package com.myplanetservices.stillwords.data.repository

import android.content.Context
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
 * 3. Thread-safe in-memory caching of asset lists.
 */
class BackgroundRepository(private val context: Context) {

    private val themeFilesCache = ConcurrentHashMap<String, List<String>>()

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
