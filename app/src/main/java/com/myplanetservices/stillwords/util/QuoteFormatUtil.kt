package com.myplanetservices.stillwords.util

import com.myplanetservices.stillwords.data.model.Quote
import java.net.URLDecoder

object QuoteFormatUtil {

    /**
     * Formats the source and translator line for classic quotes.
     * E.g. "Meditations · tr. George Long, 1862"
     * Returns null for Stillwords original quotes or user-created quotes.
     */
    fun formatClassicQuoteSource(quote: Quote): String? {
        if (quote.isOriginal || quote.isUserCreated) return null
        val rawSource = quote.source.trim()
        val translator = quote.translator?.trim()

        val workTitle = parseWorkTitle(quote.author, rawSource)
        val trPart = if (!translator.isNullOrBlank()) {
            if (translator.startsWith("tr.", ignoreCase = true)) translator else "tr. $translator"
        } else null

        return when {
            workTitle != null && trPart != null -> "$workTitle · $trPart"
            workTitle != null -> workTitle
            trPart != null -> trPart
            else -> null
        }
    }

    private fun parseWorkTitle(author: String, rawSource: String): String? {
        if (rawSource.isBlank()) return null
        val fragment = if (rawSource.contains("#")) {
            rawSource.substringAfterLast("#")
        } else if (rawSource.contains("/")) {
            rawSource.substringAfterLast("/")
        } else {
            rawSource
        }

        val decoded = try {
            URLDecoder.decode(fragment, "UTF-8")
        } catch (e: Exception) {
            fragment
        }

        // Handle specific classical author works
        if (author.equals("Marcus Aurelius", ignoreCase = true)) {
            val bookPart = if (decoded.contains("Book", ignoreCase = true)) {
                decoded.replace("_", " ").trim()
            } else null
            return if (bookPart != null && bookPart.isNotBlank()) "Meditations, $bookPart" else "Meditations"
        }

        if (author.equals("Seneca the Younger", ignoreCase = true) || author.equals("Seneca", ignoreCase = true)) {
            if (decoded.contains("Letter", ignoreCase = true)) {
                return decoded.replace("_", " ").trim()
            }
        }

        var clean = decoded.replace("_", " ").trim()
        if (clean.startsWith("w") && clean.length > 15 && clean.contains("book")) {
            val bookIndex = clean.indexOf("book")
            clean = clean.substring(bookIndex + 4)
        }

        // If it's just the author's name or an unparsed URL, don't show it as the work title
        if (clean.equals(author, ignoreCase = true) || clean.startsWith("http", ignoreCase = true)) {
            return null
        }

        return clean.ifBlank { null }
    }
}
