package com.example.game.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

enum class ClubType(
    val displayName: String,
    val shortName: String,
    val baseDistanceYds: Float,
    val minDistanceYds: Float,
    val maxDistanceYds: Float,
    val launchAngleDeg: Float,
    val rollFactor: Float,
    val isPutter: Boolean = false,
    val clubheadSpeedMph: Float = 100f,
    val smashFactor: Float = 1.45f,
    val baseBackspinRpm: Int = 3000,
    val forgivenessMoi: Float = 0.85f,
    val categoryName: String = "Iron"
) {
    DRIVER("Driver (1W)", "1W", 260f, 210f, 310f, 13f, 0.22f, false, 113f, 1.49f, 2550, 0.82f, "Driver"),
    WOOD_3("3-Wood (3W)", "3W", 220f, 180f, 250f, 16f, 0.18f, false, 107f, 1.46f, 3650, 0.85f, "Fairway Wood"),
    IRON_4("4-Iron (4I)", "4I", 195f, 165f, 215f, 20f, 0.14f, false, 98f, 1.39f, 4850, 0.87f, "Long Iron"),
    IRON_7("7-Iron (7I)", "7I", 160f, 120f, 185f, 28f, 0.10f, false, 90f, 1.33f, 6950, 0.90f, "Short Iron"),
    IRON_9("9-Iron (9I)", "9I", 135f, 100f, 155f, 36f, 0.07f, false, 85f, 1.28f, 8200, 0.92f, "Short Iron"),
    PITCHING_WEDGE("Pitch Wedge (PW)", "PW", 110f, 60f, 130f, 44f, 0.05f, false, 81f, 1.24f, 9300, 0.94f, "Wedge"),
    SAND_WEDGE("Sand Wedge (SW)", "SW", 80f, 30f, 95f, 54f, 0.03f, false, 74f, 1.18f, 10400, 0.95f, "Wedge"),
    PUTTER("Tour Putter (PT)", "PT", 25f, 4f, 40f, 0f, 0.01f, true, 18f, 1.02f, 450, 0.98f, "Putter");

    val expectedRangeText: String
        get() = "${minDistanceYds.toInt()}–${maxDistanceYds.toInt()} YDS"
}

enum class SurfaceType(
    val displayName: String,
    val drag: Float,
    val bounce: Float,
    val distanceMultiplier: Float,
    val isHazard: Boolean = false
) {
    TEE("Tee Box", 0.04f, 0.65f, 1.0f),
    FAIRWAY("Fairway", 0.05f, 0.55f, 1.0f),
    ROUGH("Deep Rough", 0.25f, 0.20f, 0.72f),
    SAND_BUNKER("Sand Trap", 0.60f, 0.05f, 0.50f),
    GREEN("Putting Green", 0.02f, 0.40f, 1.0f),
    WATER_HAZARD("Water Hazard", 0.99f, 0.0f, 0.0f, isHazard = true),
    OUT_OF_BOUNDS("Out of Bounds", 0.99f, 0.0f, 0.0f, isHazard = true)
}

data class WindCondition(
    val speedMph: Float,
    val directionDeg: Float // 0 = Blowing North (up), 90 = Blowing East (right), 180 = South, 270 = West
) {
    // Vector components for wind displacement
    val vectorX: Float
        get() = (Math.sin(Math.toRadians(directionDeg.toDouble())) * (speedMph / 10f)).toFloat()
    val vectorY: Float
        get() = (-Math.cos(Math.toRadians(directionDeg.toDouble())) * (speedMph / 10f)).toFloat()
}

data class HazardZone(
    val type: SurfaceType,
    val center: Offset,
    val radiusX: Float,
    val radiusY: Float,
    val rotationDeg: Float = 0f
) {
    fun contains(point: Offset): Boolean {
        // Approximate ellipse containment
        val rad = Math.toRadians(-rotationDeg.toDouble())
        val cos = Math.cos(rad).toFloat()
        val sin = Math.sin(rad).toFloat()
        val dx = point.x - center.x
        val dy = point.y - center.y
        val unrotatedX = cos * dx - sin * dy
        val unrotatedY = sin * dx + cos * dy
        val normalized = (unrotatedX * unrotatedX) / (radiusX * radiusX) +
                (unrotatedY * unrotatedY) / (radiusY * radiusY)
        return normalized <= 1.0f
    }
}

