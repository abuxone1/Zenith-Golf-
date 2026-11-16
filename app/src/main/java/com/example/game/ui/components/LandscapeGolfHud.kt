package com.example.game.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material.icons.filled.Videocam
import com.example.game.model.CameraViewMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.model.ClubType
import com.example.game.model.SurfaceType
import com.example.game.viewmodel.GameUiState
import com.example.game.viewmodel.SwingMeterPhase
import com.example.ui.theme.ZenithCyan
import com.example.ui.theme.ZenithGold
import com.example.ui.theme.ZenithNeonGreen
import com.example.ui.theme.ZenithSurfaceBorder
import com.example.ui.theme.ZenithSurfaceElevated
import com.example.ui.theme.ZenithTextMuted
import com.example.ui.theme.ZenithTextPrimary
import com.example.ui.theme.ZenithTextSecondary
import kotlin.math.abs

/**
 * Primary 4 clubs requested for quick selection in the bottom-left HUD:
 * Driver, Iron, Chip, Putter.
 */
data class QuickClubOption(
    val label: String,
    val clubType: ClubType
)

val PRIMARY_LANDSCAPE_CLUBS = listOf(
    QuickClubOption("Driver", ClubType.DRIVER),
    QuickClubOption("Iron", ClubType.IRON_7),
    QuickClubOption("Chip", ClubType.PITCHING_WEDGE),
    QuickClubOption("Putter", ClubType.PUTTER)
)

/**
 * Semi-transparent Top Bar for Full-Screen Landscape Layout:
 * - Left: Back button (>= 48.dp) + Hole number and Par
 * - Center: Stroke count
 * - Right: Wind direction and speed + compact camera/telemetry actions (>= 48.dp)
 */
