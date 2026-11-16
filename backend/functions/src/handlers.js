"use strict";

const {
  DEFAULT_REWARD_CONFIG,
  BackendServiceError,
  verifyAuthAndOwnership,
  verifyAdminAccess,
  evaluateRateLimit,
  computeSubmissionHash,
  verifyGolfScoreAndAntiCheat,
  calculateOfficialMatchRewards,
  calculateTournamentStandingsAndPrizes,
  verifyAchievementEligibility,
} = require("./validators");

function resolveTimestamp(db, explicitTimestamp) {
  if (explicitTimestamp) return explicitTimestamp;
  if (db && db._firestoreClient && db.constructor && db.constructor.FieldValue) {
    return db.constructor.FieldValue.serverTimestamp();
  }
  return new Date();
}

async function getDocData(docRef) {
  const snap = await docRef.get();
  const exists = typeof snap.exists === "function" ? snap.exists() : Boolean(snap.exists);
  return exists ? snap.data() : null;
}

async function checkAndRecordRateLimit(db, userId, actionName, nowTs) {
  const rateRef = db.collection("rate_limits").doc(userId);
  const existing = await getDocData(rateRef);
  const decision = evaluateRateLimit(existing, Date.now(), actionName);
  if (!decision.allowed) {
    throw new BackendServiceError(
      "RESOURCE_EXHAUSTED",
      429,
      `Rate limit exceeded for player ${userId}. Retry after ${decision.retryAfterMs}ms.`,
      { retryAfterMs: decision.retryAfterMs }
    );
  }
  await rateRef.set({
    userId,
    windowStartEpochMs: decision.nextRecord.windowStartEpochMs,
    requestCount: decision.nextRecord.requestCount,
    lastAction: decision.nextRecord.lastAction,
    updatedAt: resolveTimestamp(db, nowTs),
  });
}

async function appendAuditLog(db, entry, nowTs) {
  const logId = `audit_${entry.actorId}_${Date.now()}_${Math.random().toString(36).slice(2, 8)}`;
  const record = {
    actorId: entry.actorId,
    targetUserId: entry.targetUserId || entry.actorId,
    actionType: entry.actionType,
    resourceType: entry.resourceType,
    resourceId: entry.resourceId,
    status: entry.status,
    metadataSummary: String(entry.metadataSummary || "Verified operation").slice(0, 300),
    createdAt: resolveTimestamp(db, nowTs),
  };
  await db.collection("audit_logs").doc(logId).set(record);
  return { logId, ...record };
}

async function loadActiveRewardConfig(db) {
  const cfgDoc = await getDocData(db.collection("reward_configs").doc("default_season_config"));
  return cfgDoc ? { ...DEFAULT_REWARD_CONFIG, ...cfgDoc } : DEFAULT_REWARD_CONFIG;
}

/**
 * 1. Secure Player Score Submission, Score Verification, Anti-Cheat Monitoring,
 *    Reward Calculation, Match History & Official Leaderboard Update.
 */
