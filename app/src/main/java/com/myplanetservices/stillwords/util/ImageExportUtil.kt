package com.myplanetservices.stillwords.util

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.myplanetservices.stillwords.data.model.CardAspectRatio
import com.myplanetservices.stillwords.data.model.CardStyle
import com.myplanetservices.stillwords.data.model.PhotoFilter
import com.myplanetservices.stillwords.data.model.QuoteFont
import com.myplanetservices.stillwords.data.repository.BackgroundRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

object ImageExportUtil {

    suspend fun createQuoteBitmap(
        context: Context,
        quoteText: String,
        author: String,
        category: String,
        cardStyle: CardStyle,
        quoteFont: QuoteFont,
        aspectRatio: CardAspectRatio,
        showWatermark: Boolean = true,
        photoAssetPath: String? = null,
        photoFilter: PhotoFilter = PhotoFilter.ORIGINAL,
        fontScale: Float = 1.0f
    ): Bitmap = withContext(Dispatchers.Default) {
        // Section 7: Sizes: 1:1 = 1080 x 1080, 4:5 = 1080 x 1350, 9:16 = 1080 x 1920
        val targetWidth = 1080
        val targetHeight = when (aspectRatio) {
            CardAspectRatio.SQUARE_1_1 -> 1080
            CardAspectRatio.PORTRAIT_4_5 -> 1350
            CardAspectRatio.STORY_9_16 -> 1920
            CardAspectRatio.LANDSCAPE_16_9 -> 608
        }

        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Ensure a photo is always included (Section 7 & requirement 4: "The image must include the photo")
        val effectivePhotoPath = photoAssetPath
            ?: BackgroundRepository(context).getBackgroundAssetPath("export_quote", category)
            ?: "backgrounds/still-water/still-water-01.webp"

        var isPhotoDrawn = false
        try {
            val photoBitmap = context.assets.open(effectivePhotoPath).use { stream ->
                BitmapFactory.decodeStream(stream)
            }
            if (photoBitmap != null) {
                val scale = maxOf(targetWidth.toFloat() / photoBitmap.width, targetHeight.toFloat() / photoBitmap.height)
                val scaledW = photoBitmap.width * scale
                val scaledH = photoBitmap.height * scale
                val left = (targetWidth - scaledW) / 2f
                val top = (targetHeight - scaledH) / 2f
                val filterPaint = Paint(Paint.FILTER_BITMAP_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(photoFilter.toAndroidColorMatrix())
                }
                canvas.drawBitmap(photoBitmap, null, RectF(left, top, left + scaledW, top + scaledH), filterPaint)
                photoBitmap.recycle()
                isPhotoDrawn = true
            }
        } catch (_: Exception) {
            isPhotoDrawn = false
        }

        if (isPhotoDrawn) {
            // Scrim: 20% -> 65% Ink (#0F0E0C) top to bottom (with custom scrims for filters like Mist for WCAG AA compliance)
            val topAlpha = if (photoFilter == PhotoFilter.MIST) (photoFilter.scrimTop.alpha * 255).toInt() else (0.20f * 255).toInt()
            val bottomAlpha = if (photoFilter == PhotoFilter.MIST) (photoFilter.scrimBottom.alpha * 255).toInt() else (0.65f * 255).toInt()
            val midAlpha = ((topAlpha + bottomAlpha) / 2)

            val topColor = android.graphics.Color.argb(topAlpha, 15, 14, 12)
            val midColor = android.graphics.Color.argb(midAlpha, 15, 14, 12)
            val bottomColor = android.graphics.Color.argb(bottomAlpha, 15, 14, 12)

            val scrimPaint = Paint().apply {
                isAntiAlias = true
                shader = LinearGradient(
                    0f, 0f, 0f, targetHeight.toFloat(),
                    intArrayOf(topColor, midColor, bottomColor),
                    floatArrayOf(0f, 0.40f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), scrimPaint)

            // Gold frame border matching luxury card style
            val framePaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 3f
                color = android.graphics.Color.argb(70, 229, 169, 60) // Brass/Gold
            }
            val inset = 40f
            canvas.drawRoundRect(
                RectF(inset, inset, targetWidth - inset, targetHeight - inset),
                32f, 32f,
                framePaint
            )
        } else {
            // Fallback plain background
            val bgPaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.argb(255, 15, 14, 12)
            }
            canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), bgPaint)
        }

        // Section 7 Bundled Fonts loading via ResourcesCompat
        val newsreaderRegular = androidx.core.content.res.ResourcesCompat.getFont(
            context,
            com.myplanetservices.stillwords.R.font.newsreader_regular
        ) ?: Typeface.SERIF

        val newsreaderItalic = androidx.core.content.res.ResourcesCompat.getFont(
            context,
            com.myplanetservices.stillwords.R.font.newsreader_italic
        ) ?: Typeface.create(Typeface.SERIF, Typeface.ITALIC)

        val newsreaderMedium = androidx.core.content.res.ResourcesCompat.getFont(
            context,
            com.myplanetservices.stillwords.R.font.newsreader_medium
        ) ?: Typeface.SERIF

        val figtreeRegular = androidx.core.content.res.ResourcesCompat.getFont(
            context,
            com.myplanetservices.stillwords.R.font.figtree_regular
        ) ?: Typeface.SANS_SERIF

        val figtreeMedium = androidx.core.content.res.ResourcesCompat.getFont(
            context,
            com.myplanetservices.stillwords.R.font.figtree_medium
        ) ?: Typeface.SANS_SERIF

        val figtreeSemibold = androidx.core.content.res.ResourcesCompat.getFont(
            context,
            com.myplanetservices.stillwords.R.font.figtree_semibold
        ) ?: Typeface.DEFAULT_BOLD

        // Colors per Section 7:
        // Quote text: Ivory #F3EDE3
        val activeTextColor = android.graphics.Color.rgb(243, 237, 227)
        // Author: Ivory 75%
        val activeAuthorColor = android.graphics.Color.argb((0.75f * 255).toInt(), 243, 237, 227)
        // Category: Brass/Gold #E8D3A8
        val activeCategoryColor = android.graphics.Color.rgb(232, 211, 168)

        // Top Category label (safe below gold frame: 190f for 1:1, 220f for 4:5, 320f for 9:16)
        val metaPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 32f
            color = activeCategoryColor
            typeface = figtreeMedium
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.25f
        }
        val topY = when (aspectRatio) {
            CardAspectRatio.LANDSCAPE_16_9 -> 118f
            CardAspectRatio.STORY_9_16 -> 320f
            CardAspectRatio.PORTRAIT_4_5 -> 220f
            CardAspectRatio.SQUARE_1_1 -> 190f
        }
        canvas.drawText("“  ${category.uppercase()}  ”", targetWidth / 2f, topY, metaPaint)

        // Typeface selection
        val mainTypeface = when (quoteFont) {
            QuoteFont.EDITORIAL_SERIF -> newsreaderRegular
            QuoteFont.ELEGANT_ITALIC -> newsreaderItalic
            QuoteFont.MODERN_SANS -> figtreeSemibold
            QuoteFont.BOLD_DISPLAY -> newsreaderMedium
            QuoteFont.MINIMALIST -> figtreeRegular
            QuoteFont.MONOSPACE_POETIC -> Typeface.MONOSPACE
        }

        // Section 7: Quote in Newsreader 400, Ivory, auto-sized to fit with ~9% side margins, balanced lines, never truncated
        val contentWidth = (targetWidth * 0.82f).toInt() // 9% margins on left and right
        val formattedText = if (quoteFont.isAllCaps) quoteText.uppercase() else quoteText

        val baseFontSize = when {
            quoteText.length > 150 -> 48f
            quoteText.length > 100 -> 56f
            quoteText.length > 50  -> 66f
            else -> 76f
        }
        val initialFontSize = (baseFontSize * fontScale)

        val maxAvailableTextHeight = when (aspectRatio) {
            CardAspectRatio.LANDSCAPE_16_9 -> targetHeight * 0.44f
            CardAspectRatio.SQUARE_1_1 -> targetHeight * 0.50f
            CardAspectRatio.PORTRAIT_4_5 -> targetHeight * 0.54f
            CardAspectRatio.STORY_9_16 -> targetHeight * 0.58f
        }

        val textPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = initialFontSize
            color = activeTextColor
            typeface = mainTypeface
            letterSpacing = quoteFont.letterSpacing
        }

        var currentFontSize = initialFontSize
        var staticLayout: StaticLayout
        while (true) {
            textPaint.textSize = currentFontSize
            @Suppress("DEPRECATION")
            staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(formattedText, 0, formattedText.length, textPaint, contentWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(14f, 1.25f)
                    .build()
            } else {
                StaticLayout(
                    formattedText,
                    textPaint,
                    contentWidth,
                    Layout.Alignment.ALIGN_CENTER,
                    1.25f,
                    14f,
                    false
                )
            }

            if (staticLayout.height <= maxAvailableTextHeight || currentFontSize <= 26f) {
                break
            }
            currentFontSize -= 2f
        }

        // Center quote vertically between header and author
        val textHeight = staticLayout.height
        val startY = ((targetHeight - textHeight) / 2f - 24f).coerceAtLeast(topY + 65f)
        val startX = (targetWidth - contentWidth) / 2f

        canvas.save()
        canvas.translate(startX, startY)
        staticLayout.draw(canvas)
        canvas.restore()

        // Section 7: Author in Newsreader Italic, Ivory 75%
        val authorPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 38f
            color = activeAuthorColor
            typeface = newsreaderItalic
            textAlign = Paint.Align.CENTER
        }
        val authorY = startY + textHeight + 64f
        canvas.drawText("— $author", targetWidth / 2f, authorY, authorPaint)

        // Section 7: Small "STILLWORDS" wordmark centred near the bottom, Figtree, letter-spacing ~2.5, Ivory 55%
        if (showWatermark) {
            val watermarkPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 26f
                color = android.graphics.Color.argb((0.55f * 255).toInt(), 243, 237, 227) // Ivory 55%
                typeface = figtreeSemibold
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.25f
            }
            val bottomY = targetHeight - 68f
            canvas.drawText("STILLWORDS", targetWidth / 2f, bottomY, watermarkPaint)
        }

        bitmap
    }

    suspend fun saveBitmapToGallery(context: Context, bitmap: Bitmap): Uri? = withContext(Dispatchers.IO) {
        val filename = "Stillwords_${System.currentTimeMillis()}.png"
        var fos: OutputStream? = null
        var imageUri: Uri? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = context.contentResolver
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Stillwords")
                }
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                if (imageUri != null) {
                    fos = resolver.openOutputStream(imageUri)
                }
            } else {
                val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val stillwordsDir = File(imagesDir, "Stillwords").apply { if (!exists()) mkdirs() }
                val imageFile = File(stillwordsDir, filename)
                fos = FileOutputStream(imageFile)
                imageUri = Uri.fromFile(imageFile)
            }

            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            imageUri
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun cacheBitmapForSharing(context: Context, bitmap: Bitmap): Uri? = withContext(Dispatchers.IO) {
        try {
            val cachePath = File(context.cacheDir, "shared_quotes")
            if (!cachePath.exists()) cachePath.mkdirs()
            val file = File(cachePath, "shared_quote_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareImage(context: Context, uri: Uri, quoteText: String, author: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "“$quoteText” — $author\n\nShared from Stillwords")
            clipData = ClipData.newUri(context.contentResolver, "Stillwords Quote", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Share Quote Card").apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(chooser)
    }

    fun shareCardImage(
        context: Context,
        quoteText: String,
        author: String,
        cardStyle: CardStyle = CardStyle.NOIR_STILLWORDS,
        quoteFont: QuoteFont = QuoteFont.EDITORIAL_SERIF,
        category: String = "Inspiration",
        photoAssetPath: String? = null,
        quoteId: String? = null,
        aspectRatio: CardAspectRatio = CardAspectRatio.PORTRAIT_4_5,
        photoFilter: PhotoFilter = PhotoFilter.ORIGINAL
    ) {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val effectivePhoto = photoAssetPath ?: if (quoteId != null) {
                    BackgroundRepository(context).getBackgroundAssetPath(quoteId, category)
                } else null

                val bitmap = createQuoteBitmap(
                    context = context,
                    quoteText = quoteText,
                    author = author,
                    category = category,
                    cardStyle = cardStyle,
                    quoteFont = quoteFont,
                    aspectRatio = aspectRatio,
                    showWatermark = true,
                    photoAssetPath = effectivePhoto,
                    photoFilter = photoFilter
                )
                val uri = cacheBitmapForSharing(context, bitmap)
                if (uri != null) {
                    shareImage(context, uri, quoteText, author)
                } else {
                    shareText(context, quoteText, author)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                shareText(context, quoteText, author)
            }
        }
    }

    fun shareText(context: Context, quoteText: String, author: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "“$quoteText”\n\n— $author\n\nDiscover more in Stillwords: Daily Inspiration")
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share Quote"))
    }
}

