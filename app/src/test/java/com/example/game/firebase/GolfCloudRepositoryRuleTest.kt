package com.example.game.firebase

import com.example.base.FirestoreEmulatorTestBase
import com.example.game.course.CourseCatalog
import com.example.game.model.ClubType
import com.example.game.model.DailyChallenge
import com.example.game.model.GolferProfile
import com.example.game.model.PlayAchievement
import com.example.game.model.PlayConsoleSettings
import com.example.game.model.PlayerCareer
import com.example.game.model.TourPassState
import com.example.game.progression.EquipmentCatalog
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test

class GolfCloudRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun syncFullPlayerState_authenticatedOwner_persistsAndRestoresAcrossCollections() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = GolfCloudRepository(firestore, auth)

        val career = PlayerCareer(
            coins = 3200,
            trophies = 9,
            mulligansRemaining = 5,
            roundsPlayed = 7,
            holesPlayed = 24,
            birdies = 10,
            eagles = 3,
            aces = 1,
            longestDriveYds = 312.5f,
            greensInRegulationCount = 18,
            tourXp = 1250,
            shootoutHighScore = 2650
        )
        val balls = EquipmentCatalog.getDefaultBalls()
        val clubs = EquipmentCatalog.getDefaultClubUpgrades().map {
            if (it.clubType == ClubType.DRIVER) it.copy(level = 3) else it
        }
        val tourPass = TourPassState(currentXp = 1250, isVipUnlocked = true)
        val golfer = GolferProfile(golferName = "Alice Apex")
        val settings = PlayConsoleSettings(soundVolume = 0.75f)
        val unlockedCourses = listOf("cup_emerald_links", "cup_dunes_open")
        val achievements = listOf(
            PlayAchievement("ach_first_drive", "Rocket Launcher", "250+ yd drive", 1000, true)
        )
        val challenges = listOf(
            DailyChallenge("ch_1", "Island Sniper", "Hit within 8y", 250, 1, true, "ISLAND_ACCURACY", true, 1, 1)
        )

        val syncResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.syncFullPlayerState(
                userId = aliceUid,
                email = ALICE_EMAIL,
                displayName = "Alice Apex",
                profileImageUrl = "https://example.com/alice.png",
                career = career,
                balls = balls,
                clubs = clubs,
                tourPass = tourPass,
                golferProfile = golfer,
                playSettings = settings,
                unlockedCourses = unlockedCourses,
                achievements = achievements,
                dailyChallenges = challenges,
                syncRevision = 3L
            )
        }
        assertTrue("Expected syncFullPlayerState to succeed", syncResult.isSuccess)

        val playerDoc = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getPlayerDocument(aliceUid).getOrThrow() }
        val statsDoc = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getPlayerStatistics(aliceUid).getOrThrow() }
        val invDoc = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getPlayerInventory(aliceUid).getOrThrow() }
        val progDoc = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getPlayerProgress(aliceUid).getOrThrow() }
        val settingsDoc = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getGameSettings(aliceUid).getOrThrow() }

        assertNotNull(playerDoc)
        assertEquals(3200L, playerDoc?.coins)
        assertEquals("Alice Apex", playerDoc?.username)
        assertEquals(24L, statsDoc?.holesPlayed)
        assertEquals(3L, invDoc?.driverLevel)
        assertEquals(true, progDoc?.isVipUnlocked)
        assertEquals(0.75, settingsDoc?.soundVolume ?: 0.0, 0.01)

        // Verify non-destructive conflict resolution preserves higher progress
        val lowerLocalCareer = PlayerCareer(coins = 1000, trophies = 2, holesPlayed = 6, tourXp = 200)
        val resolved = repository.resolveSyncConflict(
            localCareer = lowerLocalCareer,
            localBalls = EquipmentCatalog.getDefaultBalls(),
            localClubs = EquipmentCatalog.getDefaultClubUpgrades(),
            localTourPass = TourPassState(currentXp = 200),
            localGolfer = GolferProfile(golferName = "Old Name"),
            localSettings = PlayConsoleSettings(),
            localUnlockedCourses = listOf("cup_emerald_links"),
            localUnlockedAchievements = emptySet(),
            localCompletedChallenges = emptySet(),
            localClaimedChallenges = emptySet(),
            localUpdatedAtEpochMs = 100L,
            cloudPlayer = playerDoc,
            cloudStats = statsDoc,
            cloudInventory = invDoc,
            cloudProgress = progDoc,
            cloudSettings = settingsDoc
        )
        assertEquals(3200, resolved.career.coins)
        assertEquals(9, resolved.career.trophies)
        assertEquals(24, resolved.career.holesPlayed)
        assertEquals(1250, resolved.career.tourXp)
        assertTrue(resolved.unlockedCourseIds.contains("cup_dunes_open"))
    }

    @Test
    fun matchHistoryTournamentsAndRewards_authenticatedOwner_succeeds() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = GolfCloudRepository(firestore, auth)

        val cup = CourseCatalog.getTournamentCups().first()
        val tourRes = withTimeout(DEFAULT_TIMEOUT_MS) { repository.upsertTournament(cup) }
        assertTrue(tourRes.isSuccess)

        val entryRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.submitTournamentEntry(
                tournamentId = cup.id,
                username = "Alice Pro",
                strokes = 19,
                scoreToPar = -5,
                holesCompleted = 6,
                rankPosition = 1,
                rewardCoinsAwarded = 1500
            )
        }
        assertTrue(entryRes.isSuccess)

        val matchRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.recordMatchHistory(
                mode = "TOURNAMENT",
                courseName = cup.courseName,
                holesPlayed = 6,
                totalStrokes = 19,
                scoreToPar = -5,
                scoreBanner = "EAGLE! -2",
                coinsEarned = 1500,
                xpEarned = 500
            )
        }
        assertTrue(matchRes.isSuccess)

        val txRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.recordRewardTransaction(
                rewardType = "TOURNAMENT_PRIZE",
                sourceId = cup.id,
                coinsDelta = 1500,
                xpDelta = 500
            )
        }
        assertTrue(txRes.isSuccess)

        val ach = PlayAchievement("ach_eagle", "Eagle Eye", "Sink an eagle", 5000, true)
        val achRes = withTimeout(DEFAULT_TIMEOUT_MS) { repository.saveAchievementRecord(aliceUid, ach) }
        assertTrue(achRes.isSuccess)

        val ch = DailyChallenge("ch_1", "Island Sniper", "Hit within 8y", 250, 1, true, "ISLAND_ACCURACY", true, 1, 1)
        val chRes = withTimeout(DEFAULT_TIMEOUT_MS) { repository.saveDailyChallengeRecord(aliceUid, ch) }
        assertTrue(chRes.isSuccess)

        val historyList = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getUserMatchHistory(aliceUid).getOrThrow() }
        assertTrue(historyList.isNotEmpty())
    }

    @Test
    fun secureGameServices_scoreVerificationAntiCheatAndTournamentRegistration_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = GolfCloudRepository(firestore, auth)
        val uniqueRoundKey = "round_${System.currentTimeMillis()}"

        // 1. Valid score submission passes verification and calculates official rewards
        val verifiedRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.submitVerifiedMatchScore(
                mode = "QUICK_PLAY",
                courseName = "Emerald Links",
                holesPlayed = 3,
                totalStrokes = 10,
                scoreToPar = -2,
                maxDriveYds = 305.5f,
                roundDurationSec = 140,
                idempotencyKey = uniqueRoundKey
            )
        }
        assertTrue(
            "Expected valid score submission to succeed, but failed with: ${verifiedRes.exceptionOrNull()}",
            verifiedRes.isSuccess
        )
        assertEquals("VERIFIED", verifiedRes.getOrThrow().verificationStatus)

        // 2. Duplicate score submission with same idempotencyKey is rejected
        val duplicateRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.submitVerifiedMatchScore(
                mode = "QUICK_PLAY",
                courseName = "Emerald Links",
                holesPlayed = 3,
                totalStrokes = 10,
                scoreToPar = -2,
                maxDriveYds = 305.5f,
                roundDurationSec = 140,
                idempotencyKey = uniqueRoundKey
            )
        }
        assertTrue("Expected duplicate score submission to be rejected", duplicateRes.isFailure)

        // 3. Impossible drive distance (> 430 yds) triggers anti-cheat flag and rejects score
        val cheatRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.submitVerifiedMatchScore(
                mode = "QUICK_PLAY",
                courseName = "Emerald Links",
                holesPlayed = 3,
                totalStrokes = 9,
                scoreToPar = -3,
                maxDriveYds = 540.0f,
                roundDurationSec = 120,
                idempotencyKey = "${uniqueRoundKey}_cheat"
            )
        }
        assertTrue("Expected impossible drive score to be rejected by anti-cheat", cheatRes.isFailure)

        // 4. Verified tournament registration succeeds and prevents duplicate registration
        val uniqueTourId = "cup_masters_${System.currentTimeMillis()}"
        val regRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.registerForTournamentVerified(
                tournamentId = uniqueTourId,
                username = "Alice Pro",
                entryFeePaid = 250,
                trophiesAtEntry = 15
            )
        }
        assertTrue("Expected tournament registration to succeed", regRes.isSuccess)

        val dupRegRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.registerForTournamentVerified(
                tournamentId = uniqueTourId,
                username = "Alice Pro",
                entryFeePaid = 250,
                trophiesAtEntry = 15
            )
        }
        assertTrue("Expected duplicate tournament registration to be rejected", dupRegRes.isFailure)
    }

    @Test
    fun observeTourPass_authenticatedOwner_emitsRealtimeUpdate() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = GolfCloudRepository(firestore, auth)

        val passState = TourPassState(
            currentXp = 1200,
            isVipUnlocked = true,
            claimedFreeTiers = listOf(1, 2, 3),
            claimedVipTiers = listOf(1, 2)
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveTourPass(aliceUid, passState)
        }
        assertTrue(saveResult.isSuccess)

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeTourPass(aliceUid).first { it != null && it.currentXp == 1200L }
        }
        assertNotNull(emitted)
        assertEquals(true, emitted?.isVipUnlocked)
    }

    @Test
    fun getPlayerDocument_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        signInTestUser(BOB_EMAIL)
        val bobRepo = GolfCloudRepository(firestore, auth)

        // 1. Cross-user read of another player's document fails with PERMISSION_DENIED
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                bobRepo.getPlayerDocument(aliceUid).getOrThrow()
            }
            fail("Expected cross-user player document read to fail with PERMISSION_DENIED")
        } catch (e: Exception) {
            val fsEx = checkNotNull(e.unwrapFirestoreException()) {
                "Expected FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx.code)
        }

        // 2. Cross-user write to another player's document fails with PERMISSION_DENIED
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                bobRepo.savePlayerDocument(aliceUid, PlayerCareer(), GolferProfile(), BOB_EMAIL).getOrThrow()
            }
            fail("Expected cross-user player document write to fail with PERMISSION_DENIED")
        } catch (e: Exception) {
            val fsEx = checkNotNull(e.unwrapFirestoreException()) {
                "Expected FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx.code)
        }
    }

    @Test
    fun observeLeaderboard_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = GolfCloudRepository(firestore, auth)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeLeaderboard().first()
            }
            fail("Expected unauthenticated leaderboard query to fail with PERMISSION_DENIED")
        } catch (e: Exception) {
            val fsEx = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx.code)
        }
    }

    @Test
    fun deleteAllUserData_authenticatedOwner_deletesPrivateCloudRecords() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = GolfCloudRepository(firestore, auth)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.syncFullPlayerState(
                userId = aliceUid,
                email = ALICE_EMAIL,
                displayName = "Alice Erase Test",
                profileImageUrl = "",
                career = PlayerCareer(coins = 1500, trophies = 4, holesPlayed = 10),
                balls = EquipmentCatalog.getDefaultBalls(),
                clubs = EquipmentCatalog.getDefaultClubUpgrades(),
                tourPass = TourPassState(currentXp = 500),
                golferProfile = GolferProfile(golferName = "Alice Erase"),
                playSettings = PlayConsoleSettings(),
                unlockedCourses = listOf("cup_dunes_open"),
                achievements = listOf(PlayAchievement("ach_erase_1", "First Birdie", "Test", 100, true)),
                dailyChallenges = listOf(DailyChallenge("ch_erase_1", "Drive 250", "Test", 100, 1, false, "LONG_DRIVE", false, 0, 1)),
                syncRevision = 1L
            ).getOrThrow()
            repository.recordMatchHistory(
                mode = "QUICK_PLAY",
                courseName = "Zenith Dunes",
                holesPlayed = 1,
                totalStrokes = 3,
                scoreToPar = -1,
                scoreBanner = "BIRDIE",
                coinsEarned = 100,
                xpEarned = 100,
                opponentName = "",
                outcome = "WIN"
            ).getOrThrow()
        }

        val deletedCount = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.deleteAllUserData(aliceUid).getOrThrow()
        }
        assertTrue("Expected multiple cloud records to be deleted", deletedCount >= 10)

        val playerAfter = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getPlayerDocument(aliceUid).getOrThrow() }
        val statsAfter = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getPlayerStatistics(aliceUid).getOrThrow() }
        val invAfter = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getPlayerInventory(aliceUid).getOrThrow() }
        val progAfter = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getPlayerProgress(aliceUid).getOrThrow() }
        val settingsAfter = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getGameSettings(aliceUid).getOrThrow() }
        val matchesAfter = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getUserMatchHistory(aliceUid).getOrThrow() }

        assertEquals(null, playerAfter)
        assertEquals(null, statsAfter)
        assertEquals(null, invAfter)
        assertEquals(null, progAfter)
        assertEquals(null, settingsAfter)
        assertTrue("Match history should be empty after user deletion", matchesAfter.isEmpty())
    }

    private companion object {
        const val ALICE_EMAIL = "alice@zenithgolf.test"
        const val BOB_EMAIL = "bob@zenithgolf.test"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
