package com.myplanetservices.auraquotes.ui.screens.designer

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myplanetservices.auraquotes.data.model.CardAspectRatio
import com.myplanetservices.auraquotes.data.model.CardStyle
import com.myplanetservices.auraquotes.data.model.Quote
import com.myplanetservices.auraquotes.data.model.QuoteFont
import com.myplanetservices.auraquotes.data.repository.QuoteRepository
import com.myplanetservices.auraquotes.util.ImageExportUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuoteDesignerUiState(
    val quoteText: String = "Your mindset shapes your day. Choose wonder over worry, intention over habit.",
    val author: String = "Marcus Aurelius",
    val category: String = "Motivation",
    val cardStyle: CardStyle = CardStyle.NOIR_AURA,
    val quoteFont: QuoteFont = QuoteFont.EDITORIAL_SERIF,
    val aspectRatio: CardAspectRatio = CardAspectRatio.PORTRAIT_4_5,
    val showWatermark: Boolean = true,
    val isExporting: Boolean = false,
    val exportSuccessMessage: String? = null,
    val isCustomQuote: Boolean = false,
    val isSaveQuoteSuccess: Boolean = false
)

class QuoteDesignerViewModel(
    private val initialQuoteId: String?,
    private val repository: QuoteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuoteDesignerUiState())
    val uiState: StateFlow<QuoteDesignerUiState> = _uiState.asStateFlow()

    init {
        if (initialQuoteId != null) {
            viewModelScope.launch {
                val quote = repository.getQuoteById(initialQuoteId)
                if (quote != null) {
                    _uiState.value = _uiState.value.copy(
                        quoteText = quote.text,
                        author = quote.author,
                        category = quote.category,
                        cardStyle = quote.defaultStyle
                    )
                }
            }
        }
    }

    fun onQuoteTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(quoteText = text, isCustomQuote = true)
    }

    fun onAuthorChanged(author: String) {
        _uiState.value = _uiState.value.copy(author = author)
    }

    fun onCategoryChanged(category: String) {
        _uiState.value = _uiState.value.copy(category = category)
    }

    fun onStyleSelected(style: CardStyle) {
        _uiState.value = _uiState.value.copy(cardStyle = style)
    }

    fun onFontSelected(font: QuoteFont) {
        _uiState.value = _uiState.value.copy(quoteFont = font)
    }

    fun onAspectRatioSelected(ratio: CardAspectRatio) {
        _uiState.value = _uiState.value.copy(aspectRatio = ratio)
    }

    fun toggleWatermark(show: Boolean) {
        _uiState.value = _uiState.value.copy(showWatermark = show)
    }

    fun saveAsUserQuote() {
        viewModelScope.launch {
            val state = _uiState.value
            repository.createUserQuote(
                text = state.quoteText,
                author = state.author,
                category = state.category,
                tags = "custom,created"
            )
            _uiState.value = _uiState.value.copy(isSaveQuoteSuccess = true)
        }
    }

    fun exportImage(context: Context, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            val state = _uiState.value
            try {
                val bitmap = ImageExportUtil.createQuoteBitmap(
                    quoteText = state.quoteText,
                    author = state.author,
                    category = state.category,
                    cardStyle = state.cardStyle,
                    quoteFont = state.quoteFont,
                    aspectRatio = state.aspectRatio,
                    showWatermark = state.showWatermark
                )
                val uri = ImageExportUtil.saveBitmapToGallery(context, bitmap)
                _uiState.value = _uiState.value.copy(isExporting = false)
                if (uri != null) {
                    onResult(true, "Quote image saved to Pictures/AuraQuotes")
                } else {
                    onResult(false, "Could not save image to gallery")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false)
                onResult(false, e.localizedMessage ?: "Export failed")
            }
        }
    }

    fun shareImage(context: Context, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            val state = _uiState.value
            try {
                val bitmap = ImageExportUtil.createQuoteBitmap(
                    quoteText = state.quoteText,
                    author = state.author,
                    category = state.category,
                    cardStyle = state.cardStyle,
                    quoteFont = state.quoteFont,
                    aspectRatio = state.aspectRatio,
                    showWatermark = state.showWatermark
                )
                val uri = ImageExportUtil.cacheBitmapForSharing(context, bitmap)
                _uiState.value = _uiState.value.copy(isExporting = false)
                if (uri != null) {
                    ImageExportUtil.shareImage(context, uri, state.quoteText, state.author)
                    onResult(true)
                } else {
                    onResult(false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false)
                onResult(false)
            }
        }
    }

    class Factory(
        private val initialQuoteId: String?,
        private val repository: QuoteRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QuoteDesignerViewModel(initialQuoteId, repository) as T
        }
    }
}
