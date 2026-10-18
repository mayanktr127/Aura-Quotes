package com.myplanetservices.stillwords.util

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
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
        photoAssetPath: String? = null
    ): Bitmap = withContext(Dispatchers.Default) {
        val targetWidth = 1080
        val targetHeight = (targetWidth / aspectRatio.ratio).toInt()

        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        var isPhotoDrawn = false
        if (photoAssetPath != null) {
            try {
                val photoBitmap = context.assets.open(photoAssetPath).use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
                if (photoBitmap != null) {
                    val scale = maxOf(targetWidth.toFloat() / photoBitmap.width, targetHeight.toFloat() / photoBitmap.height)
                    val scaledW = photoBitmap.width * scale
                    val scaledH = photoBitmap.height * scale
                    val left = (targetWidth - scaledW) / 2f
                    val top = (targetHeight - scaledH) / 2f
                    val filterPaint = Paint(Paint.FILTER_BITMAP_FLAG)
                    canvas.drawBitmap(photoBitmap, null, RectF(left, top, left + scaledW, top + scaledH), filterPaint)
                    photoBitmap.recycle()
                    isPhotoDrawn = true
                }
            } catch (e: Exception) {
                isPhotoDrawn = false
            }
        }

        if (isPhotoDrawn) {
            // Draw dark gradient scrim (lighter at top, darker at bottom, ensures WCAG AA 4.5:1 contrast)
            val scrimPaint = Paint().apply {
                isAntiAlias = true
                shader = LinearGradient(
                    0f, 0f, 0f, targetHeight.toFloat(),
                    intArrayOf(0x66000000.toInt(), 0x99000000.toInt(), 0xDD000000.toInt()),
                    floatArrayOf(0f, 0.45f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), scrimPaint)

            // Gold frame border matching luxury card style
            val framePaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = 3f
                color = android.graphics.Color.argb(70, 229, 169, 60) // Gold
            }
            val inset = 40f
            canvas.drawRoundRect(
                RectF(inset, inset, targetWidth - inset, targetHeight - inset),
                32f, 32f,
                framePaint
            )
        } else {
            // Draw Background from cardStyle
            val bgPaint = Paint().apply {
                isAntiAlias = true
                color = android.graphics.Color.argb(
                    (cardStyle.backgroundColor.alpha * 255).toInt(),
                    (cardStyle.backgroundColor.red * 255).toInt(),
                    (cardStyle.backgroundColor.green * 255).toInt(),
                    (cardStyle.backgroundColor.blue * 255).toInt()
                )
            }
            canvas.drawRect(0f, 0f, targetWidth.toFloat(), targetHeight.toFloat(), bgPaint)

            // Draw subtle border frame for noir / luxury
            if (cardStyle == CardStyle.NOIR_STILLWORDS) {
                val framePaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                    color = android.graphics.Color.argb(70, 229, 169, 60) // Gold
                }
                val inset = 40f
                canvas.drawRoundRect(
                    RectF(inset, inset, targetWidth - inset, targetHeight - inset),
                    32f, 32f,
                    framePaint
                )
            }
        }

        // Active palette resolution: photo mode uses warm white and gold
        val activeTextColor = if (isPhotoDrawn) {
            android.graphics.Color.rgb(251, 247, 238)
        } else {
            android.graphics.Color.argb(
                (cardStyle.textColor.alpha * 255).toInt(),
                (cardStyle.textColor.red * 255).toInt(),
                (cardStyle.textColor.green * 255).toInt(),
                (cardStyle.textColor.blue * 255).toInt()
            )
        }

        val activeAuthorColor = if (isPhotoDrawn) {
            android.graphics.Color.rgb(212, 200, 176)
        } else {
            android.graphics.Color.argb(
                (cardStyle.authorColor.alpha * 255).toInt(),
                (cardStyle.authorColor.red * 255).toInt(),
                (cardStyle.authorColor.green * 255).toInt(),
                (cardStyle.authorColor.blue * 255).toInt()
            )
        }

        val activeMetaColor = if (isPhotoDrawn) {
            android.graphics.Color.rgb(229, 169, 60)
        } else {
            activeAuthorColor
        }

        // Draw Category or Stillwords emblem top
        val metaPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 34f
            color = activeMetaColor
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            letterSpacing = 0.25f
        }
        val topY = if (aspectRatio == CardAspectRatio.STORY_9_16) targetHeight * 0.22f else targetHeight * 0.16f
        canvas.drawText("“  ${category.uppercase()}  ”", targetWidth / 2f, topY, metaPaint)

        // Typeface selection
        val mainTypeface = when (quoteFont) {
            QuoteFont.EDITORIAL_SERIF -> Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            QuoteFont.ELEGANT_ITALIC -> Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            QuoteFont.MODERN_SANS -> Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            QuoteFont.BOLD_DISPLAY -> Typeface.create(Typeface.SERIF, Typeface.BOLD)
            QuoteFont.MINIMALIST -> Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            QuoteFont.MONOSPACE_POETIC -> Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        }

        // Quote Text Paint
        val baseFontSize = when {
            quoteText.length > 200 -> 48f
            quoteText.length > 120 -> 56f
            quoteText.length > 60 -> 68f
            else -> 78f
        }

        val textPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = baseFontSize
            color = activeTextColor
            typeface = mainTypeface
            letterSpacing = quoteFont.letterSpacing
        }

        val contentWidth = (targetWidth * 0.78f).toInt()
        val formattedText = if (quoteFont.isAllCaps) quoteText.uppercase() else quoteText

        @Suppress("DEPRECATION")
        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(formattedText, 0, formattedText.length, textPaint, contentWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(16f, 1.15f)
                .build()
        } else {
            StaticLayout(
                formattedText,
                textPaint,
                contentWidth,
                Layout.Alignment.ALIGN_CENTER,
                1.15f,
                16f,
                false
            )
        }

        // Center quote vertically
        val textHeight = staticLayout.height
        val startY = (targetHeight - textHeight) / 2f - 40f
        val startX = (targetWidth - contentWidth) / 2f

        canvas.save()
        canvas.translate(startX, startY)
        staticLayout.draw(canvas)
        canvas.restore()

        // Author Text
        val authorPaint = TextPaint().apply {
            isAntiAlias = true
            textSize = 38f
            color = activeAuthorColor
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        val authorY = startY + textHeight + 70f
        canvas.drawText("— $author", targetWidth / 2f, authorY, authorPaint)

        // Watermark bottom
        if (showWatermark) {
            val watermarkPaint = TextPaint().apply {
                isAntiAlias = true
                textSize = 28f
                color = if (isPhotoDrawn) {
                    android.graphics.Color.argb(160, 212, 200, 176)
                } else {
                    android.graphics.Color.argb(
                        120,
                        (cardStyle.authorColor.red * 255).toInt(),
                        (cardStyle.authorColor.green * 255).toInt(),
                        (cardStyle.authorColor.blue * 255).toInt()
                    )
                }
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
                textAlign = Paint.Align.CENTER
                letterSpacing = 0.2f
            }
            val bottomY = targetHeight - 50f
            canvas.drawText("STILLWORDS • DAILY INSPIRATION", targetWidth / 2f, bottomY, watermarkPaint)
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
        quoteId: String? = null
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
                    aspectRatio = CardAspectRatio.SQUARE_1_1,
                    showWatermark = true,
                    photoAssetPath = effectivePhoto
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

