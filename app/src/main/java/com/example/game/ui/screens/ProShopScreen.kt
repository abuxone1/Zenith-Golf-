package com.example.game.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Upgrade
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.game.model.BallTracerStyle
import com.example.game.model.ClubType
import com.example.game.model.ClubUpgrade
import com.example.game.model.GolfBallSkin
import com.example.game.model.GolferProfile
import com.example.game.model.GolferStance
import com.example.game.progression.PlayBillingHelper
import com.example.game.progression.PlayBillingStatus
import com.example.game.viewmodel.GameUiState
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PlusJakartaSansFontFamily
import com.example.ui.theme.ZenithTheme

private data class ShopConfirmAction(
    val title: String,
    val description: String,
    val costCoins: Int,
    val onConfirm: () -> Unit
)

@Composable
fun ProShopScreen(
    uiState: GameUiState,
    onBack: () -> Unit,
    onUpgradeClub: (ClubType) -> Unit,
    onEquipBall: (String) -> Unit,
    onBuyBall: (String) -> Unit,
    onSelectTracer: (BallTracerStyle) -> Unit = {},
    onUpdateGolfer: (GolferProfile) -> Unit = {},
    onOpenCustomBallStudio: () -> Unit = {},
    onPurchaseAccessory: (String, Int, (GolferProfile) -> GolferProfile) -> Boolean = { _, _, updater ->
        onUpdateGolfer(updater(uiState.golferProfile))
        true
    },
    onGrantBillingCoins: (Int) -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler {
        onBack()
    }

    val colors = ZenithTheme.colors
    val context = LocalContext.current

    // 0 = Clubs, 1 = Golf Balls & Tracers, 2 = Outfits & Kit, 3 = Shop Bundles (Play Billing)
    var selectedTab by remember { mutableIntStateOf(0) }
    // Club sub-filter: 0 = All, 1 = Drivers, 2 = Fairway Woods, 3 = Long Irons, 4 = Short Irons, 5 = Wedges, 6 = Putters
    var clubCategoryFilter by remember { mutableIntStateOf(0) }
    var pendingConfirm by remember { mutableStateOf<ShopConfirmAction?>(null) }

    val billingHelper = remember(context) {
        PlayBillingHelper(context, onVerifiedCoinsGranted = onGrantBillingCoins)
    }
    DisposableEffect(billingHelper) {
        onDispose { billingHelper.release() }
    }
    val billingStatus by billingHelper.status.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .testTag("pro_shop_screen"),
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
                            .testTag("pro_shop_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = colors.textPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.padding(end = 8.dp)) {
                        Text(
                            text = "TOUR ARMORY & PRO SHOP",
                            fontFamily = OutfitFontFamily,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentGold,
                            letterSpacing = 1.2.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "EQUIPMENT & BUNDLES",
                            fontFamily = OutfitFontFamily,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Coin Balance Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = colors.surfaceElevated,
                    border = BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonetizationOn,
                            contentDescription = null,
                            tint = colors.accentGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${uiState.career.coins}",
                            fontFamily = OutfitFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentGold
                        )
                    }
                }
            }
        }

        // Main Equipment Tabs
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = colors.surface,
            contentColor = colors.accentGold,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = colors.accentGold
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        text = "CLUBS & UPGRADES",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = if (selectedTab == 0) colors.accentGold else colors.textMuted
                    )
                },
                modifier = Modifier.testTag("equipment_tab_clubs")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Text(
                        text = "GOLF BALLS",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = if (selectedTab == 1) colors.accentGold else colors.textMuted
                    )
                },
                modifier = Modifier.testTag("equipment_tab_balls")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = {
                    Text(
                        text = "OUTFITS & KIT",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = if (selectedTab == 2) colors.accentGold else colors.textMuted
                    )
                },
                modifier = Modifier.testTag("equipment_tab_outfits")
            )
            Tab(
                selected = selectedTab == 3,
                onClick = { selectedTab = 3 },
                text = {
                    Text(
                        text = "SHOP BUNDLES",
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 11.sp,
                        color = if (selectedTab == 3) colors.accentGold else colors.textMuted
                    )
                },
                modifier = Modifier.testTag("equipment_tab_bundles")
            )
        }

        Box(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize()
        ) {
            when (selectedTab) {
                0 -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        val categories = listOf(
                            "ALL BAG",
                            "DRIVERS",
                            "FAIRWAY WOODS",
                            "LONG IRONS",
                            "SHORT IRONS",
                            "WEDGES",
                            "PUTTERS"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            categories.forEachIndexed { idx, catLabel ->
                                val isSelected = clubCategoryFilter == idx
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) colors.accentGold else colors.surface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (isSelected) colors.accentGold else colors.border
                                    ),
                                    modifier = Modifier
                                        .height(48.dp)
                                        .clickable { clubCategoryFilter = idx }
                                        .testTag("club_filter_$idx")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 14.dp)
                                    ) {
                                        Text(
                                            text = catLabel,
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Black,
                                            color = if (isSelected) colors.onGold else colors.textPrimary
                                        )
                                    }
                                }
                            }
                        }

                        val filteredClubs = uiState.clubs.filter { club ->
                            when (clubCategoryFilter) {
                                1 -> club.clubType == ClubType.DRIVER
                                2 -> club.clubType == ClubType.WOOD_3
                                3 -> club.clubType == ClubType.IRON_4
                                4 -> club.clubType == ClubType.IRON_7 || club.clubType == ClubType.IRON_9
                                5 -> club.clubType == ClubType.PITCHING_WEDGE || club.clubType == ClubType.SAND_WEDGE
                                6 -> club.clubType == ClubType.PUTTER
                                else -> true
                            }
                        }

                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredClubs) { club ->
                                ClubUpgradeCard(
                                    club = club,
                                    userCoins = uiState.career.coins,
                                    onUpgrade = {
                                        pendingConfirm = ShopConfirmAction(
                                            title = "Upgrade ${club.clubType.displayName}?",
                                            description = "Advance from Level ${club.level} to Level ${club.level + 1} for +Power, +Control, and +Spin.",
                                            costCoins = club.upgradeCost,
                                            onConfirm = { onUpgradeClub(club.clubType) }
                                        )
                                    }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(20.dp)) }
                        }
                    }
                }

                1 -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Spacer(modifier = Modifier.height(12.dp))
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.forestCard),
                                border = BorderStroke(1.5.dp, colors.accentGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "AI CUSTOM BALL FORGE",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colors.accentGold
                                        )
                                        Text(
                                            text = "Generate custom golf balls with aerodynamic boosts using AI.",
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 11.sp,
                                            color = colors.onForestSecondary
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Button(
                                        onClick = onOpenCustomBallStudio,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colors.accentGold,
                                            contentColor = colors.onGold
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(48.dp)
                                    ) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("FORGE", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Black)
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "TOUR GOLF BALLS",
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentGold,
                                letterSpacing = 1.sp
                            )
                        }

                        items(uiState.balls) { ball ->
                            BallSkinCard(
                                ball = ball,
                                userCoins = uiState.career.coins,
                                onEquip = { onEquipBall(ball.id) },
                                onBuy = {
                                    pendingConfirm = ShopConfirmAction(
                                        title = "Unlock ${ball.name}?",
                                        description = ball.description,
                                        costCoins = ball.costCoins,
                                        onConfirm = { onBuyBall(ball.id) }
                                    )
                                }
                            )
                        }

                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "TOPTRACER FLIGHT TRAILS",
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentSuccess,
                                letterSpacing = 1.sp
                            )
                        }

                        items(BallTracerStyle.entries) { tracer ->
                            BallTracerCard(
                                tracer = tracer,
                                isEquipped = uiState.equippedTracer == tracer,
                                onEquip = { onSelectTracer(tracer) }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }

                2 -> {
                    val profile = uiState.golferProfile
                    val unlockedIds = uiState.unlockedAccessoryIds
                    val visorOptions = listOf(
                        "Apex Emerald" to 0xFF123D2A,
                        "Championship Gold" to 0xFFD6B45C,
                        "Tour White" to 0xFFFFFFFF,
                        "Cyber Cyan" to 0xFF00E5FF,
                        "Crimson Pro" to 0xFFFF5252
                    )
                    val poloOptions = listOf(
                        "Championship Green" to 0xFF123D2A,
                        "Deep Forest Green" to 0xFF09271C,
                        "Tour Cyan" to 0xFF00B8D4,
                        "Royal Indigo" to 0xFF311B92,
                        "Dark Charcoal" to 0xFF263238
                    )
                    val hatStyles = listOf(
                        Triple("hat_visor", "Tour Visor", 0),
                        Triple("hat_cap", "Championship Cap", 120),
                        Triple("hat_bucket", "Bucket Hat", 200)
                    )
                    val shoeOptions = listOf(
                        Triple("shoe_white", "Spikeless Pro White", 0),
                        Triple("shoe_obsidian", "Tour Oxford Obsidian", 180),
                        Triple("shoe_emerald", "Emerald Turf Spikes", 260)
                    )
                    val bagOptions = listOf(
                        Triple("bag_forest", "Forest & Gold Tour", 0),
                        Triple("bag_carbon", "Stealth Carbon Bag", 300),
                        Triple("bag_navy", "Royal Navy Staff Bag", 420)
                    )
                    val gloveOptions = listOf(
                        "Carbon Fiber Pro",
                        "Cabretta Leather White",
                        "Gold Grip Tour",
                        "Emerald All-Weather"
                    )

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                border = BorderStroke(1.5.dp, colors.accentGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(84.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(colors.forestCard)
                                            .border(2.dp, colors.accentGold, RoundedCornerShape(16.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MiniGolferAvatarCanvas(
                                            visorColor = Color(profile.visorColorHex),
                                            poloColor = Color(profile.poloColorHex),
                                            isLeftHanded = profile.stance == GolferStance.LEFT_HANDED,
                                            modifier = Modifier.fillMaxSize(),
                                            skinColor = Color(profile.skinToneHex)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(14.dp))
                                    Column {
                                        Text(
                                            text = "ACTIVE TOUR KIT",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colors.accentGold,
                                            letterSpacing = 1.sp
                                        )
                                        Text(
                                            text = profile.golferName,
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colors.textPrimary
                                        )
                                        Text(
                                            text = "${profile.hatStyle} • ${profile.shoeStyle}",
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 12.sp,
                                            color = colors.accentSuccess
                                        )
                                        Text(
                                            text = "Bag: ${profile.golfBagStyle} • Glove: ${profile.gloveStyle}",
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 11.sp,
                                            color = colors.textSecondary
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            Text("HEADWEAR & HATS", fontFamily = OutfitFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Black, color = colors.accentGold, letterSpacing = 1.sp)
                        }
                        items(hatStyles) { (id, title, cost) ->
                            val isOwned = cost == 0 || unlockedIds.contains(id)
                            val isEquipped = profile.hatStyle == title
                            OutfitItemCard(
                                title = title,
                                subtitle = if (isOwned) "Unlocked headwear style" else "Championship sun protection ($cost Coins)",
                                swatchColor = Color(profile.visorColorHex),
                                isEquipped = isEquipped,
                                costCoins = if (isOwned) 0 else cost,
                                canAfford = uiState.career.coins >= cost,
                                onEquip = {
                                    if (isOwned) {
                                        onUpdateGolfer(profile.copy(hatStyle = title))
                                    } else {
                                        pendingConfirm = ShopConfirmAction(
                                            title = "Unlock $title?",
                                            description = "Equip this tour headwear on your golfer across all modes.",
                                            costCoins = cost,
                                            onConfirm = {
                                                onPurchaseAccessory(id, cost) { it.copy(hatStyle = title) }
                                            }
                                        )
                                    }
                                }
                            )
                        }

                        item {
                            Text("TOUR GOLF SHOES", fontFamily = OutfitFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Black, color = colors.accentInfo, letterSpacing = 1.sp)
                        }
                        items(shoeOptions) { (id, title, cost) ->
                            val isOwned = cost == 0 || unlockedIds.contains(id)
                            val isEquipped = profile.shoeStyle == title
                            OutfitItemCard(
                                title = title,
                                subtitle = if (isOwned) "Tour traction footwear" else "Pro grip turf spikes ($cost Coins)",
                                swatchColor = colors.accentInfo,
                                isEquipped = isEquipped,
                                costCoins = if (isOwned) 0 else cost,
                                canAfford = uiState.career.coins >= cost,
                                onEquip = {
                                    if (isOwned) {
                                        onUpdateGolfer(profile.copy(shoeStyle = title))
                                    } else {
                                        pendingConfirm = ShopConfirmAction(
                                            title = "Unlock $title?",
                                            description = "Add championship golf footwear to your locker.",
                                            costCoins = cost,
                                            onConfirm = {
                                                onPurchaseAccessory(id, cost) { it.copy(shoeStyle = title) }
                                            }
                                        )
                                    }
                                }
                            )
                        }

                        item {
                            Text("STAFF GOLF BAGS", fontFamily = OutfitFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Black, color = colors.accentSuccess, letterSpacing = 1.sp)
                        }
                        items(bagOptions) { (id, title, cost) ->
                            val isOwned = cost == 0 || unlockedIds.contains(id)
                            val isEquipped = profile.golfBagStyle == title
                            OutfitItemCard(
                                title = title,
                                subtitle = if (isOwned) "Equipped Tour Staff Bag" else "Signature pro tour carry bag ($cost Coins)",
                                swatchColor = colors.accentGold,
                                isEquipped = isEquipped,
                                costCoins = if (isOwned) 0 else cost,
                                canAfford = uiState.career.coins >= cost,
                                onEquip = {
                                    if (isOwned) {
                                        onUpdateGolfer(profile.copy(golfBagStyle = title))
                                    } else {
                                        pendingConfirm = ShopConfirmAction(
                                            title = "Unlock $title?",
                                            description = "Unlock and equip this Tour Staff Bag.",
                                            costCoins = cost,
                                            onConfirm = {
                                                onPurchaseAccessory(id, cost) { it.copy(golfBagStyle = title) }
                                            }
                                        )
                                    }
                                }
                            )
                        }

                        item {
                            Text("CHAMPIONSHIP POLO JERSEYS", fontFamily = OutfitFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Black, color = colors.accentGold, letterSpacing = 1.sp)
                        }
                        items(poloOptions) { (name, hex) ->
                            OutfitItemCard(
                                title = name,
                                subtitle = "Moisture-wicking tour performance polo",
                                swatchColor = Color(hex),
                                isEquipped = profile.poloColorHex == hex,
                                onEquip = { onUpdateGolfer(profile.copy(poloColorHex = hex)) }
                            )
                        }

                        item {
                            Text("VISOR TRIM & TOUR GLOVES", fontFamily = OutfitFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Black, color = colors.accentSuccess, letterSpacing = 1.sp)
                        }
                        items(visorOptions) { (name, hex) ->
                            OutfitItemCard(
                                title = "$name Trim",
                                subtitle = "Anti-glare championship trim",
                                swatchColor = Color(hex),
                                isEquipped = profile.visorColorHex == hex,
                                onEquip = { onUpdateGolfer(profile.copy(visorColorHex = hex)) }
                            )
                        }
                        items(gloveOptions) { glove ->
                            OutfitItemCard(
                                title = glove,
                                subtitle = "Enhanced clubhead grip & wet-weather control",
                                swatchColor = colors.accentGold,
                                isEquipped = profile.gloveStyle == glove,
                                onEquip = { onUpdateGolfer(profile.copy(gloveStyle = glove)) }
                            )
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }

                3 -> {
                    // Shop Bundles & Google Play Billing Integration
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.forestCard),
                                border = BorderStroke(1.5.dp, colors.accentGold),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = null,
                                        tint = colors.accentGold,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "GOOGLE PLAY STORE BUNDLES",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Black,
                                            color = colors.accentGold
                                        )
                                        Text(
                                            text = when (val st = billingStatus) {
                                                PlayBillingStatus.Checking -> "Connecting to Google Play Billing Library..."
                                                is PlayBillingStatus.Available -> "${st.products.size} Play Console INAPP packages verified."
                                                is PlayBillingStatus.Unavailable -> st.reason
                                            },
                                            fontFamily = PlusJakartaSansFontFamily,
                                            fontSize = 11.sp,
                                            color = colors.onForestSecondary,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }
                            }
                        }

                        when (val st = billingStatus) {
                            is PlayBillingStatus.Available -> {
                                items(st.products) { product ->
                                    val priceLabel = product.oneTimePurchaseOfferDetails?.formattedPrice ?: "View in Play"
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                                        border = BorderStroke(1.dp, colors.accentGold),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = product.name,
                                                    fontFamily = OutfitFontFamily,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = colors.textPrimary
                                                )
                                                Text(
                                                    text = product.description,
                                                    fontFamily = PlusJakartaSansFontFamily,
                                                    fontSize = 11.sp,
                                                    color = colors.textSecondary
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Button(
                                                onClick = {
                                                    val act = context as? Activity
                                                    if (act != null) {
                                                        billingHelper.launchPurchase(act, product)
                                                    }
                                                },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = colors.accentGold,
                                                    contentColor = colors.onGold
                                                ),
                                                shape = RoundedCornerShape(10.dp),
                                                modifier = Modifier.height(48.dp)
                                            ) {
                                                Text(priceLabel, fontFamily = OutfitFontFamily, fontSize = 12.sp, fontWeight = FontWeight.Black)
                                            }
                                        }
                                    }
                                }
                            }
                            else -> {
                                item {
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                                        border = BorderStroke(1.dp, colors.border),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(28.dp))
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column {
                                                Text(
                                                    text = "Earned Tour Economy Active",
                                                    fontFamily = OutfitFontFamily,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = colors.textPrimary
                                                )
                                                Text(
                                                    text = "Earn Coins & Mulligans from Championships, Daily Spin Roulette, Missions, and Target Shootout. Live Google Play Store SKUs activate automatically once published in Play Console.",
                                                    fontFamily = PlusJakartaSansFontFamily,
                                                    fontSize = 12.sp,
                                                    color = colors.textSecondary,
                                                    lineHeight = 17.sp
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            Text(
                                text = "TOUR KIT VALUE BUNDLES (EARNED COINS)",
                                fontFamily = OutfitFontFamily,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentGold,
                                letterSpacing = 1.sp
                            )
                        }

                        item {
                            val proKitOwned = uiState.unlockedAccessoryIds.contains("bundle_championship_pro_kit")
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = colors.surface),
                                border = BorderStroke(1.5.dp, if (proKitOwned) colors.accentSuccess else colors.accentGold.copy(alpha = 0.6f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                            Icon(Icons.Default.ShoppingBag, contentDescription = null, tint = colors.accentGold, modifier = Modifier.size(24.dp))
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = "Championship Masters Kit Bundle",
                                                    fontFamily = OutfitFontFamily,
                                                    fontSize = 15.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = colors.textPrimary
                                                )
                                                Text(
                                                    text = "Includes Championship Cap + Emerald Turf Spikes + Stealth Carbon Bag",
                                                    fontFamily = PlusJakartaSansFontFamily,
                                                    fontSize = 11.sp,
                                                    color = colors.textSecondary
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = {
                                            if (!proKitOwned) {
                                                pendingConfirm = ShopConfirmAction(
                                                    title = "Purchase Masters Kit Bundle?",
                                                    description = "Unlocks Championship Cap, Emerald Turf Spikes, and Stealth Carbon Bag together.",
                                                    costCoins = 450,
                                                    onConfirm = {
                                                        onPurchaseAccessory("bundle_championship_pro_kit", 450) {
                                                            it.copy(
                                                                hatStyle = "Championship Cap",
                                                                shoeStyle = "Emerald Turf Spikes",
                                                                golfBagStyle = "Stealth Carbon Bag"
                                                            )
                                                        }
                                                    }
                                                )
                                            }
                                        },
                                        enabled = !proKitOwned && uiState.career.coins >= 450,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = colors.accentGold,
                                            contentColor = colors.onGold
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .testTag("buy_masters_kit_bundle_btn")
                                    ) {
                                        Text(
                                            text = if (proKitOwned) "BUNDLE UNLOCKED & EQUIPPED" else "UNLOCK BUNDLE • 450 COINS",
                                            fontFamily = OutfitFontFamily,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }

                        item { Spacer(modifier = Modifier.height(24.dp)) }
                    }
                }
            }
        }
    }

    pendingConfirm?.let { confirm ->
        AlertDialog(
            onDismissRequest = { pendingConfirm = null },
            containerColor = colors.surfaceElevated,
            title = {
                Text(
                    text = confirm.title,
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Black,
                    color = colors.textPrimary
                )
            },
            text = {
                Column {
                    Text(
                        text = confirm.description,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 13.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Cost: ${confirm.costCoins} Tour Coins",
                        fontFamily = OutfitFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accentGold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirm.onConfirm()
                        pendingConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.accentGold,
                        contentColor = colors.onGold
                    ),
                    modifier = Modifier.testTag("shop_confirm_purchase_btn")
                ) {
                    Text("CONFIRM", fontFamily = OutfitFontFamily, fontWeight = FontWeight.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingConfirm = null }) {
                    Text("CANCEL", color = colors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun OutfitItemCard(
    title: String,
    subtitle: String,
    swatchColor: Color,
    isEquipped: Boolean,
    costCoins: Int = 0,
    canAfford: Boolean = true,
    onEquip: () -> Unit
) {
    val colors = ZenithTheme.colors
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(
            if (isEquipped) 1.5.dp else 1.dp,
            if (isEquipped) colors.accentGold else colors.border
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(swatchColor.copy(alpha = 0.25f))
                        .border(2.dp, swatchColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Checkroom,
                        contentDescription = null,
                        tint = swatchColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        fontFamily = OutfitFontFamily,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = subtitle,
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            if (isEquipped) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = colors.accentGold.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, colors.accentGold)
                ) {
                    Text(
                        text = "EQUIPPED",
                        fontFamily = OutfitFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accentGold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            } else {
                Button(
                    onClick = onEquip,
                    enabled = costCoins == 0 || canAfford,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (costCoins > 0) colors.accentGold else colors.forestCard,
                        contentColor = if (costCoins > 0) colors.onGold else colors.onForestCard
                    ),
                    border = BorderStroke(1.dp, colors.accentGold),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(
                        text = if (costCoins > 0) "$costCoins C" else "EQUIP",
                        fontFamily = OutfitFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
fun ClubUpgradeCard(
    club: ClubUpgrade,
    userCoins: Int,
    onUpgrade: () -> Unit
) {
    val colors = ZenithTheme.colors
    val canUpgrade = club.level < club.maxLevel && userCoins >= club.upgradeCost
    val isMaxLevel = club.level >= club.maxLevel
    val clubCategoryBadge = when (club.clubType) {
        ClubType.DRIVER -> "TOUR DRIVER"
        ClubType.WOOD_3 -> "FAIRWAY WOOD"
        ClubType.IRON_4 -> "LONG IRON"
        ClubType.IRON_7, ClubType.IRON_9 -> "SHORT IRON"
        ClubType.PITCHING_WEDGE, ClubType.SAND_WEDGE -> "SPIN WEDGE"
        ClubType.PUTTER -> "BLADE PUTTER"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("club_card_${club.clubType.shortName.lowercase()}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(1.dp, colors.border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(colors.forestCard, RoundedCornerShape(12.dp))
                            .border(1.dp, colors.accentGold.copy(alpha = 0.6f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = club.clubType.shortName,
                            fontFamily = OutfitFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentGold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = club.clubType.displayName,
                                fontFamily = OutfitFontFamily,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = colors.textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = colors.accentGold.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    text = clubCategoryBadge,
                                    fontFamily = OutfitFontFamily,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentGold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Level ${club.level}/${club.maxLevel} • Carry ${club.clubType.baseDistanceYds.toInt()}–${club.clubType.maxDistanceYds.toInt()} YDS • Spin ${club.clubType.baseBackspinRpm} RPM",
                            fontFamily = PlusJakartaSansFontFamily,
                            fontSize = 11.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onUpgrade,
                    enabled = canUpgrade,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (canUpgrade) colors.accentGold else colors.surfaceElevated,
                        contentColor = if (canUpgrade) colors.onGold else colors.textMuted
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("upgrade_${club.clubType.shortName.lowercase()}_btn")
                ) {
                    if (isMaxLevel) {
                        Text("MAX", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Black)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Upgrade, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${club.upgradeCost} C",
                                fontFamily = OutfitFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            StatBar(label = "POWER", value = club.powerStat, color = colors.accentSuccess)
            Spacer(modifier = Modifier.height(6.dp))
            StatBar(label = "CONTROL", value = club.controlStat, color = colors.accentInfo)
            Spacer(modifier = Modifier.height(6.dp))
            StatBar(label = "SPIN", value = club.spinStat, color = colors.accentGold)
        }
    }
}

@Composable
fun StatBar(label: String, value: Int, color: Color) {
    val colors = ZenithTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            fontFamily = OutfitFontFamily,
            fontSize = 9.sp,
            fontWeight = FontWeight.Black,
            color = colors.textMuted,
            modifier = Modifier.width(58.dp),
            letterSpacing = 0.5.sp
        )
        LinearProgressIndicator(
            progress = { (value / 100f).coerceIn(0f, 1f) },
            color = color,
            trackColor = colors.surfaceElevated,
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$value",
            fontFamily = OutfitFontFamily,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(26.dp)
        )
    }
}

@Composable
fun BallSkinCard(
    ball: GolfBallSkin,
    userCoins: Int,
    onEquip: () -> Unit,
    onBuy: () -> Unit
) {
    val colors = ZenithTheme.colors
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ball_card_${ball.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = if (ball.isEquipped) BorderStroke(1.5.dp, colors.accentGold) else BorderStroke(1.dp, colors.border)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(ball.glowColor.copy(alpha = 0.2f), CircleShape)
                    .border(2.dp, ball.glowColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SportsGolf,
                    contentDescription = null,
                    tint = ball.glowColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = ball.name,
                    fontFamily = OutfitFontFamily,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.textPrimary
                )
                Text(
                    text = ball.description,
                    fontFamily = PlusJakartaSansFontFamily,
                    fontSize = 11.sp,
                    color = colors.textSecondary,
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (ball.powerBonusPercent > 0) {
                        Text(
                            text = "+${ball.powerBonusPercent}% Power",
                            fontFamily = OutfitFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentSuccess
                        )
                    }
                    if (ball.windResistancePercent > 0) {
                        Text(
                            text = "+${ball.windResistancePercent}% Wind Res.",
                            fontFamily = OutfitFontFamily,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.accentInfo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (ball.isEquipped) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = colors.accentGold.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, colors.accentGold)
                ) {
                    Text(
                        text = "EQUIPPED",
                        fontFamily = OutfitFontFamily,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.accentGold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
                }
            } else if (ball.isUnlocked) {
                OutlinedButton(
                    onClick = onEquip,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, colors.accentGold),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("equip_ball_${ball.id}")
                ) {
                    Text("EQUIP", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = colors.accentGold)
                }
            } else {
                Button(
                    onClick = onBuy,
                    enabled = userCoins >= ball.costCoins,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = colors.accentGold, contentColor = colors.onGold),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("buy_ball_${ball.id}")
                ) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${ball.costCoins}", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun BallTracerCard(
    tracer: BallTracerStyle,
    isEquipped: Boolean,
    onEquip: () -> Unit
) {
    val colors = ZenithTheme.colors
    val color = Color(tracer.colorHex)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tracer_card_${tracer.name}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        border = BorderStroke(
            1.5.dp,
            if (isEquipped) color else colors.border
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.25f))
                        .border(1.5.dp, color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsGolf,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = tracer.label,
                        fontFamily = OutfitFontFamily,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Black,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Toptracer in-flight glowing ribbon & sparks",
                        fontFamily = PlusJakartaSansFontFamily,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }
            }

            if (isEquipped) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, color)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = color, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "EQUIPPED", fontFamily = OutfitFontFamily, fontSize = 10.sp, fontWeight = FontWeight.Black, color = color)
                    }
                }
            } else {
                Button(
                    onClick = onEquip,
                    colors = ButtonDefaults.buttonColors(containerColor = color, contentColor = Color.Black),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("equip_tracer_${tracer.name}")
                ) {
                    Text(text = "EQUIP", fontFamily = OutfitFontFamily, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}
