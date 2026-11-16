package com.example.game.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.outlined.CardGiftcard
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsGolf
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.viewmodel.AppScreen
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.ZenithTheme

data class ZenithNavTab(
    val label: String,
    val route: AppScreen,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

val ZENITH_BOTTOM_TABS = listOf(
    ZenithNavTab(
        label = "Home",
        route = AppScreen.HOME,
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "bottom_nav_home"
    ),
    ZenithNavTab(
        label = "Play",
        route = AppScreen.GAME_MODES,
        selectedIcon = Icons.Filled.SportsGolf,
        unselectedIcon = Icons.Outlined.SportsGolf,
        testTag = "bottom_nav_play"
    ),
    ZenithNavTab(
        label = "Events",
        route = AppScreen.CUP_SELECT,
        selectedIcon = Icons.Filled.EmojiEvents,
        unselectedIcon = Icons.Outlined.EmojiEvents,
        testTag = "bottom_nav_events"
    ),
    ZenithNavTab(
        label = "Rewards",
        route = AppScreen.DAILY_CHALLENGES,
        selectedIcon = Icons.Filled.CardGiftcard,
        unselectedIcon = Icons.Outlined.CardGiftcard,
        testTag = "bottom_nav_rewards"
    ),
    ZenithNavTab(
        label = "Profile",
        route = AppScreen.CAREER_STATS,
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "bottom_nav_profile"
    )
)

fun isBottomBarVisibleForScreen(screen: AppScreen): Boolean {
    return when (screen) {
        AppScreen.HOME,
        AppScreen.GAME_MODES,
        AppScreen.COURSE_SELECTION,
        AppScreen.CUP_SELECT,
        AppScreen.TOURNAMENT_LEADERBOARD,
        AppScreen.DAILY_CHALLENGES,
        AppScreen.CAREER_STATS,
        AppScreen.PRO_SHOP,
        AppScreen.GOLFER_LOCKER_ROOM,
        AppScreen.TOUR_PASS,
        AppScreen.PLAY_SETTINGS -> true
        else -> false
    }
}

fun activeBottomTabForScreen(screen: AppScreen): AppScreen {
    return when (screen) {
        AppScreen.HOME -> AppScreen.HOME
        AppScreen.GAME_MODES, AppScreen.COURSE_SELECTION -> AppScreen.GAME_MODES
        AppScreen.CUP_SELECT, AppScreen.TOURNAMENT_LEADERBOARD -> AppScreen.CUP_SELECT
        AppScreen.DAILY_CHALLENGES, AppScreen.TOUR_PASS -> AppScreen.DAILY_CHALLENGES
        AppScreen.CAREER_STATS, AppScreen.PRO_SHOP, AppScreen.GOLFER_LOCKER_ROOM, AppScreen.PLAY_SETTINGS -> AppScreen.CAREER_STATS
        else -> AppScreen.HOME
    }
}

@Composable
fun ZenithBottomNavBar(
    currentScreen: AppScreen,
    unclaimedRewardsCount: Int = 0,
    onSelectTab: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTab = activeBottomTabForScreen(currentScreen)
    val colors = ZenithTheme.colors
    val indicatorColor = if (colors.isDark) colors.deepForestGreen else colors.primaryGreen.copy(alpha = 0.14f)
    val selectedColor = if (colors.isDark) colors.accentGold else colors.primaryGreen

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = colors.border)
            .testTag("zenith_bottom_nav_bar"),
        color = colors.surface,
        tonalElevation = 8.dp
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            containerColor = Color.Transparent,
            contentColor = colors.textPrimary,
            tonalElevation = 0.dp
        ) {
            ZENITH_BOTTOM_TABS.forEach { tab ->
                val isSelected = activeTab == tab.route
                NavigationBarItem(
                    selected = isSelected,
                    onClick = { onSelectTab(tab.route) },
                    modifier = Modifier.testTag(tab.testTag),
                    icon = {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(24.dp)
                            )
                            if (tab.route == AppScreen.DAILY_CHALLENGES && unclaimedRewardsCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = 6.dp, y = (-4).dp)
                                        .size(16.dp)
                                        .background(colors.accentGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$unclaimedRewardsCount",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colors.onGold
                                    )
                                }
                            }
                        }
                    },
                    label = {
                        Text(
                            text = tab.label.uppercase(),
                            fontFamily = OutfitFontFamily,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        indicatorColor = indicatorColor,
                        unselectedIconColor = colors.textMuted,
                        unselectedTextColor = colors.textMuted
                    )
                )
            }
        }
    }
}

@Composable
fun ZenithNavigationRail(
    currentScreen: AppScreen,
    unclaimedRewardsCount: Int = 0,
    onSelectTab: (AppScreen) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeTab = activeBottomTabForScreen(currentScreen)
    val colors = ZenithTheme.colors
    val indicatorColor = if (colors.isDark) colors.deepForestGreen else colors.primaryGreen.copy(alpha = 0.14f)
    val selectedColor = if (colors.isDark) colors.accentGold else colors.primaryGreen

    Surface(
        modifier = modifier
            .fillMaxHeight()
            .border(width = 1.dp, color = colors.border)
            .testTag("zenith_navigation_rail"),
        color = colors.surface,
        tonalElevation = 8.dp
    ) {
        NavigationRail(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(vertical = 12.dp),
            containerColor = Color.Transparent,
            contentColor = colors.textPrimary
        ) {
            Icon(
                imageVector = Icons.Filled.SportsGolf,
                contentDescription = "Zenith Golf",
                tint = colors.accentGold,
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            ZENITH_BOTTOM_TABS.forEach { tab ->
                val isSelected = activeTab == tab.route
                NavigationRailItem(
                    selected = isSelected,
                    onClick = { onSelectTab(tab.route) },
                    modifier = Modifier.testTag(tab.testTag),
                    icon = {
                        Box(contentAlignment = Alignment.TopEnd) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(24.dp)
                            )
                            if (tab.route == AppScreen.DAILY_CHALLENGES && unclaimedRewardsCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .offset(x = 6.dp, y = (-4).dp)
                                        .size(16.dp)
                                        .background(colors.accentGold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "$unclaimedRewardsCount",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = colors.onGold
                                    )
                                }
                            }
                        }
                    },
                    label = {
                        Text(
                            text = tab.label.uppercase(),
                            fontFamily = OutfitFontFamily,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Black else FontWeight.SemiBold,
                            letterSpacing = 0.5.sp,
                            maxLines = 1
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = selectedColor,
                        selectedTextColor = selectedColor,
                        indicatorColor = indicatorColor,
                        unselectedIconColor = colors.textMuted,
                        unselectedTextColor = colors.textMuted
                    )
                )
            }
        }
    }
}