async function submitPlayerMatchScore(db, authContext, payload, nowTs = null) {
  const userId = verifyAuthAndOwnership(authContext, payload?.userId);
  await checkAndRecordRateLimit(db, userId, "SUBMIT_MATCH_SCORE", nowTs);

  const mode = String(payload.mode || "QUICK_PLAY").slice(0, 40);
  const courseName = String(payload.courseName || "Emerald Links").slice(0, 80);
  const opponentName = String(payload.opponentName || "Solo Tour").slice(0, 80);
  const outcome = String(payload.outcome || "COMPLETED").slice(0, 32);

  const submissionHash = computeSubmissionHash(
    userId,
    `${mode}:${courseName}`,
    payload.holeStrokes,
    payload.totalStrokes,
    payload.idempotencyKey
  );
  const matchId = payload.matchId
    ? String(payload.matchId).replace(/[^a-zA-Z0-9_-]/g, "")
    : `match_${userId}_${submissionHash.slice(0, 24)}`;

  // Reject duplicate score submissions
  const existingMatch = await getDocData(db.collection("match_history").doc(matchId));
  if (existingMatch) {
    await appendAuditLog(
      db,
      {
        actorId: userId,
        targetUserId: userId,
        actionType: "SCORE_SUBMITTED",
        resourceType: "match_history",
        resourceId: matchId,
        status: "REJECTED",
        metadataSummary: `Duplicate score submission rejected for matchId=${matchId}`,
      },
      nowTs
    );
    throw new BackendServiceError(
      "ALREADY_EXISTS",
      409,
      `Duplicate score submission rejected for match ${matchId}.`
    );
  }

  // Run physics & anti-cheat verification
  const verification = verifyGolfScoreAndAntiCheat(payload);
  if (!verification.isValid) {
    const firstViolation = verification.violations[0];
    const flagId = `flag_${userId}_${Date.now()}`;
    await db.collection("anti_cheat_flags").doc(flagId).set({
      userId,
      matchId,
      tournamentId: payload.tournamentId || null,
      reasonCode: firstViolation.reasonCode,
      severity: firstViolation.severity,
      details: firstViolation.details.slice(0, 300),
      reportedStrokes: Number(payload.totalStrokes || 0),
      reportedDriveYds: Number(payload.maxDriveYds || 0),
      roundDurationSec: Number(payload.roundDurationSec || 0),
      reviewStatus: "PENDING_REVIEW",
      reviewedBy: null,
      reviewNotes: null,
      createdAt: resolveTimestamp(db, nowTs),
      updatedAt: resolveTimestamp(db, nowTs),
    });

    await appendAuditLog(
      db,
      {
        actorId: userId,
        targetUserId: userId,
        actionType: "SCORE_SUBMITTED",
        resourceType: "anti_cheat_flags",
        resourceId: flagId,
        status: "FLAGGED",
        metadataSummary: `Anti-cheat flagged match ${matchId}: ${firstViolation.reasonCode} (${firstViolation.details})`,
      },
      nowTs
    );

    throw new BackendServiceError(
      "FAILED_PRECONDITION",
      422,
      `Score rejected by Zenith Anti-Cheat Monitor: ${firstViolation.details}`,
      { flagId, violations: verification.violations }
    );
  }

  const rewardConfig = await loadActiveRewardConfig(db);
  const officialRewards = calculateOfficialMatchRewards(verification, mode, rewardConfig);
  const ts = resolveTimestamp(db, nowTs);

  // 1. Write immutable verified MatchHistoryRecord
  const matchRecord = {
    userId,
    mode,
    courseName,
    holesPlayed: verification.holesPlayed,
    totalStrokes: verification.totalStrokes,
    scoreToPar: verification.scoreToPar,
    scoreBanner: verification.scoreBanner,
    coinsEarned: officialRewards.coinsEarned,
    xpEarned: officialRewards.xpEarned,
    opponentName,
    outcome,
    idempotencyKey: payload.idempotencyKey || submissionHash,
    holeStrokes: verification.holeStrokes,
    holePars: verification.holePars,
    maxDriveYds: verification.maxDriveYds,
    roundDurationSec: verification.roundDurationSec,
    verificationStatus: "VERIFIED",
    createdAt: ts,
  };
  await db.collection("match_history").doc(matchId).set(matchRecord);

  // 2. Update PlayerDocument & PlayerStatistics
  const playerRef = db.collection("players").doc(userId);
  const existingPlayer = (await getDocData(playerRef)) || {
    userId,
    username: payload.username || "Tour Pro",
    playerLevel: 1,
    xp: 0,
    coins: 1500,
    trophies: 0,
    mulligansRemaining: 3,
  };
  const nextXp = Number(existingPlayer.xp || 0) + officialRewards.xpEarned;
  const nextLevel = Math.min(500, Math.max(1, Math.floor(nextXp / 500) + 1));
  const nextCoins = Number(existingPlayer.coins || 0) + officialRewards.coinsEarned;
  const nextTrophies = Number(existingPlayer.trophies || 0) + officialRewards.trophiesDelta;

  await playerRef.set({
    userId,
    username: String(existingPlayer.username || payload.username || "Tour Pro").slice(0, 80),
    profileImageUrl: existingPlayer.profileImageUrl || null,
    email: existingPlayer.email || null,
    playerLevel: nextLevel,
    xp: nextXp,
    coins: nextCoins,
    trophies: nextTrophies,
    mulligansRemaining: Number(existingPlayer.mulligansRemaining ?? 3),
    countryTag: existingPlayer.countryTag || "GLOBAL",
    accountTier: existingPlayer.accountTier || "GOOGLE_LINKED",
    clientUpdatedAtEpochMs: Date.now(),
    updatedAt: ts,
  });

  const statsRef = db.collection("player_statistics").doc(userId);
  const existingStats = (await getDocData(statsRef)) || {
    roundsPlayed: 0,
    holesPlayed: 0,
    birdies: 0,
    eagles: 0,
    aces: 0,
    longestDriveYds: 0,
    greensInRegulationCount: 0,
    shootoutHighScore: 0,
    matchPlayWins: 0,
    matchPlayLosses: 0,
  };
  const nextLongestDrive = Math.max(
    Number(existingStats.longestDriveYds || 0),
    verification.maxDriveYds
  );
  await statsRef.set({
    userId,
    roundsPlayed: Number(existingStats.roundsPlayed || 0) + 1,
    holesPlayed: Number(existingStats.holesPlayed || 0) + verification.holesPlayed,
    birdies: Number(existingStats.birdies || 0) + verification.birdies,
    eagles: Number(existingStats.eagles || 0) + verification.eagles,
    aces: Number(existingStats.aces || 0) + verification.aces,
    longestDriveYds: nextLongestDrive,
    greensInRegulationCount:
      Number(existingStats.greensInRegulationCount || 0) +
      (verification.scoreToPar <= 0 ? verification.holesPlayed : 0),
    shootoutHighScore: Number(existingStats.shootoutHighScore || 0),
    matchPlayWins:
      Number(existingStats.matchPlayWins || 0) + (mode === "MATCH_PLAY" && outcome === "WIN" ? 1 : 0),
    matchPlayLosses:
      Number(existingStats.matchPlayLosses || 0) + (mode === "MATCH_PLAY" && outcome === "LOSS" ? 1 : 0),
    updatedAt: ts,
  });

  // 3. Record RewardTransaction
  const txId = `tx_${userId}_${matchId}`;
  await db.collection("reward_transactions").doc(txId).set({
    userId,
    rewardType: "HOLE_COMPLETION",
    sourceId: matchId,
    coinsDelta: officialRewards.coinsEarned,
    xpDelta: officialRewards.xpEarned,
    mulligansDelta: 0,
    createdAt: ts,
  });

  // 4. Update Official Leaderboard Entry
  const lbRef = db.collection("leaderboards").doc(userId);
  const existingLb = await getDocData(lbRef);
  const prevBestScore =
    existingLb && typeof existingLb.bestTournamentScoreToPar === "number"
      ? existingLb.bestTournamentScoreToPar
      : 99;
  await lbRef.set({
    userId,
    displayName: String(existingPlayer.username || payload.username || "Tour Pro").slice(0, 80),
    trophies: nextTrophies,
    shootoutHighScore: Number(existingStats.shootoutHighScore || 0),
    longestDriveYds: nextLongestDrive,
    playerLevel: nextLevel,
    bestTournamentScoreToPar: Math.min(prevBestScore, verification.scoreToPar),
    updatedAt: ts,
  });

  // 5. Append Audit Log
  await appendAuditLog(
    db,
    {
      actorId: userId,
      targetUserId: userId,
      actionType: "SCORE_SUBMITTED",
      resourceType: "match_history",
      resourceId: matchId,
      status: "SUCCESS",
      metadataSummary: `Verified ${mode} on ${courseName}: strokes=${verification.totalStrokes} (${verification.scoreToPar}), +${officialRewards.coinsEarned} coins`,
    },
    nowTs
  );

  return {
    matchId,
    verificationStatus: "VERIFIED",
    scoreBanner: verification.scoreBanner,
    coinsEarned: officialRewards.coinsEarned,
    xpEarned: officialRewards.xpEarned,
    trophiesDelta: officialRewards.trophiesDelta,
    newCoinsBalance: nextCoins,
    newXpTotal: nextXp,
    newTrophiesTotal: nextTrophies,
  };
}

