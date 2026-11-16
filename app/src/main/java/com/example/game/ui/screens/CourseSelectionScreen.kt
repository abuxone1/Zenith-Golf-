package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.ZenithDeepForestGreen
import com.example.ui.theme.ZenithNeonGreen
import com.example.ui.theme.ZenithPremiumGold
import com.example.ui.theme.ZenithTheme

data class CourseShowcaseSpec(
    val cup: TournamentCup,
    @param:DrawableRes val imageRes: Int,
    val tierLabel: String,
    val starRating: Int,
    val parTotal: Int,
    val totalYardage: Int,
    val greenStimp: String,
    val windProfile: String,
    val requiredTrophies: Int
)

@Composable
fun CourseSelectionScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onSelectCourseCup: (TournamentCup) -> Unit,
    onOpenCourseArchitect: () -> Unit,
    onPlayCustomCourse: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }
    val colors = ZenithTheme.colors
    var selectedTierFilter by rememberSaveable { mutableStateOf("ALL") }

    val playerCoins = uiState.career.coins
    val playerTrophies = uiState.career.trophies

    val courseSpecs = uiState.tournamentCups.mapIndexed { index, cup ->
        when (index) {
            0 -> CourseShowcaseSpec(
                cup = cup,
                imageRes = R.drawable.img_course_parkland_beginner_1794846398286,
                tierLabel = "BEGINNER",
                starRating = 3,
                parTotal = 23,
                totalYardage = 2035,
                greenStimp = "11.5 ft (Medium-Fast)",
                windProfile = "6–12 MPH Coastal Breeze",
                requiredTrophies = 0
            )
            1 -> CourseShowcaseSpec(
                cup = cup,
                imageRes = R.drawable.zenith_tour_banner_1791831254393,
                tierLabel = "INTERMEDIATE",
                starRating = 4,
                parTotal = 23,
                totalYardage = 2140,
                greenStimp = "12.8 ft (Tour Fast)",
                windProfile = "10–16 MPH Swirling Crosswind",
                requiredTrophies = 1
            )
            else -> CourseShowcaseSpec(
                cup = cup,
                imageRes = R.drawable.img_course_highlands_masters_1794835040498,
                tierLabel = if (cup.difficulty.contains("Apex", ignoreCase = true)) "CHAMPIONSHIP" else "PROFESSIONAL",
                starRating = 5,
                parTotal = 23,
                totalYardage = 2280,
                greenStimp = "13.5 ft (Championship Glass)",
                windProfile = "14–22 MPH Highland Gusts",
                requiredTrophies = 2
            )
        }
    }

    val filteredSpecs = if (selectedTierFilter == "ALL") {
        courseSpecs
    } else {
        courseSpecs.filter {
            it.tierLabel.equals(selectedTierFilter, ignoreCase = true) ||
                (selectedTierFilter == "PROFESSIONAL" && it.tierLabel == "CHAMPIONSHIP")
        }
    }

    val tierFilters = listOf("ALL", "BEGINNER", "INTERMEDIATE", "PROFESSIONAL", "CHAMPIONSHIP")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .testTag("course_selection_screen"),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = colors.surface,
            border = BorderStroke(1.dp, colors.border)
        ) {
            Column(modifier = Modifier.widthIn(max = 860.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("course_selection_back_btn")
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
                                text = "WORLD TOUR VENUES",
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentGold,
                                letterSpacing = 1.2.sp
                            )
                            Text(
                                text = "COURSE SELECTION",
                                fontFamily = OutfitFontFamily,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.textPrimary
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CurrencyPill(icon = Icons.Default.EmojiEvents, value = "$playerTrophies", color = colors.accentSuccess)
                        CurrencyPill(icon = Icons.Default.MonetizationOn, value = "$playerCoins", color = colors.accentGold)
                    }
                }

                // Difficulty Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tierFilters.forEach { tier ->
                        val isSelected = selectedTierFilter == tier
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) colors.primaryGreen else colors.surfaceElevated,
                            border = BorderStroke(1.dp, if (isSelected) colors.accentGold else colors.border),
                            modifier = Modifier
                                .clickable { selectedTierFilter = tier }
                                .testTag("course_filter_${tier.lowercase()}")
                        ) {
                            Text(
                                text = tier,
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) Color.White else colors.textSecondary,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
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

            items(filteredSpecs, key = { it.cup.id }) { spec ->
                val cup = spec.cup
                val hasEnoughTrophies = playerTrophies >= spec.requiredTrophies
                val hasEnoughCoins = playerCoins >= cup.entryFeeCoins
                val canTeeOff = hasEnoughTrophies && hasEnoughCoins
                val accent = Color(cup.bannerAccentHex)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("course_card_${cup.id}"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    border = BorderStroke(1.5.dp, if (canTeeOff) accent.copy(alpha = 0.65f) else colors.border)
                ) {
                    Column {
                        // Course Photograph Banner
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(172.dp)
                        ) {
                            Image(
                                painter = painterResource(id = spec.imageRes),
                                contentDescription = cup.courseName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Black.copy(alpha = 0.2f),
                                                ZenithDeepForestGreen.copy(alpha = 0.65f),
                                                Color.Black.copy(alpha = 0.9f)
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
                                    border = BorderStroke(1.dp, accent)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = spec.tierLabel,
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = accent,
                                            letterSpacing = 0.8.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        repeat(spec.starRating) {
                                            Icon(
                                                imageVector = Icons.Default.Star,
                                                contentDescription = null,
                                                tint = ZenithPremiumGold,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = ZenithDeepForestGreen.copy(alpha = 0.9f),
                                    border = BorderStroke(1.dp, if (canTeeOff) ZenithNeonGreen else ZenithPremiumGold)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = if (canTeeOff) Icons.Default.CheckCircle else Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = if (canTeeOff) ZenithNeonGreen else ZenithPremiumGold,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (canTeeOff) "UNLOCKED" else "REQ: ${spec.requiredTrophies} CUP • ${cup.entryFeeCoins} C",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (canTeeOff) ZenithNeonGreen else ZenithPremiumGold
                                        )
                                    }
                                }
                            }

                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = cup.courseName.uppercase(),
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 21.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "${cup.title} • ${cup.subtitle}",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }
                        }

                        // Course Specifications Grid
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CourseSpecPill(
                                    label = "HOLE COUNT",
                                    value = "${cup.holesCount} Holes (Par ${spec.parTotal})",
                                    accent = colors.textPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                CourseSpecPill(
                                    label = "TOTAL YARDAGE",
                                    value = "${spec.totalYardage} YDS",
                                    accent = colors.accentGold,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CourseSpecPill(
                                    label = "COURSE CONDITIONS",
                                    value = "${cup.defaultWeather.displayName} • ${spec.windProfile.substringBefore(" MPH")} MPH",
                                    accent = colors.accentInfo,
                                    modifier = Modifier.weight(1f)
                                )
                                CourseSpecPill(
                                    label = "GREEN SPEED",
                                    value = spec.greenStimp,
                                    accent = colors.accentSuccess,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CourseSpecPill(
                                    label = "UNLOCK REQUIREMENT",
                                    value = if (spec.requiredTrophies == 0 && cup.entryFeeCoins == 0) {
                                        "Open Tour Access (Free)"
                                    } else {
                                        "${spec.requiredTrophies}+ Trophies & ${cup.entryFeeCoins} Coins"
                                    },
                                    accent = if (canTeeOff) colors.accentSuccess else colors.accentGold,
                                    modifier = Modifier.weight(1f)
                                )
                                CourseSpecPill(
                                    label = "CHAMPION PURSE",
                                    value = "+${cup.prizeCoins} Coins • +${cup.prizeTrophies} Cups",
                                    accent = colors.accentGold,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { onSelectCourseCup(cup) },
                                enabled = hasEnoughCoins,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = colors.accentGold,
                                    contentColor = colors.onGold,
                                    disabledContainerColor = colors.surfaceElevated,
                                    disabledContentColor = colors.textMuted
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .testTag("select_course_btn_${cup.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (hasEnoughCoins) {
                                        "TEE OFF ON ${cup.courseName.uppercase()}"
                                    } else {
                                        "REQUIRES ${cup.entryFeeCoins} COINS ENTRY"
                                    },
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.6.sp
                                )
                            }
                        }
                    }
                }
            }

            // Custom Course Architect Sandbox Card
            item {
                val blueprint = uiState.customHoleBlueprint
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("course_card_custom_architect"),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = colors.forestCard),
                    border = BorderStroke(1.5.dp, colors.accentGold)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = ZenithNeonGreen.copy(alpha = 0.2f),
                                border = BorderStroke(1.dp, ZenithNeonGreen)
                            ) {
                                Text(
                                    text = "CUSTOM COURSE ARCHITECT",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ZenithNeonGreen,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                            Text(
                                text = "PAR ${blueprint.par} • ${blueprint.totalYardage} YDS",
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = ZenithPremiumGold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = blueprint.holeName,
                            fontFamily = OutfitFontFamily,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.onForestCard
                        )
                        Text(
                            text = "Fairway: ${blueprint.fairwayShape} • Green: ${blueprint.greenStyle} • Bunkers: ${blueprint.sandBunkerCount} • Slope: ${blueprint.slopeDifficulty}",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            color = colors.onForestSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onPlayCustomCourse,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ZenithNeonGreen,
                                    contentColor = Color(0xFF003816)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("play_custom_course_btn")
                            ) {
                                Icon(Icons.Default.SportsGolf, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "PLAYTEST HOLE",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }

                            Button(
                                onClick = onOpenCourseArchitect,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = ZenithDeepForestGreen,
                                    contentColor = ZenithPremiumGold
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, ZenithPremiumGold),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("open_course_architect_btn")
                            ) {
                                Icon(Icons.Default.Palette, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "EDIT BLUEPRINT",
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun CourseSpecPill(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = ZenithTheme.colors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = colors.surfaceElevated,
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
            Text(
                text = label,
                fontFamily = OutfitFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textMuted,
                letterSpacing = 0.6.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontFamily = OutfitFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = accent
            )
        }
    }
}
