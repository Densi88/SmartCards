package com.example.smart_cards.viewModels

import androidx.lifecycle.ViewModel
import com.example.smart_cards.repository.LevelRepository
import androidx.lifecycle.viewModelScope
import com.example.smart_cards.models.Card
import com.example.smart_cards.models.GameQuestion
import com.example.smart_cards.models.GameSession
import com.example.smart_cards.repository.CardsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random


class LevelViewModel(
    private val repository: LevelRepository,
): ViewModel() {

    sealed class GameUiState {
        object Loading : GameUiState()
        data class LevelSelection(val levels: List<Int>) : GameUiState()
        data class Question(
            val question: GameQuestion,
            val progress: Int,  // текущий вопрос / всего
            val score: Int,      // правильные ответы
            val level: Int
        ) : GameUiState()
        data class GameComplete(
            val score: Int,
            val total: Int,
            val level: Int
        ) : GameUiState()
        data class Error(val message: String) : GameUiState()
    }

    private val _uiState = MutableStateFlow<GameUiState>(GameUiState.Loading)
    val uiState: StateFlow<GameUiState> = _uiState

    private var currentGameSession: GameSession? = null
    private var currentQuestions: List<GameQuestion> = emptyList()

    init {
        loadLevels()

    }

    private fun loadLevels() {
        viewModelScope.launch {
            try {
                // Запускаем в отдельном потоке
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val levels = repository.getAvailableLevels()
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        if (levels.isEmpty()) {
                            _uiState.value = GameUiState.Error("Нет доступных уровней")
                        } else {
                            _uiState.value = GameUiState.LevelSelection(levels)
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = GameUiState.Error(e.message ?: "Ошибка загрузки")
            }
        }
    }

    fun startLevel(levelNumber: Int) {
        viewModelScope.launch {
            _uiState.value = GameUiState.Loading

            try {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val cards = repository.getCardsForLevel(levelNumber)

                    if (cards.isEmpty()) {
                        throw Exception("На этом уровне нет карточек")
                    }

                    // Создаем игровую сессию
                    val session = GameSession(
                        levelNumber = levelNumber,
                        cards = cards,
                        totalQuestions = cards.size
                    )

                    // Создаем вопросы с вариантами ответов
                    val questions = generateQuestions(cards)

                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        currentGameSession = session
                        currentQuestions = questions

                        showNextQuestion()
                    }
                }
            } catch (e: Exception) {
                _uiState.value = GameUiState.Error(e.message ?: "Ошибка старта уровня")
            }
        }
    }

    private fun generateQuestions(cards: List<Card>): List<GameQuestion> {
        val questions = mutableListOf<GameQuestion>()

        cards.forEach { card ->
            // Варианты ответов: правильный + 3 случайных неправильных
            val otherTranslations = cards
                .filter { it.word != card.word }
                .map { it.translate }
                .distinct()
                .shuffled()
                .take(3)

            val options = (otherTranslations + card.translate).shuffled()

            questions.add(
                GameQuestion(
                    card = card,
                    options = options,
                    correctAnswer = card.translate
                )
            )
        }

        return questions.shuffled() // Перемешиваем вопросы
    }

    private fun showNextQuestion() {
        val session = currentGameSession ?: return

        if (session.currentCardIndex >= currentQuestions.size) {
            // Игра закончена
            _uiState.value = GameUiState.GameComplete(
                score = session.correctAnswers,
                total = session.totalQuestions,
                level = session.levelNumber
            )
            return
        }

        val currentQuestion = currentQuestions[session.currentCardIndex]

        _uiState.value = GameUiState.Question(
            question = currentQuestion,
            progress = session.currentCardIndex + 1,
            score = session.correctAnswers,
            level = session.levelNumber
        )
    }

    fun answerQuestion(selectedAnswer: String) {
        val session = currentGameSession ?: return
        val currentQuestion = currentQuestions.getOrNull(session.currentCardIndex) ?: return

        val isCorrect = selectedAnswer == currentQuestion.correctAnswer

        val newSession = session.copy(
            currentCardIndex = session.currentCardIndex + 1,
            correctAnswers = if (isCorrect) session.correctAnswers + 1 else session.correctAnswers
        )

        currentGameSession = newSession

        showNextQuestion()
    }

    fun restartGame() {
        currentGameSession = null
        currentQuestions = emptyList()
        loadLevels()
    }

}