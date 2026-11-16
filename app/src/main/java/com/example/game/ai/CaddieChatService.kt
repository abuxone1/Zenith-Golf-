package com.example.game.ai

import com.example.game.model.HoleStatistics
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    CADDIE
}

enum class AiModelChoice(val modelId: String, val label: String) {
    FLASH("gemini-3-flash-preview", "Zenith Flash (Fast)"),
    PRO("gemini-3.1-pro-preview", "Zenith Pro (Deep Tactical)")
}

object CaddieChatService {

    private const val SYSTEM_INSTRUCTION = """
You are 'Zenith AI Caddie', an elite, charismatic, highly knowledgeable golf caddie and sports strategist in the futuristic arcade game 'ZENITH GOLF'.

Your personality:
- Confident, sharp, supportive, articulate, and deeply immersed in tournament golf.
- You treat the user as a championship tour contender.
- You know golf mechanics thoroughly: wind vectors, surface drag (fairway 0.05, rough 0.25, sand 0.60, green 0.02), club trajectories (Driver, 3-Wood, 7-Iron, Pitching Wedge, Putter), hook/slice spin shaping, and drag-and-release swing power control.
- You know the courses of Zenith Tour: Zenith Dunes, Island Apex (Hole 2 Par 3), Falcon Dogleg (Hole 3 Par 5), Whispering Pines (Hole 4 Par 4), Cyber Canyon (Hole 5 Par 3), Zenith Colosseum (Hole 6 Par 4).

Guidelines for your responses:
- Keep answers practical, punchy, and engaging (1-3 paragraphs maximum).
- Give concrete yardages, club recommendations, aim angles, and spin tips when asked about shots or holes.
- Celebrate their great shots and offer tactical bounce-back advice after bogeys or water penalties.
- You are not a generic assistant; you are their dedicated tour caddie!
"""

    suspend fun sendMessage(
        history: List<ChatMessage>,
        userMessage: String,
        modelChoice: AiModelChoice = AiModelChoice.FLASH,
        currentHoleStats: HoleStatistics? = null
    ): String = withContext(Dispatchers.IO) {
        val enrichedPrompt = if (currentHoleStats != null) {
            """
[Live Game Context: Hole #${currentHoleStats.holeNumber} Par ${currentHoleStats.par}, ${currentHoleStats.totalHoleYardage}y. Current Stroke: ${currentHoleStats.strokesTaken + 1}, Dist to Pin: ${currentHoleStats.remainingDistanceToPin.toInt()}y, Lie: ${currentHoleStats.currentLie.displayName}, Score vs Par: ${if (currentHoleStats.scoreRelativeToPar >= 0) "+${currentHoleStats.scoreRelativeToPar}" else currentHoleStats.scoreRelativeToPar}]
Player asks: $userMessage
""".trimIndent()
        } else {
            userMessage
        }

        try {
            val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel(
                    modelName = modelChoice.modelId,
                    systemInstruction = content { text(SYSTEM_INSTRUCTION) },
                    generationConfig = generationConfig {
                        temperature = 0.7f
                        maxOutputTokens = 500
                    }
                )

            val recentHistory = history.takeLast(10).map { msg ->
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                content(role = role) { text(msg.text) }
            }

            val chat = model.startChat(history = recentHistory)
            val response = chat.sendMessage(enrichedPrompt)
            val reply = response.text
            if (!reply.isNullOrBlank()) {
                return@withContext reply.trim()
            }
        } catch (_: Exception) {
            // Fallback gracefully when offline or in local unit test environments
        }

        return@withContext generateHeuristicCaddieResponse(userMessage, currentHoleStats)
    }

    private fun generateHeuristicCaddieResponse(
        userMessage: String,
        stats: HoleStatistics?
    ): String {
        val lower = userMessage.lowercase()
        return when {
            lower.contains("island") || lower.contains("hole 2") ->
                "On Hole 2's Island Green (165 yds Par 3), wind is your primary hazard. Do not pin-hunt if the wind is blowing over 12 mph; take a 7-Iron or 3-Wood, aim for the fat center of the green, and secure your 2-putt par!"

            lower.contains("wind") || lower.contains("crosswind") ->
                "When addressing crosswinds: Driver and 3-Wood experience substantial aerodynamic drift due to longer flight times. Compensate by rotating your aim ~1° per 2 mph of crosswind. For instance, an 18 mph crosswind requires an 8° to 9° counter-aim!"

            lower.contains("draw") || lower.contains("fade") || lower.contains("curve") || lower.contains("hook") || lower.contains("slice") ->
                "Curve spin is your secret weapon around doglegs! A Draw (curve left) helps you hug left-turning fairways with extra forward roll. A Fade (curve right) lands softer on elevated greens with reduced rollout."

            lower.contains("putt") || lower.contains("break") || lower.contains("slope") ->
                "On our greens, check the contour slope vectors. If the break indicator points right, aim 1-2 cups to the left. Remember: green surface drag is minimal (0.02), so gentle power touches inside 15 yards are critical to avoid running past the cup."

            lower.contains("stat") || lower.contains("score") || lower.contains("gir") -> {
                if (stats != null) {
                    "Currently on Hole #${stats.holeNumber} (Par ${stats.par}). You've taken ${stats.strokesTaken} strokes with ${stats.remainingDistanceToPin.toInt()} yards left to the pin. Lie: ${stats.currentLie.displayName}. Fairway Hit: ${if (stats.fairwayHit) "Yes" else "No"}."
                } else {
                    "Your tour profile shows strong long-game performance. To climb the leaderboard against Apex Archer and Nova Vance, focus on Greens in Regulation (GIR)—landing safely on the putting surface in par-2 strokes is key!"
                }
            }

            else ->
                "Solid question, pro! Pull back smoothly on the drag-and-release swing control, align your aim line against the wind vector, and let's keep hunting birdies out here on the Zenith Tour."
        }
    }
}
