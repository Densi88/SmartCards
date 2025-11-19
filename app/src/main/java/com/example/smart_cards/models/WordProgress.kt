import java.util.Date

data class WordProgress(
    val word: String,
    val translation: String,
    val correctCount: Int,
    val wrongCount: Int,
    val lastPracticed: Date,
    val difficulty: Int // 1-5
)