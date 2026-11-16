package com.example.game.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.GolferProfile
import com.example.game.model.GolferStance
import com.example.game.model.GolferSwingStyle
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ZenithTheme

@Composable
fun GolferCustomizationScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onUpdateGolfer: (GolferProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val colors = ZenithTheme.colors
    val scrollState = rememberScrollState()
    var profile by remember(uiState.golferProfile) { mutableStateOf(uiState.golferProfile) }

    val skinTones = listOf(
        "Fair" to 0xFFFFCCBC,
        "Warm Sand" to 0xFFF1C27D,
        "Olive" to 0xFFC68642,
        "Bronze" to 0xFF8D5524,
        "Espresso" to 0xFF5C3317
    )
    val visorColors = listOf(0xFF123D2A, 0xFFD6B45C, 0xFF00E5FF, 0xFFFF5252, 0xFFFFFFFF)
    val poloColors = listOf(0xFF123D2A, 0xFF09271C, 0xFF00B8D4, 0xFF311B92, 0xFF263238)
    val hatStyles = listOf("Tour Visor", "Championship Cap", "Bucket Hat", "No Hat")
    val gloveStyles = listOf("Carbon Fiber Pro", "Cabretta Leather White", "Gold Grip Tour", "Emerald All-Weather")
    val shoeStyles = listOf("Spikeless Pro White", "Tour Oxford Obsidian", "Emerald Turf Spikes", "Gold Trim Athletic")
    val golfBagStyles = listOf("Forest & Gold Tour", "Stealth Carbon Bag", "Royal Navy Staff Bag", "Alpine Pearl Bag")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(scrollState)
            .padding(bottom = 32.dp)
            .testTag("golfer_customization_screen"),
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
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("golfer_back_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                }
                Column {
                    Text(
                        text = "PRO GOLFER LOCKER ROOM",
                        fontFamily = OutfitFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Apex Dynamics Athlete Creation & Kit Studio",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        color = colors.accentInfo
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Column(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // Golfer Avatar Preview & Identity Card
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = colors.surface,
                border = BorderStroke(1.5.dp, colors.accentGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(colors.forestCard)
                            .border(1.5.dp, colors.accentGold, RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        GolferAvatarCanvas(
                            visorColor = Color(profile.visorColorHex),
                            poloColor = Color(profile.poloColorHex),
                            skinColor = Color(profile.skinToneHex),
                            hatStyle = profile.hatStyle,
                            isLeftHanded = profile.stance == GolferStance.LEFT_HANDED,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = colors.accentGold.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "TOUR CARD • OVR ${(profile.powerAttribute + profile.accuracyAttribute + profile.spinControlAttribute + profile.puttingAttribute) / 4}",
                                fontFamily = OutfitFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentGold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = profile.golferName,
                            fontFamily = OutfitFontFamily,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "${profile.stance.label} • ${profile.swingStyle.label}",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 12.sp,
                            color = colors.accentInfo,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${profile.hatStyle} • ${profile.shoeStyle}",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                        Text(
                            text = "Bag: ${profile.golfBagStyle}",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            color = colors.textMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Golfer Name Field
            Text("ATHLETE DISPLAY NAME", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Black, color = colors.accentGold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = profile.golferName,
                onValueChange = {
                    profile = profile.copy(golferName = it)
                    onUpdateGolfer(profile)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colors.accentGold,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor = colors.textPrimary,
                    unfocusedTextColor = colors.textPrimary,
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Skin Tone Swatches
            Text("GOLFER SKIN TONE", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Black, color = colors.accentGold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                skinTones.forEach { (toneLabel, hex) ->
                    val selected = profile.skinToneHex == hex
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable {
                                profile = profile.copy(skinToneHex = hex)
                                onUpdateGolfer(profile)
                            }
                            .testTag("skin_tone_${toneLabel.lowercase().replace(" ", "_")}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(hex))
                                .border(
                                    width = if (selected) 3.dp else 1.dp,
                                    color = if (selected) colors.accentGold else colors.border,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stance Selection
            Text("SWING STANCE ORIENTATION", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Black, color = colors.accentGold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GolferStance.entries.forEach { stanceOption ->
                    val isSelected = profile.stance == stanceOption
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) colors.forestCard else colors.surface,
                        border = BorderStroke(1.5.dp, if (isSelected) colors.accentGold else colors.border),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable {
                                profile = profile.copy(stance = stanceOption)
                                onUpdateGolfer(profile)
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = stanceOption.label.uppercase(),
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = if (isSelected) colors.accentGold else colors.textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Headwear, Gloves, Shoes & Golf Bag selectors
            LockerOptionRow(
                title = "HEADWEAR & HAT STYLE",
                options = hatStyles,
                selected = profile.hatStyle,
                onSelect = {
                    profile = profile.copy(hatStyle = it)
                    onUpdateGolfer(profile)
                }
            )
            LockerOptionRow(
                title = "GOLF SHOES",
                options = shoeStyles,
                selected = profile.shoeStyle,
                onSelect = {
                    profile = profile.copy(shoeStyle = it)
                    onUpdateGolfer(profile)
                }
            )
            LockerOptionRow(
                title = "TOUR GOLF BAG",
                options = golfBagStyles,
                selected = profile.golfBagStyle,
                onSelect = {
                    profile = profile.copy(golfBagStyle = it)
                    onUpdateGolfer(profile)
                }
            )
            LockerOptionRow(
                title = "GRIP GLOVE",
                options = gloveStyles,
                selected = profile.gloveStyle,
                onSelect = {
                    profile = profile.copy(gloveStyle = it)
                    onUpdateGolfer(profile)
                }
            )

            // Swing Archetype
            Text("BIOMECHANICAL SWING ARCHETYPE", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Black, color = colors.accentGold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                GolferSwingStyle.entries.forEach { style ->
                    val isSelected = profile.swingStyle == style
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) colors.surfaceElevated else colors.surface,
                        border = BorderStroke(
                            if (isSelected) 1.5.dp else 1.dp,
                            if (isSelected) colors.accentGold else colors.border
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                profile = profile.copy(swingStyle = style)
                                onUpdateGolfer(profile)
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(style.label, fontFamily = OutfitFontFamily, fontSize = 13.sp, fontWeight = FontWeight.Black, color = colors.textPrimary)
                                Text(
                                    "Power ${if (style.powerMod >= 0) "+${style.powerMod}" else style.powerMod}% • Control ${if (style.controlMod >= 0) "+${style.controlMod}" else style.controlMod}%",
                                    fontFamily = PlusJakartaSansFontFamily,
                                    fontSize = 11.sp,
                                    color = colors.accentInfo
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Selected", tint = colors.accentSuccess, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Apparel Color Pickers
            Text("TOUR VISOR & POLO KIT COLORS", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Black, color = colors.accentGold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Visor Trim:", fontFamily = PlusJakartaSansFontFamily, fontSize = 12.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    visorColors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable {
                                    profile = profile.copy(visorColorHex = hex)
                                    onUpdateGolfer(profile)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(hex))
                                    .border(
                                        width = if (profile.visorColorHex == hex) 3.dp else 1.dp,
                                        color = if (profile.visorColorHex == hex) colors.accentGold else colors.border,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Tour Polo:", fontFamily = PlusJakartaSansFontFamily, fontSize = 12.sp, color = colors.textSecondary, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    poloColors.forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .clickable {
                                    profile = profile.copy(poloColorHex = hex)
                                    onUpdateGolfer(profile)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(hex))
                                    .border(
                                        width = if (profile.poloColorHex == hex) 3.dp else 1.dp,
                                        color = if (profile.poloColorHex == hex) colors.accentGold else colors.border,
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Attribute Skill Points Allocation
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = colors.surface,
                border = BorderStroke(1.dp, colors.border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ATHLETE ATTRIBUTES", fontFamily = OutfitFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Black, color = colors.accentGold, letterSpacing = 1.sp)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = colors.accentSuccess.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "${profile.unspentSkillPoints} SKILL PTS",
                                fontFamily = OutfitFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentSuccess,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    AttributeSkillBar(
                        label = "Driving Power",
                        value = profile.powerAttribute,
                        canUpgrade = profile.unspentSkillPoints > 0 && profile.powerAttribute < 99,
                        onUpgrade = {
                            profile = profile.copy(
                                powerAttribute = (profile.powerAttribute + 2).coerceAtMost(99),
                                unspentSkillPoints = profile.unspentSkillPoints - 1
                            )
                            onUpdateGolfer(profile)
                        }
                    )

                    AttributeSkillBar(
                        label = "Iron Accuracy",
                        value = profile.accuracyAttribute,
                        canUpgrade = profile.unspentSkillPoints > 0 && profile.accuracyAttribute < 99,
                        onUpgrade = {
                            profile = profile.copy(
                                accuracyAttribute = (profile.accuracyAttribute + 2).coerceAtMost(99),
                                unspentSkillPoints = profile.unspentSkillPoints - 1
                            )
                            onUpdateGolfer(profile)
                        }
                    )

                    AttributeSkillBar(
                        label = "Aerodynamic Spin",
                        value = profile.spinControlAttribute,
                        canUpgrade = profile.unspentSkillPoints > 0 && profile.spinControlAttribute < 99,
                        onUpgrade = {
                            profile = profile.copy(
                                spinControlAttribute = (profile.spinControlAttribute + 2).coerceAtMost(99),
                                unspentSkillPoints = profile.unspentSkillPoints - 1
                            )
                            onUpdateGolfer(profile)
                        }
                    )

                    AttributeSkillBar(
                        label = "Green Putting Touch",
                        value = profile.puttingAttribute,
                        canUpgrade = profile.unspentSkillPoints > 0 && profile.puttingAttribute < 99,
                        onUpgrade = {
                            profile = profile.copy(
                                puttingAttribute = (profile.puttingAttribute + 2).coerceAtMost(99),
                                unspentSkillPoints = profile.unspentSkillPoints - 1
                            )
                            onUpdateGolfer(profile)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LockerOptionRow(
    title: String,
    options: List<String>,
    selected: String,
    onSelect: (String) -> Unit
) {
    val colors = ZenithTheme.colors
    Text(title, fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Black, color = colors.accentGold, letterSpacing = 1.sp)
    Spacer(modifier = Modifier.height(6.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { option ->
            val isSelected = selected == option
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isSelected) colors.forestCard else colors.surface,
                border = BorderStroke(1.dp, if (isSelected) colors.accentGold else colors.border),
                modifier = Modifier
                    .height(48.dp)
                    .clickable { onSelect(option) }
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(horizontal = 14.dp)
                ) {
                    Text(
                        text = option,
                        fontFamily = OutfitFontFamily,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                        color = if (isSelected) colors.accentGold else colors.textPrimary
                    )
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(14.dp))
}

@Composable
private fun AttributeSkillBar(
    label: String,
    value: Int,
    canUpgrade: Boolean,
    onUpgrade: () -> Unit
) {
    val colors = ZenithTheme.colors
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontFamily = PlusJakartaSansFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = colors.textPrimary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("$value / 99", fontFamily = OutfitFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Black, color = colors.accentInfo)
                if (canUpgrade) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(
                        onClick = onUpgrade,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = "Upgrade $label",
                            tint = colors.accentSuccess,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { value / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = colors.accentSuccess,
            trackColor = colors.surfaceElevated
        )
    }
}

@Composable
private fun GolferAvatarCanvas(
    visorColor: Color,
    poloColor: Color,
    skinColor: Color,
    hatStyle: String,
    isLeftHanded: Boolean,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(poloColor.copy(alpha = 0.35f), Color.Transparent)
            ),
            radius = w * 0.45f,
            center = Offset(cx, h * 0.55f)
        )

        // Torso / Tour Polo
        drawRoundRect(
            color = poloColor,
            topLeft = Offset(cx - 22f, h * 0.52f),
            size = Size(44f, 38f),
            cornerRadius = CornerRadius(12f, 12f)
        )

        // Head
        drawCircle(
            color = skinColor,
            radius = 15f,
            center = Offset(cx, h * 0.34f)
        )

        // Headwear
        when (hatStyle) {
            "Championship Cap" -> {
                drawArc(
                    color = visorColor,
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(cx - 16f, h * 0.16f),
                    size = Size(32f, 22f)
                )
                drawRoundRect(
                    color = visorColor,
                    topLeft = Offset(cx - 19f, h * 0.24f),
                    size = Size(38f, 6f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }
            "Bucket Hat" -> {
                drawRoundRect(
                    color = visorColor,
                    topLeft = Offset(cx - 13f, h * 0.17f),
                    size = Size(26f, 10f),
                    cornerRadius = CornerRadius(5f, 5f)
                )
                drawRoundRect(
                    color = visorColor,
                    topLeft = Offset(cx - 22f, h * 0.25f),
                    size = Size(44f, 5f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }
            "No Hat" -> {
                // Dark sculpted hair silhouette
                drawArc(
                    color = Color(0xFF263238),
                    startAngle = 180f,
                    sweepAngle = 180f,
                    useCenter = true,
                    topLeft = Offset(cx - 15f, h * 0.18f),
                    size = Size(30f, 16f)
                )
            }
            else -> {
                drawRoundRect(
                    color = visorColor,
                    topLeft = Offset(cx - 17f, h * 0.22f),
                    size = Size(34f, 8f),
                    cornerRadius = CornerRadius(4f, 4f)
                )
            }
        }

        // Golf Club shaft across shoulder
        val clubStart = if (isLeftHanded) Offset(cx + 18f, h * 0.65f) else Offset(cx - 18f, h * 0.65f)
        val clubEnd = if (isLeftHanded) Offset(cx - 24f, h * 0.16f) else Offset(cx + 24f, h * 0.16f)
        drawLine(
            color = Color(0xFFE0E0E0),
            start = clubStart,
            end = clubEnd,
            strokeWidth = 3.5f
        )
        drawCircle(
            color = Color(0xFFD6B45C),
            radius = 5.5f,
            center = clubEnd
        )
    }
}
