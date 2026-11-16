package com.example.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.ClubType
import com.example.game.model.SurfaceType
import com.example.game.model.WindCondition
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.ZenithBunker
import com.example.ui.theme.ZenithCyan
import com.example.ui.theme.ZenithEmerald
import com.example.ui.theme.ZenithGold
import com.example.ui.theme.ZenithHazardRed
import com.example.ui.theme.ZenithNeonGreen
import com.example.ui.theme.ZenithRough
import com.example.ui.theme.ZenithSurface
import com.example.ui.theme.ZenithSurfaceBorder
import com.example.ui.theme.ZenithSurfaceElevated
import com.example.ui.theme.ZenithTextMuted
import com.example.ui.theme.ZenithTextPrimary
import com.example.ui.theme.ZenithTextSecondary
import com.example.ui.theme.ZenithWater

@Composable
fun TopHoleHeader(
    uiState: GameUiState,
    onBack: () -> Unit,
    onOpenStats: () -> Unit = {},
    onCycleCamera: () -> Unit = {},
    onCycleWeather: () -> Unit = {},
    onToggleRangefinder: () -> Unit = {},
    onTogglePhysicsDebug: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hole = uiState.currentHole
    val distYds = uiState.currentDistanceToPin.toInt()
    val lie = uiState.ballState.lie

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_hole_header"),
        shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
        colors = CardDefaults.cardColors(containerColor = ZenithSurface.copy(alpha = 0.94f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(ZenithSurfaceBorder, ZenithNeonGreen.copy(alpha = 0.3f))))
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Hole Info Pill
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Exit Hole",
                            tint = ZenithTextPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = if (uiState.isPracticeMode) "Practice Range" else "HOLE ${hole.holeNumber} • PAR ${hole.par}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZenithNeonGreen,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = hole.holeTitle,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = ZenithTextPrimary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    // Camera Toggle Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ZenithSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ZenithCyan.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clickable { onCycleCamera() }
                            .testTag("cycle_camera_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Switch Camera View",
                                tint = ZenithCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = uiState.cameraMode.label.take(4).uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = ZenithCyan
                            )
                        }
                    }

                    // Weather Cycle Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ZenithSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ZenithGold.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clickable { onCycleWeather() }
                            .testTag("cycle_weather_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Weather",
                                tint = ZenithGold,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // GPS Rangefinder Toggle Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (uiState.gpsRangefinder.isEnabled) ZenithNeonGreen.copy(alpha = 0.25f) else ZenithSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (uiState.gpsRangefinder.isEnabled) ZenithNeonGreen else ZenithCyan.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .clickable { onToggleRangefinder() }
                            .testTag("toggle_rangefinder_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "GPS Rangefinder",
                                tint = if (uiState.gpsRangefinder.isEnabled) ZenithNeonGreen else ZenithCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "GPS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (uiState.gpsRangefinder.isEnabled) ZenithNeonGreen else ZenithCyan
                            )
                        }
                    }

                    // Telemetry Stats Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ZenithSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ZenithCyan.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clickable { onOpenStats() }
                            .testTag("open_hole_stats_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.QueryStats,
                                contentDescription = "Hole Stats",
                                tint = ZenithCyan,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "STATS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = ZenithCyan
                            )
                        }
                    }

                    // Physics & Collision Debug Gizmo Toggle Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (uiState.isPhysicsDebugEnabled) ZenithGold.copy(alpha = 0.25f) else ZenithSurfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (uiState.isPhysicsDebugEnabled) ZenithGold else ZenithGold.copy(alpha = 0.4f)
                        ),
                        modifier = Modifier
                            .clickable { onTogglePhysicsDebug() }
                            .testTag("toggle_physics_debug_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "DBG",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = if (uiState.isPhysicsDebugEnabled) ZenithGold else ZenithTextSecondary
                            )
                        }
                    }

                    // Stroke Counter Box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ZenithSurfaceElevated,
                        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(ZenithNeonGreen.copy(alpha = 0.4f), Color.Transparent)))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "S",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ZenithTextMuted
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = "${uiState.currentStrokeCount + 1}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = ZenithGold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Sub-row: Distance to pin, Lie badge, Wind Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Distance to pin
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = "Pin Distance",
                        tint = ZenithCyan,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$distYds",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = ZenithTextPrimary
                    )
                    Text(
                        text = " YDS TO PIN",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZenithTextMuted,
                        modifier = Modifier.padding(start = 4.dp, top = 6.dp)
                    )
                }

                // Lie Badge
                LieBadge(lie = lie)

                // Wind Vane
                WindVaneWidget(wind = uiState.wind)
            }
        }
    }
}

