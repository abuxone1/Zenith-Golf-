package com.example

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.firebase.GolfCloudRepository
import com.example.game.firebase.GoogleAuthManager
import com.example.game.firebase.NetworkConnectivityObserver
import com.example.game.model.ZenithThemeMode
import com.example.game.ui.components.ZenithBottomNavBar
import com.example.game.ui.components.ZenithNavigationRail
import com.example.game.ui.components.isBottomBarVisibleForScreen
import com.example.game.ui.screens.CaddieChatScreen
import com.example.game.ui.screens.CareerStatsScreen
import com.example.game.ui.screens.CourseArchitectScreen
import com.example.game.ui.screens.CourseSelectionScreen
import com.example.game.ui.screens.CupSelectScreen
import com.example.game.ui.screens.CustomBallStudioScreen
import com.example.game.ui.screens.DailyChallengesScreen
import com.example.game.ui.screens.GameModesScreen
import com.example.game.ui.screens.GolferCustomizationScreen
import com.example.game.ui.screens.HoleSummaryScreen
import com.example.game.ui.screens.HomeScreen
import com.example.game.ui.screens.MatchPlayDuelScreen
import com.example.game.ui.screens.PlayHoleScreen
import com.example.game.ui.screens.PlaySettingsScreen
import com.example.game.ui.screens.ProShopScreen
import com.example.game.ui.screens.ReplayTheaterScreen
import com.example.game.ui.screens.SplashScreen
import com.example.game.ui.screens.TargetShootoutScreen
import com.example.game.ui.screens.TechnicalBlueprintScreen
import com.example.game.ui.screens.TourPassScreen
import com.example.game.ui.screens.TournamentLeaderboardScreen
import com.example.game.ui.screens.ZenithSignInScreen
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.GolfGameViewModel
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.ZenithTheme
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.recaptcha.RecaptchaAppCheckProviderFactory
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

private const val TAG = "AppCheckInit"
private const val DEBUG_TOKEN_EXTRA = "FIREBASE_APPCHECK_DEBUG_TOKEN"
private const val DEBUG_STORE_PREFIX = "com.google.firebase.appcheck.debug.store."
private const val DEBUG_SECRET_KEY = "com.google.firebase.appcheck.debug.DEBUG_SECRET"
private const val RECAPTCHA_SITE_KEY_RES = "recaptcha_site_key"

fun configureAppCheck(context: Context, intent: Intent?) {
    try {
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }

        if (BuildConfig.DEBUG) {
            val debugToken = intent?.getStringExtra(DEBUG_TOKEN_EXTRA)

            if (!debugToken.isNullOrEmpty()) {
                val persistenceKey = FirebaseApp.getInstance().persistenceKey
                val prefs = context.getSharedPreferences(
                    "$DEBUG_STORE_PREFIX$persistenceKey",
                    Context.MODE_PRIVATE
                )
                prefs.edit()
                    .putString(DEBUG_SECRET_KEY, debugToken)
                    .commit()
                Log.d(TAG, "App Check debug secret configured successfully.")
            } else {
                Log.w(TAG, "No debug token found in Intent extra.")
            }

            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance()
            )
            Log.d(TAG, "DebugAppCheckProviderFactory installed.")
        } else {
            val resId = context.resources.getIdentifier(RECAPTCHA_SITE_KEY_RES, "string", context.packageName)
            if (resId != 0) {
                val siteKey = context.getString(resId)
                if (siteKey.isNotBlank()) {
                    FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                        RecaptchaAppCheckProviderFactory.getInstance(siteKey)
                    )
                    Log.d(TAG, "RecaptchaAppCheckProviderFactory installed.")
                } else {
                    Log.e(TAG, "recaptcha_site_key resource string is blank.")
                }
            } else {
                Log.e(TAG, "recaptcha_site_key string resource not found in package.")
            }
        }
    } catch (e: Exception) {
        Log.e(TAG, "Failed to initialize Firebase App Check", e)
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Configure App Check with launch intent before any UI or Firebase operations
        configureAppCheck(this, intent)

        com.example.game.engine.GolfAudioHapticEngine.initialize(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val colors = ZenithTheme.colors
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colors.background),
                    color = colors.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember(context) { GoogleAuthManager(context) }
    val currentUser by authManager.authStateFlow().collectAsStateWithLifecycle(initialValue = authManager.currentUser)
    var hasEnteredFromSplash by rememberSaveable { mutableStateOf(false) }
    var isGuestMode by rememberSaveable { mutableStateOf(false) }

    if (!hasEnteredFromSplash) {
        SplashScreen(
            onEnterClubhouse = { hasEnteredFromSplash = true },
            autoAdvanceMs = 2600L
        )
    } else {
        val user = currentUser
        if (user == null && !isGuestMode) {
            ZenithSignInScreen(
                onSignInSuccess = { isGuestMode = false },
                onContinueAsGuest = { isGuestMode = true }
            )
        } else {
            GolfAppRoot(
                currentUser = user,
                isGuestSession = user == null && isGuestMode,
                onRequestAccountSignIn = { isGuestMode = false },
                onSignOut = {
                    scope.launch {
                        authManager.signOut()
                        isGuestMode = false
                    }
                },
                onDeleteAuthAccount = { authManager.deleteFirebaseAccount() }
            )
        }
    }
}

