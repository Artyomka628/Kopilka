package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.Debt
import com.example.model.DebtType
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(viewModel: KopilkaViewModel, lang: AppLanguage) {
    BackHandler {
        viewModel.navigateTo(AppScreen.MAIN)
    }

    val debts by viewModel.debts.collectAsStateWithLifecycle()
    val customEnabled by viewModel.customCurrencyEnabled.collectAsStateWithLifecycle()
    val customSymbol by viewModel.customCurrencySymbol.collectAsStateWithLifecycle()
    val displaySymbol = if (customEnabled && customSymbol.isNotEmpty()) customSymbol else "$"

    val ElegantLavender = rememberPrimaryColor(viewModel)
    val ElegantBtnText = Color(0xFF1C1B1F)

    var showAddSheet by remember { mutableStateOf(false) }
    var addDebtType by remember { mutableStateOf(DebtType.OWED_TO_ME) }

    var editingDebt by remember { mutableStateOf<Debt?>(null) }

    var selectedFilter by remember { mutableStateOf<DebtType?>(null) } // null = All

    val filteredDebts = remember(debts, selectedFilter) {
        if (selectedFilter == null) debts else debts.filter { it.type == selectedFilter }
    }

    val totalIOwe = remember(debts) {
        debts.filter { it.type == DebtType.I_OWE }.sumOf { it.amount }
    }
    val totalOwedToMe = remember(debts) {
        debts.filter { it.type == DebtType.OWED_TO_ME }.sumOf { it.amount }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = LanguageHelper.getString("debtsTitle", lang),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary
                        )
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateTo(AppScreen.MAIN) },
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ElegantCardBg)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = ElegantTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = ElegantDarkBg,
                    titleContentColor = ElegantTextPrimary
                )
            )
        },
        containerColor = ElegantDarkBg
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // Debt summary totals card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ElegantHeaderBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Owed to me (Вам должны)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = LanguageHelper.getString("totalOwedToMe", lang).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ElegantTextTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val formattedOwed = if (customEnabled && customSymbol.isNotEmpty()) {
                            "${formatDouble(totalOwedToMe)} $customSymbol"
                        } else {
                            formatDouble(totalOwedToMe)
                        }
                        Text(
                            text = formattedOwed,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = ColorTopUp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(36.dp)
                            .background(ElegantTextSecondary.copy(alpha = 0.3f))
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    // I owe (Вы должны)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = LanguageHelper.getString("totalIOwe", lang).uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = ElegantTextTertiary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        val formattedOwe = if (customEnabled && customSymbol.isNotEmpty()) {
                            "${formatDouble(totalIOwe)} $customSymbol"
                        } else {
                            formatDouble(totalIOwe)
                        }
                        Text(
                            text = formattedOwe,
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = ColorSpend,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action buttons: "Дать в долг" & "Взять в долг"
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Give debt (Дать в долг -> мне должны)
                Button(
                    onClick = {
                        addDebtType = DebtType.OWED_TO_ME
                        showAddSheet = true
                    },
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
                        text = LanguageHelper.getString("giveDebt", lang),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Take debt (Взять в долг -> я должен)
                Button(
                    onClick = {
                        addDebtType = DebtType.I_OWE
                        showAddSheet = true
                    },
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
                        text = LanguageHelper.getString("takeDebt", lang),
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filter chips (Все / Мне должны / Я должен)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == null,
                    onClick = { selectedFilter = null },
                    label = { Text(LanguageHelper.getString("filterAll", lang)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ElegantLavender,
                        selectedLabelColor = ElegantBtnText,
                        containerColor = ElegantCardBg,
                        labelColor = ElegantTextPrimary
                    )
                )

                FilterChip(
                    selected = selectedFilter == DebtType.OWED_TO_ME,
                    onClick = { selectedFilter = DebtType.OWED_TO_ME },
                    label = { Text(LanguageHelper.getString("filterOwedToMe", lang)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ColorTopUp,
                        selectedLabelColor = Color.Black,
                        containerColor = ElegantCardBg,
                        labelColor = ElegantTextPrimary
                    )
                )

                FilterChip(
                    selected = selectedFilter == DebtType.I_OWE,
                    onClick = { selectedFilter = DebtType.I_OWE },
                    label = { Text(LanguageHelper.getString("filterIOwe", lang)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ColorSpend,
                        selectedLabelColor = Color.Black,
                        containerColor = ElegantCardBg,
                        labelColor = ElegantTextPrimary
                    )
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Debts List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (filteredDebts.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = LanguageHelper.getString("emptyDebts", lang),
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
                        items(filteredDebts, key = { it.id }) { debt ->
                            DebtItemCard(
                                debt = debt,
                                sdf = sdf,
                                lang = lang,
                                customEnabled = customEnabled,
                                customSymbol = customSymbol,
                                onClick = { editingDebt = debt }
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Add Debt
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ElegantCardBg,
            contentColor = ElegantTextPrimary,
            dragHandle = { BottomSheetDefaults.DragHandle(color = ElegantTextSecondary) }
        ) {
            AddDebtSheetContent(
                type = addDebtType,
                lang = lang,
                onConfirm = { name, amount ->
                    viewModel.addDebt(name, amount, addDebtType)
                    showAddSheet = false
                },
                onCancel = { showAddSheet = false }
            )
        }
    }

    // Modal Bottom Sheet: Edit Debt (Изменить сколько ты/он должен + кнопка полностью вернул)
    editingDebt?.let { debt ->
        ModalBottomSheet(
            onDismissRequest = { editingDebt = null },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = ElegantCardBg,
            contentColor = ElegantTextPrimary,
            dragHandle = { BottomSheetDefaults.DragHandle(color = ElegantTextSecondary) }
        ) {
            EditDebtSheetContent(
                debt = debt,
                lang = lang,
                onUpdateAmount = { newAmount ->
                    viewModel.updateDebtAmount(debt.id, newAmount)
                    editingDebt = null
                },
                onFullyReturned = {
                    viewModel.deleteDebt(debt.id)
                    editingDebt = null
                },
                onCancel = { editingDebt = null }
            )
        }
    }
}

@Composable
fun DebtItemCard(
    debt: Debt,
    sdf: SimpleDateFormat,
    lang: AppLanguage,
    customEnabled: Boolean,
    customSymbol: String,
    onClick: () -> Unit
) {
    val isOwedToMe = debt.type == DebtType.OWED_TO_ME
    val badgeColor = if (isOwedToMe) ColorTopUp else ColorSpend
    val badgeBg = badgeColor.copy(alpha = 0.15f)
    val typeLabel = if (isOwedToMe) LanguageHelper.getString("owedToMe", lang) else LanguageHelper.getString("iOwe", lang)

    val formattedAmount = formatDouble(debt.amount)
    val displayAmount = if (customEnabled && customSymbol.isNotEmpty()) {
        "$formattedAmount $customSymbol"
    } else {
        formattedAmount
    }

    val dateStr = remember(debt.timestamp) { sdf.format(Date(debt.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
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
                // Circular icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isOwedToMe) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = debt.name,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElegantTextPrimary
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$typeLabel • $dateStr",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ElegantTextSecondary,
                            fontSize = 12.sp
                        )
                    )
                }
            }

            Text(
                text = displayAmount,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = badgeColor,
                    fontSize = 17.sp
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * Bottom Sheet panel styled like TopUpSheetContent (пополнить)
 */
@Composable
fun AddDebtSheetContent(
    type: DebtType,
    lang: AppLanguage,
    onConfirm: (name: String, amount: Double) -> Unit,
    onCancel: () -> Unit
) {
    var nameText by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    val isOwedToMe = type == DebtType.OWED_TO_ME
    val titleText = if (isOwedToMe) LanguageHelper.getString("giveDebt", lang) else LanguageHelper.getString("takeDebt", lang)
    val accentColor = if (isOwedToMe) ColorTopUp else ColorSpend

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = titleText,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = ElegantTextPrimary
            ),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        // Name input
        OutlinedTextField(
            value = nameText,
            onValueChange = {
                nameText = it
                errorText = null
            },
            label = { Text(text = LanguageHelper.getString("personName", lang), color = ElegantTextSecondary) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = ElegantTextSecondary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ElegantTextPrimary,
                unfocusedTextColor = ElegantTextPrimary,
                focusedBorderColor = accentColor,
                unfocusedBorderColor = ElegantTextSecondary,
                focusedContainerColor = ElegantDarkBg,
                unfocusedContainerColor = ElegantDarkBg
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        // Amount input
        OutlinedTextField(
            value = amountText,
            onValueChange = {
                amountText = it
                errorText = null
            },
            label = { Text(text = LanguageHelper.getString("debtAmount", lang), color = ElegantTextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ElegantTextPrimary,
                unfocusedTextColor = ElegantTextPrimary,
                focusedBorderColor = accentColor,
                unfocusedBorderColor = ElegantTextSecondary,
                focusedContainerColor = ElegantDarkBg,
                unfocusedContainerColor = ElegantDarkBg
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (errorText != null) {
            Text(
                text = errorText ?: "",
                color = ColorSpend,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Confirm button
            Button(
                onClick = {
                    val trimmedName = nameText.trim()
                    if (trimmedName.isEmpty()) {
                        errorText = LanguageHelper.getString("invalidName", lang)
                        return@Button
                    }
                    val amt = amountText.replace(',', '.').toDoubleOrNull()?.let { Math.round(it * 100.0) / 100.0 }
                    if (amt == null || amt <= 0.0 || amt.isNaN() || amt.isInfinite()) {
                        errorText = LanguageHelper.getString("invalidAmount", lang)
                        return@Button
                    }
                    if (amt > 1_000_000_000.0) {
                        errorText = LanguageHelper.getString("amountTooLarge", lang)
                        return@Button
                    }
                    onConfirm(trimmedName, amt)
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

            // Cancel button
            Button(
                onClick = onCancel,
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

/**
 * Edit Debt Bottom Sheet:
 * Change amount remaining + "Fully returned" button (полностью вернул)
 */
@Composable
fun EditDebtSheetContent(
    debt: Debt,
    lang: AppLanguage,
    onUpdateAmount: (Double) -> Unit,
    onFullyReturned: () -> Unit,
    onCancel: () -> Unit
) {
    var amountText by remember { mutableStateOf(formatInputAmount(debt.amount)) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val isOwedToMe = debt.type == DebtType.OWED_TO_ME
    val accentColor = if (isOwedToMe) ColorTopUp else ColorSpend

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "${LanguageHelper.getString("editDebt", lang)}: ${debt.name}",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = ElegantTextPrimary
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = if (isOwedToMe) LanguageHelper.getString("owedToMe", lang) else LanguageHelper.getString("iOwe", lang),
            style = MaterialTheme.typography.bodyMedium.copy(
                color = accentColor,
                fontWeight = FontWeight.SemiBold
            )
        )

        // Amount remaining input
        OutlinedTextField(
            value = amountText,
            onValueChange = {
                amountText = it
                errorText = null
            },
            label = { Text(text = LanguageHelper.getString("debtAmountRemaining", lang), color = ElegantTextSecondary) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ElegantTextPrimary,
                unfocusedTextColor = ElegantTextPrimary,
                focusedBorderColor = accentColor,
                unfocusedBorderColor = ElegantTextSecondary,
                focusedContainerColor = ElegantDarkBg,
                unfocusedContainerColor = ElegantDarkBg
            ),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        if (errorText != null) {
            Text(
                text = errorText ?: "",
                color = ColorSpend,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Big "Fully Returned" (Полностью вернул) Button
        Button(
            onClick = onFullyReturned,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ColorTopUp.copy(alpha = 0.2f)),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, ColorTopUp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = ColorTopUp,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = LanguageHelper.getString("fullyReturned", lang),
                color = ColorTopUp,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Save updated amount
            Button(
                onClick = {
                    val amt = amountText.replace(',', '.').toDoubleOrNull()?.let { Math.round(it * 100.0) / 100.0 }
                    if (amt == null || amt < 0.0 || amt.isNaN() || amt.isInfinite()) {
                        errorText = LanguageHelper.getString("invalidAmount", lang)
                        return@Button
                    }
                    if (amt > 1_000_000_000.0) {
                        errorText = LanguageHelper.getString("amountTooLarge", lang)
                        return@Button
                    }
                    onUpdateAmount(amt)
                },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ElegantHeaderBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Save",
                    tint = ColorTopUp,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Cancel button
            Button(
                onClick = onCancel,
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
