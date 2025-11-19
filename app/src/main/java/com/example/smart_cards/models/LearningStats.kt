import java.util.Date

data class LearningStats(
    val date: Date,
    val wordsLearned: Int,
    val correctAnswers: Int,
    val totalAttempts: Int,
    val sessionDuration: Int // в минутах
)