data class FairwayZone(
    val center: Offset,
    val width: Float,
    val height: Float,
    val angleDeg: Float = 0f
) {
    fun contains(point: Offset): Boolean {
        val rad = Math.toRadians(-angleDeg.toDouble())
        val cos = Math.cos(rad).toFloat()
        val sin = Math.sin(rad).toFloat()
        val dx = point.x - center.x
        val dy = point.y - center.y
        val rx = Math.abs(cos * dx - sin * dy)
        val ry = Math.abs(sin * dx + cos * dy)
        return rx <= width / 2f && ry <= height / 2f
    }
}

data class Hole(
    val id: Int,
    val courseName: String,
    val holeNumber: Int,
    val par: Int,
    val yardage: Int,
    val teePosition: Offset,
    val pinPosition: Offset,
    val greenCenter: Offset,
    val greenRadius: Float,
    val fairways: List<FairwayZone>,
    val hazards: List<HazardZone>,
    val greenSlope: Offset = Offset(0f, 0f), // Slope force when rolling on green
    val elevationDiffYds: Float = 0f,
    val holeTitle: String = "Zenith Links #$holeNumber"
) {
    /**
     * Deterministic corridor tree positions flanking the rough along the hole for 3D obstacle physics & rendering.
     */
    val treeObstacles: List<Offset>
        get() = listOf(
            Offset(55f + (id % 3) * 8f, 540f),
            Offset(345f - (id % 3) * 8f, 520f),
            Offset(48f + (holeNumber % 4) * 6f, 410f),
            Offset(352f - (holeNumber % 4) * 6f, 390f),
            Offset(58f, 280f),
            Offset(342f, 265f)
        ).filter { pt ->
            (pt - greenCenter).getDistance() > greenRadius + 22f &&
                (pt - teePosition).getDistance() > 35f &&
                fairways.none { it.contains(pt) }
        }
}

data class BallState(
    val x: Float,
    val y: Float,
    val z: Float = 0f, // Altitude
    val vx: Float = 0f,
    val vy: Float = 0f,
    val vz: Float = 0f,
    val isAirborne: Boolean = false,
    val isRolling: Boolean = false,
    val isResting: Boolean = true,
    val inHole: Boolean = false,
    val lie: SurfaceType = SurfaceType.TEE,
    val distanceTraveled: Float = 0f,
    val lastShotYardage: Float = 0f,
    val trajectoryPoints: List<Offset> = emptyList()
)

data class ShotAim(
    val powerRatio: Float, // 0.1f to 1.0f
    val aimAngleDeg: Float, // Aim heading in degrees
    val curveSpin: Float, // -1.0f (Hook/Draw) to +1.0f (Slice/Fade)
    val apexTimingAccuracy: Float, // 1.0f = Perfect Apex, 0.7f = Good, 0.4f = Off
    val verticalSpin: Float = 0f // -1.0f (Topspin / Low Rollout) to +1.0f (Backspin / High Bite)
)

data class TournamentRival(
    val id: String,
    val name: String,
    val title: String,
    val avatarColor: Color,
    val skillRating: Int,
    val scores: MutableList<Int> = mutableListOf(),
    var currentTotalScore: Int = 0
)

data class GolfBallSkin(
    val id: String,
    val name: String,
    val description: String,
    val glowColor: Color,
    val powerBonusPercent: Int,
    val windResistancePercent: Int,
    val isEquipped: Boolean = false,
    val isUnlocked: Boolean = false,
    val costCoins: Int = 0
)

data class ClubUpgrade(
    val clubType: ClubType,
    var level: Int = 1,
    val maxLevel: Int = 5,
    val powerStat: Int = 70,
    val controlStat: Int = 65,
    val spinStat: Int = 60
) {
    val upgradeCost: Int
        get() = level * 250
}

data class PlayerCoordinates(
    val position: Offset,
    val facingAngleDeg: Float,
    val stanceDistance: Float = 4.0f
)

data class PhysicsCollisionPoint(
    val position: Offset,
    val surface: SurfaceType,
    val impactSpeedMps: Float,
    val reboundSpeedMps: Float,
    val bounceNumber: Int,
    val spinRpmAfterImpact: Int
)

