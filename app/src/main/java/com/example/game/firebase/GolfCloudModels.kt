package com.example.game.firebase

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class CloudUserProfile(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val coins: Long = 850L,
    val trophies: Long = 1L,
    val mulligansRemaining: Long = 3L,
    val roundsPlayed: Long = 0L,
    val holesPlayed: Long = 0L,
    val birdies: Long = 0L,
    val eagles: Long = 0L,
    val aces: Long = 0L,
    val longestDriveYds: Double = 0.0,
    val tourXp: Long = 0L,
    val shootoutHighScore: Long = 0L,
    val equippedBallId: String = "standard_white",
    val equippedTracer: String = "Cyan Surge",
    val unlockedBallIds: List<String> = listOf("standard_white"),
    val driverLevel: Long = 1L,
    val wood3Level: Long = 1L,
    val iron7Level: Long = 1L,
    val wedgeLevel: Long = 1L,
    val putterLevel: Long = 1L,
    val golferName: String = "Apex Pro",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudTourPass(
    @DocumentId val id: String = "state",
    val userId: String = "",
    val currentXp: Long = 0L,
    val claimedFreeTiers: List<Long> = emptyList(),
    val claimedVipTiers: List<Long> = emptyList(),
    @get:PropertyName("isVipUnlocked")
    @PropertyName("isVipUnlocked")
    val isVipUnlocked: Boolean = false,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * 1. `players` collection model (`/players/{userId}`)
 */
@IgnoreExtraProperties
data class CloudPlayerDocument(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "Apex Pro",
    val profileImageUrl: String = "",
    val email: String = "",
    val playerLevel: Long = 1L,
    val xp: Long = 0L,
    val coins: Long = 850L,
    val trophies: Long = 1L,
    val mulligansRemaining: Long = 3L,
    val countryTag: String = "USA • APEX TOUR",
    val accountTier: String = "GOOGLE_LINKED",
    val clientUpdatedAtEpochMs: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * 2. `player_statistics` collection model (`/player_statistics/{userId}`)
 */
@IgnoreExtraProperties
data class CloudPlayerStatistics(
    @DocumentId val id: String = "",
    val userId: String = "",
    val roundsPlayed: Long = 0L,
    val holesPlayed: Long = 0L,
    val birdies: Long = 0L,
    val eagles: Long = 0L,
    val aces: Long = 0L,
    val longestDriveYds: Double = 0.0,
    val greensInRegulationCount: Long = 0L,
    val shootoutHighScore: Long = 0L,
    val matchPlayWins: Long = 0L,
    val matchPlayLosses: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * 3. `player_inventory` collection model (`/player_inventory/{userId}`)
 */
@IgnoreExtraProperties
data class CloudPlayerInventory(
    @DocumentId val id: String = "",
    val userId: String = "",
    val equippedBallId: String = "standard_white",
    val equippedTracer: String = "Cyan Surge",
    val unlockedBallIds: List<String> = listOf("standard_white"),
    val driverLevel: Long = 1L,
    val wood3Level: Long = 1L,
    val iron4Level: Long = 1L,
    val iron7Level: Long = 1L,
    val iron9Level: Long = 1L,
    val wedgeLevel: Long = 1L,
    val sandWedgeLevel: Long = 1L,
    val putterLevel: Long = 1L,
    val visorColorHex: Long = 0xFF00E676,
    val poloColorHex: Long = 0xFF00E5FF,
    val gloveStyle: String = "Carbon Fiber Pro",
    val stance: String = "RIGHT_HANDED",
    val swingStyle: String = "TOUR_TEMPO",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * 4. `player_progress` collection model (`/player_progress/{userId}`)
 */
@IgnoreExtraProperties
data class CloudPlayerProgress(
    @DocumentId val id: String = "",
    val userId: String = "",
    val playerLevel: Long = 1L,
    val tourXp: Long = 0L,
    val trophies: Long = 1L,
    val unlockedCourses: List<String> = listOf(
        "cup_emerald_links",
        "cup_dunes_open",
        "cup_cyber_twilight",
        "cup_zenith_masters"
    ),
    val unlockedAchievementIds: List<String> = listOf("ach_first_drive", "ach_bullseye"),
    val completedChallengeIds: List<String> = emptyList(),
    val claimedChallengeIds: List<String> = emptyList(),
    val claimedFreeTiers: List<Long> = listOf(1L, 2L),
    val claimedVipTiers: List<Long> = emptyList(),
    @get:PropertyName("isVipUnlocked")
    @PropertyName("isVipUnlocked")
    val isVipUnlocked: Boolean = false,
    val syncRevision: Long = 1L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * 5. `game_settings` collection model (`/game_settings/{userId}`)
 */
@IgnoreExtraProperties
data class CloudGameSettings(
    @DocumentId val id: String = "",
    val userId: String = "",
    @get:PropertyName("isSoundEnabled")
    @PropertyName("isSoundEnabled")
    val isSoundEnabled: Boolean = true,
    val soundVolume: Double = 0.85,
    @get:PropertyName("isHapticsEnabled")
    @PropertyName("isHapticsEnabled")
    val isHapticsEnabled: Boolean = true,
    @get:PropertyName("isCaddieAudioTipsEnabled")
    @PropertyName("isCaddieAudioTipsEnabled")
    val isCaddieAudioTipsEnabled: Boolean = true,
    @get:PropertyName("isHighFpsMode")
    @PropertyName("isHighFpsMode")
    val isHighFpsMode: Boolean = true,
    @get:PropertyName("isPuttingGridVisible")
    @PropertyName("isPuttingGridVisible")
    val isPuttingGridVisible: Boolean = true,
    @get:PropertyName("isSlopeBreakRadarVisible")
    @PropertyName("isSlopeBreakRadarVisible")
    val isSlopeBreakRadarVisible: Boolean = true,
    @get:PropertyName("isCloudSyncEnabled")
    @PropertyName("isCloudSyncEnabled")
    val isCloudSyncEnabled: Boolean = true,
    val graphicsTier: String = "FLAGSHIP_ULTRA",
    val aiDifficulty: String = "PRO",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * 6. `match_history` collection model (`/match_history/{matchId}`)
 */
@IgnoreExtraProperties
data class CloudMatchHistoryRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val mode: String = "TOURNAMENT",
    val courseName: String = "Zenith Championship Links",
    val holesPlayed: Long = 1L,
    val totalStrokes: Long = 4L,
    val scoreToPar: Long = 0L,
    val scoreBanner: String = "PAR",
    val coinsEarned: Long = 75L,
    val xpEarned: Long = 100L,
    val opponentName: String = "Tour Field",
    val outcome: String = "COMPLETED",
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * 7. `tournaments` collection model (`/tournaments/{tournamentId}`)
 */
@IgnoreExtraProperties
data class CloudTournamentDocument(
    @DocumentId val id: String = "",
    val tournamentId: String = "",
    val title: String = "Zenith Masters Open",
    val courseName: String = "Zenith Championship Links",
    val entryFeeCoins: Long = 100L,
    val prizeCoins: Long = 2500L,
    val prizeTrophies: Long = 3L,
    val holesCount: Long = 6L,
    val difficulty: String = "Championship Pro",
    val status: String = "OPEN",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * 8. `tournament_entries` collection model (`/tournament_entries/{entryId}`)
 */
@IgnoreExtraProperties
data class CloudTournamentEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val tournamentId: String = "",
    val username: String = "Apex Pro",
    val strokes: Long = 18L,
    val scoreToPar: Long = -2L,
    val holesCompleted: Long = 6L,
    val rankPosition: Long = 1L,
    val rewardCoinsAwarded: Long = 0L,
    val status: String = "COMPLETED",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * 9. `achievements` collection model (`/achievements/{achievementDocId}`)
 */
@IgnoreExtraProperties
data class CloudAchievementRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val achievementId: String = "",
    val title: String = "",
    val description: String = "",
    val xpValue: Long = 1000L,
    @get:PropertyName("isUnlocked")
    @PropertyName("isUnlocked")
    val isUnlocked: Boolean = true,
    @ServerTimestamp val unlockedAt: Timestamp? = null
)

/**
 * 10. `daily_challenges` collection model (`/daily_challenges/{challengeDocId}`)
 */
@IgnoreExtraProperties
data class CloudDailyChallengeRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val challengeId: String = "",
    val title: String = "",
    val targetType: String = "",
    val currentProgress: Long = 0L,
    val targetGoal: Long = 1L,
    val rewardCoins: Long = 200L,
    val rewardMulligans: Long = 1L,
    @get:PropertyName("isCompleted")
    @PropertyName("isCompleted")
    val isCompleted: Boolean = false,
    @get:PropertyName("isClaimed")
    @PropertyName("isClaimed")
    val isClaimed: Boolean = false,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * 11. `reward_transactions` collection model (`/reward_transactions/{txId}`)
 */
@IgnoreExtraProperties
data class CloudRewardTransaction(
    @DocumentId val id: String = "",
    val userId: String = "",
    val rewardType: String = "HOLE_COMPLETION",
    val sourceId: String = "",
    val coinsDelta: Long = 0L,
    val xpDelta: Long = 0L,
    val mulligansDelta: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * 12. `leaderboards` collection model (`/leaderboards/{userId}` & `/leaderboards/{boardId}/entries/{userId}`)
 */
@IgnoreExtraProperties
data class CloudLeaderboardEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Golfer",
    val trophies: Long = 0L,
    val shootoutHighScore: Long = 0L,
    val longestDriveYds: Double = 0.0,
    val playerLevel: Long = 1L,
    val bestTournamentScoreToPar: Long = 0L,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * 13. `tournament_registrations` collection model (`/tournament_registrations/{regId}`)
 */
@IgnoreExtraProperties
data class CloudTournamentRegistration(
    @DocumentId val id: String = "",
    val userId: String = "",
    val tournamentId: String = "",
    val username: String = "Apex Pro",
    val entryFeePaid: Long = 0L,
    val trophiesAtEntry: Long = 0L,
    val status: String = "REGISTERED",
    val idempotencyKey: String = "",
    @ServerTimestamp val registeredAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * 14. `anti_cheat_flags` collection model (`/anti_cheat_flags/{flagId}`)
 */
@IgnoreExtraProperties
data class CloudAntiCheatFlag(
    @DocumentId val id: String = "",
    val userId: String = "",
    val matchId: String = "",
    val tournamentId: String? = null,
    val reasonCode: String = "IMPOSSIBLE_STROKES",
    val severity: String = "CRITICAL",
    val details: String = "",
    val reportedStrokes: Long = 0L,
    val reportedDriveYds: Double = 0.0,
    val roundDurationSec: Long = 0L,
    val reviewStatus: String = "PENDING_REVIEW",
    val reviewedBy: String? = null,
    val reviewNotes: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * 15. `audit_logs` collection model (`/audit_logs/{logId}`)
 */
@IgnoreExtraProperties
data class CloudAuditLogRecord(
    @DocumentId val id: String = "",
    val actorId: String = "",
    val targetUserId: String = "",
    val actionType: String = "",
    val resourceType: String = "",
    val resourceId: String = "",
    val status: String = "SUCCESS",
    val metadataSummary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

/**
 * 16. `rate_limits` collection model (`/rate_limits/{userId}`)
 */
@IgnoreExtraProperties
data class CloudRateLimitRecord(
    @DocumentId val id: String = "",
    val userId: String = "",
    val windowStartEpochMs: Long = 0L,
    val requestCount: Long = 1L,
    val lastAction: String = "API_CALL",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * 17. `reward_configs` collection model (`/reward_configs/{configId}`)
 */
@IgnoreExtraProperties
data class CloudRewardConfig(
    @DocumentId val id: String = "",
    val configId: String = "default_season_config",
    val holeCompletionBaseCoins: Long = 60L,
    val birdieBonusCoins: Long = 75L,
    val eagleBonusCoins: Long = 200L,
    val aceBonusCoins: Long = 750L,
    val tournamentFirstRatio: Double = 0.5,
    val tournamentSecondRatio: Double = 0.3,
    val tournamentThirdRatio: Double = 0.2,
    val updatedBy: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

/**
 * 18. `published_challenges` collection model (`/published_challenges/{challengeId}`)
 */
@IgnoreExtraProperties
data class CloudPublishedChallenge(
    @DocumentId val id: String = "",
    val challengeId: String = "",
    val title: String = "",
    val description: String = "",
    val targetType: String = "",
    val targetGoal: Long = 1L,
    val rewardCoins: Long = 250L,
    val rewardMulligans: Long = 1L,
    @get:PropertyName("isActive")
    @PropertyName("isActive")
    val isActive: Boolean = true,
    val publishedBy: String = "",
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class VerifiedScoreSubmissionResult(
    val matchId: String,
    val verificationStatus: String,
    val scoreBanner: String,
    val coinsEarned: Int,
    val xpEarned: Int,
    val trophiesDelta: Int,
    val antiCheatFlagId: String? = null
)

