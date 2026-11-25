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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.ui.platform.LocalDensity
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
import com.example.color_or_suit.ui.theme.DesignSystem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

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

    // 3D-kortvändning via rotationY - improved with spring
    val rotation by animateFloatAsState(
        targetValue = if (isFlipped) 180f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "CardFlipAnimation"
    )

    // Entrance animations state
    val titleAnim = remember { mutableStateOf(0f) }
    val cardAnim = remember { mutableStateOf(0f) }
    val scoreAnim = remember { mutableStateOf(0f) }
    val colorBtnsAnim = remember { mutableStateOf(0f) }
    val suitBtnsAnim = remember { mutableStateOf(0f) }
    val backBtnAnim = remember { mutableStateOf(0f) }

    // Density for Dp to Int conversion
    val density = LocalDensity.current

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

    // Trigger entrance animations
    androidx.compose.runtime.LaunchedEffect(key1 = true) {
        titleAnim.value = 1f
        delay(DesignSystem.Motion.staggerDelay)
        cardAnim.value = 1f
        delay(DesignSystem.Motion.staggerDelay)
        scoreAnim.value = 1f
        delay(DesignSystem.Motion.staggerDelay)
        colorBtnsAnim.value = 1f
        delay(DesignSystem.Motion.staggerDelay)
        suitBtnsAnim.value = 1f
        delay(DesignSystem.Motion.staggerDelay)
        backBtnAnim.value = 1f
    }

    // Spring animation specs for entrance - spring for float (fade/scale) and IntOffset (slide)
    val springSpecFloat = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    val springSpecSlide = spring<IntOffset>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )
    val springSpecFade = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow
    )

    // Convert Dp offsets to pixel ints using density (for slide animations)
    val slideOffsetTitle = with(density) { (-60.dp).roundToPx() }
    val slideOffsetScore = with(density) { (-30.dp).roundToPx() }
    val slideOffsetButtons = with(density) { (40.dp).roundToPx() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(DesignSystem.Spacing.screenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Rubrik + entrance animation
            AnimatedVisibility(
                visible = titleAnim.value > 0.5f,
                enter = slideInVertically(
                    initialOffsetY = { slideOffsetTitle },
                    animationSpec = springSpecSlide
                ) + fadeIn(animationSpec = springSpecFade),
                exit = slideOutVertically(
                    targetOffsetY = { slideOffsetTitle },
                    animationSpec = springSpecSlide
                ) + fadeOut(animationSpec = springSpecFade)
            ) {
                Text(
                    text = stringResource(R.string.colorOrSuit),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = DesignSystem.Spacing.xs)
                )
            }

            // Kortkomponent med äkta 3D rotation + entrance animation
            AnimatedVisibility(
                visible = cardAnim.value > 0.5f,
                enter = fadeIn(animationSpec = springSpecFloat) + 
                    scaleIn(
                        initialScale = 0.8f,
                        animationSpec = springSpecFloat
                    ),
                exit = fadeOut(animationSpec = springSpecFloat) + 
                    scaleOut(
                        targetScale = 0.8f,
                        animationSpec = springSpecFloat
                    )
            ) {
                Card(
                    shape = DesignSystem.Shape.card,
                    elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = DesignSystem.Elevation.gameCard),
                    modifier = Modifier
                        .size(width = DesignSystem.Game.cardWidth, height = DesignSystem.Game.cardHeight)
                        .graphicsLayer {
                            rotationY = rotation
                            cameraDistance = 12f * density.density
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
            }

            // Poängvisning med floating score overlay + entrance animation
            AnimatedVisibility(
                visible = scoreAnim.value > 0.5f,
                enter = slideInVertically(
                    initialOffsetY = { slideOffsetScore },
                    animationSpec = springSpecSlide
                ) + fadeIn(animationSpec = springSpecFade),
                exit = slideOutVertically(
                    targetOffsetY = { slideOffsetScore },
                    animationSpec = springSpecSlide
                ) + fadeOut(animationSpec = springSpecFade)
            ) {
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
            }

            // Färg-knappar (Röd / Svart) + entrance animation
            AnimatedVisibility(
                visible = colorBtnsAnim.value > 0.5f,
                enter = slideInVertically(
                    initialOffsetY = { slideOffsetButtons },
                    animationSpec = springSpecSlide
                ) + fadeIn(animationSpec = springSpecFade),
                exit = slideOutVertically(
                    targetOffsetY = { slideOffsetButtons },
                    animationSpec = springSpecSlide
                ) + fadeOut(animationSpec = springSpecFade)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.lg),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { handleGuess("Red", 1) },
                        enabled = buttonsEnabled,
                        shape = DesignSystem.Shape.button,
                        colors = ButtonDefaults.buttonColors(containerColor = CardRed),
                        modifier = Modifier
                            .size(width = 110.dp, height = DesignSystem.TouchTarget.comfortable)
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
                        shape = DesignSystem.Shape.button,
                        colors = ButtonDefaults.buttonColors(containerColor = CardBlack),
                        modifier = Modifier
                            .size(width = 110.dp, height = DesignSystem.TouchTarget.comfortable)
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
            }

            // Svit-ikoner (+5 poäng) + entrance animation
            AnimatedVisibility(
                visible = suitBtnsAnim.value > 0.5f,
                enter = slideInVertically(
                    initialOffsetY = { slideOffsetButtons },
                    animationSpec = springSpecSlide
                ) + fadeIn(animationSpec = springSpecFade),
                exit = slideOutVertically(
                    targetOffsetY = { slideOffsetButtons },
                    animationSpec = springSpecSlide
                ) + fadeOut(animationSpec = springSpecFade)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.md),
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
            }

            // Tillbaka-knapp + entrance animation
            AnimatedVisibility(
                visible = backBtnAnim.value > 0.5f,
                enter = slideInVertically(
                    initialOffsetY = { slideOffsetButtons },
                    animationSpec = springSpecSlide
                ) + fadeIn(animationSpec = springSpecFade),
                exit = slideOutVertically(
                    targetOffsetY = { slideOffsetButtons },
                    animationSpec = springSpecSlide
                ) + fadeOut(animationSpec = springSpecFade)
            ) {
                Button(
                    onClick = onBackClick,
                    shape = DesignSystem.Shape.button,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .width(130.dp)
                        .height(DesignSystem.TouchTarget.comfortable)
                        .padding(bottom = DesignSystem.Spacing.xs)
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
        }

        // Vinst / Förlust Dialog
        if (showGameOverDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .semantics { contentDescription = "" }
            ) {
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

                // Confetti overlay - only on win
                if (isWin) {
                    ConfettiOverlay(visible = true)
                }
            }
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
        animationSpec = tween(
            durationMillis = DesignSystem.Game.scoreAnimationDuration,
            easing = FastOutSlowInEasing
        ),
        label = "FloatingScoreY"
    )
    val animatedAlpha by animateFloatAsState(
        targetValue = 0f,
        animationSpec = tween(
            durationMillis = DesignSystem.Game.scoreAnimationDuration,
            delayMillis = 200,
            easing = FastOutSlowInEasing
        ),
        label = "FloatingScoreAlpha"
    )
    val animatedScale by animateFloatAsState(
        targetValue = 1.2f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
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
        shape = DesignSystem.Shape.suitButton,
        shadowElevation = DesignSystem.TouchTarget.suitButton, // Use as elevation reference
        color = surfaceColor,
        modifier = Modifier
            .size(DesignSystem.TouchTarget.suitButton) // 56dp minimum touch target (Material Design)
            .clip(DesignSystem.Shape.suitButton)
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
                .padding(DesignSystem.Spacing.sm),
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
