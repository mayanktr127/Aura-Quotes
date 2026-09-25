package com.example.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.CardStyle
import com.example.data.model.Quote
import com.example.data.model.QuoteCollection
import com.example.data.repository.QuoteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class QuoteDetailUiState(
    val quote: Quote? = null,
    val cardStyle: CardStyle = CardStyle.NOIR_AURA,
    val relatedQuotes: List<Quote> = emptyList(),
    val reflectionText: String = "",
    val isReflectionEditing: Boolean = false,
    val collections: List<QuoteCollection> = emptyList(),
    val activeCollectionIds: List<Long> = emptyList(),
    val isCollectionSheetOpen: Boolean = false
)

private data class UiControls(
    val cardStyle: CardStyle = CardStyle.NOIR_AURA,
    val reflectionText: String = "",
    val isReflectionEditing: Boolean = false,
    val isCollectionSheetOpen: Boolean = false
)

class QuoteDetailViewModel(
    private val quoteId: String,
    private val repository: QuoteRepository
) : ViewModel() {

    private val _cardStyle = MutableStateFlow(CardStyle.NOIR_AURA)
    private val _isReflectionEditing = MutableStateFlow(false)
    private val _isCollectionSheetOpen = MutableStateFlow(false)
    private val _reflectionText = MutableStateFlow("")

    private val _controlsFlow: Flow<UiControls> = combine(
        _cardStyle,
        _reflectionText,
        _isReflectionEditing,
        _isCollectionSheetOpen
    ) { style, reflection, isEditing, isSheetOpen ->
        UiControls(style, reflection, isEditing, isSheetOpen)
    }

    val uiState: StateFlow<QuoteDetailUiState> = combine(
        repository.allQuotesFlow,
        _controlsFlow,
        repository.collectionsFlow,
        repository.getCollectionIdsForQuote(quoteId)
    ) { allQuotes: List<Quote>, controls: UiControls, collections: List<QuoteCollection>, activeColIds: List<Long> ->
        val currentQuote = allQuotes.firstOrNull { it.id == quoteId }
        val related = if (currentQuote != null) {
            allQuotes.filter {
                it.id != quoteId && (
                        it.category.equals(currentQuote.category, ignoreCase = true) ||
                                it.author.equals(currentQuote.author, ignoreCase = true)
                        )
            }.take(6)
        } else emptyList()

        QuoteDetailUiState(
            quote = currentQuote,
            cardStyle = controls.cardStyle,
            relatedQuotes = related,
            reflectionText = controls.reflectionText,
            isReflectionEditing = controls.isReflectionEditing,
            collections = collections,
            activeCollectionIds = activeColIds,
            isCollectionSheetOpen = controls.isCollectionSheetOpen
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = QuoteDetailUiState()
    )

    init {
        viewModelScope.launch {
            repository.recordHistory(quoteId)
            repository.getReflection(quoteId).collect { note ->
                _reflectionText.value = note ?: ""
            }
        }
    }

    fun onStyleChanged(style: CardStyle) {
        _cardStyle.value = style
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            repository.toggleFavorite(quoteId)
        }
    }

    fun onReflectionTextChanged(text: String) {
        _reflectionText.value = text
    }

    fun setReflectionEditing(isEditing: Boolean) {
        _isReflectionEditing.value = isEditing
    }

    fun saveReflection() {
        viewModelScope.launch {
            repository.saveReflection(quoteId, _reflectionText.value)
            _isReflectionEditing.value = false
        }
    }

    fun setCollectionSheetOpen(open: Boolean) {
        _isCollectionSheetOpen.value = open
    }

    fun toggleCollectionMembership(collectionId: Long) {
        viewModelScope.launch {
            val isMember = uiState.value.activeCollectionIds.contains(collectionId)
            if (isMember) {
                repository.removeQuoteFromCollection(collectionId, quoteId)
            } else {
                repository.addQuoteToCollection(collectionId, quoteId)
            }
        }
    }

    fun createAndAddToCollection(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val colId = repository.createCollection(name.trim())
            repository.addQuoteToCollection(colId, quoteId)
        }
    }

    class Factory(
        private val quoteId: String,
        private val repository: QuoteRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return QuoteDetailViewModel(quoteId, repository) as T
        }
    }
}
