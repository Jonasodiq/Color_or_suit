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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ActivityGame : AppCompatActivity() {

    private val viewModel: GameViewModel by viewModels()
    private lateinit var soundManager: SoundManager

    private lateinit var cardContainer: CardView
    private lateinit var cardFront: ImageView
    private lateinit var scoreView: TextView
    private lateinit var guessButtons: List<android.view.View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        soundManager = SoundManager(this)

        // Hämta vald svårighetsgrad från Inställningar
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        viewModel.setDifficultyFromPreference(prefs.getString("difficulty", "medium"))

        // Link layout elements
        scoreView     = findViewById(R.id.score)
        val redButton     = findViewById<Button>(R.id.redButton)
        val blackButton   = findViewById<Button>(R.id.blackButton)
        val heartButton   = findViewById<ImageView>(R.id.heartsIcon)
        val diamondButton = findViewById<ImageView>(R.id.diamondsIcon)
        val clubButton    = findViewById<ImageView>(R.id.clubsIcon)
        val spadeButton   = findViewById<ImageView>(R.id.spadesIcon)
        val backButton    = findViewById<Button>(R.id.backButton)

        cardContainer = findViewById(R.id.cardImageContainer)
        cardFront     = findViewById(R.id.cardFront)

        // Samla alla gissningsknappar i en lista
        guessButtons = listOf(redButton, blackButton, heartButton, diamondButton, clubButton, spadeButton)

        // Återställ poäng i UI vid skärmrotation
        scoreView.text = viewModel.score.toString()

        // functions button press
        redButton.setOnClickListener    { handleButtonClick("Red",      +1) }
        blackButton.setOnClickListener  { handleButtonClick("Black",    +1) }
        heartButton.setOnClickListener  { handleButtonClick("Hearts",   +5) }
        diamondButton.setOnClickListener{ handleButtonClick("Diamonds", +5) }
        clubButton.setOnClickListener   { handleButtonClick("Clubs",    +5) }
        spadeButton.setOnClickListener  { handleButtonClick("Spades",   +5) }

        backButton.setOnClickListener {
            soundManager.play(SoundManager.SoundType.BUTTON_CLICK)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }

    // Click methods
    private fun handleButtonClick(guess: String, scoreIncrement: Int) {
        setButtonsEnabled(false)
        flipToFront {
            val isCorrect = viewModel.isGuessCorrect(guess)
            if (isCorrect) {
                viewModel.updateScore(scoreIncrement)
                if (scoreIncrement == 5) {
                    soundManager.play(SoundManager.SoundType.CORRECT_SUIT)
                } else {
                    soundManager.play(SoundManager.SoundType.CORRECT)
                }
            } else {
                viewModel.updateScore(-1)
                soundManager.play(SoundManager.SoundType.WRONG)
            }
            scoreView.text = viewModel.score.toString()

            // Kontrollera vinst/förlust baserat på svårighetsgrad
            when {
                viewModel.isWin() -> {
                    soundManager.play(SoundManager.SoundType.WIN)
                    displayResult(true)
                }
                viewModel.isGameOver() -> {
                    soundManager.play(SoundManager.SoundType.LOSE)
                    displayResult(false)
                }
            }

            // Vänta en stund innan vi döljer kortet igen
            cardContainer.postDelayed({
                flipToBack { setButtonsEnabled(true) }
            }, 1000)
        }
    }

    // Aktivera/inaktivera alla gissningsknappar
    private fun setButtonsEnabled(enabled: Boolean) {
        guessButtons.forEach { it.isEnabled = enabled }
    }

    // Update card image from ViewModel state
    private fun updateCardImage() {
        viewModel.pickRandomCard()
        cardFront.setImageResource(viewModel.cards[viewModel.currentCardIndex].first)
    }

    // Display the result
    private fun displayResult(isWin: Boolean) {
        val message = if (isWin) {
            getString(R.string.win_message, viewModel.score)
        } else {
            getString(R.string.lose_message)
        }

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.game_over_title))
            .setMessage(message)
            .setPositiveButton(getString(R.string.play_again)) { _, _ -> resetGame() }
            .setNegativeButton(getString(R.string.exit))       { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    // Reset the game
    private fun resetGame() {
        viewModel.resetGame()
        scoreView.text = viewModel.score.toString()
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
                updateCardImage()
                soundManager.play(SoundManager.SoundType.CARD_FLIP)
                animeToBack.start()
            }
        })
        animeToBack.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                super.onAnimationEnd(animation)
                onFlipEnd()
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
