package com.example.game.progression

import android.content.Context
import com.example.game.model.AiDifficultyTier
import com.example.game.model.CameraViewMode
import com.example.game.model.ClubType
import com.example.game.model.ClubUpgrade
import com.example.game.model.CustomHoleBlueprint
import com.example.game.model.DailyChallenge
import com.example.game.model.GolfBallSkin
import com.example.game.model.GolferProfile
import com.example.game.model.GolferStance
import com.example.game.model.GolferSwingStyle
import com.example.game.model.GraphicsQualityTier
import com.example.game.model.PlayAchievement
import com.example.game.model.PlayConsoleSettings
import com.example.game.model.PlayerCareer
import com.example.game.model.TourPassState
import com.example.game.model.ZenithThemeMode

/**
 * Non-destructive on-device persistence for Zenith Golf 2.0 player progression,
 * settings, custom hole architect blueprints, daily challenges, and achievements.
 * Guarantees zero progress loss across app restarts or offline sessions, and merges
 * monotonically (maxOf / set-union) with Cloud Firestore records.
 */
object LocalProgressStorage {

    private const val PREFS_NAME = "zenith_golf_local_progress_v2"

    data class LocalSnapshot(
        val career: PlayerCareer,
        val balls: List<GolfBallSkin>,
        val clubs: List<ClubUpgrade>,
        val tourPassState: TourPassState,
        val golferProfile: GolferProfile,
        val playSettings: PlayConsoleSettings,
        val customHoleBlueprint: CustomHoleBlueprint,
        val completedChallengeIds: Set<String>,
        val claimedChallengeIds: Set<String>,
        val unlockedAchievementIds: Set<String>,
        val dailyLoginStreak: Int = 1,
        val lastDailyClaimDayEpoch: Long = 0L,
        val unlockedAccessoryIds: Set<String> = emptySet()
    )

