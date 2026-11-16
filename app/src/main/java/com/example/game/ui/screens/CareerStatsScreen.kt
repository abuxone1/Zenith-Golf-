package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.GolferProfile
import com.example.game.model.GolferStance
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ZenithTheme

private val COUNTRY_REGION_OPTIONS = listOf(
    "USA • APEX TOUR",
    "GBR • ROYAL LINKS",
    "SCO • HIGHLANDS TOUR",
    "CAN • NORTHERN TOUR",
    "JPN • PACIFIC CIRCUIT",
    "AUS • SOUTHERN LINKS",
    "ESP • MEDITERRANEAN TOUR",
    "GER • EURO MASTERS",
    "KOR • SEOUL APEX"
)

@Composable
fun CareerStatsScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onNavigate: (AppScreen) -> Unit = {},
    onUpdateGolfer: (GolferProfile) -> Unit = {},
    onRequestAccountSignIn: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val colors = ZenithTheme.colors
    val career = uiState.career
    val profile = uiState.golferProfile
    val scrollState = rememberScrollState()
    var selectedSection by remember { mutableIntStateOf(0) } // 0 = Stats, 1 = Achievements, 2 = Rankings & History, 3 = Attributes
    var showRegionDialog by remember { mutableStateOf(false) }

    val playerLevel = (career.tourXp / 200) + 1
    val xpIntoLevel = career.tourXp % 200
    val golferOvr = (profile.powerAttribute + profile.accuracyAttribute + profile.spinControlAttribute + profile.puttingAttribute) / 4

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .testTag("career_stats_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Bar
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("career_back_btn")
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
                            text = "APEX TOUR LICENSE",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentGold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "PLAYER PROFILE",
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
                    border = BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .clickable { onNavigate(AppScreen.PRO_SHOP) }
                        .testTag("profile_open_equipment_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ShoppingBag, contentDescription = "Pro Shop", tint = colors.accentGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "EQUIPMENT",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.onForestCard
                        )
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            if (uiState.isGuestSession) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.forestCard,
                    border = BorderStroke(1.dp, colors.accentGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("profile_guest_upgrade_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.AccountCircle, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "GUEST TOUR SESSION",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentGold
                                )
                                Text(
                                    text = "Link Google or Email to preserve your career stats & cloud saves.",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp,
                                    color = colors.onForestSecondary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onRequestAccountSignIn,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentGold,
                                contentColor = colors.onGold
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("LINK", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }

            // Pro Golfer Identity Hero Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = BorderStroke(1.5.dp, colors.accentGold.copy(alpha = 0.65f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(colors.forestCard)
                                .border(2.dp, colors.accentGold, RoundedCornerShape(18.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            MiniGolferAvatarCanvas(
                                visorColor = Color(profile.visorColorHex),
                                poloColor = Color(profile.poloColorHex),
                                isLeftHanded = profile.stance == GolferStance.LEFT_HANDED,
                                modifier = Modifier.fillMaxSize(),
                                skinColor = Color(profile.skinToneHex)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = colors.accentGold.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, colors.accentGold)
                                ) {
                                    Text(
                                        text = "LEVEL $playerLevel • OVR $golferOvr",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colors.accentGold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = colors.accentGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = profile.golferName,
                                fontFamily = OutfitFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showRegionDialog = true }
                                    .padding(vertical = 2.dp)
                                    .testTag("profile_region_selector")
                            ) {
                                Icon(Icons.Default.Public, contentDescription = "Change country or region", tint = colors.accentInfo, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${profile.countryTag} • ${profile.stance.label}",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp,
                                    color = colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // XP Bar to Next Level
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TOUR XP PROGRESS ($xpIntoLevel / 200 XP TO LVL ${playerLevel + 1})",
                                fontFamily = OutfitFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMuted
                            )
                            Text(
                                text = "${career.tourXp} TOTAL XP",
                                fontFamily = OutfitFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentSuccess
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (xpIntoLevel / 200f).coerceIn(0f, 1f) },
                            color = colors.accentSuccess,
                            trackColor = colors.surfaceElevated,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons: Customize Golfer & Equipment Bag
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onNavigate(AppScreen.GOLFER_LOCKER_ROOM) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentGold,
                                contentColor = colors.onGold
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("profile_customize_golfer_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "CUSTOMIZE GOLFER",
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }

                        OutlinedButton(
                            onClick = { onNavigate(AppScreen.PRO_SHOP) },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, colors.accentSuccess),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = colors.forestCard,
                                contentColor = colors.onForestCard
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("profile_upgrade_bag_btn")
                        ) {
                            Icon(Icons.Default.SportsGolf, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "UPGRADE BAG",
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Trophy Cabinet Summary Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CabinetTrophyCard(
                    title = "Zenith Cups",
                    count = "${career.trophies}",
                    color = colors.accentGold,
                    modifier = Modifier.weight(1f)
                )
                CabinetTrophyCard(
                    title = "Aces (HIO)",
                    count = "${career.aces}",
                    color = colors.accentInfo,
                    modifier = Modifier.weight(1f)
                )
                CabinetTrophyCard(
                    title = "Eagles",
                    count = "${career.eagles}",
                    color = colors.accentSuccess,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Profile Sub-Navigation Tabs
            TabRow(
                selectedTabIndex = selectedSection,
                containerColor = colors.surface,
                contentColor = colors.accentGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedSection]),
                        color = colors.accentGold
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, colors.border, RoundedCornerShape(12.dp))
            ) {
                listOf("STATISTICS", "ACHIEVEMENTS", "RANK & HISTORY", "CUSTOMIZE").forEachIndexed { idx, label ->
                    Tab(
                        selected = selectedSection == idx,
                        onClick = { selectedSection = idx },
                        text = {
                            Text(
                                text = label,
                                fontFamily = OutfitFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (selectedSection == idx) colors.accentGold else colors.textMuted,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        modifier = Modifier.testTag("profile_tab_$idx")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedSection) {
                0 -> {
                    Text(
                        text = "CAREER PERFORMANCE METRICS",
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accentGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
                        MetricRow(label = "Longest Drive (Carry)", value = "${career.longestDriveYds.toInt()} YDS", highlight = true)
                        MetricRow(label = "Total Birdies Logged", value = "${career.birdies}")
                        MetricRow(label = "Total Eagles Logged", value = "${career.eagles}")
                        MetricRow(label = "Championship Rounds Completed", value = "${career.roundsPlayed}")
                        MetricRow(label = "Individual Holes Played", value = "${career.holesPlayed}")
                        MetricRow(label = "Greens in Regulation (GIR)", value = "${career.greensInRegulationCount}")
                        MetricRow(label = "1v1 Match Play Record", value = "${career.matchPlayWins}W - ${career.matchPlayLosses}L", highlight = true)
                        MetricRow(label = "Bullseye Shootout High Score", value = "${career.shootoutHighScore} PTS")
                        MetricRow(label = "Total Career Purse Balance", value = "${career.coins} Coins")
                        MetricRow(label = "Season 1 Tour XP", value = "${career.tourXp} XP")
                    }
                }

                1 -> {
                    Text(
                        text = "TOUR ACHIEVEMENTS (${uiState.achievements.count { it.isUnlocked }}/${uiState.achievements.size} UNLOCKED)",
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accentGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        uiState.achievements.forEach { ach ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = colors.surface,
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
                                            imageVector = if (ach.isUnlocked) Icons.Default.CheckCircle else Icons.Default.EmojiEvents,
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
                                            text = if (ach.isUnlocked) "UNLOCKED" else "+${ach.xpValue} XP",
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

                2 -> {
                    Text(
                        text = "WORLD TOUR RANKINGS & MATCH HISTORY",
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accentGold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    MetricRow(
                        label = "Current Tournament Standing",
                        value = "Rank #${uiState.tournamentFinalRank} (${uiState.selectedCup.title})",
                        highlight = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    MetricRow(
                        label = "1v1 Match Play Win Rate",
                        value = "${career.matchPlayWins} Wins • ${career.matchPlayLosses} Losses"
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { onNavigate(AppScreen.TOURNAMENT_LEADERBOARD) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.forestCard,
                            contentColor = colors.accentGold
                        ),
                        border = BorderStroke(1.dp, colors.accentGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Leaderboard, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "OPEN GLOBAL CLOUD TOP 25 LEADERBOARD",
                            fontFamily = OutfitFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    if (uiState.cloudMatchHistory.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "CLOUD VERIFIED MATCH HISTORY (${uiState.cloudMatchHistory.size})",
                            fontFamily = OutfitFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentInfo,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        uiState.cloudMatchHistory.take(6).forEach { match ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                border = BorderStroke(1.dp, colors.border),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = match.courseName,
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "${match.mode} • ${match.scoreBanner} • ${match.totalStrokes} Strokes",
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                    Text(
                                        text = "+${match.coinsEarned} C",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colors.accentGold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "RECENT SHOT TELEMETRY & MATCH VAULT",
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accentSuccess,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    uiState.savedReplays.forEach { replay ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            border = BorderStroke(1.dp, colors.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onNavigate(AppScreen.REPLAY_THEATER) }
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = colors.accentSuccess,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = replay.title,
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.textPrimary
                                    )
                                    Text(
                                        text = "${replay.holeName} • ${replay.clubUsed.shortName} • ${replay.ballSpeedMph.toInt()} MPH",
                                        fontFamily = PlusJakartaSansFontFamily,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                Text(
                                    text = replay.scoreAchieved,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentGold
                                )
                            }
                        }
                    }
                }

                3 -> {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        border = BorderStroke(1.dp, colors.border),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "ATHLETE SKILL RATINGS",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentGold
                                )
                                Text(
                                    text = "${profile.unspentSkillPoints} SKILL PTS AVAILABLE",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentSuccess
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            ProfileAttributeRow("Driving Power", profile.powerAttribute, colors.accentGold)
                            ProfileAttributeRow("Iron Accuracy", profile.accuracyAttribute, colors.accentSuccess)
                            ProfileAttributeRow("Aerodynamic Spin", profile.spinControlAttribute, colors.accentInfo)
                            ProfileAttributeRow("Green Putting Touch", profile.puttingAttribute, colors.accentGold)

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { onNavigate(AppScreen.GOLFER_LOCKER_ROOM) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accentGold,
                                    contentColor = colors.onGold
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Text(
                                    text = "OPEN FULL LOCKER ROOM & KIT BUILDER",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showRegionDialog) {
        AlertDialog(
            onDismissRequest = { showRegionDialog = false },
            containerColor = colors.surfaceElevated,
            title = {
                Text(
                    text = "Select Tour Country & Region",
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    COUNTRY_REGION_OPTIONS.forEach { region ->
                        val selected = profile.countryTag == region
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) colors.forestCard else colors.surface,
                            border = BorderStroke(1.dp, if (selected) colors.accentGold else colors.border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onUpdateGolfer(profile.copy(countryTag = region))
                                    showRegionDialog = false
                                }
                        ) {
                            Text(
                                text = region,
                                fontFamily = OutfitFontFamily,
                                fontSize = 13.sp,
                                fontWeight = if (selected) FontWeight.Black else FontWeight.SemiBold,
                                color = if (selected) colors.accentGold else colors.textPrimary,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showRegionDialog = false }) {
                    Text("CLOSE", color = colors.accentGold, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun ProfileAttributeRow(
    label: String,
    value: Int,
    color: Color
) {
    val colors = ZenithTheme.colors
    Column(modifier = Modifier.padding(vertical = 5.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
            Text(
                text = "$value / 99",
                fontFamily = OutfitFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            color = color,
            trackColor = colors.surfaceElevated,
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
    }
}

@Composable
fun CabinetTrophyCard(
    title: String,
    count: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colors = ZenithTheme.colors
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = count,
                fontFamily = OutfitFontFamily,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = color
            )
            Text(
                text = title,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
fun MetricRow(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    val colors = ZenithTheme.colors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, if (highlight) colors.accentGold.copy(alpha = 0.5f) else colors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 13.sp,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontFamily = OutfitFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                color = if (highlight) colors.accentGold else colors.textPrimary
            )
        }
    }
}
