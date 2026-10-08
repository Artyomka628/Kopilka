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
 * Словарь переводов на английский язык (необязательно).
 * Если категории нет в этом словаре, будет использовано её название из CATEGORY_DICTIONARY.
 */
val CATEGORY_TRANSLATIONS_EN: Map<String, String> = mapOf(
    "Прочее" to "Other",
    "Продукты" to "Groceries",
    "Кафе и рестораны" to "Cafe & Restaurants",
    "Фастфуд" to "Fast food",
    "Транспорт" to "Transport",
    "Автомобиль" to "Car & Auto",
    "Заправка" to "Fuel",
    "Жильё" to "Housing & Rent",
    "Коммуналка" to "Utilities",
    "Связь и интернет" to "Internet & Mobile",
    "Здоровье" to "Health",
    "Аптека" to "Pharmacy",
    "Одежда" to "Clothing & Shoes",
    "Красота" to "Beauty & Care",
    "Развлечения" to "Entertainment",
    "Игры" to "Games",
    "Подписки" to "Subscriptions",
    "Техника" to "Tech & Electronics",
    "Образование" to "Education",
    "Подарки" to "Gifts",
    "Путешествия" to "Travel",
    "Питомцы" to "Pets",
    "Спорт" to "Sports",
    "Услуги" to "Services",
    "Кредиты и долги" to "Debts & Loans",
    "Налоги" to "Taxes & Fees",
    "Хобби" to "Hobby",
    "Дети" to "Kids"
)

/**
 * Словарь для обратной совместимости со старыми идентификаторами сохраненных транзакций
 */
private val LEGACY_CATEGORY_ID_MAP: Map<String, String> = mapOf(
    "other" to "Прочее",
    "groceries" to "Продукты",
    "cafe" to "Кафе и рестораны",
    "fastfood" to "Фастфуд",
    "transport" to "Транспорт",
    "auto" to "Автомобиль",
    "fuel" to "Заправка",
    "housing" to "Жильё",
    "utilities" to "Коммуналка",
    "internet" to "Связь и интернет",
    "health" to "Здоровье",
    "pharmacy" to "Аптека",
    "clothing" to "Одежда",
    "beauty" to "Красота",
    "entertainment" to "Развлечения",
    "games" to "Игры",
    "subscriptions" to "Подписки",
    "tech" to "Техника",
    "education" to "Образование",
    "gifts" to "Подарки",
    "travel" to "Путешествия",
    "pets" to "Питомцы",
    "sports" to "Спорт",
    "services" to "Услуги",
    "debts" to "Кредиты и долги",
    "taxes" to "Налоги",
    "hobby" to "Хобби",
    "kids" to "Дети"
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

            // 3. Совпадение со старыми идентификаторами версий (e.g. "groceries" -> "Продукты")
            val legacyName = LEGACY_CATEGORY_ID_MAP[id.lowercase()]
            if (legacyName != null && CATEGORY_DICTIONARY.containsKey(legacyName)) {
                return ExpenseCategory(name = legacyName, icon = CATEGORY_DICTIONARY[legacyName]!!)
            }

            // 4. Совпадение по английскому названию
            val reverseEnMatch = CATEGORY_TRANSLATIONS_EN.entries.firstOrNull {
                it.value.equals(id, ignoreCase = true)
            }
            if (reverseEnMatch != null && CATEGORY_DICTIONARY.containsKey(reverseEnMatch.key)) {
                return ExpenseCategory(
                    name = reverseEnMatch.key,
                    icon = CATEGORY_DICTIONARY[reverseEnMatch.key]!!
                )
            }

            // 5. Если категория была создана пользователем вручную и удалена позже
            return ExpenseCategory(name = id, icon = OTHER.icon)
        }
    }
}
