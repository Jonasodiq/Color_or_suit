package com.example.color_or_suit

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}