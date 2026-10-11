package com.example.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Transaction
import com.example.model.ExpenseCategory
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(
    viewModel: KopilkaViewModel,
    lang: AppLanguage,
    tutorialStep: Int = 0,
    onTutorialTarget: (Int, Float) -> Unit = { _, _ -> }
) {
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
                    text = LanguageHelper.getString("cancelOperationTitle", lang),
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
                        text = LanguageHelper.getString("confirm", lang),
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // App Header Row matching layout of top bar
        HeaderRow(viewModel = viewModel, lang = lang, tutorialStep = tutorialStep, onTutorialTarget = onTutorialTarget)

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
                text = LanguageHelper.getString("historyTitle", lang),
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
        BottomControlsRow(viewModel = viewModel, lang = lang, tutorialStep = tutorialStep, onTutorialTarget = onTutorialTarget)
    }
}

@Composable
fun HeaderRow(
    viewModel: KopilkaViewModel,
    lang: AppLanguage,
    tutorialStep: Int = 0,
    onTutorialTarget: (Int, Float) -> Unit = { _, _ -> }
) {
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
        val syncDialogTitle = LanguageHelper.getString("syncDialogTitle", lang)
        val unsyncedMsg = LanguageHelper.getString("unsyncedEntriesMessage", lang)
        val unsyncedCountMsg = LanguageHelper.getString("unsyncedEntriesCount", lang)
            .replace("{count}", unsyncedTxIds.size.toString())
        val syncBtnText = LanguageHelper.getString("syncDialogButton", lang)
        val closeBtnText = LanguageHelper.getString("closeButton", lang)

        AlertDialog(
            onDismissRequest = { showSyncDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SyncProblem,
                        contentDescription = LanguageHelper.getString("syncInfoDescription", lang),
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
            horizontalArrangement = Arrangement.spacedBy(12.dp)
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
                        contentDescription = LanguageHelper.getString("syncedSuccessfullyDescription", lang),
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
            } else if (!com.example.BuildConfig.DEBUG && currentUserEmail != null && unsyncedTxIds.isNotEmpty()) {
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
                        contentDescription = LanguageHelper.getString("syncWarningDescription", lang),
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else if (com.example.BuildConfig.DEBUG && currentUserEmail != null) {
                // Cloud sync disabled in test build indicator
                IconButton(
                    onClick = {
                        viewModel.showToast(LanguageHelper.getString("syncDisabledInTestBuild", lang))
                    },
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(ElegantCardBg)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = LanguageHelper.getString("cloudSyncDisabledDescription", lang),
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Settings button
            IconButton(
                onClick = { viewModel.navigateTo(AppScreen.SETTINGS) },
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(ElegantCardBg)
                    .onGloballyPositioned {
                        if (tutorialStep == 7) {
                            onTutorialTarget(7, it.boundsInRoot().center.y)
                        }
                    }
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = LanguageHelper.getString("settingsIconDescription", lang),
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
                text = LanguageHelper.getString("currentBalance", lang),
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

            val debts by viewModel.debts.collectAsStateWithLifecycle()
            val totalIOwe = remember(debts) {
                debts.filter { it.type == com.example.model.DebtType.I_OWE }.sumOf { it.amount }
            }
            val totalOwedToMe = remember(debts) {
                debts.filter { it.type == com.example.model.DebtType.OWED_TO_ME }.sumOf { it.amount }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = balanceTextToShow,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElegantTextPrimary
                    )
                )

                if (totalIOwe > 0.0 || totalOwedToMe > 0.0) {
                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.navigateTo(AppScreen.DEBTS) }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        if (totalOwedToMe > 0.0) {
                            val owedFormatted = if (customEnabled && customSymbol.isNotEmpty()) {
                                "${formatDouble(totalOwedToMe)} $customSymbol"
                            } else {
                                formatDouble(totalOwedToMe)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = LanguageHelper.getString("toYouLabel", lang),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ColorTopUp,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = owedFormatted,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ColorTopUp,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                        if (totalIOwe > 0.0) {
                            val oweFormatted = if (customEnabled && customSymbol.isNotEmpty()) {
                                "${formatDouble(totalIOwe)} $customSymbol"
                            } else {
                                formatDouble(totalIOwe)
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = LanguageHelper.getString("youOweLabel", lang),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ColorSpend,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                )
                                Text(
                                    text = oweFormatted,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = ColorSpend,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Split metrics displaying dynamic totals
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = LanguageHelper.getString("balanceIncomeLabel", lang).uppercase(),
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
                        text = LanguageHelper.getString("expenseSummaryLabel", lang).uppercase(),
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
    val isCancel = tx.reason.contains("Отмена") || tx.reason.contains("Cancellation")
    val isRecalc = tx.reason.contains("Перерасчёт") || tx.reason.contains("Recalculation")

    val expenseCategory = if (!isIncome && !isStatsReset && !isCancel && !isRecalc) {
        ExpenseCategory.fromId(tx.category)
    } else null

    val displayReason: String
    val displaySubText: String
    if (isStatsReset && tx.reason.contains("|")) {
        val parts = tx.reason.split("|")
        displayReason = parts[0]
        displaySubText = parts[1]
    } else if (expenseCategory != null) {
        val catTitle = expenseCategory.getTitle(lang)
        val noDesc = tx.reason.isEmpty() || tx.reason == "Без описания" || tx.reason == "No description"
        if (noDesc && tx.category != null) {
            displayReason = catTitle
            displaySubText = dateStr
        } else {
            displayReason = tx.reason
            displaySubText = "$catTitle • $dateStr"
        }
    } else {
        displayReason = tx.reason
        displaySubText = dateStr
    }

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
        expenseCategory != null -> expenseCategory.icon
        else -> ExpenseCategory.OTHER.icon
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
fun BottomControlsRow(
    viewModel: KopilkaViewModel,
    lang: AppLanguage,
    tutorialStep: Int = 0,
    onTutorialTarget: (Int, Float) -> Unit = { _, _ -> }
) {
    val ElegantLavender = rememberPrimaryColor(viewModel)

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
                    .height(52.dp)
                    .onGloballyPositioned {
                        if (tutorialStep == 2) {
                            onTutorialTarget(2, it.boundsInRoot().center.y)
                        }
                    },
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
                    .height(52.dp)
                    .onGloballyPositioned {
                        if (tutorialStep == 1) {
                            onTutorialTarget(1, it.boundsInRoot().center.y)
                        }
                    },
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

        // Big Debt Tracker Button
        Button(
            onClick = { viewModel.navigateTo(AppScreen.DEBTS) },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .onGloballyPositioned {
                    if (tutorialStep == 3) {
                        onTutorialTarget(3, it.boundsInRoot().center.y)
                    }
                },
            colors = ButtonDefaults.buttonColors(containerColor = ColorSettings),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalanceWallet,
                contentDescription = null,
                tint = Color(0xFF1C1B1F),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = LanguageHelper.getString("btnDebts", lang),
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color(0xFF1C1B1F),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            )
        }
    }
}
