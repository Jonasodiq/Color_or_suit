package com.example.color_or_suit

import android.content.SharedPreferences
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import com.example.color_or_suit.ui.theme.CardBlack
import com.example.color_or_suit.ui.theme.CardRed
import com.example.color_or_suit.ui.theme.ColorOrSuitTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ActivityGame : ComponentActivity(), SharedPreferences.OnSharedPreferenceChangeListener {

    private val viewModel: GameViewModel by viewModels()
    private lateinit var soundManager: SoundManager
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        soundManager = SoundManager(this)

        prefs = PreferenceManager.getDefaultSharedPreferences(this)
        viewModel.setDifficultyFromPreference(prefs.getString("difficulty", "medium"))
        
        // Load high scores
        viewModel.loadHighScores(this)

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
                        soundManager.play(SoundManager.SoundType.BUTTON_CLICK, volume = 0.35f)
                        finish()
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        prefs.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onPause() {
        super.onPause()
        prefs.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == "difficulty") {
            val newDifficulty = sharedPreferences?.getString("difficulty", "medium")
            viewModel.setDifficultyFromPreference(newDifficulty)
        }
        // SoundManager kollar isSoundEnabled() vid varje play(), så inget extra behövs här
        // Theme hanteras av ThemeManager.applyTheme() i SettingsActivity
    }

    override fun onDestroy() {
        super.onDestroy()
        // Save high scores
        viewModel.saveHighScores(this)
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
    var showGameOverDialog by remember { mutableStateOf(false) }
    var isWin by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val view = LocalView.current
    
    // Floating score animation state
    data class ScoreAnimation(val text: String, val color: Color)
    var pendingScoreAnimation by remember { mutableStateOf<ScoreAnimation?>(null) }

    // Capture theme colors at composable level for use in non-composable functions
    val correctColorPrimary = MaterialTheme.colorScheme.primary
    val correctColorGreen = Color.Green
    val errorColor = MaterialTheme.colorScheme.error

    // Knappar inaktiva under flip-animation (anti-spam)
    val buttonsEnabled = !isFlipped

    // 3D-kortvändning via rotationY
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "CardFlipAnimation"
    )

    // Accessibility: Announce card flip state changes
    val cardContentDescription = remember { mutableStateOf("Card back") }
    
    // Pre-compute string resources for semantics to avoid calling composable functions inside semantics
    val pointsLabel = stringResource(R.string.text_points)
    val highScoreFormat = stringResource(R.string.high_score)
    val scoreContentDesc = remember { mutableStateOf("$pointsLabel: ${viewModel.score}") }
    val highScoreContentDesc = remember { mutableStateOf(highScoreFormat.replace("%1\$d", viewModel.getHighScore().toString())) }
    
    // Update content descriptions when score changes
    LaunchedEffect(viewModel.score) {
        scoreContentDesc.value = "$pointsLabel: ${viewModel.score}"
    }
    LaunchedEffect(viewModel.getHighScore()) {
        highScoreContentDesc.value = highScoreFormat.replace("%1\$d", viewModel.getHighScore().toString())
    }
    
    LaunchedEffect(rotation) {
        if (rotation > 90f && rotation <= 180f) {
            // Card is face up - announce the card
            val card = viewModel.cards[viewModel.currentCardIndex]
            val suit = when (card.second) {
                "Hearts" -> "Hearts"
                "Diamonds" -> "Diamonds"
                "Clubs" -> "Clubs"
                "Spades" -> "Spades"
                else -> card.second
            }
            val color = when (suit) {
                "Hearts", "Diamonds" -> "Red"
                "Clubs", "Spades" -> "Black"
                else -> "Unknown"
            }
            cardContentDescription.value = "$suit, $color"
        } else {
            cardContentDescription.value = "Card back"
        }
    }

    fun performHaptic() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    fun handleGuess(guess: String, increment: Int) {
        performHaptic()
        viewModel.pickRandomCard()
        soundManager.play(SoundManager.SoundType.CARD_FLIP)
        isFlipped = true

        val correct = viewModel.isGuessCorrect(guess)
        if (correct) {
            viewModel.updateScore(increment)
        } else {
            viewModel.updateScore(-1)
        }

        // Kontrollera vinst/förlust först – spela BARA ETT ljud!
        if (viewModel.isWin()) {
            isWin = true
            soundManager.play(SoundManager.SoundType.WIN)
            showGameOverDialog = true
        } else if (viewModel.isGameOver()) {
            isWin = false
            soundManager.play(SoundManager.SoundType.LOSE)
            showGameOverDialog = true
        } else {
            // Spelet fortsätter: spela rätt- eller fel-ljud
            if (correct) {
                if (increment == 5) {
                    soundManager.play(SoundManager.SoundType.CORRECT_SUIT)
                } else {
                    soundManager.play(SoundManager.SoundType.CORRECT)
                }
            } else {
                soundManager.play(SoundManager.SoundType.WRONG)
            }
        }

        // Trigger floating score animation from composable context
        // We set the pending animation which will be consumed by the UI
        val animationText = if (correct) "+$increment" else "-1"
        val animationColor = if (correct) {
            if (increment == 5) correctColorGreen else correctColorPrimary
        } else {
            errorColor
        }
        pendingScoreAnimation = ScoreAnimation(animationText, animationColor)

        // Vänd tillbaka kortet efter 1 sekund
        scope.launch {
            delay(1000)
            isFlipped = false
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
                    .semantics {
                        // Accessibility: Treat card as an image with dynamic content description
                        role = Role.Image
                        stateDescription = if (isFlipped) "Card face up" else "Card face down"
                        contentDescription = cardContentDescription.value
                    }
            ) {
                if (rotation <= 90f) {
                    // Kortsidan: Baksida
                    Image(
                        painter = painterResource(id = R.drawable.back),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Kortsidan: Framsida
                    val cardRes = viewModel.cards[viewModel.currentCardIndex].first
                    Image(
                        painter = painterResource(id = cardRes),
                        contentDescription = "",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { rotationY = 180f }
                    )
                }
            }

            // Poängvisning med floating score overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.CenterVertically),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.semantics {
                                contentDescription = scoreContentDesc.value
                                liveRegion = LiveRegionMode.Polite
                            }
                        )
                    }
                    // High score
                    Text(
                        text = stringResource(R.string.high_score, viewModel.getHighScore()),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics {
                            contentDescription = highScoreContentDesc.value
                        }
                    )
                }
                // Floating score animation (overlay - påverkar inte layout)
                if (pendingScoreAnimation != null) {
                    FloatingScoreText(
                        text = pendingScoreAnimation!!.text,
                        color = pendingScoreAnimation!!.color,
                        onAnimationEnd = { pendingScoreAnimation = null }
                    )
                }
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
                    modifier = Modifier
                        .size(width = 110.dp, height = 56.dp) // 56dp minimum touch target
                        .semantics {
                            role = Role.Button
                            stateDescription = if (buttonsEnabled) "Enabled" else "Disabled during animation"
                        }
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
                    modifier = Modifier
                        .size(width = 110.dp, height = 56.dp) // 56dp minimum touch target
                        .semantics {
                            role = Role.Button
                            stateDescription = if (buttonsEnabled) "Enabled" else "Disabled during animation"
                        }
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
                    contentDesc = stringResource(R.string.suit_hearts),
                    onClick = { handleGuess("Hearts", 5) },
                    enabled = buttonsEnabled
                )
                SuitButton(
                    iconRes = R.drawable.ic_diamonds,
                    contentDesc = stringResource(R.string.suit_diamonds),
                    onClick = { handleGuess("Diamonds", 5) },
                    enabled = buttonsEnabled
                )
                SuitButton(
                    iconRes = R.drawable.ic_clubs,
                    contentDesc = stringResource(R.string.suit_clubs),
                    onClick = { handleGuess("Clubs", 5) },
                    enabled = buttonsEnabled
                )
                SuitButton(
                    iconRes = R.drawable.ic_spades,
                    contentDesc = stringResource(R.string.suit_spades),
                    onClick = { handleGuess("Spades", 5) },
                    enabled = buttonsEnabled
                )
            }

            // Tillbaka-knapp
            Button(
                onClick = onBackClick,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .width(130.dp)
                    .height(56.dp) // 56dp minimum touch target
                    .padding(bottom = 8.dp)
                    .semantics {
                        role = Role.Button
                    }
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
                            performHaptic()
                            viewModel.resetGame()
                            isFlipped = false
                            showGameOverDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth()
                            .semantics { role = Role.Button }
                    ) {
                        Text(stringResource(R.string.play_again), color = Color.White)
                    }
                },
                dismissButton = {
                    Button(
                        onClick = {
                            performHaptic()
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier
                            .height(48.dp)
                            .fillMaxWidth()
                            .semantics { role = Role.Button }
                    ) {
                        Text(stringResource(R.string.exit), color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            )
        }
    }
}

