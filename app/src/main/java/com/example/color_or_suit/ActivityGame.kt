package com.example.color_or_suit

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.os.Bundle
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
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
        flipToFront {
            if (isGuessCorrect(guess)) {
                updateScore(scoreIncrement, scoreTextView)
            } else {
                updateScore(-1, scoreTextView)
            }

            // Vänta en stund innan vi döljer kortet igen
            cardContainer.postDelayed({
                flipToBack()
            }, 1000)
        }
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
            score == 21 -> displayResult(true) // wins exactly 21
            score  > 21 || score < 0 -> displayResult(false)
        }
    }

    // Display the result
    private fun displayResult(isWin: Boolean) {
        val message = if (isWin) {
            getString(R.string.win_message)
        } else {
            getString(R.string.lose_message)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.game_over_title))
            .setMessage(message)
            .setPositiveButton(getString(R.string.play_again)) { _, _ ->
                resetGame()
            }
            .setNegativeButton(getString(R.string.exit)) { _, _ ->
                finish()
            }
            // Prevent the dialog from closing by tapping outside it
            .setCancelable(false)
            .show()
    }

    // Reset the game
    private fun resetGame() {
        score = 0
        findViewById<TextView>(R.id.score).text = "$score" // Reset the score
        updateCardImage() // update a new card
    }



    // Animation to front
    private fun flipToFront(onFlipEnd: () -> Unit) {
        val animeToFront = ObjectAnimator.ofFloat(cardContainer, "scaleX", 1f, 0f).apply {
            interpolator = DecelerateInterpolator()
            duration = 300
        }

        val animeToBack = ObjectAnimator.ofFloat(cardContainer, "scaleX", 0f, 1f).apply {
            interpolator = AccelerateInterpolator()
            duration = 300
        }

        animeToFront.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                updateCardImage() // Update the card image
                animeToBack.start() // Start the next part
            }
        })

        animeToBack.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                onFlipEnd() // Run further after the front page is displayed
            }
        })
        animeToFront.start()
    }

    // Animation to back
    private fun flipToBack(onFlipEnd: (() -> Unit)? = null) {
        val animeToFront = ObjectAnimator.ofFloat(cardContainer, "scaleX", 1f, 0f).apply {
            interpolator = DecelerateInterpolator()
            duration = 300
        }

        val animeToBack = ObjectAnimator.ofFloat(cardContainer, "scaleX", 0f, 1f).apply {
            interpolator = AccelerateInterpolator()
            duration = 300
        }

        animeToFront.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                cardFront.setImageResource(R.drawable.back)
                animeToBack.start()
            }
        })

        animeToBack.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                onFlipEnd?.invoke()
            }
        })
        animeToFront.start()
    }
}