/**
 * 2. Tournament Registration (Validates tournament state, trophy requirement,
 *    entry fee balance, prevents duplicate registration, deducts fee & logs).
 */
async function registerPlayerForTournament(db, authContext, payload, nowTs = null) {
  const userId = verifyAuthAndOwnership(authContext, payload?.userId);
  await checkAndRecordRateLimit(db, userId, "TOURNAMENT_REGISTER", nowTs);

  const tournamentId = String(payload?.tournamentId || "").trim();
  if (!tournamentId) {
    throw new BackendServiceError("INVALID_ARGUMENT", 400, "tournamentId is required.");
  }

  const tournamentDoc = await getDocData(db.collection("tournaments").doc(tournamentId));
  if (!tournamentDoc) {
    throw new BackendServiceError("NOT_FOUND", 404, `Tournament ${tournamentId} does not exist.`);
  }
  if (!["OPEN", "ACTIVE"].includes(tournamentDoc.status)) {
    throw new BackendServiceError(
      "FAILED_PRECONDITION",
      422,
      `Tournament ${tournamentId} is not open for registration (status=${tournamentDoc.status}).`
    );
  }

  const regId = `${tournamentId}_${userId}`;
  const regRef = db.collection("tournament_registrations").doc(regId);
  const existingReg = await getDocData(regRef);
  if (existingReg) {
    throw new BackendServiceError(
      "ALREADY_EXISTS",
      409,
      `Player ${userId} is already registered for tournament ${tournamentId}.`
    );
  }

  const playerRef = db.collection("players").doc(userId);
  const playerDoc = await getDocData(playerRef);
  if (!playerDoc) {
    throw new BackendServiceError("NOT_FOUND", 404, `Player profile ${userId} not found.`);
  }

  const requiredTrophies = Number(tournamentDoc.requiredTrophies || 0);
  const playerTrophies = Number(playerDoc.trophies || 0);
  if (playerTrophies < requiredTrophies) {
    throw new BackendServiceError(
      "FAILED_PRECONDITION",
      422,
      `Insufficient trophies for ${tournamentDoc.title}: required ${requiredTrophies}, player has ${playerTrophies}.`
    );
  }

  const entryFee = Number(tournamentDoc.entryFeeCoins || 0);
  const playerCoins = Number(playerDoc.coins || 0);
  if (playerCoins < entryFee) {
    throw new BackendServiceError(
      "FAILED_PRECONDITION",
      422,
      `Insufficient coins for tournament entry fee: required ${entryFee}, player has ${playerCoins}.`
    );
  }

  const ts = resolveTimestamp(db, nowTs);
  const updatedCoins = playerCoins - entryFee;

  await playerRef.set({
    ...playerDoc,
    coins: updatedCoins,
    updatedAt: ts,
  });

  const registrationRecord = {
    userId,
    tournamentId,
    username: String(playerDoc.username || payload.username || "Tour Pro").slice(0, 80),
    entryFeePaid: entryFee,
    trophiesAtEntry: playerTrophies,
    status: "REGISTERED",
    idempotencyKey: payload.idempotencyKey || regId,
    registeredAt: ts,
    updatedAt: ts,
  };
  await regRef.set(registrationRecord);

  if (entryFee > 0) {
    await db.collection("reward_transactions").doc(`tx_fee_${regId}`).set({
      userId,
      rewardType: "TOURNAMENT_ENTRY_FEE",
      sourceId: tournamentId,
      coinsDelta: -entryFee,
      xpDelta: 0,
      mulligansDelta: 0,
      createdAt: ts,
    });
  }

  await appendAuditLog(
    db,
    {
      actorId: userId,
      targetUserId: userId,
      actionType: "TOURNAMENT_REGISTERED",
      resourceType: "tournament_registrations",
      resourceId: regId,
      status: "SUCCESS",
      metadataSummary: `Registered for ${tournamentId}, entryFeePaid=${entryFee}, remainingCoins=${updatedCoins}`,
    },
    nowTs
  );

  return {
    registrationId: regId,
    tournamentId,
    status: "REGISTERED",
    entryFeePaid: entryFee,
    remainingCoins: updatedCoins,
  };
}

