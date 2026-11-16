package com.example.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.model.HoleStatistics
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.ZenithTheme

@Composable
fun HoleStatisticsDialog(
    stats: HoleStatistics,
    onDismiss: () -> Unit
) {
    val colors = ZenithTheme.colors

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, colors.accentGold.copy(alpha = 0.55f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("hole_statistics_dialog")
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(colors.accentInfo.copy(alpha = 0.16f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueryStats,
                                contentDescription = null,
                                tint = colors.accentInfo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "HOLE #${stats.holeNumber} TELEMETRY",
                                fontFamily = OutfitFontFamily,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentInfo,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Par ${stats.par} • ${stats.totalHoleYardage} Yards",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("close_stats_dialog_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = colors.textMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StatPillCard(
                        title = "STROKES",
                        value = "${stats.strokesTaken}",
                        valueColor = colors.accentGold,
                        modifier = Modifier.weight(1f)
                    )
                    StatPillCard(
                        title = "TO PIN",
                        value = "${stats.remainingDistanceToPin.toInt()}y",
                        valueColor = colors.accentInfo,
                        modifier = Modifier.weight(1f)
                    )
                    StatPillCard(
                        title = "TRAVELED",
                        value = "${stats.distanceTraveled.toInt()}y",
                        valueColor = colors.accentSuccess,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (stats.fairwayHit) colors.accentSuccess.copy(alpha = 0.16f) else colors.surfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (stats.fairwayHit) colors.accentSuccess else colors.border),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FAIRWAY: ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMuted
                            )
                            Text(
                                text = if (stats.fairwayHit) "HIT" else if (stats.strokesTaken > 0) "MISSED" else "PENDING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (stats.fairwayHit) colors.accentSuccess else colors.textSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (stats.greenInRegulation) colors.accentInfo.copy(alpha = 0.16f) else colors.surfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (stats.greenInRegulation) colors.accentInfo else colors.border),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "GIR: ",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textMuted
                            )
                            Text(
                                text = if (stats.greenInRegulation) "ON GREEN" else "PENDING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (stats.greenInRegulation) colors.accentInfo else colors.textSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SHOT-BY-SHOT BREAKDOWN",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.accentSuccess,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (stats.shotLogs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .background(colors.surfaceElevated, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Awaiting tee shot on this hole...",
                            fontSize = 12.sp,
                            color = colors.textMuted
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(stats.shotLogs) { log ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = colors.surfaceElevated,
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Shot #${log.strokeNumber}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colors.accentGold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "${log.clubUsed.shortName} • ${log.yardage.toInt()}y",
                                            fontSize = 12.sp,
                                            color = colors.textPrimary
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = colors.accentInfo.copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = log.apexRating,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = colors.accentInfo,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
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
}

@Composable
private fun StatPillCard(
    title: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    val colors = ZenithTheme.colors
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Black,
                color = valueColor
            )
        }
    }
}
