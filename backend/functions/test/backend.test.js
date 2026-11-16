"use strict";

const { test, beforeEach } = require("node:test");
const assert = require("node:assert/strict");
const {
  dispatchHttpRequest,
  submitPlayerMatchScore,
  registerPlayerForTournament,
  submitTournamentEntryScore,
  verifyAndClaimAchievement,
  validateAndClaimDailyChallenge,
  adminCreateOrScheduleTournament,
  adminConfigureRewards,
  adminPublishChallenge,
  adminReviewSuspiciousMatch,
  adminGetTournamentStatistics,
} = require("../index");

/**
 * In-memory Firestore-compatible database adapter for fast, deterministic backend unit & HTTP dispatch testing.
 */
function createInMemoryFirestore() {
  const store = new Map();

  function getCollectionMap(colName) {
    if (!store.has(colName)) {
      store.set(colName, new Map());
    }
    return store.get(colName);
  }

  return {
    collection(colName) {
      const colMap = getCollectionMap(colName);
      return {
        doc(docId) {
          return {
            async get() {
              const exists = colMap.has(docId);
              const val = colMap.get(docId);
              return {
                id: docId,
                exists,
                data: () => (exists ? structuredClone(val) : undefined),
              };
            },
            async set(data) {
              colMap.set(docId, structuredClone(data));
            },
            async update(partial) {
              const prev = colMap.get(docId) || {};
              colMap.set(docId, { ...structuredClone(prev), ...structuredClone(partial) });
            },
          };
        },
        where(field, op, value) {
          return {
            async get() {
              const docs = [];
              for (const [id, data] of colMap.entries()) {
                if (op === "==" && data[field] === value) {
                  docs.push({
                    id,
                    exists: true,
                    data: () => structuredClone(data),
                  });
                }
              }
              return { docs };
            },
          };
        },
      };
    },
  };
}

function createMockAuthService(tokenMap) {
  return {
    async verifyIdToken(token) {
      if (!tokenMap[token]) {
        throw new Error("Token signature verification failed");
      }
      return tokenMap[token];
    },
  };
}

function invokeHttp(services, { method = "POST", path, token, body = {}, query = {} }) {
  return new Promise((resolve) => {
    const req = {
      method,
      path,
      headers: token ? { Authorization: `Bearer ${token}` } : {},
      body,
      query,
    };
    const res = {
      statusCode: 200,
      status(code) {
        this.statusCode = code;
        return this;
      },
      json(payload) {
        resolve({ status: this.statusCode, body: payload });
      },
    };
    dispatchHttpRequest(req, res, services);
  });
}

let db;
let services;
const ALICE_UID = "alice_pro_01";
const BOB_UID = "bob_contender_02";
const ADMIN_UID = "apex_director_99";

beforeEach(async () => {
  db = createInMemoryFirestore();
  const auth = createMockAuthService({
    "valid-token-alice": { uid: ALICE_UID, email: "alice@zenithgolf.test", admin: false },
    "valid-token-bob": { uid: BOB_UID, email: "bob@zenithgolf.test", admin: false },
    "valid-token-admin": { uid: ADMIN_UID, email: "director@apexdynamics.studio", admin: true },
  });
  services = { db, auth };

  // Seed initial player profiles
  await db.collection("players").doc(ALICE_UID).set({
    userId: ALICE_UID,
    username: "Alice Apex",
    playerLevel: 3,
    xp: 1200,
    coins: 2500,
    trophies: 40,
    mulligansRemaining: 3,
    updatedAt: new Date(),
  });
  await db.collection("players").doc(BOB_UID).set({
    userId: BOB_UID,
    username: "Bob Fairway",
    playerLevel: 2,
    xp: 600,
    coins: 1500,
    trophies: 25,
    mulligansRemaining: 2,
    updatedAt: new Date(),
  });
});

