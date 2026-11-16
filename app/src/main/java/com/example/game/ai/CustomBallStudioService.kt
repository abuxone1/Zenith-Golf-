package com.example.game.ai

import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import com.example.game.model.GolfBallSkin
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.ResponseModality
import com.google.firebase.ai.type.asImageOrNull
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object CustomBallStudioService {

    data class GenerationResult(
        val success: Boolean,
        val createdBall: GolfBallSkin,
        val bitmap: Bitmap? = null,
        val message: String
    )

    suspend fun generateCustomBall(
        prompt: String,
        ballName: String
    ): GenerationResult = withContext(Dispatchers.IO) {
        val cleanName = if (ballName.isNotBlank()) ballName.trim() else "Custom AI Ball"
        val id = "custom_ball_${UUID.randomUUID().toString().take(6)}"

        val colorPalette = listOf(
            Color(0xFF00E676),
            Color(0xFF00E5FF),
            Color(0xFFFFD600),
            Color(0xFFFF1744),
            Color(0xFFE040FB),
            Color(0xFFFF6D00)
        )
        val selectedColor = colorPalette[Math.abs(cleanName.hashCode()) % colorPalette.size]

        try {
            val imagePrompt = "A glowing futuristic golf ball with high-tech texture: $prompt. Macro sports equipment photography, centered on solid dark background."

            val model = Firebase.ai(backend = GenerativeBackend.googleAI())
                .generativeModel(
                    modelName = "gemini-2.5-flash-image",
                    generationConfig = generationConfig {
                        responseModalities = listOf(ResponseModality.TEXT, ResponseModality.IMAGE)
                    }
                )

            val response = model.generateContent(imagePrompt)
            val generatedBitmap: Bitmap? = response.candidates.firstOrNull()?.content?.parts
                ?.firstNotNullOfOrNull { part -> part.asImageOrNull() }

            val createdBall = GolfBallSkin(
                id = id,
                name = cleanName,
                description = "Custom Gemini AI design: \"$prompt\".",
                glowColor = selectedColor,
                powerBonusPercent = (8..14).random(),
                windResistancePercent = (10..18).random(),
                isEquipped = true,
                isUnlocked = true,
                costCoins = 0
            )

            return@withContext GenerationResult(
                success = true,
                createdBall = createdBall,
                bitmap = generatedBitmap,
                message = "Successfully forged and equipped '$cleanName' via Firebase AI Logic!"
            )
        } catch (_: Exception) {
            // Fallback gracefully
        }

        val proceduralBall = GolfBallSkin(
            id = id,
            name = cleanName,
            description = "AI Studio design: \"$prompt\". Engineered for high aerodynamic stability.",
            glowColor = selectedColor,
            powerBonusPercent = 8,
            windResistancePercent = 10,
            isEquipped = true,
            isUnlocked = true,
            costCoins = 0
        )
        return@withContext GenerationResult(
            success = true,
            createdBall = proceduralBall,
            bitmap = null,
            message = "Created and equipped '$cleanName'!"
        )
    }
}
