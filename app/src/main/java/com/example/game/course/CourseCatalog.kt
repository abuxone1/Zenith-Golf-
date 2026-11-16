package com.example.game.course

import androidx.compose.ui.geometry.Offset
import com.example.game.model.FairwayZone
import com.example.game.model.HazardZone
import com.example.game.model.Hole
import com.example.game.model.SurfaceType

object CourseCatalog {

    fun getChampionshipHoles(): List<Hole> {
        return listOf(
            // HOLE 1: Par 4 - 385 Yards "Zenith Gateway"
            Hole(
                id = 1,
                courseName = "Zenith Dunes Championship",
                holeNumber = 1,
                par = 4,
                yardage = 385,
                teePosition = Offset(200f, 680f),
                pinPosition = Offset(200f, 120f),
                greenCenter = Offset(200f, 130f),
                greenRadius = 38f,
                fairways = listOf(
                    FairwayZone(center = Offset(200f, 540f), width = 75f, height = 210f),
                    FairwayZone(center = Offset(200f, 310f), width = 70f, height = 230f),
                    FairwayZone(center = Offset(200f, 150f), width = 60f, height = 90f)
                ),
                hazards = listOf(
                    // Sand bunkers
                    HazardZone(SurfaceType.SAND_BUNKER, center = Offset(145f, 420f), radiusX = 20f, radiusY = 35f),
                    HazardZone(SurfaceType.SAND_BUNKER, center = Offset(255f, 320f), radiusX = 22f, radiusY = 30f),
                    HazardZone(SurfaceType.SAND_BUNKER, center = Offset(155f, 140f), radiusX = 18f, radiusY = 25f),
                    HazardZone(SurfaceType.SAND_BUNKER, center = Offset(245f, 110f), radiusX = 16f, radiusY = 22f)
                ),
                greenSlope = Offset(0.3f, 0.4f),
                holeTitle = "Gateway Fairway"
            ),

            // HOLE 2: Par 3 - 165 Yards "The Island Apex"
            Hole(
                id = 2,
                courseName = "Zenith Dunes Championship",
                holeNumber = 2,
                par = 3,
                yardage = 165,
                teePosition = Offset(200f, 580f),
                pinPosition = Offset(200f, 160f),
                greenCenter = Offset(200f, 165f),
                greenRadius = 45f,
                fairways = listOf(
                    FairwayZone(center = Offset(200f, 480f), width = 50f, height = 80f),
                    FairwayZone(center = Offset(200f, 170f), width = 80f, height = 75f)
                ),
                hazards = listOf(
                    // Water lake surrounding island green
                    HazardZone(SurfaceType.WATER_HAZARD, center = Offset(120f, 180f), radiusX = 55f, radiusY = 80f),
                    HazardZone(SurfaceType.WATER_HAZARD, center = Offset(280f, 180f), radiusX = 55f, radiusY = 80f),
                    HazardZone(SurfaceType.WATER_HAZARD, center = Offset(200f, 255f), radiusX = 75f, radiusY = 35f),
                    // Sand trap behind pin
                    HazardZone(SurfaceType.SAND_BUNKER, center = Offset(200f, 105f), radiusX = 28f, radiusY = 14f)
                ),
                greenSlope = Offset(-0.2f, 0.5f),
                holeTitle = "Island Sanctuary"
            ),

            // HOLE 3: Par 5 - 520 Yards "Cyber Dogleg"
            Hole(
                id = 3,
                courseName = "Zenith Dunes Championship",
                holeNumber = 3,
                par = 5,
                yardage = 520,
                teePosition = Offset(120f, 720f),
                pinPosition = Offset(280f, 90f),
                greenCenter = Offset(280f, 100f),
                greenRadius = 36f,
                fairways = listOf(
                    // Tee to corner
                    FairwayZone(center = Offset(130f, 560f), width = 65f, height = 230f, angleDeg = -5f),
                    // Corner bend
                    FairwayZone(center = Offset(180f, 370f), width = 75f, height = 160f, angleDeg = 35f),
                    // Approach to green
                    FairwayZone(center = Offset(265f, 220f), width = 60f, height = 180f, angleDeg = -8f),
                    FairwayZone(center = Offset(280f, 110f), width = 65f, height = 80f)
                ),
                hazards = listOf(
                    // Water lake on inside corner
                    HazardZone(SurfaceType.WATER_HAZARD, center = Offset(240f, 440f), radiusX = 50f, radiusY = 75f),
                    // Sand traps on landing zones
                    HazardZone(SurfaceType.SAND_BUNKER, center = Offset(75f, 460f), radiusX = 18f, radiusY = 32f),
                    HazardZone(SurfaceType.SAND_BUNKER, center = Offset(220f, 120f), radiusX = 16f, radiusY = 28f),
                    HazardZone(SurfaceType.SAND_BUNKER, center = Offset(335f, 95f), radiusX = 18f, radiusY = 22f)
                ),
                greenSlope = Offset(0.4f, -0.3f),
                holeTitle = "Falcon Dogleg"
            ),

            // HOLE 4: Par 4 - 410 Yards "Emerald Dunes"
            Hole(
                id = 4,
                courseName = "Zenith Dunes Championship",
                holeNumber = 4,
                par = 4,
                yardage = 410,
                teePosition = Offset(250f, 700f),
                pinPosition = Offset(150f, 110f),
                greenCenter = Offset(150f, 120f),
                greenRadius = 40f,
                fairways = listOf(
                    FairwayZone(center = Offset(230f, 540f), width = 70f, height = 220f, angleDeg = 8f),
                    FairwayZone(center = Offset(175f, 320f), width = 65f, height = 210f, angleDeg = -14f),
                    FairwayZone(center = Offset(150f, 130f), width = 65f, height = 90f)
                ),
                hazards = listOf(
                    HazardZone(SurfaceType.SAND_BUNKER, center = Offset(290f, 480f), radiusX = 22f, radiusY = 35f),
                    HazardZone(SurfaceType.SAND_BUNKER, center = Offset(110f, 320f), radiusX = 24f, radiusY = 40f),
                    HazardZone(SurfaceType.SAND_BUNKER, center = Offset(95f, 130f), radiusX = 18f, radiusY = 25f),
                    HazardZone(SurfaceType.WATER_HAZARD, center = Offset(230f, 180f), radiusX = 40f, radiusY = 55f)
                ),
                greenSlope = Offset(-0.3f, 0.2f),
                holeTitle = "Whispering Pines"
            ),

            // HOLE 5: Par 3 - 195 Yards "Cyber Canyon"
            Hole(
                id = 5,
                courseName = "Zenith Dunes Championship",
                holeNumber = 5,
                par = 3,
                yardage = 195,
                teePosition = Offset(200f, 620f),
                pinPosition = Offset(200f, 130f),
                greenCenter = Offset(200f, 140f),
                greenRadius = 42f,
                fairways = listOf(
                    FairwayZone(center = Offset(200f, 540f), width = 45f, height = 70f),
                    FairwayZone(center = Offset(200f, 145f), width = 70f, height = 80f)
                ),
                hazards = listOf(
                    HazardZone(SurfaceType.WATER_HAZARD, center = Offset(200f, 350f), radiusX = 120f, radiusY = 90f),
                    HazardZone(SurfaceType.SAND_BUNKER, center = Offset(140f, 140f), radiusX = 18f, radiusY = 28f),
                    HazardZone(SurfaceType.SAND_BUNKER, center = Offset(260f, 140f), radiusX = 18f, radiusY = 28f)
                ),
                greenSlope = Offset(0.1f, 0.4f),
                holeTitle = "Canyon Leap"
            ),

            // HOLE 6: Par 4 - 360 Yards "Championship Finale"
            Hole(
                id = 6,
                courseName = "Zenith Dunes Championship",
                holeNumber = 6,
                par = 4,
                yardage = 360,
                teePosition = Offset(180f, 660f),
                pinPosition = Offset(220f, 95f),
                greenCenter = Offset(220f, 105f),
                greenRadius = 44f,
                fairways = listOf(
                    FairwayZone(center = Offset(190f, 500f), width = 75f, height = 220f),
                    FairwayZone(center = Offset(210f, 270f), width = 80f, height = 230f),
                    FairwayZone(center = Offset(220f, 120f), width = 70f, height = 90f)
                ),
                hazards = listOf(
                    HazardZone(SurfaceType.WATER_HAZARD, center = Offset(105f, 260f), radiusX = 45f, radiusY = 90f),
                    HazardZone(SurfaceType.SAND_BUNKER, center = Offset(280f, 400f), radiusX = 22f, radiusY = 35f),
                    HazardZone(SurfaceType.SAND_BUNKER, center = Offset(155f, 105f), radiusX = 20f, radiusY = 30f),
                    HazardZone(SurfaceType.SAND_BUNKER, center = Offset(285f, 95f), radiusX = 22f, radiusY = 25f)
                ),
                greenSlope = Offset(0.2f, 0.3f),
                holeTitle = "Zenith Colosseum"
            )
        )
    }

