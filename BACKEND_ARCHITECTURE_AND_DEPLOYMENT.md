# Zenith Golf 2.0 – Secure Game Services Backend
**Developer:** Apex Dynamics Game Studios  
**Runtime:** Firebase Cloud Functions v2 (Node.js 20) + Cloud Firestore + Firebase Authentication  
**Backend Source Location:** `/backend/functions/` (Strictly isolated from Android client `/app/`)

---

## 1. Architecture & Credential Isolation
- **Strict Separation:** All privileged backend logic resides in `/backend/functions/` (`index.js`, `src/validators.js`, `src/handlers.js`).
- **Zero Client Secrets:** No service account JSON keys, private tokens, or admin secrets are bundled in the Android APK (`app/`). The Android client (`GolfSecureBackendClient.kt` / `GolfCloudRepository.kt`) authenticates exclusively using the signed-in player's short-lived Firebase Authentication ID token (`Authorization: Bearer <Firebase_ID_Token>`).
- **Server-Side Admin Credentials:** Cloud Functions initialize `firebase-admin` using Google Cloud Application Default Credentials (ADC) inside the managed Cloud Run / Cloud Functions runtime.

---

## 2. Complete Firestore Database Schema (19 Collections)

### Player & Gameplay Collections (12 Core Collections)
1. **`players/{userId}` (`PlayerDocument`)**: `userId`, `username`, `profileImageUrl`, `email`, `playerLevel`, `xp`, `coins`, `trophies`, `mulligansRemaining`, `countryTag`, `accountTier`, `clientUpdatedAtEpochMs`, `updatedAt`.
2. **`player_statistics/{userId}` (`PlayerStatistics`)**: `userId`, `roundsPlayed`, `holesPlayed`, `birdies`, `eagles`, `aces`, `longestDriveYds`, `greensInRegulationCount`, `shootoutHighScore`, `matchPlayWins`, `matchPlayLosses`, `updatedAt`.
3. **`player_inventory/{userId}` (`PlayerInventory`)**: `userId`, `equippedBallId`, `equippedTracer`, `unlockedBallIds`, `driverLevel`, `wood3Level`, `iron4Level`, `iron7Level`, `iron9Level`, `wedgeLevel`, `sandWedgeLevel`, `putterLevel`, `visorColorHex`, `poloColorHex`, `gloveStyle`, `stance`, `swingStyle`, `updatedAt`.
4. **`player_progress/{userId}` (`PlayerProgress`)**: `userId`, `playerLevel`, `tourXp`, `trophies`, `unlockedCourses`, `unlockedAchievementIds`, `completedChallengeIds`, `claimedChallengeIds`, `claimedFreeTiers`, `claimedVipTiers`, `isVipUnlocked`, `syncRevision`, `updatedAt`.
5. **`game_settings/{userId}` (`GameSettings`)**: `userId`, `isSoundEnabled`, `soundVolume`, `isHapticsEnabled`, `isCaddieAudioTipsEnabled`, `isHighFpsMode`, `isPuttingGridVisible`, `isSlopeBreakRadarVisible`, `isCloudSyncEnabled`, `graphicsTier`, `aiDifficulty`, `updatedAt`.
6. **`match_history/{matchId}` (`MatchHistoryRecord`)**: `userId`, `mode`, `courseName`, `holesPlayed`, `totalStrokes`, `scoreToPar`, `scoreBanner`, `coinsEarned`, `xpEarned`, `opponentName`, `outcome`, `idempotencyKey`, `holeStrokes`, `holePars`, `maxDriveYds`, `roundDurationSec`, `verificationStatus`, `createdAt`.
7. **`tournaments/{tournamentId}` (`TournamentDocument`)**: `tournamentId`, `title`, `courseName`, `entryFeeCoins`, `prizeCoins`, `prizeTrophies`, `holesCount`, `difficulty`, `status`, `requiredTrophies`, `scheduledStartEpochMs`, `scheduledEndEpochMs`, `createdByAdminId`, `updatedAt`.
8. **`tournament_entries/{entryId}` (`TournamentEntry`)**: `userId`, `tournamentId`, `username`, `strokes`, `scoreToPar`, `holesCompleted`, `rankPosition`, `rewardCoinsAwarded`, `status`, `holeStrokes`, `submissionHash`, `verificationStatus`, `updatedAt`.
9. **`achievements/{achievementDocId}` (`AchievementRecord`)**: `userId`, `achievementId`, `title`, `description`, `xpValue`, `isUnlocked`, `unlockedAt`.
10. **`daily_challenges/{challengeDocId}` (`DailyChallengeRecord`)**: `userId`, `challengeId`, `title`, `targetType`, `currentProgress`, `targetGoal`, `rewardCoins`, `rewardMulligans`, `isCompleted`, `isClaimed`, `updatedAt`.
11. **`reward_transactions/{txId}` (`RewardTransaction`)**: `userId`, `rewardType`, `sourceId`, `coinsDelta`, `xpDelta`, `mulligansDelta`, `createdAt`.
12. **`leaderboards/{userId}` & `/leaderboards/{boardId}/entries/{userId}` (`LeaderboardEntry`)**: `userId`, `displayName`, `trophies`, `shootoutHighScore`, `longestDriveYds`, `playerLevel`, `bestTournamentScoreToPar`, `updatedAt`.