data class BallTrajectory(
    val predictedPoints: List<Offset> = emptyList(),
    val actualFlightPoints: List<Offset> = emptyList(),
    val apexAltitude: Float = 0f,
    val carryDistanceYds: Float = 0f,
    val totalRollDistanceYds: Float = 0f,
    val collisionEvents: List<PhysicsCollisionPoint> = emptyList(),
    val launchSpeedMph: Float = 0f,
    val launchAngleDeg: Float = 0f,
    val backspinRpm: Int = 0,
    val sidespinRpm: Int = 0,
    val dragForcePeakN: Float = 0f,
    val magnusForcePeakN: Float = 0f
)

data class HoleShotLog(
    val strokeNumber: Int,
    val clubUsed: ClubType,
    val startLie: SurfaceType,
    val endLie: SurfaceType,
    val yardage: Float,
    val apexRating: String
)

data class HoleStatistics(
    val holeNumber: Int,
    val par: Int,
    val totalHoleYardage: Int,
    val strokesTaken: Int,
    val remainingDistanceToPin: Float,
    val distanceTraveled: Float,
    val currentLie: SurfaceType,
    val fairwayHit: Boolean = false,
    val greenInRegulation: Boolean = false,
    val scoreRelativeToPar: Int = 0,
    val shotLogs: List<HoleShotLog> = emptyList()
)

data class PlayerCareer(
    var coins: Int = 850,
    var mulligansRemaining: Int = 3,
    var trophies: Int = 1,
    var roundsPlayed: Int = 4,
    var holesPlayed: Int = 18,
    var birdies: Int = 5,
    var eagles: Int = 1,
    var aces: Int = 0,
    var longestDriveYds: Float = 284f,
    var greensInRegulationCount: Int = 12,
    var tourXp: Int = 450,
    var shootoutHighScore: Int = 1850,
    var equippedBallId: String = "standard_white",
    var equippedTracer: String = "Cyan Surge",
    var matchPlayWins: Int = 2,
    var matchPlayLosses: Int = 1
)

enum class WeatherType(
    val displayName: String,
    val turfDragMultiplier: Float,
    val rollDecayMultiplier: Float,
    val skyTint: Long
) {
    SUNNY("Sunny Links", 1.0f, 1.0f, 0x00000000),
    TWILIGHT("Neon Twilight", 0.95f, 1.05f, 0x220D1B2A),
    RAIN_STORM("Wet Rainstorm", 1.30f, 0.75f, 0x33001F3F),
    WINDY_TEMPEST("Windy Tempest", 1.05f, 0.90f, 0x221B4332),
    GOLDEN_HOUR("Golden Sunset", 1.0f, 1.02f, 0x28FF6B00),
    MIDNIGHT_CYBER("Midnight Cyber", 0.92f, 1.10f, 0x4405051A)
}

enum class CameraViewMode(val label: String, val shortTag: String) {
    OVERVIEW("Course Overview", "OVERVIEW"),
    LANDING_ZONE("Ball Tracking Cam", "BALL TRACK"),
    GREEN_APPROACH("Putting Camera", "PUTTING"),
    PERSPECTIVE_3D("3rd-Person Golfer", "3RD PERSON")
}

data class DailyChallenge(
    val id: String,
    val title: String,
    val description: String,
    val rewardCoins: Int,
    val rewardMulligans: Int,
    var isCompleted: Boolean = false,
    val targetType: String,
    var isClaimed: Boolean = false,
    val currentProgress: Int = 0,
    val targetGoal: Int = 1
)

data class LuckyWheelPrize(
    val id: String,
    val label: String,
    val subLabel: String,
    val coins: Int,
    val mulligans: Int,
    val colorHex: Long,
    val isJackpot: Boolean = false
)

enum class BallTracerStyle(val label: String, val colorHex: Long) {
    NEON_CYAN("Cyan Surge", 0xFF00FFCC),
    FIRESTORM("Firestorm Amber", 0xFFFF9900),
    ULTRAVIOLET("Ultraviolet Ray", 0xFFBD00FF),
    GOLD_CHAMPION("Golden Starlight", 0xFFFFD700)
}

data class TargetRing(
    val radiusYards: Float,
    val points: Int,
    val colorHex: Long,
    val ringLabel: String
)

