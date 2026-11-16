package com.example.game.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.ai.CustomBallStudioService
import com.example.game.model.GolfBallSkin
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.ZenithTheme
import kotlinx.coroutines.launch

@Composable
fun CustomBallStudioScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onBallCreatedAndEquipped: (GolfBallSkin) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val colors = ZenithTheme.colors
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var promptInput by remember { mutableStateOf("Neon lightning cyber golf ball with glowing electric blue matrix rings") }
    var ballNameInput by remember { mutableStateOf("Cyber Bolt MK-I") }
    var isGenerating by remember { mutableStateOf(false) }
    var lastGeneratedBall by remember { mutableStateOf<GolfBallSkin?>(null) }
    var lastGeneratedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }

    val presetIdeas = listOf(
        Pair("Cyber Bolt", "Neon lightning cyber golf ball with glowing electric blue matrix rings"),
        Pair("Solar Flare", "Thermal magma golf ball with glowing solar flare corona and ruby sparks"),
        Pair("Cosmic Nebula", "Deep space galaxy golf ball swirling with violet stardust and cyan nebula"),
        Pair("Golden Apex", "24K polished gold championship golf ball with laser etched zenith circuits")
    )

    fun handleGenerate() {
        if (promptInput.isBlank() || isGenerating) return
        isGenerating = true
        statusMessage = null

        coroutineScope.launch {
            val result = CustomBallStudioService.generateCustomBall(
                prompt = promptInput,
                ballName = ballNameInput
            )
            isGenerating = false
            if (result.success) {
                lastGeneratedBall = result.createdBall
                lastGeneratedBitmap = result.bitmap
                statusMessage = result.message
                onBallCreatedAndEquipped(result.createdBall)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("custom_ball_studio_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("studio_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = "ZENITH DESIGN LAB",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accentGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Create Custom Golf Balls",
                        fontFamily = OutfitFontFamily,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .background(colors.accentInfo.copy(alpha = 0.16f), CircleShape)
                            .border(1.5.dp, colors.accentInfo, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = colors.accentInfo,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "Custom Equipment Workshop",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Design custom golf ball styles with specialized aerodynamic bonuses and equip them straight to your bag.",
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "STYLE PRESETS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = colors.accentInfo,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(presetIdeas) { (title, prompt) ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = colors.surfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                        modifier = Modifier.clickable {
                            ballNameInput = title
                            promptInput = prompt
                        }
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.textPrimary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "BALL NAME",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = ballNameInput,
                onValueChange = { ballNameInput = it },
                placeholder = { Text("e.g. Cyber Pulse MK-I", color = colors.textMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.primaryGreen,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth().testTag("ball_name_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "VISUAL DESIGN PROMPT",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMuted,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = promptInput,
                onValueChange = { promptInput = it },
                placeholder = { Text("Describe golf ball colors, finish, and pattern...", color = colors.textMuted) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accentInfo,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                maxLines = 4,
                modifier = Modifier.fillMaxWidth().testTag("ball_prompt_input")
            )

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = { handleGenerate() },
                enabled = promptInput.isNotBlank() && !isGenerating,
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.primaryGreen,
                    contentColor = colors.onForestCard
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("generate_ball_cta")
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = colors.onForestCard,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "FORGING BALL DESIGN...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "FORGE CUSTOM GOLF BALL",
                        fontFamily = OutfitFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            AnimatedVisibility(visible = lastGeneratedBall != null) {
                val ball = lastGeneratedBall
                if (ball != null) {
                    Column {
                        Spacer(modifier = Modifier.height(20.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = colors.surface),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, colors.accentGold)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                if (lastGeneratedBitmap != null) {
                                    Image(
                                        bitmap = lastGeneratedBitmap!!.asImageBitmap(),
                                        contentDescription = ball.name,
                                        modifier = Modifier
                                            .size(140.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .border(2.dp, ball.glowColor, RoundedCornerShape(16.dp))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .background(ball.glowColor.copy(alpha = 0.22f), CircleShape)
                                            .border(3.dp, ball.glowColor, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SportsGolf,
                                            contentDescription = null,
                                            tint = ball.glowColor,
                                            modifier = Modifier.size(48.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = ball.name,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.textPrimary
                                )

                                Text(
                                    text = ball.description,
                                    fontSize = 12.sp,
                                    color = colors.textSecondary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = colors.accentSuccess.copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = "+${ball.powerBonusPercent}% Power",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colors.accentSuccess,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = colors.accentInfo.copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = "+${ball.windResistancePercent}% Wind Res.",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colors.accentInfo,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = colors.accentSuccess.copy(alpha = 0.16f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentSuccess)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = colors.accentSuccess,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "EQUIPPED IN BAG",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colors.accentSuccess
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
