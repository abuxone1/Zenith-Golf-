const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

function validTimestamp() {
  return new Date(Date.now() - 1000);
}

// --- SECURITY BOUNDS & 12-COLLECTION VALIDATION TESTS ---

test("1. Unauthenticated user cannot read or write protected player collections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("players").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("player_statistics").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("player_inventory").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("player_progress").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("game_settings").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("players").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "Alice Pro",
      playerLevel: 2,
      xp: 450,
      coins: 850,
      updatedAt: validTimestamp(),
    })
  );
});

test("2. Authenticated user cannot read another user's private records (Cross-user isolation)", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await db.collection("players").doc(BOB_UID).set({
      userId: BOB_UID,
      username: "Bob Golfer",
      email: "bob@example.com",
      playerLevel: 4,
      xp: 900,
      coins: 1200,
      updatedAt: validTimestamp(),
    });
    await db.collection("player_statistics").doc(BOB_UID).set({
      userId: BOB_UID,
      roundsPlayed: 5,
      holesPlayed: 15,
      birdies: 4,
      eagles: 1,
      aces: 0,
      longestDriveYds: 290.5,
      updatedAt: validTimestamp(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("players").doc(BOB_UID).get());
  await assertFails(aliceDb.collection("player_statistics").doc(BOB_UID).get());
});

test("3. Authenticated owner can create and read players, player_statistics, player_inventory, player_progress, and game_settings", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();

  // 1. players
  await assertSucceeds(
    aliceDb.collection("players").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "Alice Apex",
      profileImageUrl: "https://example.com/avatar.png",
      email: "alice@example.com",
      playerLevel: 3,
      xp: 650,
      coins: 1400,
      trophies: 6,
      mulligansRemaining: 4,
      countryTag: "USA • APEX TOUR",
      accountTier: "GOOGLE_LINKED",
      clientUpdatedAtEpochMs: 1700000000000,
      updatedAt: validTimestamp(),
    })
  );

  // 2. player_statistics
  await assertSucceeds(
    aliceDb.collection("player_statistics").doc(ALICE_UID).set({
      userId: ALICE_UID,
      roundsPlayed: 4,
      holesPlayed: 18,
      birdies: 6,
      eagles: 2,
      aces: 1,
      longestDriveYds: 302.4,
      greensInRegulationCount: 12,
      shootoutHighScore: 2100,
      matchPlayWins: 3,
      matchPlayLosses: 1,
      updatedAt: validTimestamp(),
    })
  );

  // 3. player_inventory
  await assertSucceeds(
    aliceDb.collection("player_inventory").doc(ALICE_UID).set({
      userId: ALICE_UID,
      equippedBallId: "standard_white",
      equippedTracer: "Cyan Surge",
      unlockedBallIds: ["standard_white", "neon_viper"],
      driverLevel: 2,
      wood3Level: 2,
      iron4Level: 1,
      iron7Level: 2,
      iron9Level: 1,
      wedgeLevel: 2,
      sandWedgeLevel: 1,
      putterLevel: 2,
      visorColorHex: 0xff00e676,
      poloColorHex: 0xff00e5ff,
      gloveStyle: "Carbon Fiber Pro",
      stance: "RIGHT_HANDED",
      swingStyle: "TOUR_TEMPO",
      updatedAt: validTimestamp(),
    })
  );

  // 4. player_progress
  await assertSucceeds(
    aliceDb.collection("player_progress").doc(ALICE_UID).set({
      userId: ALICE_UID,
      playerLevel: 3,
      tourXp: 650,
      trophies: 6,
      unlockedCourses: ["cup_emerald_links", "cup_dunes_open"],
      unlockedAchievementIds: ["ach_first_drive"],
      completedChallengeIds: ["ch_1"],
      claimedChallengeIds: ["ch_1"],
      claimedFreeTiers: [1, 2],
      claimedVipTiers: [],
      isVipUnlocked: false,
      syncRevision: 2,
      updatedAt: validTimestamp(),
    })
  );

  // 5. game_settings
  await assertSucceeds(
    aliceDb.collection("game_settings").doc(ALICE_UID).set({
      userId: ALICE_UID,
      isSoundEnabled: true,
      soundVolume: 0.85,
      isHapticsEnabled: true,
      isCaddieAudioTipsEnabled: true,
      isHighFpsMode: true,
      isPuttingGridVisible: true,
      isSlopeBreakRadarVisible: true,
      isCloudSyncEnabled: true,
      graphicsTier: "FLAGSHIP_ULTRA",
      aiDifficulty: "PRO",
      updatedAt: validTimestamp(),
    })
  );

  await assertSucceeds(aliceDb.collection("players").doc(ALICE_UID).get());
  await assertSucceeds(aliceDb.collection("player_inventory").doc(ALICE_UID).get());
  await assertSucceeds(aliceDb.collection("game_settings").doc(ALICE_UID).get());
});

