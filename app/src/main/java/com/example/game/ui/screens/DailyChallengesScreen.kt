package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.engine.GolfAudioHapticEngine
import com.example.game.model.DailyChallenge
import com.example.game.model.LuckyWheelPrize
import com.example.game.model.PlayAchievement
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ZenithTheme
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

private val wheelPrizes = listOf(
    LuckyWheelPrize("p1", "100 COINS", "+100 Gold", coins = 100, mulligans = 0, colorHex = 0xFF123D2A),
    LuckyWheelPrize("p2", "+1 MULLIGAN", "Retry Power", coins = 0, mulligans = 1, colorHex = 0xFF00B8D4),
    LuckyWheelPrize("p3", "250 COINS", "+250 Gold", coins = 250, mulligans = 0, colorHex = 0xFFD6B45C),
    LuckyWheelPrize("p4", "50 COINS", "Pocket Cash", coins = 50, mulligans = 0, colorHex = 0xFF10B981),
    LuckyWheelPrize("p5", "+2 MULLIGANS", "Double Retry", coins = 0, mulligans = 2, colorHex = 0xFF7C4DFF),
    LuckyWheelPrize("p6", "150 COINS", "+150 Gold", coins = 150, mulligans = 0, colorHex = 0xFF0091EA),
    LuckyWheelPrize("p7", "500 JACKPOT", "★ MEGA PRIZE ★", coins = 500, mulligans = 2, colorHex = 0xFFFFAB00, isJackpot = true),
    LuckyWheelPrize("p8", "300 COINS", "+300 Gold", coins = 300, mulligans = 1, colorHex = 0xFF2E7D32)
)

@Composable
fun DailyChallengesScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onClaimChallenge: (String) -> Unit,
    onSpinWheelResult: (LuckyWheelPrize) -> Unit,
    onPlayChallenge: (DailyChallenge) -> Unit,
    onNavigate: (AppScreen) -> Unit = {},
    onClaimDailyLoginReward: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val colors = ZenithTheme.colors
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var winningPrizeDialog by remember { mutableStateOf<LuckyWheelPrize?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val wheelAngle = remember { Animatable(0f) }
    var isSpinning by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .testTag("daily_challenges_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Navigation Header
        DailyTopBar(
            coins = uiState.career.coins,
            mulligans = uiState.career.mulligansRemaining,
            trophies = uiState.career.trophies,
            onBack = onBack
        )

        // 4-Tab Selector
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = colors.surface,
            contentColor = colors.accentGold,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = colors.accentGold,
                    height = 3.dp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, colors.border)
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        text = "DAILY REWARDS",
                        fontFamily = OutfitFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (selectedTabIndex == 0) colors.accentGold else colors.textMuted
                    )
                },
                modifier = Modifier.testTag("tab_lucky_wheel")
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Text(
                        text = "MISSIONS (${uiState.dailyChallenges.size})",
                        fontFamily = OutfitFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (selectedTabIndex == 1) colors.accentGold else colors.textMuted
                    )
                },
                modifier = Modifier.testTag("tab_daily_missions")
            )
            Tab(
                selected = selectedTabIndex == 2,
                onClick = { selectedTabIndex = 2 },
                text = {
                    Text(
                        text = "ACHIEVEMENTS",
                        fontFamily = OutfitFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (selectedTabIndex == 2) colors.accentGold else colors.textMuted
                    )
                },
                modifier = Modifier.testTag("tab_rewards_achievements")
            )
            Tab(
                selected = selectedTabIndex == 3,
                onClick = { selectedTabIndex = 3 },
                text = {
                    Text(
                        text = "CURRENCY",
                        fontFamily = OutfitFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (selectedTabIndex == 3) colors.accentGold else colors.textMuted
                    )
                },
                modifier = Modifier.testTag("tab_rewards_currency")
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            when (selectedTabIndex) {
                0 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        DailyLoginStreakCard(
                            streakDay = uiState.dailyLoginStreak,
                            isClaimedToday = uiState.isDailyRewardClaimedToday,
                            onClaim = onClaimDailyLoginReward
                        )

                        LuckyWheelSection(
                            wheelAngle = wheelAngle.value,
                            isSpinning = isSpinning,
                            onOpenTourPass = { onNavigate(AppScreen.TOUR_PASS) },
                            onSpinClick = {
                                if (!isSpinning) {
                                    isSpinning = true
                                    coroutineScope.launch {
                                        val targetPrizeIndex = wheelPrizes.indices.random()
                                        val sectorAngle = 360f / wheelPrizes.size
                                        val extraRotations = (4..6).random() * 360f
                                        val targetSectorCenter = targetPrizeIndex * sectorAngle + (sectorAngle / 2f)
                                        val finalAngle = wheelAngle.value + extraRotations + (270f - (wheelAngle.value % 360f) - targetSectorCenter + 360f) % 360f

                                        var lastTickDeg = wheelAngle.value
                                        wheelAngle.animateTo(
                                            targetValue = finalAngle,
                                            animationSpec = tween(durationMillis = 3800, easing = FastOutSlowInEasing)
                                        ) {
                                            if (value - lastTickDeg >= sectorAngle) {
                                                GolfAudioHapticEngine.playWheelTickSound()
                                                lastTickDeg = value
                                            }
                                        }

                                        val winningPrize = wheelPrizes[targetPrizeIndex]
                                        onSpinWheelResult(winningPrize)
                                        winningPrizeDialog = winningPrize
                                        isSpinning = false
                                    }
                                }
                            }
                        )
                    }
                }

                1 -> {
                    DailyMissionsSection(
                        challenges = uiState.dailyChallenges,
                        careerRounds = uiState.career.roundsPlayed,
                        careerBirdies = uiState.career.birdies,
                        onClaimChallenge = onClaimChallenge,
                        onPlayChallenge = onPlayChallenge
                    )
                }

                2 -> {
                    RewardsAchievementsSection(
                        achievements = uiState.achievements
                    )
                }

                3 -> {
                    VirtualCurrencyVaultSection(
                        uiState = uiState,
                        onOpenTourPass = { onNavigate(AppScreen.TOUR_PASS) },
                        onOpenProShop = { onNavigate(AppScreen.PRO_SHOP) }
                    )
                }
            }
        }
    }

    winningPrizeDialog?.let { prize ->
        PrizeWonDialog(
            prize = prize,
            onDismiss = { winningPrizeDialog = null }
        )
    }
}

