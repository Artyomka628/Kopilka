package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.AppLanguage

/**
 * ============================================================================
 * СЛОВАРЬ КАТЕГОРИЙ РАСХОДОВ (EXPENSE CATEGORIES DICTIONARY)
 * ============================================================================
 *
 * Хранится как словарь: "Название категории" to Иконка (ImageVector).
 * Чтобы легко изменить, добавить или удалить категории в коде:
 * 1. Добавьте или измените строку вида: "Название" to Icons.Default.Иконка,
 * 2. Категория автоматически появится в интерфейсе при добавлении расхода!
 */
val CATEGORY_DICTIONARY: Map<String, ImageVector> = linkedMapOf(
    "Еда и напитки" to Icons.Default.Restaurant,
    "Развлечения и культура" to Icons.Default.Movie,
    "Книги, подписки и медиа" to Icons.Default.Subscriptions,
    "Хобби и творчество" to Icons.Default.Palette,
    "Спорт и фитнес" to Icons.Default.FitnessCenter,
    "Путешествия и туризм" to Icons.Default.Flight,
    "Жильё и коммунальные услуги" to Icons.Default.Home,
    "Дом, мебель и ремонт" to Icons.Default.Build,
    "Одежда и аксессуары" to Icons.Default.Checkroom,
    "Красота и уход" to Icons.Default.Spa,
    "Медицина и страхование" to Icons.Default.Favorite,
    "Автомобиль" to Icons.Default.DirectionsCar,
    "Транспорт" to Icons.Default.DirectionsBus,
    "Образование" to Icons.Default.School,
    "Дети" to Icons.Default.ChildCare,
    "Домашние животные" to Icons.Default.Pets,
    "Подарки и праздники" to Icons.Default.CardGiftcard,
    "Благотворительность и религия" to Icons.Default.VolunteerActivism,
    "Налоги, штрафы и госуслуги" to Icons.Default.AccountBalance,
    "Юридические и бухгалтерские услуги" to Icons.Default.Gavel,
    "Банковские и финансовые расходы" to Icons.Default.CreditCard,
    "Сбережения и инвестиции" to Icons.Default.TrendingUp,
    "Покупки" to Icons.Default.ShoppingCart,
    "Электроника и программное обеспечение" to Icons.Default.Devices,
    "Связь, интернет и почта" to Icons.Default.Wifi,
    "Рабочие расходы" to Icons.Default.Work,
    "Безопасность" to Icons.Default.Shield,
    "Личные и бытовые услуги" to Icons.Default.Handyman,
    "Переезд и хранение вещей" to Icons.Default.LocalShipping,
    "Документы и визы" to Icons.Default.Description,
    "Природа и отдых" to Icons.Default.Forest,
    "Ночная жизнь и мероприятия" to Icons.Default.Celebration,
    "Социальные и семейные расходы" to Icons.Default.People,
    "Повседневные и непредвиденные расходы" to Icons.Default.Payments,
    "Крупные и разовые покупки" to Icons.Default.ShoppingBag,
    "Сезонные расходы" to Icons.Default.CalendarToday,
    "Прочее" to Icons.Default.MoreHoriz
)

/**
 * Словарь переводов категорий на английский язык для всех элементов меню.
 */
val CATEGORY_TRANSLATIONS_EN: Map<String, String> = mapOf(
    "Еда и напитки" to "Food & Drinks",
    "Развлечения и культура" to "Entertainment & Culture",
    "Книги, подписки и медиа" to "Books, Subscriptions & Media",
    "Хобби и творчество" to "Hobbies & Crafts",
    "Спорт и фитнес" to "Sports & Fitness",
    "Путешествия и туризм" to "Travel & Tourism",
    "Жильё и коммунальные услуги" to "Housing & Utilities",
    "Дом, мебель и ремонт" to "Home, Furniture & Repair",
    "Одежда и аксессуары" to "Clothing & Accessories",
    "Красота и уход" to "Beauty & Personal Care",
    "Медицина и страхование" to "Medicine & Insurance",
    "Автомобиль" to "Car & Automotive",
    "Транспорт" to "Transport",
    "Образование" to "Education",
    "Дети" to "Kids & Family",
    "Домашние животные" to "Pets",
    "Подарки и праздники" to "Gifts & Celebrations",
    "Благотворительность и религия" to "Charity & Donations",
    "Налоги, штрафы и госуслуги" to "Taxes, Fines & Gov Services",
    "Юридические и бухгалтерские услуги" to "Legal & Accounting",
    "Банковские и финансовые расходы" to "Banking & Financial Expenses",
    "Сбережения и инвестиции" to "Savings & Investments",
    "Покупки" to "Shopping",
    "Электроника и программное обеспечение" to "Electronics & Software",
    "Связь, интернет и почта" to "Mobile, Internet & Mail",
    "Рабочие расходы" to "Work Expenses",
    "Безопасность" to "Security",
    "Личные и бытовые услуги" to "Personal & Home Services",
    "Переезд и хранение вещей" to "Moving & Storage",
    "Документы и визы" to "Documents & Visas",
    "Природа и отдых" to "Nature & Outdoors",
    "Ночная жизнь и мероприятия" to "Nightlife & Events",
    "Социальные и семейные расходы" to "Social & Family",
    "Повседневные и непредвиденные расходы" to "Daily & Incidentals",
    "Крупные и разовые покупки" to "Major & One-Off Purchases",
    "Сезонные расходы" to "Seasonal Expenses",
    "Прочее" to "Other"
)

data class ExpenseCategory(
    val name: String,
    val icon: ImageVector
) {
    val id: String get() = name

    fun getTitle(lang: AppLanguage): String {
        return if (lang == AppLanguage.RU) {
            name
        } else {
            CATEGORY_TRANSLATIONS_EN[name] ?: name
        }
    }

    companion object {
        val OTHER: ExpenseCategory
            get() = ExpenseCategory(
                name = "Прочее",
                icon = CATEGORY_DICTIONARY["Прочее"] ?: Icons.Default.MoreHoriz
            )

        fun values(): Array<ExpenseCategory> = getAll().toTypedArray()

        fun getAll(): List<ExpenseCategory> {
            return CATEGORY_DICTIONARY.map { (name, icon) ->
                ExpenseCategory(name = name, icon = icon)
            }
        }

        fun fromId(id: String?): ExpenseCategory {
            if (id.isNullOrBlank()) return OTHER

            // 1. Прямое совпадение по названию из словаря
            val exactIcon = CATEGORY_DICTIONARY[id]
            if (exactIcon != null) {
                return ExpenseCategory(name = id, icon = exactIcon)
            }

            // 2. Поиск без учета регистра
            val match = CATEGORY_DICTIONARY.entries.firstOrNull {
                it.key.equals(id, ignoreCase = true)
            }
            if (match != null) {
                return ExpenseCategory(name = match.key, icon = match.value)
            }

            // 3. Совпадение по английскому названию
            val reverseEnMatch = CATEGORY_TRANSLATIONS_EN.entries.firstOrNull {
                it.value.equals(id, ignoreCase = true)
            }
            if (reverseEnMatch != null && CATEGORY_DICTIONARY.containsKey(reverseEnMatch.key)) {
                return ExpenseCategory(
                    name = reverseEnMatch.key,
                    icon = CATEGORY_DICTIONARY[reverseEnMatch.key]!!
                )
            }

            // 4. По умолчанию для неизвестных категорий
            return OTHER
        }
    }
}
