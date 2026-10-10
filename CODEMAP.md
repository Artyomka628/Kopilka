# Kopilka code map

## Где что менять

- `app/src/main/java/com/example/model/` — модели данных: операции, категории и долги.
- `app/src/main/java/com/example/ui/KopilkaApp.kt` — точка входа в Compose-интерфейс, навигация, окна при запуске и выбор файлов для резервной копии.
- `app/src/main/java/com/example/ui/MainScreen.kt` — главный экран, баланс, история, основные действия и карточки.
- `app/src/main/java/com/example/ui/SettingsScreen.kt` и `SettingsContent.kt` — экран настроек и его элементы.
- `app/src/main/java/com/example/ui/DebtsScreen.kt` — долги, фильтры, редактирование и отметка возврата.
- `app/src/main/java/com/example/ui/TransactionSheets.kt` — окна дохода, расхода, цели, подсчёта денег и информации о приложении.
- `app/src/main/java/com/example/ui/OnboardingDialog.kt` — обучение при первом запуске.
- `app/src/main/java/com/example/ui/KopilkaViewModel.kt` — состояние приложения, сохранение настроек и операции с данными.
- `app/src/main/java/com/example/ui/LanguageHelper.kt` — выбор языка и поиск перевода.
- `app/src/main/java/com/example/ui/EnglishStrings.kt` и `RussianStrings.kt` — тексты интерфейса. Для новой надписи добавляйте одинаковый ключ в обе карты.
- `app/src/main/java/com/example/ui/theme/` — цвета, типографика и тема Compose.
- `app/src/main/res/` — ресурсы Android: иконки приложения, XML-ресурсы и темы.
- `app/build.gradle.kts`, `gradle/` и корневые файлы Gradle — версия, зависимости и параметры сборки.
- `.github/workflows/android.yml` — автоматическая сборка APK.

## Когда запускается сборка

Push и pull request запускают сборку при изменениях исходников приложения, ресурсов или конфигурации Gradle. Изменение только workflow не запускает сборку приложения. Для ручного запуска используйте **Actions → Android build → Run workflow**.
