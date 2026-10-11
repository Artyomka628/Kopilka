package com.example.ui

import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.flow.collect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.*
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

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val showOnboarding by viewModel.showOnboarding.collectAsStateWithLifecycle()
    val onboardingStep by viewModel.onboardingStep.collectAsStateWithLifecycle()
    val onboardingInteractionOpen by viewModel.onboardingInteractionOpen.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })

    // Update the selected screen only after the user's swipe has settled.
    // Observing settledPage through snapshotFlow avoids recomposing the root during pager movement.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { page ->
            val targetScreen = when (page) {
                0 -> AppScreen.DEBTS
                1 -> AppScreen.MAIN
                2 -> AppScreen.SETTINGS
                else -> AppScreen.MAIN
            }
            if (viewModel.currentScreen.value != targetScreen) {
                viewModel.navigateTo(targetScreen)
            }
        }
    }

    // Navigate only when the pager is not already moving toward the requested page.
    LaunchedEffect(currentScreen) {
        val targetPage = when (currentScreen) {
            AppScreen.SETTINGS -> 2
            AppScreen.MAIN -> 1
            AppScreen.DEBTS -> 0
        }
        if (pagerState.targetPage != targetPage) {
            pagerState.animateScrollToPage(
                page = targetPage,
                animationSpec = tween(durationMillis = 260)
            )
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
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !showOnboarding || onboardingStep == 3 || onboardingStep == 6
            ) { page ->
                when (page) {
                    0 -> {
                        DebtsScreen(
                            viewModel = viewModel,
                            lang = lang
                        )
                    }
                    1 -> {
                        MainScreen(
                            viewModel = viewModel,
                            lang = lang
                        )
                    }
                    2 -> {
                        SettingsScreen(
                            viewModel = viewModel,
                            onSaveClick = {
                                exportLauncher.launch("kopilka_history.json")
                            },
                            onLoadClick = {
                                importLauncher.launch(arrayOf("application/json"))
                            },
                            lang = lang
                        )
                    }
                }
            }

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
                        else -> {}
                    }
                }
            }

            if (showOnboarding && currentSheet == null && !onboardingInteractionOpen) {
                OnboardingDialog(
                    lang = lang,
                    step = onboardingStep,
                    onContinue = { viewModel.continueInteractiveOnboarding() },
                    onSkip = { viewModel.finishInteractiveOnboarding() },
                    onFinish = { viewModel.finishInteractiveOnboarding() },
                    onOpenDebts = { viewModel.navigateTo(AppScreen.DEBTS) },
                    onOpenSettings = { viewModel.navigateTo(AppScreen.SETTINGS) }
                )
            }

            // Test build warning appears only after the onboarding is completed.
            val showTestBuildDialog by viewModel.showTestBuildDialog.collectAsStateWithLifecycle()
            if (showTestBuildDialog && !showOnboarding) {
                AlertDialog(
                    onDismissRequest = { viewModel.dismissTestBuildDialog() },
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
                            onClick = { viewModel.dismissTestBuildDialog() },
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
        }
    }
}