// Floating score animation (+1, +5, -1)
@Composable
fun FloatingScoreText(
    text: String,
    color: Color,
    onAnimationEnd: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val animatedOffsetY by animateFloatAsState(
        targetValue = -80f,
        animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
        label = "FloatingScoreY"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(durationMillis = 1200, delayMillis = 200, easing = FastOutSlowInEasing),
        label = "FloatingScoreAlpha"
    )
    val animatedScale by animateFloatAsState(
        targetValue = 1.2f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "FloatingScoreScale"
    )

    LaunchedEffect(key1 = text) {
        delay(1300)
        onAnimationEnd()
    }

    // Använd Box med fixed size 0 så den inte påverkar layout
    Box(
        modifier = Modifier
            .size(0.dp)
            .graphicsLayer {
                translationY = animatedOffsetY
                scaleX = animatedScale
                scaleY = animatedScale
                alpha = animatedAlpha
            }
    ) {
        Text(
            text = text,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            // Text placeras centrerat i Box men påverkar inte layout
        )
    }
}

@Composable
fun SuitButton(
    iconRes: Int,
    contentDesc: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    // Använd tema-anpassad bakgrundsfärg för synlighet i både ljust/mörkt läge
    val surfaceColor = MaterialTheme.colorScheme.surfaceContainerHighest
    
    Surface(
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
        color = surfaceColor,
        modifier = Modifier
            .size(56.dp) // 56dp minimum touch target (Material Design)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                enabled = enabled, 
                onClick = onClick,
                role = Role.Button,
                onClickLabel = contentDesc
            )
            .semantics {
                role = Role.Button
                stateDescription = if (enabled) "Enabled" else "Disabled during animation"
                contentDescription = contentDesc
            }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = "", // Handled by parent semantics
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
