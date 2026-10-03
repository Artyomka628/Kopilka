package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.AppLanguage

enum class ExpenseCategory(
    val id: String,
    val titleRu: String,
    val titleEn: String,
    val icon: ImageVector
) {
    OTHER("other", "Прочее", "Other", Icons.Default.MoreHoriz),
    GROCERIES("groceries", "Продукты", "Groceries", Icons.Default.ShoppingCart),
    CAFE("cafe", "Кафе и рестораны", "Cafe & Restaurants", Icons.Default.Restaurant),
    FASTFOOD("fastfood", "Фастфуд", "Fast food", Icons.Default.Fastfood),
    TRANSPORT("transport", "Транспорт", "Transport", Icons.Default.DirectionsBus),
    AUTO("auto", "Автомобиль", "Car & Auto", Icons.Default.DirectionsCar),
    FUEL("fuel", "Заправка", "Fuel", Icons.Default.LocalGasStation),
    HOUSING("housing", "Жильё и аренда", "Housing & Rent", Icons.Default.Home),
    UTILITIES("utilities", "Коммуналка", "Utilities", Icons.Default.WaterDrop),
    INTERNET("internet", "Связь и интернет", "Internet & Mobile", Icons.Default.Wifi),
    HEALTH("health", "Здоровье", "Health", Icons.Default.Favorite),
    PHARMACY("pharmacy", "Аптека", "Pharmacy", Icons.Default.Medication),
    CLOTHING("clothing", "Одежда и обувь", "Clothing & Shoes", Icons.Default.Checkroom),
    BEAUTY("beauty", "Красота и уход", "Beauty & Care", Icons.Default.Spa),
    ENTERTAINMENT("entertainment", "Развлечения", "Entertainment", Icons.Default.Movie),
    GAMES("games", "Игры", "Games", Icons.Default.SportsEsports),
    SUBSCRIPTIONS("subscriptions", "Подписки", "Subscriptions", Icons.Default.Subscriptions),
    TECH("tech", "Техника", "Tech & Electronics", Icons.Default.Devices),
    EDUCATION("education", "Образование", "Education", Icons.Default.School),
    GIFTS("gifts", "Подарки", "Gifts", Icons.Default.CardGiftcard),
    TRAVEL("travel", "Путешествия", "Travel", Icons.Default.Flight),
    PETS("pets", "Питомцы", "Pets", Icons.Default.Pets),
    SPORTS("sports", "Спорт", "Sports", Icons.Default.FitnessCenter),
    SERVICES("services", "Услуги", "Services", Icons.Default.Build),
    DEBTS("debts", "Кредиты и долги", "Debts & Loans", Icons.Default.CreditCard),
    TAXES("taxes", "Налоги и сборы", "Taxes & Fees", Icons.Default.AccountBalance),
    HOBBY("hobby", "Хобби", "Hobby", Icons.Default.Palette),
    KIDS("kids", "Дети", "Kids", Icons.Default.ChildCare);

    fun getTitle(lang: AppLanguage): String = if (lang == AppLanguage.RU) titleRu else titleEn

    companion object {
        fun fromId(id: String?): ExpenseCategory {
            if (id == null) return OTHER
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) || it.name.equals(id, ignoreCase = true) } ?: OTHER
        }
    }
}
