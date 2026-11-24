package com.example.color_or_suit

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.preference.PreferenceManager
import com.example.color_or_suit.ui.theme.ColorOrSuitTheme

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

    // Visa onboarding-dialog första gången appen öppnas
    androidx.compose.runtime.LaunchedEffect(key1 = true) {
        val prefs = androidx.preference.PreferenceManager.getDefaultSharedPreferences(context)
        val hasSeenOnboarding = prefs.getBoolean("has_seen_onboarding", false)
        if (!hasSeenOnboarding) {
            showOnboardingDialog = true
            prefs.edit().putBoolean("has_seen_onboarding", true).apply()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                // App-titel med Kavoon-font
                Text(
                    text = stringResource(R.string.app_title),
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    lineHeight = 52.sp,
                    modifier = Modifier.padding(bottom = 60.dp)
                )

                // Start / Play-knapp
                Button(
                    onClick = {
                        performHaptic()
                        onPlayClick()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .width(180.dp)
                        .height(56.dp)
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

                Spacer(modifier = Modifier.height(20.dp))

                // Inställningar / Settings-knapp
                Button(
                    onClick = {
                        performHaptic()
                        onSettingsClick()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .width(180.dp)
                        .height(56.dp)
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

                Spacer(modifier = Modifier.height(20.dp))

                // Om / About-knapp
                Button(
                    onClick = {
                        performHaptic()
                        onButtonClick()
                        showAboutDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .width(180.dp)
                        .height(56.dp)
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
