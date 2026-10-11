package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElegantCardBg
import com.example.ui.theme.ElegantLavender
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary

private data class OnboardingStep(val titleKey: String, val descriptionKey: String, val practice: Int = 0)

@Composable
fun OnboardingDialog(lang: AppLanguage, onFinish: () -> Unit) {
    val steps = listOf(
        OnboardingStep("onboardingWelcomeTitle", "onboardingWelcomeText"),
        OnboardingStep("onboardingIncomeTitle", "onboardingIncomeText", 1),
        OnboardingStep("onboardingSpendTitle", "onboardingSpendText", 2),
        OnboardingStep("onboardingDebtsTitle", "onboardingDebtsText", 3),
        OnboardingStep("onboardingDebtsTitle", "onboardingDebtsText", 4),
        OnboardingStep("onboardingSettingsTitle", "onboardingSettingsText", 5),
        OnboardingStep("onboardingAccountTitle", "onboardingAccountText")
    )
    var stepIndex by remember { mutableIntStateOf(0) }
    var demoBalance by remember { mutableIntStateOf(0) }
    val practiced = remember { mutableStateListOf<Int>() }
    val exploredSettings = remember { mutableStateListOf<Int>() }
    val step = steps[stepIndex]
    val isPracticeComplete = when (step.practice) {
        1 -> 1 in practiced
        2 -> 2 in practiced
        3 -> 3 in practiced
        4 -> 4 in practiced
        5 -> exploredSettings.size == 4
        else -> true
    }
    val strings = { key: String -> LanguageHelper.getString(key, lang) }

    AlertDialog(
        onDismissRequest = onFinish,
        icon = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("${stepIndex + 1} / ${steps.size}", color = ElegantTextSecondary, style = MaterialTheme.typography.labelLarge)
                Text(strings("onboardingPracticeProgress"), color = ElegantTextSecondary, style = MaterialTheme.typography.labelSmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(steps.size) { index ->
                        androidx.compose.foundation.layout.Box(
                            Modifier.weight(1f).height(4.dp).background(
                                if (index <= stepIndex) ElegantLavender else ElegantTextSecondary.copy(alpha = 0.25f),
                                RoundedCornerShape(4.dp)
                            )
                        )
                    }
                }
            }
        },
        title = {
            Text(strings(step.titleKey), color = ElegantTextPrimary, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        },
        text = {
            Column(
                Modifier.fillMaxWidth().heightIn(max = 360.dp).verticalScroll(rememberScrollState()).padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(strings(step.descriptionKey), color = ElegantTextSecondary, style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp))
                if (step.practice in 1..4) {
                    Text(strings("onboardingPracticeLabel"), color = ElegantLavender, fontWeight = FontWeight.Bold)
                    Text(strings("onboardingTapToPractice"), color = ElegantTextSecondary, style = MaterialTheme.typography.bodySmall)
                    Text("${strings("onboardingDemoBalance")}: $demoBalance", color = ElegantTextPrimary, fontWeight = FontWeight.Bold)
                    val actionKey = when (step.practice) {
                        1 -> "onboardingDemoAdd"
                        2 -> "onboardingDemoSpend"
                        3 -> "onboardingDemoGive"
                        else -> "onboardingDemoBorrow"
                    }
                    Button(
                        onClick = {
                            when (step.practice) {
                                1 -> demoBalance += 10
                                2 -> demoBalance = (demoBalance - 3).coerceAtLeast(0)
                                3, 4 -> Unit
                            }
                            practiced.add(step.practice)
                        },
                        enabled = step.practice !in practiced,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender)
                    ) {
                        Text(strings(if (step.practice in practiced) "onboardingDemoComplete" else actionKey), color = Color(0xFF1C1B1F), fontWeight = FontWeight.Bold)
                    }
                }
                if (step.practice == 5) {
                    Text(strings("onboardingSettingsPracticeText"), color = ElegantTextSecondary)
                    val settingsKeys = listOf("onboardingPracticeTheme", "onboardingPracticeGoal", "onboardingPracticeLanguage", "onboardingPracticeBackup")
                    settingsKeys.forEachIndexed { index, key ->
                        TextButton(
                            onClick = { if (index !in exploredSettings) exploredSettings.add(index) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${if (index in exploredSettings) "✓ " else "○ "}${strings(key)}",
                                color = if (index in exploredSettings) ElegantLavender else ElegantTextPrimary
                            )
                        }
                    }
                }
                if (stepIndex == steps.lastIndex) {
                    Text(strings("onboardingAccountReminder"), color = ElegantLavender, fontWeight = FontWeight.SemiBold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (stepIndex < steps.lastIndex) stepIndex++ else onFinish() },
                enabled = isPracticeComplete,
                colors = ButtonDefaults.buttonColors(containerColor = ElegantLavender)
            ) {
                Text(strings(if (stepIndex < steps.lastIndex) "onboardingNext" else "onboardingFinish"), color = Color(0xFF1C1B1F), fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = { if (stepIndex == 0) onFinish() else stepIndex-- }) {
                Text(strings(if (stepIndex == 0) "onboardingSkip" else "onboardingBack"), color = ElegantTextPrimary)
            }
        },
        containerColor = ElegantCardBg,
        shape = RoundedCornerShape(24.dp)
    )
}
