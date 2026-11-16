package com.example

import androidx.compose.ui.geometry.Offset
import com.example.game.ai.CaddieBrain
import com.example.game.course.CourseCatalog
import com.example.game.engine.GolfPhysicsEngine
import com.example.game.model.ClubType
import com.example.game.model.ShotAim
import com.example.game.model.SurfaceType
import com.example.game.model.WindCondition
import com.example.game.viewmodel.GolfGameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testCourseCatalogProvidesChampionshipHoles() {
        val holes = CourseCatalog.getChampionshipHoles()
        assertTrue("Course should have at least 6 championship holes", holes.size >= 6)
        assertEquals(1, holes[0].holeNumber)
        assertEquals(4, holes[0].par)
        assertTrue("Yardage should be positive", holes[0].yardage > 100)
    }

    @Test
    fun testPhysicsTrajectoryCalculation() {
        val hole = CourseCatalog.getChampionshipHoles()[0]
        val wind = WindCondition(speedMph = 10f, directionDeg = 90f)
        val aim = ShotAim(powerRatio = 1.0f, aimAngleDeg = 0f, curveSpin = 0f, apexTimingAccuracy = 1.0f)
        val trajectory = GolfPhysicsEngine.calculateAimTrajectory(
            start = hole.teePosition,
            club = ClubType.DRIVER,
            aim = aim,
            wind = wind,
            hole = hole
        )
        assertTrue("Trajectory should have simulated waypoints", trajectory.size > 10)
    }

    @Test
    fun testCaddieRecommendation() {
        val hole = CourseCatalog.getChampionshipHoles()[0]
        val wind = WindCondition(speedMph = 12f, directionDeg = 45f)
        val recommendation = CaddieBrain.analyzeShot(
            ballPos = hole.teePosition,
            hole = hole,
            lie = SurfaceType.TEE,
            wind = wind
        )
        assertNotNull(recommendation.recommendedClub)
        assertEquals(ClubType.DRIVER, recommendation.recommendedClub)
        assertTrue(recommendation.tacticalTip.isNotEmpty())
    }

    @Test
    fun testSurfaceDetection() {
        val hole = CourseCatalog.getChampionshipHoles()[0]
        val teeLie = GolfPhysicsEngine.getSurfaceAt(hole.teePosition, hole)
        assertEquals(SurfaceType.TEE, teeLie)

        val greenLie = GolfPhysicsEngine.getSurfaceAt(hole.greenCenter, hole)
        assertEquals(SurfaceType.GREEN, greenLie)
    }

    @Test
    fun testGolfGameViewModelStateManagement() {
        val viewModel = GolfGameViewModel()
        val state = viewModel.uiState.value

        // 1. Verify Player Coordinates
        assertNotNull(state.playerCoordinates)
        assertTrue("Player stance distance should be greater than zero", state.playerCoordinates.stanceDistance > 0f)
        val playerPos = state.playerCoordinates.position
        val ballPos = Offset(state.ballState.x, state.ballState.y)
        val distToBall = (playerPos - ballPos).getDistance()
        assertTrue("Player should be standing near ball stance position", distToBall > 1f && distToBall < 10f)

        // 2. Verify Ball Trajectory
        assertNotNull(state.ballTrajectory)
        assertTrue("Trajectory should have predicted waypoints", state.ballTrajectory.predictedPoints.isNotEmpty())

        // 3. Verify Current Hole Statistics
        val stats = state.holeStatistics
        assertNotNull(stats)
        assertEquals(state.currentHole.holeNumber, stats.holeNumber)
        assertEquals(state.currentHole.par, stats.par)
        assertEquals(state.currentHole.yardage, stats.totalHoleYardage)
        assertEquals(0, stats.strokesTaken)
        assertEquals(SurfaceType.TEE, stats.currentLie)
        assertTrue("Distance to pin should be positive", stats.remainingDistanceToPin > 0f)
    }

    @Test
    fun testCaddieChatServiceHeuristicResponse() = kotlinx.coroutines.test.runTest {
        val reply = com.example.game.ai.CaddieChatService.sendMessage(
            history = emptyList(),
            userMessage = "How do I play Hole 2 Island Green?"
        )
        assertNotNull(reply)
        assertTrue("Reply should contain tactical island advice", reply.contains("Island") || reply.contains("Hole 2"))
    }

    @Test
    fun testCustomBallStudioServiceProceduralCreation() = kotlinx.coroutines.test.runTest {
        val result = com.example.game.ai.CustomBallStudioService.generateCustomBall(
            prompt = "Neon plasma lightning ball with emerald glow",
            ballName = "Plasma Surge"
        )
        assertTrue(result.success)
        assertEquals("Plasma Surge", result.createdBall.name)
        assertTrue(result.createdBall.powerBonusPercent > 0)
    }

    @Test
    fun testDailyChallengesAndLuckyWheel() {
        val viewModel = GolfGameViewModel()
        val initialChallenges = viewModel.uiState.value.dailyChallenges
        assertTrue("Should have daily challenges", initialChallenges.isNotEmpty())

        val ch1 = initialChallenges[0]
        ch1.isCompleted = true
        val prevCoins = viewModel.uiState.value.career.coins
        viewModel.claimDailyChallenge(ch1.id)

        val updated = viewModel.uiState.value.dailyChallenges.first { it.id == ch1.id }
        assertTrue("Challenge should be claimed", updated.isClaimed)
        assertEquals(prevCoins + ch1.rewardCoins, viewModel.uiState.value.career.coins)

        // Test Lucky Wheel Spin
        val prize = com.example.game.model.LuckyWheelPrize(
            id = "test_jackpot",
            label = "TEST JACKPOT",
            subLabel = "+500 Coins",
            coins = 500,
            mulligans = 2,
            colorHex = 0xFFFFD700,
            isJackpot = true
        )
        val coinsBeforeSpin = viewModel.uiState.value.career.coins
        val mulligansBeforeSpin = viewModel.uiState.value.career.mulligansRemaining
        viewModel.spinLuckyWheel(prize)
        assertEquals(coinsBeforeSpin + 500, viewModel.uiState.value.career.coins)
        assertEquals(mulligansBeforeSpin + 2, viewModel.uiState.value.career.mulligansRemaining)
    }

    @Test
    fun testCameraAndWeatherCycling() {
        val viewModel = GolfGameViewModel()
        val initialCamera = viewModel.uiState.value.cameraMode
        viewModel.cycleCameraMode()
        assertFalse(initialCamera == viewModel.uiState.value.cameraMode)

        val initialWeather = viewModel.uiState.value.weather
        viewModel.cycleWeather()
        assertFalse(initialWeather == viewModel.uiState.value.weather)
    }

    @Test
    fun testMatchPlayDuelWorkflow() {
        val viewModel = GolfGameViewModel()
        val rivals = CourseCatalog.getDefaultMatchPlayRivals()
        assertTrue("Rivals should not be empty", rivals.isNotEmpty())
        val selectedRival = rivals[0]

        viewModel.startMatchPlayDuel(selectedRival, totalHoles = 3)
        val state = viewModel.uiState.value
        assertTrue("Match play state should be active", state.matchPlayState.isActive)
        assertEquals(1, state.matchPlayState.currentMatchHole)
        assertEquals(3, state.matchPlayState.totalMatchHoles)
        assertEquals(selectedRival.id, state.matchPlayState.rival?.id)
    }

    @Test
    fun testCourseArchitectHoleGeneration() {
        val blueprint = com.example.game.model.CustomHoleBlueprint(
            holeName = "Custom Test Lake Hole",
            par = 4,
            totalYardage = 420,
            fairwayShape = "Right Dogleg",
            greenStyle = "Wide Kidney",
            sandBunkerCount = 3,
            hasWaterLake = true
        )
        val customHole = CourseCatalog.createCustomHole(blueprint)
        assertEquals(4, customHole.par)
        assertEquals(420, customHole.yardage)
        assertEquals("Custom Test Lake Hole", customHole.holeTitle)
        assertTrue("Should contain fairways", customHole.fairways.isNotEmpty())
        assertTrue("Should contain hazards", customHole.hazards.isNotEmpty())
    }

    @Test
    fun testReplayTheaterVault() {
        val defaultReplays = CourseCatalog.getDefaultReplays()
        assertTrue("Should have default replays in catalog", defaultReplays.isNotEmpty())
        val firstReplay = defaultReplays[0]
        assertTrue(firstReplay.carryYards > 0)
        assertTrue(firstReplay.ballSpeedMph > 0)
    }

    @Test
    fun testClubSpecificShotCharacteristics() {
        val neutralAim = ShotAim(powerRatio = 1.0f, aimAngleDeg = 0f, curveSpin = 0f, apexTimingAccuracy = 1.0f, verticalSpin = 0f)
        val driverLaunch = com.example.game.engine.GolfClubController.calculateClubLaunch(ClubType.DRIVER, neutralAim, SurfaceType.TEE)
        val iron7Launch = com.example.game.engine.GolfClubController.calculateClubLaunch(ClubType.IRON_7, neutralAim, SurfaceType.FAIRWAY)
        val wedgeLaunch = com.example.game.engine.GolfClubController.calculateClubLaunch(ClubType.PITCHING_WEDGE, neutralAim, SurfaceType.FAIRWAY)
        val putterLaunch = com.example.game.engine.GolfClubController.calculateClubLaunch(ClubType.PUTTER, neutralAim, SurfaceType.GREEN)

        // Driver has highest ball speed and lowest launch angle among full clubs
        assertTrue("Driver ball speed should exceed 7-Iron", driverLaunch.ballSpeedMph > iron7Launch.ballSpeedMph)
        assertTrue("7-Iron ball speed should exceed Pitching Wedge", iron7Launch.ballSpeedMph > wedgeLaunch.ballSpeedMph)
        assertTrue("Pitching Wedge launch angle should exceed Driver", wedgeLaunch.dynamicLaunchAngleDeg > driverLaunch.dynamicLaunchAngleDeg)
        // Wedge generates highest backspin RPM
        assertTrue("Pitching Wedge backspin RPM should exceed Driver", wedgeLaunch.backspinRpm > driverLaunch.backspinRpm)
        // Putter stays on ground (0 launch angle, vz = 0)
        assertEquals(0f, putterLaunch.dynamicLaunchAngleDeg, 0.01f)
        assertEquals(0f, putterLaunch.vz, 0.01f)
    }

    @Test
    fun testBackspinTopspinAndSidespinPhysics() {
        val hole = CourseCatalog.getChampionshipHoles()[0]
        val calmWind = WindCondition(speedMph = 0f, directionDeg = 0f)

        val topspinAim = ShotAim(powerRatio = 0.9f, aimAngleDeg = 0f, curveSpin = 0f, apexTimingAccuracy = 1.0f, verticalSpin = -0.8f)
        val backspinAim = ShotAim(powerRatio = 0.9f, aimAngleDeg = 0f, curveSpin = 0f, apexTimingAccuracy = 1.0f, verticalSpin = 0.85f)

        val topspinReport = com.example.game.engine.GolfBallPhysics.simulateFullShotWithCollisions(
            startPos = hole.teePosition,
            club = ClubType.IRON_7,
            aim = topspinAim,
            wind = calmWind,
            hole = hole
        )
        val backspinReport = com.example.game.engine.GolfBallPhysics.simulateFullShotWithCollisions(
            startPos = hole.teePosition,
            club = ClubType.IRON_7,
            aim = backspinAim,
            wind = calmWind,
            hole = hole
        )

        // Backspin shot climbs to a higher apex and generates higher RPM than topspin shot
        assertTrue("Backspin shot should reach higher apex than topspin", backspinReport.apexAltitudeYds > topspinReport.apexAltitudeYds)
        assertTrue("Backspin RPM should exceed topspin RPM", backspinReport.launchTelemetry.backspinRpm > topspinReport.launchTelemetry.backspinRpm)

        // Sidespin: Draw (-0.75) vs Fade (+0.75) lateral deflection
        val drawAim = ShotAim(powerRatio = 0.9f, aimAngleDeg = 0f, curveSpin = -0.75f, apexTimingAccuracy = 1.0f)
        val fadeAim = ShotAim(powerRatio = 0.9f, aimAngleDeg = 0f, curveSpin = 0.75f, apexTimingAccuracy = 1.0f)
        val drawTraj = GolfPhysicsEngine.calculateAimTrajectory(hole.teePosition, ClubType.DRIVER, drawAim, calmWind, hole)
        val fadeTraj = GolfPhysicsEngine.calculateAimTrajectory(hole.teePosition, ClubType.DRIVER, fadeAim, calmWind, hole)

        assertTrue("Draw shot should curve left of Tee X", drawTraj.last().x < hole.teePosition.x)
        assertTrue("Fade shot should curve right of Tee X", fadeTraj.last().x > hole.teePosition.x)
    }

    @Test
    fun testWindSpeedDirectionAndAltitudeShear() {
        val wind = WindCondition(speedMph = 18f, directionDeg = 90f) // Blowing East (+X)
        val lowAltWind = com.example.game.engine.GolfWindSystem.getAltitudeAdjustedWindVector(wind, altitudeZ = 2f)
        val highAltWind = com.example.game.engine.GolfWindSystem.getAltitudeAdjustedWindVector(wind, altitudeZ = 35f)

        assertTrue("Upper-altitude wind shear should exceed ground-level wind", highAltWind.x > lowAltWind.x)

        // Decompose relative to North (0 deg) heading: 90 deg wind is pure crosswind
        val decomposed = com.example.game.engine.GolfWindSystem.decomposeWindRelativeToHeading(wind, aimHeadingDeg = 0f)
        assertEquals(0f, decomposed.headwindMph, 0.1f)
        assertEquals(18f, decomposed.crosswindMph, 0.1f)
    }

    @Test
    fun testTerrainSurfaceBounceFrictionAndPutting() {
        // 1. Compare Fairway vs Sand Bunker bounce restitution
        val fairwayBounce = com.example.game.engine.GolfSurfacePhysics.resolveGroundBounce(
            vx = 0f, vy = -25f, vz = -12f,
            groundLie = SurfaceType.FAIRWAY,
            backspinRpm = 3500,
            verticalSpinRatio = 0f
        )
        val bunkerBounce = com.example.game.engine.GolfSurfacePhysics.resolveGroundBounce(
            vx = 0f, vy = -25f, vz = -12f,
            groundLie = SurfaceType.SAND_BUNKER,
            backspinRpm = 3500,
            verticalSpinRatio = 0f
        )
        val waterBounce = com.example.game.engine.GolfSurfacePhysics.resolveGroundBounce(
            vx = 0f, vy = -25f, vz = -12f,
            groundLie = SurfaceType.WATER_HAZARD,
            backspinRpm = 3500,
            verticalSpinRatio = 0f
        )

        assertTrue("Fairway should produce an upward bounce", fairwayBounce.didBounce && fairwayBounce.newVz > 0f)
        assertFalse("Sand bunker should absorb steep impact without high bounce", bunkerBounce.didBounce)
        assertEquals(0f, waterBounce.reboundSpeed, 0.01f)

        // 2. Putting cup capture at proper speed vs lip-out at excessive speed
        val hole = CourseCatalog.getChampionshipHoles()[0]
        val nearPin = hole.pinPosition + Offset(0f, 2f)
        val capturedPutt = com.example.game.engine.GolfSurfacePhysics.stepGroundRollAndPutting(
            currentPos = nearPin,
            vx = 0f,
            vy = -5f,
            groundLie = SurfaceType.GREEN,
            hole = hole,
            weather = com.example.game.model.WeatherType.SUNNY,
            dt = 0.05f
        )
        assertTrue("Controlled putt entering cup radius should drop in hole", capturedPutt.inHole)
    }

    @Test
    fun testCollisionEventsAndPhysicsDebugToggle() {
        val viewModel = GolfGameViewModel()
        assertFalse(viewModel.uiState.value.isPhysicsDebugEnabled)
        viewModel.togglePhysicsDebug()
        assertTrue(viewModel.uiState.value.isPhysicsDebugEnabled)

        viewModel.setVerticalSpin(0.85f)
        assertEquals(0.85f, viewModel.uiState.value.verticalSpin, 0.01f)
        assertTrue("Backspin RPM in trajectory preview should update", viewModel.uiState.value.ballTrajectory.backspinRpm > 3000)

        val hole = CourseCatalog.getChampionshipHoles()[0]
        val aim = ShotAim(powerRatio = 0.85f, aimAngleDeg = 0f, curveSpin = 0f, apexTimingAccuracy = 1.0f, verticalSpin = 0f)
        val report = com.example.game.engine.GolfBallPhysics.simulateFullShotWithCollisions(
            startPos = hole.teePosition,
            club = ClubType.DRIVER,
            aim = aim,
            wind = WindCondition(5f, 0f),
            hole = hole
        )
        assertTrue("Full shot simulation should record ground bounce collision events", report.collisionEvents.isNotEmpty())
        assertTrue("Peak aerodynamic drag force should be positive", report.peakDragForceN > 0f)
        assertTrue("Peak Magnus lift force should be positive", report.peakMagnusForceN > 0f)
    }

    @Test
    fun testLandscapeClubSelectorAndVerticalSwingMeterPhases() {
        val clubs = com.example.game.ui.components.PRIMARY_LANDSCAPE_CLUBS
        assertEquals(4, clubs.size)
        assertEquals("Driver", clubs[0].label)
        assertEquals("Iron", clubs[1].label)
        assertEquals("Chip", clubs[2].label)
        assertEquals("Putter", clubs[3].label)

        val state0 = com.example.game.viewmodel.GameUiState()
        assertEquals(com.example.game.viewmodel.SwingMeterPhase.IDLE, state0.swingMeterPhase)
    }

    @Test
    fun testDragAndReleaseSwingControlDeadZoneCancelAndClubCap() {
        val viewModel = GolfGameViewModel()
        val touchStart = Offset(400f, 300f)
        val ballScreen = Offset(400f, 300f)

        // 1. Touch down starts drag without launching ball
        viewModel.onSwingDragStart(touchStart)
        assertTrue(viewModel.uiState.value.isDraggingSwing)
        assertTrue(viewModel.uiState.value.isSwingInCancelZone)
        assertFalse(viewModel.uiState.value.isBallInFlight)

        // 2. Tiny drag inside dead zone (< 28px) does not trigger a shot on release
        viewModel.onSwingDragUpdate(
            currentScreenPos = touchStart + Offset(0f, 12f),
            ballScreenPos = ballScreen,
            maxPullDistancePx = 200f
        )
        assertTrue(viewModel.uiState.value.isSwingInCancelZone)
        viewModel.onSwingDragRelease()
        assertFalse("Tiny drag inside dead zone should not launch ball", viewModel.uiState.value.isBallInFlight)
        assertEquals(0, viewModel.uiState.value.currentStrokeCount)

        // 3. Pull back down-left (dx = -50, dy = +150): shot direction is opposite (up-right, angle > 0)
        viewModel.onSwingDragStart(touchStart)
        viewModel.onSwingDragUpdate(
            currentScreenPos = touchStart + Offset(-50f, 150f),
            ballScreenPos = ballScreen,
            maxPullDistancePx = 200f
        )
        val activeState = viewModel.uiState.value
        assertFalse("Pull past dead zone should exit cancel zone", activeState.isSwingInCancelZone)
        assertTrue("Shot direction should be opposite to pull (right of 0 deg)", activeState.aimAngleDeg > 10f)
        assertTrue("Power should scale with pullback distance", activeState.aimPowerRatio > 0.5f)
        assertFalse("Ball must never launch while dragging", activeState.isBallInFlight)

        // 4. Dragging back to the ball cancels the swing on release
        viewModel.onSwingDragUpdate(
            currentScreenPos = ballScreen + Offset(5f, 5f),
            ballScreenPos = ballScreen,
            maxPullDistancePx = 200f
        )
        assertTrue("Dragging back to ball should enter cancel zone", viewModel.uiState.value.isSwingInCancelZone)
        viewModel.onSwingDragRelease()
        assertFalse("Releasing in ball cancel zone must not launch ball", viewModel.uiState.value.isBallInFlight)
        assertEquals(0, viewModel.uiState.value.currentStrokeCount)
        assertEquals("SWING CANCELLED", viewModel.uiState.value.apexQualityText)

        // 5. Power is capped at selected club's max distance (Driver longest, Putter shortest)
        viewModel.selectClub(ClubType.DRIVER)
        viewModel.onSwingDragStart(touchStart)
        viewModel.onSwingDragUpdate(touchStart + Offset(0f, 500f), ballScreen, 200f)
        val driverMaxTarget = viewModel.uiState.value.currentPullbackTargetYds
        assertEquals(ClubType.DRIVER.maxDistanceYds, driverMaxTarget, 0.1f)
        viewModel.onSwingDragCancel()

        viewModel.selectClub(ClubType.PUTTER)
        viewModel.onSwingDragStart(touchStart)
        viewModel.onSwingDragUpdate(touchStart + Offset(0f, 500f), ballScreen, 200f)
        val putterMaxTarget = viewModel.uiState.value.currentPullbackTargetYds
        assertEquals(ClubType.PUTTER.maxDistanceYds, putterMaxTarget, 0.1f)
        assertTrue("Driver max pullback distance must exceed Putter", driverMaxTarget > putterMaxTarget)
        viewModel.onSwingDragCancel()
    }

    @Test
    fun testZenithGolf20SettingsAchievementsTreePhysicsAndAiDifficulty() {
        val viewModel = GolfGameViewModel()

        // 1. Settings -> Audio/Haptic Engine synchronization
        val customSettings = viewModel.uiState.value.playSettings.copy(
            isSoundEnabled = false,
            soundVolume = 0.42f,
            isHapticsEnabled = false,
            aiDifficulty = com.example.game.model.AiDifficultyTier.APEX_MASTER
        )
        viewModel.updatePlaySettings(customSettings)
        assertFalse(com.example.game.engine.GolfAudioHapticEngine.isSoundEnabled)
        assertEquals(0.42f, com.example.game.engine.GolfAudioHapticEngine.soundVolume, 0.01f)
        assertFalse(com.example.game.engine.GolfAudioHapticEngine.isHapticsEnabled)

        // Restore sound/haptics
        viewModel.updatePlaySettings(customSettings.copy(isSoundEnabled = true, isHapticsEnabled = true))
        assertTrue(com.example.game.engine.GolfAudioHapticEngine.isSoundEnabled)
        assertTrue(com.example.game.engine.GolfAudioHapticEngine.isHapticsEnabled)

        // 2. Achievement unlocking awards Tour XP once and is idempotent
        val xpBefore = viewModel.uiState.value.career.tourXp
        viewModel.unlockAchievement("ach_eagle")
        val eagleAch = viewModel.uiState.value.achievements.first { it.id == "ach_eagle" }
        assertTrue("Eagle achievement should be unlocked", eagleAch.isUnlocked)
        assertEquals(xpBefore + eagleAch.xpValue, viewModel.uiState.value.career.tourXp)

        // Unlocking again does not double-award XP
        val xpAfterFirstUnlock = viewModel.uiState.value.career.tourXp
        viewModel.unlockAchievement("ach_eagle")
        assertEquals(xpAfterFirstUnlock, viewModel.uiState.value.career.tourXp)

        // 3. 3D Tree Canopy Collision deflection
        val hole = CourseCatalog.getChampionshipHoles()[0]
        assertTrue("Hole should have corridor tree obstacles", hole.treeObstacles.isNotEmpty())
        val firstTree = hole.treeObstacles.first()
        val treeCollision = com.example.game.engine.GolfSurfacePhysics.resolveTreeCanopyCollision(
            ballPos = firstTree + Offset(2f, 2f),
            altitudeZ = 10f,
            vx = 5f,
            vy = -40f,
            vz = 8f,
            hole = hole
        )
        assertTrue("Airborne ball inside tree canopy radius should register tree collision", treeCollision.hitTree)
        assertTrue("Tree collision should deflect ball downward", treeCollision.newVz < 0f)

        // 4. AI Difficulty scaling
        val rival = CaddieBrain.createDefaultRivals().first()
        val rookieScores = (1..40).map {
            CaddieBrain.simulateRivalScore(rival, hole, com.example.game.model.AiDifficultyTier.ROOKIE)
        }.average()
        val masterScores = (1..40).map {
            CaddieBrain.simulateRivalScore(rival, hole, com.example.game.model.AiDifficultyTier.APEX_MASTER)
        }.average()
        assertTrue("Apex Master AI should average lower strokes than Rookie AI", masterScores < rookieScores)
    }

    @Test
    fun testZenithGolf20FrontendNavigationAndBottomTabs() {
        val viewModel = GolfGameViewModel()

        // Verify 5 bottom navigation tabs (Home | Play | Events | Rewards | Profile)
        val tabs = com.example.game.ui.components.ZENITH_BOTTOM_TABS
        assertEquals(5, tabs.size)
        assertEquals("Home", tabs[0].label)
        assertEquals("Play", tabs[1].label)
        assertEquals("Events", tabs[2].label)
        assertEquals("Rewards", tabs[3].label)
        assertEquals("Profile", tabs[4].label)

        // Verify navigation across all 9 frontend screens
        val allNineScreens = listOf(
            com.example.game.viewmodel.AppScreen.SPLASH,
            com.example.game.viewmodel.AppScreen.HOME,
            com.example.game.viewmodel.AppScreen.GAME_MODES,
            com.example.game.viewmodel.AppScreen.COURSE_SELECTION,
            com.example.game.viewmodel.AppScreen.CAREER_STATS,
            com.example.game.viewmodel.AppScreen.PRO_SHOP,
            com.example.game.viewmodel.AppScreen.CUP_SELECT,
            com.example.game.viewmodel.AppScreen.DAILY_CHALLENGES,
            com.example.game.viewmodel.AppScreen.PLAY_SETTINGS
        )
        allNineScreens.forEach { screen ->
            viewModel.navigateTo(screen)
            assertEquals(screen, viewModel.uiState.value.currentScreen)
        }

        // Verify bottom bar visibility rules (visible on hub screens, hidden on SPLASH & PLAY_HOLE)
        assertFalse(com.example.game.ui.components.isBottomBarVisibleForScreen(com.example.game.viewmodel.AppScreen.SPLASH))
        assertFalse(com.example.game.ui.components.isBottomBarVisibleForScreen(com.example.game.viewmodel.AppScreen.PLAY_HOLE))
        assertTrue(com.example.game.ui.components.isBottomBarVisibleForScreen(com.example.game.viewmodel.AppScreen.HOME))
        assertTrue(com.example.game.ui.components.isBottomBarVisibleForScreen(com.example.game.viewmodel.AppScreen.GAME_MODES))
        assertTrue(com.example.game.ui.components.isBottomBarVisibleForScreen(com.example.game.viewmodel.AppScreen.COURSE_SELECTION))
    }

    @Test
    fun testZenithGolf20GameplayPhysicsClubsScoringAndPauseUpgrade() {
        val hole = CourseCatalog.getChampionshipHoles()[0]
        val calmWind = WindCondition(speedMph = 6f, directionDeg = 45f)
        val aim = ShotAim(powerRatio = 0.90f, aimAngleDeg = 0f, curveSpin = 0.25f, apexTimingAccuracy = 1.0f, verticalSpin = 0.20f)

        // 1. Deterministic Physics Model (Zero random ball movement)
        val sim1 = com.example.game.engine.GolfBallPhysics.simulateFullShotWithCollisions(
            startPos = hole.teePosition,
            club = ClubType.DRIVER,
            aim = aim,
            wind = calmWind,
            hole = hole
        )
        val sim2 = com.example.game.engine.GolfBallPhysics.simulateFullShotWithCollisions(
            startPos = hole.teePosition,
            club = ClubType.DRIVER,
            aim = aim,
            wind = calmWind,
            hole = hole
        )
        assertEquals(sim1.carryDistanceYds, sim2.carryDistanceYds, 0.001f)
        assertEquals(sim1.totalDistanceYds, sim2.totalDistanceYds, 0.001f)
        assertEquals(sim1.endPosition.x, sim2.endPosition.x, 0.001f)
        assertEquals(sim1.endPosition.y, sim2.endPosition.y, 0.001f)

        // 2. All 6 Club Categories (Driver, Fairway Wood, Long Iron, Short Irons, Wedges, Putter)
        // Verify distinct expected distance ranges and progressive distance hierarchy
        assertTrue(ClubType.DRIVER.baseDistanceYds > ClubType.WOOD_3.baseDistanceYds)
        assertTrue(ClubType.WOOD_3.baseDistanceYds > ClubType.IRON_4.baseDistanceYds)
        assertTrue(ClubType.IRON_4.baseDistanceYds > ClubType.IRON_7.baseDistanceYds)
        assertTrue(ClubType.IRON_7.baseDistanceYds > ClubType.IRON_9.baseDistanceYds)
        assertTrue(ClubType.IRON_9.baseDistanceYds > ClubType.PITCHING_WEDGE.baseDistanceYds)
        assertTrue(ClubType.PITCHING_WEDGE.baseDistanceYds > ClubType.SAND_WEDGE.baseDistanceYds)
        assertTrue(ClubType.SAND_WEDGE.baseDistanceYds > ClubType.PUTTER.baseDistanceYds)

        // Sand Wedge outperforms Long Iron out of a Sand Bunker
        val swBunker = com.example.game.engine.GolfClubController.calculateClubLaunch(ClubType.SAND_WEDGE, aim, SurfaceType.SAND_BUNKER)
        val iron4Bunker = com.example.game.engine.GolfClubController.calculateClubLaunch(ClubType.IRON_4, aim, SurfaceType.SAND_BUNKER)
        assertTrue("Sand Wedge backspin in bunker should exceed 4-Iron", swBunker.backspinRpm > iron4Bunker.backspinRpm)

        // 3. Course Conditions: Elevation Carry Difference (Downhill vs Uphill)
        val uphillHole = hole.copy(elevationDiffYds = 20f)
        val downhillHole = hole.copy(elevationDiffYds = -20f)
        val uphillShot = com.example.game.engine.GolfBallPhysics.simulateFullShotWithCollisions(
            startPos = hole.teePosition,
            club = ClubType.IRON_7,
            aim = aim,
            wind = WindCondition(0f, 0f),
            hole = uphillHole
        )
        val downhillShot = com.example.game.engine.GolfBallPhysics.simulateFullShotWithCollisions(
            startPos = hole.teePosition,
            club = ClubType.IRON_7,
            aim = aim,
            wind = WindCondition(0f, 0f),
            hole = downhillHole
        )
        assertTrue("Downhill hole should yield longer carry than uphill hole", downhillShot.carryDistanceYds > uphillShot.carryDistanceYds)

        // 4. Camera System (4 distinct modes: Overview, Ball Tracking, Putting, 3rd-Person Golfer)
        val camModes = com.example.game.model.CameraViewMode.values()
        assertEquals(4, camModes.size)

        // 5. Scoring Engine (Hole-in-One, Eagle, Birdie, Par, Bogey, Double Bogey, Total Strokes)
        val aceScore = com.example.game.model.GolfScoringEngine.evaluateHoleScore(strokes = 1, par = 3)
        val eagleScore = com.example.game.model.GolfScoringEngine.evaluateHoleScore(strokes = 3, par = 5)
        val birdieScore = com.example.game.model.GolfScoringEngine.evaluateHoleScore(strokes = 3, par = 4)
        val parScore = com.example.game.model.GolfScoringEngine.evaluateHoleScore(strokes = 4, par = 4)
        val bogeyScore = com.example.game.model.GolfScoringEngine.evaluateHoleScore(strokes = 5, par = 4)
        val doubleBogeyScore = com.example.game.model.GolfScoringEngine.evaluateHoleScore(strokes = 6, par = 4)

        assertEquals("HOLE-IN-ONE", aceScore.term)
        assertEquals("EAGLE", eagleScore.term)
        assertEquals("BIRDIE", birdieScore.term)
        assertEquals("PAR", parScore.term)
        assertEquals("BOGEY", bogeyScore.term)
        assertEquals("DOUBLE BOGEY", doubleBogeyScore.term)
        assertEquals("-2", com.example.game.model.GolfScoringEngine.formatTotalScoreToPar(totalStrokes = 70, totalPar = 72))

        // 6. Pause, Resume, and Shot Confirmation in ViewModel
        val viewModel = GolfGameViewModel()
        assertFalse(viewModel.uiState.value.isGamePaused)
        viewModel.pauseGame()
        assertTrue(viewModel.uiState.value.isGamePaused)
        viewModel.resumeGame()
        assertFalse(viewModel.uiState.value.isGamePaused)
    }
}

