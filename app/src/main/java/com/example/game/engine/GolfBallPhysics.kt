package com.example.game.engine

import androidx.compose.ui.geometry.Offset
import com.example.game.model.ClubType
import com.example.game.model.Hole
import com.example.game.model.PhysicsCollisionPoint
import com.example.game.model.ShotAim
import com.example.game.model.SurfaceType
import com.example.game.model.WeatherType
import com.example.game.model.WindCondition
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Core aerodynamic golf ball physics solver (mirrors Unity 6 GolfBallPhysics.cs).
 * Integrates gravity, aerodynamic drag (Cd), 3-axis Magnus lift & curve (backspin, topspin, sidespin),
 * wind shear via GolfWindSystem, and ground collisions via GolfSurfacePhysics.
 */
object GolfBallPhysics {

    const val GRAVITY = 9.81f
    private const val BALL_MASS_KG = 0.04593f
    private const val BALL_RADIUS_M = 0.021335f
    private const val AIR_DENSITY_KG_M3 = 1.225f
    private const val CROSS_SECTION_AREA_M2 = (Math.PI * BALL_RADIUS_M * BALL_RADIUS_M).toFloat()

    data class AerodynamicForces(
        val dragForceNewton: Float,
        val magnusForceNewton: Float,
        val liftAccelZ: Float,
        val lateralAccelX: Float,
        val lateralAccelY: Float
    )

    /**
     * Compute real-world aerodynamic drag force (N) and 3-axis Magnus lift/curve force (N)
     * for telemetry debug visualization and airborne step integration.
     */
    fun computeAerodynamicForces(
        vx: Float,
        vy: Float,
        vz: Float,
        club: ClubType,
        aim: ShotAim,
        wind: WindCondition,
        altitudeZ: Float
    ): AerodynamicForces {
        val relVx = vx - wind.vectorX
        val relVy = vy - wind.vectorY
        val relSpeed = sqrt(relVx * relVx + relVy * relVy + vz * vz).coerceAtLeast(0.1f)

        // Convert simulation velocity to m/s equivalent for Newton force telemetry
        val speedMps = relSpeed * 1.15f
        val spinMultiplier = 1.0f + (aim.verticalSpin * 0.45f)
        val totalSpinRpm = (club.baseBackspinRpm * spinMultiplier).coerceAtLeast(200f)
        val omegaRadPerSec = totalSpinRpm * (2f * Math.PI.toFloat() / 60f)
        val spinRatio = (omegaRadPerSec * BALL_RADIUS_M) / speedMps.coerceAtLeast(1f)

        // Dimpled golf ball drag coefficient Cd increases slightly with spin ratio
        val cd = 0.22f + 0.14f * spinRatio.coerceIn(0f, 0.6f)
        val dragForceN = 0.5f * AIR_DENSITY_KG_M3 * CROSS_SECTION_AREA_M2 * cd * speedMps * speedMps

        // Magnus lift coefficient Cl from backspin/topspin + sidespin
        val cl = (0.14f + 0.38f * spinRatio).coerceIn(0.05f, 0.42f)
        val magnusForceN = 0.5f * AIR_DENSITY_KG_M3 * CROSS_SECTION_AREA_M2 * cl * speedMps * speedMps

        // Vertical Magnus lift modifier: Backspin (+verticalSpin) adds upward lift; Topspin (-verticalSpin) pushes ball down faster
        val liftAccelZ = aim.verticalSpin * 2.8f

        // Lateral Magnus curve (Sidespin Hook/Draw vs Slice/Fade) perpendicular to horizontal velocity
        val perpAngle = atan2(vy.toDouble(), vx.toDouble()) + Math.PI / 2.0
        val spinForce = aim.curveSpin * 6.5f
        val lateralAccelX = (cos(perpAngle) * spinForce).toFloat()
        val lateralAccelY = (sin(perpAngle) * spinForce).toFloat()

        return AerodynamicForces(
            dragForceNewton = dragForceN,
            magnusForceNewton = magnusForceN,
            liftAccelZ = liftAccelZ,
            lateralAccelX = lateralAccelX,
            lateralAccelY = lateralAccelY
        )
    }

