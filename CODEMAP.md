# Kopilka Code Map

## Where to make changes

- `app/src/main/java/com/example/model/` — data models for transactions, categories, and debts.
- `app/src/main/java/com/example/ui/KopilkaApp.kt` — Compose entry point, navigation, startup dialogs, and backup file pickers.
- `app/src/main/java/com/example/ui/MainScreen.kt` — main screen, balance, transaction history, primary actions, and cards.
- `app/src/main/java/com/example/ui/SettingsScreen.kt` and `SettingsContent.kt` — settings screen and its components.
- `app/src/main/java/com/example/ui/DebtsScreen.kt` — debts, filters, editing, and repayment status.
- `app/src/main/java/com/example/ui/TransactionSheets.kt` — income, expense, goal, cash-counting, and app-information sheets.
- `app/src/main/java/com/example/ui/OnboardingDialog.kt` — first-launch interactive tutorial.
- `app/src/main/java/com/example/ui/KopilkaViewModel.kt` — app state, settings persistence, and data operations.
- `app/src/main/java/com/example/ui/LanguageHelper.kt` — language selection and translated-string lookup.
- `app/src/main/java/com/example/ui/EnglishStrings.kt` and `RussianStrings.kt` — UI text. Add every new string key to both maps.
- `app/src/main/java/com/example/ui/theme/` — Compose colors, typography, and theme.
- `app/src/main/res/` — Android resources, including app icons, XML resources, and themes.
- `app/build.gradle.kts`, `gradle/`, and root Gradle files — app version, dependencies, and build configuration.
- `.github/workflows/android.yml` — automated APK builds.

For a quick directory guide and shortcuts to key files, see [links/README.md](links/README.md).
