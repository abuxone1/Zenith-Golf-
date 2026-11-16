package com.example.game.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ZenithChampionshipGreen
import com.example.ui.theme.ZenithDarkInterfaceBg
import com.example.ui.theme.ZenithDeepForestGreen
import com.example.ui.theme.ZenithNeonGreen
import com.example.ui.theme.ZenithPremiumGold
import com.example.ui.theme.ZenithPureWhite
import com.example.ui.theme.ZenithTextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onEnterClubhouse: () -> Unit,
    autoAdvanceMs: Long = 0L,
    modifier: Modifier = Modifier
) {
    var loadingProgress by remember { mutableFloatStateOf(0.25f) }
    val infiniteTransition = rememberInfiniteTransition(label = "splashPulse")
    val crestScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "crestScale"
    )

    LaunchedEffect(Unit) {
        val steps = 15
        for (i in 1..steps) {
            delay(45L)
            loadingProgress = (i.toFloat() / steps.toFloat()).coerceIn(0f, 1f)
        }
        if (autoAdvanceMs > 0L) {
            delay(autoAdvanceMs)
            onEnterClubhouse()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ZenithDarkInterfaceBg)
            .testTag("splash_screen")
    ) {
        // Championship Golf Course Backdrop
        Image(
            painter = painterResource(id = R.drawable.img_course_coastal_dunes_1794835040452),
            contentDescription = "Zenith Golf Championship Course",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Championship Green & Deep Forest Gradient Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            ZenithDeepForestGreen.copy(alpha = 0.72f),
                            ZenithChampionshipGreen.copy(alpha = 0.68f),
                            ZenithDarkInterfaceBg.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .widthIn(max = 560.dp)
                .align(Alignment.Center)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Presenter Pill
            Surface(
                shape = RoundedCornerShape(50),
                color = ZenithDeepForestGreen.copy(alpha = 0.92f),
                border = BorderStroke(1.dp, ZenithPremiumGold.copy(alpha = 0.75f)),
                modifier = Modifier.testTag("splash_studio_badge")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = ZenithPremiumGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "POWERED BY APEX DYNAMICS GAME STUDIOS",
                        fontFamily = OutfitFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = ZenithPremiumGold,
                        letterSpacing = 1.4.sp
                    )
                }
            }

            // Center Championship Crest & Zenith Golf Branding
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .scale(crestScale)
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(ZenithChampionshipGreen, ZenithDeepForestGreen)
                            )
                        )
                        .border(3.dp, ZenithPremiumGold, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.zenith_golf_icon_1791831231277),
                        contentDescription = "Zenith Golf Crest",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(22.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ZenithPremiumGold.copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, ZenithPremiumGold)
                ) {
                    Text(
                        text = "CHAMPIONSHIP TOUR EDITION • v2.0",
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZenithPremiumGold,
                        letterSpacing = 1.4.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "ZENITH GOLF",
                    fontFamily = OutfitFontFamily,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    color = ZenithPureWhite,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("splash_title")
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Powered by APEX DYNAMICS GAME STUDIOS",
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = ZenithNeonGreen,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SplashFeatureChip(icon = Icons.Default.SportsGolf, text = "3D Physics Sim")
                    SplashFeatureChip(icon = Icons.Default.EmojiEvents, text = "Championship Tour")
                }
            }

            // Bottom Engine Readiness & Enter Button
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (loadingProgress < 1f) "PREPARING FAIRWAYS & WIND TELEMETRY..." else "CLUBHOUSE READY",
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZenithTextSecondary,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "${(loadingProgress * 100).toInt()}%",
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = ZenithPremiumGold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { loadingProgress },
                    color = ZenithPremiumGold,
                    trackColor = ZenithDeepForestGreen,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = onEnterClubhouse,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ZenithPremiumGold,
                        contentColor = Color(0xFF1A1200)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("splash_enter_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ENTER CLUBHOUSE",
                        fontFamily = OutfitFontFamily,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.2.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun SplashFeatureChip(
    icon: ImageVector,
    text: String
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = ZenithDeepForestGreen.copy(alpha = 0.88f),
        border = BorderStroke(1.dp, ZenithPremiumGold.copy(alpha = 0.45f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ZenithPremiumGold,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = text,
                fontFamily = PlusJakartaSansFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ZenithPureWhite
            )
        }
    }
}
