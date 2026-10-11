package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElegantCardBg
import com.example.ui.theme.ElegantLavender
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary

@Composable
fun OnboardingDialog(
    lang: AppLanguage,
    step: Int,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    onFinish: () -> Unit
) {
    val titleKey = when (step) {
        0 -> "onboardingWelcomeTitle"
        1 -> "onboardingIncomeTitle"
        2 -> "onboardingSpendTitle"
        3, 4 -> "onboardingDebtsTitle"
        5 -> "onboardingSettingsTitle"
        else -> "onboardingAccountTitle"
    }
    val descriptionKey = when (step) {
        0 -> "onboardingTourIntroText"
        1 -> "onboardingTourIncomeText"
        2 -> "onboardingTourExpenseText"
        3 -> "onboardingTourLendText"
        4 -> "onboardingTourBorrowText"
        5 -> "onboardingTourSettingsText"
        else -> "onboardingTourAccountText"
    }
    val strings = { key: String -> LanguageHelper.getString(key, lang) }
    val finalStep = step >= 6
    val hasContinueAction = step == 0 || step == 5 || finalStep
    val alignment = if (step <= 2 || step >= 5) Alignment.TopCenter else Alignment.BottomCenter

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = alignment
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .then(
                    if (step <= 2 || step >= 5) Modifier.padding(top = 116.dp)
                    else Modifier.padding(bottom = 16.dp)
                ),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantCardBg.copy(alpha = 0.98f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${strings("onboardingTourStep")} ${step + 1}/7",
                        color = ElegantTextSecondary,
                        style = MaterialTheme.typography.labelMedium
                    )
                    if (!finalStep) {
                        TextButton(onClick = onSkip) {
                            Text(strings("onboardingTourSkip"), color = ElegantTextSecondary)
                        }
                    }
                }

                Text(
                    text = strings(titleKey),
                    color = ElegantTextPrimary,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = strings(descriptionKey),
                    color = ElegantTextSecondary,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 21.sp)
                )

                if (hasContinueAction) {
                    Button(
                        onClick = {
                            if (finalStep) onFinish() else onContinue()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = strings(
                                when {
                                    step == 0 -> "onboardingTourStart"
                                    finalStep -> "onboardingTourFinish"
                                    else -> "onboardingTourContinue"
                                }
                            ),
                            color = Color(0xFF1C1B1F),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
