package com.myplanetservices.stillwords.ui.screens.designer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.myplanetservices.stillwords.data.model.BackgroundTheme
import com.myplanetservices.stillwords.data.model.CardAspectRatio
import com.myplanetservices.stillwords.data.model.CardStyle
import com.myplanetservices.stillwords.data.model.PhotoFilter
import com.myplanetservices.stillwords.data.model.Quote
import com.myplanetservices.stillwords.data.model.QuoteFont
import com.myplanetservices.stillwords.data.repository.BackgroundRepository
import com.myplanetservices.stillwords.data.repository.QuoteRepository
import com.myplanetservices.stillwords.util.ImageExportUtil
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuoteDesignerUiState(
    val quoteText: String = "Your mindset shapes your day. Choose wonder over worry, intention over habit.",
    val author: String = "Stillwords",
    val category: String = "Motivation",
    val cardStyle: CardStyle = CardStyle.NOIR_STILLWORDS,
    val selectedPhotoPath: String? = null,
    val selectedFilter: PhotoFilter = PhotoFilter.ORIGINAL,
    val availableThemes: List<Pair<BackgroundTheme, String>> = emptyList(),
    val quoteFont: QuoteFont = QuoteFont.EDITORIAL_SERIF,
    val aspectRatio: CardAspectRatio = CardAspectRatio.PORTRAIT_4_5,
    val showWatermark: Boolean = true,
    val isExporting: Boolean = false,
    val exportSuccessMessage: String? = null,
    val isCustomQuote: Boolean = false,
    val isSaveQuoteSuccess: Boolean = false,
    val isPremiumUnlocked: Boolean = false,
    val premiumExpiresAt: Long = 0L
)

class QuoteDesignerViewModel(
    private val initialQuoteId: String?,
    private val repository: QuoteRepository,
    private val backgroundRepository: BackgroundRepository,
    private val preferencesRepository: com.myplanetservices.stillwords.data.preferences.UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuoteDesignerUiState())
    val uiState: StateFlow<QuoteDesignerUiState> = _uiState.asStateFlow()

    init {
        val themes = backgroundRepository.getAllThemeThumbnails()
        _uiState.value = _uiState.value.copy(availableThemes = themes)

        // Observe premium unlock status from preferences
        viewModelScope.launch {
            preferencesRepository.userPreferencesFlow.collect { prefs ->
                _uiState.value = _uiState.value.copy(
                    isPremiumUnlocked = prefs.isPremiumUnlocked,
                    premiumExpiresAt = prefs.premiumUnlockedUntil
                )
            }
        }

        if (initialQuoteId != null) {
            viewModelScope.launch {
                val quote = repository.getQuoteById(initialQuoteId)
                if (quote != null) {
                    val photoPath = backgroundRepository.getBackgroundAssetPath(quote.id, quote.category)
                    _uiState.value = _uiState.value.copy(
                        quoteText = quote.text,
                        author = quote.author,
                        category = quote.category,
                        cardStyle = quote.defaultStyle,
                        selectedPhotoPath = photoPath
                    )
                }
            }
        } else {
            val defaultPhoto = backgroundRepository.getBackgroundAssetPath("default_quote", "Motivation")
            _uiState.value = _uiState.value.copy(selectedPhotoPath = defaultPhoto)
        }
    }

    fun onQuoteTextChanged(text: String) {
        _uiState.value = _uiState.value.copy(quoteText = text, isCustomQuote = true)
    }

    fun onAuthorChanged(author: String) {
        _uiState.value = _uiState.value.copy(author = author)
    }

    fun onCategoryChanged(category: String) {
        val photoPath = backgroundRepository.getBackgroundAssetPath(initialQuoteId ?: "preview", category)
        _uiState.value = _uiState.value.copy(
            category = category,
            selectedPhotoPath = photoPath ?: _uiState.value.selectedPhotoPath
        )
    }

    fun onPhotoSelected(photoPath: String?) {
        _uiState.value = _uiState.value.copy(selectedPhotoPath = photoPath)
    }

    fun onFilterSelected(filter: PhotoFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun onStyleSelected(style: CardStyle) {
        // When a solid/gradient card style is explicitly selected, clear photo so style is displayed
        _uiState.value = _uiState.value.copy(cardStyle = style, selectedPhotoPath = null)
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
                    context = context,
                    quoteText = state.quoteText,
                    author = state.author,
                    category = state.category,
                    cardStyle = state.cardStyle,
                    quoteFont = state.quoteFont,
                    aspectRatio = state.aspectRatio,
                    showWatermark = state.showWatermark,
                    photoAssetPath = state.selectedPhotoPath,
                    photoFilter = state.selectedFilter
                )
                val uri = ImageExportUtil.saveBitmapToGallery(context, bitmap)
                _uiState.value = _uiState.value.copy(isExporting = false)
                if (uri != null) {
                    onResult(true, "Quote image saved to Pictures/Stillwords")
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
                    context = context,
                    quoteText = state.quoteText,
                    author = state.author,
                    category = state.category,
                    cardStyle = state.cardStyle,
                    quoteFont = state.quoteFont,
                    aspectRatio = state.aspectRatio,
                    showWatermark = state.showWatermark,
                    photoAssetPath = state.selectedPhotoPath,
                    photoFilter = state.selectedFilter
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

    fun unlockPremiumFor24Hours() {
        viewModelScope.launch {
            preferencesRepository.unlockPremiumFor24Hours()
        }
    }

    class Factory(
        private val context: Context,
        private val initialQuoteId: String?,
        private val repository: QuoteRepository,
        private val preferencesRepository: com.myplanetservices.stillwords.data.preferences.UserPreferencesRepository? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val prefs = preferencesRepository ?: com.myplanetservices.stillwords.data.preferences.UserPreferencesRepository(context)
            return QuoteDesignerViewModel(initialQuoteId, repository, BackgroundRepository(context), prefs) as T
        }
    }
}