    fun getPracticeHole(): Hole {
        return Hole(
            id = 99,
            courseName = "Apex Driving Range",
            holeNumber = 0,
            par = 3,
            yardage = 300,
            teePosition = Offset(200f, 650f),
            pinPosition = Offset(200f, 150f),
            greenCenter = Offset(200f, 160f),
            greenRadius = 55f,
            fairways = listOf(
                FairwayZone(center = Offset(200f, 400f), width = 220f, height = 450f)
            ),
            hazards = listOf(
                HazardZone(SurfaceType.SAND_BUNKER, center = Offset(110f, 250f), radiusX = 30f, radiusY = 40f),
                HazardZone(SurfaceType.SAND_BUNKER, center = Offset(290f, 250f), radiusX = 30f, radiusY = 40f)
            ),
            greenSlope = Offset(0.0f, 0.0f),
            holeTitle = "Target Range"
        )
    }

    fun getShootoutHole(): Hole {
        return Hole(
            id = 100,
            courseName = "Bullseye Island Stadium",
            holeNumber = 1,
            par = 3,
            yardage = 145,
            teePosition = Offset(200f, 640f),
            pinPosition = Offset(200f, 220f),
            greenCenter = Offset(200f, 220f),
            greenRadius = 50f,
            fairways = listOf(
                FairwayZone(center = Offset(200f, 520f), width = 60f, height = 120f),
                FairwayZone(center = Offset(200f, 220f), width = 100f, height = 100f)
            ),
            hazards = listOf(
                // Surrounding stadium water
                HazardZone(SurfaceType.WATER_HAZARD, center = Offset(100f, 220f), radiusX = 55f, radiusY = 85f),
                HazardZone(SurfaceType.WATER_HAZARD, center = Offset(300f, 220f), radiusX = 55f, radiusY = 85f),
                HazardZone(SurfaceType.WATER_HAZARD, center = Offset(200f, 320f), radiusX = 80f, radiusY = 35f),
                HazardZone(SurfaceType.WATER_HAZARD, center = Offset(200f, 120f), radiusX = 80f, radiusY = 35f)
            ),
            greenSlope = Offset(0.0f, 0.0f),
            holeTitle = "Bullseye Coliseum"
        )
    }

