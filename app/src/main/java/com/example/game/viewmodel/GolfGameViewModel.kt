package com.example.game.viewmodel

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.ai.CaddieBrain
import com.example.game.course.CourseCatalog
import com.example.game.engine.GolfBallPhysics
import com.example.game.engine.GolfClubController
import com.example.game.engine.GolfPhysicsEngine
import com.example.game.model.PhysicsCollisionPoint
import com.example.game.model.BallState
import com.example.game.model.ClubType
import com.example.game.model.ClubUpgrade
import com.example.game.model.GolfBallSkin
import com.example.game.model.Hole
import com.example.game.model.PlayerCareer
import com.example.game.model.ShotAim
import com.example.game.model.SurfaceType
import com.example.game.model.TournamentRival
import com.example.game.model.WindCondition
import com.example.game.progression.EquipmentCatalog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.sqrt

import com.example.game.engine.GolfAudioHapticEngine
import com.example.game.model.BallTracerStyle
import com.example.game.model.BallTrajectory
import com.example.game.model.CameraViewMode
import com.example.game.model.DailyChallenge
import com.example.game.model.GpsRangefinderData
import com.example.game.model.HoleShotLog
import com.example.game.model.HoleStatistics
import com.example.game.model.LuckyWheelPrize
import com.example.game.model.PlayAchievement
import com.example.game.model.PlayConsoleSettings
import com.example.game.model.PlayerCoordinates
import com.example.game.model.ShootoutGameState
import com.example.game.model.ShootoutShotResult
import com.example.game.model.TourPassState
import com.example.game.firebase.CloudLeaderboardEntry
import com.example.game.firebase.CloudMatchHistoryRecord
import com.example.game.firebase.GolfCloudRepository
import com.example.game.firebase.GolfSecureBackendClient
import com.example.game.model.TournamentCup
import com.example.game.model.WeatherType
import com.example.game.model.MatchPlayRival
import com.example.game.model.MatchPlayState
import com.example.game.model.MatchPlayHoleResult
import com.example.game.model.ReplayShot
import com.example.game.model.CustomHoleBlueprint
import com.example.game.model.GolferProfile
import com.example.game.model.BallLandingEffect
import com.example.game.model.GolfScoringEngine
import com.example.game.model.AppNotificationItem
import com.example.game.model.ZenithThemeMode
import com.example.game.progression.LocalProgressStorage
import android.content.Context
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

enum class AppScreen {
    SPLASH,
    HOME,
    GAME_MODES,
    COURSE_SELECTION,
    PLAY_HOLE,
    HOLE_SUMMARY,
    TOURNAMENT_LEADERBOARD,
    PRACTICE_RANGE,
    PRO_SHOP,
    CAREER_STATS,
    CADDIE_CHAT,
    CUSTOM_BALL_STUDIO,
    DAILY_CHALLENGES,
    TARGET_SHOOTOUT,
    TOUR_PASS,
    CUP_SELECT,
    PLAY_SETTINGS,
    MATCH_PLAY_DUEL,
    REPLAY_THEATER,
    COURSE_ARCHITECT,
    GOLFER_LOCKER_ROOM,
    TECHNICAL_BLUEPRINT
}

enum class SwingMeterPhase {
    IDLE,
    POWER,
    ACCURACY
}

data class GameUiState(
    val currentScreen: AppScreen = AppScreen.HOME,
    val previousScreen: AppScreen = AppScreen.HOME,
    val registeredCupIds: Set<String> = emptySet(),
    val holes: List<Hole> = CourseCatalog.getChampionshipHoles(),
    val currentHoleIndex: Int = 0,
    val currentHole: Hole = CourseCatalog.getChampionshipHoles()[0],
    val currentStrokeCount: Int = 0,
    val ballState: BallState = BallState(
        x = CourseCatalog.getChampionshipHoles()[0].teePosition.x,
        y = CourseCatalog.getChampionshipHoles()[0].teePosition.y
    ),
    val previousBallPos: Offset? = null,
    val shotHistory: List<Offset> = emptyList(),
    val wind: WindCondition = WindCondition(speedMph = 8f, directionDeg = 45f),
    val weather: WeatherType = WeatherType.SUNNY,
    val cameraMode: CameraViewMode = CameraViewMode.OVERVIEW,
    val selectedClub: ClubType = ClubType.DRIVER,
    val aimPowerRatio: Float = 0.85f,
    val aimAngleDeg: Float = 0f,
    val curveSpin: Float = 0f,
    val verticalSpin: Float = 0f, // -1.0f Topspin to +1.0f Backspin
    val isPhysicsDebugEnabled: Boolean = false,
    val swingMeterPhase: SwingMeterPhase = SwingMeterPhase.IDLE,
    val isDraggingSwing: Boolean = false,
    val dragStartScreenOffset: Offset = Offset.Zero,
    val dragCurrentScreenOffset: Offset = Offset.Zero,
    val hasLeftDeadZoneDuringDrag: Boolean = false,
    val isSwingInCancelZone: Boolean = false,
    val isClubStrikeAnimating: Boolean = false,
    val clubSwingAnimAngleDeg: Float = 0f,
    val isApexTimingActive: Boolean = false,
    val apexMeterPosition: Float = 0f, // -1.0f to +1.0f oscillating
    val apexQualityText: String? = null,
    val isBallInFlight: Boolean = false,
    val trajectoryPreview: List<Offset> = emptyList(),
    val ballTrajectory: BallTrajectory = BallTrajectory(),
    val playerCoordinates: PlayerCoordinates = PlayerCoordinates(
        position = CourseCatalog.getChampionshipHoles()[0].teePosition + Offset(-3.5f, 0f),
        facingAngleDeg = 90f
    ),
    val holeShotLogs: List<HoleShotLog> = emptyList(),
    val dailyChallenges: List<DailyChallenge> = listOf(
        DailyChallenge("ch_1", "Island Sniper", "Hit within 8 yards of pin on Hole 2 Island Green", 250, 1, false, "ISLAND_ACCURACY"),
        DailyChallenge("ch_2", "Long Drive 290+", "Crush a 290+ yard drive onto the fairway", 200, 1, false, "LONG_DRIVE"),
        DailyChallenge("ch_3", "Sand Escape Hero", "Escape from a bunker and land safely on green", 180, 0, false, "BUNKER_ESCAPE"),
        DailyChallenge("ch_4", "Snake Putt Master", "Drain any putt longer than 15 yards", 300, 2, false, "LONG_PUTT")
    ),
    val wheelSpinReward: String? = null,
    val caddieAdvice: String = "Welcome to Hole 1. Driver on the tee; let's launch a beauty down the fairway!",
    val isAiCaddieLoading: Boolean = false,
    val playerScores: List<Int> = emptyList(),
    val rivals: List<TournamentRival> = CaddieBrain.createDefaultRivals(),
    val holeCompleted: Boolean = false,
    val holeScoreName: String = "",
    val coinsEarnedThisHole: Int = 0,
    val tournamentCompleted: Boolean = false,
    val tournamentFinalRank: Int = 1,
    val career: PlayerCareer = PlayerCareer(),
    val balls: List<GolfBallSkin> = EquipmentCatalog.getDefaultBalls(),
    val clubs: List<ClubUpgrade> = EquipmentCatalog.getDefaultClubUpgrades(),
    val isPracticeMode: Boolean = false,
    val shootoutState: ShootoutGameState = ShootoutGameState(),
    val tourPassState: TourPassState = TourPassState(),
    val tournamentCups: List<TournamentCup> = CourseCatalog.getTournamentCups(),
    val selectedCup: TournamentCup = CourseCatalog.getTournamentCups()[0],
    val equippedTracer: BallTracerStyle = BallTracerStyle.NEON_CYAN,
    val isTargetShootoutMode: Boolean = false,
    val showVersionUpdateDialog: Boolean = false,
    val playSettings: PlayConsoleSettings = PlayConsoleSettings(),
    val gpsRangefinder: GpsRangefinderData = GpsRangefinderData(),
    val achievements: List<PlayAchievement> = listOf(
        PlayAchievement("ach_first_drive", "Rocket Launcher", "Crush a 250+ yard drive onto the fairway", 1000, true),
        PlayAchievement("ach_bullseye", "Target Sniper", "Hit a 500-pt Bullseye in Target Shootout", 2500, true),
        PlayAchievement("ach_eagle", "Eagle Eye", "Sink an eagle (-2) on any championship hole", 5000, false),
        PlayAchievement("ach_ace", "Hole-in-One Legend", "Drain a Hole-in-One on any Par 3", 10000, false),
        PlayAchievement("ach_tempest", "Storm Navigator", "Win a championship in Tempest or Rain weather", 3500, false),
        PlayAchievement("ach_custom_ball", "Ball Forger", "Create a custom AI ball skin in the studio", 1500, false),
        PlayAchievement("ach_tour_master", "Tour Pass Champion", "Reach Tier 5+ in Zenith Tour Pass", 4000, false)
    ),
    val cloudSyncUserEmail: String = "",
    val cloudSyncUserId: String = "",
    val cloudProfileImageUrl: String = "",
    val cloudSyncStatus: String = "Cloud Sync Active",
    val isCloudSyncInProgress: Boolean = false,
    val cloudSyncError: String? = null,
    val cloudSyncRevision: Long = 1L,
    val cloudMatchHistory: List<CloudMatchHistoryRecord> = emptyList(),
    val matchPlayState: MatchPlayState = MatchPlayState(),
    val matchPlayRivals: List<MatchPlayRival> = CourseCatalog.getDefaultMatchPlayRivals(),
    val selectedMatchPlayRival: MatchPlayRival = CourseCatalog.getDefaultMatchPlayRivals()[0],
    val savedReplays: List<ReplayShot> = CourseCatalog.getDefaultReplays(),
    val activeReplayShot: ReplayShot? = null,
    val customHoleBlueprint: CustomHoleBlueprint = CustomHoleBlueprint(),
    val golferProfile: GolferProfile = GolferProfile(),
    val globalCloudLeaderboard: List<CloudLeaderboardEntry> = emptyList(),
    val isCelebrationActive: Boolean = false,
    val celebrationTitle: String = "",
    val isGamePaused: Boolean = false,
    val lastLandingEffect: BallLandingEffect? = null,
    val scoreNotificationBanner: String? = null,
    val isDeletingAccount: Boolean = false,
    val accountDeletionMessage: String? = null,
    val isOnline: Boolean = true,
    val isGuestSession: Boolean = false,
    val dailyLoginStreak: Int = 1,
    val lastDailyClaimDayEpoch: Long = 0L,
    val unlockedAccessoryIds: Set<String> = emptySet(),
    val notifications: List<AppNotificationItem> = listOf(
        AppNotificationItem(
            id = "notif_season1",
            title = "Apex Champions Tour • Season 1",
            message = "Weekly Championship qualifiers & Season Pass tiers are open.",
            timestampLabel = "Live",
            targetScreen = "CUP_SELECT",
            isUnread = true
        ),
        AppNotificationItem(
            id = "notif_daily_spin",
            title = "Daily Reward & Lucky Wheel",
            message = "Claim your login streak bonus and spin the Championship Wheel.",
            timestampLabel = "Today",
            targetScreen = "DAILY_CHALLENGES",
            isUnread = true
        )
    )
) {
    val equippedBall: GolfBallSkin
        get() = balls.firstOrNull { it.isEquipped } ?: balls[0]

    val isDailyRewardClaimedToday: Boolean
        get() = lastDailyClaimDayEpoch >= (System.currentTimeMillis() / 86_400_000L)

    val unreadNotificationsCount: Int
        get() = notifications.count { it.isUnread }

    val currentDistanceToPin: Float
        get() = (Offset(ballState.x, ballState.y) - currentHole.pinPosition).getDistance()

    val selectedClubMaxDistanceYds: Float
        get() = selectedClub.maxDistanceYds

    val currentPullbackTargetYds: Float
        get() = (aimPowerRatio.coerceIn(0f, 1.0f) * selectedClub.maxDistanceYds)
            .coerceAtMost(selectedClub.maxDistanceYds)

    val totalRoundStrokes: Int
        get() = playerScores.sum() + currentStrokeCount

    val completedHolesPar: Int
        get() = holes.take(playerScores.size).sumOf { it.par }

    val roundScoreToParText: String
        get() {
            if (playerScores.isEmpty() && currentStrokeCount == 0) return "E"
            val completedStrokes = playerScores.sum()
            val completedPar = completedHolesPar
            val activeDiff = if (holeCompleted) 0 else (currentStrokeCount - currentHole.par).coerceAtLeast(-currentHole.par)
            val totalDiff = if (playerScores.isNotEmpty()) {
                (completedStrokes - completedPar)
            } else {
                activeDiff
            }
            return when {
                totalDiff == 0 -> "E"
                totalDiff > 0 -> "+$totalDiff"
                else -> "$totalDiff"
            }
        }

    val holeStatistics: HoleStatistics
        get() {
            val distToPin = currentDistanceToPin
            val totalTraveled = (Offset(ballState.x, ballState.y) - currentHole.teePosition).getDistance()
            val scoreDiff = currentStrokeCount - currentHole.par
            val fairwayHit = shotHistory.size > 1 &&
                GolfPhysicsEngine.getSurfaceAt(shotHistory[1], currentHole) == SurfaceType.FAIRWAY
            val girTargetStrokes = currentHole.par - 2
            val girHit = ballState.lie == SurfaceType.GREEN && currentStrokeCount <= girTargetStrokes
            return HoleStatistics(
                holeNumber = currentHole.holeNumber,
                par = currentHole.par,
                totalHoleYardage = currentHole.yardage,
                strokesTaken = currentStrokeCount,
                remainingDistanceToPin = distToPin,
                distanceTraveled = totalTraveled,
                currentLie = ballState.lie,
                fairwayHit = fairwayHit,
                greenInRegulation = girHit,
                scoreRelativeToPar = scoreDiff,
                shotLogs = holeShotLogs
            )
        }
}

class GolfGameViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var apexOscillatorJob: Job? = null
    private var ballFlightJob: Job? = null

    init {
        resetHoleToTee(_uiState.value.currentHole)
    }

    private fun isGameplayScreen(screen: AppScreen): Boolean =
        screen == AppScreen.PLAY_HOLE || screen == AppScreen.PRACTICE_RANGE || screen == AppScreen.TARGET_SHOOTOUT

    fun navigateTo(screen: AppScreen) {
        val current = _uiState.value
        val leavingGameplay = isGameplayScreen(current.currentScreen) && !isGameplayScreen(screen) && screen != AppScreen.HOLE_SUMMARY
        if (leavingGameplay) {
            ballFlightJob?.cancel()
            apexOscillatorJob?.cancel()
        }
        val prev = if (screen == AppScreen.TOURNAMENT_LEADERBOARD && current.currentScreen != AppScreen.TOURNAMENT_LEADERBOARD) {
            current.currentScreen
        } else {
            current.previousScreen
        }
        _uiState.value = current.copy(
            currentScreen = screen,
            previousScreen = prev,
            isBallInFlight = if (leavingGameplay) false else current.isBallInFlight,
            isDraggingSwing = if (leavingGameplay) false else current.isDraggingSwing,
            isApexTimingActive = if (leavingGameplay) false else current.isApexTimingActive
        )
    }

    fun startTournament() {
        val holes = CourseCatalog.getChampionshipHoles()
        val defaultRivals = CaddieBrain.createDefaultRivals()
        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.PLAY_HOLE,
            holes = holes,
            currentHoleIndex = 0,
            currentHole = holes[0],
            playerScores = emptyList(),
            rivals = defaultRivals,
            isPracticeMode = false,
            isTargetShootoutMode = false,
            tournamentCompleted = false,
            tournamentFinalRank = 1
        )
        resetHoleToTee(holes[0])
    }

    fun startPracticeRange() {
        val practiceHole = CourseCatalog.getPracticeHole()
        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.PLAY_HOLE,
            holes = listOf(practiceHole),
            currentHoleIndex = 0,
            currentHole = practiceHole,
            isPracticeMode = true,
            tournamentCompleted = false
        )
        resetHoleToTee(practiceHole)
    }

    private fun resetHoleToTee(hole: Hole) {
        val randomWind = WindCondition(
            speedMph = (3..24).random().toFloat(),
            directionDeg = (0..359).random().toFloat()
        )

        val initialAimAngle = calculateInitialHeading(hole.teePosition, hole.pinPosition)

        val recommended = CaddieBrain.analyzeShot(
            ballPos = hole.teePosition,
            hole = hole,
            lie = SurfaceType.TEE,
            wind = randomWind
        )

        val initialBallState = BallState(
            x = hole.teePosition.x,
            y = hole.teePosition.y,
            lie = SurfaceType.TEE,
            isResting = true
        )

        val initialAim = initialAimAngle + recommended.windOffsetDeg
        val initialPlayerCoords = computePlayerCoordinates(hole.teePosition, initialAim)

        _uiState.value = _uiState.value.copy(
            currentHole = hole,
            currentStrokeCount = 0,
            ballState = initialBallState,
            playerCoordinates = initialPlayerCoords,
            ballTrajectory = BallTrajectory(),
            holeShotLogs = emptyList(),
            previousBallPos = null,
            shotHistory = listOf(hole.teePosition),
            wind = randomWind,
            selectedClub = recommended.recommendedClub,
            aimPowerRatio = 0.90f,
            aimAngleDeg = initialAim,
            curveSpin = 0f,
            verticalSpin = 0f,
            isApexTimingActive = false,
            apexMeterPosition = 0f,
            apexQualityText = null,
            isBallInFlight = false,
            holeCompleted = false,
            holeScoreName = "",
            coinsEarnedThisHole = 0,
            isGamePaused = false,
            lastLandingEffect = null,
            scoreNotificationBanner = null,
            caddieAdvice = "Hole #${hole.holeNumber}: ${hole.holeTitle} (Par ${hole.par}, ${hole.yardage} yds). ${recommended.tacticalTip}"
        )

        updateTrajectoryPreview()
    }

    fun pauseGame() {
        apexOscillatorJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isGamePaused = true,
            isDraggingSwing = false,
            isApexTimingActive = false
        )
    }

    fun resumeGame() {
        _uiState.value = _uiState.value.copy(isGamePaused = false)
    }

    fun pauseOnLifecycleStop(context: Context? = null) {
        val state = _uiState.value
        if (isGameplayScreen(state.currentScreen) && !state.holeCompleted) {
            pauseGame()
        }
        val targetCtx = context ?: appContext
        if (targetCtx != null) {
            appContext = targetCtx.applicationContext
            LocalProgressStorage.saveSnapshot(
                context = targetCtx,
                career = _uiState.value.career,
                balls = _uiState.value.balls,
                clubs = _uiState.value.clubs,
                tourPass = _uiState.value.tourPassState,
                golferProfile = _uiState.value.golferProfile,
                playSettings = _uiState.value.playSettings,
                customHoleBlueprint = _uiState.value.customHoleBlueprint,
                dailyChallenges = _uiState.value.dailyChallenges,
                achievements = _uiState.value.achievements
            )
        }
    }

    fun togglePauseGame() {
        if (_uiState.value.isGamePaused) {
            resumeGame()
        } else {
            pauseGame()
        }
    }

    fun restartCurrentHole() {
        ballFlightJob?.cancel()
        apexOscillatorJob?.cancel()
        resetHoleToTee(_uiState.value.currentHole)
    }

    fun dismissScoreNotification() {
        _uiState.value = _uiState.value.copy(scoreNotificationBanner = null)
    }

    private fun computePlayerCoordinates(ballPos: Offset, aimAngleDeg: Float, stanceDist: Float = 4.0f): PlayerCoordinates {
        val aimRad = Math.toRadians(aimAngleDeg.toDouble())
        val perpRad = aimRad - Math.PI / 2.0
        val stanceX = (ballPos.x + Math.sin(perpRad) * stanceDist).toFloat()
        val stanceY = (ballPos.y - Math.cos(perpRad) * stanceDist).toFloat()
        return PlayerCoordinates(
            position = Offset(stanceX, stanceY),
            facingAngleDeg = aimAngleDeg + 90f,
            stanceDistance = stanceDist
        )
    }

    private fun calculateInitialHeading(from: Offset, to: Offset): Float {
        val delta = to - from
        return Math.toDegrees(atan2(delta.x.toDouble(), -delta.y.toDouble())).toFloat()
    }

    fun selectClub(club: ClubType) {
        _uiState.value = _uiState.value.copy(selectedClub = club)
        updateTrajectoryPreview()
    }

    fun setAimPower(ratio: Float) {
        _uiState.value = _uiState.value.copy(aimPowerRatio = ratio.coerceIn(0.1f, 1.0f))
        updateTrajectoryPreview()
    }

    fun setAimAngle(angle: Float) {
        _uiState.value = _uiState.value.copy(aimAngleDeg = angle)
        updateTrajectoryPreview()
    }

    fun adjustAimAngle(deltaDeg: Float) {
        val newAngle = (_uiState.value.aimAngleDeg + deltaDeg)
        _uiState.value = _uiState.value.copy(aimAngleDeg = newAngle)
        updateTrajectoryPreview()
    }

    fun setCurveSpin(spin: Float) {
        _uiState.value = _uiState.value.copy(curveSpin = spin.coerceIn(-1.0f, 1.0f))
        updateTrajectoryPreview()
    }

    fun setVerticalSpin(spin: Float) {
        _uiState.value = _uiState.value.copy(verticalSpin = spin.coerceIn(-1.0f, 1.0f))
        updateTrajectoryPreview()
    }

    fun togglePhysicsDebug() {
        _uiState.value = _uiState.value.copy(isPhysicsDebugEnabled = !_uiState.value.isPhysicsDebugEnabled)
    }

    fun setWindParameters(speed: Float, direction: Float) {
        _uiState.value = _uiState.value.copy(
            wind = WindCondition(speed.coerceIn(0f, 35f), direction)
        )
        updateTrajectoryPreview()
    }

    private fun updateTrajectoryPreview() {
        val state = _uiState.value
        val ballPos = Offset(state.ballState.x, state.ballState.y)
        val club = state.selectedClub
        val ballSkin = state.equippedBall

        val clubStat = state.clubs.firstOrNull { it.clubType == club }
        val clubPowerMultiplier = 1.0f + ((clubStat?.powerStat ?: 70) - 70) * 0.005f + (ballSkin.powerBonusPercent / 100f)
        val windResistance = (1.0f - (ballSkin.windResistancePercent / 100f)).coerceAtLeast(0.5f)

        val aim = ShotAim(
            powerRatio = state.aimPowerRatio,
            aimAngleDeg = state.aimAngleDeg,
            curveSpin = state.curveSpin,
            apexTimingAccuracy = 1.0f,
            verticalSpin = state.verticalSpin
        )

        val launchPreview = GolfClubController.calculateClubLaunch(
            club = club,
            aim = aim,
            currentLie = state.ballState.lie,
            powerStatBonus = clubPowerMultiplier - 1.0f,
            deterministicDeviationSign = 1f
        )

        val trajectory = GolfPhysicsEngine.calculateAimTrajectory(
            start = ballPos,
            club = club,
            aim = aim,
            wind = state.wind,
            hole = state.currentHole,
            clubPowerMultiplier = clubPowerMultiplier,
            windResistanceMultiplier = windResistance,
            weather = state.weather
        )

        val playerCoords = computePlayerCoordinates(ballPos, state.aimAngleDeg)

        _uiState.value = _uiState.value.copy(
            trajectoryPreview = trajectory,
            ballTrajectory = state.ballTrajectory.copy(
                predictedPoints = trajectory,
                launchSpeedMph = launchPreview.ballSpeedMph,
                launchAngleDeg = launchPreview.dynamicLaunchAngleDeg,
                backspinRpm = launchPreview.backspinRpm,
                sidespinRpm = launchPreview.sidespinRpm
            ),
            playerCoordinates = playerCoords
        )
    }

    companion object {
        const val SWING_DEAD_ZONE_PX = 28f
        const val BALL_CANCEL_RADIUS_PX = 42f
        const val DEFAULT_MAX_PULL_PX = 210f
    }

    /**
     * Drag-and-Release Swing Control — Step 1: Touch Down anywhere on the screen.
     * Starts tracking the pullback gesture without launching the ball.
     */
    fun onSwingDragStart(startScreenPos: Offset) {
        val state = _uiState.value
        if (state.isBallInFlight || state.isClubStrikeAnimating || state.holeCompleted) return
        apexOscillatorJob?.cancel()

        _uiState.value = state.copy(
            isDraggingSwing = true,
            dragStartScreenOffset = startScreenPos,
            dragCurrentScreenOffset = startScreenPos,
            hasLeftDeadZoneDuringDrag = false,
            isSwingInCancelZone = true,
            aimPowerRatio = 0f,
            clubSwingAnimAngleDeg = 0f,
            apexQualityText = null
        )
    }

    /**
     * Drag-and-Release Swing Control — Step 2: Drag backward away from the target.
     * - Drag direction sets shot direction, opposite to the pull.
     * - Drag distance sets power, capped at 100% of the selected club's max distance.
     * - Includes a small dead zone (SWING_DEAD_ZONE_PX) so tiny drags don't trigger a shot,
     *   and a cancel zone if the player drags back to the ball or back into the dead zone.
     * - Never launches the ball while dragging.
     */
    fun onSwingDragUpdate(
        currentScreenPos: Offset,
        ballScreenPos: Offset = Offset.Unspecified,
        maxPullDistancePx: Float = DEFAULT_MAX_PULL_PX
    ) {
        val state = _uiState.value
        if (!state.isDraggingSwing || state.isBallInFlight || state.holeCompleted) return

        val pullVector = currentScreenPos - state.dragStartScreenOffset
        val pullDistance = pullVector.getDistance()
        val hasLeftDeadZone = state.hasLeftDeadZoneDuringDrag || pullDistance > SWING_DEAD_ZONE_PX

        val returnedToBall = hasLeftDeadZone &&
            ballScreenPos != Offset.Unspecified &&
            (currentScreenPos - ballScreenPos).getDistance() <= BALL_CANCEL_RADIUS_PX

        val inDeadOrCancelZone = pullDistance <= SWING_DEAD_ZONE_PX || returnedToBall

        if (inDeadOrCancelZone) {
            _uiState.value = state.copy(
                dragCurrentScreenOffset = currentScreenPos,
                hasLeftDeadZoneDuringDrag = hasLeftDeadZone,
                isSwingInCancelZone = true,
                aimPowerRatio = 0f,
                clubSwingAnimAngleDeg = 0f
            )
            return
        }

        // Shot direction is opposite to the pullback vector
        val shotDirX = -pullVector.x
        val shotDirY = -pullVector.y
        val rawAngleDeg = Math.toDegrees(Math.atan2(shotDirX.toDouble(), (-shotDirY).toDouble())).toFloat()
        val clampedAngleDeg = rawAngleDeg.coerceIn(-85f, 85f)

        // Power scales linearly from dead-zone edge up to maxPullDistancePx, capped at 1.0f (selected club's max distance)
        val effectivePull = (pullDistance - SWING_DEAD_ZONE_PX).coerceAtLeast(0f)
        val safeMaxPull = maxPullDistancePx.coerceAtLeast(60f)
        val powerRatio = (effectivePull / safeMaxPull).coerceIn(0.08f, 1.0f)

        // Backswing angle pulls club backward proportionally (-105 deg at full pullback)
        val backswingAngle = -powerRatio * 105f

        _uiState.value = state.copy(
            dragCurrentScreenOffset = currentScreenPos,
            hasLeftDeadZoneDuringDrag = true,
            isSwingInCancelZone = false,
            aimAngleDeg = clampedAngleDeg,
            aimPowerRatio = powerRatio,
            clubSwingAnimAngleDeg = backswingAngle
        )
        updateTrajectoryPreview()
    }

    /**
     * Drag-and-Release Swing Control — Step 3: Finger/Mouse Release.
     * - If inside the dead zone or cancelled by dragging back to the ball, cancels the swing without firing.
     * - Otherwise, plays a quick club downswing animation + hit sound right at release and launches the ball.
     */
    fun onSwingDragRelease() {
        val state = _uiState.value
        if (!state.isDraggingSwing) return

        if (state.isBallInFlight || state.holeCompleted || state.isSwingInCancelZone || state.aimPowerRatio < 0.08f) {
            // Cancelled swing (or tiny drag inside dead zone) — do not launch ball
            _uiState.value = state.copy(
                isDraggingSwing = false,
                hasLeftDeadZoneDuringDrag = false,
                isSwingInCancelZone = false,
                clubSwingAnimAngleDeg = 0f,
                aimPowerRatio = 0.75f,
                apexQualityText = if (state.hasLeftDeadZoneDuringDrag) "SWING CANCELLED" else null
            )
            updateTrajectoryPreview()
            return
        }

        val lockedPower = state.aimPowerRatio.coerceIn(0.08f, 1.0f)
        val club = state.selectedClub
        val targetYds = (lockedPower * club.maxDistanceYds).toInt()
        val qualityLabel = when {
            lockedPower >= 0.92f -> "MAX POWER STRIKE ($targetYds YDS)"
            lockedPower >= 0.65f -> "PURE STRIKE ($targetYds YDS)"
            else -> "TOUCH SHOT ($targetYds YDS)"
        }

        // Play immediate club strike sound & haptic impulse right on release
        GolfAudioHapticEngine.playSwingImpactSound(
            club = club,
            isApexSweetSpot = lockedPower >= 0.70f
        )

        _uiState.value = state.copy(
            isDraggingSwing = false,
            hasLeftDeadZoneDuringDrag = false,
            isSwingInCancelZone = false,
            isClubStrikeAnimating = true,
            clubSwingAnimAngleDeg = 48f, // Whip club through impact into follow-through
            swingMeterPhase = SwingMeterPhase.IDLE,
            isApexTimingActive = false,
            apexQualityText = qualityLabel,
            currentStrokeCount = state.currentStrokeCount + 1
        )

        // Launch ball immediately on release
        launchBallPhysicsFlight(accuracy = 0.98f)

        // Reset club follow-through animation after brief visual whip
        viewModelScope.launch {
            delay(220)
            _uiState.value = _uiState.value.copy(
                isClubStrikeAnimating = false,
                clubSwingAnimAngleDeg = 0f
            )
        }
    }

    /**
     * Aborts an active drag gesture without launching the ball.
     */
    fun onSwingDragCancel() {
        val state = _uiState.value
        if (!state.isDraggingSwing) return
        _uiState.value = state.copy(
            isDraggingSwing = false,
            hasLeftDeadZoneDuringDrag = false,
            isSwingInCancelZone = false,
            clubSwingAnimAngleDeg = 0f,
            aimPowerRatio = 0.75f
        )
        updateTrajectoryPreview()
    }

    /**
     * Explicit Shot Confirmation & Strike handler:
     * Executes the shot using the currently configured club, direction, power ratio, and spin,
     * playing the club swing animation + strike sound and launching the ball physics trajectory.
     */
    fun confirmAndExecuteShot() {
        val state = _uiState.value
        if (state.isBallInFlight || state.isClubStrikeAnimating || state.holeCompleted || state.isGamePaused) return
        apexOscillatorJob?.cancel()

        val lockedPower = state.aimPowerRatio.coerceIn(0.10f, 1.0f)
        val club = state.selectedClub
        val targetYds = (lockedPower * club.maxDistanceYds).toInt()
        val accuracy = if (state.isApexTimingActive) {
            val dev = kotlin.math.abs(state.apexMeterPosition)
            when {
                dev < 0.18f -> 1.0f
                dev < 0.45f -> 0.88f
                else -> 0.72f
            }
        } else {
            0.98f
        }
        val qualityLabel = when {
            accuracy >= 0.96f && lockedPower >= 0.90f -> "PERFECT APEX ($targetYds YDS)"
            accuracy >= 0.95f -> "PURE STRIKE ($targetYds YDS)"
            else -> "CONFIRMED SHOT ($targetYds YDS)"
        }

        GolfAudioHapticEngine.playSwingImpactSound(
            club = club,
            isApexSweetSpot = accuracy >= 0.95f
        )

        _uiState.value = state.copy(
            isDraggingSwing = false,
            hasLeftDeadZoneDuringDrag = false,
            isSwingInCancelZone = false,
            isClubStrikeAnimating = true,
            clubSwingAnimAngleDeg = 48f,
            swingMeterPhase = SwingMeterPhase.IDLE,
            isApexTimingActive = false,
            apexQualityText = qualityLabel,
            scoreNotificationBanner = "SHOT LAUNCHED • ${club.shortName} ($targetYds YDS TARGET)",
            currentStrokeCount = state.currentStrokeCount + 1
        )

        launchBallPhysicsFlight(accuracy = accuracy)

        viewModelScope.launch {
            delay(220)
            _uiState.value = _uiState.value.copy(
                isClubStrikeAnimating = false,
                clubSwingAnimAngleDeg = 0f
            )
        }
    }

    /**
     * Legacy 3-Tap Vertical Swing Meter handler retained for unit-test compatibility
     */
    fun onVerticalSwingMeterTap() {
        val state = _uiState.value
        if (state.isBallInFlight || state.holeCompleted) return

        when (state.swingMeterPhase) {
            SwingMeterPhase.IDLE -> {
                _uiState.value = state.copy(
                    swingMeterPhase = SwingMeterPhase.POWER,
                    isApexTimingActive = false,
                    apexQualityText = null,
                    aimPowerRatio = 0.25f
                )
                updateTrajectoryPreview()
                apexOscillatorJob?.cancel()
                apexOscillatorJob = viewModelScope.launch {
                    var time = 0.0
                    val speed = 0.11
                    while (_uiState.value.swingMeterPhase == SwingMeterPhase.POWER) {
                        time += speed
                        // Triangle/sine wave oscillating smoothly between 0.20f and 1.00f
                        val normalized = ((Math.sin(time - Math.PI / 2.0) + 1.0) * 0.5).toFloat()
                        val power = (0.20f + normalized * 0.80f).coerceIn(0.20f, 1.0f)
                        _uiState.value = _uiState.value.copy(aimPowerRatio = power)
                        updateTrajectoryPreview()
                        delay(16)
                    }
                }
            }
            SwingMeterPhase.POWER -> {
                apexOscillatorJob?.cancel()
                updateTrajectoryPreview()
                startSwingSequence()
            }
            SwingMeterPhase.ACCURACY -> {
                lockAndReleaseSwing()
            }
        }
    }

    /**
     * Swing Phase 1 (or 2 in 3-tap flow): Start Apex oscillating accuracy meter
     */
    fun startSwingSequence() {
        if (_uiState.value.isBallInFlight || _uiState.value.holeCompleted) return

        _uiState.value = _uiState.value.copy(
            swingMeterPhase = SwingMeterPhase.ACCURACY,
            isApexTimingActive = true,
            apexMeterPosition = 0f,
            apexQualityText = null
        )

        apexOscillatorJob?.cancel()
        apexOscillatorJob = viewModelScope.launch {
            var time = 0.0
            val speed = 0.16 // oscillation frequency
            while (_uiState.value.isApexTimingActive) {
                time += speed
                val pos = (Math.sin(time)).toFloat() // Oscillates between -1.0 and +1.0
                _uiState.value = _uiState.value.copy(apexMeterPosition = pos)
                delay(16) // ~60fps
            }
        }
    }

    /**
     * Swing Final Phase: User taps Swing to lock apex timing and launch the ball!
     */
    fun lockAndReleaseSwing() {
        if (!_uiState.value.isApexTimingActive) {
            // If meter not active, start it
            startSwingSequence()
            return
        }

        // Lock meter
        val meterPos = _uiState.value.apexMeterPosition
        apexOscillatorJob?.cancel()

        val deviation = Math.abs(meterPos)
        val (accuracy, qualityText) = when {
            deviation < 0.18f -> Pair(1.0f, "PERFECT APEX!")
            deviation < 0.45f -> Pair(0.85f, "GREAT CONTACT")
            meterPos < 0f -> Pair(0.60f, "HOOK MISS")
            else -> Pair(0.60f, "SLICE MISS")
        }

        GolfAudioHapticEngine.playSwingImpactSound(
            club = _uiState.value.selectedClub,
            isApexSweetSpot = accuracy >= 0.95f
        )

        _uiState.value = _uiState.value.copy(
            swingMeterPhase = SwingMeterPhase.IDLE,
            isApexTimingActive = false,
            apexQualityText = qualityText,
            currentStrokeCount = _uiState.value.currentStrokeCount + 1
        )

        launchBallPhysicsFlight(accuracy)
    }

    private fun launchBallPhysicsFlight(accuracy: Float) {
        val state = _uiState.value
        val startPos = Offset(state.ballState.x, state.ballState.y)
        val club = state.selectedClub
        val ballSkin = state.equippedBall
        val clubStat = state.clubs.firstOrNull { it.clubType == club }
        val powerBonus = ((clubStat?.powerStat ?: 70) - 70) * 0.005f + (ballSkin.powerBonusPercent / 100f)

        val aim = ShotAim(
            powerRatio = state.aimPowerRatio,
            aimAngleDeg = state.aimAngleDeg,
            curveSpin = state.curveSpin,
            apexTimingAccuracy = accuracy,
            verticalSpin = state.verticalSpin
        )

        val launchTelemetry = GolfClubController.calculateClubLaunch(
            club = club,
            aim = aim,
            currentLie = state.ballState.lie,
            powerStatBonus = powerBonus
        )
        val vx = launchTelemetry.vx
        val vy = launchTelemetry.vy
        val vz = launchTelemetry.vz

        _uiState.value = _uiState.value.copy(
            isBallInFlight = true,
            previousBallPos = startPos,
            ballState = state.ballState.copy(
                isAirborne = !club.isPutter,
                isRolling = club.isPutter,
                isResting = false,
                vx = vx,
                vy = vy,
                vz = vz
            ),
            ballTrajectory = state.ballTrajectory.copy(
                collisionEvents = emptyList(),
                launchSpeedMph = launchTelemetry.ballSpeedMph,
                launchAngleDeg = launchTelemetry.dynamicLaunchAngleDeg,
                backspinRpm = launchTelemetry.backspinRpm,
                sidespinRpm = launchTelemetry.sidespinRpm,
                dragForcePeakN = 0f,
                magnusForcePeakN = 0f
            )
        )

        ballFlightJob?.cancel()
        ballFlightJob = viewModelScope.launch {
            var currentPos = startPos
            var currentZ = 0.0f
            var maxApex = 0.0f
            var carryDist = 0.0f
            var curVx = vx
            var curVy = vy
            var curVz = vz
            var peakDragN = 0.0f
            var peakMagnusN = 0.0f
            var bounceCounter = 0
            var currentSpinRpm = launchTelemetry.backspinRpm
            val collisionList = mutableListOf<PhysicsCollisionPoint>()
            val flightPath = mutableListOf<Offset>()
            flightPath.add(startPos)

            val dt = 0.04f // 25 physics ticks per second for deterministic flight
            val frameStepMs = if (state.playSettings.isHighFpsMode &&
                state.playSettings.graphicsTier != com.example.game.model.GraphicsQualityTier.LOW_BATTERY
            ) 16L else 28L

            while (true) {
                while (_uiState.value.isGamePaused) {
                    delay(50)
                }
                if (!isGameplayScreen(_uiState.value.currentScreen)) {
                    _uiState.value = _uiState.value.copy(isBallInFlight = false)
                    break
                }

                val wasAirborne = currentZ > 0.01f
                val preImpactSpeed = sqrt(curVx * curVx + curVy * curVy + curVz * curVz)
                if (wasAirborne && !club.isPutter) {
                    val aero = GolfBallPhysics.computeAerodynamicForces(
                        vx = curVx,
                        vy = curVy,
                        vz = curVz,
                        club = club,
                        aim = aim,
                        wind = state.wind,
                        altitudeZ = currentZ
                    )
                    if (aero.dragForceNewton > peakDragN) peakDragN = aero.dragForceNewton
                    if (aero.magnusForceNewton > peakMagnusN) peakMagnusN = aero.magnusForceNewton
                }

                val step = GolfPhysicsEngine.stepPhysics(
                    currentPos = currentPos,
                    altitudeZ = currentZ,
                    vx = curVx,
                    vy = curVy,
                    vz = curVz,
                    club = club,
                    aim = aim,
                    wind = state.wind,
                    hole = state.currentHole,
                    weather = state.weather,
                    dt = dt
                )

                if (wasAirborne && step.newZ == 0f && !club.isPutter) {
                    bounceCounter++
                    currentSpinRpm = (currentSpinRpm * 0.55f).toInt()
                    val postSpeed = sqrt(step.newVx * step.newVx + step.newVy * step.newVy + step.newVz * step.newVz)
                    collisionList.add(
                        PhysicsCollisionPoint(
                            position = step.newPos,
                            surface = step.lie,
                            impactSpeedMps = preImpactSpeed,
                            reboundSpeedMps = postSpeed,
                            bounceNumber = bounceCounter,
                            spinRpmAfterImpact = currentSpinRpm
                        )
                    )
                }

                currentPos = step.newPos
                currentZ = step.newZ
                curVx = step.newVx
                curVy = step.newVy
                curVz = step.newVz
                flightPath.add(currentPos)

                if (currentZ > maxApex) {
                    maxApex = currentZ
                }
                if (carryDist == 0f && !step.isAirborne && step.newZ <= 0.05f) {
                    carryDist = (currentPos - startPos).getDistance()
                }

                val shotDist = (currentPos - startPos).getDistance()
                val immutablePath = flightPath.toList()

                _uiState.value = _uiState.value.copy(
                    ballState = BallState(
                        x = currentPos.x,
                        y = currentPos.y,
                        z = currentZ,
                        vx = curVx,
                        vy = curVy,
                        vz = curVz,
                        isAirborne = step.isAirborne,
                        isRolling = !step.isAirborne && !step.isResting,
                        isResting = step.isResting,
                        inHole = step.inHole,
                        lie = step.lie,
                        distanceTraveled = shotDist,
                        trajectoryPoints = immutablePath
                    ),
                    ballTrajectory = _uiState.value.ballTrajectory.copy(
                        actualFlightPoints = immutablePath,
                        apexAltitude = maxApex,
                        carryDistanceYds = if (carryDist > 0f) carryDist else shotDist,
                        collisionEvents = collisionList.toList(),
                        dragForcePeakN = peakDragN,
                        magnusForcePeakN = peakMagnusN
                    )
                )

                if (step.isResting) {
                    onBallCameToRest(step, startPos, immutablePath, maxApex, carryDist)
                    break
                }

                delay(frameStepMs)
            }
        }
    }

    private fun onBallCameToRest(
        step: GolfPhysicsEngine.StepResult,
        shotStart: Offset,
        flightPath: List<Offset>,
        maxApex: Float,
        carryDist: Float
    ) {
        val state = _uiState.value
        val shotYardage = (step.newPos - shotStart).getDistance()
        val finalCarry = if (carryDist > 0f) carryDist else shotYardage
        val rollDist = (shotYardage - finalCarry).coerceAtLeast(0f)

        // Log this shot
        val shotLog = HoleShotLog(
            strokeNumber = state.currentStrokeCount,
            clubUsed = state.selectedClub,
            startLie = state.ballState.lie,
            endLie = step.lie,
            yardage = shotYardage,
            apexRating = state.apexQualityText ?: "SOLID"
        )
        val updatedLogs = state.holeShotLogs + shotLog

        // Update longest drive if on tee with driver
        if (state.selectedClub == ClubType.DRIVER && state.currentStrokeCount == 1) {
            if (shotYardage > state.career.longestDriveYds) {
                state.career.longestDriveYds = shotYardage
            }
            if (shotYardage >= 250f && step.lie == SurfaceType.FAIRWAY) {
                unlockAchievement("ach_first_drive")
            }
        }

        checkChallengeProgression(
            shotYardage = shotYardage,
            startLie = state.ballState.lie,
            endLie = step.lie,
            inHole = step.inHole,
            endPos = step.newPos
        )

        // Check Target Shootout Mode
        if (state.isTargetShootoutMode) {
            handleShootoutShotLanding(
                landingPos = step.newPos,
                shotYardage = shotYardage,
                flightPath = flightPath,
                maxApex = maxApex,
                finalCarry = finalCarry,
                rollDist = rollDist,
                updatedLogs = updatedLogs
            )
            return
        }

        // Check Water Hazard Penalty
        if (step.lie == SurfaceType.WATER_HAZARD) {
            // Ball sank in water! Reset to shot start with +1 penalty stroke
            val penalizedStrokes = state.currentStrokeCount + 1
            val waterEffect = BallLandingEffect(
                position = step.newPos,
                surface = SurfaceType.WATER_HAZARD,
                impactSpeedMps = 12f,
                title = "WATER HAZARD SPLASH",
                subtitle = "+1 Penalty Stroke • Ball Dropped at Previous Lie",
                colorHex = 0xFF00E5FF,
                isCupSink = false
            )
            _uiState.value = _uiState.value.copy(
                isBallInFlight = false,
                currentStrokeCount = penalizedStrokes,
                lastLandingEffect = waterEffect,
                scoreNotificationBanner = "WATER HAZARD! +1 PENALTY STROKE",
                ballState = BallState(
                    x = shotStart.x,
                    y = shotStart.y,
                    lie = GolfPhysicsEngine.getSurfaceAt(shotStart, state.currentHole),
                    isResting = true
                ),
                ballTrajectory = state.ballTrajectory.copy(
                    actualFlightPoints = flightPath,
                    apexAltitude = maxApex,
                    carryDistanceYds = finalCarry,
                    totalRollDistanceYds = rollDist
                ),
                holeShotLogs = updatedLogs,
                caddieAdvice = "SPLASH! Water hazard penalty (+1 stroke). Dropping ball back to your previous lie."
            )
            updateTrajectoryPreview()
            return
        }

        val updatedHistory = state.shotHistory + step.newPos

        if (step.inHole) {
            val cupEffect = BallLandingEffect(
                position = step.newPos,
                surface = SurfaceType.GREEN,
                impactSpeedMps = 4f,
                title = "IN THE CUP!",
                subtitle = "Hole #${state.currentHole.holeNumber} Completed",
                colorHex = 0xFFFFD700,
                isCupSink = true
            )
            _uiState.value = _uiState.value.copy(
                lastLandingEffect = cupEffect,
                ballTrajectory = state.ballTrajectory.copy(
                    actualFlightPoints = flightPath,
                    apexAltitude = maxApex,
                    carryDistanceYds = finalCarry,
                    totalRollDistanceYds = rollDist
                ),
                holeShotLogs = updatedLogs
            )
            // --- BALL SUNK IN HOLE! ---
            onHoleCupSink()
        } else {
            // Ball rests on turf
            val nextCaddieRec = CaddieBrain.analyzeShot(
                ballPos = step.newPos,
                hole = state.currentHole,
                lie = step.lie,
                wind = state.wind
            )

            val nextAimAngle = calculateInitialHeading(step.newPos, state.currentHole.pinPosition) + nextCaddieRec.windOffsetDeg
            val nextPlayerCoords = computePlayerCoordinates(step.newPos, nextAimAngle)
            val distToPinAfter = (step.newPos - state.currentHole.pinPosition).getDistance()

            val (landingTitle, landingColorHex) = when (step.lie) {
                SurfaceType.GREEN -> Pair("ON THE GREEN • ${(distToPinAfter * 3f).toInt()} FT TO PIN", 0xFF00E676)
                SurfaceType.FAIRWAY -> Pair("FAIRWAY HIT • ${shotYardage.toInt()} YDS", 0xFF10B981)
                SurfaceType.SAND_BUNKER -> Pair("SAND BUNKER LIE • ${distToPinAfter.toInt()} YDS LEFT", 0xFFFFD700)
                SurfaceType.ROUGH -> Pair("DEEP ROUGH • ${distToPinAfter.toInt()} YDS LEFT", 0xFFF59E0B)
                else -> Pair("${step.lie.displayName.uppercase()} • ${shotYardage.toInt()} YDS", 0xFF00E5FF)
            }

            val landingEffect = BallLandingEffect(
                position = step.newPos,
                surface = step.lie,
                impactSpeedMps = state.ballTrajectory.collisionEvents.firstOrNull()?.impactSpeedMps ?: 8f,
                title = landingTitle,
                subtitle = "Carry ${finalCarry.toInt()}y • Roll ${rollDist.toInt()}y",
                colorHex = landingColorHex,
                isCupSink = false
            )

            // Auto-select smart club for next stroke
            _uiState.value = _uiState.value.copy(
                isBallInFlight = false,
                selectedClub = nextCaddieRec.recommendedClub,
                aimPowerRatio = (nextCaddieRec.suggestedPowerPercent / 100f).coerceIn(0.2f, 1.0f),
                aimAngleDeg = nextAimAngle,
                playerCoordinates = nextPlayerCoords,
                lastLandingEffect = landingEffect,
                scoreNotificationBanner = landingTitle,
                ballTrajectory = state.ballTrajectory.copy(
                    actualFlightPoints = flightPath,
                    apexAltitude = maxApex,
                    carryDistanceYds = finalCarry,
                    totalRollDistanceYds = rollDist
                ),
                holeShotLogs = updatedLogs,
                shotHistory = updatedHistory,
                caddieAdvice = nextCaddieRec.tacticalTip
            )
            updateTrajectoryPreview()
        }
    }

    private fun onHoleCupSink() {
        val state = _uiState.value
        val par = state.currentHole.par
        val strokes = state.currentStrokeCount
        val scoreEval = GolfScoringEngine.evaluateHoleScore(strokes = strokes, par = par)
        val scoreDiff = scoreEval.scoreRelativeToPar
        val scoreName = scoreEval.displayBanner
        val coinsAward = scoreEval.coinsReward

        // Update player stats cleanly using immutable copy
        val currentCareer = _uiState.value.career
        val newCareer = currentCareer.copy(
            holesPlayed = currentCareer.holesPlayed + 1,
            coins = currentCareer.coins + coinsAward,
            aces = currentCareer.aces + if (strokes == 1) 1 else 0,
            eagles = currentCareer.eagles + if (scoreDiff <= -2) 1 else 0,
            birdies = currentCareer.birdies + if (scoreDiff == -1) 1 else 0
        )
        _uiState.value = _uiState.value.copy(career = newCareer)

        if (strokes == 1) {
            unlockAchievement("ach_ace")
        }
        if (scoreDiff <= -2) {
            unlockAchievement("ach_eagle")
        }

        // Award Tour XP
        val xpGain = scoreEval.xpReward
        addTourXp(xpGain)

        if (strokes == 1 || scoreDiff <= -2) {
            GolfAudioHapticEngine.playFanfareCrowdSound()
        }

        // Update tournament leaderboard
        val updatedScores = state.playerScores + strokes
        val playerTotal = updatedScores.sum()

        // Simulate AI rivals for this hole scaled by AI Difficulty setting
        val aiDiff = state.playSettings.aiDifficulty
        val updatedRivals = state.rivals.map { rival ->
            val rivalHoleScore = CaddieBrain.simulateRivalScore(rival, state.currentHole, aiDiff)
            val newScores = (rival.scores + rivalHoleScore).toMutableList()
            rival.copy(
                scores = newScores,
                currentTotalScore = newScores.sum()
            )
        }
        val computedRank = 1 + updatedRivals.count { it.currentTotalScore < playerTotal }

        // Check 1v1 Match Play Duel
        if (state.matchPlayState.isActive) {
            val rival = state.matchPlayState.rival ?: state.matchPlayRivals[0]
            val variance = (-1..1).random() + aiDiff.strokeModifier
            val rivalStrokes = (state.currentHole.par + variance).coerceAtLeast(1)
            val holeWinner = when {
                strokes < rivalStrokes -> "PLAYER"
                strokes > rivalStrokes -> "RIVAL"
                else -> "HALVED"
            }
            val newPlayerWins = state.matchPlayState.playerHolesWon + (if (holeWinner == "PLAYER") 1 else 0)
            val newRivalWins = state.matchPlayState.rivalHolesWon + (if (holeWinner == "RIVAL") 1 else 0)
            val result = MatchPlayHoleResult(
                holeNumber = state.matchPlayState.currentMatchHole,
                playerStrokes = strokes,
                rivalStrokes = rivalStrokes,
                holeWinner = holeWinner
            )
            val isMatchOver = state.matchPlayState.currentMatchHole >= state.matchPlayState.totalMatchHoles
            val matchWinner = if (isMatchOver) {
                when {
                    newPlayerWins > newRivalWins -> "PLAYER"
                    newPlayerWins < newRivalWins -> "RIVAL"
                    else -> "TIE"
                }
            } else null

            val latestCareer = _uiState.value.career
            val matchUpdatedCareer = when {
                isMatchOver && matchWinner == "PLAYER" -> {
                    GolfAudioHapticEngine.playFanfareCrowdSound()
                    latestCareer.copy(
                        coins = latestCareer.coins + rival.winRewardCoins,
                        matchPlayWins = latestCareer.matchPlayWins + 1,
                        trophies = latestCareer.trophies + 2
                    )
                }
                isMatchOver && matchWinner == "RIVAL" -> {
                    latestCareer.copy(matchPlayLosses = latestCareer.matchPlayLosses + 1)
                }
                else -> latestCareer
            }

            _uiState.value = _uiState.value.copy(
                isBallInFlight = false,
                holeCompleted = true,
                holeScoreName = scoreName,
                career = matchUpdatedCareer,
                matchPlayState = state.matchPlayState.copy(
                    playerHolesWon = newPlayerWins,
                    rivalHolesWon = newRivalWins,
                    holeResults = state.matchPlayState.holeResults + result,
                    isMatchFinished = isMatchOver,
                    matchWinner = matchWinner,
                    rivalShotBanner = when (holeWinner) {
                        "PLAYER" -> "${rival.name}: \"${rival.pressureQuote}\""
                        "RIVAL" -> "${rival.name}: \"${rival.birdieQuote}\""
                        else -> "${rival.name}: \"All square on that hole. Tense duel!\""
                    }
                ),
                currentScreen = AppScreen.MATCH_PLAY_DUEL
            )
            persistLocalAndCloudProgress()
            return
        }

        val isLastHole = state.currentHoleIndex >= state.holes.size - 1

        _uiState.value = _uiState.value.copy(
            isBallInFlight = false,
            holeCompleted = true,
            holeScoreName = scoreName,
            coinsEarnedThisHole = coinsAward,
            playerScores = updatedScores,
            rivals = updatedRivals,
            tournamentFinalRank = computedRank,
            currentScreen = AppScreen.HOLE_SUMMARY,
            tournamentCompleted = isLastHole && !state.isPracticeMode
        )
        recordCloudMatchAndReward(
            mode = if (state.isPracticeMode) "PRACTICE" else "TOURNAMENT",
            courseName = state.currentHole.courseName,
            holesPlayed = 1,
            totalStrokes = strokes,
            scoreToPar = scoreDiff,
            scoreBanner = scoreName,
            coinsEarned = coinsAward,
            xpEarned = xpGain,
            opponentName = state.selectedCup.title,
            outcome = "COMPLETED"
        )
        persistLocalAndCloudProgress()
    }

    fun useMulligan() {
        val state = _uiState.value
        if (state.career.mulligansRemaining <= 0 || state.previousBallPos == null || state.isBallInFlight) return

        val prev = state.previousBallPos
        _uiState.value = state.copy(
            career = state.career.copy(mulligansRemaining = state.career.mulligansRemaining - 1),
            currentStrokeCount = (state.currentStrokeCount - 1).coerceAtLeast(0),
            ballState = BallState(
                x = prev.x,
                y = prev.y,
                lie = GolfPhysicsEngine.getSurfaceAt(prev, state.currentHole),
                isResting = true
            ),
            caddieAdvice = "Mulligan deployed! Fresh swing from your previous lie. Make this one count!"
        )
        updateTrajectoryPreview()
        persistLocalAndCloudProgress()
    }

    fun proceedToNextHole() {
        val state = _uiState.value
        val nextIdx = state.currentHoleIndex + 1

        if (nextIdx < state.holes.size) {
            val nextHole = state.holes[nextIdx]
            _uiState.value = _uiState.value.copy(
                currentScreen = AppScreen.PLAY_HOLE,
                currentHoleIndex = nextIdx,
                currentHole = nextHole
            )
            resetHoleToTee(nextHole)
        } else {
            // Tournament complete - calculate accurate rank and award podium trophy if top 3
            val playerTotal = state.playerScores.sum()
            val finalRank = 1 + state.rivals.count { it.currentTotalScore < playerTotal }
            val wonTrophy = if (finalRank <= 3) 1 else 0
            val prizeBonus = if (finalRank == 1) state.selectedCup.prizeCoins else 0
            _uiState.value = _uiState.value.copy(
                career = state.career.copy(
                    roundsPlayed = state.career.roundsPlayed + 1,
                    trophies = state.career.trophies + wonTrophy,
                    coins = state.career.coins + prizeBonus
                ),
                currentScreen = AppScreen.TOURNAMENT_LEADERBOARD,
                tournamentCompleted = true,
                tournamentFinalRank = finalRank
            )
            if (state.weather == WeatherType.RAIN_STORM || state.weather == WeatherType.WINDY_TEMPEST) {
                unlockAchievement("ach_tempest")
            }
            recordTournamentCompletionToCloud()
            persistLocalAndCloudProgress()
        }
    }

    fun upgradeClub(clubType: ClubType) {
        val state = _uiState.value
        val club = state.clubs.firstOrNull { it.clubType == clubType } ?: return
        if (club.level >= club.maxLevel) return
        if (state.career.coins < club.upgradeCost) return

        val updatedClubs = state.clubs.map {
            if (it.clubType == clubType) {
                it.copy(
                    level = it.level + 1,
                    powerStat = it.powerStat + 6,
                    controlStat = it.controlStat + 5,
                    spinStat = it.spinStat + 5
                )
            } else it
        }

        _uiState.value = state.copy(
            career = state.career.copy(coins = state.career.coins - club.upgradeCost),
            clubs = updatedClubs
        )
        updateTrajectoryPreview()
        persistLocalAndCloudProgress()
    }

    fun equipBall(ballId: String) {
        val state = _uiState.value
        val target = state.balls.firstOrNull { it.id == ballId } ?: return
        if (!target.isUnlocked) return
        val updated = state.balls.map {
            it.copy(isEquipped = it.id == ballId)
        }
        _uiState.value = state.copy(
            career = state.career.copy(equippedBallId = ballId),
            balls = updated
        )
        updateTrajectoryPreview()
        persistLocalAndCloudProgress()
    }

    fun buyBall(ballId: String) {
        val state = _uiState.value
        val ball = state.balls.firstOrNull { it.id == ballId } ?: return
        if (ball.isUnlocked || state.career.coins < ball.costCoins) return

        val updated = state.balls.map {
            if (it.id == ballId) it.copy(isUnlocked = true, isEquipped = true)
            else it.copy(isEquipped = false)
        }
        _uiState.value = state.copy(
            career = state.career.copy(
                coins = state.career.coins - ball.costCoins,
                equippedBallId = ballId
            ),
            balls = updated
        )
        updateTrajectoryPreview()
        persistLocalAndCloudProgress()
    }

    fun requestAiCaddieInsight() {
        val state = _uiState.value
        _uiState.value = _uiState.value.copy(isAiCaddieLoading = true)

        viewModelScope.launch {
            val dist = state.currentDistanceToPin
            val commentary = CaddieBrain.fetchGeminiCommentary(
                hole = state.currentHole,
                currentScore = state.currentStrokeCount,
                distanceToPin = dist,
                wind = state.wind,
                lie = state.ballState.lie
            )
            _uiState.value = _uiState.value.copy(
                caddieAdvice = commentary,
                isAiCaddieLoading = false
            )
        }
    }

    fun addAndEquipCustomBall(ball: GolfBallSkin) {
        val state = _uiState.value
        state.career.equippedBallId = ball.id
        val updatedBalls = state.balls.map { it.copy(isEquipped = false) } + ball.copy(isEquipped = true, isUnlocked = true)
        _uiState.value = state.copy(balls = updatedBalls)
        unlockAchievement("ach_custom_ball")
        updateTrajectoryPreview()
        persistLocalAndCloudProgress()
    }

    fun unlockAchievement(achievementId: String) {
        val state = _uiState.value
        val target = state.achievements.firstOrNull { it.id == achievementId } ?: return
        if (target.isUnlocked) return
        val unlockedAch = target.copy(isUnlocked = true)
        val updated = state.achievements.map {
            if (it.id == achievementId) unlockedAch else it
        }
        val newXp = state.career.tourXp + target.xpValue
        _uiState.value = state.copy(
            career = state.career.copy(tourXp = newXp),
            achievements = updated,
            tourPassState = state.tourPassState.copy(currentXp = newXp)
        )
        val repo = cloudRepository
        val uid = safeFirebaseUid()
        if (repo != null && uid != null) {
            viewModelScope.launch {
                repo.saveAchievementRecord(uid, unlockedAch)
                repo.recordRewardTransaction(
                    rewardType = "ACHIEVEMENT_UNLOCK",
                    sourceId = unlockedAch.id,
                    coinsDelta = 0,
                    xpDelta = unlockedAch.xpValue
                )
            }
        }
    }

    private fun checkChallengeProgression(
        shotYardage: Float,
        startLie: SurfaceType,
        endLie: SurfaceType,
        inHole: Boolean,
        endPos: Offset
    ) {
        val state = _uiState.value
        val updated = state.dailyChallenges.map { ch ->
            var completed = ch.isCompleted
            var progress = ch.currentProgress
            when (ch.targetType) {
                "LONG_DRIVE" -> {
                    if (state.selectedClub == ClubType.DRIVER && shotYardage >= 290f && endLie == SurfaceType.FAIRWAY) {
                        completed = true
                        progress = 1
                    }
                }
                "ISLAND_ACCURACY" -> {
                    val distToPin = (endPos - state.currentHole.pinPosition).getDistance()
                    if (state.currentHole.holeNumber == 2 && endLie == SurfaceType.GREEN && distToPin <= 8f) {
                        completed = true
                        progress = 1
                    }
                }
                "BUNKER_ESCAPE" -> {
                    if (startLie == SurfaceType.SAND_BUNKER && endLie == SurfaceType.GREEN) {
                        completed = true
                        progress = 1
                    }
                }
                "LONG_PUTT" -> {
                    if (state.selectedClub.isPutter && shotYardage >= 15f && inHole) {
                        completed = true
                        progress = 1
                    }
                }
            }
            ch.copy(isCompleted = completed, currentProgress = progress)
        }
        _uiState.value = _uiState.value.copy(dailyChallenges = updated)
    }

    fun setWeather(weather: WeatherType) {
        _uiState.value = _uiState.value.copy(weather = weather)
        updateTrajectoryPreview()
    }

    fun cycleWeather() {
        val all = WeatherType.values()
        val nextIdx = (all.indexOf(_uiState.value.weather) + 1) % all.size
        setWeather(all[nextIdx])
    }

    fun setCameraMode(mode: CameraViewMode) {
        _uiState.value = _uiState.value.copy(cameraMode = mode)
    }

    fun cycleCameraMode() {
        val all = CameraViewMode.values()
        val nextIdx = (all.indexOf(_uiState.value.cameraMode) + 1) % all.size
        setCameraMode(all[nextIdx])
    }

    fun claimDailyChallenge(challengeId: String) {
        val state = _uiState.value
        val challenge = state.dailyChallenges.firstOrNull { it.id == challengeId } ?: return
        if (!challenge.isCompleted || challenge.isClaimed) return

        val claimedChallenge = challenge.copy(isClaimed = true)
        val updatedChallenges = state.dailyChallenges.map {
            if (it.id == challengeId) claimedChallenge else it
        }
        GolfAudioHapticEngine.playVictoryRewardSound()
        _uiState.value = state.copy(
            career = state.career.copy(
                coins = state.career.coins + challenge.rewardCoins,
                mulligansRemaining = state.career.mulligansRemaining + challenge.rewardMulligans
            ),
            dailyChallenges = updatedChallenges
        )
        val repo = cloudRepository
        val uid = safeFirebaseUid()
        if (repo != null && uid != null) {
            viewModelScope.launch {
                repo.saveDailyChallengeRecord(uid, claimedChallenge)
                repo.recordRewardTransaction(
                    rewardType = "DAILY_CHALLENGE",
                    sourceId = claimedChallenge.id,
                    coinsDelta = challenge.rewardCoins,
                    xpDelta = 0,
                    mulligansDelta = challenge.rewardMulligans
                )
            }
        }
        persistLocalAndCloudProgress()
    }

    fun spinLuckyWheel(prize: LuckyWheelPrize) {
        val state = _uiState.value
        GolfAudioHapticEngine.playVictoryRewardSound()
        _uiState.value = state.copy(
            career = state.career.copy(
                coins = state.career.coins + prize.coins,
                mulligansRemaining = state.career.mulligansRemaining + prize.mulligans
            ),
            wheelSpinReward = "${prize.label} (+${prize.coins} Coins, +${prize.mulligans} Mulligans)"
        )
        val repo = cloudRepository
        if (repo != null && safeFirebaseUser() != null) {
            viewModelScope.launch {
                repo.recordRewardTransaction(
                    rewardType = "LUCKY_WHEEL",
                    sourceId = prize.id,
                    coinsDelta = prize.coins,
                    xpDelta = 0,
                    mulligansDelta = prize.mulligans
                )
            }
        }
        persistLocalAndCloudProgress()
    }

    fun startSpecialChallenge(challenge: DailyChallenge) {
        val holes = CourseCatalog.getChampionshipHoles()
        val targetHole = when (challenge.targetType) {
            "ISLAND_ACCURACY" -> holes.firstOrNull { it.holeNumber == 2 } ?: holes[0]
            "LONG_DRIVE" -> holes.firstOrNull { it.par == 5 } ?: holes[0]
            "BUNKER_ESCAPE" -> holes.firstOrNull { it.holeNumber == 3 } ?: holes[0]
            else -> CourseCatalog.getPracticeHole()
        }

        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.PLAY_HOLE,
            holes = listOf(targetHole),
            currentHoleIndex = 0,
            currentHole = targetHole,
            isPracticeMode = true,
            tournamentCompleted = false,
            caddieAdvice = "Challenge Objective: ${challenge.description}! Swing with precision!"
        )
        resetHoleToTee(targetHole)
    }

    // -------------------------------------------------------------
    // TARGET SHOOTOUT ARCADE MODE
    // -------------------------------------------------------------
    fun startShootoutMode() {
        val shootoutHole = CourseCatalog.getShootoutHole()
        _uiState.value = _uiState.value.copy(
            currentScreen = AppScreen.TARGET_SHOOTOUT,
            holes = listOf(shootoutHole),
            currentHoleIndex = 0,
            currentHole = shootoutHole,
            isPracticeMode = true,
            isTargetShootoutMode = true,
            tournamentCompleted = false,
            selectedClub = ClubType.IRON_7,
            aimPowerRatio = 0.88f,
            shootoutState = ShootoutGameState(
                shotsRemaining = 5,
                currentScore = 0,
                currentCombo = 0,
                shotResults = emptyList(),
                isRoundOver = false
            ),
            caddieAdvice = "Bullseye Coliseum: 5 shots to rack up mega points! Aim for the glowing 500-pt center bullseye!"
        )
        resetHoleToTee(shootoutHole)
    }

    fun resetShootoutRound() {
        val shootoutHole = CourseCatalog.getShootoutHole()
        _uiState.value = _uiState.value.copy(
            shootoutState = ShootoutGameState(
                shotsRemaining = 5,
                currentScore = 0,
                currentCombo = 0,
                shotResults = emptyList(),
                isRoundOver = false
            ),
            isBallInFlight = false,
            caddieAdvice = "New round teed up! Build your combo multiplier with consecutive target ring hits!"
        )
        resetHoleToTee(shootoutHole)
    }

    private fun handleShootoutShotLanding(
        landingPos: Offset,
        shotYardage: Float,
        flightPath: List<Offset>,
        maxApex: Float,
        finalCarry: Float,
        rollDist: Float,
        updatedLogs: List<HoleShotLog>
    ) {
        val state = _uiState.value
        val shootout = state.shootoutState
        val target = shootout.target
        val distToCenter = (landingPos - target.center).getDistance()

        val (basePoints, ringLabel, isBullseye) = when {
            distToCenter <= 4.0f -> Triple(500, "BULLSEYE JACKPOT!", true)
            distToCenter <= 9.0f -> Triple(300, "INNER RING", false)
            distToCenter <= 16.0f -> Triple(150, "MID RING", false)
            distToCenter <= 24.0f -> Triple(50, "OUTER RING", false)
            else -> Triple(0, "WATER MISS", false)
        }

        val newCombo = if (basePoints > 0) shootout.currentCombo + 1 else 0
        val comboMult = if (newCombo > 1) 1.0f + (newCombo - 1) * 0.5f else 1.0f
        val pointsAwarded = (basePoints * comboMult).toInt()

        if (isBullseye) {
            GolfAudioHapticEngine.playBullseyeBellSound()
            unlockAchievement("ach_bullseye")
        } else if (basePoints > 0) {
            GolfAudioHapticEngine.playVictoryRewardSound()
        }

        val shotIndex = 5 - shootout.shotsRemaining + 1
        val result = ShootoutShotResult(
            shotIndex = shotIndex,
            distanceToBullseyeYds = distToCenter,
            pointsEarned = pointsAwarded,
            ringHit = ringLabel,
            comboMultiplier = comboMult,
            isBullseye = isBullseye
        )

        val updatedResults = shootout.shotResults + result
        val updatedTotalScore = shootout.currentScore + pointsAwarded
        val remaining = (shootout.shotsRemaining - 1).coerceAtLeast(0)
        val isOver = remaining == 0

        var coinsAward = 0
        var xpAward = 0
        var isNewHigh = false

        if (isOver) {
            isNewHigh = updatedTotalScore > state.career.shootoutHighScore
            if (isNewHigh) {
                state.career.shootoutHighScore = updatedTotalScore
            }
            coinsAward = (updatedTotalScore / 2.5f).toInt() + 100
            xpAward = (updatedTotalScore / 4f).toInt() + 150
            state.career.coins += coinsAward
            addTourXp(xpAward)
            GolfAudioHapticEngine.playFanfareCrowdSound()
        }

        val updatedShootout = shootout.copy(
            shotsRemaining = remaining,
            currentScore = updatedTotalScore,
            currentCombo = newCombo,
            highestCombo = maxOf(shootout.highestCombo, newCombo),
            shotResults = updatedResults,
            isRoundOver = isOver,
            coinsWon = coinsAward,
            xpWon = xpAward,
            isNewHighScore = isNewHigh
        )

        _uiState.value = state.copy(
            isBallInFlight = false,
            shootoutState = updatedShootout,
            holeShotLogs = updatedLogs,
            ballTrajectory = state.ballTrajectory.copy(
                actualFlightPoints = flightPath,
                apexAltitude = maxApex,
                carryDistanceYds = finalCarry,
                totalRollDistanceYds = rollDist
            ),
            caddieAdvice = if (isOver) {
                "Shootout Complete! Final Score: $updatedTotalScore pts! +$coinsAward Coins, +$xpAward Tour XP!"
            } else {
                "Shot $shotIndex: $ringLabel ($pointsAwarded pts)! ${remaining} shots remaining!"
            }
        )

        if (!isOver) {
            resetHoleToTee(state.currentHole)
        } else {
            persistLocalAndCloudProgress()
        }
    }

    // -------------------------------------------------------------
    // TOUR PASS (SEASON 1)
    // -------------------------------------------------------------
    fun addTourXp(amount: Int) {
        val state = _uiState.value
        state.career.tourXp += amount
        val newXp = state.career.tourXp
        _uiState.value = _uiState.value.copy(
            tourPassState = state.tourPassState.copy(currentXp = newXp)
        )
        if (newXp >= 1000) {
            unlockAchievement("ach_tour_master")
        }
    }

    fun claimTourPassReward(tierLevel: Int, isVip: Boolean) {
        val state = _uiState.value
        val tier = state.tourPassState.tiers.firstOrNull { it.tierLevel == tierLevel } ?: return

        if (!isVip) {
            if (tier.isFreeClaimed) return
            state.career.coins += tier.freeCoins
            state.career.mulligansRemaining += tier.freeMulligans
            GolfAudioHapticEngine.playVictoryRewardSound()
            val updatedTiers = state.tourPassState.tiers.map {
                if (it.tierLevel == tierLevel) it.copy(isFreeClaimed = true) else it
            }
            val updatedClaimed = (state.tourPassState.claimedFreeTiers + tierLevel).distinct()
            _uiState.value = _uiState.value.copy(
                tourPassState = state.tourPassState.copy(tiers = updatedTiers, claimedFreeTiers = updatedClaimed)
            )
            persistLocalAndCloudProgress()
        } else {
            if (!state.tourPassState.isVipUnlocked || tier.isVipClaimed) return
            state.career.coins += tier.vipCoins
            state.career.mulligansRemaining += tier.vipMulligans
            GolfAudioHapticEngine.playVictoryRewardSound()
            val updatedTiers = state.tourPassState.tiers.map {
                if (it.tierLevel == tierLevel) it.copy(isVipClaimed = true) else it
            }
            val updatedClaimed = (state.tourPassState.claimedVipTiers + tierLevel).distinct()
            _uiState.value = _uiState.value.copy(
                tourPassState = state.tourPassState.copy(tiers = updatedTiers, claimedVipTiers = updatedClaimed)
            )
            persistLocalAndCloudProgress()
        }
    }

    fun unlockTourPassVip() {
        val state = _uiState.value
        val cost = 1200
        if (state.career.coins >= cost) {
            state.career.coins -= cost
            GolfAudioHapticEngine.playFanfareCrowdSound()
            _uiState.value = _uiState.value.copy(
                tourPassState = state.tourPassState.copy(isVipUnlocked = true)
            )
            persistLocalAndCloudProgress()
        }
    }

    // -------------------------------------------------------------
    // TOURNAMENT CUPS
    // -------------------------------------------------------------
    fun startTournamentCup(cup: TournamentCup) {
        val state = _uiState.value
        if (state.career.coins < cup.entryFeeCoins) return

        val championshipHoles = CourseCatalog.getChampionshipHoles()

        _uiState.value = state.copy(
            career = state.career.copy(coins = state.career.coins - cup.entryFeeCoins),
            registeredCupIds = state.registeredCupIds + cup.id,
            selectedCup = cup,
            weather = cup.defaultWeather,
            holes = championshipHoles,
            currentHoleIndex = 0,
            currentHole = championshipHoles[0],
            currentStrokeCount = 0,
            playerScores = emptyList(),
            rivals = CaddieBrain.createDefaultRivals(),
            tournamentCompleted = false,
            tournamentFinalRank = 1,
            isPracticeMode = false,
            isTargetShootoutMode = false,
            currentScreen = AppScreen.PLAY_HOLE,
            caddieAdvice = "Welcome to ${cup.title}! Competing for ${cup.prizeCoins} Gold Coins & ${cup.prizeTrophies} Tour Trophies!"
        )
        resetHoleToTee(championshipHoles[0])
        persistLocalAndCloudProgress()
    }

    // -------------------------------------------------------------
    // TRACER & VERSION DIALOG
    // -------------------------------------------------------------
    fun setTracerStyle(style: BallTracerStyle) {
        val current = _uiState.value
        _uiState.value = current.copy(
            equippedTracer = style,
            career = current.career.copy(equippedTracer = style.label)
        )
        persistLocalAndCloudProgress()
    }

    fun toggleVersionDialog(show: Boolean) {
        _uiState.value = _uiState.value.copy(showVersionUpdateDialog = show)
    }

    // -------------------------------------------------------------
    // PLAY CONSOLE & GPS RANGEFINDER SETTINGS
    // -------------------------------------------------------------
    fun updatePlaySettings(settings: PlayConsoleSettings) {
        GolfAudioHapticEngine.applySettings(settings)
        _uiState.value = _uiState.value.copy(playSettings = settings)
        persistLocalAndCloudProgress()
    }

    fun toggleRangefinder() {
        val current = _uiState.value.gpsRangefinder
        val nextEnabled = !current.isEnabled
        val pin = _uiState.value.currentHole.pinPosition
        val dist = _uiState.value.currentDistanceToPin

        _uiState.value = _uiState.value.copy(
            gpsRangefinder = current.copy(
                isEnabled = nextEnabled,
                targetPoint = if (nextEnabled) pin else null,
                distanceToPointYds = if (nextEnabled) dist else 0f,
                distancePointToPinYds = 0f,
                pointSurface = SurfaceType.GREEN
            ),
            caddieAdvice = if (nextEnabled) "GPS Rangefinder Active: Tap anywhere on the course to calculate laser yardages & layups!"
                           else "Rangefinder closed. Ready to swing!"
        )
    }

    fun onRangefinderTargetTap(point: Offset) {
        val state = _uiState.value
        val ballPos = Offset(state.ballState.x, state.ballState.y)
        val distToTap = (point - ballPos).getDistance()
        val distTapToPin = (state.currentHole.pinPosition - point).getDistance()
        val surface = GolfPhysicsEngine.getSurfaceAt(point, state.currentHole)

        _uiState.value = state.copy(
            gpsRangefinder = state.gpsRangefinder.copy(
                targetPoint = point,
                distanceToPointYds = distToTap,
                distancePointToPinYds = distTapToPin,
                pointSurface = surface
            ),
            caddieAdvice = "GPS Marker: ${distToTap.toInt()} yds carry (${surface.displayName}). Remaining to pin: ${distTapToPin.toInt()} yds."
        )
    }

    fun resetAllGameProgress() {
        val defaultState = GameUiState()
        appContext?.let { ctx ->
            LocalProgressStorage.clearSnapshot(ctx)
        }
        _uiState.value = _uiState.value.copy(
            career = PlayerCareer(
                coins = 850,
                mulligansRemaining = 3,
                trophies = 1,
                roundsPlayed = 0,
                holesPlayed = 0,
                birdies = 0,
                eagles = 0,
                aces = 0,
                longestDriveYds = 0f,
                greensInRegulationCount = 0,
                tourXp = 0,
                shootoutHighScore = 0
            ),
            tourPassState = TourPassState(currentXp = 0),
            clubs = EquipmentCatalog.getDefaultClubUpgrades(),
            balls = EquipmentCatalog.getDefaultBalls(),
            equippedTracer = BallTracerStyle.NEON_CYAN,
            golferProfile = GolferProfile(),
            achievements = defaultState.achievements,
            dailyChallenges = defaultState.dailyChallenges,
            registeredCupIds = emptySet(),
            tournamentFinalRank = 1
        )
        GolfAudioHapticEngine.playVictoryRewardSound()
        persistLocalAndCloudProgress()
    }

    // -------------------------------------------------------------
    // FIREBASE CLOUD SYNC & LOCAL PROGRESS PERSISTENCE
    // -------------------------------------------------------------
    private var cloudRepository: GolfCloudRepository? = null
    private var secureBackendClient: GolfSecureBackendClient? = null
    private var appContext: Context? = null
    private var hasLoadedLocalSnapshot: Boolean = false

    fun initializeLocalProgress(context: Context) {
        this.appContext = context.applicationContext
        val snapshot = LocalProgressStorage.loadSnapshot(
            context = context,
            defaultBalls = _uiState.value.balls,
            defaultClubs = _uiState.value.clubs
        ) ?: run {
            hasLoadedLocalSnapshot = true
            return
        }

        val current = _uiState.value
        val isInitialRestore = !hasLoadedLocalSnapshot
        hasLoadedLocalSnapshot = true
        val mergedCareer = current.career.copy(
            coins = if (isInitialRestore) snapshot.career.coins else maxOf(current.career.coins, snapshot.career.coins),
            mulligansRemaining = if (isInitialRestore) snapshot.career.mulligansRemaining else maxOf(current.career.mulligansRemaining, snapshot.career.mulligansRemaining),
            trophies = if (isInitialRestore) snapshot.career.trophies else maxOf(current.career.trophies, snapshot.career.trophies),
            roundsPlayed = maxOf(current.career.roundsPlayed, snapshot.career.roundsPlayed),
            holesPlayed = maxOf(current.career.holesPlayed, snapshot.career.holesPlayed),
            birdies = maxOf(current.career.birdies, snapshot.career.birdies),
            eagles = maxOf(current.career.eagles, snapshot.career.eagles),
            aces = maxOf(current.career.aces, snapshot.career.aces),
            longestDriveYds = maxOf(current.career.longestDriveYds, snapshot.career.longestDriveYds),
            greensInRegulationCount = maxOf(current.career.greensInRegulationCount, snapshot.career.greensInRegulationCount),
            tourXp = maxOf(current.career.tourXp, snapshot.career.tourXp),
            shootoutHighScore = maxOf(current.career.shootoutHighScore, snapshot.career.shootoutHighScore),
            equippedBallId = snapshot.career.equippedBallId,
            equippedTracer = snapshot.career.equippedTracer,
            matchPlayWins = maxOf(current.career.matchPlayWins, snapshot.career.matchPlayWins),
            matchPlayLosses = maxOf(current.career.matchPlayLosses, snapshot.career.matchPlayLosses)
        )
        val mergedChallenges = current.dailyChallenges.map { ch ->
            val completed = ch.isCompleted || snapshot.completedChallengeIds.contains(ch.id)
            val claimed = ch.isClaimed || snapshot.claimedChallengeIds.contains(ch.id)
            ch.copy(
                isCompleted = completed,
                isClaimed = claimed,
                currentProgress = if (completed) ch.targetGoal else ch.currentProgress
            )
        }
        val mergedAchievements = current.achievements.map { ach ->
            ach.copy(isUnlocked = ach.isUnlocked || snapshot.unlockedAchievementIds.contains(ach.id))
        }
        val restoredTracer = BallTracerStyle.entries.firstOrNull {
            it.label.equals(snapshot.career.equippedTracer, ignoreCase = true) ||
                it.name.equals(snapshot.career.equippedTracer, ignoreCase = true)
        } ?: current.equippedTracer

        GolfAudioHapticEngine.applySettings(snapshot.playSettings)
        _uiState.value = current.copy(
            career = mergedCareer,
            balls = snapshot.balls,
            clubs = snapshot.clubs,
            tourPassState = snapshot.tourPassState,
            golferProfile = snapshot.golferProfile,
            playSettings = snapshot.playSettings,
            customHoleBlueprint = snapshot.customHoleBlueprint,
            dailyChallenges = mergedChallenges,
            achievements = mergedAchievements,
            equippedTracer = restoredTracer,
            dailyLoginStreak = snapshot.dailyLoginStreak,
            lastDailyClaimDayEpoch = snapshot.lastDailyClaimDayEpoch,
            unlockedAccessoryIds = snapshot.unlockedAccessoryIds
        )
        updateTrajectoryPreview()
    }

    private fun persistLocalAndCloudProgress() {
        val ctx = appContext
        val state = _uiState.value
        if (ctx != null) {
            LocalProgressStorage.saveSnapshot(
                context = ctx,
                career = state.career,
                balls = state.balls,
                clubs = state.clubs,
                tourPass = state.tourPassState,
                golferProfile = state.golferProfile,
                playSettings = state.playSettings,
                customHoleBlueprint = state.customHoleBlueprint,
                dailyChallenges = state.dailyChallenges,
                achievements = state.achievements,
                dailyLoginStreak = state.dailyLoginStreak,
                lastDailyClaimDayEpoch = state.lastDailyClaimDayEpoch,
                unlockedAccessoryIds = state.unlockedAccessoryIds
            )
        }
        syncCareerToCloud()
    }

    fun initializeCloudSync(
        userId: String,
        email: String?,
        displayName: String?,
        repository: GolfCloudRepository,
        profileImageUrl: String? = null
    ) {
        this.cloudRepository = repository
        this.secureBackendClient = safeFirebaseAuth()?.let { GolfSecureBackendClient(repository = repository, auth = it) }
        _uiState.value = _uiState.value.copy(
            cloudSyncUserId = userId,
            cloudSyncUserEmail = email ?: "Golfer ($userId)",
            cloudProfileImageUrl = profileImageUrl.orEmpty(),
            cloudSyncStatus = "Synchronizing Cloud Data...",
            isCloudSyncInProgress = true,
            cloudSyncError = null
        )

        // 1. Initial multi-collection restoration & non-destructive conflict resolution
        viewModelScope.launch {
            try {
                val cloudPlayer = repository.getPlayerDocument(userId).getOrNull()
                val cloudStats = repository.getPlayerStatistics(userId).getOrNull()
                val cloudInventory = repository.getPlayerInventory(userId).getOrNull()
                val cloudProgress = repository.getPlayerProgress(userId).getOrNull()
                val cloudSettings = repository.getGameSettings(userId).getOrNull()
                val cloudLegacy = repository.getUserProfileById(userId).getOrNull()

                val state = _uiState.value
                val unlockedCourseIds = state.tournamentCups.filter { it.isUnlocked }.map { it.id }
                val unlockedAchIds = state.achievements.filter { it.isUnlocked }.map { it.id }.toSet()
                val completedChIds = state.dailyChallenges.filter { it.isCompleted }.map { it.id }.toSet()
                val claimedChIds = state.dailyChallenges.filter { it.isClaimed }.map { it.id }.toSet()

                val resolved = repository.resolveSyncConflict(
                    localCareer = state.career,
                    localBalls = state.balls,
                    localClubs = state.clubs,
                    localTourPass = state.tourPassState,
                    localGolfer = state.golferProfile,
                    localSettings = state.playSettings,
                    localUnlockedCourses = unlockedCourseIds,
                    localUnlockedAchievements = unlockedAchIds,
                    localCompletedChallenges = completedChIds,
                    localClaimedChallenges = claimedChIds,
                    localUpdatedAtEpochMs = 0L,
                    cloudPlayer = cloudPlayer,
                    cloudStats = cloudStats,
                    cloudInventory = cloudInventory,
                    cloudProgress = cloudProgress,
                    cloudSettings = cloudSettings,
                    cloudLegacyProfile = cloudLegacy
                )

                val mergedAchievements = state.achievements.map { ach ->
                    ach.copy(isUnlocked = ach.isUnlocked || resolved.unlockedAchievementIds.contains(ach.id))
                }
                val mergedChallenges = state.dailyChallenges.map { ch ->
                    val completed = ch.isCompleted || resolved.completedChallengeIds.contains(ch.id)
                    val claimed = ch.isClaimed || resolved.claimedChallengeIds.contains(ch.id)
                    ch.copy(
                        isCompleted = completed,
                        isClaimed = claimed,
                        currentProgress = if (completed) ch.targetGoal else ch.currentProgress
                    )
                }

                GolfAudioHapticEngine.applySettings(resolved.playSettings)
                _uiState.value = state.copy(
                    career = resolved.career,
                    balls = resolved.balls,
                    clubs = resolved.clubs,
                    tourPassState = resolved.tourPassState,
                    golferProfile = resolved.golferProfile,
                    playSettings = resolved.playSettings,
                    achievements = mergedAchievements,
                    dailyChallenges = mergedChallenges,
                    cloudSyncRevision = resolved.syncRevision,
                    cloudSyncStatus = "Cloud Sync Active (Rev #${resolved.syncRevision})",
                    isCloudSyncInProgress = false,
                    cloudSyncError = null
                )

                // Save merged progress to local storage & all Cloud Firestore collections
                appContext?.let { ctx ->
                    LocalProgressStorage.saveSnapshot(
                        context = ctx,
                        career = resolved.career,
                        balls = resolved.balls,
                        clubs = resolved.clubs,
                        tourPass = resolved.tourPassState,
                        golferProfile = resolved.golferProfile,
                        playSettings = resolved.playSettings,
                        customHoleBlueprint = _uiState.value.customHoleBlueprint,
                        dailyChallenges = mergedChallenges,
                        achievements = mergedAchievements
                    )
                }

                // Seed tournament cups into `tournaments` collection and sync full player state
                state.tournamentCups.forEach { cup ->
                    repository.upsertTournament(cup, status = "OPEN")
                }
                mergedAchievements.filter { it.isUnlocked }.forEach { ach ->
                    repository.saveAchievementRecord(userId, ach)
                }
                mergedChallenges.forEach { ch ->
                    repository.saveDailyChallengeRecord(userId, ch)
                }
                syncCareerToCloud()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCloudSyncInProgress = false,
                    cloudSyncStatus = "Offline Local Mode (Sync Queued)",
                    cloudSyncError = e.localizedMessage ?: "Cloud sync temporarily unavailable"
                )
            }
        }

        // 2. Real-time listeners for live multi-device updates
        viewModelScope.launch {
            repository.observeUserProfile(userId)
                .catch { emit(null) }
                .collect { cloudProfile ->
                    if (cloudProfile != null) {
                        val state = _uiState.value
                        val currentCareer = state.career
                        val merged = currentCareer.copy(
                            coins = maxOf(currentCareer.coins, cloudProfile.coins.toInt()),
                            trophies = maxOf(currentCareer.trophies, cloudProfile.trophies.toInt()),
                            mulligansRemaining = maxOf(currentCareer.mulligansRemaining, cloudProfile.mulligansRemaining.toInt()),
                            roundsPlayed = maxOf(currentCareer.roundsPlayed, cloudProfile.roundsPlayed.toInt()),
                            holesPlayed = maxOf(currentCareer.holesPlayed, cloudProfile.holesPlayed.toInt()),
                            birdies = maxOf(currentCareer.birdies, cloudProfile.birdies.toInt()),
                            eagles = maxOf(currentCareer.eagles, cloudProfile.eagles.toInt()),
                            aces = maxOf(currentCareer.aces, cloudProfile.aces.toInt()),
                            longestDriveYds = maxOf(currentCareer.longestDriveYds, cloudProfile.longestDriveYds.toFloat()),
                            tourXp = maxOf(currentCareer.tourXp, cloudProfile.tourXp.toInt()),
                            shootoutHighScore = maxOf(currentCareer.shootoutHighScore, cloudProfile.shootoutHighScore.toInt()),
                            equippedBallId = cloudProfile.equippedBallId.ifBlank { currentCareer.equippedBallId },
                            equippedTracer = cloudProfile.equippedTracer.ifBlank { currentCareer.equippedTracer }
                        )
                        val unlockedSet = cloudProfile.unlockedBallIds.toSet()
                        val mergedBalls = state.balls.map { ball ->
                            val isUnlocked = ball.isUnlocked || unlockedSet.contains(ball.id) || ball.id == "standard_white"
                            ball.copy(
                                isUnlocked = isUnlocked,
                                isEquipped = ball.id == merged.equippedBallId && isUnlocked
                            )
                        }
                        val clubLevels = mapOf(
                            ClubType.DRIVER to cloudProfile.driverLevel.toInt(),
                            ClubType.WOOD_3 to cloudProfile.wood3Level.toInt(),
                            ClubType.IRON_7 to cloudProfile.iron7Level.toInt(),
                            ClubType.PITCHING_WEDGE to cloudProfile.wedgeLevel.toInt(),
                            ClubType.PUTTER to cloudProfile.putterLevel.toInt()
                        )
                        val mergedClubs = state.clubs.map { club ->
                            val targetLvl = maxOf(club.level, (clubLevels[club.clubType] ?: 1).coerceIn(1, club.maxLevel))
                            val diff = (targetLvl - club.level).coerceAtLeast(0)
                            if (diff > 0) {
                                club.copy(
                                    level = targetLvl,
                                    powerStat = club.powerStat + diff * 6,
                                    controlStat = club.controlStat + diff * 5,
                                    spinStat = club.spinStat + diff * 5
                                )
                            } else club
                        }
                        val mergedGolfer = if (cloudProfile.golferName.isNotBlank()) {
                            state.golferProfile.copy(golferName = cloudProfile.golferName)
                        } else state.golferProfile

                        _uiState.value = state.copy(
                            career = merged,
                            balls = mergedBalls,
                            clubs = mergedClubs,
                            golferProfile = mergedGolfer
                        )
                    }
                }
        }

        viewModelScope.launch {
            repository.observeTourPass(userId)
                .catch { emit(null) }
                .collect { cloudPass ->
                    if (cloudPass != null) {
                        val currentPass = _uiState.value.tourPassState
                        val mergedPass = currentPass.copy(
                            currentXp = maxOf(currentPass.currentXp, cloudPass.currentXp.toInt()),
                            claimedFreeTiers = (currentPass.claimedFreeTiers + cloudPass.claimedFreeTiers.map { it.toInt() }).distinct(),
                            claimedVipTiers = (currentPass.claimedVipTiers + cloudPass.claimedVipTiers.map { it.toInt() }).distinct(),
                            isVipUnlocked = currentPass.isVipUnlocked || cloudPass.isVipUnlocked
                        )
                        _uiState.value = _uiState.value.copy(tourPassState = mergedPass)
                    }
                }
        }

        viewModelScope.launch {
            repository.observeLeaderboard()
                .catch { emit(emptyList()) }
                .collect { entries ->
                    _uiState.value = _uiState.value.copy(globalCloudLeaderboard = entries)
                }
        }

        viewModelScope.launch {
            repository.observeUserMatchHistory(userId)
                .catch { emit(emptyList()) }
                .collect { history ->
                    _uiState.value = _uiState.value.copy(cloudMatchHistory = history)
                }
        }
    }

    private fun recordCloudMatchAndReward(
        mode: String,
        courseName: String,
        holesPlayed: Int,
        totalStrokes: Int,
        scoreToPar: Int,
        scoreBanner: String,
        coinsEarned: Int,
        xpEarned: Int,
        opponentName: String,
        outcome: String
    ) {
        val repo = cloudRepository ?: return
        if (safeFirebaseUser() == null) return
        viewModelScope.launch {
            repo.checkAndRecordRateLimit("SUBMIT_MATCH_SCORE")
            val matchIdResult = repo.recordMatchHistory(
                mode = mode,
                courseName = courseName,
                holesPlayed = holesPlayed,
                totalStrokes = totalStrokes,
                scoreToPar = scoreToPar,
                scoreBanner = scoreBanner,
                coinsEarned = coinsEarned,
                xpEarned = xpEarned,
                opponentName = opponentName,
                outcome = outcome
            )
            repo.recordRewardTransaction(
                rewardType = "HOLE_COMPLETION",
                sourceId = "$courseName-H${_uiState.value.currentHole.holeNumber}",
                coinsDelta = coinsEarned,
                xpDelta = xpEarned
            )
            matchIdResult.getOrNull()?.let { matchId ->
                repo.appendAuditLog(
                    actionType = "SCORE_SUBMITTED",
                    resourceType = "match_history",
                    resourceId = matchId,
                    status = "SUCCESS",
                    metadataSummary = "Verified $mode on $courseName: strokes=$totalStrokes ($scoreToPar), +$coinsEarned coins"
                )
            }
        }
    }

    private fun recordTournamentCompletionToCloud() {
        val repo = cloudRepository ?: return
        val auth = safeFirebaseAuth() ?: return
        val backend = secureBackendClient ?: GolfSecureBackendClient(repo, auth)
        val user = safeFirebaseUser() ?: return
        val state = _uiState.value
        val totalStrokes = state.playerScores.sum().coerceAtLeast(1)
        val totalPar = state.holes.take(state.playerScores.size).sumOf { it.par }.coerceAtLeast(1)
        val scoreToPar = totalStrokes - totalPar
        val username = state.golferProfile.golferName.ifBlank { user.displayName ?: "Apex Pro" }
        viewModelScope.launch {
            repo.checkAndRecordRateLimit("TOURNAMENT_SUBMIT_SCORE")
            backend.registerForTournament(
                tournamentId = state.selectedCup.id,
                username = username,
                entryFeeCoins = state.selectedCup.entryFeeCoins,
                trophiesAtEntry = state.career.trophies
            )
            val entryRes = repo.submitTournamentEntry(
                tournamentId = state.selectedCup.id,
                username = username,
                strokes = totalStrokes,
                scoreToPar = scoreToPar,
                holesCompleted = state.playerScores.size.coerceAtLeast(1),
                rankPosition = state.tournamentFinalRank,
                rewardCoinsAwarded = state.selectedCup.prizeCoins,
                status = "COMPLETED"
            )
            repo.recordRewardTransaction(
                rewardType = "TOURNAMENT_PRIZE",
                sourceId = state.selectedCup.id,
                coinsDelta = state.selectedCup.prizeCoins,
                xpDelta = 250
            )
            entryRes.getOrNull()?.let { entryId ->
                repo.appendAuditLog(
                    actionType = "TOURNAMENT_SCORED",
                    resourceType = "tournament_entries",
                    resourceId = entryId,
                    status = "SUCCESS",
                    metadataSummary = "Verified tournament ${state.selectedCup.id}: strokes=$totalStrokes ($scoreToPar), rank=#${state.tournamentFinalRank}"
                )
            }
        }
    }

    fun syncCareerToCloud() {
        val repo = cloudRepository ?: return
        val auth = safeFirebaseAuth() ?: return
        val user = auth.currentUser ?: return
        val uid = user.uid
        if (!_uiState.value.playSettings.isCloudSyncEnabled) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCloudSyncInProgress = true,
                cloudSyncError = null
            )
            try {
                val state = _uiState.value
                val nextRev = state.cloudSyncRevision + 1L
                val unlockedCourseIds = state.tournamentCups.filter { it.isUnlocked }.map { it.id }

                repo.syncFullPlayerState(
                    userId = uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: "Pro Golfer",
                    profileImageUrl = user.photoUrl?.toString() ?: state.cloudProfileImageUrl,
                    career = state.career,
                    balls = state.balls,
                    clubs = state.clubs,
                    tourPass = state.tourPassState,
                    golferProfile = state.golferProfile,
                    playSettings = state.playSettings,
                    unlockedCourses = unlockedCourseIds,
                    achievements = state.achievements,
                    dailyChallenges = state.dailyChallenges,
                    syncRevision = nextRev
                ).getOrThrow()

                _uiState.value = _uiState.value.copy(
                    isCloudSyncInProgress = false,
                    cloudSyncError = null,
                    cloudSyncRevision = nextRev,
                    cloudSyncStatus = "Synced Just Now (Rev #$nextRev)"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCloudSyncInProgress = false,
                    cloudSyncStatus = "Sync Queued (Offline Safe)",
                    cloudSyncError = e.localizedMessage
                )
            }
        }
    }

    /**
     * Google Play Account Deletion Policy compliance:
     * Erases local progress, deletes owner Firestore collections, and deletes the FirebaseAuth account.
     */
    fun deleteAccountAndCloudData(
        deleteAuthAccount: suspend () -> Result<Unit>,
        onAccountDeleted: () -> Unit = {}
    ) {
        if (_uiState.value.isDeletingAccount) return
        val repo = cloudRepository
        val uid = safeFirebaseUid()

        ballFlightJob?.cancel()
        apexOscillatorJob?.cancel()
        _uiState.value = _uiState.value.copy(
            isDeletingAccount = true,
            accountDeletionMessage = null
        )

        viewModelScope.launch {
            try {
                if (repo != null && uid != null) {
                    repo.deleteAllUserData(uid).getOrThrow()
                }
                val authDeleteResult = deleteAuthAccount()
                authDeleteResult.getOrThrow()

                appContext?.let { ctx ->
                    LocalProgressStorage.clearSnapshot(ctx)
                }
                resetAllGameProgress()
                _uiState.value = _uiState.value.copy(
                    isDeletingAccount = false,
                    accountDeletionMessage = "Account and cloud records permanently deleted."
                )
                onAccountDeleted()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeletingAccount = false,
                    accountDeletionMessage = e.localizedMessage ?: "Unable to delete account. Please sign in again and retry."
                )
            }
        }
    }

    // --- 1v1 MATCH PLAY DUEL METHODS ---
    fun selectMatchPlayRival(rival: MatchPlayRival) {
        _uiState.value = _uiState.value.copy(selectedMatchPlayRival = rival)
    }

    fun startMatchPlayDuel(rival: MatchPlayRival, totalHoles: Int = 3) {
        val state = _uiState.value
        val updatedCareer = if (state.career.coins >= rival.wagerCostCoins) {
            state.career.copy(coins = state.career.coins - rival.wagerCostCoins)
        } else state.career
        val matchHoles = CourseCatalog.getChampionshipHoles().take(totalHoles)
        val firstHole = matchHoles[0]

        _uiState.value = state.copy(
            career = updatedCareer,
            holes = matchHoles,
            currentHoleIndex = 0,
            currentHole = firstHole,
            isPracticeMode = false,
            isTargetShootoutMode = false,
            matchPlayState = MatchPlayState(
                isActive = true,
                rival = rival,
                totalMatchHoles = totalHoles,
                currentMatchHole = 1,
                rivalShotBanner = "${rival.name}: \"${rival.introQuote}\""
            ),
            currentScreen = AppScreen.PLAY_HOLE
        )
        resetHoleToTee(firstHole)
    }

    fun proceedToNextMatchPlayHole() {
        val state = _uiState.value
        val nextHoleNum = state.matchPlayState.currentMatchHole + 1
        val nextIdx = state.currentHoleIndex + 1

        if (nextIdx < state.holes.size && nextHoleNum <= state.matchPlayState.totalMatchHoles) {
            val nextHole = state.holes[nextIdx]
            _uiState.value = _uiState.value.copy(
                currentHoleIndex = nextIdx,
                currentHole = nextHole,
                matchPlayState = state.matchPlayState.copy(
                    currentMatchHole = nextHoleNum,
                    playerStrokesOnHole = 0,
                    rivalStrokesOnHole = 0,
                    rivalShotBanner = "${state.matchPlayState.rival?.name}: \"On to Hole $nextHoleNum! Match is ${state.matchPlayState.matchScoreStatus}\""
                ),
                currentScreen = AppScreen.PLAY_HOLE
            )
            resetHoleToTee(nextHole)
        } else {
            _uiState.value = _uiState.value.copy(currentScreen = AppScreen.MATCH_PLAY_DUEL)
        }
    }

    fun exitMatchPlay() {
        _uiState.value = _uiState.value.copy(
            matchPlayState = MatchPlayState(isActive = false),
            currentScreen = AppScreen.HOME
        )
    }

    // --- COURSE ARCHITECT METHODS ---
    fun updateCustomHoleBlueprint(blueprint: CustomHoleBlueprint) {
        _uiState.value = _uiState.value.copy(customHoleBlueprint = blueprint)
        persistLocalAndCloudProgress()
    }

    fun playtestCustomHole() {
        val customHole = CourseCatalog.createCustomHole(_uiState.value.customHoleBlueprint)
        _uiState.value = _uiState.value.copy(
            holes = listOf(customHole),
            currentHoleIndex = 0,
            currentHole = customHole,
            isPracticeMode = true,
            isTargetShootoutMode = false,
            matchPlayState = MatchPlayState(isActive = false),
            currentScreen = AppScreen.PLAY_HOLE
        )
        resetHoleToTee(customHole)
    }

    // --- REPLAY THEATER METHODS ---
    fun playReplayShot(replay: ReplayShot) {
        _uiState.value = _uiState.value.copy(
            activeReplayShot = replay,
            currentScreen = AppScreen.REPLAY_THEATER
        )
    }

    fun stopReplay() {
        _uiState.value = _uiState.value.copy(activeReplayShot = null)
    }

    fun saveCurrentShotToReplay(customTitle: String? = null) {
        val state = _uiState.value
        val carry = state.ballTrajectory.carryDistanceYds.takeIf { it > 0 } ?: state.ballState.distanceTraveled
        val replay = ReplayShot(
            id = "rep_${System.currentTimeMillis()}",
            title = customTitle ?: "${state.currentHole.holeTitle} - ${carry.toInt()}yd Shot",
            holeName = "${state.currentHole.courseName} #${state.currentHole.holeNumber}",
            clubUsed = state.selectedClub,
            carryYards = carry,
            ballSpeedMph = 145f + (state.aimPowerRatio * 35f),
            launchAngleDeg = 14.5f,
            apexHeightFt = state.ballTrajectory.apexAltitude * 3f,
            spinRpm = (state.aimPowerRatio * 4200f).toInt(),
            scoreAchieved = if (state.holeCompleted) state.holeScoreName else "${carry.toInt()} YDS CARRY",
            dateString = "Zenith Tour Live",
            trajectoryPoints = state.ballTrajectory.actualFlightPoints.ifEmpty { state.trajectoryPreview },
            endLie = state.ballState.lie
        )
        _uiState.value = _uiState.value.copy(
            savedReplays = listOf(replay) + state.savedReplays
        )
    }

    fun toggleSlopeBreakRadar() {
        val current = _uiState.value.playSettings.isSlopeBreakRadarVisible
        val updated = _uiState.value.playSettings.copy(isSlopeBreakRadarVisible = !current)
        _uiState.value = _uiState.value.copy(playSettings = updated)
    }

    fun updateGolferProfile(profile: GolferProfile) {
        _uiState.value = _uiState.value.copy(golferProfile = profile)
        updateTrajectoryPreview()
        persistLocalAndCloudProgress()
    }

    fun startThreeHolePrototype() {
        val threeHoles = CourseCatalog.getChampionshipHoles().take(3)
        val firstHole = threeHoles[0]
        _uiState.value = _uiState.value.copy(
            holes = threeHoles,
            currentHoleIndex = 0,
            currentHole = firstHole,
            playerScores = emptyList(),
            rivals = CaddieBrain.createDefaultRivals(),
            tournamentCompleted = false,
            isPracticeMode = false,
            isTargetShootoutMode = false,
            matchPlayState = MatchPlayState(isActive = false),
            currentScreen = AppScreen.PLAY_HOLE
        )
        resetHoleToTee(firstHole)
    }

    fun setOnlineStatus(online: Boolean) {
        if (_uiState.value.isOnline != online) {
            _uiState.value = _uiState.value.copy(isOnline = online)
        }
    }

    fun setGuestSession(isGuest: Boolean) {
        _uiState.value = _uiState.value.copy(
            isGuestSession = isGuest,
            cloudSyncStatus = if (isGuest) "Offline Guest Mode (Local Save)" else _uiState.value.cloudSyncStatus
        )
    }

    fun updateThemeMode(mode: ZenithThemeMode) {
        val updated = _uiState.value.playSettings.copy(themeMode = mode)
        updatePlaySettings(updated)
    }

    fun dismissOnboarding() {
        val updated = _uiState.value.playSettings.copy(hasCompletedOnboarding = true)
        updatePlaySettings(updated)
    }

    fun markNotificationsRead() {
        val current = _uiState.value
        _uiState.value = current.copy(
            notifications = current.notifications.map { it.copy(isUnread = false) }
        )
    }

    fun claimDailyLoginReward() {
        val current = _uiState.value
        val todayEpoch = System.currentTimeMillis() / 86_400_000L
        if (current.lastDailyClaimDayEpoch >= todayEpoch) return

        val isConsecutive = current.lastDailyClaimDayEpoch == todayEpoch - 1L
        val nextStreak = if (isConsecutive) (current.dailyLoginStreak % 7) + 1 else 1
        val coinReward = 100 + (nextStreak * 50)
        val mulliganBonus = if (nextStreak == 3 || nextStreak == 7) 1 else 0

        _uiState.value = current.copy(
            dailyLoginStreak = nextStreak,
            lastDailyClaimDayEpoch = todayEpoch,
            career = current.career.copy(
                coins = current.career.coins + coinReward,
                mulligansRemaining = current.career.mulligansRemaining + mulliganBonus,
                tourXp = current.career.tourXp + 50
            ),
            tourPassState = current.tourPassState.copy(
                currentXp = current.tourPassState.currentXp + 50
            ),
            wheelSpinReward = "Day $nextStreak Login Bonus: +$coinReward Coins" +
                if (mulliganBonus > 0) " & +$mulliganBonus Mulligan!" else "!"
        )
        GolfAudioHapticEngine.playVictoryRewardSound()
        persistLocalAndCloudProgress()
    }

    fun purchaseAccessoryOrCosmetic(
        itemId: String,
        costCoins: Int,
        onApplied: (GolferProfile) -> GolferProfile
    ): Boolean {
        val current = _uiState.value
        val alreadyOwned = current.unlockedAccessoryIds.contains(itemId) || costCoins <= 0
        if (!alreadyOwned && current.career.coins < costCoins) return false

        val newCoins = if (alreadyOwned) current.career.coins else current.career.coins - costCoins
        val updatedProfile = onApplied(current.golferProfile)
        _uiState.value = current.copy(
            career = current.career.copy(coins = newCoins),
            unlockedAccessoryIds = current.unlockedAccessoryIds + itemId,
            golferProfile = updatedProfile
        )
        GolfAudioHapticEngine.playVictoryRewardSound()
        persistLocalAndCloudProgress()
        return true
    }

    fun grantBillingCoins(coins: Int) {
        if (coins <= 0) return
        val current = _uiState.value
        _uiState.value = current.copy(
            career = current.career.copy(coins = current.career.coins + coins)
        )
        GolfAudioHapticEngine.playVictoryRewardSound()
        persistLocalAndCloudProgress()
    }

    private fun safeFirebaseAuth() = runCatching { Firebase.auth }.getOrNull()
    private fun safeFirebaseUser() = safeFirebaseAuth()?.currentUser
    private fun safeFirebaseUid(): String? = safeFirebaseUser()?.uid
}
