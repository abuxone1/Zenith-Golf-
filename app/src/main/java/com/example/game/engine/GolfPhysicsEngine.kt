package com.example.game.engine

import androidx.compose.ui.geometry.Offset
import com.example.game.model.ClubType
import com.example.game.model.Hole
import com.example.game.model.ShotAim
import com.example.game.model.SurfaceType
import com.example.game.model.WeatherType
import com.example.game.model.WindCondition
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

object GolfPhysicsEngine {

    const val CUP_RADIUS = 7.0f // Hole cup radius in course coordinate units
    const val GRAVITY = 9.81f // Gravity scale

    /**
     * Determine surface lie at any point on the hole
     */
    fun getSurfaceAt(point: Offset, hole: Hole): SurfaceType {
        // 1. Check Green
        val distToGreen = (point - hole.greenCenter).getDistance()
        if (distToGreen <= hole.greenRadius) {
            return SurfaceType.GREEN
        }

        // 2. Check Hazards (Water & Sand)
        for (hazard in hole.hazards) {
            if (hazard.contains(point)) {
                return hazard.type
            }
        }

        // 3. Check Fairway
        for (fairway in hole.fairways) {
            if (fairway.contains(point)) {
                return SurfaceType.FAIRWAY
            }
        }

        // 4. Check Tee Box proximity
        if ((point - hole.teePosition).getDistance() < 18f) {
            return SurfaceType.TEE
        }

        // 5. Default Rough
        return SurfaceType.ROUGH
    }

    /**
     * Compute simulated trajectory arc points for predictive aiming line
     */
    fun calculateAimTrajectory(
        start: Offset,
        club: ClubType,
        aim: ShotAim,
        wind: WindCondition,
        hole: Hole,
        clubPowerMultiplier: Float = 1.0f,
        windResistanceMultiplier: Float = 1.0f,
        weather: WeatherType = WeatherType.SUNNY,
        numPoints: Int = 30
    ): List<Offset> {
        val points = mutableListOf<Offset>()
        points.add(start)

        val lie = getSurfaceAt(start, hole)
        val lieMultiplier = lie.distanceMultiplier * weather.rollDecayMultiplier

        // Base yardage calculation with verticalSpin (backspin vs topspin rollout modifier) and hole elevation
        val verticalSpinDistMod = if (club.isPutter) 1.0f else (1.0f - aim.verticalSpin * 0.06f)
        val elevationCarryMod = if (club.isPutter) 1.0f else (1.0f - (hole.elevationDiffYds * 0.0035f)).coerceIn(0.88f, 1.12f)
        val effectiveYardage = club.baseDistanceYds * aim.powerRatio * clubPowerMultiplier * lieMultiplier * verticalSpinDistMod * elevationCarryMod

        // Aim angle in radians
        val headingRad = Math.toRadians(aim.aimAngleDeg.toDouble())

        // Wind factor based on club launch angle + verticalSpin (higher backspin apex = more wind drift)
        val effectiveLaunchDeg = (club.launchAngleDeg + aim.verticalSpin * 2.5f).coerceIn(5f, 56f)
        val windSensitivity = if (club.isPutter) 0.05f else (effectiveLaunchDeg / 45f) * windResistanceMultiplier
        val windDriftX = wind.vectorX * windSensitivity * (effectiveYardage / 100f) * 1.8f
        val windDriftY = wind.vectorY * windSensitivity * (effectiveYardage / 100f) * 1.8f

        // Spin curve (Hook/Slice)
        val spinCurveFactor = aim.curveSpin * (if (club.isPutter) 0.0f else 18f) * (effectiveYardage / 150f)

        // Generate curved trajectory points
        for (i in 1..numPoints) {
            val t = i.toFloat() / numPoints.toFloat()

            // Arc progress
            val straightX = (sin(headingRad) * effectiveYardage * t).toFloat()
            val straightY = (-cos(headingRad) * effectiveYardage * t).toFloat()

            // Spin parabolic deflection perpendicular to aim heading
            val perpAngle = headingRad + Math.PI / 2.0
            val curveX = (sin(perpAngle) * spinCurveFactor * (t * t)).toFloat()
            val curveY = (-cos(perpAngle) * spinCurveFactor * (t * t)).toFloat()

            // Wind drift increases over flight time (t^1.4)
            val appliedWindX = windDriftX * Math.pow(t.toDouble(), 1.4).toFloat()
            val appliedWindY = windDriftY * Math.pow(t.toDouble(), 1.4).toFloat()

            val ptX = start.x + straightX + curveX + appliedWindX
            val ptY = start.y + straightY + curveY + appliedWindY
            points.add(Offset(ptX, ptY))
        }

        return points
    }

    /**
     * Compute initial 3D launch velocity vector (vx, vy, vz) via GolfClubController
     */
    fun computeLaunchVelocity(
        club: ClubType,
        aim: ShotAim,
        currentLie: SurfaceType,
        powerStatBonus: Float = 0f
    ): Triple<Float, Float, Float> {
        val telemetry = GolfClubController.calculateClubLaunch(
            club = club,
            aim = aim,
            currentLie = currentLie,
            powerStatBonus = powerStatBonus
        )
        return Triple(telemetry.vx, telemetry.vy, telemetry.vz)
    }