test("1. Unauthenticated and cross-user score submissions are rejected", async () => {
  const unauthRes = await invokeHttp(services, {
    path: "/v1/matches/submit",
    token: null,
    body: { userId: ALICE_UID, holesPlayed: 3, totalStrokes: 11, scoreToPar: -1 },
  });
  assert.equal(unauthRes.status, 401);
  assert.equal(unauthRes.body.error.code, "UNAUTHENTICATED");

  const crossUserRes = await invokeHttp(services, {
    path: "/v1/matches/submit",
    token: "valid-token-bob",
    body: { userId: ALICE_UID, holesPlayed: 3, totalStrokes: 11, scoreToPar: -1 },
  });
  assert.equal(crossUserRes.status, 403);
  assert.equal(crossUserRes.body.error.code, "PERMISSION_DENIED");
});

test("2. Valid match score submission verifies physics, calculates official rewards, updates stats & leaderboard, and rejects duplicate submission", async () => {
  const payload = {
    userId: ALICE_UID,
    mode: "QUICK_PLAY",
    courseName: "Emerald Links",
    holesPlayed: 3,
    holeStrokes: [3, 4, 3],
    holePars: [4, 4, 4],
    totalStrokes: 10,
    scoreToPar: -2,
    maxDriveYds: 308.5,
    roundDurationSec: 145,
    idempotencyKey: "round_alice_emerald_001",
  };

  const res = await invokeHttp(services, {
    path: "/v1/matches/submit",
    token: "valid-token-alice",
    body: payload,
  });

  assert.equal(res.status, 200);
  assert.equal(res.body.ok, true);
  assert.equal(res.body.data.verificationStatus, "VERIFIED");
  assert.ok(res.body.data.coinsEarned > 0);
  assert.ok(res.body.data.xpEarned > 0);

  // Verify player stats and leaderboard were updated on server
  const statsSnap = await db.collection("player_statistics").doc(ALICE_UID).get();
  assert.equal(statsSnap.data().holesPlayed, 3);
  assert.equal(statsSnap.data().birdies, 2);
  assert.equal(statsSnap.data().longestDriveYds, 308.5);

  const lbSnap = await db.collection("leaderboards").doc(ALICE_UID).get();
  assert.equal(lbSnap.data().bestTournamentScoreToPar, -2);

  // Reject duplicate submission of the same round
  const dupRes = await invokeHttp(services, {
    path: "/v1/matches/submit",
    token: "valid-token-alice",
    body: payload,
  });
  assert.equal(dupRes.status, 409);
  assert.equal(dupRes.body.error.code, "ALREADY_EXISTS");
});

test("3. Anti-Cheat Monitor flags and rejects impossible strokes, speed-hack drives, and duration anomalies", async () => {
  const cheatRes = await invokeHttp(services, {
    path: "/v1/matches/submit",
    token: "valid-token-alice",
    body: {
      userId: ALICE_UID,
      mode: "QUICK_PLAY",
      courseName: "Dunes Open",
      holesPlayed: 6,
      totalStrokes: 6,
      scoreToPar: -18,
      maxDriveYds: 590.0, // Exceeds 430 yd physical ceiling
      roundDurationSec: 12, // < 8s per hole
      idempotencyKey: "cheat_attempt_01",
    },
  });

  assert.equal(cheatRes.status, 422);
  assert.equal(cheatRes.body.error.code, "FAILED_PRECONDITION");
  const flagId = cheatRes.body.error.details.flagId;
  assert.ok(flagId);

  const flagDoc = await db.collection("anti_cheat_flags").doc(flagId).get();
  assert.equal(flagDoc.exists, true);
  assert.equal(flagDoc.data().userId, ALICE_UID);
  assert.equal(flagDoc.data().reviewStatus, "PENDING_REVIEW");
});

