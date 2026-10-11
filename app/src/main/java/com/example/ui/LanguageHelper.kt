package com.example.ui

enum class AppLanguage(val code: String) {
    EN("en"),
    RU("ru")
}

object LanguageHelper {
    fun getString(key: String, language: AppLanguage): String {
        return if (language == AppLanguage.RU) {
            RussianStrings.values[key] ?: EnglishStrings.values[key] ?: key
        } else {
            EnglishStrings.values[key] ?: key
        }
    }
}