    /**
     * Advance one physics step (dt = time delta in seconds) using modular GolfWindSystem,
     * GolfBallPhysics (drag & 3-axis Magnus effect), and GolfSurfacePhysics (COR bounce & putting).
     */
    fun stepPhysics(
        currentPos: Offset,
        altitudeZ: Float,
        vx: Float,
        vy: Float,
        vz: Float,
        club: ClubType,
        aim: ShotAim,
        wind: WindCondition,
        hole: Hole,
        weather: WeatherType = WeatherType.SUNNY,
        dt: Float = 0.05f
    ): StepResult {
        var x = currentPos.x
        var y = currentPos.y
        var z = altitudeZ
        var currentVx = vx
        var currentVy = vy
        var currentVz = vz

        if ((z > 0.01f || currentVz > 0.01f) && !club.isPutter) {
            // --- AIRBORNE FLIGHT SIMULATION ---
            // 1. Altitude-sheared wind effect via GolfWindSystem
            val windAccel = GolfWindSystem.getAltitudeAdjustedWindVector(wind, z, weather)
            currentVx += windAccel.x * dt
            currentVy += windAccel.y * dt

            // 2. Aerodynamic Magnus forces (Sidespin curve + Backspin/Topspin vertical lift)
            val aero = GolfBallPhysics.computeAerodynamicForces(
                vx = currentVx,
                vy = currentVy,
                vz = currentVz,
                club = club,
                aim = aim,
                wind = wind,
                altitudeZ = z
            )
            currentVx += aero.lateralAccelX * dt
            currentVy += aero.lateralAccelY * dt

            // 3. Gravity pulling down + vertical Magnus spin lift + hole elevation carry modifier
            // Uphill holes (+elevationDiffYds) increase effective gravity pull; downhill holes (-elevationDiffYds) extend hang time
            val elevationGravityScale = (1.0f + (hole.elevationDiffYds * 0.0035f)).coerceIn(0.88f, 1.12f)
            currentVz -= (GRAVITY * 2.2f * elevationGravityScale - aero.liftAccelZ) * dt

            // 4. Aerodynamic air drag decay (Reynolds drag coefficient scaled by spin)
            val dragDecayRate = 0.02f + (if (aim.verticalSpin > 0f) aim.verticalSpin * 0.006f else 0f)
            currentVx *= (1f - dragDecayRate * dt)
            currentVy *= (1f - dragDecayRate * dt)

            // Update position
            x += currentVx * dt
            y += currentVy * dt
            z += currentVz * dt

            // 5. Check 3D Tree Canopy / Trunk Collision along rough corridors
            val treeCollision = GolfSurfacePhysics.resolveTreeCanopyCollision(
                ballPos = Offset(x, y),
                altitudeZ = z,
                vx = currentVx,
                vy = currentVy,
                vz = currentVz,
                hole = hole
            )
            if (treeCollision.hitTree) {
                currentVx = treeCollision.newVx
                currentVy = treeCollision.newVy
                currentVz = treeCollision.newVz
            }

            // Check ground collision
            if (z <= 0f) {
                z = 0f
                val groundLie = getSurfaceAt(Offset(x, y), hole)
                val estBackspin = (club.baseBackspinRpm * (1f + aim.verticalSpin * 0.55f)).toInt()
                val bounceRes = GolfSurfacePhysics.resolveGroundBounce(
                    vx = currentVx,
                    vy = currentVy,
                    vz = currentVz,
                    groundLie = groundLie,
                    backspinRpm = estBackspin,
                    verticalSpinRatio = aim.verticalSpin,
                    weather = weather
                )
                currentVx = bounceRes.newVx
                currentVy = bounceRes.newVy
                currentVz = bounceRes.newVz
            }

            return StepResult(
                newPos = Offset(x, y),
                newZ = z,
                newVx = currentVx,
                newVy = currentVy,
                newVz = currentVz,
                isResting = false,
                isAirborne = z > 0f,
                inHole = false,
                lie = getSurfaceAt(Offset(x, y), hole)
            )

        } else {
            // --- GROUND ROLL / PUTTING DYNAMICS via GolfSurfacePhysics ---
            val groundLie = getSurfaceAt(Offset(x, y), hole)
            val rollRes = GolfSurfacePhysics.stepGroundRollAndPutting(
                currentPos = Offset(x, y),
                vx = currentVx,
                vy = currentVy,
                groundLie = groundLie,
                hole = hole,
                weather = weather,
                dt = dt
            )

            return StepResult(
                newPos = rollRes.newPos,
                newZ = if (rollRes.inHole) -2f else 0f,
                newVx = rollRes.newVx,
                newVy = rollRes.newVy,
                newVz = 0f,
                isResting = rollRes.isResting,
                isAirborne = false,
                inHole = rollRes.inHole,
                lie = rollRes.lie
            )
        }
    }

    data class StepResult(
        val newPos: Offset,
        val newZ: Float,
        val newVx: Float,
        val newVy: Float,
        val newVz: Float,
        val isResting: Boolean,
        val isAirborne: Boolean,
        val inHole: Boolean,
        val lie: SurfaceType
    )

    fun calculateYardage(p1: Offset, p2: Offset): Float {
        return (p1 - p2).getDistance()
    }
}
