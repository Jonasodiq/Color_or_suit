package com.example.color_or_suit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import com.example.color_or_suit.ui.theme.CardBlack
import com.example.color_or_suit.ui.theme.CardRed
import com.example.color_or_suit.ui.theme.ColorOrSuitTheme
import kotlinx.coroutines.delay

class ActivityGame : ComponentActivity() {

    private val viewModel: GameViewModel by viewModels()
    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        soundManager = SoundManager(this)

        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        viewModel.setDifficultyFromPreference(prefs.getString("difficulty", "medium"))

        setContent {
            val themePref = remember { prefs.getString("theme", "light") }
            val darkTheme = when (themePref) {
                "dark"  -> true
                "light" -> false
                else    -> isSystemInDarkTheme()
            }

            ColorOrSuitTheme(darkTheme = darkTheme) {
                GameScreen(
                    viewModel = viewModel,
                    soundManager = soundManager,
                    onBackClick = {
                        soundManager.play(SoundManager.SoundType.BUTTON_CLICK)
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager.release()
    }
}

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    soundManager: SoundManager,
    onBackClick: () -> Unit
) {
    var isFlipped by remember { mutableStateOf(false) }
    var buttonsEnabled by remember { mutableStateOf(true) }
    var showGameOverDialog by remember { mutableStateOf(false) }
    var isWin by remember { mutableStateOf(false) }

    // 3D-kortvändning via rotationY
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "CardFlipAnimation"
    )

    // Hantera gissningar
    fun handleGuess(guess: String, increment: Int) {
        if (!buttonsEnabled) return
        buttonsEnabled = false

        viewModel.pickRandomCard()
        soundManager.play(SoundManager.SoundType.CARD_FLIP)
        isFlipped = true

        val correct = viewModel.isGuessCorrect(guess)
        if (correct) {
            viewModel.updateScore(increment)
            if (increment == 5) {
                soundManager.play(SoundManager.SoundType.CORRECT_SUIT)
            } else {
                soundManager.play(SoundManager.SoundType.CORRECT)
            }
        } else {
            viewModel.updateScore(-1)
            soundManager.play(SoundManager.SoundType.WRONG)
        }

        // Kontrollera vinst/förlust
        if (viewModel.isWin()) {
            isWin = true
            soundManager.play(SoundManager.SoundType.WIN)
            showGameOverDialog = true
        } else if (viewModel.isGameOver()) {
            isWin = false
            soundManager.play(SoundManager.SoundType.LOSE)
            showGameOverDialog = true
        }

        // Vänta 1 sekund och vänd sedan tillbaka kortet
        kotlinx.coroutines.MainScope().run {
            // Använd LaunchedEffect-mönster via delay
        }
    }

    // Timer för att vända tillbaka kortet
    LaunchedEffect(isFlipped) {
        if (isFlipped) {
            delay(1200)
            isFlipped = false
            delay(350) // Vänta tills vändningen är klar
            buttonsEnabled = true
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Rubrik
            Text(
                text = stringResource(R.string.colorOrSuit),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )

            // Kortkomponent med äkta 3D rotation
            Card(
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .size(width = 140.dp, height = 210.dp)
                    .graphicsLayer {
                        rotationY = rotation
                        cameraDistance = 12f * density
                    }
            ) {
                if (rotation <= 90f) {
                    // Kortsidan: Baksida
                    Image(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "Card Back",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Kortsidan: Framsida (spegelvänd så att kortet inte visas bakochfram)
                    val cardRes = viewModel.cards[viewModel.currentCardIndex].first
                    Image(
                        painter = painterResource(id = cardRes),
                        contentDescription = "Card Front",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                    )
                }
            }

            // Poängvisning
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.text_points) + ": ",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${viewModel.score}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Färg-knappar (Röd / Svart)
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { handleGuess("Red", 1) },
                    enabled = buttonsEnabled,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CardRed),
                    modifier = Modifier.size(width = 110.dp, height = 50.dp)
                ) {
                    Text(
                        text = stringResource(R.string.red),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Button(
                    onClick = { handleGuess("Black", 1) },
                    enabled = buttonsEnabled,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CardBlack),
                    modifier = Modifier.size(width = 110.dp, height = 50.dp)
                ) {
                    Text(
                        text = stringResource(R.string.black),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Svit-ikoner (+5 poäng)
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuitButton(
                    iconRes = R.drawable.ic_heart,
                    contentDesc = "Hearts",
                    enabled = buttonsEnabled,
                    onClick = { handleGuess("Hearts", 5) }
                )
                SuitButton(
                    iconRes = R.drawable.ic_diamonds,
                    contentDesc = "Diamonds",
                    enabled = buttonsEnabled,
                    onClick = { handleGuess("Diamonds", 5) }
                )
                SuitButton(
                    iconRes = R.drawable.ic_clubs,
                    contentDesc = "Clubs",
                    enabled = buttonsEnabled,
                    onClick = { handleGuess("Clubs", 5) }
                )
                SuitButton(
                    iconRes = R.drawable.ic_spades,
                    contentDesc = "Spades",
                    enabled = buttonsEnabled,
                    onClick = { handleGuess("Spades", 5) }
                )
            }

            // Tillbaka-knapp
            Button(
                onClick = onBackClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .width(130.dp)
                    .height(48.dp)
                    .padding(bottom = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.back),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }

        // Vinst / Förlust Dialog
        if (showGameOverDialog) {
            AlertDialog(
                onDismissRequest = { /* Lås så man måste välja knapp */ },
                title = {
                    Text(
                        text = stringResource(R.string.game_over_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    val message = if (isWin) {
                        stringResource(R.string.win_message, viewModel.score)
                    } else {
                        stringResource(R.string.lose_message)
                    }
                    Text(text = message, style = MaterialTheme.typography.bodyLarge)
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.resetGame()
                            isFlipped = false
                            buttonsEnabled = true
                            showGameOverDialog = false
                        }
                    ) {
                        Text(stringResource(R.string.play_again))
                    }
                },
                dismissButton = {
                    TextButton(onClick = onBackClick) {
                        Text(stringResource(R.string.exit))
                    }
                }
            )
        }
    }
}

@Composable
fun SuitButton(
    iconRes: Int,
    contentDesc: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
        color = Color.White,
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = contentDesc,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
