package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.ai.AiModelChoice
import com.example.game.ai.CaddieChatService
import com.example.game.ai.ChatMessage
import com.example.game.ai.MessageSender
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.ZenithTheme
import kotlinx.coroutines.launch

@Composable
fun CaddieChatScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val colors = ZenithTheme.colors
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val messages = remember {
        mutableStateListOf(
            ChatMessage(
                sender = MessageSender.CADDIE,
                text = "Welcome to the Caddie Lounge, pro! I'm your Zenith Tour Caddie. Ask me anything about shot physics, wind calculations, course strategy on Zenith Dunes, or round analysis."
            )
        )
    }

    var inputText by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var selectedModel by remember { mutableStateOf(AiModelChoice.FLASH) }

    fun sendUserMessage(text: String) {
        val trimmed = text.trim()
        if (trimmed.isEmpty() || isLoading) return

        val userMsg = ChatMessage(sender = MessageSender.USER, text = trimmed)
        messages.add(userMsg)
        inputText = ""
        isLoading = true

        coroutineScope.launch {
            listState.animateScrollToItem(messages.size - 1)
            val replyText = CaddieChatService.sendMessage(
                history = messages,
                userMessage = trimmed,
                modelChoice = selectedModel,
                currentHoleStats = uiState.holeStatistics
            )
            messages.add(ChatMessage(sender = MessageSender.CADDIE, text = replyText))
            isLoading = false
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val quickQuestions = listOf(
        "How to conquer Hole 2 Island Green?",
        "Compensating for 18mph crosswinds",
        "When to use Draw vs Fade curve?",
        "Green slope reading & putting tips",
        "Review my live hole stats & GIR"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .imePadding()
            .testTag("caddie_chat_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize()
        ) {
            Surface(
                color = colors.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("chat_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.textPrimary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(colors.accentInfo.copy(alpha = 0.16f), CircleShape)
                                .border(1.5.dp, colors.accentInfo, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = colors.accentInfo,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "ZENITH TOUR CADDIE",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.textPrimary,
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(7.dp)
                                        .background(colors.accentSuccess, CircleShape)
                                )
                            }
                            Text(
                                text = "Tactical Shot Strategy & Analytics",
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = colors.surfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentInfo.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .clickable {
                                selectedModel = if (selectedModel == AiModelChoice.FLASH) AiModelChoice.PRO else AiModelChoice.FLASH
                            }
                            .testTag("model_toggle_chip")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "Switch Strategy Mode",
                                tint = colors.accentInfo,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (selectedModel == AiModelChoice.FLASH) "Flash" else "Pro",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.accentInfo
                            )
                        }
                    }
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages, key = { it.id }) { msg ->
                    ChatBubbleItem(message = msg)
                }

                if (isLoading) {
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 6.dp)
                        ) {
                            CircularProgressIndicator(
                                color = colors.accentInfo,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Calculating shot vectors & lie angles...",
                                fontSize = 12.sp,
                                color = colors.accentInfo,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(quickQuestions) { question ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = colors.surfaceElevated,
                        border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                        modifier = Modifier.clickable {
                            sendUserMessage(question)
                        }
                    ) {
                        Text(
                            text = question,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }

            Surface(
                color = colors.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                text = "Ask Caddie about wind, clubs, hole strategy...",
                                fontSize = 13.sp,
                                color = colors.textMuted
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colors.accentInfo,
                            unfocusedBorderColor = colors.border,
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            cursorColor = colors.accentInfo
                        ),
                        shape = RoundedCornerShape(20.dp),
                        maxLines = 3,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("chat_input_field")
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { sendUserMessage(inputText) },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                if (inputText.isNotBlank() && !isLoading) colors.primaryGreen else colors.surfaceElevated,
                                CircleShape
                            )
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isLoading) colors.onForestCard else colors.textMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubbleItem(message: ChatMessage) {
    val colors = ZenithTheme.colors
    val isUser = message.sender == MessageSender.USER

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(colors.accentInfo.copy(alpha = 0.16f), CircleShape)
                    .border(1.dp, colors.accentInfo, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = colors.accentInfo,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) colors.forestCard else colors.surface,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isUser) colors.accentGold.copy(alpha = 0.55f) else colors.border
            ),
            modifier = Modifier.fillMaxWidth(if (message.text.length > 50) 0.88f else 0.75f)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = if (isUser) "YOU" else "ZENITH CADDIE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isUser) colors.accentGold else colors.accentInfo,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    fontSize = 13.sp,
                    color = if (isUser) colors.onForestCard else colors.textPrimary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
