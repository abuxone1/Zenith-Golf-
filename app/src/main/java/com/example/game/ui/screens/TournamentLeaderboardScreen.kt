package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ZenithTheme

data class LeaderboardEntry(
    val name: String,
    val title: String,
    val isPlayer: Boolean,
    val totalScore: Int,
    val scores: List<Int>,
    val color: Color
)

@Composable
fun TournamentLeaderboardScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onRestartTournament: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val colors = ZenithTheme.colors
    // 0 = Tournament Round, 1 = Global Cloud Top 25, 2 = Weekly Tour, 3 = Friends
    var selectedTab by remember { mutableIntStateOf(0) }

    val playerTotal = uiState.playerScores.sum()
    val playerEntry = LeaderboardEntry(
        name = "${uiState.golferProfile.golferName.uppercase()} (YOU)",
        title = uiState.golferProfile.countryTag,
        isPlayer = true,
        totalScore = if (playerTotal == 0) 20 else playerTotal,
        scores = uiState.playerScores,
        color = colors.accentSuccess
    )

    val rivalEntries = uiState.rivals.map {
        LeaderboardEntry(
            name = it.name,
            title = it.title,
            isPlayer = false,
            totalScore = it.currentTotalScore,
            scores = it.scores,
            color = it.avatarColor
        )
    }

    val allEntries = (listOf(playerEntry) + rivalEntries).sortedBy { it.totalScore }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .padding(16.dp)
            .testTag("leaderboard_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                            .testTag("leaderboard_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.padding(end = 8.dp)) {
                        Text(
                            text = "ZENITH TOUR OPEN",
                            fontFamily = OutfitFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentGold,
                            letterSpacing = 1.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Live Leaderboard",
                            fontFamily = OutfitFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.surfaceElevated,
                    border = BorderStroke(1.dp, colors.accentInfo.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Cloud Synced",
                            tint = colors.accentInfo,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "AI: ${uiState.playSettings.aiDifficulty.label.uppercase()}",
                            fontFamily = OutfitFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentInfo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4-Tab Filter Row
            val tabs = listOf(
                Triple(0, "TOURNAMENT ROUND", "tab_tournament_standings"),
                Triple(1, "GLOBAL CLOUD", "tab_global_cloud_leaderboard"),
                Triple(2, "WEEKLY", "tab_weekly_leaderboard"),
                Triple(3, "FRIENDS", "tab_friends_leaderboard")
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tabs.forEach { (idx, label, tag) ->
                    val selected = selectedTab == idx
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) colors.forestCard else colors.surface,
                        border = BorderStroke(
                            1.5.dp,
                            if (selected) colors.accentGold else colors.border
                        ),
                        modifier = Modifier
                            .height(48.dp)
                            .clickable { selectedTab = idx }
                            .testTag(tag)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        ) {
                            Text(
                                text = label,
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (selected) colors.accentGold else colors.textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tournament Status Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (selectedTab) {
                            1 -> "FIRESTORE GLOBAL TOUR RANKINGS"
                            2 -> "WEEKLY QUALIFIER STANDINGS"
                            3 -> "LINKED FRIENDS & CLUBHOUSE"
                            else -> if (uiState.tournamentCompleted) {
                                "TOURNAMENT FINALIZED"
                            } else {
                                "CURRENT ROUND: HOLE ${uiState.currentHoleIndex + 1} OF ${uiState.holes.size}"
                            }
                        },
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.accentGold
                    )
                    Text(
                        text = if (selectedTab == 0) "${uiState.holes.size} HOLE EVENT" else "${uiState.career.trophies} TROPHIES",
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (selectedTab) {
                0 -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(allEntries) { index, entry ->
                            val rank = index + 1
                            val rankBadgeColor = when (rank) {
                                1 -> colors.accentGold
                                2 -> Color(0xFFB0BEC5)
                                3 -> Color(0xFFCD7F32)
                                else -> colors.textMuted
                            }

                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (entry.isPlayer) colors.forestCard else colors.surface
                                ),
                                border = if (entry.isPlayer) {
                                    BorderStroke(1.5.dp, colors.accentGold)
                                } else {
                                    BorderStroke(1.dp, colors.border)
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .background(rankBadgeColor.copy(alpha = 0.2f), CircleShape)
                                            .border(1.dp, rankBadgeColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "#$rank",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = rankBadgeColor
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = entry.name,
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = if (entry.isPlayer) colors.onForestCard else colors.textPrimary
                                            )
                                            if (entry.isPlayer) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = colors.accentGold.copy(alpha = 0.25f)
                                                ) {
                                                    Text(
                                                        text = "YOU",
                                                        fontFamily = OutfitFontFamily,
                                                        fontSize = 8.sp,
                                                        fontWeight = FontWeight.Black,
                                                        color = colors.accentGold,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Text(
                                            text = entry.title,
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 11.sp,
                                            color = if (entry.isPlayer) colors.onForestSecondary else colors.textSecondary
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${entry.totalScore}",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (entry.isPlayer) colors.accentGold else colors.textPrimary
                                        )
                                        Text(
                                            text = "STROKES",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (entry.isPlayer) colors.onForestSecondary else colors.textMuted
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    val cloudList = uiState.globalCloudLeaderboard
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (cloudList.isEmpty()) {
                            item {
                                PlayerVerifiedStandingCard(uiState = uiState)
                            }
                            item {
                                HonestCloudSyncNoteCard(
                                    title = if (uiState.isOnline) "Cloud Standings Ready" else "Offline Mode Active",
                                    body = if (uiState.isOnline) {
                                        "Submit a completed Championship round to publish your verified official score to the Global Cloud Top 25."
                                    } else {
                                        "Reconnect to the internet and sign in to fetch live Firestore Global Cloud Top 25 rankings."
                                    }
                                )
                            }
                        } else {
                            itemsIndexed(cloudList) { idx, cloudEntry ->
                                val rank = idx + 1
                                val rankColor = when (rank) {
                                    1 -> colors.accentGold
                                    2 -> Color(0xFFB0BEC5)
                                    3 -> Color(0xFFCD7F32)
                                    else -> colors.accentInfo
                                }
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                                    border = BorderStroke(1.dp, colors.border),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .background(rankColor.copy(alpha = 0.2f), CircleShape)
                                                .border(1.dp, rankColor, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "#$rank",
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black,
                                                color = rankColor
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cloudEntry.displayName,
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Black,
                                                color = colors.textPrimary
                                            )
                                            Text(
                                                text = "Drive: ${cloudEntry.longestDriveYds.toInt()}y • Shootout: ${cloudEntry.shootoutHighScore} pts",
                                                fontFamily = PlusJakartaSansFontFamily,
                                                fontSize = 11.sp,
                                                color = colors.textSecondary
                                            )
                                        }
                                        Column(horizontalAlignment = Alignment.End) {
                                            Text(
                                                text = "${cloudEntry.trophies}",
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 20.sp,
                                                fontWeight = FontWeight.Black,
                                                color = colors.accentGold
                                            )
                                            Text(
                                                text = "TROPHIES",
                                                fontFamily = OutfitFontFamily,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = colors.textMuted
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Weekly Tour Rankings (Your verified standing + weekly cloud qualifiers)
                    val sortedCloud = uiState.globalCloudLeaderboard
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            PlayerVerifiedStandingCard(uiState = uiState)
                        }
                        if (sortedCloud.isEmpty()) {
                            item {
                                HonestCloudSyncNoteCard(
                                    title = "Weekly Qualifier Board",
                                    body = "Complete holes in Tournament Mode or Weekly Cups to register verified Weekly Tour points with Cloud Sync."
                                )
                            }
                        } else {
                            itemsIndexed(sortedCloud.take(10)) { idx, cloudEntry ->
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                                    border = BorderStroke(1.dp, colors.border),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("#${idx + 1} • ${cloudEntry.displayName}", fontFamily = OutfitFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                            Text("Shootout Best: ${cloudEntry.shootoutHighScore} pts", fontFamily = PlusJakartaSansFontFamily, fontSize = 11.sp, color = colors.textSecondary)
                                        }
                                        Text("${cloudEntry.trophies} Cups", fontFamily = OutfitFontFamily, fontSize = 14.sp, fontWeight = FontWeight.Black, color = colors.accentGold)
                                    }
                                }
                            }
                        }
                    }
                }

                else -> {
                    // Friends Clubhouse Leaderboard
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            PlayerVerifiedStandingCard(uiState = uiState)
                        }
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                border = BorderStroke(1.dp, colors.border),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Group, contentDescription = null, tint = colors.accentInfo, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "No Linked Friends Yet",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = if (uiState.isGuestSession) {
                                                "Sign in with Google or Email in Settings to unlock cloud friend leaderboards."
                                            } else {
                                                "Your verified scores sync to Firestore automatically. Friend clubhouse invites appear here when linked."
                                            },
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 11.sp,
                                            color = colors.textSecondary,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    if (uiState.tournamentCompleted) {
                        onRestartTournament()
                    } else {
                        onBack()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.accentGold,
                    contentColor = colors.onGold
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("leaderboard_cta_btn")
            ) {
                Icon(
                    imageVector = if (uiState.tournamentCompleted) Icons.Default.Refresh else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (uiState.tournamentCompleted) "NEW TOURNAMENT ROUND" else "RESUME TOURNAMENT",
                    fontFamily = OutfitFontFamily,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun PlayerVerifiedStandingCard(uiState: GameUiState) {
    val colors = ZenithTheme.colors
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = colors.forestCard),
        border = BorderStroke(1.5.dp, colors.accentGold),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = colors.accentGold,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${uiState.golferProfile.golferName} (YOU)",
                    fontFamily = OutfitFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.onForestCard
                )
                Text(
                    text = "Longest Drive: ${uiState.career.longestDriveYds.toInt()} yds • Shootout Best: ${uiState.career.shootoutHighScore}",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.sp,
                    color = colors.onForestSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${uiState.career.trophies}",
                    fontFamily = OutfitFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.accentGold
                )
                Text(
                    text = "TROPHIES",
                    fontFamily = OutfitFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.onForestSecondary
                )
            }
        }
    }
}

@Composable
private fun HonestCloudSyncNoteCard(title: String, body: String) {
    val colors = ZenithTheme.colors
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = colors.surface,
        border = BorderStroke(1.dp, colors.border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontFamily = OutfitFontFamily,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = body,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 11.sp,
                color = colors.textSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
