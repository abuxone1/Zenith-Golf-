package com.example.game.engine

import androidx.compose.ui.geometry.Offset
import com.example.game.model.WeatherType
import com.example.game.model.WindCondition
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

/**
 * Modular atmospheric wind & boundary-layer shear calculator (mirrors Unity 6 GolfWindSystem.cs).
 * Computes altitude-sheared wind velocity vectors, headwind/tailwind carry modifiers,
 * and crosswind lateral accelerations optimized for Android.
 */
object GolfWindSystem {

    private const val REFERENCE_ALTITUDE_YDS = 25.0f
    private const val SHEAR_EXPONENT_ALPHA = 0.22f // Power-law wind profile exponent over links turf

    data class WindComponents(
        val worldWindVx: Float,
        val worldWindVy: Float,
        val headwindMph: Float, // Positive = Tailwind, Negative = Headwind
        val crosswindMph: Float // Positive = Left-to-Right, Negative = Right-to-Left
    )

    /**
     * Calculate altitude-adjusted wind acceleration vector using atmospheric boundary-layer shear.
     * Higher trajectory shots (e.g., Pitching Wedge / 7-Iron) experience stronger upper-level winds
     * than low-penetrating stingers.
     */
    fun getAltitudeAdjustedWindVector(
        wind: WindCondition,
        altitudeZ: Float,
        weather: WeatherType = WeatherType.SUNNY
    ): Offset {
        if (wind.speedMph <= 0.05f) return Offset.Zero

        val weatherGustScale = when (weather) {
            WeatherType.WINDY_TEMPEST -> 1.28f
            WeatherType.RAIN_STORM -> 1.15f
            else -> 1.0f
        }

        val clampedAlt = altitudeZ.coerceAtLeast(1.0f)
        val shearMultiplier = (clampedAlt / REFERENCE_ALTITUDE_YDS).toDouble()
            .pow(SHEAR_EXPONENT_ALPHA.toDouble())
            .toFloat()
            .coerceIn(0.25f, 1.35f)

        val effectiveFactor = shearMultiplier * 0.6f * weatherGustScale
        return Offset(
            x = wind.vectorX * effectiveFactor,
            y = wind.vectorY * effectiveFactor
        )
    }

    /**
     * Decompose wind into Headwind/Tailwind and Crosswind components relative to the golfer's aim heading.
     */
    fun decomposeWindRelativeToHeading(
        wind: WindCondition,
        aimHeadingDeg: Float
    ): WindComponents {
        val windRad = Math.toRadians(wind.directionDeg.toDouble())
        val aimRad = Math.toRadians(aimHeadingDeg.toDouble())
        val relAngle = windRad - aimRad

        // Positive = Tailwind pushing with shot; Negative = Headwind pushing against shot
        val tailwindMph = (wind.speedMph * cos(relAngle)).toFloat()
        // Positive = Blowing Left-to-Right across fairway; Negative = Right-to-Left
        val crosswindMph = (wind.speedMph * sin(relAngle)).toFloat()

        return WindComponents(
            worldWindVx = wind.vectorX,
            worldWindVy = wind.vectorY,
            headwindMph = tailwindMph,
            crosswindMph = crosswindMph
        )
    }
}