test("4. Shadow update with undeclared ghost field is rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("players").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "Alice",
      playerLevel: 1,
      xp: 0,
      coins: 1000,
      isAdmin: true,
      updatedAt: validTimestamp(),
    })
  );
});

test("5. match_history, achievements, daily_challenges, and reward_transactions enforce owner isolation and immutability", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // 6. match_history
  await assertSucceeds(
    aliceDb.collection("match_history").doc("match_001").set({
      userId: ALICE_UID,
      mode: "TOURNAMENT",
      courseName: "Zenith Links",
      holesPlayed: 6,
      totalStrokes: 21,
      scoreToPar: -3,
      scoreBanner: "BIRDIE! -1",
      coinsEarned: 450,
      xpEarned: 300,
      opponentName: "Masters Field",
      outcome: "WIN",
      createdAt: validTimestamp(),
    })
  );

  // Match history cannot be modified after creation
  await assertFails(
    aliceDb.collection("match_history").doc("match_001").update({
      totalStrokes: 10,
    })
  );

  // 9. achievements
  await assertSucceeds(
    aliceDb.collection("achievements").doc(`${ALICE_UID}_ach_eagle`).set({
      userId: ALICE_UID,
      achievementId: "ach_eagle",
      title: "Eagle Eye",
      description: "Sink an eagle",
      xpValue: 5000,
      isUnlocked: true,
      unlockedAt: validTimestamp(),
    })
  );

  // 10. daily_challenges
  await assertSucceeds(
    aliceDb.collection("daily_challenges").doc(`${ALICE_UID}_ch_1`).set({
      userId: ALICE_UID,
      challengeId: "ch_1",
      title: "Island Sniper",
      targetType: "ISLAND_ACCURACY",
      currentProgress: 1,
      targetGoal: 1,
      rewardCoins: 250,
      rewardMulligans: 1,
      isCompleted: true,
      isClaimed: true,
      updatedAt: validTimestamp(),
    })
  );

  // 11. reward_transactions (append-only ledger)
  await assertSucceeds(
    aliceDb.collection("reward_transactions").doc("tx_001").set({
      userId: ALICE_UID,
      rewardType: "DAILY_CHALLENGE",
      sourceId: "ch_1",
      coinsDelta: 250,
      xpDelta: 0,
      mulligansDelta: 1,
      createdAt: validTimestamp(),
    })
  );

  // Reward transactions cannot be updated or deleted, and out-of-range coinsDelta fails
  await assertFails(
    aliceDb.collection("reward_transactions").doc("tx_001").update({
      coinsDelta: 999999,
    })
  );
  await assertFails(
    aliceDb.collection("reward_transactions").doc("tx_tamper").set({
      userId: ALICE_UID,
      rewardType: "HACK",
      sourceId: "hack",
      coinsDelta: 9999999,
      xpDelta: 0,
      createdAt: validTimestamp(),
    })
  );

  // Bob cannot read Alice's reward_transactions or match_history
  await assertFails(bobDb.collection("reward_transactions").doc("tx_001").get());
  await assertFails(bobDb.collection("match_history").doc("match_001").get());
});

