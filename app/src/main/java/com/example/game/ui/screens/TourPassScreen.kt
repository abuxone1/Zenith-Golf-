package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.TourPassTier
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ZenithTheme

@Composable
fun TourPassScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onClaimReward: (tierLevel: Int, isVip: Boolean) -> Unit,
    onUnlockVip: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val colors = ZenithTheme.colors
    val pass = uiState.tourPassState
    val currentXp = pass.currentXp
    val maxTierXp = pass.tiers.lastOrNull()?.requiredXp ?: 3300
    val progressFraction = (currentXp.toFloat() / maxTierXp.toFloat()).coerceIn(0f, 1f)
    var showVipConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("tour_pass_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TourPassTopBar(
            onBack = onBack,
            coins = uiState.career.coins
        )

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(6.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.forestCard),
                    border = BorderStroke(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(colors.accentGold, colors.accentSuccess))
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.accentGold.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, colors.accentGold)
                            ) {
                                Text(
                                    text = "SEASON 1 • ${pass.daysRemaining} DAYS REMAINING",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    letterSpacing = 1.sp
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Stars,
                                    contentDescription = null,
                                    tint = colors.accentGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "$currentXp XP",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentGold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = pass.seasonTitle.uppercase(),
                            fontFamily = OutfitFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.onForestCard,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Earn Tour XP across championships & shootouts to unlock exclusive rewards!",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            color = colors.onForestSecondary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LinearProgressIndicator(
                            progress = { progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = colors.accentGold,
                            trackColor = Color(0x33FFFFFF)
                        )
                    }
                }
            }

            // VIP Pass Activation Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(
                        1.5.dp,
                        if (pass.isVipUnlocked) colors.accentSuccess else colors.accentGold
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(if (pass.isVipUnlocked) colors.accentSuccess.copy(alpha = 0.2f) else colors.accentGold.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WorkspacePremium,
                                    contentDescription = null,
                                    tint = if (pass.isVipUnlocked) colors.accentSuccess else colors.accentGold,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (pass.isVipUnlocked) "VIP TOUR PASS ACTIVE" else "UPGRADE TO VIP PASS",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (pass.isVipUnlocked) colors.accentSuccess else colors.accentGold
                                )
                                Text(
                                    text = if (pass.isVipUnlocked) "All 10 premium tier rewards unlocked!" else "Unlock double gold, mulligans, & custom balls",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                            }
                        }

                        if (!pass.isVipUnlocked) {
                            Button(
                                onClick = { showVipConfirm = true },
                                enabled = uiState.career.coins >= 1200,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accentGold,
                                    contentColor = colors.onGold
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .height(48.dp)
                                    .testTag("unlock_vip_pass_btn")
                            ) {
                                Text(text = "1,200", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.accentSuccess.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = colors.accentSuccess, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(text = "VIP ON", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Black, color = colors.accentSuccess)
                                }
                            }
                        }
                    }
                }
            }

            items(pass.tiers) { tier ->
                TourPassTierCard(
                    tier = tier,
                    currentXp = currentXp,
                    isVipUnlocked = pass.isVipUnlocked,
                    onClaimFree = { onClaimReward(tier.tierLevel, false) },
                    onClaimVip = { onClaimReward(tier.tierLevel, true) }
                )
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
                    border = BorderStroke(1.dp, colors.border)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = colors.accentInfo, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "HOW TO EARN TOUR XP", fontFamily = OutfitFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.accentInfo)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        XpMethodRow(label = "Tee off & finish any tournament hole", xp = "+100 XP")
                        XpMethodRow(label = "Sink a Birdie (-1) or Eagle (-2)", xp = "+200 to +350 XP")
                        XpMethodRow(label = "Drain a Bullseye in Target Shootout", xp = "+150 to +300 XP")
                        XpMethodRow(label = "Complete Daily Tour Missions", xp = "+200 XP")
                    }
                }
            }
        }
    }

    if (showVipConfirm) {
        AlertDialog(
            onDismissRequest = { showVipConfirm = false },
            containerColor = colors.surfaceElevated,
            title = { Text("Activate VIP Tour Pass?", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Black, color = colors.textPrimary) },
            text = { Text("Spend 1,200 Tour Coins to unlock all 10 VIP reward tiers for Season 1?", fontFamily = PlusJakartaSansFontFamily, color = colors.textSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        showVipConfirm = false
                        onUnlockVip()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold, contentColor = colors.onGold)
                ) {
                    Text("UNLOCK VIP", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showVipConfirm = false }) {
                    Text("CANCEL", color = colors.textSecondary)
                }
            }
        )
    }
}

