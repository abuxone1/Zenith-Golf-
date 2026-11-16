package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import com.example.game.model.CameraViewMode
import com.example.game.model.FairwayZone
import com.example.game.model.GolfBallSkin
import com.example.game.model.GraphicsQualityTier
import com.example.game.model.HazardZone
import com.example.game.model.Hole
import com.example.game.model.SurfaceType
import com.example.game.model.WeatherType
import com.example.game.viewmodel.GameUiState
import kotlin.math.atan2

data class CourseCameraTransform(
    val scale: Float,
    val offsetX: Float,
    val offsetY: Float
) {
    fun toScreen(pt: Offset): Offset = Offset(offsetX + pt.x * scale, offsetY + pt.y * scale)
    fun toCourse(screenPt: Offset): Offset = Offset((screenPt.x - offsetX) / scale, (screenPt.y - offsetY) / scale)

    companion object {
        fun compute(
            canvasW: Float,
            canvasH: Float,
            cameraMode: CameraViewMode,
            ballPos: Offset,
            pinPos: Offset,
            isBallInFlight: Boolean,
            trajectoryEnd: Offset?
        ): CourseCameraTransform {
            val isLandscape = canvasW > canvasH
            val baseScaleX = canvasW / 400f
            val baseScaleY = canvasH / 800f
            val baseScale = if (isLandscape) (baseScaleY * 1.18f) else minOf(baseScaleX, baseScaleY)
            val (zoom, targetFocus) = when (cameraMode) {
                CameraViewMode.LANDING_ZONE -> Pair(
                    1.75f,
                    if (isBallInFlight) ballPos else (trajectoryEnd ?: ballPos)
                )
                CameraViewMode.GREEN_APPROACH -> Pair(2.3f, pinPos)
                CameraViewMode.PERSPECTIVE_3D -> {
                    val followY = (ballPos.y * 0.78f + pinPos.y * 0.22f).coerceIn(160f, 640f)
                    Pair(1.52f, Offset(ballPos.x.coerceIn(110f, 290f), followY))
                }
                CameraViewMode.OVERVIEW -> {
                    if (isLandscape) {
                        val focusY = (ballPos.y * 0.62f + pinPos.y * 0.38f).coerceIn(220f, 580f)
                        Pair(1.15f, Offset(ballPos.x.coerceIn(130f, 270f), focusY))
                    } else {
                        Pair(1.0f, Offset(200f, 400f))
                    }
                }
            }
            val scale = baseScale * zoom
            val offsetX = canvasW / 2f - targetFocus.x * scale
            val offsetY = canvasH / 2f - targetFocus.y * scale
            return CourseCameraTransform(scale = scale, offsetX = offsetX, offsetY = offsetY)
        }
    }
}

