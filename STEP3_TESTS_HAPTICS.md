# Step 3: Unit Tests & Haptic Feedback

## 1. Unit Tests for GameViewModel

**File:** `app/src/test/java/com/example/color_or_suit/GameViewModelTest.kt` (ny fil)

**Test cases:**
- `pickRandomCard()` - verifiera att index är inom gränser
- `isGuessCorrect()` - testa alla kombinationer (Red/Black + 4 sviter)
- `updateScore()` - addera och subtrahera
- `isWin()` - alla svårighetsgrader (EASY: 15-18, MEDIUM: 21, HARD: 31)
- `isGameOver()` - över max, under min
- `resetGame()` - score=0, nytt kort
- `setDifficultyFromPreference()` - easy/medium/hard/invalid

## 2. Haptic Feedback på knapptryck

**Filer:**
- `app/src/main/java/com/example/color_or_suit/ActivityGame.kt` - lägg till haptic i `handleGuess` och knappar
- `app/src/main/java/com/example/color_or_suit/MainActivity.kt` - haptic på menyknappar

**Implementation:**
```kotlin
import android.view.View
import androidx.core.view.performHapticFeedback
import android.view.HapticFeedbackConstants

// I onClick:
View(view).performHapticFeedback(HapticFeedbackConstants.CONFIRM)
```

## 3. Dialog Button Consistency (GameOver)

**Problem:** `confirmButton` är `Button`, `dismissButton` är `TextButton` - se annorlunda ut.

**Lösning:** Gör båda till `Button` med olika färger, eller båda `TextButton`.

## 4. Accessibility - contentDescription

Kontrollera att alla interaktiva element har `contentDescription`.

## Verification
- Build: `./gradlew assembleDebug`
- Tester (i Android Studio): Right-click GameViewModelTest → Run
- Manuellt: Testa haptic på fysisk enhet