/**
 * 3. Tournament Entry Validation, Official Score Verification, Official Leaderboard
 *    Calculation & Server-Controlled Prize Distribution.
 */
async function submitTournamentEntryScore(db, authContext, payload, nowTs = null) {
  const userId = verifyAuthAndOwnership(authContext, payload?.userId);
  await checkAndRecordRateLimit(db, userId, "TOURNAMENT_SUBMIT_SCORE", nowTs);

  const tournamentId = String(payload?.tournamentId || "").trim();
  if (!tournamentId) {
    throw new BackendServiceError("INVALID_ARGUMENT", 400, "tournamentId is required.");
  }

  const tournamentDoc = await getDocData(db.collection("tournaments").doc(tournamentId));
  if (!tournamentDoc) {
    throw new BackendServiceError("NOT_FOUND", 404, `Tournament ${tournamentId} not found.`);
  }

  const regId = `${tournamentId}_${userId}`;
  const regRef = db.collection("tournament_registrations").doc(regId);
  const regDoc = await getDocData(regRef);
  if (!regDoc) {
    throw new BackendServiceError(
      "FAILED_PRECONDITION",
      422,
      `Player ${userId} must register for tournament ${tournamentId} before submitting a scorecard.`
    );
  }
  if (regDoc.status === "SCORED") {
    throw new BackendServiceError(
      "ALREADY_EXISTS",
      409,
      `Official scorecard for tournament ${tournamentId} has already been submitted and locked.`
    );
  }
  if (regDoc.status === "DISQUALIFIED") {
    throw new BackendServiceError(
      "PERMISSION_DENIED",
      403,
      `Player registration for tournament ${tournamentId} was disqualified by anti-cheat.`
    );
  }

  if (Number(payload.holesPlayed) !== Number(tournamentDoc.holesCount)) {
    throw new BackendServiceError(
      "INVALID_ARGUMENT",
      400,
      `Tournament ${tournamentId} requires ${tournamentDoc.holesCount} holes; received ${payload.holesPlayed}.`
    );
  }

  const ts = resolveTimestamp(db, nowTs);
  const verification = verifyGolfScoreAndAntiCheat(payload);

  if (!verification.isValid) {
    const v = verification.violations[0];
    const flagId = `flag_tour_${regId}_${Date.now()}`;
    await db.collection("anti_cheat_flags").doc(flagId).set({
      userId,
      matchId: regId,
      tournamentId,
      reasonCode: v.reasonCode,
      severity: v.severity,
      details: v.details.slice(0, 300),
      reportedStrokes: Number(payload.totalStrokes || 0),
      reportedDriveYds: Number(payload.maxDriveYds || 0),
      roundDurationSec: Number(payload.roundDurationSec || 0),
      reviewStatus: "PENDING_REVIEW",
      reviewedBy: null,
      reviewNotes: null,
      createdAt: ts,
      updatedAt: ts,
    });

    await regRef.set({
      ...regDoc,
      status: "DISQUALIFIED",
      updatedAt: ts,
    });

    await appendAuditLog(
      db,
      {
        actorId: userId,
        targetUserId: userId,
        actionType: "TOURNAMENT_SCORED",
        resourceType: "anti_cheat_flags",
        resourceId: flagId,
        status: "FLAGGED",
        metadataSummary: `Disqualified tournament entry ${regId}: ${v.reasonCode}`,
      },
      nowTs
    );

    throw new BackendServiceError(
      "FAILED_PRECONDITION",
      422,
      `Tournament score rejected by Anti-Cheat Monitor: ${v.details}`,
      { flagId, violations: verification.violations }
    );
  }

  const rewardConfig = await loadActiveRewardConfig(db);
  const submissionHash = computeSubmissionHash(
    userId,
    tournamentId,
    verification.holeStrokes,
    verification.totalStrokes,
    payload.idempotencyKey
  );

  // Gather existing tournament entries to compute official server-controlled rank & prize
  let existingEntries = [];
  try {
    const snap = await db
      .collection("tournament_entries")
      .where("tournamentId", "==", tournamentId)
      .get();
    if (snap && Array.isArray(snap.docs)) {
      existingEntries = snap.docs.map((d) => d.data());
    }
  } catch (_err) {
    existingEntries = [];
  }

  const candidateEntry = {
    userId,
    tournamentId,
    username: regDoc.username,
    strokes: verification.totalStrokes,
    scoreToPar: verification.scoreToPar,
    holesCompleted: verification.holesPlayed,
    status: "FINALIZED",
    holeStrokes: verification.holeStrokes,
    submissionHash,
    verificationStatus: "VERIFIED",
    updatedAt: ts,
  };

  const mergedEntries = [
    ...existingEntries.filter((e) => e.userId !== userId),
    candidateEntry,
  ];
  const standings = calculateTournamentStandingsAndPrizes(
    mergedEntries,
    tournamentDoc,
    rewardConfig
  );
  const playerStanding = standings.find((s) => s.userId === userId) || {
    ...candidateEntry,
    rankPosition: 1,
    rewardCoinsAwarded: Number(tournamentDoc.prizeCoins || 1000),
    rewardTrophiesAwarded: Number(tournamentDoc.prizeTrophies || 10),
  };

  const finalEntryRecord = {
    userId,
    tournamentId,
    username: regDoc.username,
    strokes: verification.totalStrokes,
    scoreToPar: verification.scoreToPar,
    holesCompleted: verification.holesPlayed,
    rankPosition: playerStanding.rankPosition,
    rewardCoinsAwarded: playerStanding.rewardCoinsAwarded,
    status: "FINALIZED",
    holeStrokes: verification.holeStrokes,
    submissionHash,
    verificationStatus: "VERIFIED",
    updatedAt: ts,
  };

  await db.collection("tournament_entries").doc(regId).set(finalEntryRecord);
  await regRef.set({
    ...regDoc,
    status: "SCORED",
    updatedAt: ts,
  });

  // Credit server-controlled tournament prize to player
  const playerRef = db.collection("players").doc(userId);
  const playerDoc = (await getDocData(playerRef)) || {
    userId,
    username: regDoc.username,
    playerLevel: 1,
    xp: 0,
    coins: 0,
    trophies: 0,
  };
  const xpAwarded = verification.holesPlayed * 100;
  const newCoins = Number(playerDoc.coins || 0) + playerStanding.rewardCoinsAwarded;
  const newTrophies = Number(playerDoc.trophies || 0) + playerStanding.rewardTrophiesAwarded;
  const newXp = Number(playerDoc.xp || 0) + xpAwarded;
  const newLevel = Math.min(500, Math.max(1, Math.floor(newXp / 500) + 1));

  await playerRef.set({
    ...playerDoc,
    coins: newCoins,
    trophies: newTrophies,
    xp: newXp,
    playerLevel: newLevel,
    updatedAt: ts,
  });

  await db.collection("reward_transactions").doc(`tx_prize_${regId}`).set({
    userId,
    rewardType: "TOURNAMENT_PRIZE",
    sourceId: tournamentId,
    coinsDelta: playerStanding.rewardCoinsAwarded,
    xpDelta: xpAwarded,
    mulligansDelta: 0,
    createdAt: ts,
  });

  await db.collection("leaderboards").doc(userId).set({
    userId,
    displayName: regDoc.username,
    trophies: newTrophies,
    shootoutHighScore: 0,
    longestDriveYds: verification.maxDriveYds,
    playerLevel: newLevel,
    bestTournamentScoreToPar: verification.scoreToPar,
    updatedAt: ts,
  });

  await appendAuditLog(
    db,
    {
      actorId: userId,
      targetUserId: userId,
      actionType: "TOURNAMENT_SCORED",
      resourceType: "tournament_entries",
      resourceId: regId,
      status: "SUCCESS",
      metadataSummary: `Official entry ${regId} finalized: rank=${playerStanding.rankPosition}, scoreToPar=${verification.scoreToPar}, prize=${playerStanding.rewardCoinsAwarded}`,
    },
    nowTs
  );

  return {
    entryId: regId,
    tournamentId,
    rankPosition: playerStanding.rankPosition,
    rewardCoinsAwarded: playerStanding.rewardCoinsAwarded,
    rewardTrophiesAwarded: playerStanding.rewardTrophiesAwarded,
    xpAwarded,
    verificationStatus: "VERIFIED",
  };
}

