package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TrackChanges
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.model.ClubType
import com.example.game.model.ShootoutShotResult
import com.example.game.ui.CourseCanvas
import com.example.game.ui.components.AiCaddieBanner
import com.example.game.ui.components.SwingControlDeck
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.ZenithCyan
import com.example.ui.theme.ZenithDarkBg
import com.example.ui.theme.ZenithGold
import com.example.ui.theme.ZenithHazardRed
import com.example.ui.theme.ZenithNeonGreen
import com.example.ui.theme.ZenithSurface
import com.example.ui.theme.ZenithSurfaceBorder
import com.example.ui.theme.ZenithSurfaceElevated
import com.example.ui.theme.ZenithTextPrimary
import com.example.ui.theme.ZenithTextSecondary

@Composable
fun TargetShootoutScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onSelectClub: (ClubType) -> Unit,
    onAimAdjust: (angle: Float, power: Float) -> Unit,
    onAdjustAngle: (deltaDeg: Float) -> Unit,
    onSetPower: (Float) -> Unit,
    onSetSpin: (Float) -> Unit,
    onStartApexSwing: () -> Unit,
    onLockReleaseSwing: () -> Unit,
    onMulligan: () -> Unit,
    onPlayAgain: () -> Unit,
    onSwingDragStart: (androidx.compose.ui.geometry.Offset) -> Unit = {},
    onSwingDragUpdate: (
        currentPos: androidx.compose.ui.geometry.Offset,
        ballScreenPos: androidx.compose.ui.geometry.Offset,
        maxPullPx: Float
    ) -> Unit = { _, _, _ -> },
    onSwingDragRelease: () -> Unit = {},
    onSwingDragCancel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val shootout = uiState.shootoutState

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ZenithDarkBg)
            .testTag("target_shootout_screen")
    ) {
        // 1. Course Canvas with Concentric Target Rings & Touch-Anywhere Drag-and-Release Swing
        CourseCanvas(
            uiState = uiState,
            onAimAdjust = onAimAdjust,
            onSwingDragStart = onSwingDragStart,
            onSwingDragUpdate = onSwingDragUpdate,
            onSwingDragRelease = onSwingDragRelease,
            onSwingDragCancel = onSwingDragCancel,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Arcade Telemetry HUD
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            ShootoutTopBar(
                shootoutState = shootout,
                onBack = onBack
            )

            Spacer(modifier = Modifier.height(6.dp))

            // AI Caddie Tip
            AiCaddieBanner(
                advice = uiState.caddieAdvice,
                isLoading = uiState.isAiCaddieLoading,
                onAskAi = {},
                modifier = Modifier.padding(horizontal = 14.dp)
            )
        }

        // 3. In-flight Live Distance Badge
        AnimatedVisibility(
            visible = uiState.isBallInFlight,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xDD0A140E),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, ZenithGold)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 22.dp, vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FLIGHT CARRY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = ZenithGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${uiState.ballState.distanceTraveled.toInt()} YDS",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                }
            }
        }

        // 4. Bottom Controls (Club, Aim, Apex Meter, Swing Button)
        if (!shootout.isRoundOver) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                SwingControlDeck(
                    uiState = uiState,
                    onSelectClub = onSelectClub,
                    onAdjustAngle = onAdjustAngle,
                    onSetPower = onSetPower,
                    onSetSpin = onSetSpin,
                    onStartApexSwing = onStartApexSwing,
                    onLockReleaseSwing = onLockReleaseSwing,
                    onMulligan = onMulligan
                )
            }
        }

        // 5. Round Over Summary Dialog
        if (shootout.isRoundOver) {
            ShootoutGameOverDialog(
                shootoutState = shootout,
                onPlayAgain = onPlayAgain,
                onExit = onBack
            )
        }
    }
}