@Composable
fun TourPassTopBar(
    onBack: () -> Unit,
    coins: Int,
    modifier: Modifier = Modifier
) {
    val colors = ZenithTheme.colors
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f, fill = false)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("tour_pass_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column(modifier = Modifier.padding(end = 8.dp)) {
                    Text(
                        text = "ZENITH TOUR PASS",
                        fontFamily = OutfitFontFamily,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Season 1 Tier Progression",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.surfaceElevated,
                border = BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MonetizationOn,
                        contentDescription = null,
                        tint = colors.accentGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$coins",
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accentGold
                    )
                }
            }
        }
    }
}

@Composable
fun TourPassTierCard(
    tier: TourPassTier,
    currentXp: Int,
    isVipUnlocked: Boolean,
    onClaimFree: () -> Unit,
    onClaimVip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = ZenithTheme.colors
    val isTierUnlocked = currentXp >= tier.requiredXp

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isTierUnlocked) colors.surface else colors.surfaceElevated
        ),
        border = BorderStroke(
            1.dp,
            if (isTierUnlocked) colors.accentSuccess.copy(alpha = 0.5f) else colors.border
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(if (isTierUnlocked) colors.accentSuccess else colors.border),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${tier.tierLevel}",
                            fontFamily = OutfitFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isTierUnlocked) Color.White else colors.textMuted
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TIER ${tier.tierLevel}",
                        fontFamily = OutfitFontFamily,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isTierUnlocked) colors.textPrimary else colors.textSecondary
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = colors.surfaceElevated
                ) {
                    Text(
                        text = "${tier.requiredXp} XP",
                        fontFamily = OutfitFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isTierUnlocked) colors.accentInfo else colors.textMuted,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            RewardItemRow(
                trackLabel = "FREE",
                rewardTitle = tier.freeRewardTitle,
                isUnlocked = isTierUnlocked,
                isClaimed = tier.isFreeClaimed,
                accentColor = colors.accentInfo,
                onClaim = onClaimFree
            )

            Spacer(modifier = Modifier.height(8.dp))

            RewardItemRow(
                trackLabel = "VIP",
                rewardTitle = tier.vipRewardTitle,
                isUnlocked = isTierUnlocked && isVipUnlocked,
                isClaimed = tier.isVipClaimed,
                accentColor = colors.accentGold,
                isLockedVip = !isVipUnlocked,
                onClaim = onClaimVip
            )
        }
    }
}

@Composable
fun RewardItemRow(
    trackLabel: String,
    rewardTitle: String,
    isUnlocked: Boolean,
    isClaimed: Boolean,
    accentColor: Color,
    isLockedVip: Boolean = false,
    onClaim: () -> Unit
) {
    val colors = ZenithTheme.colors
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = colors.surfaceElevated,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = accentColor.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, accentColor)
                ) {
                    Text(
                        text = trackLabel,
                        fontFamily = OutfitFontFamily,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = rewardTitle,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isUnlocked) colors.textPrimary else colors.textSecondary
                )
            }

            when {
                isClaimed -> {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = colors.accentSuccess.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = colors.accentSuccess, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(text = "CLAIMED", fontFamily = OutfitFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.accentSuccess)
                        }
                    }
                }
                isLockedVip -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(text = "VIP LOCKED", fontFamily = OutfitFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.accentGold)
                    }
                }
                isUnlocked -> {
                    Button(
                        onClick = onClaim,
                        colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.White),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Text(text = "CLAIM", fontFamily = OutfitFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
                else -> {
                    Text(text = "LOCKED", fontFamily = OutfitFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.textMuted)
                }
            }
        }
    }
}

@Composable
fun XpMethodRow(label: String, xp: String) {
    val colors = ZenithTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "• $label", fontFamily = PlusJakartaSansFontFamily, fontSize = 11.sp, color = colors.textSecondary)
        Text(text = xp, fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.accentInfo)
    }
}