    /**
     * Run a full synchronous trajectory simulation and collect all waypoints and ground collision events.
     * Used by debug tools and unit test suites to inspect complete shot trajectories and bounce data.
     */
    fun simulateFullShotWithCollisions(
        startPos: Offset,
        club: ClubType,
        aim: ShotAim,
        wind: WindCondition,
        hole: Hole,
        weather: WeatherType = WeatherType.SUNNY,
        powerStatBonus: Float = 0f,
        dt: Float = 0.04f,
        maxSteps: Int = 500
    ): SimulatedShotReport {
        val startLie = GolfPhysicsEngine.getSurfaceAt(startPos, hole)
        val launch = GolfClubController.calculateClubLaunch(
            club = club,
            aim = aim,
            currentLie = startLie,
            powerStatBonus = powerStatBonus,
            deterministicDeviationSign = 1f
        )

        var pos = startPos
        var z = 0f
        var vx = launch.vx
        var vy = launch.vy
        var vz = launch.vz
        var maxApex = 0f
        var carryYds = 0f
        var peakDragN = 0f
        var peakMagnusN = 0f
        var currentSpinRpm = launch.backspinRpm
        var bounceCount = 0

        val flightPoints = mutableListOf(startPos)
        val collisions = mutableListOf<PhysicsCollisionPoint>()

        for (stepIdx in 0 until maxSteps) {
            if (z > 0.01f || (stepIdx == 0 && !club.isPutter)) {
                val aero = computeAerodynamicForces(vx, vy, vz, club, aim, wind, z)
                if (aero.dragForceNewton > peakDragN) peakDragN = aero.dragForceNewton
                if (aero.magnusForceNewton > peakMagnusN) peakMagnusN = aero.magnusForceNewton
            }

            val wasAirborne = z > 0.01f || (stepIdx == 0 && !club.isPutter)
            val preImpactSpeed = sqrt(vx * vx + vy * vy + vz * vz)

            val step = GolfPhysicsEngine.stepPhysics(
                currentPos = pos,
                altitudeZ = z,
                vx = vx,
                vy = vy,
                vz = vz,
                club = club,
                aim = aim,
                wind = wind,
                hole = hole,
                weather = weather,
                dt = dt
            )

            // Detect ground impact / bounce transition
            if (wasAirborne && step.newZ == 0f && !club.isPutter) {
                bounceCount++
                val postImpactSpeed = sqrt(step.newVx * step.newVx + step.newVy * step.newVy + step.newVz * step.newVz)
                currentSpinRpm = (currentSpinRpm * 0.55f).toInt()
                collisions.add(
                    PhysicsCollisionPoint(
                        position = step.newPos,
                        surface = step.lie,
                        impactSpeedMps = preImpactSpeed,
                        reboundSpeedMps = postImpactSpeed,
                        bounceNumber = bounceCount,
                        spinRpmAfterImpact = currentSpinRpm
                    )
                )
                if (carryYds == 0f) {
                    carryYds = (step.newPos - startPos).getDistance()
                }
            }

            pos = step.newPos
            z = step.newZ
            vx = step.newVx
            vy = step.newVy
            vz = step.newVz
            flightPoints.add(pos)

            if (z > maxApex) maxApex = z
            if (step.isResting) {
                break
            }
        }

        val totalDistance = (pos - startPos).getDistance()
        val finalCarry = if (carryYds > 0f) carryYds else totalDistance
        val rollYds = (totalDistance - finalCarry).coerceAtLeast(0f)

        return SimulatedShotReport(
            launchTelemetry = launch,
            flightPoints = flightPoints,
            collisionEvents = collisions,
            carryDistanceYds = finalCarry,
            rollDistanceYds = rollYds,
            totalDistanceYds = totalDistance,
            apexAltitudeYds = maxApex,
            peakDragForceN = peakDragN,
            peakMagnusForceN = peakMagnusN,
            endLie = GolfPhysicsEngine.getSurfaceAt(pos, hole),
            endPosition = pos
        )
    }

    data class SimulatedShotReport(
        val launchTelemetry: GolfClubController.ClubLaunchTelemetry,
        val flightPoints: List<Offset>,
        val collisionEvents: List<PhysicsCollisionPoint>,
        val carryDistanceYds: Float,
        val rollDistanceYds: Float,
        val totalDistanceYds: Float,
        val apexAltitudeYds: Float,
        val peakDragForceN: Float,
        val peakMagnusForceN: Float,
        val endLie: SurfaceType,
        val endPosition: Offset
    )
}
