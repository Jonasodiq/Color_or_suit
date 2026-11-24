# Quick Fixes - Step 1

## 1. Fix AndroidManifest.xml label
**File:** `app/src/main/AndroidManifest.xml`
**Line 18:** Change `android:label="Settings"` to `android:label="@string/settings"`

## 2. Fix version in build.gradle.kts
**File:** `app/build.gradle.kts`
**Lines 15-16:** Change:
- `versionCode = 1` → `versionCode = 2`
- `versionName = "1.0"` → `versionName = "1.1.0"`

## 3. Fix targetSdk
**File:** `app/build.gradle.kts`
**Line 14:** Change `targetSdk = 34` → `targetSdk = 35`

## 4. Fix icon sizes (all to 40x40dp)
**Files:** `app/src/main/res/drawable/ic_heart.xml`, `ic_diamonds.xml`, `ic_clubs.xml`, `ic_spades.xml`
- `ic_heart.xml`: Change `android:width="40dp"` `android:height="50dp"` → `android:width="40dp"` `android:height="40dp"`
- `ic_diamonds.xml`: Already 40x40dp ✓
- `ic_clubs.xml`: Already 40x40dp ✓
- `ic_spades.xml`: Already 40x40dp ✓

## 5. Fix icon tint (remove or change to white)
**Files:** All 4 icon XML files
Change `android:tint="?attr/cardBackgroundColor"` → `android:tint="@android:color/white"`

## 6. Update README version
**File:** `README.md`
**Line 61:** Change `Version: 1.1.0` → `Version: 1.1.0` (already correct, but versionCode should be 2)

## Verification
After changes:
- Build project: `./gradlew assembleDebug`
- Run unit tests: `./gradlew test`