package com.example.game.qa

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import com.example.GolfAppRoot
import com.example.game.model.GraphicsQualityTier
import com.example.game.model.PlayConsoleSettings
import com.example.game.progression.LocalProgressStorage
import com.example.game.ui.screens.SplashScreen
import com.example.game.viewmodel.AppScreen
import com.example.game.viewmodel.GolfGameViewModel
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ZenithGolfEndToEndRobolectricQaTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val testDispatcher = UnconfinedTestDispatcher()
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
    @Config(sdk = [36], qualifiers = "w320dp-h568dp-mdpi")
    fun `lowEndCompactPhone - splash home dashboard bottom nav and settings fit without overlap`() {
        val vm = GolfGameViewModel()
        vm.updatePlaySettings(
            PlayConsoleSettings(
                graphicsTier = GraphicsQualityTier.LOW_BATTERY,
                isHighFpsMode = false
            )
        )

        var splashEntered = false
        composeRule.setContent {
            var entered by remember { mutableStateOf(false) }
            splashEntered = entered
            MyApplicationTheme {
                if (!entered) {
                    SplashScreen(
                        onEnterClubhouse = {
                            entered = true
                            splashEntered = true
                        },
                        autoAdvanceMs = 60_000L
                    )
                } else {
                    GolfAppRoot(viewModel = vm)
                }
            }
        }
        composeRule.waitForIdle()

        // 1. Application Startup / Splash branding
        composeRule.onNodeWithTag("splash_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("splash_enter_btn").performClick()
        composeRule.waitForIdle()
        assertTrue(splashEntered)

        // 2. Home Dashboard displays key HUD elements & bottom navigation
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("home_player_profile_header").assertIsDisplayed()
        composeRule.onNodeWithTag("home_coins_pill").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_top_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("zenith_bottom_nav_bar").assertIsDisplayed()

        // Verify top-header profile & settings button do NOT overlap even at 320dp width
        val headerBounds = composeRule.onNodeWithTag("home_player_profile_header").getUnclippedBoundsInRoot()
        val settingsBounds = composeRule.onNodeWithTag("settings_top_btn").getUnclippedBoundsInRoot()
        assertTrue(
            "Profile header right (${headerBounds.right}) must stay left of Settings btn (${settingsBounds.left}) on 320dp screen",
            headerBounds.right <= settingsBounds.left + 2.dp
        )

        // Verify all 5 bottom nav tabs navigate cleanly on compact device
        composeRule.onNodeWithTag("bottom_nav_play").performClick()
        composeRule.waitForIdle()
        assertEquals(AppScreen.GAME_MODES, vm.uiState.value.currentScreen)
        composeRule.onNodeWithTag("game_modes_screen").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom_nav_events").performClick()
        composeRule.waitForIdle()
        assertEquals(AppScreen.CUP_SELECT, vm.uiState.value.currentScreen)
        composeRule.onNodeWithTag("cup_select_screen").assertIsDisplayed()

        composeRule.onNodeWithTag("bottom_nav_rewards").performClick()
        composeRule.waitForIdle()
        assertEquals(AppScreen.DAILY_CHALLENGES, vm.uiState.value.currentScreen)

        composeRule.onNodeWithTag("bottom_nav_profile").performClick()
        composeRule.waitForIdle()
        assertEquals(AppScreen.CAREER_STATS, vm.uiState.value.currentScreen)

        composeRule.onNodeWithTag("bottom_nav_home").performClick()
        composeRule.waitForIdle()
        assertEquals(AppScreen.HOME, vm.uiState.value.currentScreen)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w411dp-h915dp-xxhdpi")
    fun `midRangePhone - courses tournaments leaderboard equipment and settings flow`() {
        val vm = GolfGameViewModel()
        composeRule.setContent {
            MyApplicationTheme {
                GolfAppRoot(viewModel = vm)
            }
        }
        composeRule.waitForIdle()

        // Navigate Home -> Course Selection
        composeRule.onNodeWithTag("change_course_chip").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("course_selection_screen").assertIsDisplayed()

        // Navigate Events -> Tournaments -> Leaderboard -> Back safely returns to CupSelect
        composeRule.onNodeWithTag("course_selection_back_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("bottom_nav_events").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tournaments_open_leaderboard_card").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("leaderboard_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("tab_global_cloud_leaderboard").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("leaderboard_back_btn").performClick()
        composeRule.waitForIdle()
        assertEquals(AppScreen.CUP_SELECT, vm.uiState.value.currentScreen)

        // Open Equipment / Pro Shop and upgrade Driver
        vm.navigateTo(AppScreen.PRO_SHOP)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("pro_shop_screen").assertIsDisplayed()
        val coinsBeforeUpgrade = vm.uiState.value.career.coins
        vm.upgradeClub(com.example.game.model.ClubType.DRIVER)
        composeRule.waitForIdle()
        assertTrue(vm.uiState.value.career.coins < coinsBeforeUpgrade)

        // Open Settings, verify BuildConfig release identity, Data Safety dialog, and Account Deletion dialog
        assertEquals("com.aistudio.zenithgolf.tzmpq", com.example.BuildConfig.APPLICATION_ID)
        assertEquals(5, com.example.BuildConfig.VERSION_CODE)
        assertEquals("2.0", com.example.BuildConfig.VERSION_NAME)

        vm.navigateTo(AppScreen.PLAY_SETTINGS)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("play_settings_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("settings_cat_6").performClick() // Privacy
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("privacy_policy_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Google Play Data Safety & Privacy").assertIsDisplayed()
        composeRule.onNodeWithText("CLOSE").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("settings_cat_5").performClick() // Account
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("delete_account_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("confirm_delete_account_btn").assertIsDisplayed()
        composeRule.onNodeWithText("CANCEL").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("play_settings_back_btn").performClick()
        composeRule.waitForIdle()
        assertEquals(AppScreen.HOME, vm.uiState.value.currentScreen)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w1024dp-h768dp-land-xhdpi")
    fun `highEndExpandedTabletLandscape - play hole hud nonOverlapping club swing and pause flow`() {
        val vm = GolfGameViewModel()
        vm.updatePlaySettings(
            PlayConsoleSettings(
                graphicsTier = GraphicsQualityTier.FLAGSHIP_ULTRA,
                isHighFpsMode = true
            )
        )
        vm.startTournament()

        composeRule.setContent {
            MyApplicationTheme {
                GolfAppRoot(viewModel = vm)
            }
        }
        composeRule.waitForIdle()

        // Verify full-screen landscape Play Hole HUD
        composeRule.onNodeWithTag("play_hole_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("landscape_top_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom_left_club_panel").assertIsDisplayed()
        composeRule.onNodeWithTag("bottom_center_distance_badge").assertIsDisplayed()
        composeRule.onNodeWithTag("drag_release_power_hud").assertIsDisplayed()

        // Assert ZERO horizontal overlap between Left Hole Badge, Center Stroke Badge, and Right Wind Badge
        val leftBadge = composeRule.onNodeWithTag("top_bar_hole_par_badge").getUnclippedBoundsInRoot()
        val centerBadge = composeRule.onNodeWithTag("top_bar_stroke_badge").getUnclippedBoundsInRoot()
        val rightBadge = composeRule.onNodeWithTag("top_bar_wind_badge").getUnclippedBoundsInRoot()
        assertTrue(
            "Left hole badge (${leftBadge.right}) must not overlap center stroke badge (${centerBadge.left})",
            leftBadge.right <= centerBadge.left
        )
        assertTrue(
            "Center stroke badge (${centerBadge.right}) must not overlap right wind badge (${rightBadge.left})",
            centerBadge.right <= rightBadge.left
        )

        // Club selection switch & Camera cycling
        composeRule.onNodeWithText("Iron").performClick()
        composeRule.waitForIdle()
        assertEquals(com.example.game.model.ClubType.IRON_7, vm.uiState.value.selectedClub)

        composeRule.onNodeWithTag("cycle_camera_button").performClick()
        composeRule.waitForIdle()

        // Pause -> Resume -> Restart Hole
        composeRule.onNodeWithTag("pause_game_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("pause_menu_overlay").assertIsDisplayed()
        composeRule.onNodeWithTag("resume_game_button").performClick()
        composeRule.waitForIdle()
        assertTrue(!vm.uiState.value.isGamePaused)

        // Confirm shot launches ball
        composeRule.onNodeWithTag("confirm_shot_button").performClick()
        composeRule.waitForIdle()
        assertEquals(1, vm.uiState.value.currentStrokeCount)
    }

    @Test
    @Config(sdk = [36], qualifiers = "w1024dp-h768dp-land-xhdpi")
    fun `zenithRedesign - signInGuestFlow tabletNavigationRail themeSwitch offlineBanner and rewards`() {
        val vm = GolfGameViewModel()
        var enteredGuestMode = false

        composeRule.setContent {
            var guestSession by remember { mutableStateOf(false) }
            MyApplicationTheme {
                if (!guestSession) {
                    com.example.game.ui.screens.ZenithSignInScreen(
                        onContinueAsGuest = {
                            guestSession = true
                            enteredGuestMode = true
                        }
                    )
                } else {
                    GolfAppRoot(
                        isGuestSession = true,
                        viewModel = vm
                    )
                }
            }
        }
        composeRule.waitForIdle()

        // 1. Sign-In screen: verify Email & Create Account toggle + Privacy link + Guest Play
        composeRule.onNodeWithTag("sign_in_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("toggle_create_account_btn").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("email_auth_card").assertIsDisplayed()
        composeRule.onNodeWithTag("email_auth_username_input").assertIsDisplayed()

        composeRule.onNodeWithTag("guest_play_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertTrue(enteredGuestMode)

        // 2. Tablet Home displays NavigationRail (width >= 840dp) instead of BottomNavBar
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("zenith_navigation_rail").assertIsDisplayed()

        // 3. Offline banner toggles cleanly when connectivity drops
        vm.setOnlineStatus(false)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("offline_status_banner").assertIsDisplayed()
        vm.setOnlineStatus(true)
        composeRule.waitForIdle()

        // 4. Daily Challenges screen allows claiming Daily Login Streak reward
        vm.navigateTo(AppScreen.DAILY_CHALLENGES)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("daily_challenges_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("rewards_daily_login_streak_card").assertIsDisplayed()
        val coinsBeforeStreak = vm.uiState.value.career.coins
        composeRule.onNodeWithTag("rewards_claim_daily_streak_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertTrue(vm.uiState.value.career.coins > coinsBeforeStreak)

        // 5. ProShop Bundles tab & purchase confirmation dialog
        vm.navigateTo(AppScreen.PRO_SHOP)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("equipment_tab_bundles").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("buy_masters_kit_bundle_btn").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("shop_confirm_purchase_btn").performClick()
        composeRule.waitForIdle()

        // 6. Settings theme mode switching (Dark -> Light)
        vm.navigateTo(AppScreen.PLAY_SETTINGS)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("theme_mode_light").performScrollTo().performClick()
        composeRule.waitForIdle()
        assertEquals(com.example.game.model.ZenithThemeMode.LIGHT, vm.uiState.value.playSettings.themeMode)
    }
}
