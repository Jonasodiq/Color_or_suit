package com.example.color_or_suit

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
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
                        soundManager.play(SoundManager.SoundType.BUTTON_CLICK)
                        startActivity(Intent(this, ActivityGame::class.java))
                    },
                    onSettingsClick = {
                        soundManager.play(SoundManager.SoundType.BUTTON_CLICK)
                        startActivity(Intent(this, SettingsActivity::class.java))
                    },
                    onButtonClick = {
                        soundManager.play(SoundManager.SoundType.BUTTON_CLICK)
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
                    onClick = onPlayClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .width(180.dp)
                        .height(56.dp)
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
                    onClick = onSettingsClick,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .width(180.dp)
                        .height(56.dp)
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
                        onButtonClick()
                        showAboutDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .width(180.dp)
                        .height(56.dp)
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
                    title = {
                        Text(
                            text = stringResource(R.string.about_the_game),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.dsc_the_game),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                onButtonClick()
                                showAboutDialog = false
                            }
                        ) {
                            Text("OK", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                )
            }
        }
    }
}