@Composable
fun CourseCanvas(
    uiState: GameUiState,
    onAimAdjust: (angle: Float, power: Float) -> Unit,
    onTapCourse: (Offset) -> Unit = {},
    onSwingDragStart: (Offset) -> Unit = {},
    onSwingDragUpdate: (currentPos: Offset, ballScreenPos: Offset, maxPullPx: Float) -> Unit = { _, _, _ -> },
    onSwingDragRelease: () -> Unit = {},
    onSwingDragCancel: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hole = uiState.currentHole
    val ball = uiState.ballState
    val trajectory = uiState.trajectoryPreview
    val equippedBall = uiState.equippedBall
    val graphicsTier = uiState.playSettings.graphicsTier
    val isLowPower = graphicsTier == GraphicsQualityTier.LOW_BATTERY

    val latestUiState by rememberUpdatedState(uiState)
    val reusableTrajectoryPath = remember { Path() }
    val reusableFlagPath = remember { Path() }
    val bgBrush = remember {
        Brush.verticalGradient(
            colors = listOf(
                Color(0xFF0A2216),
                Color(0xFF143D22),
                Color(0xFF194B2A),
                Color(0xFF0E2B17)
            )
        )
    }
    val twilightBrush = remember {
        Brush.verticalGradient(colors = listOf(Color(0x333A0CA3), Color(0x2210002B)))
    }
    val goldenBrush = remember {
        Brush.verticalGradient(colors = listOf(Color(0x33FF6F00), Color(0x22FFB300), Color(0x11000000)))
    }
    val cyberBrush = remember {
        Brush.verticalGradient(colors = listOf(Color(0x4405051A), Color(0x3310002B)))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(
                uiState.gpsRangefinder.isEnabled,
                uiState.isBallInFlight,
                uiState.holeCompleted
            ) {
                fun currentTransform(): CourseCameraTransform {
                    val st = latestUiState
                    return CourseCameraTransform.compute(
                        canvasW = size.width.toFloat(),
                        canvasH = size.height.toFloat(),
                        cameraMode = st.cameraMode,
                        ballPos = Offset(st.ballState.x, st.ballState.y),
                        pinPos = st.currentHole.pinPosition,
                        isBallInFlight = st.isBallInFlight,
                        trajectoryEnd = st.trajectoryPreview.lastOrNull()
                    )
                }

                if (latestUiState.gpsRangefinder.isEnabled && !latestUiState.isBallInFlight) {
                    detectTapGestures { tapOffset ->
                        onTapCourse(currentTransform().toCourse(tapOffset))
                    }
                } else if (!latestUiState.isBallInFlight && !latestUiState.holeCompleted) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            onSwingDragStart(startOffset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val st = latestUiState
                            val ballScreenPos = currentTransform().toScreen(Offset(st.ballState.x, st.ballState.y))
                            val maxPullPx = (minOf(size.width, size.height) * 0.42f).coerceAtLeast(140f)
                            onSwingDragUpdate(change.position, ballScreenPos, maxPullPx)
                        },
                        onDragEnd = {
                            onSwingDragRelease()
                        },
                        onDragCancel = {
                            onSwingDragCancel()
                        }
                    )
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height
            val camTransform = CourseCameraTransform.compute(
                canvasW = canvasW,
                canvasH = canvasH,
                cameraMode = uiState.cameraMode,
                ballPos = Offset(ball.x, ball.y),
                pinPos = hole.pinPosition,
                isBallInFlight = uiState.isBallInFlight,
                trajectoryEnd = trajectory.lastOrNull()
            )
            val scale = camTransform.scale

            fun toScreen(pt: Offset): Offset = camTransform.toScreen(pt)

            // 1. Full-Screen 3D Course Environment Background (Sky Horizon + Deep 3D Links Turf)
            if (isLowPower) {
                drawRect(color = Color(0xFF123820), size = size)
            } else {
                drawRect(brush = bgBrush, size = size)
            }

            // 3D Sky & Mountain Horizon Band at top of full-screen landscape
            val horizonHeight = canvasH * 0.14f
            if (isLowPower) {
                drawRect(color = Color(0xFF0E2B1A), size = Size(canvasW, horizonHeight))
            } else {
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF081A28),
                            Color(0xFF103142),
                            Color(0xFF0E2B1A)
                        ),
                        startY = 0f,
                        endY = horizonHeight
                    ),
                    size = Size(canvasW, horizonHeight)
                )
            }

            // Skip subtle mowing stripes on battery-saver tier to minimize overdraw
            if (!isLowPower) {
                drawMowingStripes(this, size)
            }

            // 2. Draw Fairways
            for (fairway in hole.fairways) {
                drawFairwaySegment(this, fairway, ::toScreen, scale, isLowPower)
            }

            // 3. Draw Water & Sand Hazards
            for (hazard in hole.hazards) {
                drawHazardZone(this, hazard, ::toScreen, scale, isLowPower)
            }

            // 3b. Draw 3D Corridor Trees along Rough
            for (treePt in hole.treeObstacles) {
                val screenTree = toScreen(treePt)
                val canopyR = 8.5f * scale
                if (!isLowPower) {
                    drawCircle(
                        color = Color(0x44000000),
                        radius = canopyR * 1.05f,
                        center = screenTree + Offset(3.5f * scale, 4.5f * scale)
                    )
                }
                drawCircle(
                    color = Color(0xFF0B381B),
                    radius = canopyR,
                    center = screenTree
                )
                drawCircle(
                    color = Color(0xFF145228),
                    radius = canopyR * 0.72f,
                    center = screenTree - Offset(1.2f * scale, 1.2f * scale)
                )
                if (!isLowPower) {
                    drawCircle(
                        color = Color(0xFF1E7038),
                        radius = canopyR * 0.38f,
                        center = screenTree - Offset(2.0f * scale, 2.0f * scale)
                    )
                }
            }

            // 4. Draw Putting Green
            drawGreenZone(this, hole, ::toScreen, scale, uiState.playSettings.isPuttingGridVisible)

            // 5. Draw Tee Box
            val screenTee = toScreen(hole.teePosition)
            drawCircle(
                color = Color(0xFF2E7D32),
                radius = 16f * scale,
                center = screenTee
            )
            // Tee markers
            drawCircle(
                color = Color(0xFF00E676),
                radius = 3.5f * scale,
                center = screenTee + Offset(-9f * scale, 0f)
            )
            drawCircle(
                color = Color(0xFF00E676),
                radius = 3.5f * scale,
                center = screenTee + Offset(9f * scale, 0f)
            )

            // 6. Draw Shot History Lines
            if (uiState.shotHistory.size > 1) {
                for (i in 0 until uiState.shotHistory.size - 1) {
                    val p1 = toScreen(uiState.shotHistory[i])
                    val p2 = toScreen(uiState.shotHistory[i + 1])
                    drawLine(
                        color = Color(0x77FFFFFF),
                        start = p1,
                        end = p2,
                        strokeWidth = 2.5f * scale,
                        cap = StrokeCap.Round
                    )
                    drawCircle(
                        color = Color(0xAA00E5FF),
                        radius = 4f * scale,
                        center = p1
                    )
                }
            }

            // 7. Draw Predictive Trajectory Arc & Dynamic Aim Line from Ball (when not in flight)
            if (!uiState.isBallInFlight && trajectory.size > 1 && !(uiState.isDraggingSwing && uiState.isSwingInCancelZone)) {
                val screenFirst = toScreen(trajectory.first())
                val screenLast = toScreen(trajectory.last())
                val screenBallOrigin = toScreen(Offset(ball.x, ball.y))

                // Direct Aim Line from the ball
                val aimRad = Math.toRadians(uiState.aimAngleDeg.toDouble())
                val clubMaxCourseLength = uiState.selectedClub.maxDistanceYds * 0.72f * scale
                val dynamicAimLength = (uiState.aimPowerRatio.coerceIn(0.08f, 1.0f) * clubMaxCourseLength)
                val aimLineEnd = screenBallOrigin + Offset(
                    (Math.sin(aimRad) * dynamicAimLength).toFloat(),
                    (-Math.cos(aimRad) * dynamicAimLength).toFloat()
                )

                if (!isLowPower) {
                    drawLine(
                        color = Color(0x6600E676),
                        start = screenBallOrigin,
                        end = aimLineEnd,
                        strokeWidth = 7f * scale,
                        cap = StrokeCap.Round
                    )
                }
                drawLine(
                    color = Color(0xFF00E676),
                    start = screenBallOrigin,
                    end = aimLineEnd,
                    strokeWidth = 2.8f * scale,
                    cap = StrokeCap.Round
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.2f * scale,
                    center = aimLineEnd
                )

                // Populate pre-allocated Path without extra list allocation
                reusableTrajectoryPath.reset()
                reusableTrajectoryPath.moveTo(screenFirst.x, screenFirst.y)
                for (i in 1 until trajectory.size) {
                    val p = toScreen(trajectory[i])
                    reusableTrajectoryPath.lineTo(p.x, p.y)
                }

                if (!isLowPower) {
                    drawPath(
                        path = reusableTrajectoryPath,
                        color = Color(0x5500E5FF),
                        style = Stroke(width = 8f * scale, cap = StrokeCap.Round)
                    )
                }

                if (isLowPower) {
                    drawPath(
                        path = reusableTrajectoryPath,
                        color = Color(0xFF00E5FF),
                        style = Stroke(width = 3.2f * scale, cap = StrokeCap.Round)
                    )
                } else {
                    drawPath(
                        path = reusableTrajectoryPath,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF00E676),
                                Color(0xFF00E5FF),
                                Color(0xFFFFD600)
                            ),
                            start = screenFirst,
                            end = screenLast
                        ),
                        style = Stroke(width = 3.5f * scale, cap = StrokeCap.Round)
                    )
                }

                // Landing Target Indicator Ring at projected impact point
                drawCircle(
                    color = Color(0x88FFD600),
                    radius = 16f * scale,
                    center = screenLast,
                    style = Stroke(width = 2.5f * scale)
                )
                drawCircle(
                    color = Color(0xFFFFD600),
                    radius = 4f * scale,
                    center = screenLast
                )
            }

            // 7a. Draw Drag-and-Release Pullback Vector, Dead Zone & Ball Cancel Ring
            if (uiState.isDraggingSwing && !uiState.isBallInFlight) {
                val screenBallPos = toScreen(Offset(ball.x, ball.y))
                val pullVec = uiState.dragCurrentScreenOffset - uiState.dragStartScreenOffset
                val visualPullEnd = screenBallPos + pullVec
                val cancelColor = if (uiState.isSwingInCancelZone) Color(0xFFFF5252) else Color(0x55FFFFFF)

                // Cancel zone ring around the ball (dragging back to the ball cancels the swing)
                drawCircle(
                    color = cancelColor,
                    radius = com.example.game.viewmodel.GolfGameViewModel.BALL_CANCEL_RADIUS_PX,
                    center = screenBallPos,
                    style = Stroke(width = if (uiState.isSwingInCancelZone) 3.5f else 1.8f)
                )
                if (uiState.isSwingInCancelZone) {
                    drawCircle(
                        color = Color(0x33FF5252),
                        radius = com.example.game.viewmodel.GolfGameViewModel.BALL_CANCEL_RADIUS_PX,
                        center = screenBallPos
                    )
                }

                // Pullback tension line from ball opposite to target
                val pullColor = when {
                    uiState.isSwingInCancelZone -> Color(0xFFFF5252)
                    uiState.aimPowerRatio >= 0.90f -> Color(0xFFFFD600)
                    else -> Color(0xFF00E5FF)
                }
                drawLine(
                    color = pullColor.copy(alpha = 0.45f),
                    start = screenBallPos,
                    end = visualPullEnd,
                    strokeWidth = 8f,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = pullColor,
                    start = screenBallPos,
                    end = visualPullEnd,
                    strokeWidth = 3.2f,
                    cap = StrokeCap.Round
                )
                // Handle knob at pullback end
                drawCircle(
                    color = pullColor,
                    radius = 11f,
                    center = visualPullEnd
                )
                drawCircle(
                    color = Color.White,
                    radius = 5f,
                    center = visualPullEnd
                )
            }

            // 7b. Draw Target Shootout Concentric Rings (if in Shootout mode or Hole 100)
            if (uiState.isTargetShootoutMode || hole.id == 100) {
                val targetCenter = toScreen(Offset(200f, 220f))

                // Outer Ring: 50 pts (radius 24)
                drawCircle(
                    color = Color(0x3300E676),
                    radius = 24f * scale,
                    center = targetCenter
                )
                drawCircle(
                    color = Color(0xFF00E676),
                    radius = 24f * scale,
                    center = targetCenter,
                    style = Stroke(width = 2.5f * scale)
                )

                // Mid Ring: 150 pts (radius 16)
                drawCircle(
                    color = Color(0x3300E5FF),
                    radius = 16f * scale,
                    center = targetCenter
                )
                drawCircle(
                    color = Color(0xFF00E5FF),
                    radius = 16f * scale,
                    center = targetCenter,
                    style = Stroke(width = 2.5f * scale)
                )

                // Inner Ring: 300 pts (radius 9)
                drawCircle(
                    color = Color(0x44FF3B30),
                    radius = 9f * scale,
                    center = targetCenter
                )
                drawCircle(
                    color = Color(0xFFFF3B30),
                    radius = 9f * scale,
                    center = targetCenter,
                    style = Stroke(width = 3.0f * scale)
                )

                // Bullseye Center: 500 pts (radius 4)
                drawCircle(
                    color = Color(0xFFFFD700),
                    radius = 4f * scale,
                    center = targetCenter
                )
                drawCircle(
                    color = Color.White,
                    radius = 4.2f * scale,
                    center = targetCenter,
                    style = Stroke(width = 1.8f * scale)
                )
            }

            // 8. Draw Hole Cup and Pin Flag
            val screenPin = toScreen(hole.pinPosition)
            // Cup hole shadow & rim
            drawCircle(
                color = Color(0xFF101010),
                radius = 7f * scale,
                center = screenPin
            )
            drawCircle(
                color = Color(0xFFE8F5E9),
                radius = 7f * scale,
                center = screenPin,
                style = Stroke(width = 1.5f * scale)
            )
            // Pin flagpole
            val flagPoleTop = screenPin - Offset(0f, 32f * scale)
            drawLine(
                color = Color(0xFFF1F8F4),
                start = screenPin,
                end = flagPoleTop,
                strokeWidth = 2.5f * scale,
                cap = StrokeCap.Round
            )
            // Neon Flag Banner (reuses cached Path)
            reusableFlagPath.reset()
            reusableFlagPath.moveTo(flagPoleTop.x, flagPoleTop.y)
            reusableFlagPath.lineTo(flagPoleTop.x + 18f * scale, flagPoleTop.y + 6f * scale)
            reusableFlagPath.lineTo(flagPoleTop.x, flagPoleTop.y + 13f * scale)
            reusableFlagPath.close()
            drawPath(
                path = reusableFlagPath,
                color = Color(0xFFFF1744)
            )

            // 8b. Draw Player Coordinates & Golfer Stance
            if (!uiState.isBallInFlight && !uiState.holeCompleted) {
                val screenPlayer = toScreen(uiState.playerCoordinates.position)
                val playerRadius = 7.5f * scale

                // Player stance shadow
                drawCircle(
                    color = Color(0x66000000),
                    radius = playerRadius * 1.2f,
                    center = screenPlayer + Offset(2f * scale, 3f * scale)
                )

                // Golfer avatar body (Polo Kit Color)
                val poloColor = Color(uiState.golferProfile.poloColorHex)
                val visorColor = Color(uiState.golferProfile.visorColorHex)
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            poloColor,
                            poloColor.copy(alpha = 0.7f),
                            Color(0xFF0A140E)
                        ),
                        center = screenPlayer - Offset(2f * scale, 2f * scale),
                        radius = playerRadius
                    ),
                    radius = playerRadius,
                    center = screenPlayer
                )

                // Golfer cap / visor direction
                val facingRad = Math.toRadians(uiState.playerCoordinates.facingAngleDeg.toDouble())
                val visorEnd = screenPlayer + Offset(
                    (Math.sin(facingRad) * playerRadius * 1.15f).toFloat(),
                    (-Math.cos(facingRad) * playerRadius * 1.15f).toFloat()
                )
                drawLine(
                    color = visorColor,
                    start = screenPlayer,
                    end = visorEnd,
                    strokeWidth = 2.8f * scale,
                    cap = StrokeCap.Round
                )

                // Animated Club Shaft & Clubhead (rotates back during pullback and whips through on release!)
                val screenBallPos = toScreen(Offset(ball.x, ball.y))
                rotate(degrees = uiState.clubSwingAnimAngleDeg, pivot = screenPlayer) {
                    drawLine(
                        color = if (uiState.isClubStrikeAnimating) Color.White else Color(0xFFD6E4EA),
                        start = screenPlayer,
                        end = screenBallPos,
                        strokeWidth = (if (uiState.isClubStrikeAnimating) 3.0f else 2.0f) * scale,
                        cap = StrokeCap.Round
                    )
                    // Clubhead at end of shaft
                    drawCircle(
                        color = if (uiState.isClubStrikeAnimating) Color(0xFFFFD600) else Color(0xFF00E5FF),
                        radius = 3.0f * scale,
                        center = screenBallPos
                    )
                }

                // Impact strike shockwave flash right when finger releases
                if (uiState.isClubStrikeAnimating) {
                    drawCircle(
                        color = Color(0xAA00E676),
                        radius = 16f * scale,
                        center = screenBallPos,
                        style = Stroke(width = 3.0f * scale)
                    )
                    drawCircle(
                        color = Color(0x66FFD600),
                        radius = 24f * scale,
                        center = screenBallPos,
                        style = Stroke(width = 2.0f * scale)
                    )
                }
            }

            // 8c. Toptracer Flight Trail Ribbon & Spark Particles
            val flightPoints = uiState.ballTrajectory.actualFlightPoints
            if (flightPoints.size >= 2) {
                val tracerColor = Color(uiState.equippedTracer.colorHex)
                for (i in 0 until flightPoints.size - 1) {
                    val p1 = toScreen(flightPoints[i])
                    val p2 = toScreen(flightPoints[i + 1])
                    val progress = i.toFloat() / flightPoints.size.toFloat()
                    val alpha = (0.25f + progress * 0.75f).coerceIn(0.1f, 1.0f)
                    val strokeW = (2.0f + progress * 2.5f) * scale

                    // Tracer glow ribbon
                    drawLine(
                        color = tracerColor.copy(alpha = alpha * 0.4f),
                        start = p1,
                        end = p2,
                        strokeWidth = strokeW * 2.2f,
                        cap = StrokeCap.Round
                    )
                    // Tracer core line
                    drawLine(
                        color = Color.White.copy(alpha = alpha),
                        start = p1,
                        end = p2,
                        strokeWidth = strokeW * 0.8f,
                        cap = StrokeCap.Round
                    )

                    // Sparks along trail
                    if (i % 3 == 0) {
                        drawCircle(
                            color = tracerColor,
                            radius = 2.5f * scale,
                            center = p2
                        )
                    }
                }
            }

            // 8d. Physics Debug Gizmos (Trajectory Waypoint Nodes, Collision Bounce Rings & Wind Vector Field)
            if (uiState.isPhysicsDebugEnabled) {
                // 1. Wind Shear Vector Field Grid across course
                val windVec = Offset(uiState.wind.vectorX * 16f * scale, uiState.wind.vectorY * 16f * scale)
                for (gx in 60..340 step 90) {
                    for (gy in 140..660 step 130) {
                        val gridStart = toScreen(Offset(gx.toFloat(), gy.toFloat()))
                        val gridEnd = gridStart + windVec
                        drawLine(
                            color = Color(0x5500E5FF),
                            start = gridStart,
                            end = gridEnd,
                            strokeWidth = 1.6f * scale,
                            cap = StrokeCap.Round
                        )
                        drawCircle(
                            color = Color(0x9900E5FF),
                            radius = 2.2f * scale,
                            center = gridEnd
                        )
                    }
                }

                // 2. Predicted Trajectory Waypoint Nodes (every 4th point)
                if (trajectory.size > 2) {
                    for (idx in trajectory.indices step 4) {
                        val nodeScreen = toScreen(trajectory[idx])
                        drawCircle(
                            color = Color(0xFFFFD600),
                            radius = 3.2f * scale,
                            center = nodeScreen
                        )
                        drawCircle(
                            color = Color(0x88FFD600),
                            radius = 6.0f * scale,
                            center = nodeScreen,
                            style = Stroke(width = 1.2f * scale)
                        )
                    }
                }

                // 3. Ground Collision & Bounce Impact Markers
                for (collision in uiState.ballTrajectory.collisionEvents) {
                    val impactScreen = toScreen(collision.position)
                    val impactColor = when (collision.surface) {
                        SurfaceType.GREEN -> Color(0xFF00E676)
                        SurfaceType.FAIRWAY -> Color(0xFF00E5FF)
                        SurfaceType.SAND_BUNKER -> Color(0xFFFFD700)
                        SurfaceType.WATER_HAZARD, SurfaceType.OUT_OF_BOUNDS -> Color(0xFFFF1744)
                        else -> Color(0xFFFF9100)
                    }
                    // Outer collision shockwave ring
                    drawCircle(
                        color = impactColor.copy(alpha = 0.85f),
                        radius = (11f + collision.bounceNumber * 3f) * scale,
                        center = impactScreen,
                        style = Stroke(width = 2.2f * scale)
                    )
                    // Inner impact point
                    drawCircle(
                        color = impactColor,
                        radius = 4.5f * scale,
                        center = impactScreen
                    )
                }
            }

            // 8e. Draw Surface Ball Landing Effect (Fairway divot, Sand splash, Water ripple, or Cup sink burst)
            uiState.lastLandingEffect?.let { effect ->
                val effectScreen = toScreen(effect.position)
                val effectColor = Color(effect.colorHex)
                drawCircle(
                    color = effectColor.copy(alpha = 0.22f),
                    radius = 22f * scale,
                    center = effectScreen
                )
                drawCircle(
                    color = effectColor.copy(alpha = 0.75f),
                    radius = 15f * scale,
                    center = effectScreen,
                    style = Stroke(width = 2.2f * scale)
                )
                drawCircle(
                    color = effectColor,
                    radius = 9f * scale,
                    center = effectScreen,
                    style = Stroke(width = 1.6f * scale)
                )
                if (effect.surface == SurfaceType.SAND_BUNKER || effect.isCupSink) {
                    // Radial sand grains or gold starburst rays
                    for (ray in 0 until 8) {
                        val rad = Math.toRadians((ray * 45).toDouble())
                        val rStart = effectScreen + Offset(
                            (Math.cos(rad) * 8f * scale).toFloat(),
                            (Math.sin(rad) * 8f * scale).toFloat()
                        )
                        val rEnd = effectScreen + Offset(
                            (Math.cos(rad) * 19f * scale).toFloat(),
                            (Math.sin(rad) * 19f * scale).toFloat()
                        )
                        drawLine(
                            color = effectColor,
                            start = rStart,
                            end = rEnd,
                            strokeWidth = 2.0f * scale,
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // 9. Draw Ball (with Altitude Shadow and Glow)
            val screenBall = toScreen(Offset(ball.x, ball.y))
            val altitude = ball.z // yards/height

            if (altitude > 0.1f) {
                // Ball Shadow on Turf (displaced diagonally and scaled down)
                val shadowOffset = Offset(altitude * 0.4f * scale, altitude * 0.7f * scale)
                val shadowRadius = (5.5f * scale) * (1f - (altitude / 50f).coerceIn(0f, 0.6f))
                drawCircle(
                    color = Color(0x55000000),
                    radius = shadowRadius.coerceAtLeast(2f),
                    center = screenBall + shadowOffset
                )
            }

            // Ball Sphere with Altitude Scaling
            val ballSizeMultiplier = 1.0f + (altitude / 18f).coerceIn(0f, 1.2f)
            val ballRadius = 6.0f * scale * ballSizeMultiplier

            // Outer Glow from Equipped Skin
            drawCircle(
                color = equippedBall.glowColor.copy(alpha = 0.5f),
                radius = ballRadius * 1.6f,
                center = screenBall
            )

            // Inner Ball Core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        equippedBall.glowColor,
                        Color(0xFFB0BEC5)
                    ),
                    center = screenBall - Offset(ballRadius * 0.25f, ballRadius * 0.25f),
                    radius = ballRadius
                ),
                radius = ballRadius,
                center = screenBall
            )

            // 10. Dynamic Atmospheric Weather Overlays (particle count scaled by GraphicsQualityTier)
            val rainCount = when (graphicsTier) {
                GraphicsQualityTier.LOW_BATTERY -> 15
                GraphicsQualityTier.MID_BALANCED -> 30
                GraphicsQualityTier.FLAGSHIP_ULTRA -> 55
            }
            val windCount = when (graphicsTier) {
                GraphicsQualityTier.LOW_BATTERY -> 8
                GraphicsQualityTier.MID_BALANCED -> 16
                GraphicsQualityTier.FLAGSHIP_ULTRA -> 24
            }
            when (uiState.weather) {
                WeatherType.RAIN_STORM -> {
                    for (i in 0 until rainCount) {
                        val rx = ((i * 137.5f) % size.width)
                        val ry = ((i * 83.1f) % size.height)
                        drawLine(
                            color = Color(0x66B0BEC5),
                            start = Offset(rx, ry),
                            end = Offset(rx + 6f, ry + 16f),
                            strokeWidth = 1.5f
                        )
                    }
                    drawRect(
                        color = Color(0x220D1B2A),
                        size = size
                    )
                }
                WeatherType.TWILIGHT -> {
                    drawRect(brush = twilightBrush, size = size)
                }
                WeatherType.WINDY_TEMPEST -> {
                    val windRad = Math.toRadians(uiState.wind.directionDeg.toDouble())
                    val dirVector = Offset(
                        (Math.cos(windRad) * 45f).toFloat(),
                        (Math.sin(windRad) * 45f).toFloat()
                    )
                    for (i in 0 until windCount) {
                        val wx = ((i * 179.3f) % size.width)
                        val wy = ((i * 113.7f) % size.height)
                        drawLine(
                            color = Color(0x33E0F2FE),
                            start = Offset(wx, wy),
                            end = Offset(wx + dirVector.x, wy + dirVector.y),
                            strokeWidth = 1.5f,
                            cap = StrokeCap.Round
                        )
                    }
                }
                WeatherType.GOLDEN_HOUR -> {
                    drawRect(brush = goldenBrush, size = size)
                }
                WeatherType.MIDNIGHT_CYBER -> {
                    drawRect(brush = cyberBrush, size = size)
                }
                WeatherType.SUNNY -> Unit
            }
        }
    }
}

