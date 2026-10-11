package com.example.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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
    lang: AppLanguage,
    isFullScreen: Boolean = false
) {
    val ElegantLavender = rememberPrimaryColor(viewModel)
    val ElegantBtnText = Color(0xFF1C1B1F)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = if (isFullScreen) 8.dp else 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!isFullScreen) {
            Text(
                text = LanguageHelper.getString("settingsTitle", lang),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = ElegantTextPrimary
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }

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

        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (com.example.BuildConfig.DEBUG) Modifier.blur(6.dp) else Modifier)
                    .background(ElegantCardBg, shape = RoundedCornerShape(16.dp))
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

                if (com.example.BuildConfig.DEBUG) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFE65100).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = LanguageHelper.getString("syncDisabledInTestBuild", lang),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFFFCC80),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { viewModel.syncData(showExplicitToast = true) },
                        enabled = !com.example.BuildConfig.DEBUG && !isSyncing,
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

            if (com.example.BuildConfig.DEBUG) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(ElegantCardBg.copy(alpha = 0.78f))
                        .clickable { },
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = ElegantLavender,
                            modifier = Modifier.size(36.dp)
                        )
                        Text(
                            text = LanguageHelper.getString("syncDisabledInTestBuild", lang),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = ElegantTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Statistics Reset Interval settings
        val currentFreq by viewModel.statsResetFrequency.collectAsStateWithLifecycle()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(ElegantCardBg, shape = RoundedCornerShape(16.dp))
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
                .background(ElegantCardBg, shape = RoundedCornerShape(16.dp))
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
                .background(ElegantCardBg, shape = RoundedCornerShape(16.dp))
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
                                .background(ElegantCardBg),
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
                .background(ElegantCardBg, shape = RoundedCornerShape(16.dp))
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
                .background(ElegantCardBg, shape = RoundedCornerShape(16.dp))
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
        var showAccountClearWarning by remember { mutableStateOf(false) }

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
                            showClearConfirm = false
                            if (currentUserEmail != null) {
                                showAccountClearWarning = true
                            } else {
                                viewModel.clearHistoryLocally()
                                viewModel.hideSheet()
                            }
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

        if (showAccountClearWarning) {
            AlertDialog(
                onDismissRequest = { showAccountClearWarning = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ColorSpend
                        )
                        Text(
                            text = LanguageHelper.getString("accountClearWarningTitle", lang),
                            color = ElegantTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val emailDesc = if (currentUserEmail != null) " ($currentUserEmail)" else ""
                        Text(
                            text = if (lang == AppLanguage.RU)
                                "Вы вошли в аккаунт$emailDesc. Ваша история синхронизирована с облаком. Выберите вариант очистки:"
                            else
                                "You are signed into your account$emailDesc. Your history is synced with the cloud. Choose how to clear:",
                            color = ElegantTextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Option 1: Clear Everywhere
                        Button(
                            onClick = {
                                viewModel.clearHistoryEverywhere()
                                showAccountClearWarning = false
                                viewModel.hideSheet()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ColorSpend),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = LanguageHelper.getString("clearEverywhere", lang),
                                color = Color.Black,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Option 2: Sign Out and Clear Locally
                        Button(
                            onClick = {
                                viewModel.signOutAndClearLocally()
                                showAccountClearWarning = false
                                viewModel.hideSheet()
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElegantHeaderBg),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = LanguageHelper.getString("signOutAndClearLocally", lang),
                                color = ElegantLavender,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Option 3: Cancel
                        TextButton(
                            onClick = { showAccountClearWarning = false },
                            modifier = Modifier.fillMaxWidth().height(40.dp)
                        ) {
                            Text(
                                text = LanguageHelper.getString("cancel", lang),
                                color = ElegantTextPrimary
                            )
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {},
                containerColor = ElegantCardBg,
                titleContentColor = ElegantTextPrimary,
                textContentColor = ElegantTextSecondary,
                shape = RoundedCornerShape(24.dp)
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
