package com.example.game.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.AiDifficultyTier
import com.example.game.model.CameraViewMode
import com.example.game.model.GraphicsQualityTier
import com.example.game.model.PlayAchievement
import com.example.game.model.PlayConsoleSettings
import com.example.game.model.ZenithThemeMode
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ZenithTheme

@Composable
fun PlaySettingsScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onUpdateSettings: (PlayConsoleSettings) -> Unit,
    onResetProgress: () -> Unit,
    onSyncToCloud: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onDeleteAccount: () -> Unit = {},
    onRequestAccountSignIn: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val colors = ZenithTheme.colors
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val settings = uiState.playSettings

    // 0 = All, 1 = Graphics & Theme, 2 = Audio, 3 = Controls & Camera, 4 = Notifications, 5 = Account, 6 = Privacy, 7 = Support
    var selectedCategory by remember { mutableIntStateOf(0) }

    var showResetDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    var showHelpFaqDialog by remember { mutableStateOf(false) }
    var showAppSigningDetails by remember { mutableStateOf(false) }
    var isCheckingUpdates by remember { mutableStateOf(false) }
    var updateCheckResult by remember { mutableStateOf<String?>(null) }
    var userRatingStars by remember { mutableIntStateOf(5) }

    val categories = listOf(
        "ALL",
        "GRAPHICS",
        "AUDIO",
        "CONTROLS",
        "NOTIFICATIONS",
        "ACCOUNT",
        "PRIVACY",
        "SUPPORT"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .testTag("play_settings_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("play_settings_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "APEX DYNAMICS • SYSTEM CONFIG",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentGold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "SETTINGS & ACCOUNT",
                            fontFamily = OutfitFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.forestCard,
                    border = BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.7f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Android, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "v${settings.versionName}",
                            fontFamily = OutfitFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.onForestCard
                        )
                    }
                }
            }
        }

        // Category Filter Pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEachIndexed { idx, label ->
                val isSelected = selectedCategory == idx
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) colors.accentGold else colors.surface,
                    border = BorderStroke(1.dp, if (isSelected) colors.accentGold else colors.border),
                    modifier = Modifier
                        .height(48.dp)
                        .clickable { selectedCategory = idx }
                        .testTag("settings_cat_$idx")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 14.dp)
                    ) {
                        Text(
                            text = label,
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isSelected) colors.onGold else colors.textPrimary
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. GRAPHICS, THEME & LANGUAGE
            if (selectedCategory == 0 || selectedCategory == 1) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "1. APPEARANCE, GRAPHICS & LANGUAGE",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentGold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interface Theme Mode
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BrightnessMedium, contentDescription = null, tint = colors.accentInfo, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "INTERFACE THEME (LIGHT / DARK)",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.textSecondary,
                                    letterSpacing = 0.8.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ZenithThemeMode.entries.forEach { mode ->
                                    val selected = settings.themeMode == mode
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) colors.forestCard else colors.surfaceElevated,
                                        border = BorderStroke(1.dp, if (selected) colors.accentGold else colors.border),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(48.dp)
                                            .clickable { onUpdateSettings(settings.copy(themeMode = mode)) }
                                            .testTag("theme_mode_${mode.name.lowercase()}")
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = mode.label,
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (selected) colors.accentGold else colors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "DEVICE GRAPHICS SCALING TIER",
                                fontFamily = OutfitFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.textSecondary,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                GraphicsQualityTier.entries.forEach { tier ->
                                    val selected = settings.graphicsTier == tier
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) colors.forestCard else colors.surfaceElevated,
                                        border = BorderStroke(
                                            1.dp,
                                            if (selected) colors.accentGold else colors.border
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                onUpdateSettings(
                                                    settings.copy(
                                                        graphicsTier = tier,
                                                        isHighFpsMode = tier != GraphicsQualityTier.LOW_BATTERY
                                                    )
                                                )
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = tier.label.substringBefore(" ("),
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (selected) colors.accentGold else colors.textPrimary
                                            )
                                            Text(
                                                text = "${tier.targetFps} FPS",
                                                fontFamily = PlusJakartaSansFontFamily,
                                                fontSize = 9.sp,
                                                color = if (selected) colors.onForestSecondary else colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Language, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "DISPLAY LANGUAGE",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("English", "Spanish", "French", "Japanese").forEach { lang ->
                                    val selected = settings.languageTag == lang
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) colors.forestCard else colors.surfaceElevated,
                                        border = BorderStroke(1.dp, if (selected) colors.accentGold else colors.border),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .clickable { onUpdateSettings(settings.copy(languageTag = lang)) }
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(horizontal = 14.dp)
                                        ) {
                                            Text(
                                                text = lang,
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selected) colors.accentGold else colors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            SettingToggleRow(
                                title = "High Frame Rate Mode (60/120 FPS)",
                                subtitle = "Ultra-smooth 3D ball flight trajectories and camera tracking",
                                isChecked = settings.isHighFpsMode,
                                onCheckedChange = { onUpdateSettings(settings.copy(isHighFpsMode = it)) }
                            )

                            SettingToggleRow(
                                title = "Putting Green Contour Grid & Slope Break",
                                subtitle = "Overlay topographical grid and break vector arrows on putting greens",
                                isChecked = settings.isPuttingGridVisible,
                                onCheckedChange = { onUpdateSettings(settings.copy(isPuttingGridVisible = it)) }
                            )

                            SettingToggleRow(
                                title = "Live Putting Slope Radar HUD",
                                subtitle = "Display break angles and distance pills while lining up putts",
                                isChecked = settings.isSlopeBreakRadarVisible,
                                onCheckedChange = { onUpdateSettings(settings.copy(isSlopeBreakRadarVisible = it)) }
                            )
                        }
                    }
                }
            }

            // 2. AUDIO & HAPTICS SETTINGS
            if (selectedCategory == 0 || selectedCategory == 2) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = colors.accentSuccess, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "2. AUDIO, MUSIC & HAPTICS",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentSuccess,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            SettingToggleRow(
                                title = "Master Game Sound Effects",
                                subtitle = "Club strike acoustics, cup drop rattle & fan cheers",
                                isChecked = settings.isSoundEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(isSoundEnabled = it)) }
                            )

                            SliderControlRow(
                                label = "Sound Effects Volume",
                                valueText = "${(settings.soundVolume * 100).toInt()}%",
                                value = settings.soundVolume,
                                onValueChange = { onUpdateSettings(settings.copy(soundVolume = it)) }
                            )

                            SliderControlRow(
                                label = "Clubhouse Music Volume",
                                valueText = "${(settings.musicVolume * 100).toInt()}%",
                                value = settings.musicVolume,
                                onValueChange = { onUpdateSettings(settings.copy(musicVolume = it)) }
                            )

                            SettingToggleRow(
                                title = "Haptic Vibration Feedback",
                                subtitle = "Tactile impulse on club impact, bunker splash, and cup drops",
                                isChecked = settings.isHapticsEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(isHapticsEnabled = it)) }
                            )

                            SettingToggleRow(
                                title = "AI Caddie Audio Commentary",
                                subtitle = "Tactical audio callouts before critical approach shots",
                                isChecked = settings.isCaddieAudioTipsEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(isCaddieAudioTipsEnabled = it)) }
                            )
                        }
                    }
                }
            }

            // 3. CONTROLS, CAMERA & AI OPPONENTS
            if (selectedCategory == 0 || selectedCategory == 3) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Gamepad, contentDescription = null, tint = colors.accentInfo, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "3. SWING CONTROLS, CAMERA & AI",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentInfo,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            SliderControlRow(
                                label = "Touch Drag & Aim Sensitivity",
                                valueText = String.format("%.2fx", settings.controlSensitivity),
                                value = ((settings.controlSensitivity - 0.5f) / 1.5f).coerceIn(0f, 1f),
                                onValueChange = {
                                    onUpdateSettings(settings.copy(controlSensitivity = 0.5f + it * 1.5f))
                                }
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "DEFAULT COURSE CAMERA VIEW",
                                fontFamily = OutfitFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentGold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CameraViewMode.entries.forEach { cam ->
                                    val selected = settings.preferredCameraMode == cam
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) colors.forestCard else colors.surfaceElevated,
                                        border = BorderStroke(1.dp, if (selected) colors.accentGold else colors.border),
                                        modifier = Modifier
                                            .height(48.dp)
                                            .clickable { onUpdateSettings(settings.copy(preferredCameraMode = cam)) }
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.padding(horizontal = 12.dp)
                                        ) {
                                            Text(
                                                text = cam.label,
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selected) colors.accentGold else colors.textPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            PlayConfigField(label = "Primary Swing Input", value = "Drag-and-Release Pullback")
                            PlayConfigField(label = "Safety Dead-Zone Radius", value = "28px Cancel Zone Around Ball")
                            PlayConfigField(label = "Club Power Cap", value = "Dynamic per Club (PT 40y – 1W 310y)")

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "AI TOUR OPPONENT DIFFICULTY",
                                fontFamily = OutfitFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentGold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                AiDifficultyTier.entries.forEach { diff ->
                                    val selected = settings.aiDifficulty == diff
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = if (selected) colors.forestCard else colors.surfaceElevated,
                                        border = BorderStroke(
                                            1.dp,
                                            if (selected) colors.accentGold else colors.border
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                onUpdateSettings(settings.copy(aiDifficulty = diff))
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = diff.label,
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (selected) colors.accentGold else colors.textPrimary
                                            )
                                            Text(
                                                text = "Pace ${if (diff.strokeModifier > 0) "+${diff.strokeModifier}" else "${diff.strokeModifier}"}",
                                                fontFamily = PlusJakartaSansFontFamily,
                                                fontSize = 9.sp,
                                                color = if (selected) colors.onForestSecondary else colors.textSecondary
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 4. NOTIFICATIONS SETTINGS
            if (selectedCategory == 0 || selectedCategory == 4) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "4. NOTIFICATIONS & ALERTS",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentGold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            SettingToggleRow(
                                title = "Championship Cup & Event Alerts",
                                subtitle = "Notify when new Tour Cups or leaderboard milestones open",
                                isChecked = settings.notificationsTourEvents,
                                onCheckedChange = { onUpdateSettings(settings.copy(notificationsTourEvents = it)) }
                            )

                            SettingToggleRow(
                                title = "Daily Lucky Wheel & Mission Reminders",
                                subtitle = "Alert when your daily free roulette spin and quests refresh",
                                isChecked = settings.notificationsDailyWheel,
                                onCheckedChange = { onUpdateSettings(settings.copy(notificationsDailyWheel = it)) }
                            )
                        }
                    }
                }
            }

            // 5. ACCOUNT & CLOUD SYNC SETTINGS
            if (selectedCategory == 0 || selectedCategory == 5) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.55f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "5. ACCOUNT & CLOUD SAVING",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colors.accentGold,
                                        letterSpacing = 1.sp
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = colors.forestCard
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (uiState.isGuestSession) "GUEST" else "VERIFIED",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colors.onForestCard
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            if (uiState.isGuestSession) {
                                Button(
                                    onClick = onRequestAccountSignIn,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.accentGold,
                                        contentColor = colors.onGold
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .padding(bottom = 8.dp)
                                        .testTag("settings_link_account_btn")
                                ) {
                                    Text("LINK GOOGLE OR EMAIL ACCOUNT", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            PlayConfigField(label = "Player ID", value = uiState.cloudSyncUserId.ifBlank { "Local Profile" })
                            PlayConfigField(label = "Golfer Account", value = uiState.golferProfile.golferName)
                            PlayConfigField(label = "Authenticated Email", value = uiState.cloudSyncUserEmail.ifBlank { if (uiState.isGuestSession) "Guest Session" else "Account Linked" })
                            PlayConfigField(
                                label = "Cloud Sync Status",
                                value = if (uiState.isCloudSyncInProgress) "Synchronizing 12 Collections..." else uiState.cloudSyncStatus
                            )
                            PlayConfigField(label = "Firestore Collections", value = "12 Active (Zero-Trust Rules)")
                            PlayConfigField(label = "Firebase Project ID", value = settings.firebaseProjectId)

                            if (uiState.cloudSyncError != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = colors.error.copy(alpha = 0.16f),
                                    border = BorderStroke(1.dp, colors.error.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = "Cloud Sync Alert: ${uiState.cloudSyncError}",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 11.sp,
                                        color = colors.textPrimary,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            SettingToggleRow(
                                title = "Automatic Cloud Save Synchronization",
                                subtitle = "Sync career trophies, XP, inventory, and leaderboard scores to Firestore",
                                isChecked = settings.isCloudSyncEnabled,
                                onCheckedChange = { onUpdateSettings(settings.copy(isCloudSyncEnabled = it)) }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onSyncToCloud,
                                    enabled = !uiState.isCloudSyncInProgress,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.forestCard,
                                        contentColor = colors.onForestCard
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, colors.accentGold),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("sync_cloud_now_btn")
                                ) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (uiState.isCloudSyncInProgress) "SYNCING..." else "SYNC CLOUD",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }

                                Button(
                                    onClick = onSignOut,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.surfaceElevated,
                                        contentColor = colors.textPrimary
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, colors.border),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("sign_out_google_btn")
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SIGN OUT", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            if (uiState.accountDeletionMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    color = colors.error.copy(alpha = 0.14f),
                                    border = BorderStroke(1.dp, colors.error.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = uiState.accountDeletionMessage,
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 11.sp,
                                        color = colors.textPrimary,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { showDeleteAccountDialog = true },
                                enabled = !uiState.isDeletingAccount,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.error.copy(alpha = 0.14f),
                                    contentColor = colors.error
                                ),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, colors.error.copy(alpha = 0.65f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("delete_account_btn")
                            ) {
                                Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.isDeletingAccount) "DELETING ACCOUNT..." else "DELETE ACCOUNT & CLOUD DATA",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            // 6. PRIVACY & DATA SAFETY
            if (selectedCategory == 0 || selectedCategory == 6) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Security, contentDescription = null, tint = colors.accentSuccess, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "6. PRIVACY, CONSENT & DATA CONTROLS",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentSuccess,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Zenith Golf collects zero third-party advertising IDs or background location telemetry. You retain full control to inspect Data Safety disclosures or permanently delete your account.",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 12.sp,
                                color = colors.textSecondary,
                                lineHeight = 16.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showPrivacyDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.surfaceElevated,
                                        contentColor = colors.accentInfo
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("privacy_policy_btn")
                                ) {
                                    Icon(Icons.Default.Policy, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("DATA SAFETY", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = { showResetDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.error.copy(alpha = 0.14f),
                                        contentColor = colors.error
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("reset_progress_btn")
                                ) {
                                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("RESET LOCAL", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 7. SUPPORT, HELP FAQ & ABOUT ZENITH GOLF
            if (selectedCategory == 0 || selectedCategory == 7) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.border)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.AutoMirrored.Filled.HelpOutline, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "7. HELP, SUPPORT & ABOUT ZENITH GOLF",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentGold,
                                    letterSpacing = 1.sp
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            PlayConfigField(label = "Developer Studio", value = settings.studioName)
                            PlayConfigField(label = "Release Version", value = "Zenith Golf ${settings.versionName} (Build ${settings.versionCode})")
                            PlayConfigField(label = "Support Contact", value = settings.developerEmail)
                            PlayConfigField(label = "Application ID", value = settings.packageName)
                            PlayConfigField(label = "Target SDK / Arch", value = "API ${settings.targetSdk} • ${settings.arm64Architecture}")

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { showHelpFaqDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.forestCard,
                                        contentColor = colors.onForestCard
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("help_faq_btn")
                                ) {
                                    Text("HELP & FAQ", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }

                                Button(
                                    onClick = {
                                        isCheckingUpdates = true
                                        updateCheckResult = "Google Play Core: Running latest v${settings.versionName} (Code ${settings.versionCode})."
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.surfaceElevated,
                                        contentColor = colors.accentInfo
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("check_updates_btn")
                                ) {
                                    Icon(Icons.Default.SystemUpdate, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isCheckingUpdates) "UP TO DATE" else "UPDATES",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = { showRatingDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = colors.accentGold,
                                        contentColor = colors.onGold
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("rate_play_store_btn")
                                ) {
                                    Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("RATE", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                }
                            }

                            if (updateCheckResult != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = updateCheckResult!!,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp,
                                    color = colors.accentSuccess,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Key, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "PLAY APP SIGNING CERTIFICATES",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colors.accentGold
                                    )
                                }
                                Text(
                                    text = if (showAppSigningDetails) "HIDE" else "VIEW SHA KEYS",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accentInfo,
                                    modifier = Modifier
                                        .clickable { showAppSigningDetails = !showAppSigningDetails }
                                        .padding(6.dp)
                                )
                            }

                            AnimatedVisibility(
                                visible = showAppSigningDetails,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(modifier = Modifier.padding(top = 8.dp)) {
                                    FingerprintRow(
                                        label = "SHA-1 FINGERPRINT",
                                        value = settings.sha1Fingerprint,
                                        onCopy = {
                                            clipboardManager.setText(AnnotatedString(settings.sha1Fingerprint))
                                            Toast.makeText(context, "SHA-1 copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    FingerprintRow(
                                        label = "SHA-256 FINGERPRINT",
                                        value = settings.sha256Fingerprint,
                                        onCopy = {
                                            clipboardManager.setText(AnnotatedString(settings.sha256Fingerprint))
                                            Toast.makeText(context, "SHA-256 copied to clipboard", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    if (showHelpFaqDialog) {
        AlertDialog(
            onDismissRequest = { showHelpFaqDialog = false },
            containerColor = colors.surfaceElevated,
            title = { Text("Zenith Golf 2.0 Help & FAQ", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Black, color = colors.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• How do I swing? Drag back from the golf ball toward your target distance or use AIM SWING + CONFIRM SHOT.", fontSize = 12.sp, color = colors.textSecondary)
                    Text("• What affects the ball? Wind speed/direction, elevation slopes, fairway/rough/bunker friction, and sidespin.", fontSize = 12.sp, color = colors.textSecondary)
                    Text("• How does Cloud Save work? Sign in via Google or Email to sync 12 Firestore collections across devices.", fontSize = 12.sp, color = colors.textSecondary)
                    Text("• Support: support@zenithgolf.app • Apex Dynamics Game Studios.", fontSize = 12.sp, color = colors.accentGold)
                }
            },
            confirmButton = {
                TextButton(onClick = { showHelpFaqDialog = false }) {
                    Text("CLOSE", color = colors.accentGold, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(text = "Reset Local Game Data?", fontWeight = FontWeight.Black, color = colors.textPrimary) },
            text = { Text(text = "This wipes local tour career records, XP, and coin balances on this device. To permanently remove cloud records and your Firebase account, use Delete Account.", color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        onResetProgress()
                        showResetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error)
                ) {
                    Text("YES, RESET DATA", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("CANCEL", color = colors.textSecondary)
                }
            },
            containerColor = colors.surfaceElevated
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            title = { Text(text = "Permanently Delete Account?", fontWeight = FontWeight.Black, color = colors.error) },
            text = {
                Column {
                    Text(
                        text = "This permanently deletes your Firebase Authentication account, local saves, and Firestore profile, stats, inventory, progression, settings, match history, achievements, challenges, and leaderboard entries.",
                        color = colors.textSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Immutable security audit and anti-cheat verification logs remain anonymized only as required for tournament integrity. This action cannot be undone.",
                        color = colors.textMuted,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        onDeleteAccount()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.error),
                    modifier = Modifier.testTag("confirm_delete_account_btn")
                ) {
                    Text("PERMANENTLY DELETE", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("CANCEL", color = colors.textSecondary)
                }
            },
            containerColor = colors.surfaceElevated
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(text = "Google Play Data Safety & Privacy", fontWeight = FontWeight.Black, color = colors.textPrimary) },
            text = {
                Column {
                    Text(text = "1. Data Collected: Sign-In name, email, avatar URL & Firestore game saves (trophies, XP, inventory, match history).", color = colors.textSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "2. Privacy & Zero Ads: Zero Advertising ID (AD_ID removed), zero location tracking, and zero data sold or shared with ad networks.", color = colors.textSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = "3. Encryption & Deletion: Encrypted in transit (TLS). Delete your account and personal cloud data anytime from Settings -> Account.", color = colors.textSecondary, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPrivacyDialog = false
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(settings.privacyPolicyUrl))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            Toast.makeText(context, "Privacy Policy: ${settings.privacyPolicyUrl}", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold, contentColor = colors.onGold)
                ) {
                    Text("PRIVACY POLICY", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("CLOSE", color = colors.textSecondary)
                }
            },
            containerColor = colors.surfaceElevated
        )
    }

    if (showRatingDialog) {
        AlertDialog(
            onDismissRequest = { showRatingDialog = false },
            title = { Text(text = "Rate Zenith Golf 2.0", fontWeight = FontWeight.Black, color = colors.accentGold) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.Center) {
                        for (star in 1..5) {
                            IconButton(onClick = { userRatingStars = star }, modifier = Modifier.size(48.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "$star stars",
                                    tint = if (star <= userRatingStars) colors.accentGold else colors.textMuted,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your rating: $userRatingStars Stars! Reviews on Google Play help other golfers discover the championship links.",
                        color = colors.textSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRatingDialog = false
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${settings.packageName}"))
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(intent)
                        } catch (_: Exception) {
                            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${settings.packageName}"))
                            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            context.startActivity(webIntent)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold, contentColor = colors.onGold)
                ) {
                    Text("SUBMIT REVIEW", fontWeight = FontWeight.Black, fontSize = 11.sp)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRatingDialog = false }) {
                    Text("MAYBE LATER", color = colors.textSecondary)
                }
            },
            containerColor = colors.surfaceElevated
        )
    }
}

@Composable
private fun SliderControlRow(
    label: String,
    valueText: String,
    value: Float,
    onValueChange: (Float) -> Unit
) {
    val colors = ZenithTheme.colors
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = valueText,
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp,
                color = colors.accentGold,
                fontWeight = FontWeight.Black
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            colors = SliderDefaults.colors(
                thumbColor = colors.accentGold,
                activeTrackColor = colors.accentGold,
                inactiveTrackColor = colors.surfaceElevated
            )
        )
    }
}

@Composable
fun PlayConfigField(label: String, value: String) {
    val colors = ZenithTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontFamily = PlusJakartaSansFontFamily, fontSize = 11.sp, color = colors.textSecondary)
        Text(text = value, fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.accentGold)
    }
}

@Composable
fun FingerprintRow(label: String, value: String, onCopy: () -> Unit) {
    val colors = ZenithTheme.colors
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = colors.surfaceElevated,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 10.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, fontFamily = OutfitFontFamily, fontSize = 9.sp, fontWeight = FontWeight.Black, color = colors.accentGold)
                Text(text = value, fontFamily = PlusJakartaSansFontFamily, fontSize = 10.sp, color = colors.textPrimary, fontWeight = FontWeight.Medium)
            }
            IconButton(onClick = onCopy, modifier = Modifier.size(48.dp)) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = colors.accentInfo, modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
fun SettingToggleRow(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val colors = ZenithTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontFamily = OutfitFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Text(text = subtitle, fontFamily = PlusJakartaSansFontFamily, fontSize = 11.sp, color = colors.textSecondary, lineHeight = 15.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.onGold,
                checkedTrackColor = colors.accentGold,
                uncheckedThumbColor = colors.textMuted,
                uncheckedTrackColor = colors.surfaceElevated
            )
        )
    }
}

@Composable
fun AchievementItemRow(achievement: PlayAchievement) {
    val colors = ZenithTheme.colors
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = colors.surfaceElevated,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (achievement.isUnlocked) colors.accentGold.copy(alpha = 0.2f) else colors.border.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (achievement.isUnlocked) Icons.Default.EmojiEvents else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (achievement.isUnlocked) colors.accentGold else colors.textMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = achievement.title,
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (achievement.isUnlocked) colors.textPrimary else colors.textSecondary
                    )
                    Text(
                        text = achievement.description,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 10.sp,
                        color = colors.textSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (achievement.isUnlocked) colors.accentSuccess.copy(alpha = 0.2f) else colors.border.copy(alpha = 0.4f)
            ) {
                Text(
                    text = if (achievement.isUnlocked) "+${achievement.xpValue} XP" else "LOCKED",
                    fontFamily = OutfitFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = if (achievement.isUnlocked) colors.accentSuccess else colors.textMuted,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
