package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.ExpenseCategory
import com.example.ui.theme.*
import java.util.*

@Composable
fun SpendSheetContent(viewModel: KopilkaViewModel, lang: AppLanguage) {
    var amountText by remember { mutableStateOf("") }
    var reasonText by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.OTHER) }

    val categories = remember { ExpenseCategory.values().toList() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = LanguageHelper.getString("btnSpend", lang),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = ElegantTextPrimary
            ),
            modifier = Modifier.padding(bottom = 2.dp)
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

        // Category selection dropdown
        var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = LanguageHelper.getString("category", lang),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = ElegantTextPrimary
                )
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isCategoryDropdownExpanded = true },
                    color = ElegantDarkBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        width = if (isCategoryDropdownExpanded) 2.dp else 1.dp,
                        color = if (isCategoryDropdownExpanded) ColorSpend else ElegantHeaderBg
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(ColorSpend.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = selectedCategory.icon,
                                    contentDescription = selectedCategory.getTitle(lang),
                                    tint = ColorSpend,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Text(
                                text = selectedCategory.getTitle(lang),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = ElegantTextPrimary,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Icon(
                            imageVector = if (isCategoryDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle category dropdown",
                            tint = ElegantTextSecondary
                        )
                    }
                }

                DropdownMenu(
                    expanded = isCategoryDropdownExpanded,
                    onDismissRequest = { isCategoryDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .heightIn(max = 340.dp)
                        .background(ElegantCardBg)
                ) {
                    categories.forEach { category ->
                        val isSelected = category == selectedCategory
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = category.getTitle(lang),
                                    color = if (isSelected) ColorSpend else ElegantTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = category.icon,
                                    contentDescription = category.getTitle(lang),
                                    tint = if (isSelected) ColorSpend else ElegantTextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = ColorSpend,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            } else null,
                            onClick = {
                                selectedCategory = category
                                isCategoryDropdownExpanded = false
                            },
                            modifier = Modifier.background(
                                if (isSelected) ColorSpend.copy(alpha = 0.12f) else Color.Transparent
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

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
                        viewModel.addTransaction(-amt, reasonText, selectedCategory.id)
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
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
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
            modifier = Modifier.padding(bottom = 2.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            val commitDisplay = if (com.example.BuildInfo.COMMIT_HASH.length > 8) {
                com.example.BuildInfo.COMMIT_HASH.take(8)
            } else {
                com.example.BuildInfo.COMMIT_HASH
            }
            Text(
                text = "${LanguageHelper.getString("commitHash", lang)}: $commitDisplay",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ElegantTextSecondary.copy(alpha = 0.8f)
                ),
                textAlign = TextAlign.Center
            )
            Text(
                text = "${LanguageHelper.getString("buildTime", lang)}: ${com.example.BuildInfo.BUILD_TIME}",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = ElegantTextSecondary.copy(alpha = 0.8f)
                ),
                textAlign = TextAlign.Center
            )
        }

        if (com.example.BuildConfig.DEBUG) {
            var showDetailDialog by remember { mutableStateOf(false) }

            if (showDetailDialog) {
                AlertDialog(
                    onDismissRequest = { showDetailDialog = false },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    title = {
                        Text(
                            text = LanguageHelper.getString("testBuildTitle", lang),
                            color = ElegantTextPrimary,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = LanguageHelper.getString("testBuildSubtitle", lang),
                                color = ElegantTextSecondary,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    lineHeight = 21.sp
                                ),
                                textAlign = TextAlign.Start
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = { showDetailDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100))
                        ) {
                            Text(
                                text = if (lang == AppLanguage.RU) "Понятно" else "I Understand",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    containerColor = ElegantCardBg,
                    shape = RoundedCornerShape(20.dp)
                )
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showDetailDialog = true },
                color = Color(0xFFE65100).copy(alpha = 0.15f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = LanguageHelper.getString("testBuildBanner", lang),
                            style = MaterialTheme.typography.labelLarge.copy(
                                color = Color(0xFFFFCC80),
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Text(
                        text = LanguageHelper.getString("syncDisabledInTestBuild", lang),
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = ElegantTextSecondary,
                            lineHeight = 16.sp
                        )
                    )
                }
            }
        }

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
                .padding(bottom = 16.dp)
                .height(48.dp),
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