test("4. Tournament lifecycle: Admin creation, Player registration, Score verification, Official Leaderboard & Prize distribution", async () => {
  // Non-admin cannot create tournament
  const forbiddenAdmin = await invokeHttp(services, {
    path: "/v1/admin/tournaments",
    token: "valid-token-alice",
    body: { tournamentId: "cup_masters_2026", title: "Zenith Masters" },
  });
  assert.equal(forbiddenAdmin.status, 403);

  // Apex Dynamics Admin creates official tournament
  const createRes = await invokeHttp(services, {
    path: "/v1/admin/tournaments",
    token: "valid-token-admin",
    body: {
      tournamentId: "cup_masters_2026",
      title: "Zenith Masters Championship",
      courseName: "Royal Pines Championship",
      entryFeeCoins: 500,
      prizeCoins: 4000,
      prizeTrophies: 50,
      holesCount: 3,
      difficulty: "CHAMPIONSHIP",
      status: "OPEN",
      requiredTrophies: 20,
    },
  });
  assert.equal(createRes.status, 200);

  // Alice and Bob register for tournament
  const regAlice = await invokeHttp(services, {
    path: "/v1/tournaments/register",
    token: "valid-token-alice",
    body: { userId: ALICE_UID, tournamentId: "cup_masters_2026" },
  });
  assert.equal(regAlice.status, 200);
  assert.equal(regAlice.body.data.remainingCoins, 2000);

  // Duplicate registration is rejected
  const dupRegAlice = await invokeHttp(services, {
    path: "/v1/tournaments/register",
    token: "valid-token-alice",
    body: { userId: ALICE_UID, tournamentId: "cup_masters_2026" },
  });
  assert.equal(dupRegAlice.status, 409);

  const regBob = await invokeHttp(services, {
    path: "/v1/tournaments/register",
    token: "valid-token-bob",
    body: { userId: BOB_UID, tournamentId: "cup_masters_2026" },
  });
  assert.equal(regBob.status, 200);

  // Bob submits 3-hole tournament score (-1)
  const scoreBob = await invokeHttp(services, {
    path: "/v1/tournaments/submit-score",
    token: "valid-token-bob",
    body: {
      userId: BOB_UID,
      tournamentId: "cup_masters_2026",
      holesPlayed: 3,
      holeStrokes: [4, 3, 4],
      holePars: [4, 4, 4],
      totalStrokes: 11,
      scoreToPar: -1,
      maxDriveYds: 285.0,
      roundDurationSec: 120,
    },
  });
  assert.equal(scoreBob.status, 200);
  assert.equal(scoreBob.body.data.rankPosition, 1);

  // Alice submits better 3-hole tournament score (-3) and takes 1st place
  const scoreAlice = await invokeHttp(services, {
    path: "/v1/tournaments/submit-score",
    token: "valid-token-alice",
    body: {
      userId: ALICE_UID,
      tournamentId: "cup_masters_2026",
      holesPlayed: 3,
      holeStrokes: [3, 3, 3],
      holePars: [4, 4, 4],
      totalStrokes: 9,
      scoreToPar: -3,
      maxDriveYds: 315.0,
      roundDurationSec: 130,
    },
  });
  assert.equal(scoreAlice.status, 200);
  assert.equal(scoreAlice.body.data.rankPosition, 1);
  assert.equal(scoreAlice.body.data.rewardCoinsAwarded, 2000); // 50% of 4000 prize pool

  // Duplicate tournament score submission is rejected
  const dupScoreAlice = await invokeHttp(services, {
    path: "/v1/tournaments/submit-score",
    token: "valid-token-alice",
    body: {
      userId: ALICE_UID,
      tournamentId: "cup_masters_2026",
      holesPlayed: 3,
      holeStrokes: [3, 3, 3],
      holePars: [4, 4, 4],
      totalStrokes: 9,
      scoreToPar: -3,
      maxDriveYds: 315.0,
      roundDurationSec: 130,
    },
  });
  assert.equal(dupScoreAlice.status, 409);

  // Admin queries official tournament statistics
  const statsRes = await invokeHttp(services, {
    method: "GET",
    path: "/v1/admin/tournaments/stats",
    token: "valid-token-admin",
    query: { tournamentId: "cup_masters_2026" },
  });
  assert.equal(statsRes.status, 200);
  assert.equal(statsRes.body.data.totalEntrants, 2);
  assert.equal(statsRes.body.data.bestScoreToPar, -3);
});

