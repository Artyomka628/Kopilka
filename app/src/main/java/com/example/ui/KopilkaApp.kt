package com.example.ui

import android.os.Build

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Transaction
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun rememberPrimaryColor(viewModel: KopilkaViewModel): Color {
    val selectedTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
    val customThemeColor by viewModel.customThemeColor.collectAsStateWithLifecycle()
    val context = LocalContext.current
    return remember(selectedTheme, customThemeColor, context) {
        if (selectedTheme == AppTheme.CUSTOM) {
            customThemeColor
        } else if (selectedTheme == AppTheme.MATERIAL_YOU) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                dynamicDarkColorScheme(context).primary
            } else {
                Color(0xFFD0BCFF)
            }
        } else {
            selectedTheme.primaryColor
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KopilkaApp(viewModel: KopilkaViewModel) {
    val context = LocalContext.current
    val currentSheet by viewModel.currentSheet.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()

    val ElegantLavender = rememberPrimaryColor(viewModel)
    val ElegantBtnText = Color(0xFF1C1B1F) // beautiful dark charcoal contrast color for primary buttons

    val lang = selectedLanguage ?: AppLanguage.RU // Default to RU for UI text until set

    // Show toast messages
    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // Storage Access Framework Launchers for Backup (Save) and Restore (Load)
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            try {
                val jsonString = viewModel.exportStateToJsonString()
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    outputStream.write(jsonString.toByteArray())
                }
                viewModel.showToast(LanguageHelper.getString("historySaved", lang))
            } catch (e: Exception) {
                viewModel.showToast(LanguageHelper.getString("errorSaving", lang))
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            try {
                val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    inputStream.bufferedReader().use { it.readText() }
                }
                if (jsonString != null && viewModel.importStateFromJsonString(jsonString)) {
                    viewModel.showToast(LanguageHelper.getString("historyLoaded", lang))
                } else {
                    viewModel.showToast(LanguageHelper.getString("errorLoading", lang))
                }
            } catch (e: Exception) {
                viewModel.showToast(LanguageHelper.getString("errorLoading", lang))
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = ElegantDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Screen content
            MainScreen(
                viewModel = viewModel,
                lang = lang
            )

            // Bottom Sheet Modal (Standard Material 3 styled elegantly)
            if (currentSheet != null) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.hideSheet() },
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                    containerColor = ElegantCardBg,
                    contentColor = ElegantTextPrimary,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = ElegantTextSecondary) }
                ) {
                    when (currentSheet) {
                        SheetType.LANGUAGE -> LanguageSheetContent(viewModel)
                        SheetType.SETTINGS -> SettingsSheetContent(viewModel, onSaveClick = {
                            viewModel.hideSheet()
                            exportLauncher.launch("kopilka_history.json")
                        }, onLoadClick = {
                            viewModel.hideSheet()
                            importLauncher.launch(arrayOf("application/json"))
                        }, lang = lang)
                        SheetType.SPEND -> SpendSheetContent(viewModel, lang)
                        SheetType.TOP_UP -> TopUpSheetContent(viewModel, lang)
                        SheetType.SET_GOAL -> SetGoalSheetContent(viewModel, lang)
                        SheetType.COUNT_MONEY -> CountMoneySheetContent(viewModel, lang)
                        SheetType.ABOUT_APP -> AboutAppSheetContent(viewModel, lang)
                        null -> {}
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: KopilkaViewModel, lang: AppLanguage) {
    val balance by viewModel.balance.collectAsStateWithLifecycle()
    val goal by viewModel.goal.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    val lastResetIdx = remember(transactions) {
        transactions.indexOfFirst {
            it.reason.startsWith("Статистика за") || it.reason.startsWith("Statistics for")
        }
    }
    val activeTransactions = remember(transactions, lastResetIdx) {
        if (lastResetIdx == -1) {
            transactions
        } else {
            transactions.take(lastResetIdx)
        }
    }

    val totalIn = remember(activeTransactions) {
        activeTransactions.filter { it.amount > 0 }.sumOf { it.amount }
    }
    val totalOut = remember(activeTransactions) {
        activeTransactions.filter { it.amount < 0 }.sumOf { it.amount }
    }

    val formattedBalance = formatDouble(balance)

    var transactionToCancel by remember { mutableStateOf<Transaction?>(null) }

    if (transactionToCancel != null) {
        AlertDialog(
            onDismissRequest = { transactionToCancel = null },
            title = {
                Text(
                    text = if (lang == AppLanguage.RU) "Отмена операции" else "Cancel Operation",
                    color = ElegantTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = LanguageHelper.getString("cancelTxConfirm", lang),
                    color = ElegantTextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        transactionToCancel?.let { tx ->
                            viewModel.cancelTransaction(tx.id)
                        }
                        transactionToCancel = null
                    }
                ) {
                    Text(
                        text = if (lang == AppLanguage.RU) "Подтвердить" else "Confirm",
                        color = ColorSpend,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { transactionToCancel = null }
                ) {
                    Text(
                        text = if (lang == AppLanguage.RU) "Отмена" else "Cancel",
                        color = ElegantTextPrimary
                    )
                }
            },
            containerColor = ElegantCardBg,
            titleContentColor = ElegantTextPrimary,
            textContentColor = ElegantTextSecondary
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Header Row matching layout of top bar
        HeaderRow(viewModel = viewModel, lang = lang)

        Spacer(modifier = Modifier.height(4.dp))

        // Balance Summary Card featuring calculated incomes & expenses
        BalanceSummaryCard(
            balance = balance,
            formattedBalance = formattedBalance,
            totalIn = totalIn,
            totalOut = totalOut,
            goal = goal,
            lang = lang,
            viewModel = viewModel
        )

        Spacer(modifier = Modifier.height(4.dp))

        // History Label Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (lang == AppLanguage.RU) "История" else "History",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = ElegantTextPrimary
                )
            )
            val currentDateStr = remember {
                val sdf = SimpleDateFormat("d MMM, yyyy", Locale.getDefault())
                sdf.format(Date())
            }
            Text(
                text = currentDateStr,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ElegantTextSecondary,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        // Transactions scrolling feed area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = LanguageHelper.getString("historyEmpty", lang),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            color = ElegantTextSecondary.copy(alpha = 0.5f)
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                val sdf = remember { SimpleDateFormat("d MMMM yyyy", Locale.getDefault()) }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(transactions) { index, tx ->
                        TransactionItemRow(
                            tx = tx,
                            sdf = sdf,
                            lang = lang,
                            viewModel = viewModel,
                            onLongClick = { transactionToCancel = tx }
                        )
                    }
                }
            }
        }

        // Bottom action choices
        BottomControlsRow(viewModel = viewModel, lang = lang)
    }
}