### Secure Backend, Anti-Cheat & Studio Administration Collections
13. **`tournament_registrations/{regId}` (`TournamentRegistration`)**: `userId`, `tournamentId`, `username`, `entryFeePaid`, `trophiesAtEntry`, `status` (`REGISTERED`, `SCORED`, `DISQUALIFIED`), `idempotencyKey`, `registeredAt`, `updatedAt`.
14. **`anti_cheat_flags/{flagId}` (`AntiCheatFlag`)**: `userId`, `matchId`, `tournamentId`, `reasonCode` (`IMPOSSIBLE_STROKES`, `DRIVE_SPEED_EXCEEDED`, `DURATION_ANOMALY`, `SCORE_MISMATCH`), `severity`, `details`, `reportedStrokes`, `reportedDriveYds`, `roundDurationSec`, `reviewStatus` (`PENDING_REVIEW`, `CONFIRMED_CHEAT`, `CLEARED`), `reviewedBy`, `reviewNotes`, `createdAt`, `updatedAt`.
15. **`audit_logs/{logId}` (`AuditLogRecord`)**: `actorId`, `targetUserId`, `actionType`, `resourceType`, `resourceId`, `status`, `metadataSummary`, `createdAt`. (Strictly append-only).
16. **`rate_limits/{userId}` (`RateLimitRecord`)**: `userId`, `windowStartEpochMs`, `requestCount` (max 60/min), `lastAction`, `updatedAt`.
17. **`studio_admins/{adminUid}` (`StudioAdmin`)**: `adminUid`, `email`, `role` (`STUDIO_ADMIN`, `TOURNAMENT_DIRECTOR`, `SECURITY_ANALYST`), `createdAt`.
18. **`reward_configs/{configId}` (`RewardConfig`)**: `configId`, `holeCompletionBaseCoins`, `birdieBonusCoins`, `eagleBonusCoins`, `aceBonusCoins`, `tournamentFirstRatio`, `tournamentSecondRatio`, `tournamentThirdRatio`, `updatedBy`, `updatedAt`.
19. **`published_challenges/{challengeId}` (`PublishedChallenge`)**: `challengeId`, `title`, `description`, `targetType`, `targetGoal`, `rewardCoins`, `rewardMulligans`, `isActive`, `publishedBy`, `updatedAt`.

---

## 3. API Endpoint Definitions (`zenithGameServicesApi` & Callable Functions)

All endpoints require header `Authorization: Bearer <Firebase_ID_Token>`.

### Player Endpoints
| Method & Path | Callable Name | Purpose |
| :--- | :--- | :--- |
| `POST /v1/matches/submit` | `submitMatchScore` | Validates hole-by-hole strokes, drive physics (`<= 430 yds`), and round duration (`>= 8s/hole`), rejects duplicate submissions (`idempotencyKey`), flags anti-cheat violations, calculates server-controlled rewards, updates stats & leaderboards, and writes `audit_logs`. |
| `POST /v1/tournaments/register` | `registerForTournament` | Validates tournament status, minimum trophy threshold, and coin balance; deducts entry fee; creates `tournament_registrations/{tournamentId}_{userId}` and `reward_transactions`. |
| `POST /v1/tournaments/submit-score` | `submitTournamentScore` | Verifies active tournament registration, validates hole-by-hole scorecard against anti-cheat bounds, computes official rank and prize distribution, locks scorecard (`FINALIZED`), and awards prizes. |
| `POST /v1/achievements/claim` | `claimAchievement` | Verifies achievement eligibility against authoritative `player_statistics` & `players` records, prevents duplicate claims, and awards XP/coins. |
| `POST /v1/challenges/claim` | `claimDailyChallenge` | Validates `currentProgress >= targetGoal` and `!isClaimed` on `daily_challenges`, grants coins/mulligans, and records `reward_transactions`. |

### Apex Dynamics Game Studios Administrative Endpoints
Requires `request.auth.token.admin == true` or membership in `studio_admins/{uid}`.

| Method & Path | Callable Name | Purpose |
| :--- | :--- | :--- |
| `POST /v1/admin/tournaments` | `adminCreateTournament` | Creates or schedules official tournaments with server-controlled prize pools, entry fees, and start/end windows. |
| `POST /v1/admin/rewards/config` | `adminConfigureRewards` | Updates server-controlled reward multipliers and tournament payout ratios in `reward_configs`. |
| `POST /v1/admin/challenges/publish` | `adminPublishChallenge` | Publishes official daily or weekly challenges to `published_challenges`. |
| `POST /v1/admin/anti-cheat/review` | `adminReviewSuspiciousMatch` | Resolves flagged matches in `anti_cheat_flags` (`CONFIRMED_CHEAT` or `CLEARED`) with audit logging. |
| `GET /v1/admin/tournaments/stats` | `adminGetTournamentStats` | Aggregates entrant counts, average strokes, best score-to-par, total prizes distributed, and anti-cheat flag counts. |

---

## 4. Deployment & Testing Instructions

### Run Backend Unit & API Dispatch Tests
```bash
node --test backend/functions/test/backend.test.js
```

### Run Firestore Security Rules Tests (Tier 1)
```bash
FIRESTORE_EMULATOR_HOST="127.0.0.1:8085" node --test firestore.test.js
```

### Run Android Robolectric Integration Tests (Tier 2)
```bash
gradle :app:testDebugUnitTest --tests '*Rule*'
```

### Deploy Security Rules & Cloud Functions
```bash
firebase deploy --only firestore:rules,functions
```