@Composable
fun LandscapeTopBar(
    uiState: GameUiState,
    onBack: () -> Unit,
    onOpenStats: () -> Unit,
    onCycleCamera: () -> Unit,
    onOpenCaddieChat: () -> Unit,
    onTogglePhysicsDebug: () -> Unit,
    onPauseGame: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val hole = uiState.currentHole
    val wind = uiState.wind
    val windDirCardinal = remember(wind.directionDeg) {
        val normalized = ((wind.directionDeg % 360f) + 360f) % 360f
        when {
            normalized < 22.5f || normalized >= 337.5f -> "N"
            normalized < 67.5f -> "NE"
            normalized < 112.5f -> "E"
            normalized < 157.5f -> "SE"
            normalized < 202.5f -> "S"
            normalized < 247.5f -> "SW"
            normalized < 292.5f -> "W"
            else -> "NW"
        }
    }

    Surface(
        color = Color(0x9E06110B),
        border = BorderStroke(1.dp, ZenithSurfaceBorder.copy(alpha = 0.55f)),
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("landscape_top_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT: Back Button (>= 48.dp) + Pause Button + Hole Number & Par
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xCC0D1F15),
                    border = BorderStroke(1.dp, ZenithSurfaceBorder),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onBack() }
                        .testTag("hole_back_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Exit Hole",
                            tint = ZenithTextPrimary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                Surface(
                    shape = CircleShape,
                    color = Color(0xCC0D1F15),
                    border = BorderStroke(1.dp, ZenithGold.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onPauseGame() }
                        .testTag("pause_game_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause Game",
                            tint = ZenithGold,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                val elevText = when {
                    hole.elevationDiffYds > 0.5f -> "+${hole.elevationDiffYds.toInt()}Y UPHILL"
                    hole.elevationDiffYds < -0.5f -> "${hole.elevationDiffYds.toInt()}Y DOWNHILL"
                    else -> "LEVEL ELEV"
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xB80D1F15),
                    border = BorderStroke(1.dp, ZenithNeonGreen.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .heightIn(min = 48.dp)
                        .clickable { onOpenStats() }
                        .testTag("top_bar_hole_par_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Text(
                                text = "HOLE ${hole.holeNumber} • PAR ${hole.par} • ${hole.yardage}Y",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = ZenithNeonGreen,
                                letterSpacing = 0.4.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${hole.holeTitle} • $elevText",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ZenithTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.QueryStats,
                            contentDescription = "Hole Statistics",
                            tint = ZenithCyan,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            // CENTER: Stroke Count & Total Round Score vs Par
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xCC0A1910),
                border = BorderStroke(1.dp, ZenithGold.copy(alpha = 0.65f)),
                modifier = Modifier
                    .heightIn(min = 48.dp)
                    .testTag("top_bar_stroke_badge")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SportsGolf,
                        contentDescription = null,
                        tint = ZenithGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STROKE ${uiState.currentStrokeCount} • TOTAL ${uiState.totalRoundStrokes}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            letterSpacing = 0.4.sp,
                            maxLines = 1
                        )
                        Text(
                            text = "PAR ${hole.par} • ROUND (${uiState.roundScoreToParText})",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZenithGold,
                            maxLines = 1
                        )
                    }
                }
            }

            // RIGHT: Wind Direction & Speed + Compact Utility Buttons (>= 48.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xCC0D1F15),
                    border = BorderStroke(1.dp, ZenithCyan.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("top_bar_wind_badge")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(ZenithCyan.copy(alpha = 0.16f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = "Wind Direction $windDirCardinal",
                                tint = ZenithCyan,
                                modifier = Modifier
                                    .size(16.dp)
                                    .rotate(wind.directionDeg)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Air,
                                    contentDescription = null,
                                    tint = ZenithCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "${wind.speedMph.toInt()} MPH $windDirCardinal",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    maxLines = 1
                                )
                            }
                            Text(
                                text = "WIND ${wind.directionDeg.toInt()}°",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = ZenithCyan,
                                maxLines = 1
                            )
                        }
                    }
                }

                // Camera Toggle Button (>= 48.dp)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xCC0D1F15),
                    border = BorderStroke(1.dp, ZenithNeonGreen.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clickable { onCycleCamera() }
                        .testTag("cycle_camera_button")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Cycle Camera View",
                            tint = ZenithNeonGreen,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = uiState.cameraMode.shortTag,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = ZenithNeonGreen,
                            maxLines = 1
                        )
                    }
                }

                // AI Caddie Advice Button (>= 48.dp)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xCC0D1F15),
                    border = BorderStroke(1.dp, ZenithSurfaceBorder),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onOpenCaddieChat() }
                        .testTag("ai_caddie_banner_button")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Ask AI Caddie",
                            tint = ZenithCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "CADDIE",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = ZenithCyan
                        )
                    }
                }

                // Physics Debug Telemetry Toggle (>= 48.dp)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (uiState.isPhysicsDebugEnabled) ZenithGold.copy(alpha = 0.25f) else Color(0xCC0D1F15),
                    border = BorderStroke(1.dp, if (uiState.isPhysicsDebugEnabled) ZenithGold else ZenithSurfaceBorder),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onTogglePhysicsDebug() }
                        .testTag("toggle_physics_debug_button")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsGolf,
                            contentDescription = "Toggle Physics Telemetry",
                            tint = if (uiState.isPhysicsDebugEnabled) ZenithGold else ZenithTextSecondary,
                            modifier = Modifier.size(17.dp)
                        )
                        Text(
                            text = "DBG",
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Black,
                            color = if (uiState.isPhysicsDebugEnabled) ZenithGold else ZenithTextSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * Bottom-Left Club Selector & Aim Trim Panel:
 * - Features compact buttons for Driver, Iron, Chip, Putter plus a compact dropdown for all clubs.
 * - Includes Left / Right aim buttons (>= 48.dp).
 * - Anchored strictly at BottomStart away from screen center and ball view.
 */
@Composable
fun BottomLeftClubSelector(
    selectedClub: ClubType,
    aimAngleDeg: Float,
    disabled: Boolean,
    onSelectClub: (ClubType) -> Unit,
    onAdjustAngle: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    var dropdownExpanded by remember { mutableStateOf(false) }

    val activeQuickLabel = remember(selectedClub) {
        PRIMARY_LANDSCAPE_CLUBS.firstOrNull { it.clubType == selectedClub }?.label
            ?: selectedClub.displayName
    }

    Surface(
        color = Color(0xB807130C),
        border = BorderStroke(1.dp, ZenithSurfaceBorder.copy(alpha = 0.7f)),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
            .widthIn(max = 310.dp)
            .testTag("bottom_left_club_panel")
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Compact Club Dropdown Trigger + Aim Left / Right buttons (all >= 48.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ZenithSurfaceElevated,
                        border = BorderStroke(1.dp, ZenithNeonGreen.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clickable(enabled = !disabled) { dropdownExpanded = true }
                            .testTag("club_dropdown_trigger")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "CLUB: ${activeQuickLabel.uppercase()}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ZenithNeonGreen
                                )
                                Text(
                                    text = "${selectedClub.baseDistanceYds.toInt()} YDS MAX • LAUNCH ${selectedClub.launchAngleDeg.toInt()}°",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ZenithTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.ExpandMore,
                                contentDescription = "Select Club Dropdown",
                                tint = ZenithNeonGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                        modifier = Modifier.background(Color(0xFF0D2115))
                    ) {
                        ClubType.values().forEach { clubItem ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = "${clubItem.displayName} • ${clubItem.categoryName}",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                color = if (clubItem == selectedClub) ZenithNeonGreen else Color.White
                                            )
                                            Text(
                                                text = "Range ${clubItem.expectedRangeText} • Launch ${clubItem.launchAngleDeg.toInt()}° • Spin ${clubItem.baseBackspinRpm} RPM",
                                                fontSize = 10.sp,
                                                color = ZenithTextSecondary
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "${clubItem.baseDistanceYds.toInt()}y",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Black,
                                            color = ZenithCyan
                                        )
                                    }
                                },
                                onClick = {
                                    onSelectClub(clubItem)
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Aim Left Button (>= 48.dp x 48.dp)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ZenithSurfaceElevated,
                    border = BorderStroke(1.dp, ZenithSurfaceBorder),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(enabled = !disabled) { onAdjustAngle(-3f) }
                        .testTag("aim_left_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Aim Left",
                            tint = ZenithTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Aim Right Button (>= 48.dp x 48.dp)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ZenithSurfaceElevated,
                    border = BorderStroke(1.dp, ZenithSurfaceBorder),
                    modifier = Modifier
                        .size(48.dp)
                        .clickable(enabled = !disabled) { onAdjustAngle(3f) }
                        .testTag("aim_right_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Aim Right",
                            tint = ZenithTextPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // Bottom Row: 4 Compact Quick-Select Buttons (Driver, Iron, Chip, Putter), each >= 48.dp tall
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PRIMARY_LANDSCAPE_CLUBS.forEach { option ->
                    val isSelected = selectedClub == option.clubType
                    val bgColor = if (isSelected) ZenithNeonGreen else ZenithSurfaceElevated
                    val textColor = if (isSelected) Color(0xFF003314) else ZenithTextPrimary
                    val borderColor = if (isSelected) ZenithNeonGreen else ZenithSurfaceBorder

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = bgColor,
                        border = BorderStroke(1.dp, borderColor),
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 48.dp)
                            .clickable(enabled = !disabled) { onSelectClub(option.clubType) }
                            .testTag("club_btn_${option.clubType.shortName.lowercase()}")
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = option.label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = textColor,
                                maxLines = 1
                            )
                            Text(
                                text = "${option.clubType.baseDistanceYds.toInt()}y",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF003314).copy(alpha = 0.85f) else ZenithTextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bottom-Center Distance to the Hole Badge:
 * - Compact, semi-transparent pill at the very bottom-center of the screen.
 * - Shows distance to hole + current surface lie + optional green slope break hint.
 */
@Composable
fun BottomCenterDistanceBadge(
    uiState: GameUiState,
    modifier: Modifier = Modifier
) {
    val distYds = uiState.currentDistanceToPin.toInt()
    val isPutting = uiState.selectedClub.isPutter || uiState.ballState.lie == SurfaceType.GREEN
    val distanceLabel = if (isPutting && distYds < 45) {
        "${(uiState.currentDistanceToPin * 3f).toInt()} FT TO HOLE (${distYds} YDS)"
    } else {
        "$distYds YDS TO HOLE"
    }

    Surface(
        color = Color(0xBF06110B),
        border = BorderStroke(1.dp, ZenithCyan.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .heightIn(min = 48.dp)
            .testTag("bottom_center_distance_badge")
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Flag,
                contentDescription = null,
                tint = ZenithNeonGreen,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = distanceLabel,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 0.6.sp
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "LIE: ${uiState.ballState.lie.displayName.uppercase()}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ZenithCyan
                    )
                    if (isPutting && uiState.playSettings.isSlopeBreakRadarVisible) {
                        val slopeX = uiState.currentHole.greenSlope.x
                        val breakText = when {
                            slopeX > 0.15f -> "• BREAKS R +${(slopeX * 10).toInt()}°"
                            slopeX < -0.15f -> "• BREAKS L ${(slopeX * 10).toInt()}°"
                            else -> "• FLAT PUTT"
                        }
                        Text(
                            text = breakText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = ZenithGold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Right-Side Drag-and-Release Power Bar HUD:
 * - Replaces the 3-tap swing meter with a live pullback power bar.
 * - Shows drag power (0%..100%) capped at the selected club's max distance (Driver longest, Putter shortest).
 * - Indicates Dead Zone / Cancel state when dragging back to the ball.
 * - Positioned strictly on the right edge away from the center ball view.
 */
@Composable
fun DragReleasePowerBarHud(
    uiState: GameUiState,
    onCancelSwing: () -> Unit,
    onMulligan: () -> Unit,
    onConfirmShot: () -> Unit = {},
    onStartApexTiming: () -> Unit = {},
    onSetPower: (Float) -> Unit = {},
    onSetCurveSpin: (Float) -> Unit = {},
    onSetVerticalSpin: (Float) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isDragging = uiState.isDraggingSwing
    val inCancelZone = isDragging && uiState.isSwingInCancelZone
    val activePowerRatio = if (inCancelZone) 0f else uiState.aimPowerRatio.coerceIn(0f, 1.0f)
    val powerPct = (activePowerRatio * 100).toInt()
    val clubMaxYds = uiState.selectedClub.maxDistanceYds.toInt()
    val targetYds = uiState.currentPullbackTargetYds.toInt()

    val (statusTitle, statusColor) = when {
        uiState.isBallInFlight -> Pair("IN FLIGHT", ZenithCyan)
        uiState.isClubStrikeAnimating -> Pair("STRIKE!", ZenithGold)
        uiState.isApexTimingActive -> Pair("LOCK TIMING!", ZenithGold)
        inCancelZone -> Pair("CANCEL ZONE", Color(0xFFFF5252))
        isDragging -> Pair("RELEASE TO HIT", ZenithNeonGreen)
        else -> Pair("SWING READY", ZenithCyan)
    }

    Surface(
        color = Color(0xBF07130C),
        border = BorderStroke(
            width = 1.5.dp,
            color = when {
                inCancelZone -> Color(0xFFFF5252)
                isDragging || uiState.isApexTimingActive -> ZenithNeonGreen
                else -> ZenithSurfaceBorder
            }
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .width(124.dp)
            .testTag("drag_release_power_hud")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp)
        ) {
            // Status Pill
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = ZenithSurfaceElevated,
                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.65f))
            ) {
                Text(
                    text = statusTitle,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = statusColor,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                )
            }

            // Live Power & Club-Capped Yardage Readout
            Text(
                text = if (inCancelZone) "CANCEL" else "$powerPct% PWR",
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                color = if (inCancelZone) Color(0xFFFF5252) else Color.White
            )
            Text(
                text = if (inCancelZone) "0 / ${clubMaxYds}Y" else "${targetYds}Y / ${clubMaxYds}Y",
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = ZenithTextSecondary
            )

            // Vertical Power Bar Track (scales from 0% to 100% of selected club's max distance)
            Box(
                modifier = Modifier
                    .width(54.dp)
                    .height(128.dp)
                    .background(Color(0xFF091810), RoundedCornerShape(14.dp))
                    .border(
                        width = 1.5.dp,
                        color = if (inCancelZone) Color(0xFFFF5252) else ZenithSurfaceBorder,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clip(RoundedCornerShape(14.dp))
                    .testTag("drag_power_bar")
            ) {
                // Max club distance cap label at top
                Text(
                    text = "MAX ${clubMaxYds}Y",
                    fontSize = 7.sp,
                    fontWeight = FontWeight.Black,
                    color = ZenithGold,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp)
                )

                // Power Fill Bar from bottom
                if (!inCancelZone && activePowerRatio > 0.01f) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .fillMaxHeight(activePowerRatio.coerceIn(0.05f, 1.0f))
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        ZenithGold,
                                        ZenithNeonGreen,
                                        Color(0xFF1B8E4B)
                                    )
                                )
                            )
                    )
                }

                // 75% / 50% / 25% distance tick marks
                listOf(0.25f, 0.50f, 0.75f).forEach { tick ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = (128f * tick).dp)
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color.White.copy(alpha = 0.28f))
                    )
                }

                // Dead-zone threshold line near bottom
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(12.dp)
                        .background(Color(0x33FF5252))
                )

                Text(
                    text = if (inCancelZone) "DEAD ZONE" else uiState.selectedClub.shortName,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 3.dp)
                )
            }

            // Fine Power Adjustment Row (-10% / +10%) & Spin Toggle
            if (!isDragging && !uiState.isBallInFlight) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ZenithSurfaceElevated,
                        border = BorderStroke(1.dp, ZenithSurfaceBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clickable { onSetPower((uiState.aimPowerRatio - 0.10f).coerceIn(0.15f, 1.0f)) }
                            .testTag("power_minus_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("-PWR", fontSize = 8.sp, fontWeight = FontWeight.Black, color = ZenithTextSecondary)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ZenithSurfaceElevated,
                        border = BorderStroke(1.dp, ZenithSurfaceBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(28.dp)
                            .clickable { onSetPower((uiState.aimPowerRatio + 0.10f).coerceIn(0.15f, 1.0f)) }
                            .testTag("power_plus_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("+PWR", fontSize = 8.sp, fontWeight = FontWeight.Black, color = ZenithNeonGreen)
                        }
                    }
                }

                // Spin Shaping Quick Cycle (Draw / Straight / Fade & Backspin / Topspin)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val curveLabel = when {
                        uiState.curveSpin < -0.2f -> "DRAW"
                        uiState.curveSpin > 0.2f -> "FADE"
                        else -> "STRT"
                    }
                    val vSpinLabel = when {
                        uiState.verticalSpin > 0.2f -> "BACK"
                        uiState.verticalSpin < -0.2f -> "TOP"
                        else -> "FLAT"
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ZenithSurfaceElevated,
                        border = BorderStroke(1.dp, if (abs(uiState.curveSpin) > 0.2f) ZenithCyan else ZenithSurfaceBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp)
                            .clickable {
                                val nextCurve = when {
                                    uiState.curveSpin < -0.2f -> 0f
                                    uiState.curveSpin > 0.2f -> -0.65f
                                    else -> 0.65f
                                }
                                onSetCurveSpin(nextCurve)
                            }
                            .testTag("cycle_curve_spin_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(curveLabel, fontSize = 8.sp, fontWeight = FontWeight.Black, color = ZenithCyan)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ZenithSurfaceElevated,
                        border = BorderStroke(1.dp, if (abs(uiState.verticalSpin) > 0.2f) ZenithGold else ZenithSurfaceBorder),
                        modifier = Modifier
                            .weight(1f)
                            .height(26.dp)
                            .clickable {
                                val nextVSpin = when {
                                    uiState.verticalSpin > 0.2f -> -0.75f
                                    uiState.verticalSpin < -0.2f -> 0f
                                    else -> 0.80f
                                }
                                onSetVerticalSpin(nextVSpin)
                            }
                            .testTag("cycle_vertical_spin_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(vSpinLabel, fontSize = 8.sp, fontWeight = FontWeight.Black, color = ZenithGold)
                        }
                    }
                }
            }

            // Shot Impact Quality / Cancelled Feedback Pill
            AnimatedVisibility(
                visible = uiState.apexQualityText != null && !isDragging,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val quality = uiState.apexQualityText ?: ""
                val qColor = when {
                    quality.contains("CANCEL") -> Color(0xFFFF5252)
                    quality.contains("MAX") || quality.contains("PERFECT") -> ZenithGold
                    else -> ZenithNeonGreen
                }
                Text(
                    text = quality,
                    fontSize = 8.sp,
                    lineHeight = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = qColor,
                    textAlign = TextAlign.Center
                )
            }

            // Primary Action: Explicit Cancel while dragging OR Shot Confirmation Button (>= 48.dp)
            if (isDragging) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0x33FF5252),
                    border = BorderStroke(1.dp, Color(0xFFFF5252)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { onCancelSwing() }
                        .testTag("cancel_swing_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "CANCEL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFFF5252)
                        )
                    }
                }
            } else if (!uiState.isBallInFlight && !uiState.holeCompleted) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ZenithNeonGreen,
                    border = BorderStroke(1.dp, ZenithGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { onConfirmShot() }
                        .testTag("confirm_shot_button")
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "CONFIRM SHOT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF002B11)
                        )
                        Text(
                            text = "OR DRAG SCREEN",
                            fontSize = 7.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF004D1F)
                        )
                    }
                }

                if (uiState.previousBallPos != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = ZenithSurfaceElevated,
                        border = BorderStroke(1.dp, ZenithGold.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 44.dp)
                            .clickable { onMulligan() }
                            .testTag("mulligan_button")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Use Mulligan",
                                tint = ZenithGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "MULLIGAN",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = ZenithGold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-Screen Pause & Tactical Menu Overlay for PlayHoleScreen:
 * Supports Resume, Restart Hole, Camera View Selection (3rd-Person Golfer, Ball Tracking, Overview, Putting),
 * Course Conditions readout, and Exit to Clubhouse.
 */
@Composable
fun GolfPauseMenuOverlay(
    uiState: GameUiState,
    onResume: () -> Unit,
    onRestartHole: () -> Unit,
    onSelectCameraMode: (CameraViewMode) -> Unit,
    onExitToClubhouse: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hole = uiState.currentHole
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xD9040C08))
            .testTag("pause_menu_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xF50B1D13),
            border = BorderStroke(1.5.dp, ZenithGold.copy(alpha = 0.75f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(20.dp)
                .widthIn(max = 460.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "MATCH PAUSED • HOLE #${hole.holeNumber}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = ZenithGold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${hole.holeTitle} • Par ${hole.par} • ${hole.yardage} Yds • Total Strokes: ${uiState.totalRoundStrokes} (${uiState.roundScoreToParText})",
                    fontSize = 12.sp,
                    color = ZenithTextSecondary,
                    textAlign = TextAlign.Center
                )

                // Camera System Selector (4 Modes)
                Text(
                    text = "CAMERA SYSTEM",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    color = ZenithCyan,
                    letterSpacing = 0.8.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CameraViewMode.values().forEach { mode ->
                        val selected = uiState.cameraMode == mode
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (selected) ZenithNeonGreen else ZenithSurfaceElevated,
                            border = BorderStroke(1.dp, if (selected) ZenithNeonGreen else ZenithSurfaceBorder),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                                .clickable { onSelectCameraMode(mode) }
                                .testTag("pause_cam_${mode.name.lowercase()}")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = mode.shortTag,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (selected) Color(0xFF002B11) else Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Primary Resume Button
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = ZenithNeonGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("resume_game_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF002B11)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "RESUME MATCH",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF002B11)
                    )
                }

                // Restart Hole & Exit Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ZenithSurfaceElevated,
                        border = BorderStroke(1.dp, ZenithGold.copy(alpha = 0.65f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable { onRestartHole() }
                            .testTag("restart_hole_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "RESTART HOLE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = ZenithGold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ZenithSurfaceElevated,
                        border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.65f)),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clickable { onExitToClubhouse() }
                            .testTag("pause_exit_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "EXIT TO CLUBHOUSE",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF5252)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-Screen "Rotate your device" Overlay shown when the phone is held in portrait.
 * Includes a 1-tap button to switch the Activity to landscape or continue immediately.
 */
@Composable
fun RotateDevicePortraitOverlay(
    onForceLandscape: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF050E09),
                        Color(0xFF0B2114),
                        Color(0xFF050E09)
                    )
                )
            )
            .testTag("rotate_device_overlay"),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = Color(0xEE0A1A10),
            border = BorderStroke(1.5.dp, ZenithNeonGreen.copy(alpha = 0.7f)),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(24.dp)
                .widthIn(max = 380.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(28.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .background(ZenithNeonGreen.copy(alpha = 0.15f), CircleShape)
                        .border(1.5.dp, ZenithNeonGreen, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = "Rotate your device",
                        tint = ZenithNeonGreen,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Text(
                    text = "Rotate your device",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Please turn your phone horizontally (landscape) for the full-screen 3D course view, bottom-left club selector, and right-side vertical swing meter.",
                    fontSize = 13.sp,
                    color = ZenithTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(
                    onClick = onForceLandscape,
                    colors = ButtonDefaults.buttonColors(containerColor = ZenithNeonGreen),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .testTag("rotate_to_landscape_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ScreenRotation,
                        contentDescription = null,
                        tint = Color(0xFF003314),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ROTATE TO LANDSCAPE",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF003314)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = ZenithSurfaceElevated,
                    border = BorderStroke(1.dp, ZenithSurfaceBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .clickable { onBack() }
                        .testTag("portrait_overlay_back_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(vertical = 12.dp)
                    ) {
                        Text(
                            text = "Back to Clubhouse",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = ZenithTextSecondary
                        )
                    }
                }
            }
        }
    }
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
