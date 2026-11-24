package com.example.color_or_suit

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import kotlin.random.Random

class GameViewModel : ViewModel() {

    enum class Difficulty(val targetScore: Int, val minScore: Int, val maxScore: Int) {
        EASY(targetScore = 15, minScore = -3, maxScore = 18),
        MEDIUM(targetScore = 21, minScore = 0, maxScore = 21),
        HARD(targetScore = 31, minScore = 0, maxScore = 31)
    }

    var difficulty: Difficulty = Difficulty.MEDIUM

    val cards = listOf(
        Pair(R.drawable.clubs_10, "Clubs"),
        Pair(R.drawable.clubs_j, "Clubs"),
        Pair(R.drawable.clubs_k, "Clubs"),
        Pair(R.drawable.clubs_q, "Clubs"),
        Pair(R.drawable.diamonds_10, "Diamonds"),
        Pair(R.drawable.diamonds_j, "Diamonds"),
        Pair(R.drawable.diamonds_k, "Diamonds"),
        Pair(R.drawable.diamonds_q, "Diamonds"),
        Pair(R.drawable.heart_10, "Hearts"),
        Pair(R.drawable.heart_j, "Hearts"),
        Pair(R.drawable.heart_k, "Hearts"),
        Pair(R.drawable.heart_q, "Hearts"),
        Pair(R.drawable.spades_10, "Spades"),
        Pair(R.drawable.spades_j, "Spades"),
        Pair(R.drawable.spades_k, "Spades"),
        Pair(R.drawable.spades_q, "Spades"),
    )

    var score = 0
    var currentCardIndex = 0

    // High score per difficulty
    private var highScores: MutableMap<Difficulty, Int> = mutableMapOf(
        Difficulty.EASY to 0,
        Difficulty.MEDIUM to 0,
        Difficulty.HARD to 0
    )

    fun setDifficultyFromPreference(pref: String?) {
        difficulty = when (pref?.lowercase()) {
            "easy" -> Difficulty.EASY
            "hard" -> Difficulty.HARD
            else   -> Difficulty.MEDIUM
        }
    }

    fun pickRandomCard() {
        currentCardIndex = Random.nextInt(cards.size)
    }

    fun isGuessCorrect(guess: String): Boolean {
        val cardSuit = cards[currentCardIndex].second
        return when (guess) {
            "Red"   -> cardSuit == "Hearts" || cardSuit == "Diamonds"
            "Black" -> cardSuit == "Clubs"  || cardSuit == "Spades"
            else    -> cardSuit == guess
        }
    }

    fun updateScore(value: Int) {
        score += value
        // Update high score if current score is higher
        val currentHigh = highScores[difficulty] ?: 0
        if (score > currentHigh) {
            highScores[difficulty] = score
        }
    }

    fun isWin(): Boolean {
        return when (difficulty) {
            Difficulty.EASY -> score in 15..18
            Difficulty.MEDIUM, Difficulty.HARD -> score == difficulty.targetScore
        }
    }

    fun isGameOver(): Boolean = score > difficulty.maxScore || score < difficulty.minScore

    fun resetGame() {
        score = 0
        pickRandomCard()
    }

    fun getHighScore(difficulty: Difficulty = this.difficulty): Int {
        return highScores[difficulty] ?: 0
    }

    fun loadHighScores(context: Context) {
        val prefs = context.getSharedPreferences("high_scores", Context.MODE_PRIVATE)
        highScores[Difficulty.EASY] = prefs.getInt("high_score_easy", 0)
        highScores[Difficulty.MEDIUM] = prefs.getInt("high_score_medium", 0)
        highScores[Difficulty.HARD] = prefs.getInt("high_score_hard", 0)
    }

    fun saveHighScores(context: Context) {
        val prefs = context.getSharedPreferences("high_scores", Context.MODE_PRIVATE).edit()
        highScores.forEach { (difficulty, score) ->
            val key = when (difficulty) {
                Difficulty.EASY -> "high_score_easy"
                Difficulty.MEDIUM -> "high_score_medium"
                Difficulty.HARD -> "high_score_hard"
            }
            prefs.putInt(key, score)
        }
        prefs.apply()
    }
}