    fun getTournamentCups(): List<com.example.game.model.TournamentCup> {
        return listOf(
            com.example.game.model.TournamentCup(
                id = "cup_dunes_open",
                title = "Zenith Dunes Open",
                subtitle = "Tour Season Kickoff across coastal fairways and rolling sand dunes",
                courseName = "Zenith Dunes Championship",
                entryFeeCoins = 0,
                prizeCoins = 2500,
                prizeTrophies = 1,
                holesCount = 6,
                defaultWeather = com.example.game.model.WeatherType.SUNNY,
                difficulty = "Open",
                bannerAccentHex = 0xFF00E676
            ),
            com.example.game.model.TournamentCup(
                id = "cup_cyber_night",
                title = "Neon Tokyo Classic",
                subtitle = "Play under stadium twilight floodlights with slick greens and rain",
                courseName = "Zenith Cyber Links",
                entryFeeCoins = 1000,
                prizeCoins = 6000,
                prizeTrophies = 3,
                holesCount = 6,
                defaultWeather = com.example.game.model.WeatherType.TWILIGHT,
                difficulty = "Championship",
                bannerAccentHex = 0xFF00E5FF
            ),
            com.example.game.model.TournamentCup(
                id = "cup_apex_masters",
                title = "Apex Masters Invitational",
                subtitle = "The ultimate test of skill against tempest winds and tucked pins",
                courseName = "Apex Highlands National",
                entryFeeCoins = 2500,
                prizeCoins = 15000,
                prizeTrophies = 5,
                holesCount = 6,
                defaultWeather = com.example.game.model.WeatherType.WINDY_TEMPEST,
                difficulty = "Masters",
                bannerAccentHex = 0xFFFFD700
            )
        )
    }