/**
 * 4. Achievement Verification & Reward Claim.
 */
async function verifyAndClaimAchievement(db, authContext, payload, nowTs = null) {
  const userId = verifyAuthAndOwnership(authContext, payload?.userId);
  await checkAndRecordRateLimit(db, userId, "CLAIM_ACHIEVEMENT", nowTs);

  const achievementId = String(payload?.achievementId || "").trim();
  if (!achievementId) {
    throw new BackendServiceError("INVALID_ARGUMENT", 400, "achievementId is required.");
  }

  const achDocId = `${userId}_${achievementId}`;
  const achRef = db.collection("achievements").doc(achDocId);
  const existingAch = await getDocData(achRef);
  if (existingAch && existingAch.isUnlocked) {
    throw new BackendServiceError(
      "ALREADY_EXISTS",
      409,
      `Achievement ${achievementId} has already been verified and claimed.`
    );
  }

  const statsDoc = await getDocData(db.collection("player_statistics").doc(userId));
  const playerDoc = await getDocData(db.collection("players").doc(userId));
  const { eligible, spec } = verifyAchievementEligibility(achievementId, statsDoc, playerDoc);

  if (!eligible) {
    await appendAuditLog(
      db,
      {
        actorId: userId,
        targetUserId: userId,
        actionType: "ACHIEVEMENT_VERIFIED",
        resourceType: "achievements",
        resourceId: achDocId,
        status: "REJECTED",
        metadataSummary: `Player ${userId} did not meet server criteria for ${achievementId}`,
      },
      nowTs
    );
    throw new BackendServiceError(
      "FAILED_PRECONDITION",
      422,
      `Player ${userId} has not yet met the career criteria for achievement '${spec.title}'.`
    );
  }

  const ts = resolveTimestamp(db, nowTs);
  await achRef.set({
    userId,
    achievementId: spec.achievementId,
    title: spec.title,
    description: spec.description,
    xpValue: spec.xpValue,
    isUnlocked: true,
    unlockedAt: ts,
  });

  if (playerDoc) {
    const nextXp = Number(playerDoc.xp || 0) + spec.xpValue;
    const nextCoins = Number(playerDoc.coins || 0) + spec.coinsReward;
    const nextLevel = Math.min(500, Math.max(1, Math.floor(nextXp / 500) + 1));
    await db.collection("players").doc(userId).set({
      ...playerDoc,
      xp: nextXp,
      coins: nextCoins,
      playerLevel: nextLevel,
      updatedAt: ts,
    });
  }

  await db.collection("reward_transactions").doc(`tx_ach_${achDocId}`).set({
    userId,
    rewardType: "ACHIEVEMENT_REWARD",
    sourceId: achievementId,
    coinsDelta: spec.coinsReward,
    xpDelta: spec.xpValue,
    mulligansDelta: 0,
    createdAt: ts,
  });

  await appendAuditLog(
    db,
    {
      actorId: userId,
      targetUserId: userId,
      actionType: "ACHIEVEMENT_VERIFIED",
      resourceType: "achievements",
      resourceId: achDocId,
      status: "SUCCESS",
      metadataSummary: `Verified achievement ${achievementId}: +${spec.xpValue} XP, +${spec.coinsReward} coins`,
    },
    nowTs
  );

  return {
    achievementId: spec.achievementId,
    title: spec.title,
    xpAwarded: spec.xpValue,
    coinsAwarded: spec.coinsReward,
    isUnlocked: true,
  };
}

