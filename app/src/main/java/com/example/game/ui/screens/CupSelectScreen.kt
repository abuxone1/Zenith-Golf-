package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.model.TournamentCup
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ZenithTheme

@Composable
fun CupSelectScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onSelectCup: (TournamentCup) -> Unit,
    onOpenLeaderboard: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val colors = ZenithTheme.colors
    val cups = uiState.tournamentCups
    val playerCoins = uiState.career.coins

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .testTag("cup_select_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Header Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("cup_select_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.padding(end = 8.dp)) {
                        Text(
                            text = "OFFICIAL TOUR SCHEDULE",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentGold,
                            letterSpacing = 1.2.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "TOURNAMENTS",
                            fontFamily = OutfitFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Coin Balance
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surfaceElevated,
                    border = BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = colors.accentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$playerCoins",
                            fontFamily = OutfitFontFamily,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.accentGold
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Live Player Rankings & Leaderboard Summary Card
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenLeaderboard() }
                        .testTag("tournaments_open_leaderboard_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.forestCard),
                    border = BorderStroke(1.5.dp, colors.accentGold)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Leaderboard,
                                    contentDescription = null,
                                    tint = colors.accentGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PLAYER RANKINGS & STANDINGS",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentGold,
                                    letterSpacing = 1.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tour Rank: #${uiState.tournamentFinalRank} • ${uiState.career.trophies} Championship Cups",
                                fontFamily = OutfitFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.onForestCard
                            )
                            Text(
                                text = "Tap to view Live Standings & Global Cloud Top 25",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = colors.onForestSecondary
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = colors.accentGold,
                            modifier = Modifier.heightIn(min = 48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "RANKINGS",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.onGold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "ACTIVE CHAMPIONSHIP TOURNAMENTS",
                    fontFamily = OutfitFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.accentGold,
                    letterSpacing = 1.1.sp
                )
            }

            itemsIndexed(cups) { idx, cup ->
                val canAfford = playerCoins >= cup.entryFeeCoins
                val isRegisteredActive = uiState.registeredCupIds.contains(cup.id)
                val accent = Color(cup.bannerAccentHex)
                val scheduleTag = when (idx) {
                    0 -> "OPEN SERIES • LIVE 24/7"
                    1 -> "PRO TOUR • WEEKLY CIRCUIT"
                    else -> "CHAMPIONS MAJOR • FEATURED"
                }
                val courseImg = when (idx) {
                    0 -> R.drawable.img_course_coastal_dunes_1794835040452
                    1 -> R.drawable.zenith_tour_banner_1791831254393
                    else -> R.drawable.img_course_highlands_masters_1794835040498
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(
                        if (isRegisteredActive) 2.dp else 1.5.dp,
                        if (isRegisteredActive) colors.accentGold else accent.copy(alpha = 0.6f)
                    )
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(142.dp)
                        ) {
                            Image(
                                painter = painterResource(id = courseImg),
                                contentDescription = cup.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color(0x4409271C),
                                                Color(0xB809271C),
                                                Color(0xE609271C)
                                            )
                                        )
                                    )
                            )

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xCC09271C),
                                    border = BorderStroke(1.dp, accent)
                                ) {
                                    Text(
                                        text = "${cup.difficulty.uppercase()} • ${cup.holesCount} HOLES",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xCC09271C),
                                    border = BorderStroke(
                                        1.dp,
                                        when {
                                            isRegisteredActive -> colors.accentGold
                                            canAfford -> colors.accentSuccess
                                            else -> colors.error
                                        }
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (isRegisteredActive) Icons.Default.Verified else Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = when {
                                                isRegisteredActive -> colors.accentGold
                                                canAfford -> Color(0xFF69F0AE)
                                                else -> Color(0xFFFF8A80)
                                            },
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = when {
                                                isRegisteredActive -> "REGISTERED"
                                                canAfford -> "OPEN ENTRY"
                                                else -> "INSUFFICIENT COINS"
                                            },
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFFD6B45C), modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = scheduleTag,
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD6B45C)
                                    )
                                }
                                Text(
                                    text = cup.title,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "${cup.courseName} • ${cup.subtitle}",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp,
                                    color = Color(0xFFDCE7E0)
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "1ST PLACE PURSE",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 10.sp,
                                        color = colors.textMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = "${cup.prizeCoins} Coins + ${cup.prizeTrophies} Cups",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colors.accentGold
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "ENTRY FEE",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 10.sp,
                                        color = colors.textMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (cup.entryFeeCoins == 0) "FREE" else "${cup.entryFeeCoins} Coins",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (canAfford) colors.accentSuccess else colors.error
                                    )
                                }

                                Column {
                                    Text(
                                        text = "WEATHER",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 10.sp,
                                        color = colors.textMuted,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = cup.defaultWeather.displayName.take(12),
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = colors.accentInfo
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { onSelectCup(cup) },
                                enabled = canAfford,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accentGold,
                                    contentColor = colors.onGold
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("enter_cup_${cup.id}")
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = when {
                                        !canAfford -> "NEED ${cup.entryFeeCoins} COINS TO ENTER"
                                        isRegisteredActive -> "TEE OFF REGISTERED TOURNAMENT"
                                        else -> "REGISTER & TEE OFF"
                                    },
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
