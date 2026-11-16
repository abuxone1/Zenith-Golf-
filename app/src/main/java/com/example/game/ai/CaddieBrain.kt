package com.example.game.ai

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import com.example.game.model.ClubType
import com.example.game.model.Hole
import com.example.game.model.SurfaceType
import com.example.game.model.TournamentRival
import com.example.game.model.WindCondition
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sin

object CaddieBrain {

    data class Recommendation(
        val recommendedClub: ClubType,
        val suggestedPowerPercent: Int,
        val windOffsetDeg: Float,
        val tacticalTip: String,
        val riskLevel: String // "Safe", "Moderate", "High Risk / High Reward"
    )

    /**
     * Compute real-time smart club recommendation and tactical guidance
     */
    fun analyzeShot(
        ballPos: Offset,
        hole: Hole,
        lie: SurfaceType,
        wind: WindCondition
    ): Recommendation {
        val distToPinYds = (ballPos - hole.pinPosition).getDistance()
        val distToGreenCenter = (ballPos - hole.greenCenter).getDistance()

        // If on the putting green or very close (within 35 yards of green edge)
        if (lie == SurfaceType.GREEN || distToPinYds < 28f) {
            val slopeTip = when {
                hole.greenSlope.x > 0.1f -> "Green slopes to the right. Aim slightly left of the cup."
                hole.greenSlope.x < -0.1f -> "Green slopes to the left. Aim slightly right of the cup."
                else -> "Fairly flat green. Commit firmly to the center of the cup."
            }
            return Recommendation(
                recommendedClub = ClubType.PUTTER,
                suggestedPowerPercent = ((distToPinYds / ClubType.PUTTER.baseDistanceYds) * 100).toInt().coerceIn(15, 100),
                windOffsetDeg = 0f,
                tacticalTip = "$slopeTip Distance: ${distToPinYds.toInt()} yds.",
                riskLevel = "Safe"
            )
        }

        // Determine best club by distance adjusted for lie penalty
        val liePenalty = if (lie == SurfaceType.SAND_BUNKER) 0.75f else lie.distanceMultiplier
        val bestClub = when {
            lie == SurfaceType.SAND_BUNKER && distToPinYds <= 95f -> ClubType.SAND_WEDGE
            distToPinYds > 210f && lie != SurfaceType.SAND_BUNKER -> ClubType.DRIVER
            distToPinYds > 190f && lie != SurfaceType.SAND_BUNKER -> ClubType.WOOD_3
            distToPinYds > 165f && lie != SurfaceType.SAND_BUNKER -> ClubType.IRON_4
            distToPinYds > 138f -> ClubType.IRON_7
            distToPinYds > 115f -> ClubType.IRON_9
            distToPinYds > 82f -> ClubType.PITCHING_WEDGE
            else -> ClubType.SAND_WEDGE
        }

        val effectiveClubReach = bestClub.baseDistanceYds * liePenalty
        val suggestedPower = ((distToPinYds / effectiveClubReach) * 100).toInt().coerceIn(35, 100)

        // Calculate wind drift angle adjustment
        val shotVector = hole.pinPosition - ballPos
        val shotHeadingDeg = Math.toDegrees(atan2(shotVector.x.toDouble(), -shotVector.y.toDouble())).toFloat()
        val windAngleDiff = wind.directionDeg - shotHeadingDeg
        val crosswindMph = (wind.speedMph * sin(Math.toRadians(windAngleDiff.toDouble()))).toFloat()

        // Invert crosswind to get recommended aim compensation
        val windCompensationDeg = (-crosswindMph * (bestClub.launchAngleDeg / 40f) * 0.9f).coerceIn(-18f, 18f)

        val tip = when {
            lie == SurfaceType.SAND_BUNKER ->
                "Bunker shot! Heavy sand resistance (-50% carry). Open stance and accelerate through impact."
            lie == SurfaceType.ROUGH ->
                "Lying in deep rough. Expect reduced backspin and a lower launch trajectory."
            abs(windCompensationDeg) > 6f ->
                "Strong crosswind (${wind.speedMph.toInt()} mph). Counter with ${abs(windCompensationDeg).toInt()}° ${if (windCompensationDeg > 0) "Right" else "Left"} aim!"
            distToPinYds < 120f ->
                "Prime wedge distance! Pin hunt now; stick this within 5 feet of the flag."
            else ->
                "Clean fairway ahead. Gauge power carefully with your pullback swing!"
        }

        val risk = when {
            distToGreenCenter < 140f && hole.hazards.any { it.type == SurfaceType.WATER_HAZARD } -> "High Risk / High Reward"
            wind.speedMph > 18f -> "Moderate"
            else -> "Safe"
        }

        return Recommendation(
            recommendedClub = bestClub,
            suggestedPowerPercent = suggestedPower,
            windOffsetDeg = windCompensationDeg,
            tacticalTip = tip,
            riskLevel = risk
        )
    }

