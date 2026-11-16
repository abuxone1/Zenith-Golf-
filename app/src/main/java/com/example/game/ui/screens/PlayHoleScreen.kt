package com.example.game.ui.screens

import android.app.Activity
import android.content.pm.ActivityInfo
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.engine.GolfWindSystem
import com.example.game.model.CameraViewMode
import com.example.game.model.ClubType
import com.example.game.ui.CourseCanvas
import com.example.game.ui.components.BottomCenterDistanceBadge
import com.example.game.ui.components.BottomLeftClubSelector
import com.example.game.ui.components.DragReleasePowerBarHud
import com.example.game.ui.components.GolfPauseMenuOverlay
import com.example.game.ui.components.HoleStatisticsDialog
import com.example.game.ui.components.LandscapeTopBar
import com.example.game.ui.components.RotateDevicePortraitOverlay
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.ZenithCyan
import com.example.ui.theme.ZenithDarkBg
import com.example.ui.theme.ZenithGold
import com.example.ui.theme.ZenithNeonGreen
import com.example.ui.theme.ZenithTextSecondary

@Composable
fun PlayHoleScreen(
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
    onAskAiCaddie: () -> Unit,
    onOpenCaddieChat: () -> Unit = {},
    onCycleCamera: () -> Unit = {},
    onCycleWeather: () -> Unit = {},
    onToggleRangefinder: () -> Unit = {},
    onSetVerticalSpin: (Float) -> Unit = {},
    onTogglePhysicsDebug: () -> Unit = {},
    onSwingDragStart: (Offset) -> Unit = {},
    onSwingDragUpdate: (currentPos: Offset, ballScreenPos: Offset, maxPullPx: Float) -> Unit = { _, _, _ -> },
    onSwingDragRelease: () -> Unit = {},
    onSwingDragCancel: () -> Unit = {},
    onConfirmShot: () -> Unit = {},
    onPauseGame: () -> Unit = {},
    onResumeGame: () -> Unit = {},
    onRestartHole: () -> Unit = {},
    onSelectCameraMode: (CameraViewMode) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler {
        if (uiState.isGamePaused) {
            onResumeGame()
        } else {
            onPauseGame()
        }
    }

    val context = LocalContext.current
    val activity = context as? Activity

    // Request sensor landscape orientation while on the 3D course screen
    DisposableEffect(activity) {
        val previousOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            activity?.requestedOrientation = previousOrientation
        }
    }

    var showStatsDialog by remember { mutableStateOf(false) }
    var portraitBypassed by rememberSaveable { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(ZenithDarkBg)
            .testTag("play_hole_screen")
    ) {
        val isPortraitWindow = maxWidth < maxHeight

        // 1. Full-Screen 3D Course Canvas filling the whole background (with touch-anywhere drag-and-release swing control)
        CourseCanvas(
            uiState = uiState,
            onAimAdjust = onAimAdjust,
            onSwingDragStart = onSwingDragStart,
            onSwingDragUpdate = onSwingDragUpdate,
            onSwingDragRelease = onSwingDragRelease,
            onSwingDragCancel = onSwingDragCancel,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Top Bar (semi-transparent): Hole & Par on left, Stroke count in center, Wind direction & speed on right
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            LandscapeTopBar(
                uiState = uiState,
                onBack = onBack,
                onOpenStats = { showStatsDialog = true },
                onCycleCamera = onCycleCamera,
                onOpenCaddieChat = onOpenCaddieChat,
                onTogglePhysicsDebug = onTogglePhysicsDebug,
                onPauseGame = onPauseGame
            )

            // Optional Compact Telemetry / Match Play / Score Notification Sub-Banners (Aligned Top-Left away from center ball view)
            Row(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Live Physics & Collision Telemetry Debug Overlay (when DBG is toggled on)
                AnimatedVisibility(visible = uiState.isPhysicsDebugEnabled) {
                    PhysicsDebugOverlayCard(
                        uiState = uiState,
                        modifier = Modifier
                            .widthIn(max = 380.dp)
                            .testTag("physics_debug_overlay")
                    )
                }

                // Live Ball Landing & Score Notification Pill
                AnimatedVisibility(
                    visible = uiState.scoreNotificationBanner != null && !uiState.isBallInFlight,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    val bannerText = uiState.scoreNotificationBanner ?: ""
                    val bannerColor = uiState.lastLandingEffect?.let { Color(it.colorHex) } ?: ZenithNeonGreen
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xD907140D),
                        border = BorderStroke(1.dp, bannerColor),
                        modifier = Modifier.testTag("score_notification_banner")
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                            Text(
                                text = bannerText,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = bannerColor
                            )
                            uiState.lastLandingEffect?.subtitle?.let { sub ->
                                Text(
                                    text = sub,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ZenithTextSecondary
                                )
                            }
                        }
                    }
                }

                // 1v1 Match Play Duel Live Status Pill (when active)
                if (uiState.matchPlayState.isActive) {
                    val match = uiState.matchPlayState
                    val rivalName = match.rival?.name ?: "Tour Rival"
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xCC0A140E),
                        border = BorderStroke(1.dp, ZenithCyan)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "1v1 VS ${rivalName.uppercase()}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = ZenithCyan
                            )
                            Text(
                                text = "${match.matchScoreStatus} (H${match.currentMatchHole}/${match.totalMatchHoles})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = ZenithNeonGreen
                            )
                        }
                    }
                }
            }
        }

        // 3. In-flight Live Carry Telemetry Pill (Positioned under Top Bar on Left, never overlapping center ball view)
        AnimatedVisibility(
            visible = uiState.isBallInFlight,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(start = 16.dp, top = 74.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xCC0A140E),
                border = BorderStroke(1.5.dp, ZenithNeonGreen)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column {
                        Text(
                            text = "BALL CARRY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = ZenithNeonGreen,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "${uiState.ballState.distanceTraveled.toInt()} YDS",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                    if (uiState.ballState.z > 0.5f) {
                        Text(
                            text = "APEX ${uiState.ballState.z.toInt()}Y",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZenithCyan
                        )
                    }
                }
            }
        }

        // 4. Bottom-Left: Club Selector (Driver, Wood, Long/Short Irons, Wedges, Putter) as compact dropdown + buttons (>= 48.dp)
        BottomLeftClubSelector(
            selectedClub = uiState.selectedClub,
            aimAngleDeg = uiState.aimAngleDeg,
            disabled = uiState.isBallInFlight || uiState.holeCompleted || uiState.isGamePaused,
            onSelectClub = onSelectClub,
            onAdjustAngle = onAdjustAngle,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .navigationBarsPadding()
                .padding(start = 12.dp, bottom = 12.dp)
        )

        // 5. Bottom-Center: Distance to the Hole (anchored at bottom edge, away from screen center)
        BottomCenterDistanceBadge(
            uiState = uiState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 12.dp)
        )

        // 6. Right-Side: Live Drag-and-Release Power Bar + Shot Confirmation & Spin Controls
        DragReleasePowerBarHud(
            uiState = uiState,
            onCancelSwing = onSwingDragCancel,
            onMulligan = onMulligan,
            onConfirmShot = onConfirmShot,
            onStartApexTiming = onStartApexSwing,
            onSetPower = onSetPower,
            onSetCurveSpin = onSetSpin,
            onSetVerticalSpin = onSetVerticalSpin,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .navigationBarsPadding()
                .padding(end = 12.dp, top = 48.dp)
        )

        // 7. In-Game Live Hole Telemetry Dialog
        if (showStatsDialog) {
            HoleStatisticsDialog(
                stats = uiState.holeStatistics,
                onDismiss = { showStatsDialog = false }
            )
        }

        // 7b. Pause & Tactical Menu Overlay
        if (uiState.isGamePaused) {
            GolfPauseMenuOverlay(
                uiState = uiState,
                onResume = onResumeGame,
                onRestartHole = onRestartHole,
                onSelectCameraMode = onSelectCameraMode,
                onExitToClubhouse = onBack
            )
        }

        // 8. Portrait Orientation Guard ("Rotate your device" message when phone is held in portrait)
        if (isPortraitWindow && !portraitBypassed) {
            RotateDevicePortraitOverlay(
                onForceLandscape = {
                    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    portraitBypassed = true
                },
                onBack = onBack
            )
        }
    }
}

