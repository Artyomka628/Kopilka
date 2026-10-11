package com.example.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElegantCardBg
import com.example.ui.theme.ElegantLavender
import com.example.ui.theme.ElegantTextPrimary
import com.example.ui.theme.ElegantTextSecondary
import kotlin.math.roundToInt

@Composable
fun OnboardingDialog(
    lang: AppLanguage,
    step: Int,
    targetCenterYInRoot: Float?,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
    onFinish: () -> Unit
) {
    val titleKey = when (step) {
        0 -> "onboardingWelcomeTitle"
        1 -> "onboardingIncomeTitle"
        2 -> "onboardingSpendTitle"
        3, 4, 5, 6 -> "onboardingDebtsTitle"
        7, 8 -> "onboardingSettingsTitle"
        else -> "onboardingTourFinishTitle"
    }
    val descriptionKey = when (step) {
        0 -> "onboardingTourIntroText"
        1 -> "onboardingTourIncomeText"
        2 -> "onboardingTourExpenseText"
        3 -> "onboardingTourGoDebtsText"
        4 -> "onboardingTourLendText"
        5 -> "onboardingTourBorrowText"
        6 -> "onboardingTourReturnFromDebtsText"
        7 -> "onboardingTourGoSettingsText"
        8 -> "onboardingTourSettingsText"
        else -> "onboardingTourFinishText"
    }
    val strings = { key: String -> LanguageHelper.getString(key, lang) }
    val finalStep = step >= 9
    val hasContinueAction = step == 0 || finalStep
    val density = LocalDensity.current
    val edgeMarginPx = with(density) { 16.dp.roundToPx() }

    var containerSize by remember { mutableStateOf(IntSize.Zero) }
    var cardSize by remember { mutableStateOf(IntSize.Zero) }
    var overlayOriginY by remember { mutableStateOf(0f) }
    var dragOffset by remember(step) { mutableStateOf(IntOffset.Zero) }

    val fallbackTargetIsUpperHalf = step in 4..8
    val targetCenterY = targetCenterYInRoot?.minus(overlayOriginY)
    val targetIsUpperHalf = targetCenterY?.let {
        it < containerSize.height / 2f
    } ?: fallbackTargetIsUpperHalf

    val automaticOffset = remember(containerSize, cardSize, targetIsUpperHalf, edgeMarginPx) {
        val y = when {
            containerSize.height <= 0 || cardSize.height <= 0 -> edgeMarginPx
            targetIsUpperHalf ->
                (containerSize.height - cardSize.height - edgeMarginPx).coerceAtLeast(edgeMarginPx)
            else -> edgeMarginPx
        }
        IntOffset(x = 0, y = y)
    }
    val animatedOffset by animateIntOffsetAsState(
        targetValue = automaticOffset,
        animationSpec = tween(durationMillis = 460, easing = FastOutSlowInEasing),
        label = "onboarding-card-position"
    )

    val latestAnimatedOffset by rememberUpdatedState(animatedOffset)
    val latestDragOffset by rememberUpdatedState(dragOffset)
    val latestContainerSize by rememberUpdatedState(containerSize)
    val latestCardSize by rememberUpdatedState(cardSize)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { containerSize = it }
            .onGloballyPositioned { overlayOriginY = it.positionInRoot().y },
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.88f)
                .offset {
                    IntOffset(
                        x = animatedOffset.x + dragOffset.x,
                        y = animatedOffset.y + dragOffset.y
                    )
                }
                .onSizeChanged { if (cardSize != it) cardSize = it }
                .pointerInput(step) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val base = latestAnimatedOffset
                        val current = latestDragOffset
                        val minY = -base.y
                        val maxY = (
                            latestContainerSize.height - latestCardSize.height - base.y
                        ).coerceAtLeast(minY)
                        val maxX = (
                            (latestContainerSize.width - latestCardSize.width) / 2
                        ).coerceAtLeast(0)
                        dragOffset = IntOffset(
                            x = (current.x + dragAmount.x.roundToInt()).coerceIn(-maxX, maxX),
                            y = (current.y + dragAmount.y.roundToInt()).coerceIn(minY, maxY)
                        )
                    }
                },
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = ElegantCardBg.copy(alpha = 0.98f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .width(34.dp)
                            .height(4.dp)
                            .then(Modifier.background(
                                color = ElegantTextSecondary.copy(alpha = 0.55f),
                                shape = CircleShape
                            ))
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${strings("onboardingTourStep")} ${step + 1}/10",
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
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 21.sp,
                        textAlign = TextAlign.Start
                    )
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
                                if (step == 0) "onboardingTourStart" else "onboardingTourFinish"
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
