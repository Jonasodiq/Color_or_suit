package com.example.color_or_suit

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GameViewModelTest {

    private lateinit var viewModel: GameViewModel

    @Before
    fun setUp() {
        viewModel = GameViewModel()
    }

    @Test
    fun testPickRandomCard_returnsValidIndex() {
        viewModel.pickRandomCard()
        assertTrue("Index should be within card list bounds", viewModel.currentCardIndex in 0..viewModel.cards.lastIndex)
    }

    @Test
    fun testIsGuessCorrect_redWithHeartsOrDiamonds() {
        // Test Hearts (Red)
        viewModel.currentCardIndex = viewModel.cards.indexOfFirst { it.second == "Hearts" }
        assertTrue("Red guess should be correct for Hearts", viewModel.isGuessCorrect("Red"))

        // Test Diamonds (Red)
        viewModel.currentCardIndex = viewModel.cards.indexOfFirst { it.second == "Diamonds" }
        assertTrue("Red guess should be correct for Diamonds", viewModel.isGuessCorrect("Red"))

        // Test Clubs (Black) - should be false for Red
        viewModel.currentCardIndex = viewModel.cards.indexOfFirst { it.second == "Clubs" }
        assertFalse("Red guess should be false for Clubs", viewModel.isGuessCorrect("Red"))

        // Test Spades (Black) - should be false for Red
        viewModel.currentCardIndex = viewModel.cards.indexOfFirst { it.second == "Spades" }
        assertFalse("Red guess should be false for Spades", viewModel.isGuessCorrect("Red"))
    }

    @Test
    fun testIsGuessCorrect_blackWithClubsOrSpades() {
        // Test Clubs (Black)
        viewModel.currentCardIndex = viewModel.cards.indexOfFirst { it.second == "Clubs" }
        assertTrue("Black guess should be correct for Clubs", viewModel.isGuessCorrect("Black"))

        // Test Spades (Black)
        viewModel.currentCardIndex = viewModel.cards.indexOfFirst { it.second == "Spades" }
        assertTrue("Black guess should be correct for Spades", viewModel.isGuessCorrect("Black"))

        // Test Hearts (Red) - should be false for Black
        viewModel.currentCardIndex = viewModel.cards.indexOfFirst { it.second == "Hearts" }
        assertFalse("Black guess should be false for Hearts", viewModel.isGuessCorrect("Black"))

        // Test Diamonds (Red) - should be false for Black
        viewModel.currentCardIndex = viewModel.cards.indexOfFirst { it.second == "Diamonds" }
        assertFalse("Black guess should be false for Diamonds", viewModel.isGuessCorrect("Black"))
    }

    @Test
    fun testIsGuessCorrect_suitMatch() {
        // Test each suit
        val suits = listOf("Hearts", "Diamonds", "Clubs", "Spades")
        for (suit in suits) {
            viewModel.currentCardIndex = viewModel.cards.indexOfFirst { it.second == suit }
            assertTrue("Guessing $suit should be correct for $suit", viewModel.isGuessCorrect(suit))

            // Wrong suit should be false
            val wrongSuit = suits.first { it != suit }
            assertFalse("Guessing $wrongSuit should be false for $suit", viewModel.isGuessCorrect(wrongSuit))
        }
    }

    @Test
    fun testUpdateScore_positiveAndNegative() {
        assertEquals(0, viewModel.score)

        viewModel.updateScore(1)
        assertEquals(1, viewModel.score)

        viewModel.updateScore(5)
        assertEquals(6, viewModel.score)

        viewModel.updateScore(-1)
        assertEquals(5, viewModel.score)

        viewModel.updateScore(-10)
        assertEquals(-5, viewModel.score)
    }

    @Test
    fun testIsWin_easyDifficulty() {
        viewModel.setDifficultyFromPreference("easy")

        // Score 15-18 should win
        for (score in 15..18) {
            viewModel.score = score
            assertTrue("Easy: score $score should be win", viewModel.isWin())
        }

        // Score 14 should not win
        viewModel.score = 14
        assertFalse("Easy: score 14 should not win", viewModel.isWin())

        // Score 19 should not win (game over)
        viewModel.score = 19
        assertFalse("Easy: score 19 should not win", viewModel.isWin())
    }

    @Test
    fun testIsWin_mediumDifficulty() {
        viewModel.setDifficultyFromPreference("medium")

        // Only exactly 21 wins
        viewModel.score = 21
        assertTrue("Medium: score 21 should win", viewModel.isWin())

        viewModel.score = 20
        assertFalse("Medium: score 20 should not win", viewModel.isWin())

        viewModel.score = 22
        assertFalse("Medium: score 22 should not win", viewModel.isWin())
    }

    @Test
    fun testIsWin_hardDifficulty() {
        viewModel.setDifficultyFromPreference("hard")

        // Only exactly 31 wins
        viewModel.score = 31
        assertTrue("Hard: score 31 should win", viewModel.isWin())

        viewModel.score = 30
        assertFalse("Hard: score 30 should not win", viewModel.isWin())

        viewModel.score = 32
        assertFalse("Hard: score 32 should not win", viewModel.isWin())
    }

    @Test
    fun testIsGameOver_easyDifficulty() {
        viewModel.setDifficultyFromPreference("easy")

        // Below -3 is game over
        viewModel.score = -4
        assertTrue("Easy: score -4 should be game over", viewModel.isGameOver())

        viewModel.score = -3
        assertFalse("Easy: score -3 should not be game over", viewModel.isGameOver())

        // Above 18 is game over
        viewModel.score = 19
        assertTrue("Easy: score 19 should be game over", viewModel.isGameOver())

        viewModel.score = 18
        assertFalse("Easy: score 18 should not be game over", viewModel.isGameOver())
    }

    @Test
    fun testIsGameOver_mediumDifficulty() {
        viewModel.setDifficultyFromPreference("medium")

        // Below 0 is game over
        viewModel.score = -1
        assertTrue("Medium: score -1 should be game over", viewModel.isGameOver())

        viewModel.score = 0
        assertFalse("Medium: score 0 should not be game over", viewModel.isGameOver())

        // Above 21 is game over
        viewModel.score = 22
        assertTrue("Medium: score 22 should be game over", viewModel.isGameOver())

        viewModel.score = 21
        assertFalse("Medium: score 21 should not be game over", viewModel.isGameOver())
    }

    @Test
    fun testIsGameOver_hardDifficulty() {
        viewModel.setDifficultyFromPreference("hard")

        // Below 0 is game over
        viewModel.score = -1
        assertTrue("Hard: score -1 should be game over", viewModel.isGameOver())

        viewModel.score = 0
        assertFalse("Hard: score 0 should not be game over", viewModel.isGameOver())

        // Above 31 is game over
        viewModel.score = 32
        assertTrue("Hard: score 32 should be game over", viewModel.isGameOver())

        viewModel.score = 31
        assertFalse("Hard: score 31 should not be game over", viewModel.isGameOver())
    }

    @Test
    fun testResetGame_resetsScoreAndPicksNewCard() {
        viewModel.score = 10
        val oldIndex = viewModel.currentCardIndex

        viewModel.resetGame()

        assertEquals(0, viewModel.score)
        // Note: Could be same index by chance, but very unlikely with 16 cards
        // We mainly test that score is reset and a valid index is picked
        assertTrue("Index should be valid after reset", viewModel.currentCardIndex in 0..viewModel.cards.lastIndex)
    }

    @Test
    fun testSetDifficultyFromPreference_allCases() {
        viewModel.setDifficultyFromPreference("easy")
        assertEquals(GameViewModel.Difficulty.EASY, viewModel.difficulty)

        viewModel.setDifficultyFromPreference("medium")
        assertEquals(GameViewModel.Difficulty.MEDIUM, viewModel.difficulty)

        viewModel.setDifficultyFromPreference("hard")
        assertEquals(GameViewModel.Difficulty.HARD, viewModel.difficulty)

        // Invalid/unknown defaults to medium
        viewModel.setDifficultyFromPreference("unknown")
        assertEquals(GameViewModel.Difficulty.MEDIUM, viewModel.difficulty)

        viewModel.setDifficultyFromPreference(null)
        assertEquals(GameViewModel.Difficulty.MEDIUM, viewModel.difficulty)

        viewModel.setDifficultyFromPreference("")
        assertEquals(GameViewModel.Difficulty.MEDIUM, viewModel.difficulty)

        // Case insensitive
        viewModel.setDifficultyFromPreference("EASY")
        assertEquals(GameViewModel.Difficulty.EASY, viewModel.difficulty)

        viewModel.setDifficultyFromPreference("Hard")
        assertEquals(GameViewModel.Difficulty.HARD, viewModel.difficulty)
    }
}