@Composable
private fun PhysicsDebugOverlayCard(
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val traj = uiState.ballTrajectory
    val windComp = GolfWindSystem.decomposeWindRelativeToHeading(
        wind = uiState.wind,
        aimHeadingDeg = uiState.aimAngleDeg
    )
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xD9060E09),
        border = BorderStroke(1.dp, ZenithGold),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PHYSICS TELEMETRY DEBUGGER",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = ZenithGold,
                    letterSpacing = 0.8.sp
                )
                Text(
                    text = "${uiState.selectedClub.shortName} • COR ${uiState.ballState.lie.bounce}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZenithNeonGreen
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Launch: ${traj.launchSpeedMph.toInt()} MPH @ ${traj.launchAngleDeg}° | Spin: ${traj.backspinRpm} RPM (Back) / ${traj.sidespinRpm} RPM (Side)",
                fontSize = 10.sp,
                color = Color.White
            )
            Text(
                text = "Wind Vector: Head/Tail ${"%.1f".format(windComp.headwindMph)} mph | Cross ${"%.1f".format(windComp.crosswindMph)} mph | Drag ${"%.2f".format(traj.dragForcePeakN)}N | Magnus ${"%.2f".format(traj.magnusForcePeakN)}N",
                fontSize = 10.sp,
                color = ZenithCyan
            )
            if (traj.collisionEvents.isNotEmpty()) {
                val firstImpact = traj.collisionEvents.first()
                Text(
                    text = "Collisions (${traj.collisionEvents.size}): #1 on ${firstImpact.surface.displayName} (${"%.1f".format(firstImpact.impactSpeedMps)} -> ${"%.1f".format(firstImpact.reboundSpeedMps)} m/s, ${firstImpact.spinRpmAfterImpact} RPM)",
                    fontSize = 10.sp,
                    color = ZenithNeonGreen
                )
            } else {
                Text(
                    text = "Collisions: Awaiting shot impact • Green Stimp 11.5 • Gravity 9.81 m/s²",
                    fontSize = 10.sp,
                    color = ZenithTextSecondary
                )
            }
        }
    }
}