/**
 * 5. Daily Challenge Validation & Claim.
 */
async function validateAndClaimDailyChallenge(db, authContext, payload, nowTs = null) {
  const userId = verifyAuthAndOwnership(authContext, payload?.userId);
  await checkAndRecordRateLimit(db, userId, "CLAIM_DAILY_CHALLENGE", nowTs);

  const challengeId = String(payload?.challengeId || "").trim();
  if (!challengeId) {
    throw new BackendServiceError("INVALID_ARGUMENT", 400, "challengeId is required.");
  }

  const docId = `${userId}_${challengeId}`;
  const chRef = db.collection("daily_challenges").doc(docId);
  const chDoc = await getDocData(chRef);
  if (!chDoc) {
    throw new BackendServiceError("NOT_FOUND", 404, `Daily challenge record ${docId} not found.`);
  }
  if (chDoc.isClaimed) {
    throw new BackendServiceError(
      "ALREADY_EXISTS",
      409,
      `Reward for daily challenge ${challengeId} has already been claimed.`
    );
  }
  if (Number(chDoc.currentProgress || 0) < Number(chDoc.targetGoal || 1)) {
    throw new BackendServiceError(
      "FAILED_PRECONDITION",
      422,
      `Daily challenge ${challengeId} is incomplete (${chDoc.currentProgress}/${chDoc.targetGoal}).`
    );
  }

  const ts = resolveTimestamp(db, nowTs);
  const rewardCoins = Number(chDoc.rewardCoins || 250);
  const rewardMulligans = Number(chDoc.rewardMulligans || 1);

  await chRef.update({
    currentProgress: chDoc.currentProgress,
    isCompleted: true,
    isClaimed: true,
    updatedAt: ts,
  });

  const playerRef = db.collection("players").doc(userId);
  const playerDoc = await getDocData(playerRef);
  if (playerDoc) {
    await playerRef.set({
      ...playerDoc,
      coins: Number(playerDoc.coins || 0) + rewardCoins,
      mulligansRemaining: Number(playerDoc.mulligansRemaining || 0) + rewardMulligans,
      updatedAt: ts,
    });
  }

  await db.collection("reward_transactions").doc(`tx_ch_${docId}`).set({
    userId,
    rewardType: "DAILY_CHALLENGE",
    sourceId: challengeId,
    coinsDelta: rewardCoins,
    xpDelta: 100,
    mulligansDelta: rewardMulligans,
    createdAt: ts,
  });

  await appendAuditLog(
    db,
    {
      actorId: userId,
      targetUserId: userId,
      actionType: "REWARD_CLAIMED",
      resourceType: "daily_challenges",
      resourceId: docId,
      status: "SUCCESS",
      metadataSummary: `Claimed daily challenge ${challengeId}: +${rewardCoins} coins, +${rewardMulligans} mulligans`,
    },
    nowTs
  );

  return {
    challengeId,
    rewardCoins,
    rewardMulligans,
    isClaimed: true,
  };
}

