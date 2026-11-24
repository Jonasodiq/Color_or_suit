package com.example.color_or_suit

import android.os.Build
import android.os.Bundle
import android.view.HapticFeedbackConstants
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.example.color_or_suit.ui.theme.ColorOrSuitTheme

class SettingsActivity : ComponentActivity() {

    private lateinit var soundManager: SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeManager.applyTheme(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        soundManager = SoundManager(this)

        setContent {
            val prefs = remember { PreferenceManager.getDefaultSharedPreferences(this) }
            var themePref by remember { mutableStateOf(prefs.getString("theme", "light") ?: "light") }

            val darkTheme = when (themePref) {
                "dark"  -> true
                "light" -> false
                else    -> isSystemInDarkTheme()
            }

            ColorOrSuitTheme(darkTheme = darkTheme) {
                SettingsScreen(
                    soundManager = soundManager,
                    onBackClick = {
                        soundManager.play(SoundManager.SoundType.BUTTON_CLICK, volume = 0.35f)
                        finish()
                    },
                    onThemeChanged = { newTheme ->
                        themePref = newTheme
                        prefs.edit { putString("theme", newTheme) }
                        ThemeManager.applyTheme(this)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    soundManager: SoundManager,
    onBackClick: () -> Unit,
    onThemeChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val view = LocalView.current
    val prefs = remember { PreferenceManager.getDefaultSharedPreferences(context) }

    var soundEnabled by remember { mutableStateOf(prefs.getBoolean("sound", true)) }
    var selectedDifficulty by remember { mutableStateOf(prefs.getString("difficulty", "medium") ?: "medium") }
    val currentTheme = prefs.getString("theme", "light") ?: "light"

    // Pre-compute string resources for semantics
    val difficultyTitle = stringResource(R.string.difficulty_title)
    val soundTitle = stringResource(R.string.sound_title)
    val themeTitle = stringResource(R.string.theme_title)
    val backDesc = stringResource(R.string.back)

    fun performHaptic() {
        val feedbackConstant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.VIRTUAL_KEY
        }
        view.performHapticFeedback(feedbackConstant)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            performHaptic()
                            onBackClick()
                        },
                        modifier = Modifier
                            .size(48.dp) // Minimum touch target
                            .semantics { role = Role.Button }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Difficulty Setting Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.difficulty_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.difficulty_dsc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    val difficultyLevels = stringArrayResource(R.array.difficulty_levels)
                    val difficultyValues = stringArrayResource(R.array.difficulty_values)

                    difficultyValues.forEachIndexed { index, value ->
                        val label = difficultyLevels.getOrElse(index) { value }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp) // Minimum touch target
                                .semantics {
                                    role = Role.RadioButton
                                    selected = (selectedDifficulty == value)
                                    contentDescription = label
                                }
                                .clickable(
                                    enabled = true,
                                    onClick = {
                                        performHaptic()
                                        soundManager.play(SoundManager.SoundType.BUTTON_CLICK, volume = 0.35f)
                                        selectedDifficulty = value
                                        prefs.edit { putString("difficulty", value) }
                                    }
                                )
                        ) {
                            RadioButton(
                                selected = (selectedDifficulty == value),
                                onClick = {
                                    performHaptic()
                                    soundManager.play(SoundManager.SoundType.BUTTON_CLICK, volume = 0.35f)
                                    selectedDifficulty = value
                                    prefs.edit { putString("difficulty", value) }
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }

            // 2. Sound Setting Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp), // Minimum touch target
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.sound_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(R.string.sound_dsc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                    Switch(
                        checked = soundEnabled,
                        onCheckedChange = { isChecked ->
                            performHaptic()
                            soundEnabled = isChecked
                            prefs.edit { putBoolean("sound", isChecked) }
                            if (isChecked) {
                                soundManager.play(SoundManager.SoundType.BUTTON_CLICK, volume = 0.35f)
                            }
                        },
                        modifier = Modifier
                            .semantics {
                                role = Role.Switch
                                contentDescription = soundTitle
                            }
                    )
                }
            }

            // 3. Theme Setting Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.theme_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.theme_dsc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )

                    val themeOptions = stringArrayResource(R.array.theme_options)
                    val themeValues = stringArrayResource(R.array.theme_values)

                    themeValues.forEachIndexed { index, value ->
                        val label = themeOptions.getOrElse(index) { value }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp) // Minimum touch target
                                .semantics {
                                    role = Role.RadioButton
                                    selected = (currentTheme == value)
                                    contentDescription = label
                                }
                                .clickable(
                                    enabled = true,
                                    onClick = {
                                        performHaptic()
                                        soundManager.play(SoundManager.SoundType.BUTTON_CLICK, volume = 0.35f)
                                        onThemeChanged(value)
                                    }
                                )
                        ) {
                            RadioButton(
                                selected = (currentTheme == value),
                                onClick = {
                                    performHaptic()
                                    soundManager.play(SoundManager.SoundType.BUTTON_CLICK, volume = 0.35f)
                                    onThemeChanged(value)
                                },
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.CenterVertically)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
        }
    }
}
