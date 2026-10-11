# Repository shortcuts

This folder is a navigation hub for the most frequently edited areas. The links below work on GitHub and in local Markdown viewers.

- [Code map](../CODEMAP.md) — overview of source files and responsibilities.
- [App entry point and navigation](../app/src/main/java/com/example/ui/KopilkaApp.kt)
- [Main screen](../app/src/main/java/com/example/ui/MainScreen.kt)
- [Settings screen](../app/src/main/java/com/example/ui/SettingsScreen.kt)
- [Debt tracker](../app/src/main/java/com/example/ui/DebtsScreen.kt)
- [Transaction sheets](../app/src/main/java/com/example/ui/TransactionSheets.kt)
- [Interactive onboarding](../app/src/main/java/com/example/ui/OnboardingDialog.kt)
- [View model and persistence](../app/src/main/java/com/example/ui/KopilkaViewModel.kt)
- [English strings](../app/src/main/java/com/example/ui/EnglishStrings.kt)
- [Russian strings](../app/src/main/java/com/example/ui/RussianStrings.kt)
- [Compose theme](../app/src/main/java/com/example/ui/theme/)
- [Android build workflow](../.github/workflows/android.yml)

We intentionally use repository-relative Markdown links rather than source-code symlinks: symlinks inside Android source/resource directories can confuse IDE indexing, packaging, and Gradle source sets. This folder keeps navigation shortcuts without changing how the app is built.