data class ShootoutTarget(
    val center: Offset,
    val distanceYds: Int,
    val rings: List<TargetRing> = listOf(
        TargetRing(radiusYards = 3.5f, points = 500, colorHex = 0xFFFFD700, ringLabel = "BULLSEYE!"),
        TargetRing(radiusYards = 8.0f, points = 300, colorHex = 0xFFFF3B30, ringLabel = "INNER RING"),
        TargetRing(radiusYards = 14.0f, points = 150, colorHex = 0xFF00E5FF, ringLabel = "MID RING"),
        TargetRing(radiusYards = 22.0f, points = 50, colorHex = 0xFF00E676, ringLabel = "OUTER RING")
    )
)

data class ShootoutShotResult(
    val shotIndex: Int,
    val distanceToBullseyeYds: Float,
    val pointsEarned: Int,
    val ringHit: String,
    val comboMultiplier: Float,
    val isBullseye: Boolean
)

data class ShootoutGameState(
    val totalShots: Int = 5,
    val shotsRemaining: Int = 5,
    val currentScore: Int = 0,
    val currentCombo: Int = 0,
    val highestCombo: Int = 0,
    val shotResults: List<ShootoutShotResult> = emptyList(),
    val isRoundOver: Boolean = false,
    val coinsWon: Int = 0,
    val xpWon: Int = 0,
    val isNewHighScore: Boolean = false,
    val target: ShootoutTarget = ShootoutTarget(center = Offset(200f, 220f), distanceYds = 145)
)

data class TourPassTier(
    val tierLevel: Int,
    val requiredXp: Int,
    val freeRewardTitle: String,
    val freeCoins: Int = 0,
    val freeMulligans: Int = 0,
    val vipRewardTitle: String,
    val vipCoins: Int = 0,
    val vipMulligans: Int = 0,
    val isFreeClaimed: Boolean = false,
    val isVipClaimed: Boolean = false
)

data class TourPassState(
    val currentXp: Int = 450,
    val seasonNumber: Int = 1,
    val seasonTitle: String = "Apex Champions Tour",
    val daysRemaining: Int = 18,
    val isVipUnlocked: Boolean = false,
    val claimedFreeTiers: List<Int> = listOf(1, 2),
    val claimedVipTiers: List<Int> = emptyList(),
    val tiers: List<TourPassTier> = listOf(
        TourPassTier(1, 100, "500 Gold Coins", freeCoins = 500, vipRewardTitle = "1,500 Gold Coins", vipCoins = 1500, isFreeClaimed = true),
        TourPassTier(2, 250, "2 Tour Mulligans", freeMulligans = 2, vipRewardTitle = "5 Tour Mulligans", vipMulligans = 5, isFreeClaimed = true),
        TourPassTier(3, 450, "1,000 Gold Coins", freeCoins = 1000, vipRewardTitle = "Cyber Emerald Ball Skin", vipCoins = 2000),
        TourPassTier(4, 700, "3 Tour Mulligans", freeMulligans = 3, vipRewardTitle = "2,500 Gold Coins", vipCoins = 2500),
        TourPassTier(5, 1000, "2,000 Gold Coins", freeCoins = 2000, vipRewardTitle = "Solar Flare Ball Skin", vipCoins = 3000),
        TourPassTier(6, 1350, "5 Tour Mulligans", freeMulligans = 5, vipRewardTitle = "Golden Dragon Ball Skin", vipMulligans = 10),
        TourPassTier(7, 1750, "3,500 Gold Coins", freeCoins = 3500, vipRewardTitle = "5,000 Gold Coins", vipCoins = 5000),
        TourPassTier(8, 2200, "6 Tour Mulligans", freeMulligans = 6, vipRewardTitle = "Apex Sniper Title & 7,500 Coins", vipCoins = 7500),
        TourPassTier(9, 2700, "5,000 Gold Coins", freeCoins = 5000, vipRewardTitle = "10,000 Gold Coins", vipCoins = 10000),
        TourPassTier(10, 3300, "Zenith Master Cup Trophy", freeCoins = 10000, freeMulligans = 10, vipRewardTitle = "Crown of Champions & 25,000 Coins", vipCoins = 25000, vipMulligans = 20)
    )
)

data class TournamentCup(
    val id: String,
    val title: String,
    val subtitle: String,
    val courseName: String,
    val entryFeeCoins: Int,
    val prizeCoins: Int,
    val prizeTrophies: Int,
    val holesCount: Int,
    val defaultWeather: WeatherType,
    val difficulty: String,
    val bannerAccentHex: Long = 0xFF00E676,
    val isUnlocked: Boolean = true
)