/**
 * 6. Administrative Operation: Create or Schedule Official Tournament (Apex Dynamics Game Studios).
 */
async function adminCreateOrScheduleTournament(db, authContext, payload, nowTs = null) {
  const adminRecord = await getDocData(db.collection("studio_admins").doc(authContext?.uid || "_"));
  const { adminUid } = verifyAdminAccess(authContext, adminRecord);

  const tournamentId = String(payload?.tournamentId || "").trim();
  if (!tournamentId) {
    throw new BackendServiceError("INVALID_ARGUMENT", 400, "tournamentId is required.");
  }

  const ts = resolveTimestamp(db, nowTs);
  const tournamentDoc = {
    tournamentId,
    title: String(payload.title || "Apex Championship").slice(0, 80),
    courseName: String(payload.courseName || "Emerald Links").slice(0, 80),
    entryFeeCoins: Math.max(0, Number(payload.entryFeeCoins ?? 500)),
    prizeCoins: Math.max(0, Number(payload.prizeCoins ?? 5000)),
    prizeTrophies: Math.max(0, Number(payload.prizeTrophies ?? 50)),
    holesCount: Math.min(18, Math.max(1, Number(payload.holesCount ?? 6))),
    difficulty: String(payload.difficulty || "PRO").slice(0, 32),
    status: String(payload.status || "OPEN").slice(0, 32),
    requiredTrophies: Math.max(0, Number(payload.requiredTrophies ?? 0)),
    scheduledStartEpochMs: Number(payload.scheduledStartEpochMs ?? Date.now()),
    scheduledEndEpochMs: Number(payload.scheduledEndEpochMs ?? Date.now() + 86400000),
    createdByAdminId: adminUid,
    updatedAt: ts,
  };

  await db.collection("tournaments").doc(tournamentId).set(tournamentDoc);

  await appendAuditLog(
    db,
    {
      actorId: adminUid,
      targetUserId: adminUid,
      actionType: "ADMIN_TOURNAMENT_CREATED",
      resourceType: "tournaments",
      resourceId: tournamentId,
      status: "SUCCESS",
      metadataSummary: `Admin ${adminUid} created/scheduled tournament ${tournamentId} (${tournamentDoc.title})`,
    },
    nowTs
  );

  return tournamentDoc;
}

/**
 * 7. Administrative Operation: Configure Global Rewards & Tournament Multipliers.
 */
async function adminConfigureRewards(db, authContext, payload, nowTs = null) {
  const adminRecord = await getDocData(db.collection("studio_admins").doc(authContext?.uid || "_"));
  const { adminUid } = verifyAdminAccess(authContext, adminRecord);

  const configId = String(payload?.configId || "default_season_config").trim();
  const ts = resolveTimestamp(db, nowTs);
  const configDoc = {
    configId,
    holeCompletionBaseCoins: Number(payload.holeCompletionBaseCoins ?? 60),
    birdieBonusCoins: Number(payload.birdieBonusCoins ?? 75),
    eagleBonusCoins: Number(payload.eagleBonusCoins ?? 200),
    aceBonusCoins: Number(payload.aceBonusCoins ?? 750),
    tournamentFirstRatio: Number(payload.tournamentFirstRatio ?? 0.5),
    tournamentSecondRatio: Number(payload.tournamentSecondRatio ?? 0.3),
    tournamentThirdRatio: Number(payload.tournamentThirdRatio ?? 0.2),
    updatedBy: adminUid,
    updatedAt: ts,
  };

  await db.collection("reward_configs").doc(configId).set(configDoc);

  await appendAuditLog(
    db,
    {
      actorId: adminUid,
      targetUserId: adminUid,
      actionType: "ADMIN_REWARD_CONFIGURED",
      resourceType: "reward_configs",
      resourceId: configId,
      status: "SUCCESS",
      metadataSummary: `Admin ${adminUid} updated reward config ${configId}`,
    },
    nowTs
  );

  return configDoc;
}

/**
 * 8. Administrative Operation: Publish Official Challenge.
 */
