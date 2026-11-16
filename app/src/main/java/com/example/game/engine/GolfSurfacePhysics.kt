package com.example.game.engine

import androidx.compose.ui.geometry.Offset
import com.example.game.model.Hole
import com.example.game.model.SurfaceType
import com.example.game.model.WeatherType
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Modular terrain surface interaction, bounce restitution, spin check/rollout, and Stimpmeter
 * putting physics engine (mirrors Unity 6 GolfSurfacePhysics.cs).
 */
object GolfSurfacePhysics {

    const val CUP_RADIUS = 7.0f
    const val MAX_CUP_CAPTURE_SPEED = 18.0f

    data class BounceResolution(
        val newVx: Float,
        val newVy: Float,
        val newVz: Float,
        val updatedBackspinRpm: Int,
        val didBounce: Boolean,
        val impactSpeed: Float,
        val reboundSpeed: Float
    )

    data class GroundRollResolution(
        val newPos: Offset,
        val newVx: Float,
        val newVy: Float,
        val isResting: Boolean,
        val inHole: Boolean,
        val lie: SurfaceType
    )

    /**
     * Resolve ground impact bounce (COR), tangential turf drag, and backspin check / topspin rollout.
     */
    fun resolveGroundBounce(
        vx: Float,
        vy: Float,
        vz: Float,
        groundLie: SurfaceType,
        backspinRpm: Int,
        verticalSpinRatio: Float,
        weather: WeatherType = WeatherType.SUNNY
    ): BounceResolution {
        val impactSpeed = sqrt(vx * vx + vy * vy + vz * vz)

        // Water or Out of Bounds absorbs impact immediately
        if (groundLie == SurfaceType.WATER_HAZARD || groundLie == SurfaceType.OUT_OF_BOUNDS) {
            return BounceResolution(
                newVx = 0f,
                newVy = 0f,
                newVz = 0f,
                updatedBackspinRpm = 0,
                didBounce = false,
                impactSpeed = impactSpeed,
                reboundSpeed = 0f
            )
        }

        // Wet turf reduces vertical bounce COR
        val weatherBounceMod = if (weather == WeatherType.RAIN_STORM) 0.80f else 1.0f
        val effectiveBounceCor = groundLie.bounce * weatherBounceMod

        if (effectiveBounceCor > 0.1f && abs(vz) > 2.0f) {
            val reboundedVz = -vz * effectiveBounceCor

            // Spin check vs Topspin rollout modifier on horizontal velocity upon turf impact:
            // High backspin (e.g., PW > 7000 RPM on Green/Fairway) checks/bites into turf;
            // Topspin (verticalSpinRatio < 0) surges forward for extra rollout yardage.
            val spinRollModifier = when {
                groundLie == SurfaceType.GREEN && backspinRpm > 7500 -> 0.52f // Strong wedge bite on green
                groundLie == SurfaceType.GREEN && backspinRpm > 5000 -> 0.72f
                verticalSpinRatio < -0.2f -> (1.0f + abs(verticalSpinRatio) * 0.28f) // Topspin rollout boost
                verticalSpinRatio > 0.2f -> (1.0f - verticalSpinRatio * 0.22f)       // Backspin check
                else -> 1.0f
            }

            val baseHorizontalRetention = (1f - groundLie.drag * 3.5f).coerceAtLeast(0.1f)
            val finalHorizontalScale = (baseHorizontalRetention * spinRollModifier).coerceIn(0.08f, 0.96f)

            val newVx = vx * finalHorizontalScale
            val newVy = vy * finalHorizontalScale
            val reboundSpeed = sqrt(newVx * newVx + newVy * newVy + reboundedVz * reboundedVz)

            val retainedSpin = (backspinRpm * 0.55f).toInt()
            return BounceResolution(
                newVx = newVx,
                newVy = newVy,
                newVz = reboundedVz,
                updatedBackspinRpm = retainedSpin,
                didBounce = true,
                impactSpeed = impactSpeed,
                reboundSpeed = reboundSpeed
            )
        } else {
            return BounceResolution(
                newVx = vx,
                newVy = vy,
                newVz = 0f,
                updatedBackspinRpm = 0,
                didBounce = false,
                impactSpeed = impactSpeed,
                reboundSpeed = sqrt(vx * vx + vy * vy)
            )
        }
    }