data class PlayConsoleSettings(
    val packageName: String = "com.aistudio.zenithgolf.tzmpq",
    val versionCode: Int = 5,
    val versionName: String = "2.0",
    val targetSdk: Int = 36,
    val minSdk: Int = 24,
    val compileSdk: Int = 36,
    val appTitle: String = "Zenith Golf",
    val shortDescription: String = "Championship arcade golf simulator with 1v1 matchplay, course architect & AI caddie.",
    val fullDescription: String = "Zenith Golf 2.0 brings next-gen championship golf to Android with deterministic 3D ball-flight physics, 6 authentic club categories, live wind shear and slope break putting radar, 1v1 Rival Match Play Showdowns, Zenith Course Architect Sandbox, Shot Replay Theater, Season Tour Pass, and Firebase Cloud Sync.",
    val contentRating: String = "PEGI 3 / Everyone (ESRB)",
    val category: String = "Sports / Golf (Simulation & Arcade)",
    val privacyPolicyUrl: String = "https://policies.google.com/privacy",
    val developerEmail: String = "support@zenithgolf.app",
    val releaseTrack: String = "Closed Testing (Google Play Console)",
    val signingKeystore: String = "Android Upload Keystore (v2 + v3 Signing)",
    val sha1Fingerprint: String = "D2:CC:88:49:72:0B:08:06:20:90:ED:D0:E7:DA:5B:B3:C8:BA:E0:52",
    val sha256Fingerprint: String = "70:E0:AF:D0:23:80:A0:6A:34:DF:D4:AA:D2:07:61:33:C8:97:D1:20:AA:67:D7:D0:0E:1E:9A:BD:E9:BE:59:F7",
    val firebaseProjectId: String = "zenith-golf",
    val firebaseProjectNumber: String = "272351721301",
    val gcpParentOrg: String = "abux-one-org",
    val isSoundEnabled: Boolean = true,
    val soundVolume: Float = 0.85f,
    val isHapticsEnabled: Boolean = true,
    val isCaddieAudioTipsEnabled: Boolean = true,
    val isHighFpsMode: Boolean = true,
    val isPuttingGridVisible: Boolean = true,
    val isPlayGamesConnected: Boolean = true,
    val isCloudSyncEnabled: Boolean = true,
    val isSlopeBreakRadarVisible: Boolean = true,
    val playStoreRating: Float = 4.9f,
    val totalPlayStoreReviews: Int = 12480,
    val inAppUpdateStatus: String = "Up to Date (v2.0 • Build 5)",
    val studioName: String = "Apex Dynamics Game Studios",
    val studioSlogan: String = "Rise Above. Master Every Swing.",
    val graphicsTier: GraphicsQualityTier = GraphicsQualityTier.FLAGSHIP_ULTRA,
    val aiDifficulty: AiDifficultyTier = AiDifficultyTier.PRO,
    val arm64Architecture: String = "ARM64-v8a (Native 64-bit)",
    val themeMode: ZenithThemeMode = ZenithThemeMode.SYSTEM,
    val musicVolume: Float = 0.65f,
    val controlSensitivity: Float = 1.0f,
    val preferredCameraMode: CameraViewMode = CameraViewMode.OVERVIEW,
    val languageTag: String = "English",
    val notificationsTourEvents: Boolean = true,
    val notificationsDailyWheel: Boolean = true,
    val hasCompletedOnboarding: Boolean = false
)

data class PlayAchievement(
    val id: String,
    val title: String,
    val description: String,
    val xpValue: Int,
    val isUnlocked: Boolean = false,
    val iconName: String = "trophy"
)

data class GpsRangefinderData(
    val isEnabled: Boolean = false,
    val targetPoint: Offset? = null,
    val distanceToPointYds: Float = 0f,
    val distancePointToPinYds: Float = 0f,
    val pointSurface: SurfaceType = SurfaceType.FAIRWAY
)

// --- NEW FEATURE 1: 1v1 MATCH PLAY DUEL MODELS ---
data class MatchPlayRival(
    val id: String,
    val name: String,
    val title: String,
    val rank: String,
    val avatarColor: Color,
    val archetype: String,
    val drivingDistanceYds: Int,
    val puttingRating: Int,
    val wagerCostCoins: Int,
    val winRewardCoins: Int,
    val introQuote: String,
    val birdieQuote: String,
    val pressureQuote: String
)