async function adminPublishChallenge(db, authContext, payload, nowTs = null) {
  const adminRecord = await getDocData(db.collection("studio_admins").doc(authContext?.uid || "_"));
  const { adminUid } = verifyAdminAccess(authContext, adminRecord);

  const challengeId = String(payload?.challengeId || "").trim();
  if (!challengeId) {
    throw new BackendServiceError("INVALID_ARGUMENT", 400, "challengeId is required.");
  }

  const ts = resolveTimestamp(db, nowTs);
  const challengeDoc = {
    challengeId,
    title: String(payload.title || "Championship Precision").slice(0, 80),
    description: String(payload.description || "Score 3 Birdies on Championship holes").slice(0, 200),
    targetType: String(payload.targetType || "BIRDIE_COUNT").slice(0, 48),
    targetGoal: Math.max(1, Number(payload.targetGoal ?? 3)),
    rewardCoins: Math.max(0, Number(payload.rewardCoins ?? 500)),
    rewardMulligans: Math.max(0, Number(payload.rewardMulligans ?? 2)),
    isActive: Boolean(payload.isActive ?? true),
    publishedBy: adminUid,
    updatedAt: ts,
  };

  await db.collection("published_challenges").doc(challengeId).set(challengeDoc);

  await appendAuditLog(
    db,
    {
      actorId: adminUid,
      targetUserId: adminUid,
      actionType: "ADMIN_CHALLENGE_PUBLISHED",
      resourceType: "published_challenges",
      resourceId: challengeId,
      status: "SUCCESS",
      metadataSummary: `Admin ${adminUid} published challenge ${challengeId} (${challengeDoc.title})`,
    },
    nowTs
  );

  return challengeDoc;
}

/**
 * 9. Administrative Operation: Review Suspicious Match Results (Anti-Cheat Triage).
 */
async function adminReviewSuspiciousMatch(db, authContext, payload, nowTs = null) {
  const adminRecord = await getDocData(db.collection("studio_admins").doc(authContext?.uid || "_"));
  const { adminUid } = verifyAdminAccess(authContext, adminRecord);

  const flagId = String(payload?.flagId || "").trim();
  const resolution = String(payload?.reviewStatus || "").trim();
  if (!flagId || !["CONFIRMED_CHEAT", "CLEARED"].includes(resolution)) {
    throw new BackendServiceError(
      "INVALID_ARGUMENT",
      400,
      "Valid flagId and reviewStatus (CONFIRMED_CHEAT or CLEARED) are required."
    );
  }

  const flagRef = db.collection("anti_cheat_flags").doc(flagId);
  const existingFlag = await getDocData(flagRef);
  if (!existingFlag) {
    throw new BackendServiceError("NOT_FOUND", 404, `Anti-cheat flag ${flagId} not found.`);
  }

  const ts = resolveTimestamp(db, nowTs);
  await flagRef.update({
    reviewStatus: resolution,
    reviewedBy: adminUid,
    reviewNotes: String(payload.reviewNotes || `Reviewed by ${adminUid}`).slice(0, 300),
    updatedAt: ts,
  });

  await appendAuditLog(
    db,
    {
      actorId: adminUid,
      targetUserId: existingFlag.userId,
      actionType: "ADMIN_ANTICHEAT_REVIEWED",
      resourceType: "anti_cheat_flags",
      resourceId: flagId,
      status: "SUCCESS",
      metadataSummary: `Admin ${adminUid} marked flag ${flagId} as ${resolution}`,
    },
    nowTs
  );

  return {
    flagId,
    userId: existingFlag.userId,
    reviewStatus: resolution,
    reviewedBy: adminUid,
  };
}

/**
 * 10. Administrative Operation: Monitor Tournament Statistics.
 */
async function adminGetTournamentStatistics(db, authContext, payload) {
  const adminRecord = await getDocData(db.collection("studio_admins").doc(authContext?.uid || "_"));
  verifyAdminAccess(authContext, adminRecord);

  const tournamentId = String(payload?.tournamentId || "").trim();
  if (!tournamentId) {
    throw new BackendServiceError("INVALID_ARGUMENT", 400, "tournamentId is required.");
  }

  const tournamentDoc = await getDocData(db.collection("tournaments").doc(tournamentId));
  if (!tournamentDoc) {
    throw new BackendServiceError("NOT_FOUND", 404, `Tournament ${tournamentId} not found.`);
  }

  const entriesSnap = await db
    .collection("tournament_entries")
    .where("tournamentId", "==", tournamentId)
    .get();
  const entries = entriesSnap.docs.map((d) => d.data());

  const flagsSnap = await db
    .collection("anti_cheat_flags")
    .where("tournamentId", "==", tournamentId)
    .get();

  const totalEntrants = entries.length;
  const averageStrokes =
    totalEntrants > 0
      ? Number((entries.reduce((s, e) => s + Number(e.strokes || 0), 0) / totalEntrants).toFixed(2))
      : 0;
  const bestScoreToPar =
    totalEntrants > 0 ? Math.min(...entries.map((e) => Number(e.scoreToPar || 0))) : 0;
  const totalPrizeCoinsDistributed = entries.reduce(
    (s, e) => s + Number(e.rewardCoinsAwarded || 0),
    0
  );

  return {
    tournamentId,
    title: tournamentDoc.title,
    status: tournamentDoc.status,
    totalEntrants,
    averageStrokes,
    bestScoreToPar,
    totalPrizeCoinsDistributed,
    flaggedAttemptsCount: flagsSnap.docs.length,
  };
}

module.exports = {
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
};