    fun getDefaultMatchPlayRivals(): List<com.example.game.model.MatchPlayRival> {
        return listOf(
            com.example.game.model.MatchPlayRival(
                id = "rival_jack",
                name = "Jack \"Apex\" Miller",
                title = "Apex Tour World #1",
                rank = "#1 Global",
                avatarColor = androidx.compose.ui.graphics.Color(0xFF00FFCC),
                archetype = "Aggressive Power Bomber",
                drivingDistanceYds = 308,
                puttingRating = 88,
                wagerCostCoins = 500,
                winRewardCoins = 1200,
                introQuote = "Step up to the tee box! Hope you're ready for 300+ yard bullets.",
                birdieQuote = "Clean as a whistle. Right in the heart of the cup!",
                pressureQuote = "Pressure makes diamonds, partner. Your turn!"
            ),
            com.example.game.model.MatchPlayRival(
                id = "rival_valkyrie",
                name = "Valkyrie Vance",
                title = "World Long Drive Champ",
                rank = "#3 Global",
                avatarColor = androidx.compose.ui.graphics.Color(0xFFFF9900),
                archetype = "High Launch Rocket",
                drivingDistanceYds = 325,
                puttingRating = 80,
                wagerCostCoins = 800,
                winRewardCoins = 2000,
                introQuote = "Distance solves all course problems. Don't blink when I launch this driver!",
                birdieQuote = "Pin-seeking laser! That's how we roll.",
                pressureQuote = "You're going to need something magical to catch that shot."
            ),
            com.example.game.model.MatchPlayRival(
                id = "rival_kaito",
                name = "Kaito \"Laser\" Tanaka",
                title = "Zenith Precision Master",
                rank = "#2 Global",
                avatarColor = androidx.compose.ui.graphics.Color(0xFF00E5FF),
                archetype = "Deadly Iron Accuracy",
                drivingDistanceYds = 285,
                puttingRating = 97,
                wagerCostCoins = 1200,
                winRewardCoins = 3000,
                introQuote = "True golf is calculated angles and zero margin for error. Let's duel.",
                birdieQuote = "Measured, calculated, executed into the bottom of the cup.",
                pressureQuote = "The wind is tricky here. Let us see your green reading."
            ),
            com.example.game.model.MatchPlayRival(
                id = "rival_sarah",
                name = "Sarah \"Eagle\" Stone",
                title = "Cyber Links Prodigy",
                rank = "#5 Global",
                avatarColor = androidx.compose.ui.graphics.Color(0xFFBD00FF),
                archetype = "Clutch Scrambler & Putter",
                drivingDistanceYds = 295,
                puttingRating = 94,
                wagerCostCoins = 2000,
                winRewardCoins = 5000,
                introQuote = "I haven't lost a matchplay duel in 3 seasons. Show me what you've got!",
                birdieQuote = "Drained it from 20 yards out! Matchplay is about moments like that.",
                pressureQuote = "The crowd is watching. Big putt right here!"
            )
        )
    }

    fun getDefaultReplays(): List<com.example.game.model.ReplayShot> {
        return listOf(
            com.example.game.model.ReplayShot(
                id = "rep_1",
                title = "318-Yard Fairway Bullet",
                holeName = "Hole 1 Gateway (Par 4)",
                clubUsed = com.example.game.model.ClubType.DRIVER,
                carryYards = 302f,
                ballSpeedMph = 174f,
                launchAngleDeg = 12.8f,
                apexHeightFt = 104f,
                spinRpm = 2350,
                scoreAchieved = "318 YDS TOTAL",
                dateString = "Zenith Tour Final",
                trajectoryPoints = listOf(
                    Offset(200f, 680f), Offset(200f, 540f), Offset(202f, 380f), Offset(201f, 290f)
                ),
                endLie = SurfaceType.FAIRWAY
            ),
            com.example.game.model.ReplayShot(
                id = "rep_2",
                title = "Island Green Tap-In Birdie",
                holeName = "Hole 2 Island Sanctuary (Par 3)",
                clubUsed = com.example.game.model.ClubType.IRON_7,
                carryYards = 162f,
                ballSpeedMph = 128f,
                launchAngleDeg = 18.5f,
                apexHeightFt = 92f,
                spinRpm = 6400,
                scoreAchieved = "2.8 FT FROM PIN",
                dateString = "Cyber Dunes Open",
                trajectoryPoints = listOf(
                    Offset(200f, 580f), Offset(200f, 380f), Offset(200f, 220f), Offset(199f, 162f)
                ),
                endLie = SurfaceType.GREEN
            ),
            com.example.game.model.ReplayShot(
                id = "rep_3",
                title = "500-PT Concentric Bullseye",
                holeName = "Bullseye Island Stadium",
                clubUsed = com.example.game.model.ClubType.PITCHING_WEDGE,
                carryYards = 145f,
                ballSpeedMph = 118f,
                launchAngleDeg = 24.2f,
                apexHeightFt = 115f,
                spinRpm = 8200,
                scoreAchieved = "DIRECT BULLSEYE (+500)",
                dateString = "Shootout Championship",
                trajectoryPoints = listOf(
                    Offset(200f, 640f), Offset(200f, 440f), Offset(200f, 300f), Offset(200f, 220f)
                ),
                endLie = SurfaceType.GREEN
            )
        )
    }

