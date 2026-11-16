package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.ZenithTheme

@Composable
fun HoleSummaryScreen(
    uiState: GameUiState,
    onNextHole: () -> Unit,
    onViewLeaderboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onNextHole()
    }

    val colors = ZenithTheme.colors
    val hole = uiState.currentHole
    val strokes = uiState.currentStrokeCount
    val scoreDiff = strokes - hole.par

    val glowColor = when {
        strokes == 1 || scoreDiff <= -2 -> colors.accentGold
        scoreDiff == -1 -> colors.accentSuccess
        scoreDiff == 0 -> colors.accentInfo
        else -> Color(0xFFE67E22)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("hole_summary_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 680.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .background(glowColor.copy(alpha = 0.16f), CircleShape)
                    .border(2.dp, glowColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = uiState.holeScoreName,
                    tint = glowColor,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = uiState.holeScoreName,
                fontFamily = OutfitFontFamily,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = glowColor,
                textAlign = TextAlign.Center,
                letterSpacing = 1.sp
            )

            Text(
                text = "Hole #${hole.holeNumber} • ${hole.holeTitle}",
                fontSize = 14.sp,
                color = colors.textSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(22.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryStatItem(label = "PAR", value = "${hole.par}")
                        SummaryStatItem(label = "STROKES", value = "$strokes", valueColor = glowColor)
                        SummaryStatItem(
                            label = "DIFF",
                            value = if (scoreDiff > 0) "+$scoreDiff" else if (scoreDiff == 0) "E" else "$scoreDiff"
                        )
                        SummaryStatItem(
                            label = "TOTAL",
                            value = "${uiState.playerScores.sum()}",
                            valueColor = colors.accentInfo
                        )
                        SummaryStatItem(
                            label = "ROUND",
                            value = uiState.roundScoreToParText,
                            valueColor = colors.accentGold
                        )
                    }

                    if (uiState.holeShotLogs.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "HOLE SHOT TELEMETRY RECAP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentSuccess,
                            letterSpacing = 0.8.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        uiState.holeShotLogs.takeLast(4).forEach { log ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Stroke #${log.strokeNumber} • ${log.clubUsed.displayName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "${log.yardage.toInt()} yds → ${log.endLie.displayName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accentInfo
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.forestCard,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MonetizationOn,
                                contentDescription = null,
                                tint = colors.accentGold,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "+${uiState.coinsEarnedThisHole} TOURNAMENT COINS EARNED!",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.onForestCard
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onNextHole,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryGreen,
                    contentColor = colors.onForestCard
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("summary_next_hole_btn")
            ) {
                Text(
                    text = if (uiState.currentHoleIndex >= uiState.holes.size - 1) "COMPLETE MATCH & RESULTS" else "PROCEED TO HOLE ${uiState.currentHoleIndex + 2}",
                    fontFamily = OutfitFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onViewLeaderboard,
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("summary_view_leaderboard_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.FormatListNumbered,
                    contentDescription = null,
                    tint = colors.accentInfo,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VIEW MATCH RESULTS & STANDINGS",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.accentInfo
                )
            }
        }
    }
}

@Composable
private fun SummaryStatItem(
    label: String,
    value: String,
    valueColor: Color = ZenithTheme.colors.textPrimary
) {
    val colors = ZenithTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = colors.textMuted,
            letterSpacing = 0.8.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontFamily = OutfitFontFamily,
            fontSize = 19.sp,
            fontWeight = FontWeight.Black,
            color = valueColor
        )
    }
}
