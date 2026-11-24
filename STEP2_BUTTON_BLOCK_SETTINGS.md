# Step 2: Button Block During Animation & Settings Live Sync

## 1. Button Blocking During Card Flip (ActivityGame.kt)

**Problem:** Knapparna är aktiva under 3D-flip-animationen (350ms + 1000ms väntan). Användare kan spamma gissningar.

**Lösning:** Lägg till `enabled`-state som inaktiverar alla gissningsknappar när `isFlipped == true`.

**Filer att ändra:**
- `app/src/main/java/com/example/color_or_suit/ActivityGame.kt`

**Ändringar:**
- Lägg till `val buttonsEnabled = !isFlipped` (eller liknande)
- Applicera på alla 6 gissningsknappar (2 färg + 4 svit)
- OBS: "Tillbaka"-knappen ska fortfarande fungera

## 2. Settings Live Sync (SettingsFragment.kt)

**Problem:** När användare ändrar svårighetsgrad eller ljud i inställningar, uppdateras inte ViewModel direkt. Måste starta om spelet.

**Lösning:** Lysna på `difficulty` och `sound` i `onSharedPreferenceChanged` och uppdatera ViewModel om spelet är aktivt.

**Filer att ändra:**
- `app/src/main/java/com/example/color_or_suit/SettingsFragment.kt`

**Ändringar:**
- Expandera `onSharedPreferenceChanged` att hantera `"difficulty"` och `"sound"`
- För `difficulty`: anropa `viewModel.setDifficultyFromPreference(newValue)` om ActivityGame är aktiv
- För `sound`: SoundManager kollar redan `isSoundEnabled()` vid varje `play()`, så inget extra behövs där

## 3. GameViewModel - exposera difficulty för live-uppdatering

**Filer att ändra:**
- `app/src/main/java/com/example/color_or_suit/GameViewModel.kt`

**Ändringar:**
- Gör `setDifficultyFromPreference` public (redan det)
- Lägg till callback/observer för när difficulty ändras (optional, för UI-uppdatering)

## Verification
- Build: `./gradlew assembleDebug`
- Testa manuellt: Öppna spelet, gissa snabbt under flip → knappar ska vara inaktiva
- Testa inställningar: Ändra svårighet mid-spelet → nästa gissning ska använda nya regler