private fun drawMowingStripes(scope: DrawScope, size: Size) {
    val stripeHeight = 36f
    var y = 0f
    var alt = false
    while (y < size.height) {
        if (alt) {
            scope.drawRect(
                color = Color(0x08FFFFFF),
                topLeft = Offset(0f, y),
                size = Size(size.width, stripeHeight)
            )
        }
        y += stripeHeight
        alt = !alt
    }
}

private fun drawFairwaySegment(
    scope: DrawScope,
    fairway: FairwayZone,
    toScreen: (Offset) -> Offset,
    scale: Float,
    isLowPower: Boolean = false
) {
    val screenCenter = toScreen(fairway.center)
    val width = fairway.width * scale
    val height = fairway.height * scale

    scope.rotate(fairway.angleDeg, screenCenter) {
        if (!isLowPower) {
            drawRoundRect(
                color = Color(0xFF1B5E20),
                topLeft = screenCenter - Offset(width / 2f + 4f, height / 2f + 4f),
                size = Size(width + 8f, height + 8f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(width / 2f, width / 2f)
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF2E7D32),
                        Color(0xFF388E3C),
                        Color(0xFF2E7D32)
                    )
                ),
                topLeft = screenCenter - Offset(width / 2f, height / 2f),
                size = Size(width, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(width / 2f, width / 2f)
            )
        } else {
            drawRoundRect(
                color = Color(0xFF338537),
                topLeft = screenCenter - Offset(width / 2f, height / 2f),
                size = Size(width, height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(width / 2f, width / 2f)
            )
        }
    }
}

