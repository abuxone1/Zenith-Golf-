package com.example.game.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.game.model.AppNotificationItem
import com.example.game.model.GolferStance
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ZenithDeepForestGreen
import com.example.ui.theme.ZenithEmerald
import com.example.ui.theme.ZenithNeonGreen
import com.example.ui.theme.ZenithPremiumGold
import com.example.ui.theme.ZenithTheme

@Composable
fun HomeScreen(
    uiState: GameUiState,
    onStartTournament: () -> Unit,
    onStartThreeHolePrototype: () -> Unit = {},
    onStartPractice: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    onClaimDailyChallenge: (String) -> Unit = {},
    onClaimDailyLoginReward: () -> Unit = {},
    onDismissOnboarding: () -> Unit = {},
    onMarkNotificationsRead: () -> Unit = {},
    onRequestAccountSignIn: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val colors = ZenithTheme.colors
    var showWhatsNewDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }

    val playerLevel = (uiState.career.tourXp / 200) + 1
    val xpIntoLevel = uiState.career.tourXp % 200
    val levelProgress = (xpIntoLevel / 200f).coerceIn(0.08f, 1f)
    val golferOvr = (uiState.golferProfile.powerAttribute +
            uiState.golferProfile.accuracyAttribute +
            uiState.golferProfile.spinControlAttribute +
            uiState.golferProfile.puttingAttribute) / 4

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .verticalScroll(scrollState)
            .padding(bottom = 28.dp)
            .testTag("home_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.fillMaxWidth().widthIn(max = 860.dp)) {
            // 1. TOP PLAYER HEADER BAR
            PlayerDashboardTopHeader(
                username = uiState.golferProfile.golferName,
                countryTag = uiState.golferProfile.countryTag,
                playerLevel = playerLevel,
                levelProgress = levelProgress,
                golferOvr = golferOvr,
                visorColor = Color(uiState.golferProfile.visorColorHex),
                poloColor = Color(uiState.golferProfile.poloColorHex),
                skinColor = Color(uiState.golferProfile.skinToneHex),
                isLeftHanded = uiState.golferProfile.stance == GolferStance.LEFT_HANDED,
                coins = uiState.career.coins,
                trophies = uiState.career.trophies,
                unreadNotifications = uiState.unreadNotificationsCount,
                isGuestSession = uiState.isGuestSession,
                isOnline = uiState.isOnline,
                onAvatarClick = { onNavigate(AppScreen.CAREER_STATS) },
                onCoinsClick = { onNavigate(AppScreen.PRO_SHOP) },
                onNotificationsClick = {
                    onMarkNotificationsRead()
                    showNotificationsDialog = true
                },
                onOpenSettings = { onNavigate(AppScreen.PLAY_SETTINGS) },
                onGuestSignInClick = onRequestAccountSignIn
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. FIRST-TIME INTERACTIVE GUIDE BANNER (Dismissible)
            if (!uiState.playSettings.hasCompletedOnboarding) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = colors.forestCard,
                    border = BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 12.dp)
                        .testTag("home_onboarding_guide")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(colors.accentGold.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = colors.accentGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "WELCOME TO ZENITH GOLF 2.0",
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentGold
                            )
                            Text(
                                text = "Tap PLAY GOLF to tee off, customize your gear in Pro Shop, or switch Light/Dark themes anytime in Settings.",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = colors.onForestSecondary,
                                lineHeight = 15.sp
                            )
                        }
                        IconButton(
                            onClick = onDismissOnboarding,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("dismiss_onboarding_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss guide",
                                tint = colors.onForestCard
                            )
                        }
                    }
                }
            }

            // 3. HERO COURSE CARD & PLAY GOLF CTA
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, colors.accentGold.copy(alpha = 0.65f)),
                colors = CardDefaults.cardColors(containerColor = ZenithDeepForestGreen)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(242.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_course_coastal_dunes_1794835040452),
                        contentDescription = "Zenith Dunes Championship Course",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.25f),
                                        ZenithDeepForestGreen.copy(alpha = 0.75f),
                                        Color.Black.copy(alpha = 0.92f)
                                    )
                                )
                            )
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ZenithDeepForestGreen.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, ZenithPremiumGold)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = ZenithPremiumGold,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = uiState.selectedCup.title.uppercase(),
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ZenithPremiumGold,
                                    letterSpacing = 0.8.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = ZenithDeepForestGreen.copy(alpha = 0.9f),
                            border = BorderStroke(1.dp, ZenithNeonGreen),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .clickable { onNavigate(AppScreen.COURSE_SELECTION) }
                                .testTag("change_course_chip")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = "Browse Courses",
                                    tint = ZenithNeonGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "COURSES",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "ZENITH CHAMPIONSHIP TOUR",
                            fontFamily = OutfitFontFamily,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "${uiState.selectedCup.courseName} • ${uiState.selectedCup.holesCount} Holes • Purse ${uiState.selectedCup.prizeCoins} Coins",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.85f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onStartTournament,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ZenithPremiumGold,
                                    contentColor = Color(0xFF1A1200)
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1.35f)
                                    .height(52.dp)
                                    .testTag("play_tournament_cta")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PLAY GOLF",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 1.sp
                                )
                            }

                            OutlinedButton(
                                onClick = onStartThreeHolePrototype,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.5.dp, ZenithNeonGreen),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = ZenithDeepForestGreen.copy(alpha = 0.88f),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("play_three_hole_prototype_cta")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SportsGolf,
                                    contentDescription = null,
                                    tint = ZenithNeonGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "3-HOLE QUICK",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. GAME MODE SHORTCUTS (Quick Play, Career Mode, Tournament, Practice, Multiplayer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "GAME MODES & TOUR HUB",
                    fontFamily = OutfitFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.accentGold,
                    letterSpacing = 1.2.sp,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clickable { onNavigate(AppScreen.GAME_MODES) }
                        .padding(horizontal = 8.dp)
                        .testTag("home_view_all_modes_btn")
                ) {
                    Text(
                        text = "VIEW ALL MODES →",
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accentSuccess,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardQuickModeTile(
                        title = "Quick Play",
                        subtitle = "3-Hole Par 4•3•5 Express",
                        badge = "INSTANT TEE",
                        icon = Icons.Default.SportsGolf,
                        accentColor = colors.accentSuccess,
                        testTag = "home_quick_play_card",
                        onClick = onStartThreeHolePrototype,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardQuickModeTile(
                        title = "Career Mode",
                        subtitle = "${uiState.career.roundsPlayed} Rounds • ${uiState.career.trophies} Cups",
                        badge = "SEASON TOUR",
                        icon = Icons.Default.WorkspacePremium,
                        accentColor = colors.accentGold,
                        testTag = "home_career_mode_card",
                        onClick = { onNavigate(AppScreen.COURSE_SELECTION) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardQuickModeTile(
                        title = "Tournament",
                        subtitle = "3 Official Cups & Top 25",
                        badge = "LIVE EVENTS",
                        icon = Icons.Default.EmojiEvents,
                        accentColor = colors.accentGold,
                        testTag = "home_tournaments_card",
                        onClick = { onNavigate(AppScreen.CUP_SELECT) },
                        modifier = Modifier.weight(1f)
                    )
                    DashboardQuickModeTile(
                        title = "Practice",
                        subtitle = "Driving Range & Shootout",
                        badge = "TRAINING",
                        icon = Icons.Default.TrackChanges,
                        accentColor = ZenithEmerald,
                        testTag = "home_practice_card",
                        onClick = onStartPractice,
                        modifier = Modifier.weight(1f)
                    )
                }

                DashboardQuickModeTile(
                    title = "Multiplayer & 1v1 Match Play",
                    subtitle = "Head-to-head rival duels (${uiState.career.matchPlayWins}W - ${uiState.career.matchPlayLosses}L) & Cloud Top 25",
                    badge = "MULTIPLAYER",
                    icon = Icons.Default.Group,
                    accentColor = colors.accentInfo,
                    testTag = "home_multiplayer_card",
                    onClick = { onNavigate(AppScreen.MATCH_PLAY_DUEL) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(22.dp))

            // 5. DAILY ACTIVITY (Daily Reward, Daily Challenges, Current Tournament, Latest Achievement)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = colors.accentSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "DAILY ACTIVITY",
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary,
                        letterSpacing = 1.1.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clickable { onNavigate(AppScreen.DAILY_CHALLENGES) }
                        .padding(horizontal = 8.dp)
                        .testTag("home_open_daily_challenges_btn")
                ) {
                    Text(
                        text = "REWARDS HUB →",
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accentGold,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Daily Login Reward Streak Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.forestCard,
                border = BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.7f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 10.dp)
                    .testTag("home_daily_reward_card")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(colors.accentGold.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CardGiftcard,
                                contentDescription = "Daily Reward",
                                tint = colors.accentGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "DAILY REWARD • DAY ${uiState.dailyLoginStreak} STREAK",
                                fontFamily = OutfitFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.onForestCard
                            )
                            Text(
                                text = if (uiState.isDailyRewardClaimedToday) {
                                    "Claimed today! Return tomorrow for Day ${(uiState.dailyLoginStreak % 7) + 1} bonus."
                                } else {
                                    "Tap Claim for +${100 + (uiState.dailyLoginStreak * 50)} Coins & Tour XP"
                                },
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = colors.onForestSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onClaimDailyLoginReward,
                        enabled = !uiState.isDailyRewardClaimedToday,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accentGold,
                            contentColor = colors.onGold,
                            disabledContainerColor = colors.surfaceElevated.copy(alpha = 0.25f),
                            disabledContentColor = colors.onForestSecondary
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("home_claim_daily_reward_btn")
                    ) {
                        Text(
                            text = if (uiState.isDailyRewardClaimedToday) "CLAIMED" else "CLAIM",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .testTag("home_daily_challenges_section"),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                uiState.dailyChallenges.take(2).forEach { challenge ->
                    val isClaimable = challenge.isCompleted && !challenge.isClaimed
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(
                            1.dp,
                            when {
                                isClaimable -> colors.accentGold
                                challenge.isClaimed -> colors.border
                                else -> colors.primaryGreen.copy(alpha = 0.55f)
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isClaimable) onClaimDailyChallenge(challenge.id)
                                else onNavigate(AppScreen.DAILY_CHALLENGES)
                            }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = challenge.title,
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = colors.accentGold.copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = "+${challenge.rewardCoins} COINS",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colors.accentGold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = challenge.description,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = {
                                        if (challenge.isCompleted) 1f
                                        else (challenge.currentProgress.toFloat() / challenge.targetGoal.coerceAtLeast(1)).coerceIn(0.1f, 1f)
                                    },
                                    color = if (challenge.isCompleted) colors.accentSuccess else colors.accentGold,
                                    trackColor = colors.surfaceElevated,
                                    modifier = Modifier
                                        .fillMaxWidth(0.85f)
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = when {
                                    challenge.isClaimed -> colors.surfaceElevated
                                    isClaimable -> colors.accentGold
                                    else -> colors.primaryGreen
                                },
                                border = BorderStroke(
                                    1.dp,
                                    if (isClaimable) colors.accentGold else colors.border
                                )
                            ) {
                                Text(
                                    text = when {
                                        challenge.isClaimed -> "CLAIMED"
                                        isClaimable -> "CLAIM"
                                        else -> "GO"
                                    },
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = when {
                                        isClaimable -> colors.onGold
                                        challenge.isClaimed -> colors.textMuted
                                        else -> Color.White
                                    },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }

                // Current Featured Tournament Quick Strip
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { onNavigate(AppScreen.CUP_SELECT) }
                        .testTag("home_current_tournament_card")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = colors.accentGold,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f, fill = false)) {
                                Text(
                                    text = "CURRENT TOURNAMENT • ${uiState.selectedCup.title.uppercase()}",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentGold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${uiState.selectedCup.courseName} • Purse +${uiState.selectedCup.prizeCoins} Coins",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open Tournaments",
                            tint = colors.accentGold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. PLAYER PROGRESS SNAPSHOT (Current Level, Recent Results, Career Stats, Upcoming Event)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PLAYER PROGRESS",
                    fontFamily = OutfitFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary,
                    letterSpacing = 1.1.sp,
                    modifier = Modifier.weight(1f, fill = false),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clickable { onNavigate(AppScreen.CAREER_STATS) }
                        .padding(horizontal = 8.dp)
                        .testTag("home_career_profile_btn")
                ) {
                    Text(
                        text = "CAREER PROFILE →",
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accentSuccess,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("home_player_progress_card")
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ProgressStatBox(
                            label = "TOUR LEVEL",
                            value = "LV $playerLevel",
                            sub = "${uiState.career.tourXp} XP",
                            accent = colors.accentGold,
                            modifier = Modifier.weight(1f)
                        )
                        ProgressStatBox(
                            label = "BIRDIES / EAGLES",
                            value = "${uiState.career.birdies} / ${uiState.career.eagles}",
                            sub = "${uiState.career.roundsPlayed} Rounds",
                            accent = colors.accentSuccess,
                            modifier = Modifier.weight(1f)
                        )
                        ProgressStatBox(
                            label = "LONG DRIVE",
                            value = "${uiState.career.longestDriveYds.toInt()}y",
                            sub = "Best Carry",
                            accent = colors.accentInfo,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val latestCloud = uiState.cloudMatchHistory.firstOrNull()
                    val latestReplay = uiState.savedReplays.firstOrNull()
                    val recentSummary = when {
                        latestCloud != null -> "${latestCloud.courseName} • ${latestCloud.totalStrokes} Strokes (${latestCloud.scoreBanner})"
                        latestReplay != null -> "${latestReplay.holeName} • ${latestReplay.carryYards.toInt()} yds (${latestReplay.scoreAchieved})"
                        else -> "Tee off in Quick Play or Career Mode to log round results"
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(colors.surfaceElevated, RoundedCornerShape(12.dp))
                            .clickable { onNavigate(AppScreen.REPLAY_THEATER) }
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "RECENT RESULT & UPCOMING EVENT",
                                fontFamily = OutfitFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentGold
                            )
                            Text(
                                text = recentSummary,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Next Cup: ${uiState.selectedCup.title} • Season 1 ends in ${uiState.tourPassState.daysRemaining}d",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 10.sp,
                                color = colors.textSecondary
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Open Replay Vault",
                            tint = colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7. LATEST ACHIEVEMENT SPOTLIGHT (Compact, decluttered milestone banner)
            val spotlightAchievement = uiState.achievements.firstOrNull { it.isUnlocked } ?: uiState.achievements.firstOrNull()
            if (spotlightAchievement != null) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag("home_recent_achievements_section")
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surface,
                        border = BorderStroke(
                            1.dp,
                            if (spotlightAchievement.isUnlocked) colors.accentGold.copy(alpha = 0.65f) else colors.border
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 56.dp)
                            .clickable { onNavigate(AppScreen.CAREER_STATS) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(
                                        if (spotlightAchievement.isUnlocked) colors.accentGold.copy(alpha = 0.18f) else colors.surfaceElevated,
                                        CircleShape
                                    )
                                    .border(
                                        1.dp,
                                        if (spotlightAchievement.isUnlocked) colors.accentGold else colors.border,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (spotlightAchievement.isUnlocked) Icons.Default.CheckCircle else Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = if (spotlightAchievement.isUnlocked) colors.accentGold else colors.textMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "LATEST ACHIEVEMENT • ${uiState.achievements.count { it.isUnlocked }}/${uiState.achievements.size} UNLOCKED",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentGold,
                                    letterSpacing = 0.6.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = spotlightAchievement.title,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = spotlightAchievement.description,
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp,
                                    color = colors.textMuted,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (spotlightAchievement.isUnlocked) colors.accentGold.copy(alpha = 0.16f) else colors.surfaceElevated
                            ) {
                                Text(
                                    text = if (spotlightAchievement.isUnlocked) "+${spotlightAchievement.xpValue} XP" else "VIEW ALL",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (spotlightAchievement.isUnlocked) colors.accentGold else colors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 8. CLUBHOUSE & PRO STUDIO (2x2 clean grid without duplicate game mode rows)
            Text(
                text = "CLUBHOUSE & PRO STUDIO",
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = colors.accentGold,
                letterSpacing = 1.2.sp,
                modifier = Modifier.padding(horizontal = 18.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardQuickModeTile(
                        title = "Pro Shop",
                        subtitle = "Clubs, Balls, Apparel & Kits",
                        badge = "ARMORY",
                        icon = Icons.Default.ShoppingBag,
                        accentColor = colors.accentGold,
                        testTag = "mode_equipment",
                        onClick = { onNavigate(AppScreen.PRO_SHOP) },
                        modifier = Modifier.weight(1f)
                    )
                    DashboardQuickModeTile(
                        title = "Locker Room",
                        subtitle = "Golfer Style, Stance & Skills",
                        badge = "ATHLETE",
                        icon = Icons.Default.Star,
                        accentColor = colors.accentSuccess,
                        testTag = "mode_locker room",
                        onClick = { onNavigate(AppScreen.GOLFER_LOCKER_ROOM) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DashboardQuickModeTile(
                        title = "Tour Caddie",
                        subtitle = "Live Strategy & Custom Balls",
                        badge = "ADVISOR",
                        icon = Icons.Default.AutoAwesome,
                        accentColor = colors.accentInfo,
                        testTag = "mode_caddie",
                        onClick = { onNavigate(AppScreen.CADDIE_CHAT) },
                        modifier = Modifier.weight(1f)
                    )
                    DashboardQuickModeTile(
                        title = "Shot Vault",
                        subtitle = "Highlights & Telemetry",
                        badge = "REPLAYS",
                        icon = Icons.Default.TrackChanges,
                        accentColor = ZenithEmerald,
                        testTag = "mode_replays",
                        onClick = { onNavigate(AppScreen.REPLAY_THEATER) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "ZENITH GOLF 2.0 • APEX DYNAMICS GAME STUDIOS",
                fontFamily = OutfitFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMuted,
                letterSpacing = 1.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("home_studio_footer")
            )

            if (showWhatsNewDialog) {
                ZenithV2WhatsNewDialog(onDismiss = { showWhatsNewDialog = false })
            }

            if (showNotificationsDialog) {
                NotificationsDialog(
                    notifications = uiState.notifications,
                    onNavigate = { target ->
                        showNotificationsDialog = false
                        val dest = runCatching { AppScreen.valueOf(target) }.getOrDefault(AppScreen.HOME)
                        onNavigate(dest)
                    },
                    onDismiss = { showNotificationsDialog = false }
                )
            }
        }
    }
}

@Composable
private fun ProgressStatBox(
    label: String,
    value: String,
    sub: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = ZenithTheme.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(
                text = label,
                fontFamily = OutfitFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = colors.textMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontFamily = OutfitFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = accent
            )
            Text(
                text = sub,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 10.sp,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun NotificationsDialog(
    notifications: List<AppNotificationItem>,
    onNavigate: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val colors = ZenithTheme.colors
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.surface,
        title = {
            Text(
                text = "TOUR NOTIFICATIONS",
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Black,
                color = colors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                notifications.forEach { item ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceElevated,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigate(item.targetScreen) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = item.title,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accentGold
                                )
                                Text(
                                    text = item.timestampLabel,
                                    fontSize = 10.sp,
                                    color = colors.textMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.message,
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("CLOSE", fontWeight = FontWeight.Bold, color = colors.accentGold)
            }
        }
    )
}

@Composable
private fun PlayerDashboardTopHeader(
    username: String,
    countryTag: String,
    playerLevel: Int,
    levelProgress: Float,
    golferOvr: Int,
    visorColor: Color,
    poloColor: Color,
    skinColor: Color,
    isLeftHanded: Boolean,
    coins: Int,
    trophies: Int,
    unreadNotifications: Int,
    isGuestSession: Boolean,
    isOnline: Boolean,
    onAvatarClick: () -> Unit,
    onCoinsClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    onOpenSettings: () -> Unit,
    onGuestSignInClick: () -> Unit
) {
    val colors = ZenithTheme.colors

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Player Avatar + Username + Level + XP Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAvatarClick() }
                        .testTag("home_player_profile_header")
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(colors.forestCard)
                                .border(2.dp, colors.accentGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            MiniGolferAvatarCanvas(
                                visorColor = visorColor,
                                poloColor = poloColor,
                                skinColor = skinColor,
                                isLeftHanded = isLeftHanded,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.accentGold,
                            border = BorderStroke(1.dp, Color.Black.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "LV $playerLevel",
                                fontFamily = OutfitFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.onGold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = username,
                                fontFamily = OutfitFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = colors.forestCard,
                                border = BorderStroke(1.dp, colors.accentSuccess.copy(alpha = 0.6f))
                            ) {
                                Text(
                                    text = "OVR $golferOvr",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ZenithNeonGreen,
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isGuestSession) "Guest • Local Save" else countryTag,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 10.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(3.dp))

                        LinearProgressIndicator(
                            progress = { levelProgress },
                            color = colors.accentSuccess,
                            trackColor = colors.surfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .widthIn(max = 110.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Right: Coins, Notifications, Settings (>=48dp touch targets)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceElevated,
                        border = BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clickable { onCoinsClick() }
                            .testTag("home_coins_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = "Coins",
                                tint = colors.accentGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "$coins",
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentGold,
                                maxLines = 1
                            )
                        }
                    }

                    // Notifications Bell
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.surfaceElevated,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onNotificationsClick() }
                            .testTag("home_notifications_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            if (unreadNotifications > 0) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = 7.dp, y = (-7).dp)
                                        .size(9.dp)
                                        .background(colors.accentGold, CircleShape)
                                )
                            }
                        }
                    }

                    // Settings Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.surfaceElevated,
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier
                            .size(48.dp)
                            .clickable { onOpenSettings() }
                            .testTag("settings_top_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (isGuestSession || !isOnline) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (isOnline) colors.accentSuccess else colors.accentGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isGuestSession) {
                                "Guest Mode • Progress saved locally ($trophies Cups)"
                            } else {
                                "Offline Mode • Scores queued locally"
                            },
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 10.sp,
                            color = colors.textSecondary
                        )
                    }
                    if (isGuestSession) {
                        Text(
                            text = "LINK ACCOUNT →",
                            fontFamily = OutfitFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentGold,
                            modifier = Modifier
                                .clickable { onGuestSignInClick() }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                .testTag("home_link_guest_btn")
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardQuickModeTile(
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ZenithTheme.colors
    Card(
        modifier = modifier
            .clickable { onClick() }
            .testTag(testTag),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(accentColor.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
                        .border(1.dp, accentColor.copy(alpha = 0.55f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = badge,
                        fontFamily = OutfitFontFamily,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontFamily = OutfitFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 11.sp,
                color = colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun MiniGolferAvatarCanvas(
    visorColor: Color,
    poloColor: Color,
    isLeftHanded: Boolean,
    modifier: Modifier = Modifier,
    skinColor: Color = Color(0xFFFFCCBC)
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(poloColor.copy(alpha = 0.4f), Color.Transparent)
            ),
            radius = w * 0.48f,
            center = Offset(cx, h * 0.55f)
        )

        drawRoundRect(
            color = poloColor,
            topLeft = Offset(cx - w * 0.24f, h * 0.54f),
            size = Size(w * 0.48f, h * 0.40f),
            cornerRadius = CornerRadius(8f, 8f)
        )

        drawCircle(
            color = skinColor,
            radius = w * 0.17f,
            center = Offset(cx, h * 0.36f)
        )

        drawRoundRect(
            color = visorColor,
            topLeft = Offset(cx - w * 0.19f, h * 0.22f),
            size = Size(w * 0.38f, h * 0.09f),
            cornerRadius = CornerRadius(3f, 3f)
        )

        val clubStart = if (isLeftHanded) Offset(cx + w * 0.18f, h * 0.68f) else Offset(cx - w * 0.18f, h * 0.68f)
        val clubEnd = if (isLeftHanded) Offset(cx - w * 0.24f, h * 0.16f) else Offset(cx + w * 0.24f, h * 0.16f)
        drawLine(
            color = Color.White,
            start = clubStart,
            end = clubEnd,
            strokeWidth = 2.5f
        )
        drawCircle(
            color = ZenithPremiumGold,
            radius = 3.5f,
            center = clubEnd
        )
    }
}

@Composable
fun TopPlayerCurrencyBar(
    coins: Int,
    trophies: Int,
    mulligans: Int,
    onOpenSettings: () -> Unit = {}
) {
    val colors = ZenithTheme.colors
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SportsGolf,
                    contentDescription = null,
                    tint = colors.accentGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ZENITH GOLF",
                    fontFamily = OutfitFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary,
                    letterSpacing = 1.sp
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CurrencyPill(icon = Icons.Default.MonetizationOn, value = "$coins", color = colors.accentGold)
                CurrencyPill(icon = Icons.Default.EmojiEvents, value = "$trophies", color = colors.accentSuccess)
                CurrencyPill(icon = Icons.Default.Refresh, value = "$mulligans", color = colors.accentInfo)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.surfaceElevated,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onOpenSettings() }
                        .testTag("settings_top_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = colors.textPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CurrencyPill(
    icon: ImageVector,
    value: String,
    color: Color
) {
    val colors = ZenithTheme.colors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                fontFamily = OutfitFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary
            )
        }
    }
}

@Composable
fun ModeSelectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    badge: String,
    onClick: () -> Unit
) {
    val colors = ZenithTheme.colors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("mode_${badge.lowercase()}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(accentColor.copy(alpha = 0.16f), RoundedCornerShape(12.dp))
                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        fontFamily = OutfitFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = accentColor.copy(alpha = 0.18f)
                    ) {
                        Text(
                            text = badge,
                            fontFamily = OutfitFontFamily,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = accentColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    lineHeight = 15.sp
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textMuted
            )
        }
    }
}

@Composable
fun ZenithV2WhatsNewDialog(
    onDismiss: () -> Unit
) {
    val colors = ZenithTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.5.dp, colors.accentGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("whats_new_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.accentGold.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, colors.accentGold)
                ) {
                    Text(
                        text = "ZENITH GOLF 2.0 • APEX DYNAMICS",
                        fontFamily = OutfitFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accentGold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "RISE ABOVE. MASTER EVERY SWING.",
                    fontFamily = OutfitFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                WhatsNewFeatureItem(
                    icon = Icons.Default.SportsGolf,
                    iconColor = colors.accentGold,
                    title = "3D Aerodynamic & Putting Physics",
                    desc = "Full Magnus lift, spin decay, crosswind shear, tree canopy collisions, and topographical green break."
                )

                Spacer(modifier = Modifier.height(10.dp))

                WhatsNewFeatureItem(
                    icon = Icons.Default.EmojiEvents,
                    iconColor = colors.accentSuccess,
                    title = "Global Cloud Top 25 & 1v1 Match Play",
                    desc = "Compete in official Tour Cups, head-to-head rival duels, and real-time Firestore rankings."
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold, contentColor = colors.onGold),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("dismiss_whats_new_btn")
                ) {
                    Text(
                        text = "ENTER CLUBHOUSE",
                        fontFamily = OutfitFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}

@Composable
fun WhatsNewFeatureItem(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    desc: String
) {
    val colors = ZenithTheme.colors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceElevated,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.2f)),
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
                Spacer(modifier = Modifier.height(2.dp))
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