test("6. tournaments and tournament_entries prevent unauthorized score and reward tampering", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // 7. tournaments
  await assertSucceeds(
    aliceDb.collection("tournaments").doc("cup_emerald_links").set({
      tournamentId: "cup_emerald_links",
      title: "Emerald Links Open",
      courseName: "Zenith Championship Links",
      entryFeeCoins: 100,
      prizeCoins: 1500,
      prizeTrophies: 2,
      holesCount: 6,
      difficulty: "Pro",
      status: "OPEN",
      updatedAt: validTimestamp(),
    })
  );

  // 8. tournament_entries
  await assertSucceeds(
    aliceDb.collection("tournament_entries").doc(`cup_emerald_links_${ALICE_UID}`).set({
      userId: ALICE_UID,
      tournamentId: "cup_emerald_links",
      username: "Alice Pro",
      strokes: 20,
      scoreToPar: -4,
      holesCompleted: 6,
      rankPosition: 1,
      rewardCoinsAwarded: 1500,
      status: "COMPLETED",
      updatedAt: validTimestamp(),
    })
  );

  // Invalid tournament score (0 strokes or reward > 50,000) is rejected
  await assertFails(
    aliceDb.collection("tournament_entries").doc(`cup_emerald_links_invalid`).set({
      userId: ALICE_UID,
      tournamentId: "cup_emerald_links",
      username: "Alice Pro",
      strokes: 0,
      scoreToPar: -99,
      holesCompleted: 6,
      rewardCoinsAwarded: 999999,
      status: "COMPLETED",
      updatedAt: validTimestamp(),
    })
  );

  // Bob cannot modify Alice's tournament entry
  await assertFails(
    bobDb.collection("tournament_entries").doc(`cup_emerald_links_${ALICE_UID}`).update({
      strokes: 99,
      updatedAt: validTimestamp(),
    })
  );
});

test("7. Global leaderboards allow owner writes, reject cross-user writes, and support ordered list queries", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  await assertSucceeds(
    aliceDb.collection("leaderboards").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Pro",
      trophies: 12,
      shootoutHighScore: 2200,
      longestDriveYds: 304.2,
      playerLevel: 4,
      bestTournamentScoreToPar: -5,
      updatedAt: validTimestamp(),
    })
  );

  await assertSucceeds(
    aliceDb.collection("leaderboards").doc("global").collection("entries").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice Pro",
      trophies: 12,
      shootoutHighScore: 2200,
      longestDriveYds: 304.2,
      playerLevel: 4,
      bestTournamentScoreToPar: -5,
      updatedAt: validTimestamp(),
    })
  );

  // Bob cannot overwrite Alice's leaderboard entry
  await assertFails(
    bobDb.collection("leaderboards").doc("global").collection("entries").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Spoofed",
      trophies: 0,
      updatedAt: validTimestamp(),
    })
  );

  // Authenticated user can query top leaderboard entries with matching filter and order
  await assertSucceeds(
    bobDb
      .collection("leaderboards")
      .doc("global")
      .collection("entries")
      .where("trophies", ">=", 0)
      .orderBy("trophies", "desc")
      .limit(25)
      .get()
  );
});

test("8. tournament_registrations, rate_limits, and audit_logs enforce owner isolation, rate bounds, and append-only audit immutability", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Valid tournament registration
  await assertSucceeds(
    aliceDb.collection("tournament_registrations").doc(`cup_emerald_links_${ALICE_UID}`).set({
      userId: ALICE_UID,
      tournamentId: "cup_emerald_links",
      username: "Alice Pro",
      entryFeePaid: 100,
      trophiesAtEntry: 25,
      status: "REGISTERED",
      idempotencyKey: "reg_alice_001",
      registeredAt: validTimestamp(),
      updatedAt: validTimestamp(),
    })
  );

  // Bob cannot read or modify Alice's tournament registration
  await assertFails(
    bobDb.collection("tournament_registrations").doc(`cup_emerald_links_${ALICE_UID}`).get()
  );

  // Valid rate limit record (<= 60 requests in window)
  await assertSucceeds(
    aliceDb.collection("rate_limits").doc(ALICE_UID).set({
      userId: ALICE_UID,
      windowStartEpochMs: Date.now() - 5000,
      requestCount: 5,
      lastAction: "SUBMIT_MATCH_SCORE",
      updatedAt: validTimestamp(),
    })
  );

  // Rate limit record exceeding 60 requests is rejected by security rules
  await assertFails(
    aliceDb.collection("rate_limits").doc(ALICE_UID).set({
      userId: ALICE_UID,
      windowStartEpochMs: Date.now() - 5000,
      requestCount: 61,
      lastAction: "SUBMIT_MATCH_SCORE",
      updatedAt: validTimestamp(),
    })
  );

  // Append-only audit log
  await assertSucceeds(
    aliceDb.collection("audit_logs").doc("audit_001").set({
      actorId: ALICE_UID,
      targetUserId: ALICE_UID,
      actionType: "SCORE_SUBMITTED",
      resourceType: "match_history",
      resourceId: "match_001",
      status: "SUCCESS",
      metadataSummary: "Verified 6-hole tournament round",
      createdAt: validTimestamp(),
    })
  );

  // Audit logs are strictly immutable (cannot be updated or deleted)
  await assertFails(
    aliceDb.collection("audit_logs").doc("audit_001").update({
      status: "CLEARED",
    })
  );
  await assertFails(aliceDb.collection("audit_logs").doc("audit_001").delete());
});