@Composable
fun LieBadge(lie: SurfaceType) {
    val (bg, fg) = when (lie) {
        SurfaceType.TEE -> Pair(ZenithNeonGreen.copy(alpha = 0.2f), ZenithNeonGreen)
        SurfaceType.FAIRWAY -> Pair(ZenithEmerald.copy(alpha = 0.2f), ZenithEmerald)
        SurfaceType.ROUGH -> Pair(ZenithRough, Color(0xFFC8E6C9))
        SurfaceType.SAND_BUNKER -> Pair(ZenithBunker.copy(alpha = 0.3f), ZenithBunker)
        SurfaceType.GREEN -> Pair(ZenithCyan.copy(alpha = 0.25f), ZenithCyan)
        SurfaceType.WATER_HAZARD -> Pair(ZenithWater.copy(alpha = 0.3f), ZenithWater)
        SurfaceType.OUT_OF_BOUNDS -> Pair(ZenithHazardRed.copy(alpha = 0.3f), ZenithHazardRed)
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bg,
        modifier = Modifier.testTag("lie_badge")
    ) {
        Text(
            text = lie.displayName.uppercase(),
            fontSize = 10.sp,
            fontWeight = FontWeight.Black,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun WindVaneWidget(wind: WindCondition) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(ZenithSurfaceElevated, RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .testTag("wind_vane")
    ) {
        Icon(
            imageVector = Icons.Default.Navigation,
            contentDescription = "Wind Direction",
            tint = ZenithCyan,
            modifier = Modifier
                .size(16.dp)
                .rotate(wind.directionDeg)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "${wind.speedMph.toInt()} MPH",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = ZenithCyan
        )
    }
}

@Composable
fun AiCaddieBanner(
    advice: String,
    isLoading: Boolean,
    onAskAi: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("ai_caddie_banner"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = ZenithSurfaceElevated.copy(alpha = 0.92f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(ZenithCyan.copy(alpha = 0.5f), ZenithNeonGreen.copy(alpha = 0.3f))))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(ZenithCyan.copy(alpha = 0.2f), CircleShape)
                    .border(1.dp, ZenithCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Caddie",
                    tint = ZenithCyan,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ZENITH AI CADDIE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = ZenithCyan,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "• SMART ADVISOR",
                        fontSize = 9.sp,
                        color = ZenithTextMuted
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = advice,
                    fontSize = 12.sp,
                    color = ZenithTextPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = onAskAi,
                enabled = !isLoading,
                modifier = Modifier
                    .size(36.dp)
                    .background(ZenithSurfaceBorder, CircleShape)
                    .testTag("ask_ai_caddie_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = ZenithCyan,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Caddie Advice",
                        tint = ZenithCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SwingControlDeck(
    uiState: GameUiState,
    onSelectClub: (ClubType) -> Unit,
    onAdjustAngle: (deltaDeg: Float) -> Unit,
    onSetPower: (Float) -> Unit,
    onSetSpin: (Float) -> Unit,
    onStartApexSwing: () -> Unit,
    onLockReleaseSwing: () -> Unit,
    onMulligan: () -> Unit,
    onSetVerticalSpin: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .widthIn(max = 680.dp)
            .fillMaxWidth()
            .testTag("swing_control_deck"),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = ZenithSurface.copy(alpha = 0.96f)),
        border = CardDefaults.outlinedCardBorder().copy(brush = Brush.verticalGradient(listOf(ZenithNeonGreen.copy(alpha = 0.4f), ZenithSurfaceBorder)))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Club Selector Row
            ClubSelectorRow(
                selectedClub = uiState.selectedClub,
                onSelectClub = onSelectClub,
                disabled = uiState.isBallInFlight || uiState.isApexTimingActive
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Aim & Spin Tuning Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Angle Stepper
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.background(ZenithSurfaceElevated, RoundedCornerShape(12.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    IconButton(
                        onClick = { onAdjustAngle(-2f) },
                        enabled = !uiState.isBallInFlight && !uiState.isApexTimingActive,
                        modifier = Modifier.size(32.dp).testTag("aim_left_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Aim Left", tint = ZenithCyan)
                    }
                    Text(
                        text = "AIM ${uiState.aimAngleDeg.toInt()}°",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZenithTextPrimary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                    IconButton(
                        onClick = { onAdjustAngle(2f) },
                        enabled = !uiState.isBallInFlight && !uiState.isApexTimingActive,
                        modifier = Modifier.size(32.dp).testTag("aim_right_button")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Aim Right", tint = ZenithCyan)
                    }
                }

                // Mulligan Button
                Button(
                    onClick = onMulligan,
                    enabled = uiState.career.mulligansRemaining > 0 && uiState.previousBallPos != null && !uiState.isBallInFlight,
                    colors = ButtonDefaults.buttonColors(containerColor = ZenithSurfaceElevated),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(36.dp).testTag("mulligan_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Mulligan",
                        tint = ZenithGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "MULLIGAN (${uiState.career.mulligansRemaining})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZenithGold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 3-Axis Spin Shaping Controls (Backspin / Topspin & Draw / Fade Sidespin)
            if (!uiState.selectedClub.isPutter) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Vertical Spin: Topspin / Flat / Backspin
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val vSpin = uiState.verticalSpin
                        SpinChipButton(
                            label = "TOPSPIN",
                            isSelected = vSpin < -0.2f,
                            activeColor = ZenithGold,
                            enabled = !uiState.isBallInFlight && !uiState.isApexTimingActive,
                            onClick = { onSetVerticalSpin(if (vSpin < -0.2f) 0f else -0.75f) },
                            tag = "spin_topspin_btn"
                        )
                        SpinChipButton(
                            label = "BACKSPIN",
                            isSelected = vSpin > 0.2f,
                            activeColor = ZenithCyan,
                            enabled = !uiState.isBallInFlight && !uiState.isApexTimingActive,
                            onClick = { onSetVerticalSpin(if (vSpin > 0.2f) 0f else 0.85f) },
                            tag = "spin_backspin_btn"
                        )
                    }

                    // Lateral Sidespin: Draw / Straight / Fade
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val cSpin = uiState.curveSpin
                        SpinChipButton(
                            label = "DRAW",
                            isSelected = cSpin < -0.2f,
                            activeColor = ZenithNeonGreen,
                            enabled = !uiState.isBallInFlight && !uiState.isApexTimingActive,
                            onClick = { onSetSpin(if (cSpin < -0.2f) 0f else -0.6f) },
                            tag = "spin_draw_btn"
                        )
                        SpinChipButton(
                            label = "FADE",
                            isSelected = cSpin > 0.2f,
                            activeColor = ZenithNeonGreen,
                            enabled = !uiState.isBallInFlight && !uiState.isApexTimingActive,
                            onClick = { onSetSpin(if (cSpin > 0.2f) 0f else 0.6f) },
                            tag = "spin_fade_btn"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            // Power Slider & Live Club Launch Telemetry Readout
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.width(68.dp)) {
                    Text(
                        text = "PWR ${(uiState.aimPowerRatio * 100).toInt()}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZenithNeonGreen
                    )
                    Text(
                        text = "${uiState.ballTrajectory.launchSpeedMph.toInt()} MPH • ${uiState.ballTrajectory.launchAngleDeg.toInt()}°",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ZenithTextSecondary
                    )
                }
                Slider(
                    value = uiState.aimPowerRatio,
                    onValueChange = onSetPower,
                    valueRange = 0.2f..1.0f,
                    enabled = !uiState.isBallInFlight && !uiState.isApexTimingActive,
                    colors = SliderDefaults.colors(
                        thumbColor = ZenithNeonGreen,
                        activeTrackColor = ZenithNeonGreen,
                        inactiveTrackColor = ZenithSurfaceBorder
                    ),
                    modifier = Modifier.weight(1f).testTag("power_slider")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // APEX PRECISION ZONE METER OR SWING BUTTON
            if (uiState.isApexTimingActive) {
                ApexTimingBar(
                    meterPosition = uiState.apexMeterPosition,
                    onLockImpact = onLockReleaseSwing
                )
            } else {
                // Primary Swing Trigger
                Button(
                    onClick = onStartApexSwing,
                    enabled = !uiState.isBallInFlight && !uiState.holeCompleted,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZenithNeonGreen,
                        contentColor = Color(0xFF003816)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("swing_button")
                        .shadow(8.dp, RoundedCornerShape(16.dp), spotColor = ZenithNeonGreen)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsGolf,
                        contentDescription = "Swing",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isBallInFlight) "BALL IN FLIGHT..." else "TAP TO SWING (APEX METER)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Quality feedback text
            AnimatedVisibility(visible = uiState.apexQualityText != null && !uiState.isApexTimingActive) {
                val quality = uiState.apexQualityText ?: ""
                val color = if (quality.contains("PERFECT")) ZenithNeonGreen else if (quality.contains("GREAT")) ZenithCyan else ZenithGold
                Text(
                    text = quality,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = color,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                )
            }
        }
    }
}

@Composable
fun ClubSelectorRow(
    selectedClub: ClubType,
    onSelectClub: (ClubType) -> Unit,
    disabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        ClubType.values().forEach { club ->
            val isSelected = club == selectedClub
            val bg = if (isSelected) ZenithNeonGreen else ZenithSurfaceElevated
            val textCol = if (isSelected) Color(0xFF003816) else ZenithTextPrimary
            val borderCol = if (isSelected) ZenithNeonGreen else ZenithSurfaceBorder

            Surface(
                shape = RoundedCornerShape(10.dp),
                color = bg,
                border = BorderStroke(1.dp, borderCol),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .clickable(enabled = !disabled) { onSelectClub(club) }
                    .testTag("club_btn_${club.shortName.lowercase()}")
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = club.shortName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = textCol
                    )
                    Text(
                        text = "${club.baseDistanceYds.toInt()}y",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF003816).copy(alpha = 0.8f) else ZenithTextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun ApexTimingBar(
    meterPosition: Float, // -1.0f to +1.0f
    onLockImpact: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "TAP TO LOCK APEX IMPACT!",
            fontSize = 12.sp,
            fontWeight = FontWeight.Black,
            color = ZenithCyan,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))

        // Oscillating Meter Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .background(ZenithSurfaceElevated, RoundedCornerShape(12.dp))
                .border(1.5.dp, ZenithSurfaceBorder, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .clickable { onLockImpact() }
                .testTag("apex_timing_meter"),
            contentAlignment = Alignment.Center
        ) {
            // Background zones: Hook (left red/gold), Apex Zone (center bright green), Slice (right red/gold)
            Row(modifier = Modifier.fillMaxWidth().height(44.dp)) {
                // Left Hook zone
                Box(modifier = Modifier.weight(0.35f).height(44.dp).background(ZenithGold.copy(alpha = 0.25f)))
                // Center Apex sweet spot
                Box(
                    modifier = Modifier
                        .weight(0.30f)
                        .height(44.dp)
                        .background(ZenithNeonGreen.copy(alpha = 0.5f))
                        .border(1.dp, ZenithNeonGreen)
                ) {
                    Text(
                        text = "APEX",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = ZenithNeonGreen,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                // Right Slice zone
                Box(modifier = Modifier.weight(0.35f).height(44.dp).background(ZenithGold.copy(alpha = 0.25f)))
            }

            // Moving needle scaled dynamically to available meter width
            val normalizedX = ((meterPosition + 1.0f) / 2.0f).coerceIn(0f, 1f)
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .padding(horizontal = 4.dp)
            ) {
                val trackSpan = (maxWidth - 6.dp).coerceAtLeast(1.dp)
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = trackSpan * normalizedX)
                        .width(6.dp)
                        .height(38.dp)
                        .background(Color.White, RoundedCornerShape(3.dp))
                        .border(1.dp, ZenithCyan, RoundedCornerShape(3.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Big Lock Button
        Button(
            onClick = onLockImpact,
            colors = ButtonDefaults.buttonColors(containerColor = ZenithCyan),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("lock_apex_impact_button")
        ) {
            Text(
                text = "HIT NOW!",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF00363D)
            )
        }
    }
}

@Composable
private fun SpinChipButton(
    label: String,
    isSelected: Boolean,
    activeColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isSelected) activeColor.copy(alpha = 0.22f) else ZenithSurfaceElevated,
        border = BorderStroke(1.dp, if (isSelected) activeColor else ZenithSurfaceBorder),
        modifier = Modifier
            .clickable(enabled = enabled) { onClick() }
            .testTag(tag)
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = if (isSelected) activeColor else ZenithTextSecondary,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun BorderStroke(width: androidx.compose.ui.unit.Dp, color: Color) =
    androidx.compose.foundation.BorderStroke(width, color)