private fun drawHazardZone(
    scope: DrawScope,
    hazard: HazardZone,
    toScreen: (Offset) -> Offset,
    scale: Float,
    isLowPower: Boolean = false
) {
    val screenCenter = toScreen(hazard.center)
    val rx = hazard.radiusX * scale
    val ry = hazard.radiusY * scale

    when (hazard.type) {
        SurfaceType.WATER_HAZARD -> {
            if (!isLowPower) {
                scope.drawOval(
                    color = Color(0x4400E5FF),
                    topLeft = screenCenter - Offset(rx + 4f, ry + 4f),
                    size = Size((rx + 4f) * 2f, (ry + 4f) * 2f)
                )
            }
            if (isLowPower) {
                scope.drawOval(
                    color = Color(0xFF01579B),
                    topLeft = screenCenter - Offset(rx, ry),
                    size = Size(rx * 2f, ry * 2f)
                )
            } else {
                scope.drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF0288D1),
                            Color(0xFF01579B),
                            Color(0xFF003666)
                        ),
                        center = screenCenter,
                        radius = rx
                    ),
                    topLeft = screenCenter - Offset(rx, ry),
                    size = Size(rx * 2f, ry * 2f)
                )
            }
        }
        SurfaceType.SAND_BUNKER -> {
            if (!isLowPower) {
                scope.drawOval(
                    color = Color(0xFF8D6E63),
                    topLeft = screenCenter - Offset(rx + 2f, ry + 2f),
                    size = Size((rx + 2f) * 2f, (ry + 2f) * 2f)
                )
            }
            if (isLowPower) {
                scope.drawOval(
                    color = Color(0xFFE2B765),
                    topLeft = screenCenter - Offset(rx, ry),
                    size = Size(rx * 2f, ry * 2f)
                )
            } else {
                scope.drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFE082),
                            Color(0xFFE2B765),
                            Color(0xFFC79A46)
                        ),
                        center = screenCenter - Offset(rx * 0.2f, ry * 0.2f),
                        radius = rx
                    ),
                    topLeft = screenCenter - Offset(rx, ry),
                    size = Size(rx * 2f, ry * 2f)
                )
            }
        }
        else -> Unit
    }
}