    /**
     * Resolve continuous ground rolling & putting physics across Fairway, Rough, Sand, and Green
     * including green slope break vectors and cup capture / lip-out mechanics.
     */
    fun stepGroundRollAndPutting(
        currentPos: Offset,
        vx: Float,
        vy: Float,
        groundLie: SurfaceType,
        hole: Hole,
        weather: WeatherType,
        dt: Float
    ): GroundRollResolution {
        var curVx = vx
        var curVy = vy

        // 1. Putting Green topographical slope gravity acceleration
        if (groundLie == SurfaceType.GREEN) {
            curVx += hole.greenSlope.x * 0.8f * dt
            curVy += hole.greenSlope.y * 0.8f * dt
        }

        // 2. Surface rolling friction scaled by weather conditions
        val effectiveDrag = groundLie.drag * weather.turfDragMultiplier
        val frictionDecay = (1.0f - (effectiveDrag * 3.8f + 0.05f) * dt).coerceIn(0.0f, 0.99f)
        curVx *= frictionDecay
        curVy *= frictionDecay

        val nextX = currentPos.x + curVx * dt
        val nextY = currentPos.y + curVy * dt
        val nextPos = Offset(nextX, nextY)

        val speed = sqrt(curVx * curVx + curVy * curVy)

        // 3. Cup capture & lip-out check
        val distToPin = (nextPos - hole.pinPosition).getDistance()
        if (distToPin <= CUP_RADIUS) {
            if (speed < MAX_CUP_CAPTURE_SPEED) {
                return GroundRollResolution(
                    newPos = hole.pinPosition,
                    newVx = 0f,
                    newVy = 0f,
                    isResting = true,
                    inHole = true,
                    lie = SurfaceType.GREEN
                )
            } else {
                // High-speed lip-out deflection: ball skims cup rim and slows down
                curVx *= 0.45f
                curVy *= 0.45f
            }
        }

        val isResting = speed < 0.6f
        return GroundRollResolution(
            newPos = nextPos,
            newVx = if (isResting) 0f else curVx,
            newVy = if (isResting) 0f else curVy,
            isResting = isResting,
            inHole = false,
            lie = groundLie
        )
    }

    data class TreeCanopyCollisionResult(
        val hitTree: Boolean,
        val newVx: Float,
        val newVy: Float,
        val newVz: Float
    )

    /**
     * Resolve 3D tree canopy/trunk collision when an airborne ball passes through a corridor tree.
     */
    fun resolveTreeCanopyCollision(
        ballPos: Offset,
        altitudeZ: Float,
        vx: Float,
        vy: Float,
        vz: Float,
        hole: Hole
    ): TreeCanopyCollisionResult {
        if (altitudeZ !in 2.0f..22.0f) {
            return TreeCanopyCollisionResult(false, vx, vy, vz)
        }
        val canopyRadius = 8.5f
        for (treeCenter in hole.treeObstacles) {
            val delta = ballPos - treeCenter
            val dist = delta.getDistance()
            if (dist <= canopyRadius && dist > 0.01f) {
                val normX = delta.x / dist
                val normY = delta.y / dist
                val speed = sqrt(vx * vx + vy * vy)
                val deflectedVx = (vx * 0.35f) + (normX * speed * 0.22f)
                val deflectedVy = (vy * 0.35f) + (normY * speed * 0.22f)
                val deflectedVz = -abs(vz) * 0.45f - 2.0f
                return TreeCanopyCollisionResult(
                    hitTree = true,
                    newVx = deflectedVx,
                    newVy = deflectedVy,
                    newVz = deflectedVz
                )
            }
        }
        return TreeCanopyCollisionResult(false, vx, vy, vz)
    }
}
