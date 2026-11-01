package com.example.ui

import java.util.Locale

enum class AppLanguage(val code: String) {
    EN("en"),
    RU("ru")
}

fun AppLanguage.toLocale(): Locale = Locale.forLanguageTag(code)

object LanguageHelper {
    fun getString(key: String, language: AppLanguage): String {
        return if (language == AppLanguage.RU) {
            RussianStrings.values[key] ?: EnglishStrings.values[key] ?: key
        } else {
            EnglishStrings.values[key] ?: key
        }
    }
}