data class MatchPlayHoleResult(
    val holeNumber: Int,
    val playerStrokes: Int,
    val rivalStrokes: Int,
    val holeWinner: String // "PLAYER", "RIVAL", "HALVED"
)

data class MatchPlayState(
    val isActive: Boolean = false,
    val rival: MatchPlayRival? = null,
    val currentMatchHole: Int = 1,
    val totalMatchHoles: Int = 3,
    val playerHolesWon: Int = 0,
    val rivalHolesWon: Int = 0,
    val playerStrokesOnHole: Int = 0,
    val rivalStrokesOnHole: Int = 0,
    val isRivalTurn: Boolean = false,
    val rivalShotBanner: String = "",
    val rivalDistanceToPin: Float = 0f,
    val holeResults: List<MatchPlayHoleResult> = emptyList(),
    val matchWinner: String? = null, // "PLAYER", "RIVAL", "TIE"
    val isMatchFinished: Boolean = false
) {
    val scoreDifferential: Int
        get() = playerHolesWon - rivalHolesWon

    val matchScoreStatus: String
        get() = when {
            isMatchFinished -> when (matchWinner) {
                "PLAYER" -> "VICTORY! YOU WON"
                "RIVAL" -> "RIVAL TOOK THE MATCH"
                else -> "ALL SQUARE (DRAW)"
            }
            scoreDifferential > 0 -> "$scoreDifferential UP"
            scoreDifferential < 0 -> "${Math.abs(scoreDifferential)} DOWN"
            else -> "ALL SQUARE"
        }
}

// --- NEW FEATURE 2: REPLAY THEATER & TELEMETRY MODELS ---
data class ReplayShot(
    val id: String,
    val title: String,
    val holeName: String,
    val clubUsed: ClubType,
    val carryYards: Float,
    val ballSpeedMph: Float,
    val launchAngleDeg: Float,
    val apexHeightFt: Float,
    val spinRpm: Int,
    val scoreAchieved: String,
    val dateString: String,
    val trajectoryPoints: List<Offset>,
    val endLie: SurfaceType
)

// --- NEW FEATURE 3: COURSE ARCHITECT MODELS ---
data class CustomHoleBlueprint(
    val holeName: String = "Architect Link #1",
    val par: Int = 4,
    val totalYardage: Int = 390,
    val fairwayShape: String = "Straight Parkway", // "Straight Parkway", "Right Dogleg", "Left Dogleg", "Island Oasis"
    val greenStyle: String = "Crown Oval", // "Crown Oval", "Wide Kidney", "Mini Island"
    val sandBunkerCount: Int = 2,
    val hasWaterLake: Boolean = true,
    val pinPositionStyle: String = "Tucked Back Right", // "Center", "Tucked Back Right", "Front Edge", "Island Center"
    val slopeDifficulty: String = "Moderate Break"
)

// --- NEW FEATURE 4: PRO GOLFER LOCKER ROOM & CREATION ---
enum class GolferStance(val label: String) {
    RIGHT_HANDED("Right-Handed"),
    LEFT_HANDED("Left-Handed")
}

enum class GolferSwingStyle(val label: String, val powerMod: Int, val controlMod: Int) {
    APEX_POWER("Apex Power Bomber", +8, -3),
    TOUR_TEMPO("Balanced Tour Tempo", +3, +3),
    SURGICAL_PRECISION("Surgical Iron Sniper", -2, +9),
    CLUTCH_PUTTER("Short-Game Specialist", 0, +7)
}

data class GolferProfile(
    val golferName: String = "Alex \"Apex\" Mercer",
    val countryTag: String = "USA • APEX TOUR",
    val stance: GolferStance = GolferStance.RIGHT_HANDED,
    val swingStyle: GolferSwingStyle = GolferSwingStyle.TOUR_TEMPO,
    val visorColorHex: Long = 0xFF00E676,
    val poloColorHex: Long = 0xFF00E5FF,
    val gloveStyle: String = "Carbon Fiber Pro",
    val powerAttribute: Int = 82,
    val accuracyAttribute: Int = 80,
    val spinControlAttribute: Int = 78,
    val puttingAttribute: Int = 85,
    val unspentSkillPoints: Int = 3,
    val skinToneHex: Long = 0xFFFFCCBC,
    val hatStyle: String = "Tour Visor",
    val shoeStyle: String = "Spikeless Pro White",
    val golfBagStyle: String = "Forest & Gold Tour"
)

