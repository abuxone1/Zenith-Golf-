package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.SlowMotionVideo
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.ReplayShot
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.ZenithCyan
import com.example.ui.theme.ZenithEmerald
import com.example.ui.theme.ZenithGold
import com.example.ui.theme.ZenithNeonGreen
import com.example.ui.theme.ZenithTextMuted
import com.example.ui.theme.ZenithTheme

@Composable
fun ReplayTheaterScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onSelectReplay: (ReplayShot) -> Unit,
    onSaveCurrentShot: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val colors = ZenithTheme.colors
    val scrollState = rememberScrollState()
    val activeReplay = uiState.activeReplayShot ?: uiState.savedReplays.firstOrNull()
    var isSlowMoActive by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("replay_theater_screen"),
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
                            .testTag("replay_back_btn")
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
                            text = "SHOT REPLAY & TELEMETRY",
                            fontFamily = OutfitFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Shot Archive & Ball Radar Diagnostics",
                            fontSize = 11.sp,
                            color = colors.accentInfo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (activeReplay != null) {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF0A1F11),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, colors.accentGold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            ReplayCanvasSimulation(
                                replay = activeReplay,
                                modifier = Modifier.fillMaxSize()
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xCC0A140E),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ZenithCyan)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.SlowMotionVideo, null, tint = ZenithCyan, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "REPLAY: ${activeReplay.clubUsed.shortName}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ZenithCyan
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ZenithNeonGreen.copy(alpha = 0.25f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ZenithNeonGreen)
                                ) {
                                    Text(
                                        text = activeReplay.scoreAchieved,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = ZenithNeonGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(bottomStart = 18.dp, bottomEnd = 18.dp),
                                color = Color(0xDD0A140E),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .align(Alignment.BottomCenter)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                        .fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    MetricColumn("CARRY", "${activeReplay.carryYards.toInt()} YDS", ZenithNeonGreen)
                                    MetricColumn("BALL SPEED", "${activeReplay.ballSpeedMph.toInt()} MPH", ZenithCyan)
                                    MetricColumn("LAUNCH", "${activeReplay.launchAngleDeg}°", ZenithGold)
                                    MetricColumn("APEX", "${activeReplay.apexHeightFt.toInt()} FT", Color.White)
                                    MetricColumn("SPIN", "${activeReplay.spinRpm} RPM", ZenithEmerald)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { isSlowMoActive = !isSlowMoActive },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSlowMoActive) colors.primaryGreen else colors.surfaceElevated,
                                contentColor = if (isSlowMoActive) colors.onForestCard else colors.textPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.SlowMotionVideo, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isSlowMoActive) "SLOW-MO: ON" else "SLOW-MO (0.5x)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = onSaveCurrentShot,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.surfaceElevated,
                                contentColor = colors.accentGold
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(Icons.Default.Bookmark, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("SAVE TO VAULT", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "HALL OF FAME SHOTS (${uiState.savedReplays.size})",
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = colors.accentGold,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (replay in uiState.savedReplays) {
                    val isSelected = activeReplay?.id == replay.id
                    Card(
                        onClick = { onSelectReplay(replay) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) colors.surfaceElevated else colors.surface
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) colors.primaryGreen else colors.border
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("replay_item_${replay.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = colors.accentSuccess.copy(alpha = 0.16f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SportsGolf,
                                        contentDescription = null,
                                        tint = colors.accentSuccess,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = replay.title,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.textPrimary
                                )
                                Text(
                                    text = "${replay.holeName} • ${replay.clubUsed.displayName}",
                                    fontSize = 11.sp,
                                    color = colors.textSecondary
                                )
                                Text(
                                    text = "${replay.carryYards.toInt()} YDS • ${replay.ballSpeedMph.toInt()} MPH • Apex ${replay.apexHeightFt.toInt()}ft",
                                    fontSize = 10.sp,
                                    color = colors.accentInfo
                                )
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.accentGold.copy(alpha = 0.16f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.5f))
                            ) {
                                Text(
                                    text = replay.scoreAchieved,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = colors.accentGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 8.sp, fontWeight = FontWeight.Bold, color = ZenithTextMuted)
        Text(text = value, fontSize = 11.sp, fontWeight = FontWeight.Black, color = color)
    }
}

@Composable
private fun ReplayCanvasSimulation(replay: ReplayShot, modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Ground turf base
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF0A1F11),
                    Color(0xFF041008)
                )
            ),
            size = size
        )

        // Trajectory arc simulation
        val startPt = Offset(40f, h - 50f)
        val endPt = Offset(w - 50f, h - 50f)
        val peakPt = Offset(w * 0.45f, h * 0.25f)

        val path = Path().apply {
            moveTo(startPt.x, startPt.y)
            quadraticTo(peakPt.x, peakPt.y, endPt.x, endPt.y)
        }

        // Glow arc
        drawPath(
            path = path,
            color = Color(0x4400E5FF),
            style = Stroke(width = 8f, cap = StrokeCap.Round)
        )

        // Bright flight ribbon
        drawPath(
            path = path,
            brush = Brush.linearGradient(
                colors = listOf(ZenithNeonGreen, ZenithCyan, ZenithGold)
            ),
            style = Stroke(width = 3f, cap = StrokeCap.Round)
        )

        // Landing target ripple
        drawCircle(
            color = ZenithGold.copy(alpha = 0.5f),
            radius = 16f,
            center = endPt,
            style = Stroke(width = 2f)
        )
        drawCircle(
            color = ZenithGold,
            radius = 4f,
            center = endPt
        )

        // Tee point
        drawCircle(
            color = ZenithNeonGreen,
            radius = 4f,
            center = startPt
        )
    }
}
