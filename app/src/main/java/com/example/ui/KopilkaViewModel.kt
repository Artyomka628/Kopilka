package com.example.ui

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.KopilkaData
import com.example.model.Transaction
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID

enum class SheetType {
    LANGUAGE,
    SETTINGS,
    SPEND,
    TOP_UP,
    SET_GOAL,
    COUNT_MONEY,
    ABOUT_APP
}

enum class StatsResetFrequency {
    MANUAL, DAILY, WEEKLY, MONTHLY
}

enum class AppTheme(val key: String, val primaryColor: Color) {
    VIOLET("violet", Color(0xFFD0BCFF)),
    TEAL("teal", Color(0xFF80CBC4)),
    AMBER("amber", Color(0xFFFFB74D)),
    EMERALD("emerald", Color(0xFF81C784)),
    ROSE("rose", Color(0xFFF48FB1)),
    BLUE("blue", Color(0xFF90CAF9)),
    RED("red", Color(0xFFEF9A9A)),
    INDIGO("indigo", Color(0xFF9FA8DA)),
    ORANGE("orange", Color(0xFFFFCC80)),
    PURPLE("purple", Color(0xFFCE93D8)),
    
    // 10 new beautiful colors
    CYAN("cyan", Color(0xFF80DEEA)),
    LIME("lime", Color(0xFFD4E157)),
    PEACH("peach", Color(0xFFFFCCBC)),
    MINT("mint", Color(0xFFA5D6A7)),
    LAVENDER("lavender", Color(0xFFE1BEE7)),
    SKY("sky", Color(0xFF80D8FF)),
    MAGENTA("magenta", Color(0xFFF06292)),
    GOLD("gold", Color(0xFFFFD54F)),
    SAPPHIRE("sapphire", Color(0xFF5C6BC0)),
    BRONZE("bronze", Color(0xFFA1887F)),

    MATERIAL_YOU("material_you", Color(0xFFD0BCFF)), // dynamic fallback
    CUSTOM("custom", Color(0xFFD0BCFF)) // customized fallback
}

class KopilkaViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("kopilka_prefs", Context.MODE_PRIVATE)

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val dataAdapter = moshi.adapter(KopilkaData::class.java)
    private val transactionsAdapter = moshi.adapter<List<Transaction>>(
        Types.newParameterizedType(List::class.java, Transaction::class.java)
    )

    private val _balance = MutableStateFlow(0.0)
    val balance: StateFlow<Double> = _balance.asStateFlow()

    private val _goal = MutableStateFlow(0.0)
    val goal: StateFlow<Double> = _goal.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _selectedLanguage = MutableStateFlow<AppLanguage?>(null)
    val selectedLanguage: StateFlow<AppLanguage?> = _selectedLanguage.asStateFlow()

    private val _customCurrencyEnabled = MutableStateFlow(false)
    val customCurrencyEnabled: StateFlow<Boolean> = _customCurrencyEnabled.asStateFlow()

    private val _customCurrencySymbol = MutableStateFlow("")
    val customCurrencySymbol: StateFlow<String> = _customCurrencySymbol.asStateFlow()

    private val _goalProgressHidden = MutableStateFlow(false)
    val goalProgressHidden: StateFlow<Boolean> = _goalProgressHidden.asStateFlow()

    private val _statsResetFrequency = MutableStateFlow(StatsResetFrequency.MANUAL)
    val statsResetFrequency: StateFlow<StatsResetFrequency> = _statsResetFrequency.asStateFlow()

    private val _lastResetTimestamp = MutableStateFlow(0L)
    val lastResetTimestamp: StateFlow<Long> = _lastResetTimestamp.asStateFlow()

    private val _currentSheet = MutableStateFlow<SheetType?>(null)
    val currentSheet: StateFlow<SheetType?> = _currentSheet.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _authLoading = MutableStateFlow(false)
    val authLoading: StateFlow<Boolean> = _authLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private val _syncSuccessTrigger = MutableStateFlow(false)
    val syncSuccessTrigger: StateFlow<Boolean> = _syncSuccessTrigger.asStateFlow()

    private val _unsyncedTxIds = MutableStateFlow<Set<String>>(emptySet())
    val unsyncedTxIds: StateFlow<Set<String>> = _unsyncedTxIds.asStateFlow()

    private val _deletedTxIds = MutableStateFlow<Set<String>>(emptySet())
    val deletedTxIds: StateFlow<Set<String>> = _deletedTxIds.asStateFlow()

    private val _currentTheme = MutableStateFlow(AppTheme.VIOLET)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()

    private val _customThemeColor = MutableStateFlow(Color(0xFFD0BCFF))
    val customThemeColor: StateFlow<Color> = _customThemeColor.asStateFlow()

    private val _launcherIconOption = MutableStateFlow("MATCH_THEME")
    val launcherIconOption: StateFlow<String> = _launcherIconOption.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    init {
        FirebaseManager.initialize(application)
        loadFromPrefs()
        _currentUserEmail.value = FirebaseManager.getCurrentUserEmail()
        checkAndPerformAutomaticReset()
        if (FirebaseManager.isUserSignedIn()) {
            syncData()
        }
        updateLauncherIconSettings()
    }

    private fun loadFromPrefs() {
        _balance.value = prefs.getFloat("balance", 0f).toDouble()
        _goal.value = prefs.getFloat("goal", 0f).toDouble()
        _customCurrencyEnabled.value = prefs.getBoolean("custom_currency_enabled", false)
        _customCurrencySymbol.value = prefs.getString("custom_currency_symbol", "") ?: ""
        _goalProgressHidden.value = prefs.getBoolean("goal_progress_hidden", false)

        val freqStr = prefs.getString("stats_reset_frequency", StatsResetFrequency.MANUAL.name)
        _statsResetFrequency.value = try {
            StatsResetFrequency.valueOf(freqStr ?: StatsResetFrequency.MANUAL.name)
        } catch (e: Exception) {
            StatsResetFrequency.MANUAL
        }
        _lastResetTimestamp.value = prefs.getLong("last_reset_timestamp", 0L)

        val langCode = prefs.getString("selected_language", null)
        _selectedLanguage.value = langCode?.let { code ->
            AppLanguage.values().find { it.code == code }
        }

        val txJson = prefs.getString("transactions_json", null)
        if (!txJson.isNullOrEmpty()) {
            try {
                _transactions.value = transactionsAdapter.fromJson(txJson) ?: emptyList()
            } catch (e: Exception) {
                _transactions.value = emptyList()
            }
        }

        val unsyncedSet = prefs.getStringSet("unsynced_tx_ids", emptySet()) ?: emptySet()
        _unsyncedTxIds.value = unsyncedSet

        val deletedSet = prefs.getStringSet("deleted_tx_ids", emptySet()) ?: emptySet()
        _deletedTxIds.value = deletedSet

        val themeStr = prefs.getString("current_theme", AppTheme.VIOLET.name)
        _currentTheme.value = try {
            AppTheme.valueOf(themeStr ?: AppTheme.VIOLET.name)
        } catch (e: Exception) {
            AppTheme.VIOLET
        }

        val customColorVal = prefs.getLong("custom_theme_color", Color(0xFFD0BCFF).value.toLong())
        _customThemeColor.value = Color(customColorVal.toULong())

        _launcherIconOption.value = prefs.getString("launcher_icon_option", "MATCH_THEME") ?: "MATCH_THEME"

        // If language isn't set, show language picker sheet on first start
        if (_selectedLanguage.value == null) {
            _currentSheet.value = SheetType.LANGUAGE
        }
    }

    private fun saveToPrefs() {
        prefs.edit().apply {
            putFloat("balance", _balance.value.toFloat())
            putFloat("goal", _goal.value.toFloat())
            putBoolean("custom_currency_enabled", _customCurrencyEnabled.value)
            putString("custom_currency_symbol", _customCurrencySymbol.value)
            putBoolean("goal_progress_hidden", _goalProgressHidden.value)
            putString("stats_reset_frequency", _statsResetFrequency.value.name)
            putLong("last_reset_timestamp", _lastResetTimestamp.value)
            putString("selected_language", _selectedLanguage.value?.code)
            putStringSet("unsynced_tx_ids", _unsyncedTxIds.value)
            putStringSet("deleted_tx_ids", _deletedTxIds.value)
            putString("current_theme", _currentTheme.value.name)
            putLong("custom_theme_color", _customThemeColor.value.value.toLong())
            putString("launcher_icon_option", _launcherIconOption.value)
            try {
                putString("transactions_json", transactionsAdapter.toJson(_transactions.value))
            } catch (e: Exception) {
                // Ignore serialization error
            }
            apply()
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _selectedLanguage.value = lang
        saveToPrefs()
    }

    fun setTheme(theme: AppTheme) {
        _currentTheme.value = theme
        saveToPrefs()
    }

    fun setCustomThemeColor(color: Color) {
        _customThemeColor.value = color
        _currentTheme.value = AppTheme.CUSTOM
        saveToPrefs()
    }

    fun setLauncherIconOption(option: String) {
        _launcherIconOption.value = option
        saveToPrefs()
    }

    private fun updateLauncherIconSettings() {
        // No-op: Disabled dynamic app launcher icon switching
    }

    fun applyLauncherIcon() {
        val context = getApplication<Application>()
        val pm = context.packageManager
        val theme = _currentTheme.value

        val targetAlias = if (theme == AppTheme.MATERIAL_YOU) {
            "com.example.MainActivityAliasMaterialYou"
        } else {
            // The 10 core themes with specific launcher icons/aliases configured in manifest
            val coreThemes = listOf(
                AppTheme.VIOLET,
                AppTheme.TEAL,
                AppTheme.AMBER,
                AppTheme.EMERALD,
                AppTheme.ROSE,
                AppTheme.BLUE,
                AppTheme.RED,
                AppTheme.INDIGO,
                AppTheme.ORANGE,
                AppTheme.PURPLE
            )

            // If selected theme is already a core theme, use its alias directly
            if (theme in coreThemes) {
                when (theme) {
                    AppTheme.VIOLET -> "com.example.MainActivityAliasViolet"
                    AppTheme.TEAL -> "com.example.MainActivityAliasTeal"
                    AppTheme.AMBER -> "com.example.MainActivityAliasAmber"
                    AppTheme.EMERALD -> "com.example.MainActivityAliasEmerald"
                    AppTheme.ROSE -> "com.example.MainActivityAliasRose"
                    AppTheme.BLUE -> "com.example.MainActivityAliasBlue"
                    AppTheme.RED -> "com.example.MainActivityAliasRed"
                    AppTheme.INDIGO -> "com.example.MainActivityAliasIndigo"
                    AppTheme.ORANGE -> "com.example.MainActivityAliasOrange"
                    AppTheme.PURPLE -> "com.example.MainActivityAliasPurple"
                    else -> "com.example.MainActivityAliasViolet"
                }
            } else {
                // Determine target color to find the closest core theme
                val targetColor = if (theme == AppTheme.CUSTOM) {
                    _customThemeColor.value
                } else {
                    theme.primaryColor
                }

                // Find the closest core theme based on Euclidean distance in RGB color space
                val closestCoreTheme = coreThemes.minByOrNull { core ->
                    val rDiff = core.primaryColor.red - targetColor.red
                    val gDiff = core.primaryColor.green - targetColor.green
                    val bDiff = core.primaryColor.blue - targetColor.blue
                    rDiff * rDiff + gDiff * gDiff + bDiff * bDiff
                } ?: AppTheme.VIOLET

                when (closestCoreTheme) {
                    AppTheme.VIOLET -> "com.example.MainActivityAliasViolet"
                    AppTheme.TEAL -> "com.example.MainActivityAliasTeal"
                    AppTheme.AMBER -> "com.example.MainActivityAliasAmber"
                    AppTheme.EMERALD -> "com.example.MainActivityAliasEmerald"
                    AppTheme.ROSE -> "com.example.MainActivityAliasRose"
                    AppTheme.BLUE -> "com.example.MainActivityAliasBlue"
                    AppTheme.RED -> "com.example.MainActivityAliasRed"
                    AppTheme.INDIGO -> "com.example.MainActivityAliasIndigo"
                    AppTheme.ORANGE -> "com.example.MainActivityAliasOrange"
                    AppTheme.PURPLE -> "com.example.MainActivityAliasPurple"
                    else -> "com.example.MainActivityAliasViolet"
                }
            }
        }

        val aliases = listOf(
            "com.example.MainActivityAliasViolet",
            "com.example.MainActivityAliasTeal",
            "com.example.MainActivityAliasAmber",
            "com.example.MainActivityAliasEmerald",
            "com.example.MainActivityAliasRose",
            "com.example.MainActivityAliasBlue",
            "com.example.MainActivityAliasRed",
            "com.example.MainActivityAliasIndigo",
            "com.example.MainActivityAliasOrange",
            "com.example.MainActivityAliasPurple",
            "com.example.MainActivityAliasMaterialYou"
        )

        viewModelScope.launch(Dispatchers.IO) {
            aliases.forEach { alias ->
                val component = android.content.ComponentName(context.packageName, alias)
                val isTarget = alias == targetAlias
                val state = if (isTarget) {
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                try {
                    pm.setComponentEnabledSetting(
                        component,
                        state,
                        android.content.pm.PackageManager.DONT_KILL_APP
                    )
                } catch (e: Exception) {
                    Log.e("KopilkaViewModel", "Error applying component: $alias", e)
                }
            }
            withContext(Dispatchers.Main) {
                _toastMessage.value = LanguageHelper.getString("applyIconSuccess", _selectedLanguage.value ?: AppLanguage.RU)
            }
        }
    }

    fun setCustomCurrencyEnabled(enabled: Boolean) {
        _customCurrencyEnabled.value = enabled
        saveToPrefs()
    }

    fun setCustomCurrencySymbol(symbol: String) {
        _customCurrencySymbol.value = symbol
        saveToPrefs()
    }

    fun setGoalProgressHidden(hidden: Boolean) {
        _goalProgressHidden.value = hidden
        saveToPrefs()
    }

    fun showSheet(type: SheetType) {
        _currentSheet.value = type
    }

    fun hideSheet() {
        _currentSheet.value = null
    }

    fun setGoal(amount: Double) {
        _goal.value = Math.round(amount * 100.0) / 100.0
        saveToPrefs()
    }

    fun setBalance(amount: Double) {
        val roundedAmount = Math.round(amount * 100.0) / 100.0
        val oldBalance = Math.round(_balance.value * 100.0) / 100.0
        val diff = Math.round((roundedAmount - oldBalance) * 100.0) / 100.0
        if (diff != 0.0) {
            val lang = _selectedLanguage.value ?: AppLanguage.RU
            val changeStr = formatChange(diff)
            val reasonStr = if (lang == AppLanguage.RU) {
                "Перерасчёт средств ($changeStr)"
            } else {
                "Recalculation of funds ($changeStr)"
            }
            if (_transactions.value.isEmpty()) {
                _balance.value = roundedAmount
                saveToPrefs()
            } else {
                addTransaction(diff, reasonStr)
            }
        } else {
            _balance.value = roundedAmount
            saveToPrefs()
        }
    }

    fun clearHistory() {
        val allIds = _transactions.value.map { it.id }.toSet()
        _deletedTxIds.value = _deletedTxIds.value + allIds
        _unsyncedTxIds.value = _unsyncedTxIds.value + allIds
        _transactions.value = emptyList()
        _balance.value = 0.0
        saveToPrefs()
        if (FirebaseManager.isUserSignedIn()) {
            syncData()
        }
    }

    fun cancelTransaction(txId: String) {
        val currentList = _transactions.value
        val targetTx = currentList.find { it.id == txId } ?: return
        val isLast = currentList.firstOrNull()?.id == txId

        // Remove from journal
        val updatedList = currentList.filter { it.id != txId }
        _transactions.value = updatedList

        _deletedTxIds.value = _deletedTxIds.value + txId
        _unsyncedTxIds.value = _unsyncedTxIds.value + txId

        val lang = _selectedLanguage.value ?: AppLanguage.RU
        val changeStr = formatChange(-targetTx.amount)
        
        if (isLast) {
            _balance.value = Math.round((_balance.value - targetTx.amount) * 100.0) / 100.0
            saveToPrefs()
            if (FirebaseManager.isUserSignedIn()) {
                syncData()
            }
        } else {
            val reasonStr = if (lang == AppLanguage.RU) {
                "Отмена операции ранее ($changeStr)"
            } else {
                "Cancellation of earlier operation ($changeStr)"
            }
            addTransaction(-targetTx.amount, reasonStr)
        }
    }

    private fun formatChange(amount: Double): String {
        val rounded = Math.round(amount * 100.0) / 100.0
        val sign = if (rounded > 0) "+" else ""
        val value = if (rounded % 1.0 == 0.0) {
            rounded.toLong().toString()
        } else {
            val str = String.format(java.util.Locale.US, "%.2f", rounded)
            if (str.endsWith("0")) str.dropLast(1) else str
        }
        return "$sign$value"
    }

    fun addTransaction(amount: Double, reason: String) {
        val roundedAmount = Math.round(amount * 100.0) / 100.0
        val newTx = Transaction(
            id = UUID.randomUUID().toString(),
            timestamp = System.currentTimeMillis(),
            reason = reason.trim().ifEmpty { 
                if (_selectedLanguage.value == AppLanguage.RU) "Без описания" else "No description"
            },
            amount = roundedAmount
        )
        val updatedList = listOf(newTx) + _transactions.value // Prepend to list to display newest first
        _transactions.value = updatedList
        _balance.value = Math.round((_balance.value + roundedAmount) * 100.0) / 100.0
        _unsyncedTxIds.value = _unsyncedTxIds.value + newTx.id
        saveToPrefs()
        if (FirebaseManager.isUserSignedIn()) {
            syncData()
        }
    }

    fun showToast(msg: String?) {
        _toastMessage.value = msg
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // Export state as JSON String
    fun exportStateToJsonString(): String {
        val data = KopilkaData(
            balance = _balance.value,
            goal = _goal.value,
            transactions = _transactions.value
        )
        return dataAdapter.toJson(data)
    }

    // Import state from JSON String
    fun importStateFromJsonString(json: String): Boolean {
        return try {
            val imported = dataAdapter.fromJson(json)
            if (imported != null) {
                _balance.value = imported.balance
                _goal.value = imported.goal
                _transactions.value = imported.transactions
                saveToPrefs()
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    fun setStatsResetFrequency(frequency: StatsResetFrequency) {
        _statsResetFrequency.value = frequency
        saveToPrefs()
        checkAndPerformAutomaticReset()
    }

    fun checkAndPerformAutomaticReset() {
        val freq = _statsResetFrequency.value
        if (freq == StatsResetFrequency.MANUAL) return
        
        val lastReset = _lastResetTimestamp.value
        val now = System.currentTimeMillis()
        
        if (lastReset == 0L) {
            _lastResetTimestamp.value = now
            saveToPrefs()
            return
        }
        
        if (shouldReset(freq, lastReset, now)) {
            performStatsReset(now)
        }
    }

    private fun shouldReset(frequency: StatsResetFrequency, lastResetTime: Long, currentTime: Long): Boolean {
        val lastCal = java.util.Calendar.getInstance().apply { timeInMillis = lastResetTime }
        val curCal = java.util.Calendar.getInstance().apply { timeInMillis = currentTime }
        
        return when (frequency) {
            StatsResetFrequency.MANUAL -> false
            StatsResetFrequency.DAILY -> {
                lastCal.get(java.util.Calendar.YEAR) != curCal.get(java.util.Calendar.YEAR) ||
                lastCal.get(java.util.Calendar.DAY_OF_YEAR) != curCal.get(java.util.Calendar.DAY_OF_YEAR)
            }
            StatsResetFrequency.WEEKLY -> {
                lastCal.get(java.util.Calendar.YEAR) != curCal.get(java.util.Calendar.YEAR) ||
                lastCal.get(java.util.Calendar.WEEK_OF_YEAR) != curCal.get(java.util.Calendar.WEEK_OF_YEAR)
            }
            StatsResetFrequency.MONTHLY -> {
                lastCal.get(java.util.Calendar.YEAR) != curCal.get(java.util.Calendar.YEAR) ||
                lastCal.get(java.util.Calendar.MONTH) != curCal.get(java.util.Calendar.MONTH)
            }
        }
    }

    fun performStatsReset(currentTime: Long = System.currentTimeMillis()) {
        val lang = _selectedLanguage.value ?: AppLanguage.RU
        val currentTransactions = _transactions.value
        
        // Find the last reset transaction to know where to count from
        val lastResetIndex = currentTransactions.indexOfFirst {
            it.reason.startsWith("Статистика за") || it.reason.startsWith("Statistics for")
        }
        
        val subList = if (lastResetIndex == -1) {
            currentTransactions
        } else {
            currentTransactions.take(lastResetIndex)
        }
        
        val income = subList.filter { it.amount > 0 }.sumOf { it.amount }
        val expense = subList.filter { it.amount < 0 }.sumOf { it.amount }
        
        val sdf = java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault())
        val dateStr = sdf.format(java.util.Date(currentTime))
        
        val title = if (lang == AppLanguage.RU) "Статистика за $dateStr" else "Statistics for $dateStr"
        val detail = if (lang == AppLanguage.RU) {
            "Доход - ${formatValueDouble(income)}\nРасход - ${formatValueDouble(Math.abs(expense))}"
        } else {
            "Income - ${formatValueDouble(income)}\nExpense - ${formatValueDouble(Math.abs(expense))}"
        }
        
        val reasonWithDetail = "$title|$detail"
        
        val resetTx = Transaction(
            id = UUID.randomUUID().toString(),
            amount = 0.0,
            reason = reasonWithDetail,
            timestamp = currentTime
        )
        
        _transactions.value = listOf(resetTx) + currentTransactions
        _lastResetTimestamp.value = currentTime
        saveToPrefs()
    }

    fun loginWithEmail(email: String, password: String) {
        _authLoading.value = true
        _authError.value = null
        val auth = FirebaseManager.getAuth()
        if (auth == null) {
            _authLoading.value = false
            _authError.value = if (_selectedLanguage.value == AppLanguage.RU) "Firebase не инициализирован" else "Firebase not initialized"
            return
        }
        auth.signInWithEmailAndPassword(email.trim(), password.trim())
            .addOnSuccessListener { result ->
                _currentUserEmail.value = result.user?.email
                _authLoading.value = false
                _authError.value = null
                showToast(if (_selectedLanguage.value == AppLanguage.RU) "Успешный вход!" else "Success login!")
                syncData()
            }
            .addOnFailureListener { ex ->
                _authLoading.value = false
                _authError.value = ex.localizedMessage
            }
    }

    fun signUpWithEmail(email: String, password: String) {
        _authLoading.value = true
        _authError.value = null
        val auth = FirebaseManager.getAuth()
        if (auth == null) {
            _authLoading.value = false
            _authError.value = if (_selectedLanguage.value == AppLanguage.RU) "Firebase не инициализирован" else "Firebase not initialized"
            return
        }
        auth.createUserWithEmailAndPassword(email.trim(), password.trim())
            .addOnSuccessListener { result ->
                _currentUserEmail.value = result.user?.email
                _authLoading.value = false
                _authError.value = null
                showToast(if (_selectedLanguage.value == AppLanguage.RU) "Аккаунт создан!" else "Account created!")
                syncData()
            }
            .addOnFailureListener { ex ->
                _authLoading.value = false
                _authError.value = ex.localizedMessage
            }
    }

    fun loginWithGoogle(idToken: String) {
        _authLoading.value = true
        _authError.value = null
        val auth = FirebaseManager.getAuth()
        if (auth == null) {
            _authLoading.value = false
            _authError.value = if (_selectedLanguage.value == AppLanguage.RU) "Firebase не инициализирован" else "Firebase not initialized"
            return
        }
        val credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnSuccessListener { result ->
                _currentUserEmail.value = result.user?.email
                _authLoading.value = false
                _authError.value = null
                showToast(if (_selectedLanguage.value == AppLanguage.RU) "Успешный вход через Google!" else "Success login via Google!")
                syncData()
            }
            .addOnFailureListener { ex ->
                _authLoading.value = false
                _authError.value = ex.localizedMessage
            }
    }

    fun signOut() {
        FirebaseManager.getAuth()?.signOut()
        _currentUserEmail.value = null
        _unsyncedTxIds.value = emptySet()
        saveToPrefs()
        showToast(if (_selectedLanguage.value == AppLanguage.RU) "Вышли из аккаунта" else "Signed out")
    }

    fun isFirebaseConfigured(): Boolean {
        return FirebaseManager.getAuth() != null
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        if (connectivityManager != null) {
            val activeNetwork = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
            return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        }
        return false
    }

    private var syncTimeoutJob: kotlinx.coroutines.Job? = null

    fun syncData() {
        val auth = FirebaseManager.getAuth()
        val db = FirebaseManager.getFirestore()
        val userId = auth?.currentUser?.uid
        
        if (auth == null || db == null || userId == null) {
            return
        }

        if (!isNetworkAvailable()) {
            showToast(if (_selectedLanguage.value == AppLanguage.RU) "Нет подключения к интернету. Синхронизация невозможна." else "No internet connection. Sync is not possible.")
            _isSyncing.value = false
            return
        }
        
        if (_isSyncing.value) return
        _isSyncing.value = true

        syncTimeoutJob?.cancel()
        syncTimeoutJob = viewModelScope.launch {
            delay(10000) // 10 seconds timeout
            if (_isSyncing.value) {
                _isSyncing.value = false
                showToast(if (_selectedLanguage.value == AppLanguage.RU) "Время ожидания истекло. Ошибка синхронизации." else "Sync timed out. Connection is too weak.")
            }
        }
        
        db.collection("users").document(userId).get()
            .addOnSuccessListener { document ->
                syncTimeoutJob?.cancel()
                viewModelScope.launch(Dispatchers.IO) {
                    try {
                        val remoteBalance = document.getDouble("balance") ?: 0.0
                        val remoteGoal = document.getDouble("goal") ?: 0.0
                        val remoteDeletedList = document.get("deletedTxIds") as? List<String> ?: emptyList()
                        val remoteDeleted = remoteDeletedList.toSet()
                        
                        val txMaps = document.get("transactions") as? List<Map<String, Any>> ?: emptyList()
                        val remoteTxs = txMaps.mapNotNull { map ->
                            val id = map["id"] as? String ?: return@mapNotNull null
                            val timestamp = (map["timestamp"] as? Number)?.toLong() ?: return@mapNotNull null
                            val reason = map["reason"] as? String ?: return@mapNotNull null
                            val amount = (map["amount"] as? Number)?.toDouble() ?: return@mapNotNull null
                            Transaction(id, timestamp, reason, amount)
                        }
                        
                        val localDeleted = _deletedTxIds.value
                        val mergedDeleted = localDeleted + remoteDeleted
                        
                        val localTxs = _transactions.value
                        val mergedTxs = FirebaseManager.mergeTransactions(localTxs, remoteTxs)
                            .filter { it.id !in mergedDeleted }
                        
                        val localSum = localTxs.sumOf { it.amount }
                        val mergedSum = mergedTxs.sumOf { it.amount }
                        val mergedBalance = _balance.value + (mergedSum - localSum)
                        
                        val localGoal = _goal.value
                        val mergedGoal = maxOf(localGoal, remoteGoal)
                        
                        withContext(Dispatchers.Main) {
                            _transactions.value = mergedTxs
                            _balance.value = mergedBalance
                            _goal.value = mergedGoal
                            _deletedTxIds.value = mergedDeleted
                            _unsyncedTxIds.value = emptySet()
                            
                            saveToPrefs()
                            
                            uploadStateToFirestore(userId, mergedBalance, mergedGoal, mergedTxs, mergedDeleted)
                        }
                    } catch (e: Exception) {
                        Log.e("Sync", "Error during merge", e)
                        withContext(Dispatchers.Main) {
                            _isSyncing.value = false
                        }
                    }
                }
            }
            .addOnFailureListener { ex ->
                syncTimeoutJob?.cancel()
                Log.e("Sync", "Error fetching remote data", ex)
                _isSyncing.value = false
            }
    }

    private fun uploadStateToFirestore(
        userId: String,
        balance: Double,
        goal: Double,
        transactions: List<Transaction>,
        deletedIds: Set<String>
    ) {
        val db = FirebaseManager.getFirestore() ?: return
        val data = mapOf(
            "balance" to balance,
            "goal" to goal,
            "deletedTxIds" to deletedIds.toList(),
            "transactions" to transactions.map { tx ->
                mapOf(
                    "id" to tx.id,
                    "timestamp" to tx.timestamp,
                    "reason" to tx.reason,
                    "amount" to tx.amount
                )
            }
        )
        
        db.collection("users").document(userId).set(data)
            .addOnSuccessListener {
                syncTimeoutJob?.cancel()
                _isSyncing.value = false
                viewModelScope.launch {
                    _syncSuccessTrigger.value = true
                    delay(2000)
                    _syncSuccessTrigger.value = false
                }
            }
            .addOnFailureListener { ex ->
                syncTimeoutJob?.cancel()
                Log.e("Sync", "Error uploading merged state", ex)
                _isSyncing.value = false
            }
    }

    private fun formatValueDouble(amount: Double): String {
        val rounded = Math.round(amount * 100.0) / 100.0
        return if (rounded % 1.0 == 0.0) {
            rounded.toLong().toString()
        } else {
            val str = String.format(java.util.Locale.US, "%.2f", rounded)
            if (str.endsWith("0")) str.dropLast(1) else str
        }
    }

    fun checkForUpdates() {
        _updateState.value = UpdateState.Checking
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val url = java.net.URL("https://raw.githubusercontent.com/Artyomka628/Kopilka/refs/heads/main/ver.txt")
                val connection = url.openConnection() as java.net.HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                
                val responseCode = connection.responseCode
                if (responseCode == 200) {
                    val latestVersion = connection.inputStream.bufferedReader().use { it.readText() }.trim()
                    val currentVersion = com.example.BuildConfig.VERSION_NAME
                    
                    if (latestVersion.isNotEmpty() && latestVersion != currentVersion) {
                        withContext(Dispatchers.Main) {
                            _updateState.value = UpdateState.UpdateAvailable(
                                latestVersion = latestVersion,
                                downloadUrl = "https://github.com/Artyomka628/Kopilka/releases"
                            )
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            _updateState.value = UpdateState.UpToDate
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        _updateState.value = UpdateState.Error("HTTP Error: $responseCode")
                    }
                }
                connection.disconnect()
            } catch (e: Exception) {
                Log.e("KopilkaViewModel", "Error checking updates", e)
                withContext(Dispatchers.Main) {
                    _updateState.value = UpdateState.Error(e.localizedMessage ?: "Unknown error")
                }
            }
        }
    }
}

sealed class UpdateState {
    object Idle : UpdateState()
    object Checking : UpdateState()
    data class UpdateAvailable(val latestVersion: String, val downloadUrl: String) : UpdateState()
    object UpToDate : UpdateState()
    data class Error(val message: String) : UpdateState()
}

// Utility nested helper object for type-safe Moshi list serialization
object Types {
    fun newParameterizedType(rawType: java.lang.reflect.Type, vararg typeArguments: java.lang.reflect.Type): java.lang.reflect.Type {
        return com.squareup.moshi.Types.newParameterizedType(rawType, *typeArguments)
    }
}
