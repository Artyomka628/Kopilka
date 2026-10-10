package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElegantCardBg
import com.example.ui.theme.ElegantLavender
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary

private data class OnboardingStep(val titleKey: String, val descriptionKey: String)

@Composable
fun OnboardingDialog(lang: AppLanguage, onFinish: () -> Unit) {
    val steps = listOf(
        OnboardingStep("onboardingWelcomeTitle", "onboardingWelcomeText"),
        OnboardingStep("onboardingIncomeTitle", "onboardingIncomeText"),
        OnboardingStep("onboardingSpendTitle", "onboardingSpendText"),
        OnboardingStep("onboardingSettingsTitle", "onboardingSettingsText"),
        OnboardingStep("onboardingDebtsTitle", "onboardingDebtsText")
    )
    var stepIndex by remember { mutableStateOf(0) }
    val step = steps[stepIndex]

    AlertDialog(
        onDismissRequest = onFinish,
        icon = {
            Text(
                text = "${stepIndex + 1} / ${steps.size}",
                color = ElegantTextSecondary,
                style = MaterialTheme.typography.labelLarge
            )
        },
        title = {
            Text(
                text = LanguageHelper.getString(step.titleKey, lang),
                color = ElegantTextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 280.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = LanguageHelper.getString(step.descriptionKey, lang),
                    color = ElegantTextSecondary,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    repeat(steps.size) { index ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .background(
                                        if (index == stepIndex) ElegantLavender else ElegantTextSecondary.copy(alpha = 0.25f),
                                        RoundedCornerShape(4.dp)
                                    )
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (stepIndex < steps.lastIndex) stepIndex++ else onFinish()
                },
                colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender)
            ) {
                Text(
                    text = LanguageHelper.getString(
                        if (stepIndex < steps.lastIndex) "onboardingNext" else "onboardingFinish",
                        lang
                    ),
                    color = androidx.compose.ui.graphics.Color(0xFF1C1B1F),
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = { if (stepIndex == 0) onFinish() else stepIndex-- }) {
                Text(
                    text = LanguageHelper.getString(
                        if (stepIndex == 0) "onboardingSkip" else "onboardingBack",
                        lang
                    ),
                    color = ElegantTextPrimary
                )
            }
        },
        containerColor = ElegantCardBg,
        shape = RoundedCornerShape(24.dp)
    )
}