@Composable
fun ShootoutTopBar(
    shootoutState: com.example.game.model.ShootoutGameState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("shootout_top_bar"),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = ZenithSurface.copy(alpha = 0.95f)),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = Brush.horizontalGradient(listOf(ZenithGold.copy(alpha = 0.6f), ZenithNeonGreen.copy(alpha = 0.4f)))
        )
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Exit & Title
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("shootout_exit_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Exit Shootout",
                            tint = ZenithTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrackChanges,
                                contentDescription = null,
                                tint = ZenithGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "BULLSEYE SHOOTOUT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = ZenithGold,
                                letterSpacing = 1.sp
                            )
                        }
                        Text(
                            text = "Target Coliseum",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZenithTextPrimary
                        )
                    }
                }

                // Live Points Counter
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SCORE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZenithTextSecondary
                    )
                    Text(
                        text = "${shootoutState.currentScore} PTS",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = ZenithGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Balls Remaining Tracker + Combo Multiplier
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Golf Balls Remaining Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "BALLS:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZenithTextSecondary
                    )
                    for (i in 1..shootoutState.totalShots) {
                        val isUsed = i > shootoutState.shotsRemaining
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(
                                    if (!isUsed) ZenithNeonGreen else Color(0x33B0BEC5)
                                )
                                .border(
                                    1.dp,
                                    if (!isUsed) Color.White else Color(0x55B0BEC5),
                                    CircleShape
                                )
                        )
                    }
                }

                // Combo Badge
                if (shootoutState.currentCombo > 1) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ZenithHazardRed.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ZenithHazardRed)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = ZenithHazardRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "COMBO x${1.0f + (shootoutState.currentCombo - 1) * 0.5f}!",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = ZenithHazardRed
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ZenithSurfaceElevated
                    ) {
                        Text(
                            text = "TARGET: 145 YDS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZenithCyan,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ShootoutGameOverDialog(
    shootoutState: com.example.game.model.ShootoutGameState,
    onPlayAgain: () -> Unit,
    onExit: () -> Unit
) {
    Dialog(onDismissRequest = onExit) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1E13)),
            border = androidx.compose.foundation.BorderStroke(2.dp, ZenithGold),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("shootout_summary_dialog")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Trophy Icon
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(ZenithGold.copy(alpha = 0.2f))
                        .border(1.5.dp, ZenithGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.EmojiEvents,
                        contentDescription = null,
                        tint = ZenithGold,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "SHOOTOUT COMPLETE!",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = ZenithGold,
                    letterSpacing = 1.sp
                )

                if (shootoutState.isNewHighScore) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ZenithNeonGreen.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, ZenithNeonGreen)
                    ) {
                        Text(
                            text = "NEW HIGH SCORE RECORD!",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = ZenithNeonGreen,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Score Stat Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = ZenithSurface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "FINAL SCORE",
                            fontSize = 12.sp,
                            color = ZenithTextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${shootoutState.currentScore} PTS",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = ZenithGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "+${shootoutState.coinsWon}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ZenithGold
                                    )
                                }
                                Text(
                                    text = "Coins Won",
                                    fontSize = 10.sp,
                                    color = ZenithTextSecondary
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Stars,
                                        contentDescription = null,
                                        tint = ZenithCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "+${shootoutState.xpWon}",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = ZenithCyan
                                    )
                                }
                                Text(
                                    text = "Tour XP",
                                    fontSize = 10.sp,
                                    color = ZenithTextSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Shot Results Log
                Text(
                    text = "SHOT BREAKDOWN",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZenithNeonGreen,
                    letterSpacing = 1.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    shootoutState.shotResults.forEach { shot ->
                        ShotResultRow(shot = shot)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Button(
                    onClick = onPlayAgain,
                    colors = ButtonDefaults.buttonColors(containerColor = ZenithNeonGreen, contentColor = Color(0xFF003816)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("shootout_play_again_btn")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "PLAY AGAIN (5 SHOTS)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = onExit,
                    colors = ButtonDefaults.buttonColors(containerColor = ZenithSurfaceElevated, contentColor = ZenithTextPrimary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("shootout_exit_summary_btn")
                ) {
                    Text(text = "BACK TO LOBBY", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ShotResultRow(shot: ShootoutShotResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = ZenithSurfaceElevated)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (shot.isBullseye) ZenithGold else ZenithSurfaceBorder,
                    modifier = Modifier.size(20.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#${shot.shotIndex}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (shot.isBullseye) Color.Black else ZenithTextPrimary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = shot.ringHit,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (shot.isBullseye) ZenithGold else ZenithTextPrimary
                    )
                    Text(
                        text = "${String.format("%.1f", shot.distanceToBullseyeYds)} yds from pin",
                        fontSize = 10.sp,
                        color = ZenithTextSecondary
                    )
                }
            }

            Text(
                text = "+${shot.pointsEarned} pts",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = if (shot.pointsEarned > 0) ZenithNeonGreen else Color(0xFFFF5252)
            )
        }
    }
}
