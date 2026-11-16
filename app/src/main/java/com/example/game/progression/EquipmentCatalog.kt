package com.example.game.progression

import androidx.compose.ui.graphics.Color
import com.example.game.model.ClubType
import com.example.game.model.ClubUpgrade
import com.example.game.model.GolfBallSkin

object EquipmentCatalog {

    fun getDefaultBalls(): List<GolfBallSkin> {
        return listOf(
            GolfBallSkin(
                id = "ball_classic",
                name = "Classic Tour White",
                description = "Standard tournament golf ball with balanced flight and clean feel.",
                glowColor = Color(0xFFFFFFFF),
                powerBonusPercent = 0,
                windResistancePercent = 0,
                isEquipped = true,
                isUnlocked = true,
                costCoins = 0
            ),
            GolfBallSkin(
                id = "ball_neon",
                name = "Zenith Neon Pulse",
                description = "High-visibility fluorescent ball. Emits glowing trail particles.",
                glowColor = Color(0xFF00E676),
                powerBonusPercent = 4,
                windResistancePercent = 5,
                isEquipped = false,
                isUnlocked = true,
                costCoins = 300
            ),
            GolfBallSkin(
                id = "ball_cyan_cyber",
                name = "Cyber Matrix Blue",
                description = "Aerodynamic dimple pattern engineered for wind penetration.",
                glowColor = Color(0xFF00E5FF),
                powerBonusPercent = 6,
                windResistancePercent = 15,
                isEquipped = false,
                isUnlocked = false,
                costCoins = 650
            ),
            GolfBallSkin(
                id = "ball_gold_zenith",
                name = "Golden Zenith Trophy",
                description = "Pure gilded tour ball. Boosts carry distance by +10% with gold sparkle trail.",
                glowColor = Color(0xFFFFD600),
                powerBonusPercent = 10,
                windResistancePercent = 10,
                isEquipped = false,
                isUnlocked = false,
                costCoins = 1200
            ),
            GolfBallSkin(
                id = "ball_solar_flare",
                name = "Solar Flare Ruby",
                description = "Blazing thermal core with maximum kinetic rebound and fire trail.",
                glowColor = Color(0xFFFF3D00),
                powerBonusPercent = 12,
                windResistancePercent = 8,
                isEquipped = false,
                isUnlocked = false,
                costCoins = 2000
            )
        )
    }

    fun getDefaultClubUpgrades(): List<ClubUpgrade> {
        return listOf(
            ClubUpgrade(ClubType.DRIVER, level = 2, powerStat = 78, controlStat = 70, spinStat = 65),
            ClubUpgrade(ClubType.WOOD_3, level = 1, powerStat = 72, controlStat = 75, spinStat = 68),
            ClubUpgrade(ClubType.IRON_4, level = 1, powerStat = 75, controlStat = 77, spinStat = 72),
            ClubUpgrade(ClubType.IRON_7, level = 2, powerStat = 74, controlStat = 80, spinStat = 75),
            ClubUpgrade(ClubType.IRON_9, level = 1, powerStat = 72, controlStat = 83, spinStat = 82),
            ClubUpgrade(ClubType.PITCHING_WEDGE, level = 2, powerStat = 70, controlStat = 85, spinStat = 88),
            ClubUpgrade(ClubType.SAND_WEDGE, level = 1, powerStat = 68, controlStat = 88, spinStat = 94),
            ClubUpgrade(ClubType.PUTTER, level = 1, powerStat = 65, controlStat = 90, spinStat = 60)
        )
    }
}
