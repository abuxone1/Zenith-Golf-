package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ZenithDeepForestGreen
import com.example.ui.theme.ZenithEmerald
import com.example.ui.theme.ZenithTheme

@Composable
fun GameModesScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onStartQuickPlay: () -> Unit,
    onStartCareerRound: () -> Unit,
    onStartPracticeRange: () -> Unit,
    onNavigate: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val colors = ZenithTheme.colors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .testTag("game_modes_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 860.dp)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("game_modes_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "ZENITH GOLF 2.0",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentGold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "SELECT GAME MODE",
                            fontFamily = OutfitFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary
                        )
                    }
                }

                // Select Course Shortcut
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.forestCard,
                    border = BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.7f)),
                    modifier = Modifier
                        .clickable { onNavigate(AppScreen.COURSE_SELECTION) }
                        .testTag("game_modes_courses_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = colors.accentGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "COURSES",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            item {
                Text(
                    text = "AVAILABLE NOW • PLAYABLE MODES",
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.accentSuccess,
                    letterSpacing = 1.2.sp
                )
            }

            // 1. QUICK PLAY CARD
            item {
                FeaturedGameModeCard(
                    title = "QUICK PLAY",
                    subtitle = "Jump straight into a 3-hole championship prototype round (Par 4 • Par 3 Island • Par 5 Dogleg) with full wind, spin, and terrain physics.",
                    badge = "3-HOLE EXPRESS",
                    statsPill = "Available • +300 Coins & XP",
                    icon = Icons.Default.SportsGolf,
                    accentColor = colors.accentSuccess,
                    imageRes = R.drawable.img_course_parkland_beginner_1794846398286,
                    primaryCtaText = "TEE OFF QUICK PLAY",
                    onPrimaryClick = onStartQuickPlay,
                    secondaryCtaText = "SELECT COURSE",
                    onSecondaryClick = { onNavigate(AppScreen.COURSE_SELECTION) },
                    testTag = "game_mode_quick_play"
                )
            }

            // 2. CAREER MODE CARD
            item {
                FeaturedGameModeCard(
                    title = "CAREER MODE",
                    subtitle = "Progress through the 6-hole Apex Tour Championship season, unlock elite courses, level up your golfer attributes, and earn Season 1 Tour Pass tiers.",
                    badge = "SEASON 1 CAREER",
                    statsPill = "${uiState.career.roundsPlayed} Rounds • ${uiState.career.trophies} Trophies",
                    icon = Icons.Default.WorkspacePremium,
                    accentColor = colors.accentGold,
                    imageRes = R.drawable.img_course_highlands_masters_1794835040498,
                    primaryCtaText = "CONTINUE CAREER (6 HOLES)",
                    onPrimaryClick = onStartCareerRound,
                    secondaryCtaText = "TOUR PASS",
                    onSecondaryClick = { onNavigate(AppScreen.TOUR_PASS) },
                    testTag = "game_mode_career"
                )
            }

            // 3. TOURNAMENT MODE CARD
            item {
                FeaturedGameModeCard(
                    title = "TOURNAMENT MODE",
                    subtitle = "Register for the Dunes Open, Neon Tokyo Classic, and Apex Masters Invitational. Climb live AI standings and the Global Cloud leaderboard.",
                    badge = "3 OFFICIAL CUPS",
                    statsPill = "Active: ${uiState.selectedCup.title}",
                    icon = Icons.Default.EmojiEvents,
                    accentColor = colors.accentGold,
                    imageRes = R.drawable.zenith_tour_banner_1791831254393,
                    primaryCtaText = "ENTER TOURNAMENTS",
                    onPrimaryClick = { onNavigate(AppScreen.CUP_SELECT) },
                    secondaryCtaText = "LEADERBOARD",
                    onSecondaryClick = { onNavigate(AppScreen.TOURNAMENT_LEADERBOARD) },
                    testTag = "game_mode_tournament"
                )
            }

            // 4. PRACTICE MODE CARD
            item {
                FeaturedGameModeCard(
                    title = "PRACTICE MODE",
                    subtitle = "Master swing power, draw/fade spin shaping, and green reading on the infinite Driving Range or the 5-Shot Bullseye Target Shootout.",
                    badge = "RANGE & SHOOTOUT",
                    statsPill = "Shootout Record: ${uiState.career.shootoutHighScore} PTS",
                    icon = Icons.Default.TrackChanges,
                    accentColor = ZenithEmerald,
                    imageRes = R.drawable.img_course_coastal_dunes_1794835040452,
                    primaryCtaText = "DRIVING RANGE",
                    onPrimaryClick = onStartPracticeRange,
                    secondaryCtaText = "TARGET SHOOTOUT",
                    onSecondaryClick = { onNavigate(AppScreen.TARGET_SHOOTOUT) },
                    testTag = "game_mode_practice"
                )
            }

            // 5. MULTIPLAYER CARD
            item {
                FeaturedGameModeCard(
                    title = "MULTIPLAYER & DUELS",
                    subtitle = "Challenge tour rivals head-to-head in 3-hole 1v1 Match Play Showdowns with coin wagers and live shot pressure, plus Global Cloud standings.",
                    badge = "1V1 MATCH PLAY",
                    statsPill = "Record: ${uiState.career.matchPlayWins}W - ${uiState.career.matchPlayLosses}L",
                    icon = Icons.Default.Group,
                    accentColor = colors.accentInfo,
                    imageRes = R.drawable.zenith_tour_banner_1791831254393,
                    primaryCtaText = "1V1 MATCH PLAY DUEL",
                    onPrimaryClick = { onNavigate(AppScreen.MATCH_PLAY_DUEL) },
                    secondaryCtaText = "CLOUD RANKINGS",
                    onSecondaryClick = { onNavigate(AppScreen.TOURNAMENT_LEADERBOARD) },
                    testTag = "game_mode_multiplayer"
                )
            }

            // 6. DAILY CHALLENGE CARD
            item {
                val completedCount = uiState.dailyChallenges.count { it.isCompleted }
                FeaturedGameModeCard(
                    title = "DAILY CHALLENGE",
                    subtitle = "Complete daily precision golf objectives (Island Sniper, 290+ Drive, Sand Escape, Long Putt), claim login streak bonuses & spin the Lucky Wheel.",
                    badge = "DAILY MISSIONS",
                    statsPill = "$completedCount/${uiState.dailyChallenges.size} Completed",
                    icon = Icons.Default.Flag,
                    accentColor = colors.accentGold,
                    imageRes = R.drawable.img_course_parkland_beginner_1794846398286,
                    primaryCtaText = "OPEN DAILY CHALLENGES",
                    onPrimaryClick = { onNavigate(AppScreen.DAILY_CHALLENGES) },
                    secondaryCtaText = "PLAY QUICK HOLE",
                    onSecondaryClick = onStartQuickPlay,
                    testTag = "game_mode_daily_challenge"
                )
            }

            // UPCOMING / SEASONAL SCHEDULED SECTION (Clear separation from available modes)
            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "UPCOMING SEASONAL EVENTS",
                    fontFamily = OutfitFontFamily,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.textMuted,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = colors.surface,
                    border = BorderStroke(1.dp, colors.border),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(colors.surfaceElevated, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Upcoming mode",
                                tint = colors.accentGold,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "SEASON 2 RYDER TEAM CLASH",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.textPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = colors.accentGold.copy(alpha = 0.16f)
                                ) {
                                    Text(
                                        text = "UPCOMING",
                                        fontFamily = OutfitFontFamily,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colors.accentGold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "4-Player Alternate Shot Club Cup unlocks next Tour Season (${uiState.tourPassState.daysRemaining} days left in Season 1).",
                                fontFamily = PlusJakartaSansFontFamily,
                                fontSize = 11.sp,
                                color = colors.textSecondary
                            )
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun FeaturedGameModeCard(
    title: String,
    subtitle: String,
    badge: String,
    statsPill: String,
    icon: ImageVector,
    accentColor: Color,
    @DrawableRes imageRes: Int,
    primaryCtaText: String,
    onPrimaryClick: () -> Unit,
    secondaryCtaText: String,
    onSecondaryClick: () -> Unit,
    testTag: String
) {
    val colors = ZenithTheme.colors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.5.dp, accentColor.copy(alpha = 0.55f))
    ) {
        Column {
            // Top Visual Course Header (always crisp dark scrim so white text is readable in both Light and Dark themes)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(136.dp)
            ) {
                Image(
                    painter = painterResource(id = imageRes),
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.25f),
                                    ZenithDeepForestGreen.copy(alpha = 0.72f),
                                    Color.Black.copy(alpha = 0.88f)
                                )
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ZenithDeepForestGreen.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, accentColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = badge,
                                fontFamily = OutfitFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = accentColor,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ZenithDeepForestGreen.copy(alpha = 0.88f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                    ) {
                        Text(
                            text = statsPill,
                            fontFamily = OutfitFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = title,
                    fontFamily = OutfitFontFamily,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }

            // Card Body & Action Buttons
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text(
                    text = subtitle,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    lineHeight = 17.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onPrimaryClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primaryGreen,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.35f)
                            .height(48.dp)
                            .testTag("${testTag}_primary_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = primaryCtaText,
                            fontFamily = OutfitFontFamily,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    OutlinedButton(
                        onClick = onSecondaryClick,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, colors.border),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = colors.surfaceElevated,
                            contentColor = colors.textPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("${testTag}_secondary_btn")
                    ) {
                        Text(
                            text = secondaryCtaText,
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
