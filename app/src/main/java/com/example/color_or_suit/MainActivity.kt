package com.example.color_or_suit

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import com.example.color_or_suit.ui.theme.ColorOrSuitTheme
import com.example.color_or_suit.ui.theme.DesignSystem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        soundManager = SoundManager(this)

        setContent {
            val prefs = remember { PreferenceManager.getDefaultSharedPreferences(this) }
            val themePref = remember { prefs.getString("theme", "light") }
            val darkTheme = when (themePref) {
                "dark"  -> true
                "light" -> false
                else    -> isSystemInDarkTheme()
            }

            ColorOrSuitTheme(darkTheme = darkTheme) {
                MainScreen(
                    onPlayClick = {
                        soundManager.play(SoundManager.SoundType.BUTTON_CLICK, volume = 0.35f)
                        startActivity(Intent(this, ActivityGame::class.java))
                    },
                    onSettingsClick = {
                        soundManager.play(SoundManager.SoundType.BUTTON_CLICK, volume = 0.35f)
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onButtonClick = {
                        soundManager.play(SoundManager.SoundType.BUTTON_CLICK, volume = 0.35f)
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
fun MainScreen(
    onPlayClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onButtonClick: () -> Unit
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showOnboardingDialog by remember { mutableStateOf(false) }
    val view = LocalView.current
    val context = LocalContext.current
    val density = LocalDensity.current

    // Pre-compute string resources for semantics
    val playDesc = stringResource(R.string.play)
    val settingsDesc = stringResource(R.string.settings)
    val aboutDesc = stringResource(R.string.about)
    val aboutGameDesc = stringResource(R.string.about_the_game)
    val dscGameDesc = stringResource(R.string.dsc_the_game)
    val onboardingTitleDesc = stringResource(R.string.onboarding_title)
    val onboardingMessageDesc = stringResource(R.string.onboarding_message)
    val onboardingGotItDesc = stringResource(R.string.onboarding_got_it)

    fun performHaptic() {
        view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
    }

    // Entrance animations state
    val titleAnim = remember { mutableStateOf(0f) }
    val playBtnAnim = remember { mutableStateOf(0f) }
    val settingsBtnAnim = remember { mutableStateOf(0f) }
    val aboutBtnAnim = remember { mutableStateOf(0f) }

    // Visa onboarding-dialog första gången appen öppnas
    androidx.compose.runtime.LaunchedEffect(key1 = true) {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        val hasSeenOnboarding = prefs.getBoolean("has_seen_onboarding", false)
        if (!hasSeenOnboarding) {
            showOnboardingDialog = true
            prefs.edit().putBoolean("has_seen_onboarding", true).apply()
        }
    }

    // Trigger entrance animations
    androidx.compose.runtime.LaunchedEffect(key1 = true) {
        titleAnim.value = 1f
        // Staggered animation for buttons
        delay(DesignSystem.Motion.staggerDelay)
        playBtnAnim.value = 1f
        delay(DesignSystem.Motion.staggerDelay)
        settingsBtnAnim.value = 1f
        delay(DesignSystem.Motion.staggerDelay)
        aboutBtnAnim.value = 1f
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
    val slideOffsetTitle = with(density) { (-80.dp).roundToPx() }
    val slideOffsetButtons = with(density) { (40.dp).roundToPx() }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(DesignSystem.Spacing.screenPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // App-titel med Kavoon-font + entrance animation
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
                        text = stringResource(R.string.app_title),
                        style = MaterialTheme.typography.displayLarge,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        lineHeight = 52.sp,
                        modifier = Modifier.padding(bottom = DesignSystem.Spacing.xl)
                    )
                }

                // Start / Play-knapp + entrance animation
                AnimatedVisibility(
                    visible = playBtnAnim.value > 0.5f,
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
                        onClick = {
                            performHaptic()
                            onPlayClick()
                        },
                        shape = DesignSystem.Shape.button,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .width(180.dp)
                            .height(DesignSystem.TouchTarget.comfortable)
                            .semantics {
                                role = Role.Button
                                contentDescription = playDesc
                            }
                    ) {
                        Text(
                            text = stringResource(R.string.play),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.buttonGap))

                // Inställningar / Settings-knapp + entrance animation
                AnimatedVisibility(
                    visible = settingsBtnAnim.value > 0.5f,
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
                        onClick = {
                            performHaptic()
                            onSettingsClick()
                        },
                        shape = DesignSystem.Shape.button,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .width(180.dp)
                            .height(DesignSystem.TouchTarget.comfortable)
                            .semantics {
                                role = Role.Button
                                contentDescription = settingsDesc
                            }
                    ) {
                        Text(
                            text = stringResource(R.string.settings),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(DesignSystem.Spacing.buttonGap))

                // Om / About-knapp + entrance animation
                AnimatedVisibility(
                    visible = aboutBtnAnim.value > 0.5f,
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
                        onClick = {
                            performHaptic()
                            onButtonClick()
                            showAboutDialog = true
                        },
                        shape = DesignSystem.Shape.button,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .width(180.dp)
                            .height(DesignSystem.TouchTarget.comfortable)
                            .semantics {
                                role = Role.Button
                                contentDescription = aboutDesc
                            }
                    ) {
                        Text(
                            text = stringResource(R.string.about),
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }

            // About / Regler Dialog i Compose
            if (showAboutDialog) {
                AlertDialog(
                    onDismissRequest = { showAboutDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                    title = {
                        Text(
                            text = stringResource(R.string.about_the_game),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.dsc_the_game),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.semantics { contentDescription = dscGameDesc }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                performHaptic()
                                onButtonClick()
                                showAboutDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth()
                                .semantics { role = Role.Button }
                        ) {
                            Text("OK", style = MaterialTheme.typography.labelLarge, color = Color.White)
                        }
                    }
                )
            }
            
            // Onboarding Dialog (första gången)
            if (showOnboardingDialog) {
                AlertDialog(
                    onDismissRequest = { showOnboardingDialog = false },
                    properties = DialogProperties(usePlatformDefaultWidth = false),
                    title = {
                        Text(
                            text = stringResource(R.string.onboarding_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.onboarding_message),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.semantics { contentDescription = onboardingMessageDesc }
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                performHaptic()
                                onButtonClick()
                                showOnboardingDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            modifier = Modifier
                                .height(48.dp)
                                .fillMaxWidth()
                                .semantics { role = Role.Button }
                        ) {
                            Text(stringResource(R.string.onboarding_got_it), style = MaterialTheme.typography.labelLarge, color = Color.White)
                        }
                    }
                )
            }
        }
    }
}
