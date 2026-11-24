# Step 4: Valfria Förbättringar (Enhancements)

## 1. High Score Persistence (SharedPreferences)

**Mål:** Spara bästa poäng per svårighetsgrad.

**Implementation:**
- Lägg till `highScores: Map<Difficulty, Int>` i `GameViewModel`
- Ladda/spara från `SharedPreferences` i `ActivityGame.onCreate/onDestroy`
- Visa high score i UI (under nuvarande poäng eller i GameOver-dialog)

## 2. Onboarding / Första Gången Tutorial

**Mål:** Visa regler första gången appen öppnas.

**Implementation:**
- `SharedPreferences` flagga `hasSeenOnboarding`
- Visa dialog vid första start i `MainActivity`
- "Visa inte igen" checkbox

## 3. Poäng-animation (+1/+5/-1 flyter upp)

**Mål:** Visuell feedback när poäng ändras.

**Implementation:**
- Compose `AnimatedVisibility` eller `animateFloatAsState` för flytande text
- Visa "+1", "+5", "-1" bredvid poängräknaren i 1-2 sekunder

## 4. Dependency Updates

**Nuvarande versioner:**
- AGP: 8.13.2 → **8.14.0** (latest stable)
- Kotlin: 2.0.21 → **2.1.0** (latest stable)
- Compose BOM: 2024.10.01 → **2025.02.00** (latest)
- Material: 1.12.0 → **1.13.0** (latest)
- Core KTX: 1.15.0 → **1.16.0** (latest)
- Activity: 1.9.3 → **1.10.0** (latest)

**Risk:** Stora versionhopp kan bryta kompatibilitet. Gör ett tag i git först.

## 5. Accessibility Improvements

- Lägg till `contentDescription` på alla ikon-knappar (redan gjort för SuitButton)
- `semantics { role = Role.Button }` på SuitButton
- Stöd för TalkBack / skärmläsare

## 6. Landscape Layout Optimization

Kontrollera att `layout-land` och `layout-sw600dp-land` fungerar bra.

## Verification
- Build: `./gradlew assembleDebug`
- Testa på emulator/enhet
- Kör tester i Android Studio