@Composable
fun HeaderRow(viewModel: KopilkaViewModel, lang: AppLanguage) {
    val ElegantLavender = rememberPrimaryColor(viewModel)
    val ElegantBtnText = Color(0xFF1C1B1F)

    val unsyncedTxIds by viewModel.unsyncedTxIds.collectAsStateWithLifecycle()
    val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()
    val syncSuccessTrigger by viewModel.syncSuccessTrigger.collectAsStateWithLifecycle()
    val currentUserEmail by viewModel.currentUserEmail.collectAsStateWithLifecycle()
    
    val customEnabled by viewModel.customCurrencyEnabled.collectAsStateWithLifecycle()
    val customSymbol by viewModel.customCurrencySymbol.collectAsStateWithLifecycle()
    val displaySymbol = if (customEnabled && customSymbol.isNotEmpty()) customSymbol else "$"
    
    var showSyncDialog by remember { mutableStateOf(false) }

    if (showSyncDialog) {
        val syncDialogTitle = if (lang == AppLanguage.RU) "Синхронизация" else "Synchronization"
        val unsyncedMsg = if (lang == AppLanguage.RU) {
            "Некоторые записи в истории не синхронизированы с облаком."
        } else {
            "Some entries in history are not synchronized with the cloud."
        }
        val unsyncedCountMsg = if (lang == AppLanguage.RU) {
            "Количество несинхронизированных записей: ${unsyncedTxIds.size}"
        } else {
            "Number of unsynchronized entries: ${unsyncedTxIds.size}"
        }
        val syncBtnText = if (lang == AppLanguage.RU) "Синхронизировать" else "Synchronize"
        val closeBtnText = if (lang == AppLanguage.RU) "Закрыть" else "Close"

        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SyncProblem,
                        contentDescription = "Sync Info",
                        tint = Color(0xFFE53935)
                    )
                    Text(text = syncDialogTitle, color = ElegantTextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = unsyncedMsg, color = ElegantTextSecondary)
                    Text(
                        text = unsyncedCountMsg,
                        color = ElegantTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.syncData()
                        showSyncDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender)
                ) {
                    Text(text = syncBtnText, color = ElegantBtnText, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSyncDialog = false }
                ) {
                    Text(text = closeBtnText, color = ElegantLavender)
                }
            },
            containerColor = ElegantHeaderBg,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Elegant badge logo
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ElegantLavender),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(ElegantDarkBg),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = displaySymbol,
                        color = ElegantLavender,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                                includeFontPadding = false
                            ),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    )
                }
            }

            Text(
                text = "Kopilka",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = ElegantTextPrimary
                )
            )
        }

        // Action buttons (Sync indicator & Settings)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (syncSuccessTrigger) {
                // Successful sync display (non-clickable green cloud with checkmark)
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2E7D32)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Synced successfully",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else if (isSyncing) {
                // Loading spinner
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ElegantCardBg),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ElegantLavender,
                        strokeWidth = 2.dp
                    )
                }
            } else if (currentUserEmail != null && unsyncedTxIds.isNotEmpty()) {
                // Sync issue warning button
                IconButton(
                    onClick = { showSyncDialog = true },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFD32F2F))
                ) {
                    Icon(
                        imageVector = Icons.Default.SyncProblem,
                        contentDescription = "Sync warning",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Settings button
            IconButton(
                onClick = { viewModel.showSheet(SheetType.SETTINGS) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ElegantCardBg)
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = ElegantTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun BalanceSummaryCard(
    balance: Double,
    formattedBalance: String,
    totalIn: Double,
    totalOut: Double,
    goal: Double,
    lang: AppLanguage,
    viewModel: KopilkaViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantHeaderBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = if (lang == AppLanguage.RU) "Текущий баланс" else "Current Balance",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = ElegantLavender,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            val customEnabled by viewModel.customCurrencyEnabled.collectAsStateWithLifecycle()
            val customSymbol by viewModel.customCurrencySymbol.collectAsStateWithLifecycle()
            val balanceTextToShow = if (customEnabled && customSymbol.isNotEmpty()) {
                "$formattedBalance $customSymbol"
            } else {
                formattedBalance
            }

            Text(
                text = balanceTextToShow,
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElegantTextPrimary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Split metrics displaying dynamic totals
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == AppLanguage.RU) "ПРИХОД" else "IN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ElegantTextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "+ ${formatDouble(totalIn)}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = ColorTopUp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(ElegantTextSecondary.copy(alpha = 0.3f))
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (lang == AppLanguage.RU) "РАСХОД" else "OUT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = ElegantTextTertiary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    )
                    Text(
                        text = "- ${formatDouble(Math.abs(totalOut))}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = ColorSpend,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            val goalProgressHidden by viewModel.goalProgressHidden.collectAsStateWithLifecycle()

            if (!goalProgressHidden) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = ElegantTextSecondary.copy(alpha = 0.15f), thickness = 0.8.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Integrated goal display
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (goal <= 0.0) {
                        Text(
                            text = LanguageHelper.getString("goalUnset", lang),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ElegantTextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    } else {
                        val remaining = maxOf(0.0, goal - balance)
                        val formattedRemaining = formatDouble(remaining)
                        Text(
                            text = "$formattedRemaining ${LanguageHelper.getString("toTheGoal", lang)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ElegantTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )

                        val progressPercent = (balance / goal).coerceIn(0.0, 1.0) * 100
                        Text(
                            text = "${progressPercent.toInt()}%",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ElegantLavender,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Premium Goal progress bar
                val progress = if (goal <= 0.0) 0f else (balance / goal).coerceIn(0.0, 1.0).toFloat()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(ElegantDarkBg.copy(alpha = 0.5f), shape = RoundedCornerShape(3.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(progress)
                            .fillMaxHeight()
                            .background(Color(0xFF00FFCC), shape = RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionItemRow(
    tx: Transaction,
    sdf: SimpleDateFormat,
    lang: AppLanguage,
    viewModel: KopilkaViewModel,
    onLongClick: () -> Unit
) {
    val isIncome = tx.amount > 0
    val dateStr = sdf.format(Date(tx.timestamp))
    
    val customEnabled by viewModel.customCurrencyEnabled.collectAsStateWithLifecycle()
    val customSymbol by viewModel.customCurrencySymbol.collectAsStateWithLifecycle()
    
    val formattedValue = formatDouble(tx.amount)
    val baseAmountText = if (isIncome) "+$formattedValue" else formattedValue
    val amountText = if (customEnabled && customSymbol.isNotEmpty()) {
        "$baseAmountText $customSymbol"
    } else {
        baseAmountText
    }
    
    val amountColor = if (isIncome) ColorTopUp else ColorSpend

    val isStatsReset = tx.reason.startsWith("Статистика за") || tx.reason.startsWith("Statistics for")
    
    val displayReason: String
    val displaySubText: String
    if (isStatsReset && tx.reason.contains("|")) {
        val parts = tx.reason.split("|")
        displayReason = parts[0]
        displaySubText = parts[1]
    } else {
        displayReason = tx.reason
        displaySubText = dateStr
    }

    val isCancel = tx.reason.contains("Отмена") || tx.reason.contains("Cancellation")
    val isRecalc = tx.reason.contains("Перерасчёт") || tx.reason.contains("Recalculation")

    val iconBgColor = when {
        isStatsReset -> Color(0xFFC5CAE9)
        isCancel -> Color(0xFFD0BCFF)
        isRecalc -> Color(0xFFEADDFF)
        isIncome -> Color(0xFFE8DEF8)
        else -> Color(0xFFFFD8E4)
    }

    val iconColor = when {
        isStatsReset -> Color(0xFF1A237E)
        isCancel -> Color(0xFF381E72)
        isRecalc -> Color(0xFF21005D)
        isIncome -> Color(0xFF1D192B)
        else -> Color(0xFF31111D)
    }

    val iconVector = when {
        isStatsReset -> Icons.Default.Article
        isCancel -> Icons.AutoMirrored.Filled.Undo
        isRecalc -> Icons.Default.CompareArrows
        isIncome -> Icons.AutoMirrored.Filled.TrendingUp
        else -> Icons.AutoMirrored.Filled.TrendingDown
    }

    val iconRotation = 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
            .clip(RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = {},
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ElegantCardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Circular vector emblem
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = displayReason,
                        tint = iconColor,
                        modifier = Modifier
                            .size(20.dp)
                            .rotate(iconRotation)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayReason,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary
                        ),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = displaySubText,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ElegantTextSecondary
                        )
                    )
                }
            }

            if (!isStatsReset) {
                Text(
                    text = amountText,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = amountColor
                    ),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun BottomControlsRow(viewModel: KopilkaViewModel, lang: AppLanguage) {
    val ElegantLavender = rememberPrimaryColor(viewModel)
    val ElegantBtnText = Color(0xFF1C1B1F)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Spend Button (Coral Red with Down Arrow Icon)
            Button(
                onClick = { viewModel.showSheet(SheetType.SPEND) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorSpend),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingDown,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = LanguageHelper.getString("btnSpend", lang),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }

            // Top Up Button (Mint Green with Up Arrow Icon)
            Button(
                onClick = { viewModel.showSheet(SheetType.TOP_UP) },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorTopUp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = LanguageHelper.getString("btnTopUp", lang),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                )
            }
        }
    }
}

@Composable
fun LanguageSheetContent(viewModel: KopilkaViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Language/Язык",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = ElegantTextPrimary
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Choose your language\nВыберите язык",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = ElegantTextSecondary,
                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Note: The date language is determined by the language on the device.\n\nВажно: Дата будет отображаться на языке устройства.",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = ElegantTextSecondary.copy(alpha = 0.7f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(28.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    viewModel.setLanguage(AppLanguage.EN)
                    viewModel.hideSheet()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorLanguageEN),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "English",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF1C1B1F),
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Button(
                onClick = {
                    viewModel.setLanguage(AppLanguage.RU)
                    viewModel.hideSheet()
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ColorLanguageRU),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "Русский",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF1C1B1F),
                        fontWeight = FontWeight.Bold
                    )
                )
            }
        }
    }
}