test("9. anti_cheat_flags prevent players from clearing their own flags and allow Apex Dynamics administrators to review", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const adminDb = testEnv.authenticatedContext("apex_admin_01", { admin: true }).firestore();

  await assertSucceeds(
    aliceDb.collection("anti_cheat_flags").doc("flag_001").set({
      userId: ALICE_UID,
      matchId: "match_cheat_001",
      tournamentId: "cup_emerald_links",
      reasonCode: "DRIVE_SPEED_EXCEEDED",
      severity: "CRITICAL",
      details: "Drive distance 510 yds exceeds physical ceiling 430 yds",
      reportedStrokes: 18,
      reportedDriveYds: 510.0,
      roundDurationSec: 120,
      reviewStatus: "PENDING_REVIEW",
      createdAt: validTimestamp(),
      updatedAt: validTimestamp(),
    })
  );

  // Player cannot self-clear an anti-cheat flag
  await assertFails(
    aliceDb.collection("anti_cheat_flags").doc("flag_001").update({
      reviewStatus: "CLEARED",
      updatedAt: validTimestamp(),
    })
  );

  // Authorized Apex Dynamics Administrator can review and resolve the flag
  await assertSucceeds(
    adminDb.collection("anti_cheat_flags").doc("flag_001").update({
      reviewStatus: "CONFIRMED_CHEAT",
      reviewedBy: "apex_admin_01",
      reviewNotes: "Verified telemetry anomaly",
      updatedAt: validTimestamp(),
    })
  );
});

test("10. reward_configs, published_challenges, and studio_admins restrict write access strictly to Apex Dynamics administrators", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const adminDb = testEnv.authenticatedContext("apex_admin_01", { admin: true }).firestore();

  // Regular player cannot self-promote to studio_admins or modify reward_configs / published_challenges
  await assertFails(
    aliceDb.collection("studio_admins").doc(ALICE_UID).set({
      adminUid: ALICE_UID,
      role: "STUDIO_ADMIN",
      createdAt: validTimestamp(),
    })
  );
  await assertFails(
    aliceDb.collection("reward_configs").doc("default_season_config").set({
      configId: "default_season_config",
      holeCompletionBaseCoins: 5000,
      birdieBonusCoins: 5000,
      eagleBonusCoins: 10000,
      aceBonusCoins: 25000,
      tournamentFirstRatio: 1.0,
      tournamentSecondRatio: 0.5,
      tournamentThirdRatio: 0.35,
      updatedBy: ALICE_UID,
      updatedAt: validTimestamp(),
    })
  );

  // Authorized Apex Dynamics Administrator can configure rewards and publish challenges
  await assertSucceeds(
    adminDb.collection("reward_configs").doc("default_season_config").set({
      configId: "default_season_config",
      holeCompletionBaseCoins: 75,
      birdieBonusCoins: 100,
      eagleBonusCoins: 250,
      aceBonusCoins: 1000,
      tournamentFirstRatio: 0.5,
      tournamentSecondRatio: 0.3,
      tournamentThirdRatio: 0.2,
      updatedBy: "apex_admin_01",
      updatedAt: validTimestamp(),
    })
  );

  await assertSucceeds(
    adminDb.collection("published_challenges").doc("ch_weekend_masters").set({
      challengeId: "ch_weekend_masters",
      title: "Weekend Masters Birdie Hunt",
      description: "Score 3 Birdies in Championship play",
      targetType: "BIRDIE_COUNT",
      targetGoal: 3,
      rewardCoins: 600,
      rewardMulligans: 2,
      isActive: true,
      publishedBy: "apex_admin_01",
      updatedAt: validTimestamp(),
    })
  );

  // Authenticated player can read active reward config and published challenges
  await assertSucceeds(aliceDb.collection("reward_configs").doc("default_season_config").get());
  await assertSucceeds(aliceDb.collection("published_challenges").doc("ch_weekend_masters").get());
});