test("5. Achievement verification and Daily Challenge claim validation prevent premature or duplicate claims", async () => {
  // Alice has not yet recorded a 250+ yd drive in stats
  const prematureAch = await invokeHttp(services, {
    path: "/v1/achievements/claim",
    token: "valid-token-alice",
    body: { userId: ALICE_UID, achievementId: "ach_first_drive" },
  });
  assert.equal(prematureAch.status, 422);

  // Seed verified stats with 295 yd drive
  await db.collection("player_statistics").doc(ALICE_UID).set({
    userId: ALICE_UID,
    roundsPlayed: 2,
    holesPlayed: 6,
    birdies: 3,
    eagles: 1,
    aces: 0,
    longestDriveYds: 295.0,
    updatedAt: new Date(),
  });

  const claimAch = await invokeHttp(services, {
    path: "/v1/achievements/claim",
    token: "valid-token-alice",
    body: { userId: ALICE_UID, achievementId: "ach_first_drive" },
  });
  assert.equal(claimAch.status, 200);
  assert.equal(claimAch.body.data.xpAwarded, 1000);

  // Duplicate achievement claim rejected
  const dupAch = await invokeHttp(services, {
    path: "/v1/achievements/claim",
    token: "valid-token-alice",
    body: { userId: ALICE_UID, achievementId: "ach_first_drive" },
  });
  assert.equal(dupAch.status, 409);

  // Daily challenge validation
  await db.collection("daily_challenges").doc(`${ALICE_UID}_ch_1`).set({
    userId: ALICE_UID,
    challengeId: "ch_1",
    title: "Island Sniper",
    targetType: "ISLAND_ACCURACY",
    currentProgress: 1,
    targetGoal: 1,
    rewardCoins: 350,
    rewardMulligans: 1,
    isCompleted: true,
    isClaimed: false,
    updatedAt: new Date(),
  });

  const claimCh = await invokeHttp(services, {
    path: "/v1/challenges/claim",
    token: "valid-token-alice",
    body: { userId: ALICE_UID, challengeId: "ch_1" },
  });
  assert.equal(claimCh.status, 200);
  assert.equal(claimCh.body.data.rewardCoins, 350);

  // Duplicate challenge claim rejected
  const dupCh = await invokeHttp(services, {
    path: "/v1/challenges/claim",
    token: "valid-token-alice",
    body: { userId: ALICE_UID, challengeId: "ch_1" },
  });
  assert.equal(dupCh.status, 409);
});

test("6. Admin reward configuration, challenge publishing, and anti-cheat flag review", async () => {
  const cfgRes = await invokeHttp(services, {
    path: "/v1/admin/rewards/config",
    token: "valid-token-admin",
    body: {
      configId: "default_season_config",
      holeCompletionBaseCoins: 80,
      birdieBonusCoins: 100,
      eagleBonusCoins: 250,
      aceBonusCoins: 1000,
    },
  });
  assert.equal(cfgRes.status, 200);
  assert.equal(cfgRes.body.data.holeCompletionBaseCoins, 80);

  const pubRes = await invokeHttp(services, {
    path: "/v1/admin/challenges/publish",
    token: "valid-token-admin",
    body: {
      challengeId: "ch_weekend_eagle",
      title: "Weekend Eagle Challenge",
      description: "Score an Eagle on any Par 5 hole",
      targetType: "EAGLE_COUNT",
      targetGoal: 1,
      rewardCoins: 600,
      rewardMulligans: 2,
      isActive: true,
    },
  });
  assert.equal(pubRes.status, 200);
  assert.equal(pubRes.body.data.challengeId, "ch_weekend_eagle");

  // Trigger an anti-cheat flag and review it as Admin
  const cheatRes = await invokeHttp(services, {
    path: "/v1/matches/submit",
    token: "valid-token-bob",
    body: {
      userId: BOB_UID,
      holesPlayed: 3,
      totalStrokes: 1,
      scoreToPar: -11,
      maxDriveYds: 300,
      roundDurationSec: 90,
    },
  });
  assert.equal(cheatRes.status, 422);
  const flagId = cheatRes.body.error.details.flagId;

  const reviewRes = await invokeHttp(services, {
    path: "/v1/admin/anti-cheat/review",
    token: "valid-token-admin",
    body: {
      flagId,
      reviewStatus: "CONFIRMED_CHEAT",
      reviewNotes: "Confirmed impossible stroke count (1 stroke across 3 holes).",
    },
  });
  assert.equal(reviewRes.status, 200);
  assert.equal(reviewRes.body.data.reviewStatus, "CONFIRMED_CHEAT");
});