enum class ZenithThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light Green"),
    DARK("Dark Tour")
}

data class ShopBundleOffer(
    val id: String,
    val productId: String,
    val title: String,
    val subtitle: String,
    val coinsAmount: Int,
    val bonusLabel: String,
    val coinExchangeCost: Int = 0
)

data class AppNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestampLabel: String,
    val targetScreen: String = "HOME",
    val isUnread: Boolean = true
)

enum class GraphicsQualityTier(
    val label: String,
    val targetFps: Int,
    val resolutionScale: String,
    val shadowsDesc: String
) {
    LOW_BATTERY("Low-End / Battery Saver", 30, "75% Native", "Blob Shadows Only"),
    MID_BALANCED("Mid-Range Balanced", 60, "100% Native", "Soft Cascaded Shadows"),
    FLAGSHIP_ULTRA("Flagship Ultra (URP)", 120, "100% + MSAA 4x", "Dynamic Ray-Traced Sun & Water")
}

enum class AiDifficultyTier(val label: String, val strokeModifier: Int, val description: String) {
    ROOKIE("Tour Rookie", +2, "Forgiving AI rivals with occasional bunker errors"),
    PRO("PGA / Tour Pro", 0, "Authentic par/birdie pace with realistic dispersion"),
    APEX_MASTER("Apex Legend", -2, "Pin-seeking surgical AI that drains long birdie putts")
}

// --- ZENITH GOLF 2.0: BALL LANDING EFFECTS & DETERMINISTIC SCORING ENGINE ---
data class BallLandingEffect(
    val position: Offset,
    val surface: SurfaceType,
    val impactSpeedMps: Float = 0f,
    val title: String,
    val subtitle: String,
    val colorHex: Long,
    val isCupSink: Boolean = false
)

data class GolfScoreResult(
    val strokes: Int,
    val par: Int,
    val scoreRelativeToPar: Int,
    val term: String,
    val displayBanner: String,
    val relativeString: String,
    val coinsReward: Int,
    val xpReward: Int
)

object GolfScoringEngine {
    /**
     * Evaluate stroke count vs par for a completed hole using official golf scoring rules:
     * Hole-in-One, Albatross, Eagle, Birdie, Par, Bogey, Double Bogey, Triple Bogey+.
     */
    fun evaluateHoleScore(strokes: Int, par: Int): GolfScoreResult {
        val safeStrokes = strokes.coerceAtLeast(1)
        val safePar = par.coerceIn(3, 6)
        val diff = safeStrokes - safePar
        val relativeStr = when {
            diff == 0 -> "E"
            diff > 0 -> "+$diff"
            else -> "$diff"
        }

        val (term, banner, coins, xp) = when {
            safeStrokes == 1 -> Quadruple("HOLE-IN-ONE", "HOLE IN ONE! ACE!", 500, 500)
            diff <= -3 -> Quadruple("ALBATROSS", "ALBATROSS! -3", 400, 420)
            diff == -2 -> Quadruple("EAGLE", "EAGLE! -2", 300, 350)
            diff == -1 -> Quadruple("BIRDIE", "BIRDIE! -1", 150, 200)
            diff == 0 -> Quadruple("PAR", "PAR", 75, 100)
            diff == 1 -> Quadruple("BOGEY", "BOGEY (+1)", 40, 60)
            diff == 2 -> Quadruple("DOUBLE BOGEY", "DOUBLE BOGEY (+2)", 20, 45)
            else -> Quadruple("TRIPLE BOGEY+", "DOUBLE BOGEY (+$diff)", 20, 35)
        }

        return GolfScoreResult(
            strokes = safeStrokes,
            par = safePar,
            scoreRelativeToPar = diff,
            term = term,
            displayBanner = banner,
            relativeString = relativeStr,
            coinsReward = coins,
            xpReward = xp
        )
    }

    fun formatTotalScoreToPar(totalStrokes: Int, totalPar: Int): String {
        if (totalStrokes <= 0 || totalPar <= 0) return "E"
        val diff = totalStrokes - totalPar
        return when {
            diff == 0 -> "E"
            diff > 0 -> "+$diff"
            else -> "$diff"
        }
    }

    private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
}
