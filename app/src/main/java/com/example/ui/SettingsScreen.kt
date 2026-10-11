package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ElegantCardBg
import com.example.ui.theme.ElegantDarkBg
import com.example.ui.theme.ElegantTextPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: KopilkaViewModel,
    onSaveClick: () -> Unit,
    onLoadClick: () -> Unit,
    lang: AppLanguage,
    tutorialStep: Int = 0,
    onTutorialTarget: (Int, Float) -> Unit = { _, _ -> }
) {
    BackHandler {
        viewModel.navigateTo(AppScreen.MAIN)
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = LanguageHelper.getString("settingsTitle", lang),
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
                            .padding(start = 16.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF35343A))
                            .onGloballyPositioned {
                                if (tutorialStep == 8) {
                                    onTutorialTarget(8, it.boundsInRoot().center.y)
                                }
                            }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = LanguageHelper.getString("back", lang),
                            tint = ElegantTextPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = ElegantDarkBg,
                    titleContentColor = ElegantTextPrimary,
                    navigationIconContentColor = ElegantTextPrimary
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
        },
        containerColor = ElegantDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SettingsSheetContent(
                viewModel = viewModel,
                onSaveClick = onSaveClick,
                onLoadClick = onLoadClick,
                lang = lang,
                isFullScreen = true
            )
        }
    }
}
