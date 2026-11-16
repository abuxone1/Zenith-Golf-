package com.example.game.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.firebase.GoogleAuthManager
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ZenithTheme
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch

@Composable
fun ZenithSignInScreen(
    modifier: Modifier = Modifier,
    onSignInSuccess: () -> Unit = {},
    onContinueAsGuest: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember(context) {
        runCatching {
            if (FirebaseApp.getApps(context).isEmpty()) {
                FirebaseApp.initializeApp(context)
            }
            GoogleAuthManager(context)
        }.getOrNull()
    }
    val colors = ZenithTheme.colors

    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var infoMessage by remember { mutableStateOf<String?>(null) }

    var showEmailForm by rememberSaveable { mutableStateOf(false) }
    var isCreateAccountMode by rememberSaveable { mutableStateOf(false) }
    var emailInput by rememberSaveable { mutableStateOf("") }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var usernameInput by rememberSaveable { mutableStateOf("") }
    var showPassword by rememberSaveable { mutableStateOf(false) }

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    LaunchedEffect(authManager) {
        authManager?.attemptSilentAccountRecovery()?.onSuccess {
            onSignInSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("sign_in_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(scrollState)
                .padding(horizontal = 22.dp, vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Hero Banner Card with Crest & Welcome Message
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = colors.forestCard,
                border = BorderStroke(1.5.dp, colors.accentGold.copy(alpha = 0.7f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.zenith_tour_banner_1791831254393),
                        contentDescription = "Zenith Championship Course",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        colors.deepForestGreen.copy(alpha = 0.55f),
                                        colors.deepForestGreen.copy(alpha = 0.92f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(62.dp)
                                .clip(CircleShape)
                                .background(colors.primaryGreen)
                                .border(2.dp, colors.accentGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.zenith_golf_icon_1791831231277),
                                contentDescription = "Zenith Golf Crest",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "WELCOME TO ZENITH GOLF",
                            fontFamily = OutfitFontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 1.4.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Championship fairways, deterministic physics & cross-device cloud career sync.",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.85f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Feature Highlights
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SignInFeaturePill(
                    icon = Icons.Default.CloudDone,
                    iconColor = colors.accentInfo,
                    title = "Cloud Career & Device Sync",
                    desc = "Safely store coins, unlocked equipment, trophies & career rounds"
                )
                SignInFeaturePill(
                    icon = Icons.Default.AutoAwesome,
                    iconColor = colors.accentGold,
                    title = "Season Tour Pass & Rewards",
                    desc = "Daily login streaks, Lucky Wheel prizes & progressive Tour tiers"
                )
                SignInFeaturePill(
                    icon = Icons.Default.EmojiEvents,
                    iconColor = colors.accentSuccess,
                    title = "Verified Tour Leaderboards",
                    desc = "Official tournament standings & career profile stats"
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status / Error Feedback Banner
            AnimatedVisibility(
                visible = errorMessage != null || infoMessage != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val isErr = errorMessage != null
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = if (isErr) colors.error.copy(alpha = 0.14f) else colors.accentSuccess.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, if (isErr) colors.error else colors.accentSuccess)
                ) {
                    Text(
                        text = errorMessage ?: infoMessage.orEmpty(),
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isErr) colors.error else colors.accentSuccess,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Primary Google Sign-In CTA
            Button(
                onClick = {
                    if (!isLoading) {
                        val manager = authManager
                        if (manager == null) {
                            errorMessage = "Cloud authentication is initializing. Use Guest Play below or retry."
                        } else {
                            isLoading = true
                            errorMessage = null
                            infoMessage = null
                            scope.launch {
                                val result = manager.signInWithGoogle()
                                isLoading = false
                                result.onSuccess {
                                    onSignInSuccess()
                                }.onFailure { error ->
                                    val msg = error.localizedMessage ?: "Sign-in was cancelled or unavailable"
                                    errorMessage = if (msg.contains("cancel", ignoreCase = true)) {
                                        "Google Sign-In cancelled. Choose an option below to continue."
                                    } else {
                                        "Google Sign-In failed: $msg"
                                    }
                                }
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("google_sign_in_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF1F1F1F)
                ),
                border = BorderStroke(1.dp, colors.border),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 3.dp)
            ) {
                if (isLoading && !showEmailForm) {
                    CircularProgressIndicator(
                        color = Color(0xFF1F1F1F),
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Connecting Google Account...",
                        fontFamily = OutfitFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Google Sign In",
                        tint = Color(0xFF4285F4),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Sign in with Google",
                        fontFamily = OutfitFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1F1F1F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Email Sign-In / Create Account Toggle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        showEmailForm = true
                        isCreateAccountMode = false
                        errorMessage = null
                        infoMessage = null
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (showEmailForm && !isCreateAccountMode) colors.accentGold else colors.border
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (showEmailForm && !isCreateAccountMode) colors.surfaceElevated else colors.surface,
                        contentColor = colors.textPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("toggle_email_signin_btn")
                ) {
                    Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Email Sign-In",
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        showEmailForm = true
                        isCreateAccountMode = true
                        errorMessage = null
                        infoMessage = null
                    },
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        1.dp,
                        if (showEmailForm && isCreateAccountMode) colors.accentGold else colors.border
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (showEmailForm && isCreateAccountMode) colors.surfaceElevated else colors.surface,
                        contentColor = colors.textPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("toggle_create_account_btn")
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Create Account",
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Expandable Email Sign-In / Registration Card
            AnimatedVisibility(visible = showEmailForm) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .testTag("email_auth_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isCreateAccountMode) "CREATE PRO GOLFER ACCOUNT" else "SIGN IN WITH EMAIL",
                            fontFamily = OutfitFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentGold
                        )

                        val fieldColors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.accentGold,
                            unfocusedBorderColor = colors.border,
                            focusedLabelColor = colors.accentGold,
                            unfocusedLabelColor = colors.textSecondary
                        )

                        if (isCreateAccountMode) {
                            OutlinedTextField(
                                value = usernameInput,
                                onValueChange = { usernameInput = it },
                                label = { Text("Golfer Display Name") },
                                singleLine = true,
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                                colors = fieldColors,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("email_auth_username_input")
                            )
                        }

                        OutlinedTextField(
                            value = emailInput,
                            onValueChange = { emailInput = it },
                            label = { Text("Email Address") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_auth_email_input")
                        )

                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text("Password (min 6 chars)") },
                            singleLine = true,
                            visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(onClick = { showPassword = !showPassword }) {
                                    Icon(
                                        imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = if (showPassword) "Hide password" else "Show password"
                                    )
                                }
                            },
                            colors = fieldColors,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("email_auth_password_input")
                        )

                        Button(
                            onClick = {
                                if (!isLoading) {
                                    val manager = authManager
                                    if (manager == null) {
                                        errorMessage = "Authentication service unavailable offline. Continue as Guest below."
                                    } else {
                                        isLoading = true
                                        errorMessage = null
                                        infoMessage = null
                                        scope.launch {
                                            val res = if (isCreateAccountMode) {
                                                manager.createAccountWithEmail(emailInput, passwordInput, usernameInput)
                                            } else {
                                                manager.signInWithEmail(emailInput, passwordInput)
                                            }
                                            isLoading = false
                                            res.onSuccess { onSignInSuccess() }
                                                .onFailure { err ->
                                                    errorMessage = err.localizedMessage ?: "Authentication failed."
                                                }
                                        }
                                    }
                                }
                            },
                            enabled = !isLoading,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentGold,
                                contentColor = colors.onGold
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("email_auth_submit_btn")
                        ) {
                            Text(
                                text = if (isCreateAccountMode) "CREATE ACCOUNT & TEE OFF" else "SIGN IN",
                                fontFamily = OutfitFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        if (!isCreateAccountMode) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(
                                    onClick = {
                                        val manager = authManager
                                        if (manager == null) {
                                            errorMessage = "Authentication service unavailable offline."
                                        } else {
                                            scope.launch {
                                                manager.sendPasswordReset(emailInput)
                                                    .onSuccess {
                                                        errorMessage = null
                                                        infoMessage = "Password reset email sent to ${emailInput.trim()}."
                                                    }
                                                    .onFailure { err ->
                                                        errorMessage = err.localizedMessage ?: "Enter your email first."
                                                    }
                                            }
                                        }
                                    }
                                ) {
                                    Text("Forgot Password?", fontSize = 12.sp, color = colors.textSecondary)
                                }
                                TextButton(onClick = { showEmailForm = false }) {
                                    Text("Close", fontSize = 12.sp, color = colors.textMuted)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Guest Play (Offline Local Session — links seamlessly upon sign-in)
            OutlinedButton(
                onClick = onContinueAsGuest,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, colors.primaryGreen.copy(alpha = 0.6f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = colors.surfaceElevated,
                    contentColor = colors.textPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("guest_play_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SportsGolf,
                    contentDescription = null,
                    tint = colors.accentSuccess,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CONTINUE AS GUEST (OFFLINE LOCAL PLAY)",
                    fontFamily = OutfitFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Privacy Policy & Terms and Conditions Links
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Privacy Policy",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.accentGold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { showPrivacyDialog = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("signin_privacy_policy_link")
                )
                Text(text = "•", color = colors.textMuted)
                Text(
                    text = "Terms & Conditions",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.accentGold,
                    textDecoration = TextDecoration.Underline,
                    modifier = Modifier
                        .clickable { showTermsDialog = true }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("signin_terms_link")
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = colors.textMuted,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Apex Dynamics Game Studios • Firebase Auth & Local Safeguard",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.sp,
                    color = colors.textMuted
                )
            }
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = colors.surface,
            title = {
                Text(
                    text = "Zenith Golf Privacy Policy",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "Apex Dynamics Game Studios stores only your golfer profile, scores, inventory, and settings in Firebase Firestore (for signed-in accounts) and encrypted on-device local storage (for offline/guest play). You may export, sync, or permanently delete your account data anytime inside Settings > Account & Privacy.",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("CLOSE", fontWeight = FontWeight.Bold, color = colors.accentGold)
                }
            }
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            containerColor = colors.surface,
            title = {
                Text(
                    text = "Terms & Conditions",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary
                )
            },
            text = {
                Text(
                    text = "By playing Zenith Golf 2.0, you agree to fair tournament play. Official tournament scores and virtual currency rewards are validated server-side against physics telemetry. Virtual coins have no monetary cash value outside the game.",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("ACCEPT", fontWeight = FontWeight.Bold, color = colors.accentGold)
                }
            }
        )
    }
}

@Composable
private fun SignInFeaturePill(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    desc: String
) {
    val colors = ZenithTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontFamily = OutfitFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = desc,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    lineHeight = 15.sp
                )
            }
        }
    }
}
