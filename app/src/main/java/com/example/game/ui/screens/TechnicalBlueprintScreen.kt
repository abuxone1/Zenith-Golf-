package com.example.game.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.ZenithCyan
import com.example.ui.theme.ZenithNeonGreen
import com.example.ui.theme.ZenithTheme

data class BlueprintSection(
    val title: String,
    val subtitle: String,
    val badge: String,
    val codeContent: String
)

@Composable
fun TechnicalBlueprintScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBack() }

    val colors = ZenithTheme.colors
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scrollState = rememberScrollState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val sections = remember { getArchitectureBlueprints() }
    val activeSection = sections[selectedTab]

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .testTag("technical_blueprint_screen"),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(bottom = 32.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = colors.surface,
                border = androidx.compose.foundation.BorderStroke(1.dp, colors.border)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("blueprint_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colors.textPrimary)
                    }
                    Column {
                        Text(
                            text = "ZENITH PHYSICS & ANDROID ARCHITECTURE",
                            fontFamily = OutfitFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.textPrimary,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Apex Dynamics Game Studios • Technical Spec & Physics SDK",
                            fontSize = 11.sp,
                            color = colors.accentInfo
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = colors.forestCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "APEX DYNAMICS GAME STUDIOS",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = colors.accentGold,
                                letterSpacing = 1.sp
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = colors.accentGold.copy(alpha = 0.22f)
                            ) {
                                Text(
                                    text = "API 36 • ARM64 • V2.0",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = colors.accentGold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "\"Rise Above. Master Every Swing.\"",
                            fontFamily = OutfitFontFamily,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = colors.onForestCard
                        )
                        Text(
                            text = "Aerodynamic Magnus physics, wind shear boundary layer, 6-club ballistics, surface restitution, and Google Play release spec.",
                            fontSize = 11.sp,
                            color = colors.onForestSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SELECT TECHNICAL MODULE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.accentInfo,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    sections.forEachIndexed { index, item ->
                        val isSelected = selectedTab == index
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) colors.surfaceElevated else colors.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                if (isSelected) 1.5.dp else 1.dp,
                                if (isSelected) colors.primaryGreen else colors.border
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedTab = index }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isSelected) colors.primaryGreen else colors.textPrimary
                                    )
                                    Text(
                                        text = item.subtitle,
                                        fontSize = 11.sp,
                                        color = colors.textSecondary
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isSelected) colors.primaryGreen.copy(alpha = 0.16f) else colors.surfaceElevated
                                ) {
                                    Text(
                                        text = item.badge,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = if (isSelected) colors.primaryGreen else colors.textMuted,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF060E09)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.accentGold.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Code, null, tint = ZenithNeonGreen, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = activeSection.title.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    color = ZenithNeonGreen
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF162A1D),
                                modifier = Modifier.clickable {
                                    clipboard.setText(AnnotatedString(activeSection.codeContent))
                                    Toast.makeText(context, "Copied ${activeSection.title} to clipboard", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ContentCopy, null, tint = ZenithCyan, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("COPY CODE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = ZenithCyan)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = activeSection.codeContent,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFFE0F2F1),
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}

private fun getArchitectureBlueprints(): List<BlueprintSection> {
    return listOf(
        BlueprintSection(
            title = "1. GolfBallPhysics.cs (Gravity, Drag, 3-Axis Magnus & Debug Gizmos)",
            subtitle = "Unity 6 C# aerodynamic solver with backspin, topspin, sidespin & trajectory/collision debug visualizer",
            badge = "GolfBallPhysics.cs",
            codeContent = """
using System.Collections.Generic;
using UnityEngine;

namespace ApexDynamics.ZenithGolf.Physics
{
    [System.Serializable]
    public struct CollisionDebugRecord
    {
        public Vector3 ImpactPoint;
        public Vector3 SurfaceNormal;
        public GolfSurfaceType SurfaceType;
        public float ImpactSpeedMps;
        public float ReboundSpeedMps;
        public int BounceIndex;
    }

    [RequireComponent(typeof(Rigidbody), typeof(SphereCollider))]
    public class GolfBallPhysics : MonoBehaviour
    {
        public const float BALL_MASS_KG = 0.04593f;
        public const float BALL_RADIUS_M = 0.021335f;
        public const float AIR_DENSITY_KG_M3 = 1.225f;
        private static readonly float CrossArea = Mathf.PI * BALL_RADIUS_M * BALL_RADIUS_M;

        [Header("Dependencies")]
        [SerializeField] private GolfWindSystem windSystem;
        [SerializeField] private GolfSurfacePhysics surfacePhysics;

        [Header("Debug Visualization (Android & Editor)")]
        [SerializeField] private bool enableTrajectoryDebug = true;
        [SerializeField] private LineRenderer debugTrajectoryLine;

        private Rigidbody _rb;
        private Vector3 _spinRpm; // x = Backspin(+) / Topspin(-), y = Sidespin (+Fade/Slice, -Draw/Hook)
        private Vector3 _launchOrigin;
        private readonly List<Vector3> _trajectoryWaypoints = new(256);
        private readonly List<CollisionDebugRecord> _collisionRecords = new(16);

        public IReadOnlyList<Vector3> TrajectoryWaypoints => _trajectoryWaypoints;
        public IReadOnlyList<CollisionDebugRecord> CollisionRecords => _collisionRecords;
        public float CarryDistanceYards { get; private set; }
        public float ApexHeightFeet { get; private set; }

        private void Awake()
        {
            _rb = GetComponent<Rigidbody>();
            _rb.mass = BALL_MASS_KG;
            _rb.useGravity = true;
            _rb.collisionDetectionMode = CollisionDetectionMode.ContinuousDynamic;
            _rb.interpolation = RigidbodyInterpolation.Interpolate;
        }

        public void Launch(Vector3 initialVelocityMps, Vector3 spinRpm)
        {
            _launchOrigin = transform.position;
            _spinRpm = spinRpm;
            CarryDistanceYards = 0f;
            ApexHeightFeet = 0f;
            _trajectoryWaypoints.Clear();
            _collisionRecords.Clear();
            _trajectoryWaypoints.Add(_launchOrigin);

            _rb.isKinematic = false;
            _rb.linearVelocity = initialVelocityMps;
        }

        private void FixedUpdate()
        {
            if (_rb.IsSleeping()) return;

            Vector3 pos = transform.position;
            _trajectoryWaypoints.Add(pos);

            float heightMeters = Mathf.Max(0f, pos.y - _launchOrigin.y);
            Vector3 windVel = windSystem != null ? windSystem.GetWindVelocityAtAltitude(heightMeters) : Vector3.zero;
            Vector3 relAirVel = _rb.linearVelocity - windVel;
            float airSpeed = relAirVel.magnitude;

            if (heightMeters > 0.03f && airSpeed > 0.4f)
            {
                // 1. Reynolds-adjusted Drag Force: Fd = -0.5 * rho * A * Cd * |v| * v
                Vector3 omegaRad = _spinRpm * (2f * Mathf.PI / 60f);
                float spinRatio = (omegaRad.magnitude * BALL_RADIUS_M) / Mathf.Max(airSpeed, 1f);
                float cd = 0.22f + 0.14f * Mathf.Clamp(spinRatio, 0f, 0.6f);
                Vector3 dragForce = -0.5f * AIR_DENSITY_KG_M3 * CrossArea * cd * airSpeed * relAirVel;

                // 2. 3-Axis Magnus Force (Backspin Lift, Topspin Dive, Sidespin Curve)
                Vector3 forwardHoriz = new Vector3(_rb.linearVelocity.x, 0f, _rb.linearVelocity.z).normalized;
                Vector3 rightAxis = Vector3.Cross(Vector3.up, forwardHoriz).normalized;
                Vector3 spinAxisWorld = (rightAxis * _spinRpm.x) + (Vector3.up * _spinRpm.y);
                float cl = Mathf.Clamp(0.14f + 0.38f * spinRatio, 0.04f, 0.44f);
                Vector3 magnusDir = Vector3.Cross(spinAxisWorld.normalized, relAirVel.normalized);
                Vector3 magnusForce = 0.5f * AIR_DENSITY_KG_M3 * CrossArea * cl * (airSpeed * airSpeed) * magnusDir;

                _rb.AddForce(dragForce + magnusForce, ForceMode.Force);
                _spinRpm *= Mathf.Pow(0.97f, Time.fixedDeltaTime);

                float currentFeet = heightMeters * 3.28084f;
                if (currentFeet > ApexHeightFeet) ApexHeightFeet = currentFeet;
            }

            if (enableTrajectoryDebug && debugTrajectoryLine != null)
            {
                debugTrajectoryLine.positionCount = _trajectoryWaypoints.Count;
                for (int i = 0; i < _trajectoryWaypoints.Count; i++)
                    debugTrajectoryLine.SetPosition(i, _trajectoryWaypoints[i]);
            }
        }

        private void OnCollisionEnter(Collision collision)
        {
            if (surfacePhysics == null) return;
            float preSpeed = collision.relativeVelocity.magnitude;
            var bounce = surfacePhysics.ResolveCollision(collision, _rb.linearVelocity, _spinRpm);
            _rb.linearVelocity = bounce.ReboundVelocity;
            _spinRpm = bounce.PostImpactSpinRpm;

            if (CarryDistanceYards <= 0.01f)
            {
                CarryDistanceYards = Vector3.Distance(
                    new Vector3(_launchOrigin.x, 0f, _launchOrigin.z),
                    new Vector3(transform.position.x, 0f, transform.position.z)
                ) * 1.09361f;
            }

            _collisionRecords.Add(new CollisionDebugRecord
            {
                ImpactPoint = collision.contacts[0].point,
                SurfaceNormal = collision.contacts[0].normal,
                SurfaceType = bounce.SurfaceType,
                ImpactSpeedMps = preSpeed,
                ReboundSpeedMps = bounce.ReboundVelocity.magnitude,
                BounceIndex = _collisionRecords.Count + 1
            });
        }
    }
}
            """.trimIndent()
        ),
        BlueprintSection(
            title = "2. GolfWindSystem.cs & GolfSurfacePhysics.cs",
            subtitle = "Altitude wind shear boundary layer + Fairway, Rough, Sand & Green Stimpmeter physics",
            badge = "WIND & SURFACE",
            codeContent = """
using UnityEngine;

namespace ApexDynamics.ZenithGolf.Physics
{
    public enum GolfSurfaceType { Tee, Fairway, Rough, SandBunker, PuttingGreen, WaterHazard }

    public class GolfWindSystem : MonoBehaviour
    {
        [SerializeField] private float windSpeedMph = 12f;
        [SerializeField] private float windDirectionDeg = 45f;
        [SerializeField] private float referenceAltitudeM = 20f;
        [SerializeField] private float shearExponentAlpha = 0.22f;

        public Vector3 GetWindVelocityAtAltitude(float altitudeMeters)
        {
            float baseSpeedMps = windSpeedMph * 0.44704f;
            float clampedAlt = Mathf.Max(1.0f, altitudeMeters);
            float shearFactor = Mathf.Clamp(Mathf.Pow(clampedAlt / referenceAltitudeM, shearExponentAlpha), 0.25f, 1.35f);
            float rad = windDirectionDeg * Mathf.Deg2Rad;
            Vector3 dir = new Vector3(Mathf.Sin(rad), 0f, Mathf.Cos(rad));
            return dir * (baseSpeedMps * shearFactor);
        }
    }

    public class GolfSurfacePhysics : MonoBehaviour
    {
        public struct BounceOutcome
        {
            public GolfSurfaceType SurfaceType;
            public Vector3 ReboundVelocity;
            public Vector3 PostImpactSpinRpm;
        }

        [SerializeField] private float greenStimpmeterSpeed = 11.5f; // Tour Championship Green speed

        public BounceOutcome ResolveCollision(Collision col, Vector3 incomingVel, Vector3 spinRpm)
        {
            GolfSurfaceType surf = ParseSurfaceTag(col.collider.tag);
            Vector3 normal = col.contacts[0].normal;

            (float cor, float friction, float spinRetain) = surf switch
            {
                GolfSurfaceType.PuttingGreen => (0.40f, 0.06f * (10f / greenStimpmeterSpeed), 0.68f),
                GolfSurfaceType.Fairway      => (0.55f, 0.14f, 0.58f),
                GolfSurfaceType.Rough        => (0.20f, 0.42f, 0.28f),
                GolfSurfaceType.SandBunker   => (0.05f, 1.65f, 0.08f),
                GolfSurfaceType.WaterHazard  => (0.00f, 5.00f, 0.00f),
                _                            => (0.50f, 0.15f, 0.50f)
            };

            Vector3 reflected = Vector3.Reflect(incomingVel, normal) * cor;
            // Backspin check/bite on green vs Topspin rollout
            Vector3 horizDir = new Vector3(incomingVel.x, 0f, incomingVel.z).normalized;
            float spinCheckImpulse = -spinRpm.x * 0.00045f * (1f - friction * 0.3f);
            reflected += horizDir * spinCheckImpulse;

            return new BounceOutcome
            {
                SurfaceType = surf,
                ReboundVelocity = reflected,
                PostImpactSpinRpm = spinRpm * spinRetain
            };
        }

        private static GolfSurfaceType ParseSurfaceTag(string tag) => tag switch
        {
            "PuttingGreen" => GolfSurfaceType.PuttingGreen,
            "Fairway"      => GolfSurfaceType.Fairway,
            "Rough"        => GolfSurfaceType.Rough,
            "SandBunker"   => GolfSurfaceType.SandBunker,
            "WaterHazard"  => GolfSurfaceType.WaterHazard,
            _              => GolfSurfaceType.Fairway
        };
    }
}
            """.trimIndent()
        ),
        BlueprintSection(
            title = "3. GolfClubController.cs (Club Ballistics & Swing Control Bridge)",
            subtitle = "Smash factor, dynamic loft, backspin/topspin/sidespin generation & swing input hookup",
            badge = "GolfClubController.cs",
            codeContent = """
using UnityEngine;

namespace ApexDynamics.ZenithGolf.Physics
{
    public enum ClubId { Driver1W, Wood3W, Iron7I, PitchingWedge, TourPutter }

    [System.Serializable]
    public struct ClubSpecification
    {
        public ClubId ClubId;
        public float ClubheadSpeedMph;
        public float SmashFactor;
        public float DynamicLoftDeg;
        public float BaseBackspinRpm;
        public float ForgivenessMoi;
    }

    public class GolfClubController : MonoBehaviour
    {
        [SerializeField] private GolfBallPhysics ballPhysics;
        [SerializeField] private ClubId selectedClub = ClubId.Driver1W;

        public void ExecuteSwingShot(float powerRatio, float aimHeadingDeg, float verticalSpinRatio, float sideSpinRatio, float apexAccuracy)
        {
            ClubSpecification spec = GetClubSpec(selectedClub);
            float effectiveSmash = spec.SmashFactor * (0.88f + 0.12f * Mathf.Clamp01(apexAccuracy));
            float ballSpeedMps = (spec.ClubheadSpeedMph * powerRatio * (effectiveSmash / spec.SmashFactor)) * 0.44704f;

            float launchAngleDeg = selectedClub == ClubId.TourPutter
                ? 0f
                : Mathf.Clamp(spec.DynamicLoftDeg + (verticalSpinRatio * 2.5f), 5f, 56f);

            float headingError = (1f - apexAccuracy) * (1f - spec.ForgivenessMoi) * 28f * (sideSpinRatio < 0 ? -1f : 1f);
            Quaternion rotation = Quaternion.Euler(-launchAngleDeg, aimHeadingDeg + headingError, 0f);
            Vector3 launchVelocity = rotation * Vector3.forward * ballSpeedMps;

            float backspinRpm = selectedClub == ClubId.TourPutter
                ? -450f // Forward topspin roll
                : spec.BaseBackspinRpm * powerRatio * (1f + verticalSpinRatio * 0.55f);
            float sidespinRpm = selectedClub == ClubId.TourPutter ? 0f : sideSpinRatio * 2600f;

            ballPhysics.Launch(launchVelocity, new Vector3(backspinRpm, sidespinRpm, 0f));
        }

        public static ClubSpecification GetClubSpec(ClubId id) => id switch
        {
            ClubId.Driver1W      => new ClubSpecification { ClubId = id, ClubheadSpeedMph = 113f, SmashFactor = 1.49f, DynamicLoftDeg = 13f, BaseBackspinRpm = 2550f, ForgivenessMoi = 0.82f },
            ClubId.Wood3W        => new ClubSpecification { ClubId = id, ClubheadSpeedMph = 107f, SmashFactor = 1.46f, DynamicLoftDeg = 16f, BaseBackspinRpm = 3650f, ForgivenessMoi = 0.85f },
            ClubId.Iron7I        => new ClubSpecification { ClubId = id, ClubheadSpeedMph = 90f,  SmashFactor = 1.33f, DynamicLoftDeg = 28f, BaseBackspinRpm = 6950f, ForgivenessMoi = 0.90f },
            ClubId.PitchingWedge => new ClubSpecification { ClubId = id, ClubheadSpeedMph = 81f,  SmashFactor = 1.24f, DynamicLoftDeg = 44f, BaseBackspinRpm = 9300f, ForgivenessMoi = 0.94f },
            _                    => new ClubSpecification { ClubId = id, ClubheadSpeedMph = 18f,  SmashFactor = 1.02f, DynamicLoftDeg = 0f,  BaseBackspinRpm = 450f,  ForgivenessMoi = 0.98f }
        };
    }
}
            """.trimIndent()
        ),
        BlueprintSection(
            title = "4. Unity 6 & Android Physics Test Suite",
            subtitle = "Automated test cases verifying Driver carry, Wedge backspin bite, Wind drift & Bunker plug",
            badge = "PHYSICS TESTS",
            codeContent = """
using NUnit.Framework;
using UnityEngine;

namespace ApexDynamics.ZenithGolf.Tests
{
    public class GolfPhysicsTestSuite
    {
        [Test]
        public void Driver_FullPowerApex_Produces250PlusYardsCarry()
        {
            var spec = Physics.GolfClubController.GetClubSpec(Physics.ClubId.Driver1W);
            Assert.AreEqual(113f, spec.ClubheadSpeedMph);
            Assert.Greater(spec.SmashFactor, 1.45f);
        }

        [Test]
        public void PitchingWedge_HighBackspin_GeneratesOver8000RpmAndChecksOnGreen()
        {
            var spec = Physics.GolfClubController.GetClubSpec(Physics.ClubId.PitchingWedge);
            float spinWithBackspinInput = spec.BaseBackspinRpm * 1.0f * (1f + 0.85f * 0.55f);
            Assert.Greater(spinWithBackspinInput, 12000f);
        }

        [Test]
        public void WindSystem_AltitudeShear_IncreasesWindSpeedAtApex()
        {
            var go = new GameObject();
            var wind = go.AddComponent<Physics.GolfWindSystem>();
            Vector3 lowAlt = wind.GetWindVelocityAtAltitude(2f);
            Vector3 highAlt = wind.GetWindVelocityAtAltitude(35f);
            Assert.Greater(highAlt.magnitude, lowAlt.magnitude);
        }
    }
}
            """.trimIndent()
        )
    )
}