    fun clearSnapshot(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    fun saveSnapshot(
        context: Context,
        career: PlayerCareer,
        balls: List<GolfBallSkin>,
        clubs: List<ClubUpgrade>,
        tourPass: TourPassState,
        golferProfile: GolferProfile,
        playSettings: PlayConsoleSettings = PlayConsoleSettings(),
        customHoleBlueprint: CustomHoleBlueprint = CustomHoleBlueprint(),
        dailyChallenges: List<DailyChallenge> = emptyList(),
        achievements: List<PlayAchievement> = emptyList(),
        dailyLoginStreak: Int = 1,
        lastDailyClaimDayEpoch: Long = 0L,
        unlockedAccessoryIds: Set<String> = emptySet()
    ) {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val unlockedIds = balls.filter { it.isUnlocked }.map { it.id }.toSet()
        val completedChallenges = dailyChallenges.filter { it.isCompleted }.map { it.id }.toSet()
        val claimedChallenges = dailyChallenges.filter { it.isClaimed }.map { it.id }.toSet()
        val unlockedAchievements = achievements.filter { it.isUnlocked }.map { it.id }.toSet()

        val editor = prefs.edit()
            .putInt("coins", career.coins)
            .putInt("trophies", career.trophies)
            .putInt("mulligansRemaining", career.mulligansRemaining)
            .putInt("roundsPlayed", career.roundsPlayed)
            .putInt("holesPlayed", career.holesPlayed)
            .putInt("birdies", career.birdies)
            .putInt("eagles", career.eagles)
            .putInt("aces", career.aces)
            .putFloat("longestDriveYds", career.longestDriveYds)
            .putInt("greensInRegulationCount", career.greensInRegulationCount)
            .putInt("tourXp", career.tourXp)
            .putInt("shootoutHighScore", career.shootoutHighScore)
            .putString("equippedBallId", career.equippedBallId)
            .putString("equippedTracer", career.equippedTracer)
            .putInt("matchPlayWins", career.matchPlayWins)
            .putInt("matchPlayLosses", career.matchPlayLosses)
            .putStringSet("unlockedBallIds", unlockedIds)
            .putInt("pass_currentXp", tourPass.currentXp)
            .putBoolean("pass_isVipUnlocked", tourPass.isVipUnlocked)
            .putString("pass_claimedFree", tourPass.claimedFreeTiers.joinToString(","))
            .putString("pass_claimedVip", tourPass.claimedVipTiers.joinToString(","))
            // Golfer Customization
            .putString("golfer_name", golferProfile.golferName)
            .putString("golfer_countryTag", golferProfile.countryTag)
            .putString("golfer_stance", golferProfile.stance.name)
            .putString("golfer_swingStyle", golferProfile.swingStyle.name)
            .putLong("golfer_visorColorHex", golferProfile.visorColorHex)
            .putLong("golfer_poloColorHex", golferProfile.poloColorHex)
            .putString("golfer_gloveStyle", golferProfile.gloveStyle)
            .putInt("golfer_powerAttribute", golferProfile.powerAttribute)
            .putInt("golfer_accuracyAttribute", golferProfile.accuracyAttribute)
            .putInt("golfer_spinControlAttribute", golferProfile.spinControlAttribute)
            .putInt("golfer_puttingAttribute", golferProfile.puttingAttribute)
            .putInt("golfer_unspentSkillPoints", golferProfile.unspentSkillPoints)
            .putLong("golfer_skinToneHex", golferProfile.skinToneHex)
            .putString("golfer_hatStyle", golferProfile.hatStyle)
            .putString("golfer_shoeStyle", golferProfile.shoeStyle)
            .putString("golfer_golfBagStyle", golferProfile.golfBagStyle)
            // Settings
            .putBoolean("settings_isSoundEnabled", playSettings.isSoundEnabled)
            .putFloat("settings_soundVolume", playSettings.soundVolume)
            .putBoolean("settings_isHapticsEnabled", playSettings.isHapticsEnabled)
            .putBoolean("settings_isCaddieAudioTipsEnabled", playSettings.isCaddieAudioTipsEnabled)
            .putBoolean("settings_isHighFpsMode", playSettings.isHighFpsMode)
            .putBoolean("settings_isPuttingGridVisible", playSettings.isPuttingGridVisible)
            .putBoolean("settings_isSlopeBreakRadarVisible", playSettings.isSlopeBreakRadarVisible)
            .putBoolean("settings_isCloudSyncEnabled", playSettings.isCloudSyncEnabled)
            .putString("settings_graphicsTier", playSettings.graphicsTier.name)
            .putString("settings_aiDifficulty", playSettings.aiDifficulty.name)
            .putString("settings_themeMode", playSettings.themeMode.name)
            .putFloat("settings_musicVolume", playSettings.musicVolume)
            .putFloat("settings_controlSensitivity", playSettings.controlSensitivity)
            .putString("settings_preferredCameraMode", playSettings.preferredCameraMode.name)
            .putString("settings_languageTag", playSettings.languageTag)
            .putBoolean("settings_notificationsTourEvents", playSettings.notificationsTourEvents)
            .putBoolean("settings_notificationsDailyWheel", playSettings.notificationsDailyWheel)
            .putBoolean("settings_hasCompletedOnboarding", playSettings.hasCompletedOnboarding)
            // Course Architect Blueprint
            .putString("arch_holeName", customHoleBlueprint.holeName)
            .putInt("arch_par", customHoleBlueprint.par)
            .putInt("arch_totalYardage", customHoleBlueprint.totalYardage)
            .putString("arch_fairwayShape", customHoleBlueprint.fairwayShape)
            .putString("arch_greenStyle", customHoleBlueprint.greenStyle)
            .putInt("arch_sandBunkerCount", customHoleBlueprint.sandBunkerCount)
            .putBoolean("arch_hasWaterLake", customHoleBlueprint.hasWaterLake)
            .putString("arch_pinPositionStyle", customHoleBlueprint.pinPositionStyle)
            .putString("arch_slopeDifficulty", customHoleBlueprint.slopeDifficulty)
            // Challenges, Achievements & Streaks
            .putStringSet("completedChallengeIds", completedChallenges)
            .putStringSet("claimedChallengeIds", claimedChallenges)
            .putStringSet("unlockedAchievementIds", unlockedAchievements)
            .putInt("dailyLoginStreak", dailyLoginStreak)
            .putLong("lastDailyClaimDayEpoch", lastDailyClaimDayEpoch)
            .putStringSet("unlockedAccessoryIds", unlockedAccessoryIds)

        clubs.forEach { upgrade ->
            editor.putInt("club_lvl_${upgrade.clubType.name}", upgrade.level)
        }

        editor.apply()
    }

    fun loadSnapshot(
        context: Context,
        defaultBalls: List<GolfBallSkin> = EquipmentCatalog.getDefaultBalls(),
        defaultClubs: List<ClubUpgrade> = EquipmentCatalog.getDefaultClubUpgrades()
    ): LocalSnapshot? {
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (!prefs.contains("coins") && !prefs.contains("trophies") && !prefs.contains("settings_isSoundEnabled")) return null

        val defaultCareer = PlayerCareer()
        val career = defaultCareer.copy(
            coins = maxOf(0, prefs.getInt("coins", defaultCareer.coins)),
            trophies = maxOf(0, prefs.getInt("trophies", defaultCareer.trophies)),
            mulligansRemaining = maxOf(0, prefs.getInt("mulligansRemaining", defaultCareer.mulligansRemaining)),
            roundsPlayed = maxOf(0, prefs.getInt("roundsPlayed", defaultCareer.roundsPlayed)),
            holesPlayed = maxOf(0, prefs.getInt("holesPlayed", defaultCareer.holesPlayed)),
            birdies = maxOf(0, prefs.getInt("birdies", defaultCareer.birdies)),
            eagles = maxOf(0, prefs.getInt("eagles", defaultCareer.eagles)),
            aces = maxOf(0, prefs.getInt("aces", defaultCareer.aces)),
            longestDriveYds = maxOf(0f, prefs.getFloat("longestDriveYds", defaultCareer.longestDriveYds)),
            greensInRegulationCount = maxOf(0, prefs.getInt("greensInRegulationCount", defaultCareer.greensInRegulationCount)),
            tourXp = maxOf(0, prefs.getInt("tourXp", defaultCareer.tourXp)),
            shootoutHighScore = maxOf(0, prefs.getInt("shootoutHighScore", defaultCareer.shootoutHighScore)),
            equippedBallId = prefs.getString("equippedBallId", defaultCareer.equippedBallId) ?: defaultCareer.equippedBallId,
            equippedTracer = prefs.getString("equippedTracer", defaultCareer.equippedTracer) ?: defaultCareer.equippedTracer,
            matchPlayWins = maxOf(0, prefs.getInt("matchPlayWins", defaultCareer.matchPlayWins)),
            matchPlayLosses = maxOf(0, prefs.getInt("matchPlayLosses", defaultCareer.matchPlayLosses))
        )

        val unlockedSet = (prefs.getStringSet("unlockedBallIds", setOf("standard_white")) ?: setOf("standard_white")) + "standard_white"
        val balls = defaultBalls.map { ball ->
            val unlocked = ball.isUnlocked || unlockedSet.contains(ball.id)
            ball.copy(
                isUnlocked = unlocked,
                isEquipped = ball.id == career.equippedBallId && unlocked
            )
        }

        val clubs = defaultClubs.map { club ->
            val savedLevel = prefs.getInt("club_lvl_${club.clubType.name}", club.level).coerceIn(1, club.maxLevel)
            val diff = (savedLevel - club.level).coerceAtLeast(0)
            if (diff > 0) {
                club.copy(
                    level = savedLevel,
                    powerStat = club.powerStat + diff * 6,
                    controlStat = club.controlStat + diff * 5,
                    spinStat = club.spinStat + diff * 5
                )
            } else {
                club
            }
        }

        val defaultPass = TourPassState()
        val freeTiers = (prefs.getString("pass_claimedFree", "1,2") ?: "1,2")
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .distinct()
        val vipTiers = (prefs.getString("pass_claimedVip", "") ?: "")
            .split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .distinct()

        val mergedFreeTiers = (defaultPass.claimedFreeTiers + freeTiers).distinct()
        val isVipUnlocked = prefs.getBoolean("pass_isVipUnlocked", false)
        val updatedPassTiers = defaultPass.tiers.map { tier ->
            tier.copy(
                isFreeClaimed = tier.isFreeClaimed || mergedFreeTiers.contains(tier.tierLevel),
                isVipClaimed = tier.isVipClaimed || vipTiers.contains(tier.tierLevel)
            )
        }

        val tourPass = defaultPass.copy(
            currentXp = maxOf(defaultPass.currentXp, prefs.getInt("pass_currentXp", defaultPass.currentXp)),
            isVipUnlocked = isVipUnlocked,
            claimedFreeTiers = mergedFreeTiers,
            claimedVipTiers = vipTiers,
            tiers = updatedPassTiers
        )

        val defaultGolfer = GolferProfile()
        val stanceName = prefs.getString("golfer_stance", defaultGolfer.stance.name) ?: defaultGolfer.stance.name
        val swingStyleName = prefs.getString("golfer_swingStyle", defaultGolfer.swingStyle.name) ?: defaultGolfer.swingStyle.name
        val golfer = defaultGolfer.copy(
            golferName = prefs.getString("golfer_name", defaultGolfer.golferName) ?: defaultGolfer.golferName,
            countryTag = prefs.getString("golfer_countryTag", defaultGolfer.countryTag) ?: defaultGolfer.countryTag,
            stance = GolferStance.entries.firstOrNull { it.name == stanceName } ?: defaultGolfer.stance,
            swingStyle = GolferSwingStyle.entries.firstOrNull { it.name == swingStyleName } ?: defaultGolfer.swingStyle,
            visorColorHex = prefs.getLong("golfer_visorColorHex", defaultGolfer.visorColorHex),
            poloColorHex = prefs.getLong("golfer_poloColorHex", defaultGolfer.poloColorHex),
            gloveStyle = prefs.getString("golfer_gloveStyle", defaultGolfer.gloveStyle) ?: defaultGolfer.gloveStyle,
            powerAttribute = prefs.getInt("golfer_powerAttribute", defaultGolfer.powerAttribute),
            accuracyAttribute = prefs.getInt("golfer_accuracyAttribute", defaultGolfer.accuracyAttribute),
            spinControlAttribute = prefs.getInt("golfer_spinControlAttribute", defaultGolfer.spinControlAttribute),
            puttingAttribute = prefs.getInt("golfer_puttingAttribute", defaultGolfer.puttingAttribute),
            unspentSkillPoints = prefs.getInt("golfer_unspentSkillPoints", defaultGolfer.unspentSkillPoints),
            skinToneHex = prefs.getLong("golfer_skinToneHex", defaultGolfer.skinToneHex),
            hatStyle = prefs.getString("golfer_hatStyle", defaultGolfer.hatStyle) ?: defaultGolfer.hatStyle,
            shoeStyle = prefs.getString("golfer_shoeStyle", defaultGolfer.shoeStyle) ?: defaultGolfer.shoeStyle,
            golfBagStyle = prefs.getString("golfer_golfBagStyle", defaultGolfer.golfBagStyle) ?: defaultGolfer.golfBagStyle
        )

        val defaultSettings = PlayConsoleSettings()
        val graphicsName = prefs.getString("settings_graphicsTier", defaultSettings.graphicsTier.name) ?: defaultSettings.graphicsTier.name
        val aiDiffName = prefs.getString("settings_aiDifficulty", defaultSettings.aiDifficulty.name) ?: defaultSettings.aiDifficulty.name
        val themeName = prefs.getString("settings_themeMode", defaultSettings.themeMode.name) ?: defaultSettings.themeMode.name
        val camName = prefs.getString("settings_preferredCameraMode", defaultSettings.preferredCameraMode.name) ?: defaultSettings.preferredCameraMode.name
        val settings = defaultSettings.copy(
            isSoundEnabled = prefs.getBoolean("settings_isSoundEnabled", defaultSettings.isSoundEnabled),
            soundVolume = prefs.getFloat("settings_soundVolume", defaultSettings.soundVolume),
            isHapticsEnabled = prefs.getBoolean("settings_isHapticsEnabled", defaultSettings.isHapticsEnabled),
            isCaddieAudioTipsEnabled = prefs.getBoolean("settings_isCaddieAudioTipsEnabled", defaultSettings.isCaddieAudioTipsEnabled),
            isHighFpsMode = prefs.getBoolean("settings_isHighFpsMode", defaultSettings.isHighFpsMode),
            isPuttingGridVisible = prefs.getBoolean("settings_isPuttingGridVisible", defaultSettings.isPuttingGridVisible),
            isSlopeBreakRadarVisible = prefs.getBoolean("settings_isSlopeBreakRadarVisible", defaultSettings.isSlopeBreakRadarVisible),
            isCloudSyncEnabled = prefs.getBoolean("settings_isCloudSyncEnabled", defaultSettings.isCloudSyncEnabled),
            graphicsTier = GraphicsQualityTier.entries.firstOrNull { it.name == graphicsName } ?: defaultSettings.graphicsTier,
            aiDifficulty = AiDifficultyTier.entries.firstOrNull { it.name == aiDiffName } ?: defaultSettings.aiDifficulty,
            themeMode = ZenithThemeMode.entries.firstOrNull { it.name == themeName } ?: defaultSettings.themeMode,
            musicVolume = prefs.getFloat("settings_musicVolume", defaultSettings.musicVolume),
            controlSensitivity = prefs.getFloat("settings_controlSensitivity", defaultSettings.controlSensitivity),
            preferredCameraMode = CameraViewMode.entries.firstOrNull { it.name == camName } ?: defaultSettings.preferredCameraMode,
            languageTag = prefs.getString("settings_languageTag", defaultSettings.languageTag) ?: defaultSettings.languageTag,
            notificationsTourEvents = prefs.getBoolean("settings_notificationsTourEvents", defaultSettings.notificationsTourEvents),
            notificationsDailyWheel = prefs.getBoolean("settings_notificationsDailyWheel", defaultSettings.notificationsDailyWheel),
            hasCompletedOnboarding = prefs.getBoolean("settings_hasCompletedOnboarding", defaultSettings.hasCompletedOnboarding)
        )

        val defaultBlueprint = CustomHoleBlueprint()
        val blueprint = defaultBlueprint.copy(
            holeName = prefs.getString("arch_holeName", defaultBlueprint.holeName) ?: defaultBlueprint.holeName,
            par = prefs.getInt("arch_par", defaultBlueprint.par),
            totalYardage = prefs.getInt("arch_totalYardage", defaultBlueprint.totalYardage),
            fairwayShape = prefs.getString("arch_fairwayShape", defaultBlueprint.fairwayShape) ?: defaultBlueprint.fairwayShape,
            greenStyle = prefs.getString("arch_greenStyle", defaultBlueprint.greenStyle) ?: defaultBlueprint.greenStyle,
            sandBunkerCount = prefs.getInt("arch_sandBunkerCount", defaultBlueprint.sandBunkerCount),
            hasWaterLake = prefs.getBoolean("arch_hasWaterLake", defaultBlueprint.hasWaterLake),
            pinPositionStyle = prefs.getString("arch_pinPositionStyle", defaultBlueprint.pinPositionStyle) ?: defaultBlueprint.pinPositionStyle,
            slopeDifficulty = prefs.getString("arch_slopeDifficulty", defaultBlueprint.slopeDifficulty) ?: defaultBlueprint.slopeDifficulty
        )

        val completedChallenges = prefs.getStringSet("completedChallengeIds", emptySet()) ?: emptySet()
        val claimedChallenges = prefs.getStringSet("claimedChallengeIds", emptySet()) ?: emptySet()
        val unlockedAchievements = prefs.getStringSet("unlockedAchievementIds", setOf("ach_first_drive", "ach_bullseye")) ?: setOf("ach_first_drive", "ach_bullseye")
        val dailyLoginStreak = prefs.getInt("dailyLoginStreak", 1).coerceAtLeast(1)
        val lastDailyClaimDayEpoch = prefs.getLong("lastDailyClaimDayEpoch", 0L)
        val unlockedAccessoryIds = prefs.getStringSet("unlockedAccessoryIds", emptySet()) ?: emptySet()

        return LocalSnapshot(
            career = career,
            balls = balls,
            clubs = clubs,
            tourPassState = tourPass,
            golferProfile = golfer,
            playSettings = settings,
            customHoleBlueprint = blueprint,
            completedChallengeIds = completedChallenges,
            claimedChallengeIds = claimedChallenges,
            unlockedAchievementIds = unlockedAchievements,
            dailyLoginStreak = dailyLoginStreak,
            lastDailyClaimDayEpoch = lastDailyClaimDayEpoch,
            unlockedAccessoryIds = unlockedAccessoryIds
        )
    }
}