@Composable
fun GolfAppRoot(
    currentUser: FirebaseUser? = null,
    isGuestSession: Boolean = false,
    onRequestAccountSignIn: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onDeleteAuthAccount: suspend () -> Result<Unit> = { Result.success(Unit) },
    viewModel: GolfGameViewModel = viewModel(key = currentUser?.uid ?: "zenith_golf_vm")
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isOnline by remember(context) {
        NetworkConnectivityObserver.observeIsOnline(context)
    }.collectAsState(initial = NetworkConnectivityObserver.isCurrentlyOnline(context))

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_PAUSE || event == Lifecycle.Event.ON_STOP) {
                viewModel.pauseOnLifecycleStop(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(isOnline) {
        viewModel.setOnlineStatus(isOnline)
    }

    LaunchedEffect(isGuestSession) {
        viewModel.setGuestSession(isGuestSession)
    }

    LaunchedEffect(currentUser?.uid) {
        viewModel.initializeLocalProgress(context)
        val user = currentUser
        if (user != null) {
            val repository = GolfCloudRepository(context)
            viewModel.initializeCloudSync(
                userId = user.uid,
                email = user.email,
                displayName = user.displayName,
                repository = repository,
                profileImageUrl = user.photoUrl?.toString()
            )
        }
    }

    val isDarkTheme = when (uiState.playSettings.themeMode) {
        ZenithThemeMode.DARK -> true
        ZenithThemeMode.LIGHT -> false
        ZenithThemeMode.SYSTEM -> isSystemInDarkTheme()
    }

    MyApplicationTheme(darkTheme = isDarkTheme) {
        val colors = ZenithTheme.colors
        val unclaimedMissionsCount = uiState.dailyChallenges.count { it.isCompleted && !it.isClaimed }
        val showBottomBar = isBottomBarVisibleForScreen(uiState.currentScreen)

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = showBottomBar && maxWidth >= 840.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = colors.background,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    if (showBottomBar && !useNavigationRail) {
                        ZenithBottomNavBar(
                            currentScreen = uiState.currentScreen,
                            unclaimedRewardsCount = unclaimedMissionsCount,
                            onSelectTab = { targetScreen ->
                                viewModel.navigateTo(targetScreen)
                            }
                        )
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        ZenithNavigationRail(
                            currentScreen = uiState.currentScreen,
                            unclaimedRewardsCount = unclaimedMissionsCount,
                            onSelectTab = { targetScreen ->
                                viewModel.navigateTo(targetScreen)
                            }
                        )
                    }

                    Column(modifier = Modifier.weight(1f).fillMaxSize()) {
                        if (!uiState.isOnline && showBottomBar) {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .statusBarsPadding()
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .testTag("offline_status_banner"),
                                color = colors.accentGold.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.CloudOff,
                                        contentDescription = "Offline Mode",
                                        tint = colors.accentGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Offline Mode — Local Save Active, Cloud Sync Queued",
                                        color = colors.textPrimary,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                            Crossfade(
                                targetState = uiState.currentScreen,
                                animationSpec = tween(durationMillis = 220),
                                label = "screenCrossfade"
                            ) { screen ->
                                when (screen) {
                                    AppScreen.SPLASH -> {
                                        SplashScreen(
                                            onEnterClubhouse = { viewModel.navigateTo(AppScreen.HOME) }
                                        )
                                    }

                                    AppScreen.HOME -> {
                                        HomeScreen(
                                            uiState = uiState,
                                            onStartTournament = { viewModel.startTournament() },
                                            onStartThreeHolePrototype = { viewModel.startThreeHolePrototype() },
                                            onStartPractice = { viewModel.startPracticeRange() },
                                            onNavigate = { nextScreen -> viewModel.navigateTo(nextScreen) },
                                            onClaimDailyChallenge = { id -> viewModel.claimDailyChallenge(id) },
                                            onClaimDailyLoginReward = { viewModel.claimDailyLoginReward() },
                                            onDismissOnboarding = { viewModel.dismissOnboarding() },
                                            onMarkNotificationsRead = { viewModel.markNotificationsRead() },
                                            onRequestAccountSignIn = onRequestAccountSignIn
                                        )
                                    }

                    AppScreen.GAME_MODES -> {
                        GameModesScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.HOME) },
                            onStartQuickPlay = { viewModel.startThreeHolePrototype() },
                            onStartCareerRound = { viewModel.startTournament() },
                            onStartPracticeRange = { viewModel.startPracticeRange() },
                            onNavigate = { nextScreen -> viewModel.navigateTo(nextScreen) }
                        )
                    }

                    AppScreen.COURSE_SELECTION -> {
                        CourseSelectionScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.HOME) },
                            onSelectCourseCup = { cup -> viewModel.startTournamentCup(cup) },
                            onOpenCourseArchitect = { viewModel.navigateTo(AppScreen.COURSE_ARCHITECT) },
                            onPlayCustomCourse = { viewModel.playtestCustomHole() }
                        )
                    }

                    AppScreen.PLAY_HOLE -> {
                        PlayHoleScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.HOME) },
                            onSelectClub = { club -> viewModel.selectClub(club) },
                            onAimAdjust = { angle, power ->
                                viewModel.setAimAngle(angle)
                                viewModel.setAimPower(power)
                            },
                            onAdjustAngle = { deltaDeg -> viewModel.adjustAimAngle(deltaDeg) },
                            onSetPower = { power -> viewModel.setAimPower(power) },
                            onSetSpin = { spin -> viewModel.setCurveSpin(spin) },
                            onStartApexSwing = { viewModel.startSwingSequence() },
                            onLockReleaseSwing = { viewModel.lockAndReleaseSwing() },
                            onMulligan = { viewModel.useMulligan() },
                            onAskAiCaddie = { viewModel.requestAiCaddieInsight() },
                            onOpenCaddieChat = { viewModel.navigateTo(AppScreen.CADDIE_CHAT) },
                            onCycleCamera = { viewModel.cycleCameraMode() },
                            onCycleWeather = { viewModel.cycleWeather() },
                            onToggleRangefinder = { viewModel.toggleRangefinder() },
                            onSetVerticalSpin = { spin -> viewModel.setVerticalSpin(spin) },
                            onTogglePhysicsDebug = { viewModel.togglePhysicsDebug() },
                            onSwingDragStart = { startPos -> viewModel.onSwingDragStart(startPos) },
                            onSwingDragUpdate = { curPos, ballPos, maxPullPx ->
                                viewModel.onSwingDragUpdate(curPos, ballPos, maxPullPx)
                            },
                            onSwingDragRelease = { viewModel.onSwingDragRelease() },
                            onSwingDragCancel = { viewModel.onSwingDragCancel() },
                            onConfirmShot = { viewModel.confirmAndExecuteShot() },
                            onPauseGame = { viewModel.pauseGame() },
                            onResumeGame = { viewModel.resumeGame() },
                            onRestartHole = { viewModel.restartCurrentHole() },
                            onSelectCameraMode = { mode -> viewModel.setCameraMode(mode) }
                        )
                    }

                    AppScreen.CADDIE_CHAT -> {
                        CaddieChatScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.HOME) }
                        )
                    }

                    AppScreen.CUSTOM_BALL_STUDIO -> {
                        CustomBallStudioScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.PRO_SHOP) },
                            onBallCreatedAndEquipped = { ball -> viewModel.addAndEquipCustomBall(ball) }
                        )
                    }

                    AppScreen.HOLE_SUMMARY -> {
                        HoleSummaryScreen(
                            uiState = uiState,
                            onNextHole = { viewModel.proceedToNextHole() },
                            onViewLeaderboard = { viewModel.navigateTo(AppScreen.TOURNAMENT_LEADERBOARD) }
                        )
                    }

                    AppScreen.TOURNAMENT_LEADERBOARD -> {
                        TournamentLeaderboardScreen(
                            uiState = uiState,
                            onBack = {
                                when {
                                    uiState.tournamentCompleted -> viewModel.navigateTo(AppScreen.HOME)
                                    uiState.previousScreen == AppScreen.CUP_SELECT -> viewModel.navigateTo(AppScreen.CUP_SELECT)
                                    uiState.playerScores.isEmpty() -> viewModel.navigateTo(AppScreen.HOME)
                                    else -> viewModel.navigateTo(AppScreen.PLAY_HOLE)
                                }
                            },
                            onRestartTournament = { viewModel.startTournament() }
                        )
                    }

                    AppScreen.PRO_SHOP -> {
                        ProShopScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.HOME) },
                            onUpgradeClub = { clubType -> viewModel.upgradeClub(clubType) },
                            onEquipBall = { ballId -> viewModel.equipBall(ballId) },
                            onBuyBall = { ballId -> viewModel.buyBall(ballId) },
                            onSelectTracer = { tracer -> viewModel.setTracerStyle(tracer) },
                            onUpdateGolfer = { profile -> viewModel.updateGolferProfile(profile) },
                            onOpenCustomBallStudio = { viewModel.navigateTo(AppScreen.CUSTOM_BALL_STUDIO) },
                            onPurchaseAccessory = { id, cost, updater ->
                                viewModel.purchaseAccessoryOrCosmetic(id, cost, updater)
                            },
                            onGrantBillingCoins = { coins -> viewModel.grantBillingCoins(coins) }
                        )
                    }

                    AppScreen.CAREER_STATS -> {
                        CareerStatsScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.HOME) },
                            onNavigate = { nextScreen -> viewModel.navigateTo(nextScreen) },
                            onUpdateGolfer = { profile -> viewModel.updateGolferProfile(profile) },
                            onRequestAccountSignIn = onRequestAccountSignIn
                        )
                    }

                    AppScreen.PRACTICE_RANGE -> {
                        PlayHoleScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.HOME) },
                            onSelectClub = { club -> viewModel.selectClub(club) },
                            onAimAdjust = { angle, power ->
                                viewModel.setAimAngle(angle)
                                viewModel.setAimPower(power)
                            },
                            onAdjustAngle = { deltaDeg -> viewModel.adjustAimAngle(deltaDeg) },
                            onSetPower = { power -> viewModel.setAimPower(power) },
                            onSetSpin = { spin -> viewModel.setCurveSpin(spin) },
                            onStartApexSwing = { viewModel.startSwingSequence() },
                            onLockReleaseSwing = { viewModel.lockAndReleaseSwing() },
                            onMulligan = { viewModel.useMulligan() },
                            onAskAiCaddie = { viewModel.requestAiCaddieInsight() },
                            onOpenCaddieChat = { viewModel.navigateTo(AppScreen.CADDIE_CHAT) },
                            onCycleCamera = { viewModel.cycleCameraMode() },
                            onCycleWeather = { viewModel.cycleWeather() },
                            onToggleRangefinder = { viewModel.toggleRangefinder() },
                            onSetVerticalSpin = { spin -> viewModel.setVerticalSpin(spin) },
                            onTogglePhysicsDebug = { viewModel.togglePhysicsDebug() },
                            onSwingDragStart = { startPos -> viewModel.onSwingDragStart(startPos) },
                            onSwingDragUpdate = { curPos, ballPos, maxPullPx ->
                                viewModel.onSwingDragUpdate(curPos, ballPos, maxPullPx)
                            },
                            onSwingDragRelease = { viewModel.onSwingDragRelease() },
                            onSwingDragCancel = { viewModel.onSwingDragCancel() }
                        )
                    }

                    AppScreen.DAILY_CHALLENGES -> {
                        DailyChallengesScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.HOME) },
                            onClaimChallenge = { id -> viewModel.claimDailyChallenge(id) },
                            onSpinWheelResult = { prize -> viewModel.spinLuckyWheel(prize) },
                            onPlayChallenge = { challenge -> viewModel.startSpecialChallenge(challenge) },
                            onNavigate = { nextScreen -> viewModel.navigateTo(nextScreen) },
                            onClaimDailyLoginReward = { viewModel.claimDailyLoginReward() }
                        )
                    }

                    AppScreen.TARGET_SHOOTOUT -> {
                        TargetShootoutScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.HOME) },
                            onSelectClub = { club -> viewModel.selectClub(club) },
                            onAimAdjust = { angle, power ->
                                viewModel.setAimAngle(angle)
                                viewModel.setAimPower(power)
                            },
                            onAdjustAngle = { deltaDeg -> viewModel.adjustAimAngle(deltaDeg) },
                            onSetPower = { power -> viewModel.setAimPower(power) },
                            onSetSpin = { spin -> viewModel.setCurveSpin(spin) },
                            onStartApexSwing = { viewModel.startSwingSequence() },
                            onLockReleaseSwing = { viewModel.lockAndReleaseSwing() },
                            onMulligan = { viewModel.useMulligan() },
                            onPlayAgain = { viewModel.resetShootoutRound() },
                            onSwingDragStart = { startPos -> viewModel.onSwingDragStart(startPos) },
                            onSwingDragUpdate = { curPos, ballPos, maxPullPx ->
                                viewModel.onSwingDragUpdate(curPos, ballPos, maxPullPx)
                            },
                            onSwingDragRelease = { viewModel.onSwingDragRelease() },
                            onSwingDragCancel = { viewModel.onSwingDragCancel() }
                        )
                    }

                    AppScreen.TOUR_PASS -> {
                        TourPassScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.HOME) },
                            onClaimReward = { tierLevel, isVip -> viewModel.claimTourPassReward(tierLevel, isVip) },
                            onUnlockVip = { viewModel.unlockTourPassVip() }
                        )
                    }

                    AppScreen.CUP_SELECT -> {
                        CupSelectScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.HOME) },
                            onSelectCup = { cup -> viewModel.startTournamentCup(cup) },
                            onOpenLeaderboard = { viewModel.navigateTo(AppScreen.TOURNAMENT_LEADERBOARD) }
                        )
                    }

                    AppScreen.PLAY_SETTINGS -> {
                        PlaySettingsScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.HOME) },
                            onUpdateSettings = { newSettings -> viewModel.updatePlaySettings(newSettings) },
                            onResetProgress = { viewModel.resetAllGameProgress() },
                            onSyncToCloud = { viewModel.syncCareerToCloud() },
                            onSignOut = onSignOut,
                            onDeleteAccount = {
                                viewModel.deleteAccountAndCloudData(
                                    deleteAuthAccount = onDeleteAuthAccount
                                )
                            },
                            onRequestAccountSignIn = onRequestAccountSignIn
                        )
                    }

                    AppScreen.MATCH_PLAY_DUEL -> {
                        MatchPlayDuelScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.HOME) },
                            onSelectRival = { rival -> viewModel.selectMatchPlayRival(rival) },
                            onStartDuel = { rival -> viewModel.startMatchPlayDuel(rival) },
                            onProceedNextHole = { viewModel.proceedToNextMatchPlayHole() },
                            onExitMatch = { viewModel.exitMatchPlay() }
                        )
                    }

                    AppScreen.REPLAY_THEATER -> {
                        ReplayTheaterScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.HOME) },
                            onSelectReplay = { replay -> viewModel.playReplayShot(replay) },
                            onSaveCurrentShot = { viewModel.saveCurrentShotToReplay() }
                        )
                    }

                    AppScreen.COURSE_ARCHITECT -> {
                        CourseArchitectScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.COURSE_SELECTION) },
                            onUpdateBlueprint = { blueprint -> viewModel.updateCustomHoleBlueprint(blueprint) },
                            onPlaytestHole = { viewModel.playtestCustomHole() }
                        )
                    }

                    AppScreen.GOLFER_LOCKER_ROOM -> {
                        GolferCustomizationScreen(
                            uiState = uiState,
                            onBack = { viewModel.navigateTo(AppScreen.CAREER_STATS) },
                            onUpdateGolfer = { profile -> viewModel.updateGolferProfile(profile) }
                        )
                    }

                    AppScreen.TECHNICAL_BLUEPRINT -> {
                        TechnicalBlueprintScreen(
                            onBack = { viewModel.navigateTo(AppScreen.HOME) }
                        )
                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