    suspend fun fetchGeminiCommentary(
        hole: Hole,
        currentScore: Int,
        distanceToPin: Float,
        wind: WindCondition,
        lie: SurfaceType
    ): String = withContext(Dispatchers.IO) {
        try {
            val prompt = """
                You are a charismatic, sharp tour golf caddie named 'Zenith AI' in a futuristic arcade sports championship.
                Current context:
                - Hole #${hole.holeNumber}: Par ${hole.par}, Title '${hole.holeTitle}'
                - Player current stroke count on this hole: $currentScore
                - Distance to pin: ${distanceToPin.toInt()} yards
                - Lie: ${lie.displayName}
                - Wind: ${wind.speedMph.toInt()} mph at ${wind.directionDeg.toInt()} degrees.
                Give 1-2 punchy, strategic, motivating sentences of advice for the next swing. No greetings, just direct caddie advice.
            """.trimIndent()

            val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel("gemini-3-flash-preview")
            val response = model.generateContent(prompt)
            val text = response.text
            if (!text.isNullOrBlank()) {
                return@withContext text.trim()
            }
        } catch (_: Exception) {
            // Graceful fallback to heuristic advice
        }

        return@withContext analyzeShot(hole.teePosition, hole, lie, wind).tacticalTip
    }

    fun createDefaultRivals(): List<TournamentRival> {
        return listOf(
            TournamentRival(
                id = "rival_1",
                name = "Apex Archer",
                title = "Long Drive Champion",
                avatarColor = Color(0xFFFF5252),
                skillRating = 94,
                scores = mutableListOf(3, 3, 4, 4, 3, 4),
                currentTotalScore = 21
            ),
            TournamentRival(
                id = "rival_2",
                name = "Nova Vance",
                title = "Iron Precisionist",
                avatarColor = Color(0xFF00E5FF),
                skillRating = 91,
                scores = mutableListOf(4, 2, 5, 4, 3, 4),
                currentTotalScore = 22
            ),
            TournamentRival(
                id = "rival_3",
                name = "Cyber Woods",
                title = "Zenith Tour Veteran",
                avatarColor = Color(0xFFFFD600),
                skillRating = 89,
                scores = mutableListOf(4, 3, 5, 3, 4, 4),
                currentTotalScore = 23
            ),
            TournamentRival(
                id = "rival_4",
                name = "Zephyr Zane",
                title = "Wind Whisperer",
                avatarColor = Color(0xFFB388FF),
                skillRating = 86,
                scores = mutableListOf(5, 3, 5, 4, 3, 5),
                currentTotalScore = 25
            )
        )
    }

    fun simulateRivalScore(
        rival: TournamentRival,
        hole: Hole,
        difficulty: com.example.game.model.AiDifficultyTier = com.example.game.model.AiDifficultyTier.PRO
    ): Int {
        val par = hole.par
        val rand = Math.random()
        val birdieChance = when (difficulty) {
            com.example.game.model.AiDifficultyTier.ROOKIE -> (rival.skillRating / 100f * 0.15f)
            com.example.game.model.AiDifficultyTier.PRO -> (rival.skillRating / 100f * 0.35f)
            com.example.game.model.AiDifficultyTier.APEX_MASTER -> (rival.skillRating / 100f * 0.62f)
        }
        val parThreshold = when (difficulty) {
            com.example.game.model.AiDifficultyTier.ROOKIE -> 0.55f
            com.example.game.model.AiDifficultyTier.PRO -> 0.80f
            com.example.game.model.AiDifficultyTier.APEX_MASTER -> 0.94f
        }
        val baseScore = when {
            rand < birdieChance -> par - 1
            rand < parThreshold -> par
            else -> par + 1 + (if (difficulty == com.example.game.model.AiDifficultyTier.ROOKIE) 1 else 0)
        }
        return baseScore.coerceAtLeast(1)
    }
}
