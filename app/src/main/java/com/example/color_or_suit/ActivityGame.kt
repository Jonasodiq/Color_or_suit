package com.example.color_or_suit

import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.random.Random

class ActivityGame : AppCompatActivity() {

    private val cards = listOf(
        Pair(R.drawable.clubs_10, "Clover"),
        Pair(R.drawable.clubs_j, "Clover"),
        Pair(R.drawable.clubs_k, "Clover"),
        Pair(R.drawable.clubs_q, "Clover"),
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

    private lateinit var cardContainer: CardView
    private lateinit var cardFront: ImageView

    var score = 0
    var currentCardIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Link layout elements
        val scoreView = findViewById<TextView>(R.id.score)
        val redButton = findViewById<Button>(R.id.redButton)
        val blackButton = findViewById<Button>(R.id.blackButton)
        val heartButton = findViewById<ImageView>(R.id.heartsIcon)
        val diamondButton = findViewById<ImageView>(R.id.diamondsIcon)
        val clubButton = findViewById<ImageView>(R.id.clubsIcon)
        val spadeButton = findViewById<ImageView>(R.id.spadesIcon)
        val backButton = findViewById<Button>(R.id.backButton)

        cardContainer = findViewById(R.id.cardImageContainer)
        cardFront = findViewById(R.id.cardFront)

        // functions button press
        redButton.setOnClickListener {
            handleButtonClick("Red", +1, scoreView)
        }
        blackButton.setOnClickListener {
            handleButtonClick("Black", +1, scoreView)
        }
        heartButton.setOnClickListener {
            handleButtonClick("Hearts", +5, scoreView)
        }
        diamondButton.setOnClickListener {
            handleButtonClick("Diamonds", +5, scoreView)
        }
        clubButton.setOnClickListener {
            handleButtonClick("Clover", +5, scoreView)
        }
        spadeButton.setOnClickListener {
            handleButtonClick("Spades", +5, scoreView)
        }

        backButton.setOnClickListener { finish() }
    }

    // Click methods
    private fun handleButtonClick(guess: String, scoreIncrement: Int, scoreTextView: TextView) {
        if (isGuessCorrect(guess)) {
            updateScore(scoreIncrement, scoreTextView)
        } else {
            updateScore(-1, scoreTextView)
        }
        updateCardImage()
    }

    // Update card
    private fun updateCardImage() {
        currentCardIndex = Random.nextInt(cards.size)
        val newCard = cards[currentCardIndex]
        cardFront.setImageResource(newCard.first) // Set the image for the new card
    }

    // Check if the guess is correct
    private fun isGuessCorrect(guess: String): Boolean {
        val cardSuit = cards[currentCardIndex].second
        return when (guess) {
              "Red" -> cardSuit == "Hearts" || cardSuit == "Diamonds"
            "Black" -> cardSuit == "Clover" || cardSuit == "Spades"
               else -> cardSuit ==  guess
        }
    }

    // Update score
    private fun updateScore(value: Int, scoreView: TextView) {
        score += value
        scoreView.text = "$score"

        // Check if wins or loses
        when {
            score == 21 -> endGame(true) // wins exactly 21
            score  > 21 || score < 0 -> endGame(false)  // otherwise lose
        }
    }

    // End the game
    private fun endGame(isWin: Boolean) {
        finish()
        if (!isWin) {
            finish()
        }


    }

}