@Composable
private fun DailyLoginStreakCard(
    streakDay: Int,
    isClaimedToday: Boolean,
    onClaim: () -> Unit
) {
    val colors = ZenithTheme.colors
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.forestCard),
        border = BorderStroke(1.5.dp, colors.accentGold),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("rewards_daily_login_streak_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CardGiftcard, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "7-DAY LOGIN STREAK REWARDS",
                            fontFamily = OutfitFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentGold
                        )
                        Text(
                            text = "Current Streak: Day $streakDay of 7",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            color = colors.onForestSecondary
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isClaimedToday) Color(0x33FFFFFF) else colors.accentGold.copy(alpha = 0.25f)
                ) {
                    Text(
                        text = if (isClaimedToday) "CLAIMED" else "AVAILABLE",
                        fontFamily = OutfitFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isClaimedToday) Color(0xFFB0BEC5) else colors.accentGold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                for (day in 1..7) {
                    val isClaimed = day < streakDay || (day == streakDay && isClaimedToday)
                    val isNext = day == streakDay && !isClaimedToday
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            isClaimed -> colors.accentGold.copy(alpha = 0.22f)
                            isNext -> colors.accentGold
                            else -> Color(0x26FFFFFF)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isClaimed || isNext) colors.accentGold else Color(0x44FFFFFF)
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "D$day",
                                fontFamily = OutfitFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isNext) colors.onGold else colors.onForestCard
                            )
                            Text(
                                text = "${100 + day * 50}",
                                fontFamily = OutfitFontFamily,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isNext) colors.onGold else colors.accentGold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onClaim,
                enabled = !isClaimedToday,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentGold,
                    contentColor = colors.onGold,
                    disabledContainerColor = Color(0x33FFFFFF),
                    disabledContentColor = Color(0xFFB0BEC5)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("rewards_claim_daily_streak_btn")
            ) {
                Text(
                    text = if (isClaimedToday) "TODAY'S STREAK BONUS CLAIMED" else "CLAIM DAY $streakDay LOGIN BONUS",
                    fontFamily = OutfitFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun RewardsAchievementsSection(
    achievements: List<PlayAchievement>
) {
    val colors = ZenithTheme.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = colors.forestCard,
            border = BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CAREER ACHIEVEMENTS & MILESTONES",
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accentGold
                    )
                    Text(
                        text = "Unlock milestones during rounds to earn Season Tour Pass XP.",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        color = colors.onForestSecondary
                    )
                }
                Text(
                    text = "${achievements.count { it.isUnlocked }}/${achievements.size}",
                    fontFamily = OutfitFontFamily,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.accentGold
                )
            }
        }

        achievements.forEach { ach ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(
                    1.dp,
                    if (ach.isUnlocked) colors.accentGold else colors.border
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(
                                if (ach.isUnlocked) colors.accentGold.copy(alpha = 0.2f) else colors.surfaceElevated,
                                CircleShape
                            )
                            .border(
                                1.dp,
                                if (ach.isUnlocked) colors.accentGold else colors.border,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (ach.isUnlocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            tint = if (ach.isUnlocked) colors.accentGold else colors.textMuted,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = ach.title,
                            fontFamily = OutfitFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = ach.description,
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (ach.isUnlocked) colors.accentGold.copy(alpha = 0.2f) else colors.surfaceElevated
                    ) {
                        Text(
                            text = if (ach.isUnlocked) "CLAIMED • +${ach.xpValue} XP" else "LOCKED • +${ach.xpValue} XP",
                            fontFamily = OutfitFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = if (ach.isUnlocked) colors.accentGold else colors.textMuted,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VirtualCurrencyVaultSection(
    uiState: GameUiState,
    onOpenTourPass: () -> Unit,
    onOpenProShop: () -> Unit
) {
    val colors = ZenithTheme.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "VIRTUAL CURRENCY & TOUR BALANCES",
            fontFamily = OutfitFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = colors.accentGold,
            letterSpacing = 1.sp
        )

        CurrencyBalanceCard(
            title = "Championship Gold Coins",
            balance = "${uiState.career.coins} COINS",
            description = "Earned from tournament purses, birdies, daily missions, and the Lucky Wheel. Used for club upgrades, balls, outfits, and tournament entries.",
            accentColor = colors.accentGold,
            ctaLabel = "SPEND IN PRO SHOP",
            onCtaClick = onOpenProShop
        )

        CurrencyBalanceCard(
            title = "Tour Mulligan Tokens",
            balance = "${uiState.career.mulligansRemaining} MULLIGANS",
            description = "Allows an instant penalty-free shot retry during any tournament or practice round.",
            accentColor = colors.accentInfo,
            ctaLabel = "EARN VIA SEASON PASS",
            onCtaClick = onOpenTourPass
        )

        CurrencyBalanceCard(
            title = "Championship Trophies",
            balance = "${uiState.career.trophies} TROPHIES",
            description = "Awarded for winning official Tour Cups. Determines your Global Cloud Top 25 leaderboard rank.",
            accentColor = colors.accentSuccess,
            ctaLabel = "VIEW SEASON 1 PASS",
            onCtaClick = onOpenTourPass
        )

        CurrencyBalanceCard(
            title = "Season 1 Tour XP",
            balance = "${uiState.tourPassState.currentXp} XP",
            description = "Unlocks 10 tiers of Free and VIP Gold rewards in the Season 1 Zenith Tour Pass.",
            accentColor = colors.accentGold,
            ctaLabel = "CLAIM PASS TIERS",
            onCtaClick = onOpenTourPass
        )
    }
}

@Composable
private fun CurrencyBalanceCard(
    title: String,
    balance: String,
    description: String,
    accentColor: Color,
    ctaLabel: String,
    onCtaClick: () -> Unit
) {
    val colors = ZenithTheme.colors
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontFamily = OutfitFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = balance,
                    fontFamily = OutfitFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = description,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 11.sp,
                color = colors.textSecondary,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onCtaClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.forestCard,
                    contentColor = colors.onForestCard
                ),
                border = BorderStroke(1.dp, accentColor),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = ctaLabel,
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
fun DailyTopBar(
    coins: Int,
    mulligans: Int,
    trophies: Int = 1,
    onBack: () -> Unit
) {
    val colors = ZenithTheme.colors
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
                        .testTag("daily_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "ZENITH TOUR REWARDS",
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accentGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "REWARDS & MISSIONS",
                        fontFamily = OutfitFontFamily,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                CurrencyPill(icon = Icons.Default.MonetizationOn, value = "$coins", color = colors.accentGold)
                CurrencyPill(icon = Icons.Default.LockReset, value = "$mulligans", color = colors.accentInfo)
                CurrencyPill(icon = Icons.Default.EmojiEvents, value = "$trophies", color = colors.accentSuccess)
            }
        }
    }
}

@Composable
fun LuckyWheelSection(
    wheelAngle: Float,
    isSpinning: Boolean,
    onOpenTourPass: () -> Unit = {},
    onSpinClick: () -> Unit
) {
    val colors = ZenithTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(1.5.dp, colors.accentGold.copy(alpha = 0.65f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colors.accentGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ZENITH TOUR DAILY ROULETTE",
                        fontFamily = OutfitFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accentGold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Spin daily for free coins, extra mulligans, and rare tour jackpot drops!",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Box(
            modifier = Modifier
                .size(288.dp)
                .testTag("lucky_wheel_canvas_box"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val radius = size.width / 2f - 14f

                rotate(wheelAngle, center) {
                    val sweepAngle = 360f / wheelPrizes.size
                    for (i in wheelPrizes.indices) {
                        val prize = wheelPrizes[i]
                        val startAngle = i * sweepAngle
                        val sectorColor = Color(prize.colorHex)

                        drawArc(
                            color = sectorColor.copy(alpha = if (i % 2 == 0) 0.90f else 0.78f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2f, radius * 2f)
                        )

                        val rad = Math.toRadians(startAngle.toDouble())
                        val endX = center.x + radius * cos(rad).toFloat()
                        val endY = center.y + radius * sin(rad).toFloat()
                        drawLine(
                            color = Color(0xFF0A140E),
                            start = center,
                            end = Offset(endX, endY),
                            strokeWidth = 3f
                        )

                        val pegRad = Math.toRadians((startAngle + sweepAngle / 2f).toDouble())
                        val pegX = center.x + (radius - 8f) * cos(pegRad).toFloat()
                        val pegY = center.y + (radius - 8f) * sin(pegRad).toFloat()
                        drawCircle(
                            color = if (prize.isJackpot) Color.Yellow else Color.White,
                            radius = 4f,
                            center = Offset(pegX, pegY)
                        )
                    }

                    drawCircle(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color(0xFFD6B45C),
                                Color(0xFF00E676),
                                Color(0xFF00E5FF),
                                Color(0xFFD6B45C)
                            ),
                            center
                        ),
                        radius = radius,
                        center = center,
                        style = Stroke(width = 8f)
                    )
                }

                drawCircle(
                    color = Color(0xFF09271C),
                    radius = 36f,
                    center = center
                )
                drawCircle(
                    color = Color(0xFFD6B45C),
                    radius = 36f,
                    center = center,
                    style = Stroke(width = 4f)
                )

                val needlePath = Path().apply {
                    moveTo(center.x, center.y - radius - 8f)
                    lineTo(center.x - 14f, center.y - radius + 18f)
                    lineTo(center.x + 14f, center.y - radius + 18f)
                    close()
                }
                drawPath(path = needlePath, color = Color(0xFFFF1744))
                drawPath(path = needlePath, color = Color.White, style = Stroke(width = 2.5f))
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsGolf,
                    contentDescription = null,
                    tint = Color(0xFFD6B45C),
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "ZENITH",
                    fontFamily = OutfitFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Button(
            onClick = onSpinClick,
            enabled = !isSpinning,
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accentGold,
                contentColor = colors.onGold,
                disabledContainerColor = colors.surfaceElevated,
                disabledContentColor = colors.textMuted
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .testTag("spin_wheel_button")
        ) {
            Icon(
                imageVector = if (isSpinning) Icons.Default.Refresh else Icons.Default.Casino,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (isSpinning) "SPINNING ROULETTE..." else "SPIN FREE DAILY WHEEL",
                fontFamily = OutfitFontFamily,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Season 1 Tour Pass Shortcut Card
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = colors.forestCard,
            border = BorderStroke(1.dp, colors.accentGold),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onOpenTourPass() }
                .testTag("rewards_open_tour_pass_btn")
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "SEASON 1 ZENITH TOUR PASS",
                            fontFamily = OutfitFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.onForestCard
                        )
                        Text(
                            text = "Claim 10 tiers of Free & VIP Gold rewards",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            color = colors.onForestSecondary
                        )
                    }
                }
                Text(
                    text = "OPEN →",
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.accentGold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "ROULETTE PRIZE POOL",
            fontFamily = OutfitFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = colors.accentGold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            wheelPrizes.chunked(2).forEach { rowPrizes ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowPrizes.forEach { prize ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.surface,
                            border = BorderStroke(
                                1.dp,
                                if (prize.isJackpot) colors.accentGold else Color(prize.colorHex).copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(prize.colorHex), CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = prize.label,
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (prize.isJackpot) colors.accentGold else colors.textPrimary
                                    )
                                    Text(
                                        text = prize.subLabel,
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 9.sp,
                                        color = colors.textSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DailyMissionsSection(
    challenges: List<DailyChallenge>,
    careerRounds: Int = 0,
    careerBirdies: Int = 0,
    onClaimChallenge: (String) -> Unit,
    onPlayChallenge: (DailyChallenge) -> Unit
) {
    val colors = ZenithTheme.colors
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(14.dp),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(colors.accentGold.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = colors.accentGold,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "DAILY TOUR OBJECTIVES",
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Complete real in-game milestones to earn coins and mulligans.",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }
        }

        challenges.forEach { challenge ->
            DailyMissionCard(
                challenge = challenge,
                onClaim = { onClaimChallenge(challenge.id) },
                onPlayNow = { onPlayChallenge(challenge) }
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "WEEKLY TOUR CHALLENGES",
            fontFamily = OutfitFontFamily,
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = colors.accentGold,
            letterSpacing = 1.sp
        )

        WeeklyObjectiveCard(
            title = "Weekly Championship Grinder",
            subtitle = "Complete 5 Championship Rounds ($careerRounds / 5)",
            progress = (careerRounds / 5f).coerceIn(0f, 1f),
            rewardText = "+1,500 Coins • +500 XP"
        )
        WeeklyObjectiveCard(
            title = "Weekly Birdie Hunter",
            subtitle = "Sink 10 Birdies across Tour courses ($careerBirdies / 10)",
            progress = (careerBirdies / 10f).coerceIn(0f, 1f),
            rewardText = "+900 Coins • +2 Mulligans"
        )
    }
}

@Composable
private fun WeeklyObjectiveCard(
    title: String,
    subtitle: String,
    progress: Float,
    rewardText: String
) {
    val colors = ZenithTheme.colors
    val completed = progress >= 1f
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, if (completed) colors.accentSuccess else colors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(title, fontFamily = OutfitFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Black, color = colors.textPrimary)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (completed) colors.accentSuccess.copy(alpha = 0.18f) else colors.surfaceElevated
                ) {
                    Text(
                        text = if (completed) "AVAILABLE" else "IN PROGRESS",
                        fontFamily = OutfitFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = if (completed) colors.accentSuccess else colors.textMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(subtitle, fontFamily = PlusJakartaSansFontFamily, fontSize = 11.sp, color = colors.textSecondary)
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { progress },
                color = colors.accentGold,
                trackColor = colors.surfaceElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(rewardText, fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.accentGold)
        }
    }
}

@Composable
fun DailyMissionCard(
    challenge: DailyChallenge,
    onClaim: () -> Unit,
    onPlayNow: () -> Unit
) {
    val colors = ZenithTheme.colors
    val isReadyToClaim = challenge.isCompleted && !challenge.isClaimed
    val isClaimed = challenge.isClaimed

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(
            1.dp,
            when {
                isReadyToClaim -> colors.accentGold
                isClaimed -> colors.border
                else -> colors.primaryGreen.copy(alpha = 0.6f)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mission_card_${challenge.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = challenge.title,
                    fontFamily = OutfitFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.accentGold.copy(alpha = 0.18f),
                        border = BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = colors.accentGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "+${challenge.rewardCoins}",
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentGold
                            )
                        }
                    }

                    if (challenge.rewardMulligans > 0) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.accentInfo.copy(alpha = 0.18f),
                            border = BorderStroke(1.dp, colors.accentInfo.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LockReset,
                                    contentDescription = null,
                                    tint = colors.accentInfo,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "+${challenge.rewardMulligans}",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentInfo
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = challenge.description,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 12.sp,
                color = colors.textSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))

            val progressFraction = if (challenge.isCompleted) 1.0f else (challenge.currentProgress.toFloat() / challenge.targetGoal.coerceAtLeast(1))
            LinearProgressIndicator(
                progress = { progressFraction.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = if (isReadyToClaim || isClaimed) colors.accentSuccess else colors.accentGold,
                trackColor = colors.surfaceElevated
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = when {
                        isClaimed -> "STATUS: CLAIMED ✓"
                        isReadyToClaim -> "STATUS: AVAILABLE!"
                        else -> "LOCKED (${if (challenge.isCompleted) 1 else challenge.currentProgress}/${challenge.targetGoal})"
                    },
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isClaimed -> colors.textMuted
                        isReadyToClaim -> colors.accentSuccess
                        else -> colors.textSecondary
                    }
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (!isClaimed) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.forestCard,
                            border = BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.7f)),
                            modifier = Modifier
                                .height(48.dp)
                                .clickable { onPlayNow() }
                                .testTag("play_mission_${challenge.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = colors.accentGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "PLAY NOW",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.onForestCard
                                )
                            }
                        }
                    }

                    Button(
                        onClick = onClaim,
                        enabled = isReadyToClaim,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.accentGold,
                            contentColor = colors.onGold,
                            disabledContainerColor = colors.surfaceElevated,
                            disabledContentColor = colors.textMuted
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("claim_btn_${challenge.id}")
                    ) {
                        Icon(
                            imageVector = if (isClaimed) Icons.Default.CheckCircle else Icons.Default.Star,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isClaimed) "CLAIMED" else "CLAIM",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrizeWonDialog(
    prize: LuckyWheelPrize,
    onDismiss: () -> Unit
) {
    val colors = ZenithTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = BorderStroke(2.dp, colors.accentGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("prize_won_dialog")
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(70.dp)
                        .background(colors.accentGold.copy(alpha = 0.2f), CircleShape)
                        .border(2.dp, colors.accentGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (prize.isJackpot) Icons.Default.EmojiEvents else Icons.Default.Casino,
                        contentDescription = null,
                        tint = colors.accentGold,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = if (prize.isJackpot) "★ MEGA JACKPOT WIN! ★" else "ROULETTE REWARD!",
                    fontFamily = OutfitFontFamily,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.accentGold,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = prize.label,
                    fontFamily = OutfitFontFamily,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary
                )

                Text(
                    text = prize.subLabel,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surfaceElevated,
                    border = BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (prize.coins > 0) {
                            Text(
                                text = "+${prize.coins} Coins",
                                fontFamily = OutfitFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentGold
                            )
                        }
                        if (prize.coins > 0 && prize.mulligans > 0) {
                            Text(
                                text = "  •  ",
                                fontSize = 14.sp,
                                color = colors.textMuted
                            )
                        }
                        if (prize.mulligans > 0) {
                            Text(
                                text = "+${prize.mulligans} Mulligans",
                                fontFamily = OutfitFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentInfo
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accentGold,
                        contentColor = colors.onGold
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("collect_prize_btn")
                ) {
                    Text(
                        text = "COLLECT REWARD",
                        fontFamily = OutfitFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                }
            }
        }
    }
}