@Composable
fun SettingsSheetContent(
    viewModel: KopilkaViewModel,
    onSaveClick: () -> Unit,
    onLoadClick: () -> Unit,
    lang: AppLanguage
) {
    val ElegantLavender = rememberPrimaryColor(viewModel)
    val ElegantBtnText = Color(0xFF1C1B1F)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = LanguageHelper.getString("settingsTitle", lang),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = ElegantTextPrimary
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        val buttonShape = RoundedCornerShape(16.dp)
        val buttonColors = ButtonDefaults.buttonColors(containerColor = ElegantLavender)
        val secondaryColors = ButtonDefaults.buttonColors(containerColor = ElegantHeaderBg)
        val textStyle = MaterialTheme.typography.titleMedium.copy(
            color = ElegantBtnText,
            fontWeight = FontWeight.Bold
        )
        val secondaryTextStyle = MaterialTheme.typography.titleMedium.copy(
            color = ElegantTextPrimary,
            fontWeight = FontWeight.SemiBold
        )

        // Set goal button
        Button(
            onClick = { viewModel.showSheet(SheetType.SET_GOAL) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = buttonColors,
            shape = buttonShape
        ) {
            Text(text = LanguageHelper.getString("setGoal", lang), style = textStyle)
        }

        // Count money button
        Button(
            onClick = { viewModel.showSheet(SheetType.COUNT_MONEY) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = buttonColors,
            shape = buttonShape
        ) {
            Text(text = LanguageHelper.getString("countMoney", lang), style = textStyle)
        }

        // Save history button
        Button(
            onClick = onSaveClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = secondaryColors,
            shape = buttonShape
        ) {
            Text(text = LanguageHelper.getString("saveHistory", lang), style = secondaryTextStyle)
        }

        // Load history button
        Button(
            onClick = onLoadClick,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = secondaryColors,
            shape = buttonShape
        ) {
            Text(text = LanguageHelper.getString("loadHistory", lang), style = secondaryTextStyle)
        }

        // Change language button
        Button(
            onClick = { viewModel.showSheet(SheetType.LANGUAGE) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF35343A)),
            shape = buttonShape
        ) {
            Text(text = LanguageHelper.getString("changeLanguage", lang), style = secondaryTextStyle.copy(color = ElegantLavender))
        }

        // About app button
        Button(
            onClick = { viewModel.showSheet(SheetType.ABOUT_APP) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF35343A)),
            shape = buttonShape
        ) {
            Text(text = LanguageHelper.getString("aboutApp", lang), style = secondaryTextStyle.copy(color = ElegantLavender))
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Account and Cloud Synchronization settings
        val currentUserEmail by viewModel.currentUserEmail.collectAsStateWithLifecycle()
        val authLoading by viewModel.authLoading.collectAsStateWithLifecycle()
        val authError by viewModel.authError.collectAsStateWithLifecycle()
        val isSyncing by viewModel.isSyncing.collectAsStateWithLifecycle()

        var isRegisterMode by remember { mutableStateOf(false) }
        var emailInput by remember { mutableStateOf("") }
        var passwordInput by remember { mutableStateOf("") }

        val context = LocalContext.current

        // Configure Google Sign-In helper launcher
        val googleSignInOptions = remember {
            val webClientIdResId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
            val webClientId = if (webClientIdResId != 0) {
                context.getString(webClientIdResId)
            } else {
                "1045595440156-ujf4j2u425damv6buk2qo5bqikmkpm3s.apps.googleusercontent.com"
            }
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()
        }
        val googleSignInClient = remember { GoogleSignIn.getClient(context, googleSignInOptions) }
        val googleLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    viewModel.loginWithGoogle(idToken)
                } else {
                    viewModel.showToast(if (lang == AppLanguage.RU) "Google вход: Нет ID токена" else "Google login: No ID Token")
                }
            } catch (e: Exception) {
                val errMsg = e.localizedMessage ?: ""
                if (errMsg.contains("10") || errMsg.contains("DEVELOPER_ERROR")) {
                    viewModel.showToast(
                        if (lang == AppLanguage.RU) 
                            "Для Google входа нужен CLIENT_ID. Пожалуйста, используйте вход по Почте и Паролю."
                        else 
                            "Google Sign-In requires CLIENT_ID. Please use Email and Password login instead."
                    )
                } else {
                    viewModel.showToast(
                        if (lang == AppLanguage.RU) "Ошибка Google входа: ${e.localizedMessage}" else "Google login failed: ${e.localizedMessage}"
                    )
                }
            }
        }

        val accountTitle = if (lang == AppLanguage.RU) "Синхронизация и Облако" else "Cloud Sync & Account"
        val emailLabel = if (lang == AppLanguage.RU) "Эл. почта" else "Email"
        val passwordLabel = if (lang == AppLanguage.RU) "Пароль" else "Password"
        val btnLoginText = if (lang == AppLanguage.RU) "Войти" else "Log In"
        val btnRegisterText = if (lang == AppLanguage.RU) "Зарегистрироваться" else "Register"
        val toggleRegisterText = if (lang == AppLanguage.RU) "Нет аккаунта? Зарегистрироваться" else "Don't have an account? Register"
        val toggleLoginText = if (lang == AppLanguage.RU) "Уже есть аккаунт? Войти" else "Already have an account? Log In"
        val signedInText = if (lang == AppLanguage.RU) "Вы вошли как:" else "Signed in as:"
        val signOutText = if (lang == AppLanguage.RU) "Выйти" else "Sign Out"
        val syncNowText = if (lang == AppLanguage.RU) "Синхронизировать" else "Sync Now"

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantDarkBg, shape = RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = accountTitle,
                style = MaterialTheme.typography.titleMedium.copy(
                    color = ElegantTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            )

            if (currentUserEmail != null) {
                // User is signed in
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Signed In Indicator",
                        tint = Color(0xFF00FFCC),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "$signedInText $currentUserEmail",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = ElegantTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.syncData() },
                        enabled = !isSyncing,
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ElegantBtnText)
                        } else {
                            Text(text = syncNowText, color = ElegantBtnText, fontWeight = FontWeight.Bold)
                        }
                    }

                    Button(
                        onClick = { viewModel.signOut() },
                        modifier = Modifier.weight(1f).height(40.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF2B8B5)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = signOutText, color = Color(0xFF601410), fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // User is not signed in - show login form
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text(text = emailLabel, color = ElegantTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ElegantTextPrimary,
                        unfocusedTextColor = ElegantTextPrimary,
                        focusedBorderColor = ElegantLavender,
                        unfocusedBorderColor = ElegantTextSecondary,
                        focusedContainerColor = ElegantHeaderBg,
                        unfocusedContainerColor = ElegantHeaderBg
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text(text = passwordLabel, color = ElegantTextSecondary) },
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ElegantTextPrimary,
                        unfocusedTextColor = ElegantTextPrimary,
                        focusedBorderColor = ElegantLavender,
                        unfocusedBorderColor = ElegantTextSecondary,
                        focusedContainerColor = ElegantHeaderBg,
                        unfocusedContainerColor = ElegantHeaderBg
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (authError != null) {
                    Text(
                        text = authError ?: "",
                        color = Color(0xFFE53935),
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            if (emailInput.isBlank() || passwordInput.isBlank()) {
                                viewModel.showToast(if (lang == AppLanguage.RU) "Заполните все поля" else "Please fill all fields")
                                return@Button
                            }
                            if (isRegisterMode) {
                                viewModel.signUpWithEmail(emailInput, passwordInput)
                            } else {
                                viewModel.loginWithEmail(emailInput, passwordInput)
                            }
                        },
                        enabled = !authLoading,
                        modifier = Modifier.weight(1f).height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (authLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = ElegantBtnText)
                        } else {
                            Text(
                                text = if (isRegisterMode) btnRegisterText else btnLoginText,
                                color = ElegantBtnText,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Google Sign-In button
                    Button(
                        onClick = {
                            try {
                                val signInIntent = googleSignInClient.signInIntent
                                googleLauncher.launch(signInIntent)
                            } catch (e: Exception) {
                                viewModel.showToast("Google Sign-In initialization failed: ${e.localizedMessage}")
                            }
                        },
                        modifier = Modifier.weight(1f).height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantHeaderBg),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CompareArrows,
                                contentDescription = "Google Sign In",
                                tint = ElegantLavender,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(text = "Google", color = ElegantTextPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Toggle register/login mode link
                Text(
                    text = if (isRegisterMode) toggleLoginText else toggleRegisterText,
                    color = ElegantLavender,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    modifier = Modifier
                        .clickable { isRegisterMode = !isRegisterMode }
                        .padding(vertical = 4.dp)
                        .align(Alignment.CenterHorizontally)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Statistics Reset Interval settings
        val currentFreq by viewModel.statsResetFrequency.collectAsStateWithLifecycle()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantDarkBg, shape = RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = LanguageHelper.getString("statsResetFrequency", lang),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = ElegantTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )

            val frequencies = listOf(
                StatsResetFrequency.MANUAL to "resetManual",
                StatsResetFrequency.DAILY to "resetDaily",
                StatsResetFrequency.WEEKLY to "resetWeekly",
                StatsResetFrequency.MONTHLY to "resetMonthly"
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                frequencies.forEach { (freq, key) ->
                    val isSelected = currentFreq == freq
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isSelected) ElegantLavender else ElegantHeaderBg)
                            .clickable {
                                viewModel.setStatsResetFrequency(freq)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = LanguageHelper.getString(key, lang),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = if (isSelected) ElegantBtnText else ElegantTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Button(
                onClick = {
                    viewModel.performStatsReset()
                    viewModel.showToast(LanguageHelper.getString("success", lang))
                },
                modifier = Modifier.fillMaxWidth().height(40.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantHeaderBg),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = LanguageHelper.getString("btnResetStatsNow", lang),
                    color = ElegantLavender,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // App Theme Color settings
        val currentTheme by viewModel.currentTheme.collectAsStateWithLifecycle()
        val customThemeColor by viewModel.customThemeColor.collectAsStateWithLifecycle()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantDarkBg, shape = RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = LanguageHelper.getString("themeTitle", lang),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = ElegantTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )

            // Expandable Preset Colors Grid
            var colorsExpanded by remember { mutableStateOf(false) }
            val presetsRow1 = remember {
                listOf(
                    AppTheme.VIOLET, AppTheme.TEAL, AppTheme.AMBER, AppTheme.EMERALD, AppTheme.ROSE
                )
            }
            val presetsRow2 = remember {
                listOf(
                    AppTheme.BLUE, AppTheme.RED, AppTheme.INDIGO, AppTheme.ORANGE, AppTheme.PURPLE
                )
            }
            val presetsRow3 = remember {
                listOf(
                    AppTheme.CYAN, AppTheme.LIME, AppTheme.PEACH, AppTheme.MINT, AppTheme.LAVENDER
                )
            }
            val presetsRow4 = remember {
                listOf(
                    AppTheme.SKY, AppTheme.MAGENTA, AppTheme.GOLD, AppTheme.SAPPHIRE, AppTheme.BRONZE
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    presetsRow1.forEach { theme ->
                        val isThemeSelected = currentTheme == theme
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(theme.primaryColor)
                                .border(
                                    width = if (isThemeSelected) 3.dp else 0.dp,
                                    color = if (isThemeSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    viewModel.setTheme(theme)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isThemeSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = ElegantDarkBg,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    presetsRow2.forEach { theme ->
                        val isThemeSelected = currentTheme == theme
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(theme.primaryColor)
                                .border(
                                    width = if (isThemeSelected) 3.dp else 0.dp,
                                    color = if (isThemeSelected) Color.White else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable {
                                    viewModel.setTheme(theme)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isThemeSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = ElegantDarkBg,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                if (colorsExpanded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presetsRow3.forEach { theme ->
                            val isThemeSelected = currentTheme == theme
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(theme.primaryColor)
                                    .border(
                                        width = if (isThemeSelected) 3.dp else 0.dp,
                                        color = if (isThemeSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.setTheme(theme)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isThemeSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = ElegantDarkBg,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        presetsRow4.forEach { theme ->
                            val isThemeSelected = currentTheme == theme
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(theme.primaryColor)
                                    .border(
                                        width = if (isThemeSelected) 3.dp else 0.dp,
                                        color = if (isThemeSelected) Color.White else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        viewModel.setTheme(theme)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isThemeSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = ElegantDarkBg,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Show More/Less toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { colorsExpanded = !colorsExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (colorsExpanded) {
                            LanguageHelper.getString("showLessColors", lang)
                        } else {
                            LanguageHelper.getString("showMoreColors", lang)
                        },
                        color = ElegantLavender,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = if (colorsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = ElegantLavender,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Material You Selection Button
            val isMaterialYouSelected = currentTheme == AppTheme.MATERIAL_YOU
            OutlinedButton(
                onClick = { viewModel.setTheme(AppTheme.MATERIAL_YOU) },
                modifier = Modifier.fillMaxWidth(),
                border = androidx.compose.foundation.BorderStroke(
                    width = if (isMaterialYouSelected) 2.dp else 1.dp,
                    color = if (isMaterialYouSelected) ElegantLavender else ElegantTextSecondary
                ),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (isMaterialYouSelected) ElegantLavender.copy(alpha = 0.12f) else Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = if (isMaterialYouSelected) Icons.Default.Check else Icons.Default.Cloud,
                    contentDescription = null,
                    tint = if (isMaterialYouSelected) ElegantLavender else ElegantTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LanguageHelper.getString("themeMaterialYou", lang),
                    color = if (isMaterialYouSelected) ElegantLavender else ElegantTextPrimary,
                    fontWeight = if (isMaterialYouSelected) FontWeight.Bold else FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Custom Color Slider Area (Triggered when CUSTOM selected or adjusted)
            val isCustomSelected = currentTheme == AppTheme.CUSTOM
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (isCustomSelected) 2.dp else 1.dp,
                        color = if (isCustomSelected) ElegantLavender else ElegantTextSecondary.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .background(ElegantHeaderBg.copy(alpha = 0.2f), shape = RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = LanguageHelper.getString("customColorPicker", lang),
                        color = ElegantTextPrimary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                    )
                    // Visual preview of current custom color
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(customThemeColor)
                            .border(1.dp, Color.White, CircleShape)
                            .clickable {
                                viewModel.setTheme(AppTheme.CUSTOM)
                            }
                    )
                }

                // RGB sliders
                var rVal by remember(customThemeColor) { mutableStateOf(customThemeColor.red) }
                var gVal by remember(customThemeColor) { mutableStateOf(customThemeColor.green) }
                var bVal by remember(customThemeColor) { mutableStateOf(customThemeColor.blue) }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Red Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "R", color = Color(0xFFEF9A9A), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(12.dp))
                        Slider(
                            value = rVal,
                            onValueChange = {
                                rVal = it
                                viewModel.setCustomThemeColor(Color(rVal, gVal, bVal))
                            },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFEF9A9A),
                                activeTrackColor = Color(0xFFEF9A9A)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = (rVal * 255).toInt().toString(), color = ElegantTextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(24.dp))
                    }

                    // Green Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "G", color = Color(0xFFA5D6A7), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(12.dp))
                        Slider(
                            value = gVal,
                            onValueChange = {
                                gVal = it
                                viewModel.setCustomThemeColor(Color(rVal, gVal, bVal))
                            },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFA5D6A7),
                                activeTrackColor = Color(0xFFA5D6A7)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = (gVal * 255).toInt().toString(), color = ElegantTextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(24.dp))
                    }

                    // Blue Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(text = "B", color = Color(0xFF9FA8DA), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(12.dp))
                        Slider(
                            value = bVal,
                            onValueChange = {
                                bVal = it
                                viewModel.setCustomThemeColor(Color(rVal, gVal, bVal))
                            },
                            valueRange = 0f..1f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFF9FA8DA),
                                activeTrackColor = Color(0xFF9FA8DA)
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = (bVal * 255).toInt().toString(), color = ElegantTextSecondary, style = MaterialTheme.typography.bodySmall, modifier = Modifier.width(24.dp))
                    }
                }
            }

            val themeNameKey = when (currentTheme) {
                AppTheme.VIOLET -> "themeViolet"
                AppTheme.TEAL -> "themeTeal"
                AppTheme.AMBER -> "themeAmber"
                AppTheme.EMERALD -> "themeEmerald"
                AppTheme.ROSE -> "themeRose"
                AppTheme.BLUE -> "themeBlue"
                AppTheme.RED -> "themeRed"
                AppTheme.INDIGO -> "themeIndigo"
                AppTheme.ORANGE -> "themeOrange"
                AppTheme.PURPLE -> "themePurple"
                AppTheme.CYAN -> "themeCyan"
                AppTheme.LIME -> "themeLime"
                AppTheme.PEACH -> "themePeach"
                AppTheme.MINT -> "themeMint"
                AppTheme.LAVENDER -> "themeLavender"
                AppTheme.SKY -> "themeSky"
                AppTheme.MAGENTA -> "themeMagenta"
                AppTheme.GOLD -> "themeGold"
                AppTheme.SAPPHIRE -> "themeSapphire"
                AppTheme.BRONZE -> "themeBronze"
                AppTheme.MATERIAL_YOU -> "themeMaterialYou"
                AppTheme.CUSTOM -> "themeCustom"
            }
            Text(
                text = LanguageHelper.getString(themeNameKey, lang),
                color = ElegantLavender,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // App Launcher Icon Settings with Theme Color Preview
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantDarkBg, shape = RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = LanguageHelper.getString("customAppIconSettings", lang),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = ElegantTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            )
            
            Text(
                text = LanguageHelper.getString("launcherIconColor", lang),
                color = ElegantTextSecondary,
                style = MaterialTheme.typography.bodySmall
            )

            // Dynamic Icon Color Preview matching the main screen's beautiful style
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(currentTheme.primaryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(ElegantDarkBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$",
                                color = currentTheme.primaryColor,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                style = androidx.compose.ui.text.TextStyle(
                                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                                        includeFontPadding = false
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            )
                        }
                    }
                    Text(
                        text = "Kopilka",
                        color = ElegantTextPrimary,
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                    )
                }
            }

            Button(
                onClick = { viewModel.applyLauncherIcon() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElegantLavender,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = LanguageHelper.getString("applyIconBtn", lang),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Custom currency settings
        val customEnabled by viewModel.customCurrencyEnabled.collectAsStateWithLifecycle()
        val customSymbol by viewModel.customCurrencySymbol.collectAsStateWithLifecycle()
        var symbolInput by remember { mutableStateOf(customSymbol) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantDarkBg, shape = RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageHelper.getString("customCurrency", lang),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = ElegantTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Switch(
                    checked = customEnabled,
                    onCheckedChange = { isChecked ->
                        viewModel.setCustomCurrencyEnabled(isChecked)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ElegantLavender,
                        checkedTrackColor = ElegantHeaderBg,
                        uncheckedThumbColor = ElegantTextSecondary,
                        uncheckedTrackColor = ElegantDarkBg
                    )
                )
            }

            if (customEnabled) {
                OutlinedTextField(
                    value = symbolInput,
                    onValueChange = { newVal ->
                        symbolInput = newVal
                        viewModel.setCustomCurrencySymbol(newVal)
                    },
                    label = { Text(text = LanguageHelper.getString("customCurrencySymbolLabel", lang), color = ElegantTextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = ElegantTextPrimary,
                        unfocusedTextColor = ElegantTextPrimary,
                        focusedBorderColor = ElegantLavender,
                        unfocusedBorderColor = ElegantTextSecondary,
                        focusedContainerColor = ElegantHeaderBg,
                        unfocusedContainerColor = ElegantHeaderBg
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Goal visibility settings
        val goalProgressHidden by viewModel.goalProgressHidden.collectAsStateWithLifecycle()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantDarkBg, shape = RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = LanguageHelper.getString("hideGoalProgress", lang),
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = ElegantTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Switch(
                    checked = goalProgressHidden,
                    onCheckedChange = { isChecked ->
                        viewModel.setGoalProgressHidden(isChecked)
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = ElegantLavender,
                        checkedTrackColor = ElegantHeaderBg,
                        uncheckedThumbColor = ElegantTextSecondary,
                        uncheckedTrackColor = ElegantDarkBg
                    )
                )
            }
        }

        // Clear History button with confirmation dialog
        var showClearConfirm by remember { mutableStateOf(false) }

        if (showClearConfirm) {
            AlertDialog(
                onDismissRequest = { showClearConfirm = false },
                title = {
                    Text(
                        text = LanguageHelper.getString("clearHistory", lang),
                        color = ElegantTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(
                        text = LanguageHelper.getString("clearHistoryConfirm", lang),
                        color = ElegantTextSecondary
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearHistory()
                            showClearConfirm = false
                            viewModel.hideSheet()
                        }
                    ) {
                        Text(
                            text = if (lang == AppLanguage.RU) "Да, очистить" else "Yes, clear",
                            color = ColorSpend,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showClearConfirm = false }
                    ) {
                        Text(
                            text = LanguageHelper.getString("cancel", lang),
                            color = ElegantTextPrimary
                        )
                    }
                },
                containerColor = ElegantCardBg,
                titleContentColor = ElegantTextPrimary,
                textContentColor = ElegantTextSecondary
            )
        }

        Button(
            onClick = { showClearConfirm = true },
            modifier = Modifier.fillMaxWidth().height(48.dp).padding(top = 8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorSpend.copy(alpha = 0.8f)),
            shape = buttonShape
        ) {
            Text(text = LanguageHelper.getString("clearHistory", lang), style = secondaryTextStyle.copy(color = Color.Black))
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SpendSheetContent(viewModel: KopilkaViewModel, lang: AppLanguage) {
    var amountText by remember { mutableStateOf("") }
    var reasonText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = LanguageHelper.getString("btnSpend", lang),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = ElegantTextPrimary
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text(text = LanguageHelper.getString("amount", lang), color = ElegantTextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ElegantTextPrimary,
                unfocusedTextColor = ElegantTextPrimary,
                focusedBorderColor = ColorSpend,
                unfocusedBorderColor = ElegantTextSecondary,
                focusedContainerColor = ElegantDarkBg,
                unfocusedContainerColor = ElegantDarkBg
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = reasonText,
            onValueChange = { reasonText = it },
            label = { Text(text = LanguageHelper.getString("reason", lang), color = ElegantTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ElegantTextPrimary,
                unfocusedTextColor = ElegantTextPrimary,
                focusedBorderColor = ColorSpend,
                unfocusedBorderColor = ElegantTextSecondary,
                focusedContainerColor = ElegantDarkBg,
                unfocusedContainerColor = ElegantDarkBg
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    val amt = amountText.replace(',', '.').toDoubleOrNull()?.let { Math.round(it * 100.0) / 100.0 }
                    if (amt == null || amt <= 0.0 || amt.isNaN() || amt.isInfinite()) {
                        viewModel.showToast(LanguageHelper.getString("invalidAmount", lang))
                    } else if (amt > 1_000_000_000.0) {
                        viewModel.showToast(LanguageHelper.getString("amountTooLarge", lang))
                    } else {
                        viewModel.addTransaction(-amt, reasonText)
                        viewModel.hideSheet()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantHeaderBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm",
                    tint = ColorTopUp,
                    modifier = Modifier.size(28.dp)
                )
            }

            Button(
                onClick = { viewModel.hideSheet() },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantHeaderBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = ColorSpend,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun TopUpSheetContent(viewModel: KopilkaViewModel, lang: AppLanguage) {
    var amountText by remember { mutableStateOf("") }
    var reasonText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = LanguageHelper.getString("btnTopUp", lang),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = ElegantTextPrimary
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text(text = LanguageHelper.getString("amount", lang), color = ElegantTextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ElegantTextPrimary,
                unfocusedTextColor = ElegantTextPrimary,
                focusedBorderColor = ColorTopUp,
                unfocusedBorderColor = ElegantTextSecondary,
                focusedContainerColor = ElegantDarkBg,
                unfocusedContainerColor = ElegantDarkBg
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        OutlinedTextField(
            value = reasonText,
            onValueChange = { reasonText = it },
            label = { Text(text = LanguageHelper.getString("reason", lang), color = ElegantTextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ElegantTextPrimary,
                unfocusedTextColor = ElegantTextPrimary,
                focusedBorderColor = ColorTopUp,
                unfocusedBorderColor = ElegantTextSecondary,
                focusedContainerColor = ElegantDarkBg,
                unfocusedContainerColor = ElegantDarkBg
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    val amt = amountText.replace(',', '.').toDoubleOrNull()?.let { Math.round(it * 100.0) / 100.0 }
                    if (amt == null || amt <= 0.0 || amt.isNaN() || amt.isInfinite()) {
                        viewModel.showToast(LanguageHelper.getString("invalidAmount", lang))
                    } else if (amt > 1_000_000_000.0) {
                        viewModel.showToast(LanguageHelper.getString("amountTooLarge", lang))
                    } else {
                        viewModel.addTransaction(amt, reasonText)
                        viewModel.hideSheet()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantHeaderBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm",
                    tint = ColorTopUp,
                    modifier = Modifier.size(28.dp)
                )
            }

            Button(
                onClick = { viewModel.hideSheet() },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantHeaderBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = ColorSpend,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun SetGoalSheetContent(viewModel: KopilkaViewModel, lang: AppLanguage) {
    val currentGoal by viewModel.goal.collectAsStateWithLifecycle()
    var goalText by remember { mutableStateOf(if (currentGoal > 0) formatInputAmount(currentGoal) else "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = LanguageHelper.getString("setGoal", lang),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = ElegantTextPrimary
            )
        )

        OutlinedTextField(
            value = goalText,
            onValueChange = { goalText = it },
            label = { Text(text = LanguageHelper.getString("inputGoal", lang), color = ElegantTextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ElegantTextPrimary,
                unfocusedTextColor = ElegantTextPrimary,
                focusedBorderColor = ElegantLavender,
                unfocusedBorderColor = ElegantTextSecondary,
                focusedContainerColor = ElegantDarkBg,
                unfocusedContainerColor = ElegantDarkBg
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    val amt = goalText.replace(',', '.').toDoubleOrNull()?.let { Math.round(it * 100.0) / 100.0 }
                    if (amt == null || amt < 0.0 || amt.isNaN() || amt.isInfinite()) {
                        viewModel.showToast(LanguageHelper.getString("invalidAmount", lang))
                    } else if (amt > 1_000_000_000.0) {
                        viewModel.showToast(LanguageHelper.getString("amountTooLarge", lang))
                    } else {
                        viewModel.setGoal(amt)
                        viewModel.showToast(LanguageHelper.getString("success", lang))
                        viewModel.showSheet(SheetType.SETTINGS)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantHeaderBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm",
                    tint = ColorTopUp,
                    modifier = Modifier.size(28.dp)
                )
            }

            Button(
                onClick = { viewModel.showSheet(SheetType.SETTINGS) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantHeaderBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = ColorSpend,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun CountMoneySheetContent(viewModel: KopilkaViewModel, lang: AppLanguage) {
    val currentBalance by viewModel.balance.collectAsStateWithLifecycle()
    var balanceText by remember { mutableStateOf(formatInputAmount(currentBalance)) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = LanguageHelper.getString("countMoney", lang),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = ElegantTextPrimary
            )
        )

        OutlinedTextField(
            value = balanceText,
            onValueChange = { balanceText = it },
            label = { Text(text = LanguageHelper.getString("inputInitial", lang), color = ElegantTextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ElegantTextPrimary,
                unfocusedTextColor = ElegantTextPrimary,
                focusedBorderColor = ElegantLavender,
                unfocusedBorderColor = ElegantTextSecondary,
                focusedContainerColor = ElegantDarkBg,
                unfocusedContainerColor = ElegantDarkBg
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    val amt = balanceText.replace(',', '.').toDoubleOrNull()?.let { Math.round(it * 100.0) / 100.0 }
                    if (amt == null || amt.isNaN() || amt.isInfinite()) {
                        viewModel.showToast(LanguageHelper.getString("invalidAmount", lang))
                    } else if (kotlin.math.abs(amt) > 1_000_000_000.0) {
                        viewModel.showToast(LanguageHelper.getString("amountTooLarge", lang))
                    } else {
                        viewModel.setBalance(amt)
                        viewModel.showToast(LanguageHelper.getString("success", lang))
                        viewModel.showSheet(SheetType.SETTINGS)
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantHeaderBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Confirm",
                    tint = ColorTopUp,
                    modifier = Modifier.size(28.dp)
                )
            }

            Button(
                onClick = { viewModel.showSheet(SheetType.SETTINGS) },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantHeaderBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = ColorSpend,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun AboutAppSheetContent(viewModel: KopilkaViewModel, lang: AppLanguage) {
    val ElegantLavender = rememberPrimaryColor(viewModel)
    val ElegantBtnText = Color(0xFF1C1B1F)
    val updateState by viewModel.updateState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val currentVersion = com.example.BuildConfig.VERSION_NAME

    LaunchedEffect(Unit) {
        viewModel.checkForUpdates()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = LanguageHelper.getString("aboutAppTitle", lang),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = ElegantTextPrimary
            ),
            textAlign = TextAlign.Center
        )

        Text(
            text = LanguageHelper.getString("aboutAppText", lang),
            style = MaterialTheme.typography.bodyLarge.copy(
                color = ElegantTextPrimary.copy(alpha = 0.85f),
                lineHeight = 22.sp
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 4.dp)
        )

        Text(
            text = "${LanguageHelper.getString("appVersion", lang)}: $currentVersion",
            style = MaterialTheme.typography.bodyMedium.copy(
                color = ElegantTextSecondary,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = ElegantDarkBg
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                when (val state = updateState) {
                    is UpdateState.Idle -> {
                        Button(
                            onClick = { viewModel.checkForUpdates() },
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = ElegantBtnText,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = LanguageHelper.getString("checkForUpdates", lang),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = ElegantBtnText,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    is UpdateState.Checking -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 8.dp)
                        ) {
                            CircularProgressIndicator(
                                color = ElegantLavender,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = LanguageHelper.getString("checkingUpdates", lang),
                                color = ElegantTextPrimary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    is UpdateState.UpdateAvailable -> {
                        Text(
                            text = "${LanguageHelper.getString("updateAvailable", lang)} (${state.latestVersion})",
                            color = Color(0xFFFFB74D),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = {
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(state.downloadUrl))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB74D)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = LanguageHelper.getString("downloadUpdate", lang),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color(0xFF1C1B1F),
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    is UpdateState.UpToDate -> {
                        Text(
                            text = LanguageHelper.getString("upToDate", lang),
                            color = Color(0xFF81C784),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        TextButton(
                            onClick = { viewModel.checkForUpdates() }
                        ) {
                            Text(
                                text = LanguageHelper.getString("checkForUpdates", lang),
                                color = ElegantLavender,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                    is UpdateState.Error -> {
                        Text(
                            text = "${LanguageHelper.getString("errorCheckingUpdates", lang)}: ${state.message}",
                            color = Color(0xFFE57373),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Button(
                            onClick = { viewModel.checkForUpdates() },
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = LanguageHelper.getString("checkForUpdates", lang),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = ElegantBtnText,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = { viewModel.showSheet(SheetType.SETTINGS) },
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .height(48.dp)
                .padding(bottom = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "OK",
                style = MaterialTheme.typography.titleMedium.copy(
                    color = ElegantBtnText,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

fun formatDouble(value: Double): String {
    val rounded = Math.round(value * 100.0) / 100.0
    return if (rounded % 1.0 == 0.0) {
        String.format(Locale.US, "%,d", rounded.toLong())
    } else {
        val str = String.format(Locale.US, "%,.2f", rounded)
        if (str.endsWith("0")) str.dropLast(1) else str
    }
}

fun formatInputAmount(value: Double): String {
    val rounded = Math.round(value * 100.0) / 100.0
    return if (rounded % 1.0 == 0.0) {
        rounded.toLong().toString()
    } else {
        val str = String.format(Locale.US, "%.2f", rounded)
        if (str.endsWith("0")) str.dropLast(1) else str
    }
}