    fun createCustomHole(blueprint: com.example.game.model.CustomHoleBlueprint): Hole {
        val tee = Offset(200f, 680f)
        val greenCenter = when (blueprint.pinPositionStyle) {
            "Tucked Back Right" -> Offset(250f, 120f)
            "Front Edge" -> Offset(200f, 160f)
            "Island Center" -> Offset(200f, 130f)
            else -> Offset(200f, 130f)
        }
        val pin = when (blueprint.pinPositionStyle) {
            "Tucked Back Right" -> Offset(258f, 112f)
            "Front Edge" -> Offset(200f, 165f)
            "Island Center" -> Offset(200f, 130f)
            else -> Offset(200f, 122f)
        }

        val greenRadius = when (blueprint.greenStyle) {
            "Wide Kidney" -> 48f
            "Mini Island" -> 32f
            else -> 40f
        }

        val fairwayWidth = 75f
        val fairways = when (blueprint.fairwayShape) {
            "Right Dogleg" -> listOf(
                FairwayZone(center = Offset(180f, 520f), width = fairwayWidth, height = 220f, angleDeg = -8f),
                FairwayZone(center = Offset(230f, 330f), width = fairwayWidth, height = 210f, angleDeg = 24f),
                FairwayZone(center = greenCenter + Offset(0f, 20f), width = fairwayWidth, height = 80f)
            )
            "Left Dogleg" -> listOf(
                FairwayZone(center = Offset(220f, 520f), width = fairwayWidth, height = 220f, angleDeg = 8f),
                FairwayZone(center = Offset(170f, 330f), width = fairwayWidth, height = 210f, angleDeg = -24f),
                FairwayZone(center = greenCenter + Offset(0f, 20f), width = fairwayWidth, height = 80f)
            )
            "Island Oasis" -> listOf(
                FairwayZone(center = Offset(200f, 520f), width = 70f, height = 180f),
                FairwayZone(center = greenCenter, width = greenRadius * 2f + 10f, height = greenRadius * 2f + 10f)
            )
            else -> listOf(
                FairwayZone(center = Offset(200f, 520f), width = fairwayWidth, height = 220f),
                FairwayZone(center = Offset(200f, 310f), width = fairwayWidth, height = 230f),
                FairwayZone(center = greenCenter + Offset(0f, 15f), width = fairwayWidth, height = 80f)
            )
        }

        val hazards = mutableListOf<HazardZone>()
        if (blueprint.hasWaterLake) {
            hazards.add(
                HazardZone(SurfaceType.WATER_HAZARD, center = Offset(110f, 360f), radiusX = 50f, radiusY = 70f)
            )
            if (blueprint.fairwayShape == "Island Oasis") {
                hazards.add(
                    HazardZone(SurfaceType.WATER_HAZARD, center = Offset(290f, 360f), radiusX = 50f, radiusY = 70f)
                )
            }
        }

        if (blueprint.sandBunkerCount >= 1) {
            hazards.add(HazardZone(SurfaceType.SAND_BUNKER, center = greenCenter + Offset(-greenRadius - 10f, 5f), radiusX = 18f, radiusY = 26f))
        }
        if (blueprint.sandBunkerCount >= 2) {
            hazards.add(HazardZone(SurfaceType.SAND_BUNKER, center = greenCenter + Offset(greenRadius + 10f, -5f), radiusX = 18f, radiusY = 24f))
        }
        if (blueprint.sandBunkerCount >= 3) {
            hazards.add(HazardZone(SurfaceType.SAND_BUNKER, center = Offset(145f, 450f), radiusX = 20f, radiusY = 32f))
        }
        if (blueprint.sandBunkerCount >= 4) {
            hazards.add(HazardZone(SurfaceType.SAND_BUNKER, center = Offset(255f, 350f), radiusX = 22f, radiusY = 30f))
        }

        val greenSlope = when (blueprint.slopeDifficulty) {
            "Severe Ridge" -> Offset(0.5f, 0.4f)
            "Moderate Break" -> Offset(0.25f, 0.3f)
            else -> Offset(0.08f, 0.1f)
        }

        return Hole(
            id = 777,
            courseName = "Custom Architect Links",
            holeNumber = 1,
            par = blueprint.par,
            yardage = blueprint.totalYardage,
            teePosition = tee,
            pinPosition = pin,
            greenCenter = greenCenter,
            greenRadius = greenRadius,
            fairways = fairways,
            hazards = hazards,
            greenSlope = greenSlope,
            holeTitle = blueprint.holeName
        )
    }
}