private fun drawGreenZone(
    scope: DrawScope,
    hole: Hole,
    toScreen: (Offset) -> Offset,
    scale: Float,
    showPuttingGrid: Boolean = true
) {
    val center = toScreen(hole.greenCenter)
    val radius = hole.greenRadius * scale

    // Fringe perimeter
    scope.drawCircle(
        color = Color(0xFF2E7D32),
        radius = radius + 6f * scale,
        center = center
    )

    // Smooth Putting Green
    scope.drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(
                Color(0xFF4CAF50),
                Color(0xFF43A047),
                Color(0xFF388E3C)
            ),
            center = center,
            radius = radius
        ),
        radius = radius,
        center = center
    )

    // Concentric green contour rings
    scope.drawCircle(
        color = Color(0x33FFFFFF),
        radius = radius * 0.65f,
        center = center,
        style = Stroke(width = 1.2f * scale)
    )
    scope.drawCircle(
        color = Color(0x22FFFFFF),
        radius = radius * 0.35f,
        center = center,
        style = Stroke(width = 1.0f * scale)
    )

    if (showPuttingGrid) {
        // High-tech laser putting grid lines
        val gridStep = 12f * scale
        var gx = center.x - radius
        while (gx <= center.x + radius) {
            val dist = Math.abs(gx - center.x)
            if (dist < radius) {
                val halfH = Math.sqrt((radius * radius - dist * dist).toDouble()).toFloat()
                scope.drawLine(
                    color = Color(0x2200FFCC),
                    start = Offset(gx, center.y - halfH),
                    end = Offset(gx, center.y + halfH),
                    strokeWidth = 1f
                )
            }
            gx += gridStep
        }
        var gy = center.y - radius
        while (gy <= center.y + radius) {
            val dist = Math.abs(gy - center.y)
            if (dist < radius) {
                val halfW = Math.sqrt((radius * radius - dist * dist).toDouble()).toFloat()
                scope.drawLine(
                    color = Color(0x2200FFCC),
                    start = Offset(center.x - halfW, gy),
                    end = Offset(center.x + halfW, gy),
                    strokeWidth = 1f
                )
            }
            gy += gridStep
        }

        // Green slope break vectors
        val slope = hole.greenSlope
        if (slope.getDistance() > 0.05f) {
            val slopeLen = (slope.getDistance() * 25f * scale).coerceIn(8f, 22f)
            val slopeNorm = slope / slope.getDistance()
            val arrowVector = Offset(slopeNorm.x * slopeLen, slopeNorm.y * slopeLen)

            val arrowOffsets = listOf(
                Offset(0f, 0f),
                Offset(-radius * 0.4f, -radius * 0.2f),
                Offset(radius * 0.4f, -radius * 0.2f),
                Offset(-radius * 0.3f, radius * 0.35f),
                Offset(radius * 0.3f, radius * 0.35f)
            )

            for (ao in arrowOffsets) {
                val p1 = center + ao
                val p2 = p1 + arrowVector
                scope.drawLine(
                    color = Color(0x88FFD700),
                    start = p1,
                    end = p2,
                    strokeWidth = 1.8f * scale,
                    cap = StrokeCap.Round
                )
                scope.drawCircle(
                    color = Color(0xCCFFD700),
                    radius = 2.5f * scale,
                    center = p2
                )
            }
        }
    }
}
