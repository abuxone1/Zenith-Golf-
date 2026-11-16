package com.example.game.engine

import com.example.game.model.ClubType
import com.example.game.model.ShotAim
import com.example.game.model.SurfaceType
import kotlin.math.cos
import kotlin.math.sin

/**
 * Modular club ballistics & shot launch calculator (mirrors Unity 6 GolfClubController.cs).
 * Computes launch velocity vectors, ball speed MPH, dynamic launch angle, and 3-axis spin RPM
 * (backspin, topspin, and sidespin) based on club-specific smash factor, loft, lie, and swing timing.
 */
object GolfClubController {

    data class ClubLaunchTelemetry(
        val vx: Float,
        val vy: Float,
        val vz: Float,
        val ballSpeedMph: Float,
        val dynamicLaunchAngleDeg: Float,
        val backspinRpm: Int, // Positive = backspin, Negative/Reduced = topspin
        val sidespinRpm: Int, // Negative = Hook/Draw, Positive = Slice/Fade
        val smashEfficiency: Float
    )

    /**
     * Compute full 3D launch telemetry for a given club, shot aim, surface lie, and player power bonus.
     */
    fun calculateClubLaunch(
        club: ClubType,
        aim: ShotAim,
        currentLie: SurfaceType,
        powerStatBonus: Float = 0f,
        deterministicDeviationSign: Float? = null
    ): ClubLaunchTelemetry {
        val lieDistanceMod = if (currentLie == SurfaceType.SAND_BUNKER && club == ClubType.SAND_WEDGE) {
            0.78f // Sand Wedge bounce sole glides through bunker sand more cleanly than long clubs
        } else {
            currentLie.distanceMultiplier
        }
        val smashPenaltyFromRough = when (currentLie) {
            SurfaceType.ROUGH -> if (club == ClubType.SAND_WEDGE || club == ClubType.PITCHING_WEDGE) 0.92f else 0.88f
            SurfaceType.SAND_BUNKER -> if (club == ClubType.SAND_WEDGE) 0.86f else 0.72f
            else -> 1.0f
        }

        // Apex timing efficiency: 1.0 = sweet spot smash factor, lower = off-center strike
        val timingQuality = aim.apexTimingAccuracy.coerceIn(0.4f, 1.0f)
        val smashEfficiency = (club.smashFactor * smashPenaltyFromRough * (0.88f + 0.12f * timingQuality))
        val effectivePower = (aim.powerRatio * (1f + powerStatBonus) * lieDistanceMod).coerceIn(0.1f, 1.3f)

        // Ball Speed in MPH = Clubhead Speed * Power * (SmashEfficiency / NominalSmash)
        val ballSpeedMph = club.clubheadSpeedMph * effectivePower * (smashEfficiency / club.smashFactor)

        // Dynamic Launch Angle: adjusted by verticalSpin (-1.0 topspin lowers launch by 2.5 deg, +1.0 backspin raises by 2.5 deg)
        val spinAngleOffset = if (club.isPutter) 0f else (aim.verticalSpin * 2.5f)
        val dynamicLaunchAngleDeg = if (club.isPutter) 0f else (club.launchAngleDeg + spinAngleOffset).coerceIn(5f, 56f)

        // Face-to-path angle deviation from mistimed swing reduced by club MOI forgiveness
        val sign = deterministicDeviationSign ?: if (aim.curveSpin < 0f) -1f else 1f
        val maxDeviation = (1f - club.forgivenessMoi) * 32f
        val apexAngleDeviationDeg = (1.0f - timingQuality) * maxDeviation * sign
        val totalHeadingDeg = aim.aimAngleDeg + apexAngleDeviationDeg

        val headingRad = Math.toRadians(totalHeadingDeg.toDouble())
        val launchRad = Math.toRadians(dynamicLaunchAngleDeg.toDouble())

        // Convert to course simulation velocity units (preserved scale so existing holes play identically)
        val nominalSpeed = (club.baseDistanceYds * 0.42f) * effectivePower
        val horizontalSpeed = (nominalSpeed * cos(launchRad)).toFloat()
        val vz = (nominalSpeed * sin(launchRad)).toFloat()

        val vx = (horizontalSpeed * sin(headingRad)).toFloat()
        val vy = (-horizontalSpeed * cos(headingRad)).toFloat()

        // Spin calculations (RPM)
        // Lie spin retention: Rough ("flier lie") and Sand reduce backspin generation
        val lieSpinModifier = when (currentLie) {
            SurfaceType.ROUGH -> 0.62f
            SurfaceType.SAND_BUNKER -> 0.55f
            else -> 1.0f
        }

        // Vertical Spin input (-1.0 Topspin to +1.0 Backspin) scales spin from -800 RPM (heavy topspin roll) up to +11,500 RPM (wedge bite)
        val verticalSpinMultiplier = 1.0f + (aim.verticalSpin * 0.55f)
        val computedBackspinRpm = if (club.isPutter) {
            // Putters impart slight forward topspin roll (~450 RPM) after initial skid
            450
        } else {
            (club.baseBackspinRpm * effectivePower * lieSpinModifier * verticalSpinMultiplier).toInt()
        }

        // Sidespin RPM: -3000 RPM (Draw/Hook) to +3000 RPM (Fade/Slice) plus mistimed face-to-path spin
        val intentionalSidespin = (aim.curveSpin * 2600f).toInt()
        val mistimedSidespin = ((1.0f - timingQuality) * 1400f * sign).toInt()
        val computedSidespinRpm = if (club.isPutter) 0 else (intentionalSidespin + mistimedSidespin)

        return ClubLaunchTelemetry(
            vx = vx,
            vy = vy,
            vz = vz,
            ballSpeedMph = ballSpeedMph,
            dynamicLaunchAngleDeg = dynamicLaunchAngleDeg,
            backspinRpm = computedBackspinRpm,
            sidespinRpm = computedSidespinRpm,
            smashEfficiency = smashEfficiency
        )
    }
}
