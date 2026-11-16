package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.MatchPlayRival
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.ZenithTheme

@Composable
fun MatchPlayDuelScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onSelectRival: (MatchPlayRival) -> Unit,
    onStartDuel: (MatchPlayRival) -> Unit,
    onProceedNextHole: () -> Unit,
    onExitMatch: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val colors = ZenithTheme.colors
    val scrollState = rememberScrollState()
    val matchState = uiState.matchPlayState

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("match_play_duel_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("match_play_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "1v1 MATCH PLAY SHOWDOWN",
                            fontFamily = OutfitFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Head-to-Head Hole-By-Hole Rival Duel",
                            fontSize = 11.sp,
                            color = colors.accentInfo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (matchState.isActive) {
                ActiveMatchPlayView(
                    uiState = uiState,
                    onProceedNextHole = onProceedNextHole,
                    onExitMatch = onExitMatch
                )
            } else {
                MatchPlayLobbyView(
                    uiState = uiState,
                    onSelectRival = onSelectRival,
                    onStartDuel = onStartDuel
                )
            }
        }
    }
}

@Composable
private fun MatchPlayLobbyView(
    uiState: GameUiState,
    onSelectRival: (MatchPlayRival) -> Unit,
    onStartDuel: (MatchPlayRival) -> Unit
) {
    val colors = ZenithTheme.colors
    val selectedRival = uiState.selectedMatchPlayRival

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = colors.forestCard,
            border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.accentGold.copy(alpha = 0.22f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentGold)
                    ) {
                        Text(
                            text = "HEAD-TO-HEAD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "TOUR RIVAL MATCHPLAY",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.onForestCard
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "High-Stakes 1v1 Hole Dueling",
                    fontFamily = OutfitFontFamily,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.onForestCard
                )
                Text(
                    text = "Win individual holes to go 1 UP. Defeat your opponent across the 3-hole sprint to claim the gold purse and tour trophies!",
                    fontSize = 12.sp,
                    color = colors.onForestSecondary,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.onForestCard.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("CAREER MATCHPLAY RECORD", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = colors.onForestSecondary)
                        Text(
                            "${uiState.career.matchPlayWins}W - ${uiState.career.matchPlayLosses}L",
                            fontFamily = OutfitFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.onForestCard
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = colors.accentGold.copy(alpha = 0.22f)
                    ) {
                        Text(
                            text = "3-Hole Sprint",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accentGold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "CHOOSE YOUR OPPONENT",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = colors.accentInfo,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        for (rival in uiState.matchPlayRivals) {
            val isSelected = rival.id == selectedRival.id
            Card(
                onClick = { onSelectRival(rival) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isSelected) colors.surfaceElevated else colors.surface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    if (isSelected) 2.dp else 1.dp,
                    if (isSelected) colors.primaryGreen else colors.border
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .testTag("rival_card_${rival.id}")
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(rival.avatarColor.copy(alpha = 0.2f))
                            .border(2.dp, rival.avatarColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsGolf,
                            contentDescription = null,
                            tint = rival.avatarColor,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = rival.name,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = colors.surfaceElevated
                            ) {
                                Text(
                                    text = rival.rank,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "${rival.archetype} • Drive ${rival.drivingDistanceYds}y • Putt ${rival.puttingRating}/100",
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "\"${rival.introQuote}\"",
                            fontSize = 11.sp,
                            color = colors.accentInfo,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.MonetizationOn, null, tint = colors.accentGold, modifier = Modifier.size(14.dp))
                            Text(
                                text = "+${rival.winRewardCoins}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentGold
                            )
                        }
                        Text(
                            text = "Wager ${rival.wagerCostCoins}",
                            fontSize = 10.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { onStartDuel(selectedRival) },
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.primaryGreen,
                contentColor = colors.onForestCard
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("start_match_duel_btn")
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "START 1v1 DUEL VS ${selectedRival.name.uppercase()}",
                fontFamily = OutfitFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun ActiveMatchPlayView(
    uiState: GameUiState,
    onProceedNextHole: () -> Unit,
    onExitMatch: () -> Unit
) {
    val colors = ZenithTheme.colors
    val match = uiState.matchPlayState
    val rival = match.rival ?: uiState.matchPlayRivals[0]

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = colors.surface,
            border = androidx.compose.foundation.BorderStroke(
                2.dp,
                if (match.isMatchFinished && match.matchWinner == "PLAYER") colors.accentGold else colors.accentInfo
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (match.isMatchFinished) colors.accentGold.copy(alpha = 0.16f) else colors.accentInfo.copy(alpha = 0.16f)
                ) {
                    Text(
                        text = if (match.isMatchFinished) "MATCH COMPLETED" else "HOLE ${match.currentMatchHole} OF ${match.totalMatchHoles}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = if (match.isMatchFinished) colors.accentGold else colors.accentInfo,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        letterSpacing = 1.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = match.matchScoreStatus,
                    fontFamily = OutfitFontFamily,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = when {
                        match.scoreDifferential > 0 -> colors.accentSuccess
                        match.scoreDifferential < 0 -> colors.error
                        else -> colors.accentInfo
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .background(colors.surfaceElevated, RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("YOU", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.accentSuccess)
                        Text("${match.playerHolesWon} Holes", fontSize = 16.sp, fontWeight = FontWeight.Black, color = colors.textPrimary)
                    }
                    Text("VS", fontSize = 14.sp, fontWeight = FontWeight.Black, color = colors.textMuted)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(rival.name.split(" ").first(), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = rival.avatarColor)
                        Text("${match.rivalHolesWon} Holes", fontSize = 16.sp, fontWeight = FontWeight.Black, color = colors.textPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (match.rivalShotBanner.isNotBlank()) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = colors.surfaceElevated,
                border = androidx.compose.foundation.BorderStroke(1.dp, rival.avatarColor.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(rival.avatarColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SportsGolf, null, tint = rival.avatarColor, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = match.rivalShotBanner,
                        fontSize = 13.sp,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Medium,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "MATCH SCORECARD",
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = colors.textSecondary,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(6.dp))

        for (result in match.holeResults) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = colors.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Hole ${result.holeNumber}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            text = "You: ${result.playerStrokes}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentSuccess
                        )
                        Text(
                            text = "${rival.name.split(" ").first()}: ${result.rivalStrokes}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = rival.avatarColor
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (result.holeWinner) {
                            "PLAYER" -> colors.accentSuccess.copy(alpha = 0.16f)
                            "RIVAL" -> colors.error.copy(alpha = 0.16f)
                            else -> colors.accentInfo.copy(alpha = 0.16f)
                        }
                    ) {
                        Text(
                            text = when (result.holeWinner) {
                                "PLAYER" -> "YOU WON"
                                "RIVAL" -> "RIVAL WON"
                                else -> "HALVED"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = when (result.holeWinner) {
                                "PLAYER" -> colors.accentSuccess
                                "RIVAL" -> colors.error
                                else -> colors.accentInfo
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (match.isMatchFinished) {
            Button(
                onClick = onExitMatch,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryGreen,
                    contentColor = colors.onForestCard
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("match_finish_continue_btn")
            ) {
                Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("RETURN TO CLUBHOUSE", fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
        } else {
            Button(
                onClick = onProceedNextHole,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryGreen,
                    contentColor = colors.onForestCard
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("match_proceed_hole_btn")
            ) {
                Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("TEE OFF HOLE ${match.currentMatchHole + 1}", fontSize = 14.sp, fontWeight = FontWeight.Black)
            }
        }
    }
}
