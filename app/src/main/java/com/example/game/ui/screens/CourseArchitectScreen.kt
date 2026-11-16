package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.CustomHoleBlueprint
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.ZenithTheme

@Composable
fun CourseArchitectScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onUpdateBlueprint: (CustomHoleBlueprint) -> Unit,
    onPlaytestHole: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val colors = ZenithTheme.colors
    val scrollState = rememberScrollState()
    var blueprint by remember { mutableStateOf(uiState.customHoleBlueprint) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("course_architect_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 36.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("architect_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = "ZENITH COURSE ARCHITECT",
                            fontFamily = OutfitFontFamily,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Custom Championship Hole Forge & Sandbox",
                            fontSize = 11.sp,
                            color = colors.accentInfo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = colors.forestCard,
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, colors.accentGold.copy(alpha = 0.65f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.onForestCard.copy(alpha = 0.14f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentGold)
                            ) {
                                Text(
                                    text = "CUSTOM BLUEPRINT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentGold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = colors.accentGold.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "PAR ${blueprint.par} • ${blueprint.totalYardage} YARDS",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentGold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = blueprint.holeName,
                            fontFamily = OutfitFontFamily,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.onForestCard
                        )

                        Text(
                            text = "${blueprint.fairwayShape} • ${blueprint.greenStyle} • ${blueprint.sandBunkerCount} Bunkers • Pin: ${blueprint.pinPositionStyle}",
                            fontSize = 12.sp,
                            color = colors.onForestSecondary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = {
                                onUpdateBlueprint(blueprint)
                                onPlaytestHole()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = colors.accentGold,
                                contentColor = colors.onGold
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("playtest_custom_hole_btn")
                        ) {
                            Icon(Icons.Default.PlayArrow, null, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PLAYTEST THIS HOLE NOW",
                                fontFamily = OutfitFontFamily,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "HOLE NAME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.accentSuccess,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = blueprint.holeName,
                    onValueChange = {
                        blueprint = blueprint.copy(holeName = it)
                        onUpdateBlueprint(blueprint)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colors.primaryGreen,
                        unfocusedBorderColor = colors.border,
                        focusedTextColor = colors.textPrimary,
                        unfocusedTextColor = colors.textPrimary,
                        focusedContainerColor = colors.surface,
                        unfocusedContainerColor = colors.surface
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "PAR RATING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.accentSuccess,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(3, 4, 5).forEach { parVal ->
                        val isSelected = blueprint.par == parVal
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) colors.primaryGreen else colors.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) colors.primaryGreen else colors.border),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .clickable {
                                    val defaultYardage = when (parVal) {
                                        3 -> 175
                                        4 -> 390
                                        else -> 530
                                    }
                                    blueprint = blueprint.copy(par = parVal, totalYardage = defaultYardage)
                                    onUpdateBlueprint(blueprint)
                                }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 12.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "PAR $parVal",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) colors.onForestCard else colors.textPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "TOTAL YARDAGE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accentSuccess,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${blueprint.totalYardage} YARDS",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accentGold
                    )
                }
                Slider(
                    value = blueprint.totalYardage.toFloat(),
                    onValueChange = {
                        blueprint = blueprint.copy(totalYardage = it.toInt())
                        onUpdateBlueprint(blueprint)
                    },
                    valueRange = 130f..580f,
                    steps = 45,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.accentGold,
                        activeTrackColor = colors.primaryGreen,
                        inactiveTrackColor = colors.border
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                OptionSelectorSection(
                    title = "FAIRWAY LAYOUT",
                    options = listOf("Straight Parkway", "Right Dogleg", "Left Dogleg", "Island Oasis"),
                    selected = blueprint.fairwayShape,
                    onSelect = {
                        blueprint = blueprint.copy(fairwayShape = it)
                        onUpdateBlueprint(blueprint)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OptionSelectorSection(
                    title = "PUTTING GREEN DESIGN",
                    options = listOf("Crown Oval", "Wide Kidney", "Mini Island"),
                    selected = blueprint.greenStyle,
                    onSelect = {
                        blueprint = blueprint.copy(greenStyle = it)
                        onUpdateBlueprint(blueprint)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                OptionSelectorSection(
                    title = "PIN DIFFICULTY & PLACEMENT",
                    options = listOf("Center", "Tucked Back Right", "Front Edge", "Island Center"),
                    selected = blueprint.pinPositionStyle,
                    onSelect = {
                        blueprint = blueprint.copy(pinPositionStyle = it)
                        onUpdateBlueprint(blueprint)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SAND BUNKERS (${blueprint.sandBunkerCount})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.accentSuccess,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0, 1, 2, 3, 4).forEach { count ->
                        val isSelected = blueprint.sandBunkerCount == count
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) colors.accentGold else colors.surface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) colors.accentGold else colors.border),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 48.dp)
                                .clickable {
                                    blueprint = blueprint.copy(sandBunkerCount = count)
                                    onUpdateBlueprint(blueprint)
                                }
                        ) {
                            Box(modifier = Modifier.padding(vertical = 10.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$count",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (isSelected) colors.onGold else colors.textPrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            blueprint = blueprint.copy(hasWaterLake = !blueprint.hasWaterLake)
                            onUpdateBlueprint(blueprint)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Water, null, tint = colors.accentInfo, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("Water Lake Hazard", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
                                Text("Lake surrounding approach fairway or green", fontSize = 11.sp, color = colors.textSecondary)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (blueprint.hasWaterLake) colors.accentInfo else colors.surfaceElevated
                        ) {
                            Text(
                                text = if (blueprint.hasWaterLake) "ENABLED" else "OFF",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = if (blueprint.hasWaterLake) colors.onGold else colors.textMuted,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OptionSelectorSection(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val colors = ZenithTheme.colors
    Column {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = colors.accentSuccess,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(6.dp))
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { option ->
                val isSelected = selected == option
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) colors.surfaceElevated else colors.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        if (isSelected) 1.5.dp else 1.dp,
                        if (isSelected) colors.primaryGreen else colors.border
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { onSelect(option) }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option,
                            fontSize = 13.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.Medium,
                            color = if (isSelected) colors.primaryGreen else colors.textPrimary
                        )
                        if (isSelected) {
                            Icon(Icons.Default.Check, null, tint = colors.primaryGreen, modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
