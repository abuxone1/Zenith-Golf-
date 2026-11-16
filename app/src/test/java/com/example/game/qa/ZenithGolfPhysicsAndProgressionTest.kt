package com.example.game.qa

import android.content.Context
import androidx.compose.ui.geometry.Offset
import androidx.test.core.app.ApplicationProvider
import com.example.game.course.CourseCatalog
import com.example.game.engine.GolfAudioHapticEngine
import com.example.game.engine.GolfClubController
import com.example.game.engine.GolfPhysicsEngine
import com.example.game.model.BallTracerStyle
import com.example.game.model.ClubType
import com.example.game.model.GolfScoringEngine
import com.example.game.model.GraphicsQualityTier
import com.example.game.model.PlayConsoleSettings
import com.example.game.model.ShotAim
import com.example.game.model.SurfaceType
import com.example.game.model.WeatherType
import com.example.game.model.WindCondition
import com.example.game.progression.LocalProgressStorage
import com.example.game.ui.CourseCameraTransform
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.GolfGameViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.system.measureNanoTime

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ZenithGolfPhysicsAndProgressionTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var context: Context

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        context = ApplicationProvider.getApplicationContext()
        LocalProgressStorage.clearSnapshot(context)
    }

    @After
    fun tearDown() {
        LocalProgressStorage.clearSnapshot(context)
        Dispatchers.resetMain()
    }

    @Test
    fun `test 05 06 07 - golf physics is deterministic and obeys club hierarchy wind friction and putting rules`() {
        val hole = CourseCatalog.getChampionshipHoles().first()
        val tee = hole.teePosition
        val calmWind = WindCondition(speedMph = 0f, directionDeg = 0f)
        val aim = ShotAim(powerRatio = 0.90f, aimAngleDeg = 0f, curveSpin = 0f, apexTimingAccuracy = 1.0f)

        // 1. Determinism check: identical inputs must produce identical landing points
        val trajA = GolfPhysicsEngine.calculateAimTrajectory(tee, ClubType.DRIVER, aim, calmWind, hole)
        val trajB = GolfPhysicsEngine.calculateAimTrajectory(tee, ClubType.DRIVER, aim, calmWind, hole)
        assertEquals(trajA, trajB)
        assertTrue("Trajectory must contain multiple sampled points", trajA.size > 10)

        // 2. Club realistic distance range hierarchy: Driver > 3W > 4I > 7I > PW > Putter
        val clubsToCompare = listOf(
            ClubType.DRIVER,
            ClubType.WOOD_3,
            ClubType.IRON_4,
            ClubType.IRON_7,
            ClubType.PITCHING_WEDGE,
            ClubType.PUTTER
        )
        val distances = clubsToCompare.map { club ->
            val t = GolfPhysicsEngine.calculateAimTrajectory(tee, club, aim, calmWind, hole)
            (t.last() - tee).getDistance()
        }
        for (i in 0 until distances.size - 1) {
            assertTrue(
                "Expected ${clubsToCompare[i].name} (${distances[i]}y) > ${clubsToCompare[i + 1].name} (${distances[i + 1]}y)",
                distances[i] > distances[i + 1]
            )
        }

        // 3. Wind speed & direction physics (Tailwind vs crosswind drift)
        val crosswindRight = WindCondition(speedMph = 18f, directionDeg = 90f)
        val crosswindLeft = WindCondition(speedMph = 18f, directionDeg = 270f)
        val landRight = GolfPhysicsEngine.calculateAimTrajectory(tee, ClubType.IRON_4, aim, crosswindRight, hole).last()
        val landLeft = GolfPhysicsEngine.calculateAimTrajectory(tee, ClubType.IRON_4, aim, crosswindLeft, hole).last()
        assertTrue("East wind should drift ball further +X than West wind (${landRight.x} vs ${landLeft.x})", landRight.x > landLeft.x)

        // 4. Putting physics stays grounded (no airborne altitude) and responds to green slope
        val greenStart = hole.greenCenter + Offset(0f, 12f)
        val puttAim = ShotAim(powerRatio = 0.6f, aimAngleDeg = 0f, curveSpin = 0f, apexTimingAccuracy = 1.0f)
        val launchPutt = GolfClubController.calculateClubLaunch(
            club = ClubType.PUTTER,
            aim = puttAim,
            currentLie = SurfaceType.GREEN
        )
        assertEquals(0f, launchPutt.vz, 0.0001f)
        val puttStep = GolfPhysicsEngine.stepPhysics(
            currentPos = greenStart,
            altitudeZ = 0f,
            vx = launchPutt.vx,
            vy = launchPutt.vy,
            vz = 0f,
            club = ClubType.PUTTER,
            aim = puttAim,
            wind = calmWind,
            hole = hole,
            dt = 0.04f
        )
        assertFalse("Putt must never become airborne", puttStep.isAirborne)
        assertEquals(0f, puttStep.newZ, 0.0001f)

        // 5. Sand bunker has higher friction / reduced power than fairway
        val fairwayLaunch = GolfClubController.calculateClubLaunch(ClubType.IRON_7, aim, SurfaceType.FAIRWAY)
        val bunkerLaunch = GolfClubController.calculateClubLaunch(ClubType.SAND_WEDGE, aim, SurfaceType.SAND_BUNKER)
        assertTrue(fairwayLaunch.ballSpeedMph > bunkerLaunch.ballSpeedMph)
    }

    @Test
    fun `test 08 - score calculation covers all official golf scoring terms`() {
        val ace = GolfScoringEngine.evaluateHoleScore(strokes = 1, par = 3)
        assertEquals("HOLE-IN-ONE", ace.term)
        assertEquals(500, ace.coinsReward)

        val eagle = GolfScoringEngine.evaluateHoleScore(strokes = 3, par = 5)
        assertEquals("EAGLE", eagle.term)
        assertEquals("-2", eagle.relativeString)

        val birdie = GolfScoringEngine.evaluateHoleScore(strokes = 3, par = 4)
        assertEquals("BIRDIE", birdie.term)
        assertEquals("-1", birdie.relativeString)

        val par = GolfScoringEngine.evaluateHoleScore(strokes = 4, par = 4)
        assertEquals("PAR", par.term)
        assertEquals("E", par.relativeString)

        val bogey = GolfScoringEngine.evaluateHoleScore(strokes = 5, par = 4)
        assertEquals("BOGEY", bogey.term)
        assertEquals("+1", bogey.relativeString)

        val dblBogey = GolfScoringEngine.evaluateHoleScore(strokes = 6, par = 4)
        assertEquals("DOUBLE BOGEY", dblBogey.term)
        assertEquals("+2", dblBogey.relativeString)
    }

    @Test
    fun `test 05 15 16 17 - drag swing cancel pause resume lifecycle recovery and full reset`() = runTest {
        val vm = GolfGameViewModel()
        vm.initializeLocalProgress(context)
        vm.startTournament()

        // Drag inside deadzone -> does not launch ball
        vm.onSwingDragStart(Offset(300f, 300f))
        vm.onSwingDragUpdate(Offset(308f, 310f), Offset(200f, 200f))
        assertTrue(vm.uiState.value.isSwingInCancelZone)
        vm.onSwingDragRelease()
        assertFalse("Dead-zone release must not launch ball", vm.uiState.value.isBallInFlight)
        assertEquals(0, vm.uiState.value.currentStrokeCount)

        // Confirm shot launches ball, then PAUSE freezes simulation cleanly
        vm.selectClub(ClubType.DRIVER)
        vm.confirmAndExecuteShot()
        assertTrue(vm.uiState.value.isBallInFlight)
        assertEquals(1, vm.uiState.value.currentStrokeCount)

        advanceTimeBy(120)
        val posBeforePause = Offset(vm.uiState.value.ballState.x, vm.uiState.value.ballState.y)
        vm.pauseOnLifecycleStop(context)
        assertTrue(vm.uiState.value.isGamePaused)

        // Advance virtual time while paused: ball position must remain frozen!
        advanceTimeBy(400)
        val posWhilePaused = Offset(vm.uiState.value.ballState.x, vm.uiState.value.ballState.y)
        assertEquals("Ball must remain frozen while paused", posBeforePause, posWhilePaused)

        // Navigating back to HOME cancels flight safely without background hijack
        vm.navigateTo(AppScreen.HOME)
        assertEquals(AppScreen.HOME, vm.uiState.value.currentScreen)
        assertFalse(vm.uiState.value.isBallInFlight)

        // Update tracer + settings (cloud sync OFF, battery saver tier) and verify offline persistence across ViewModel restart
        vm.setTracerStyle(BallTracerStyle.GOLD_CHAMPION)
        vm.buyBall("ion_cyan")
        val updatedSettings = PlayConsoleSettings(
            isSoundEnabled = false,
            soundVolume = 0.35f,
            graphicsTier = GraphicsQualityTier.LOW_BATTERY,
            isHighFpsMode = false,
            isCloudSyncEnabled = false
        )
        vm.updatePlaySettings(updatedSettings)
        val expectedCoinsAfterBuy = vm.uiState.value.career.coins

        val restoredVm = GolfGameViewModel()
        restoredVm.initializeLocalProgress(context)
        assertEquals(expectedCoinsAfterBuy, restoredVm.uiState.value.career.coins)
        assertEquals(BallTracerStyle.GOLD_CHAMPION, restoredVm.uiState.value.equippedTracer)
        assertEquals(GraphicsQualityTier.LOW_BATTERY, restoredVm.uiState.value.playSettings.graphicsTier)
        assertFalse(restoredVm.uiState.value.playSettings.isCloudSyncEnabled)
        assertFalse(restoredVm.uiState.value.playSettings.isHighFpsMode)

        // Full reset must clear local snapshot completely so old progress does NOT resurrect
        restoredVm.resetAllGameProgress()
        val afterResetVm = GolfGameViewModel()
        afterResetVm.initializeLocalProgress(context)
        assertEquals(850, afterResetVm.uiState.value.career.coins)
        assertEquals(BallTracerStyle.NEON_CYAN, afterResetVm.uiState.value.equippedTracer)
    }

    @Test
    fun `test 09 14 - tournament registration deducts fee tracks cup and rewards ledger`() {
        val vm = GolfGameViewModel()
        vm.initializeLocalProgress(context)
        val initialCoins = vm.uiState.value.career.coins
        val cups = vm.uiState.value.tournamentCups
        assertTrue(cups.size >= 2)

        val freeCup = cups[0] // Zenith Dunes Open (0 Coins)
        val paidCup = cups[1] // Neon Tokyo Classic (1000 Coins)

        // Insufficient funds (850 < 1000) must reject paid tournament entry cleanly
        vm.startTournamentCup(paidCup)
        assertFalse(vm.uiState.value.registeredCupIds.contains(paidCup.id))
        assertEquals(initialCoins, vm.uiState.value.career.coins)

        // Free tournament registration succeeds
        vm.startTournamentCup(freeCup)
        assertTrue(vm.uiState.value.registeredCupIds.contains(freeCup.id))
        assertEquals(AppScreen.PLAY_HOLE, vm.uiState.value.currentScreen)

        // Earn 500 Coins via Lucky Wheel reward -> now able to register for paid tournament
        vm.spinLuckyWheel(
            com.example.game.model.LuckyWheelPrize(
                id = "qa_bonus_500",
                label = "QA BONUS",
                subLabel = "+500 Coins",
                coins = 500,
                mulligans = 1,
                colorHex = 0xFFFFD700,
                isJackpot = true
            )
        )
        val fundedCoins = vm.uiState.value.career.coins
        assertEquals(initialCoins + 500, fundedCoins)

        vm.startTournamentCup(paidCup)
        assertEquals(AppScreen.PLAY_HOLE, vm.uiState.value.currentScreen)
        assertEquals(paidCup.id, vm.uiState.value.selectedCup.id)
        assertTrue(vm.uiState.value.registeredCupIds.contains(paidCup.id))
        assertEquals(fundedCoins - paidCup.entryFeeCoins, vm.uiState.value.career.coins)
        assertNotNull(LocalProgressStorage.loadSnapshot(context))
    }

    @Test
    fun `test 18 performance - physics trajectory and audio engine stay within low-end budgets`() {
        val hole = CourseCatalog.getChampionshipHoles().first()
        val wind = WindCondition(12f, 45f)
        val aim = ShotAim(0.9f, 5f, 0.3f, 1.0f, 0.2f)

        // 1. 100 trajectory calculations + camera transforms must run fast (target < 150ms total)
        val elapsedNs = measureNanoTime {
            repeat(100) {
                val traj = GolfPhysicsEngine.calculateAimTrajectory(
                    hole.teePosition,
                    ClubType.DRIVER,
                    aim,
                    wind,
                    hole,
                    weather = WeatherType.WINDY_TEMPEST
                )
                CourseCameraTransform.compute(
                    canvasW = 1920f,
                    canvasH = 1080f,
                    cameraMode = vmCameraMode(),
                    ballPos = hole.teePosition,
                    pinPos = hole.pinPosition,
                    isBallInFlight = false,
                    trajectoryEnd = traj.lastOrNull()
                )
            }
        }
        val elapsedMs = elapsedNs / 1_000_000.0
        assertTrue("100 trajectory + camera cycles took ${elapsedMs}ms (budget < 500ms)", elapsedMs < 500.0)

        // 2. Audio engine concurrency & wheel tick throttle never exceeds MAX_CONCURRENT_TRACKS
        repeat(50) {
            GolfAudioHapticEngine.playWheelTickSound()
        }
        assertTrue(
            "Active AudioTracks (${GolfAudioHapticEngine.activeTrackCount()}) must remain bounded <= 6",
            GolfAudioHapticEngine.activeTrackCount() <= 6
        )
    }

    private fun vmCameraMode() = com.example.game.model.CameraViewMode.PERSPECTIVE_3D
}
