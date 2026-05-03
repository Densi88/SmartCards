package com.example.smart_cards.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smart_cards.models.Card
import com.example.smart_cards.repository.CardsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CardsViewModel(
    private val repository: CardsRepository
) : ViewModel() {

    // Состояние загрузки
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Список карточек
    private val _cards = MutableStateFlow<List<Card>>(emptyList())
    val cards: StateFlow<List<Card>> = _cards.asStateFlow()

    // События (для Toast и т.д.)
    private val _event = MutableStateFlow<CardEvent?>(null)
    val event: StateFlow<CardEvent?> = _event.asStateFlow()

    sealed class CardEvent {
        data class ShowMessage(val message: String) : CardEvent()
        data class ShowError(val message: String) : CardEvent()
    }

    init {
        loadCards()
    }

    // Загрузить все карточки
    fun loadCards() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val cardsList = withContext(Dispatchers.IO) {
                    repository.readCards()
                }
                _cards.value = cardsList
            } catch (e: Exception) {
                _event.value = CardEvent.ShowError(e.message ?: "Ошибка загрузки карточек")
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Добавить карточку
    fun addCard(word: String, translation: String): Boolean {
        // Валидация
        if (word.isBlank() || translation.isBlank()) {
            _event.value = CardEvent.ShowError("Заполните все поля")
            return false
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.addCard(word.trim(), translation.trim())
                }
                _event.value = CardEvent.ShowMessage("Карточка добавлена")
                loadCards() // Обновляем список
            } catch (e: Exception) {
                _event.value = CardEvent.ShowError(e.message ?: "Ошибка добавления")
            }
        }
        return true
    }

    // Обновить карточку
    fun updateCard(oldWord: String, newWord: String, newTranslation: String): Boolean {
        if (newWord.isBlank() || newTranslation.isBlank()) {
            _event.value = CardEvent.ShowError("Заполните все поля")
            return false
        }

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.updateCard(newWord.trim(), newTranslation.trim(), oldWord)
                }
                _event.value = CardEvent.ShowMessage("Карточка обновлена")
                loadCards()
            } catch (e: Exception) {
                _event.value = CardEvent.ShowError(e.message ?: "Ошибка обновления")
            }
        }
        return true
    }

    // Удалить карточку
    fun deleteCard(word: String) {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    repository.deleteCard(word)
                }
                _event.value = CardEvent.ShowMessage("Карточка удалена")
                loadCards()
            } catch (e: Exception) {
                _event.value = CardEvent.ShowError(e.message ?: "Ошибка удаления")
            }
        }
    }

    // Получить карточку по слову
    fun getCardByWord(word: String): Card? {
        return _cards.value.find { it.word == word }
    }

    // Сбросить событие
    fun consumeEvent() {
        _event.value = null
    }
}