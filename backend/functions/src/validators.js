"use strict";

const crypto = require("node:crypto");

const RATE_LIMIT_WINDOW_MS = 60 * 1000;
const MAX_WRITE_REQUESTS_PER_WINDOW = 30;
const MAX_PHYSICAL_DRIVE_YDS = 430.0;
const MIN_SECONDS_PER_HOLE = 8;

const DEFAULT_REWARD_CONFIG = Object.freeze({
  configId: "default_season_config",
  holeCompletionBaseCoins: 60,
  birdieBonusCoins: 75,
  eagleBonusCoins: 200,
  aceBonusCoins: 750,
  tournamentFirstRatio: 0.5,
  tournamentSecondRatio: 0.3,
  tournamentThirdRatio: 0.2,
});

const OFFICIAL_ACHIEVEMENT_CATALOG = Object.freeze({
  ach_first_drive: {
    achievementId: "ach_first_drive",
    title: "Rocket Launcher",
    description: "Record a drive of 250+ yards",
    xpValue: 1000,
    coinsReward: 250,
    check: (stats) => Number(stats.longestDriveYds || 0) >= 250,
  },
  ach_eagle: {
    achievementId: "ach_eagle",
    title: "Eagle Eye",
    description: "Sink at least 1 Eagle in official play",
    xpValue: 2500,
    coinsReward: 500,
    check: (stats) => Number(stats.eagles || 0) >= 1,
  },
  ach_ace: {
    achievementId: "ach_ace",
    title: "Hole-In-One Legend",
    description: "Record an official Hole-In-One",
    xpValue: 5000,
    coinsReward: 1500,
    check: (stats) => Number(stats.aces || 0) >= 1,
  },
  ach_birdie_hunter: {
    achievementId: "ach_birdie_hunter",
    title: "Birdie Machine",
    description: "Score 10 career Birdies",
    xpValue: 1500,
    coinsReward: 400,
    check: (stats) => Number(stats.birdies || 0) >= 10,
  },
  ach_tour_veteran: {
    achievementId: "ach_tour_veteran",
    title: "Tour Veteran",
    description: "Complete 18 career holes",
    xpValue: 2000,
    coinsReward: 500,
    check: (stats) => Number(stats.holesPlayed || 0) >= 18,
  },
  ach_trophy_master: {
    achievementId: "ach_trophy_master",
    title: "Apex Contender",
    description: "Earn 100 championship trophies",
    xpValue: 3000,
    coinsReward: 1000,
    check: (_stats, player) => Number(player.trophies || 0) >= 100,
  },
});

class BackendServiceError extends Error {
  constructor(code, status, message, details = null) {
    super(message);
    this.name = "BackendServiceError";
    this.code = code;
    this.status = status;
    this.details = details;
  }
}

/**
 * Verifies that a valid Firebase Auth context exists and matches the target player UID.
 */
function verifyAuthAndOwnership(authContext, targetUserId) {
  if (!authContext || !authContext.uid || typeof authContext.uid !== "string") {
    throw new BackendServiceError(
      "UNAUTHENTICATED",
      401,
      "Valid Firebase Authentication ID token is required."
    );
  }
  if (targetUserId && authContext.uid !== targetUserId) {
    throw new BackendServiceError(
      "PERMISSION_DENIED",
      403,
      `Authenticated player (${authContext.uid}) cannot modify records owned by (${targetUserId}).`
    );
  }
  return authContext.uid;
}

/**
 * Verifies that the caller is an authorized Apex Dynamics Game Studios administrator.
 */
function verifyAdminAccess(authContext, studioAdminDoc) {
  if (!authContext || !authContext.uid) {
    throw new BackendServiceError(
      "UNAUTHENTICATED",
      401,
      "Authentication required for Apex Dynamics administrative operations."
    );
  }
  const hasAdminClaim = Boolean(authContext.token && authContext.token.admin === true);
  const hasStudioAdminRecord = Boolean(
    studioAdminDoc &&
      studioAdminDoc.adminUid === authContext.uid &&
      ["STUDIO_ADMIN", "TOURNAMENT_DIRECTOR", "SECURITY_ANALYST"].includes(studioAdminDoc.role)
  );
  if (!hasAdminClaim && !hasStudioAdminRecord) {
    throw new BackendServiceError(
      "PERMISSION_DENIED",
      403,
      "Caller is not an authorized Apex Dynamics Game Studios administrator."
    );
  }
  return {
    adminUid: authContext.uid,
    role: studioAdminDoc?.role || "STUDIO_ADMIN",
  };
}

/**
 * Evaluates per-user request rate limit window.
 */
function evaluateRateLimit(existingLimit, nowEpochMs, actionName, maxRequests = MAX_WRITE_REQUESTS_PER_WINDOW) {
  const safeNow = Number(nowEpochMs || Date.now());
  if (!existingLimit || typeof existingLimit.windowStartEpochMs !== "number") {
    return {
      allowed: true,
      nextRecord: {
        windowStartEpochMs: safeNow,
        requestCount: 1,
        lastAction: String(actionName || "API_CALL").slice(0, 64),
      },
    };
  }

  const elapsed = safeNow - existingLimit.windowStartEpochMs;
  if (elapsed >= RATE_LIMIT_WINDOW_MS) {
    return {
      allowed: true,
      nextRecord: {
        windowStartEpochMs: safeNow,
        requestCount: 1,
        lastAction: String(actionName || "API_CALL").slice(0, 64),
      },
    };
  }

  const nextCount = Number(existingLimit.requestCount || 0) + 1;
  if (nextCount > maxRequests) {
    return {
      allowed: false,
      retryAfterMs: Math.max(1000, RATE_LIMIT_WINDOW_MS - elapsed),
      nextRecord: existingLimit,
    };
  }

  return {
    allowed: true,
    nextRecord: {
      windowStartEpochMs: existingLimit.windowStartEpochMs,
      requestCount: nextCount,
      lastAction: String(actionName || "API_CALL").slice(0, 64),
    },
  };
}

/**
 * Computes a deterministic SHA-256 submission hash for duplicate detection.
 */
function computeSubmissionHash(userId, contextId, holeStrokes, totalStrokes, clientKey) {
  const raw = JSON.stringify({
    u: userId,
    c: contextId,
    hs: Array.isArray(holeStrokes) ? holeStrokes : [],
    ts: totalStrokes,
    k: clientKey || "",
  });
  return crypto.createHash("sha256").update(raw).digest("hex").slice(0, 48);
}

/**
 * Validates golf score physics bounds, hole-by-hole stroke arrays, drive distance, and round timing.
 * Returns either a verified score summary or anti-cheat violation flags.
 */
function verifyGolfScoreAndAntiCheat(payload) {
  const holesPlayed = Number(payload.holesPlayed);
  const totalStrokes = Number(payload.totalStrokes);
  const scoreToPar = Number(payload.scoreToPar);
  const maxDriveYds = Number(payload.maxDriveYds ?? 0);
  const roundDurationSec = Number(payload.roundDurationSec ?? holesPlayed * 30);
  const holeStrokes = Array.isArray(payload.holeStrokes) ? payload.holeStrokes.map(Number) : [];
  const holePars = Array.isArray(payload.holePars) ? payload.holePars.map(Number) : [];

  if (!Number.isInteger(holesPlayed) || holesPlayed < 1 || holesPlayed > 18) {
    throw new BackendServiceError("INVALID_ARGUMENT", 400, "holesPlayed must be an integer between 1 and 18.");
  }
  if (!Number.isInteger(totalStrokes) || totalStrokes < 1 || totalStrokes > 200) {
    throw new BackendServiceError("INVALID_ARGUMENT", 400, "totalStrokes must be an integer between 1 and 200.");
  }

  const violations = [];

  // 1. Minimum 1 stroke per hole physical constraint
  if (totalStrokes < holesPlayed) {
    violations.push({
      reasonCode: "IMPOSSIBLE_STROKES",
      severity: "CRITICAL",
      details: `Reported ${totalStrokes} total strokes across ${holesPlayed} holes (< 1 stroke/hole).`,
    });
  }

  // 2. Hole-by-hole stroke sum and bounds check
  if (holeStrokes.length > 0) {
    if (holeStrokes.length !== holesPlayed) {
      violations.push({
        reasonCode: "SCORE_MISMATCH",
        severity: "HIGH",
        details: `holeStrokes length (${holeStrokes.length}) does not match holesPlayed (${holesPlayed}).`,
      });
    }
    const invalidHole = holeStrokes.find((s) => !Number.isInteger(s) || s < 1 || s > 20);
    if (invalidHole !== undefined) {
      violations.push({
        reasonCode: "IMPOSSIBLE_STROKES",
        severity: "CRITICAL",
        details: `Hole stroke value (${invalidHole}) is outside physical bounds [1..20].`,
      });
    }
    const sumStrokes = holeStrokes.reduce((acc, s) => acc + s, 0);
    if (sumStrokes !== totalStrokes) {
      violations.push({
        reasonCode: "SCORE_MISMATCH",
        severity: "HIGH",
        details: `Sum of holeStrokes (${sumStrokes}) does not equal totalStrokes (${totalStrokes}).`,
      });
    }
  }

  // 3. Par array verification
  let computedScoreToPar = scoreToPar;
  let birdies = 0;
  let eagles = 0;
  let aces = 0;

  if (holePars.length > 0) {
    if (holePars.length !== holesPlayed) {
      violations.push({
        reasonCode: "SCORE_MISMATCH",
        severity: "MEDIUM",
        details: `holePars length (${holePars.length}) does not match holesPlayed (${holesPlayed}).`,
      });
    }
    const invalidPar = holePars.find((p) => !Number.isInteger(p) || p < 3 || p > 5);
    if (invalidPar !== undefined) {
      violations.push({
        reasonCode: "SCORE_MISMATCH",
        severity: "HIGH",
        details: `Invalid hole par value (${invalidPar}); expected [3..5].`,
      });
    }
    const totalPar = holePars.reduce((acc, p) => acc + p, 0);
    computedScoreToPar = totalStrokes - totalPar;
    if (scoreToPar !== computedScoreToPar) {
      violations.push({
        reasonCode: "SCORE_MISMATCH",
        severity: "HIGH",
        details: `Reported scoreToPar (${scoreToPar}) does not match totalStrokes - totalPar (${computedScoreToPar}).`,
      });
    }

    if (holeStrokes.length === holePars.length) {
      for (let i = 0; i < holeStrokes.length; i++) {
        const s = holeStrokes[i];
        const p = holePars[i];
        if (s === 1) aces += 1;
        if (s - p === -1) birdies += 1;
        else if (s - p <= -2) eagles += 1;
      }
    }
  } else {
    // Infer conservative birdie/eagle count when only aggregate score is supplied
    if (scoreToPar < -holesPlayed * 3 || scoreToPar > holesPlayed * 12) {
      violations.push({
        reasonCode: "IMPOSSIBLE_STROKES",
        severity: "CRITICAL",
        details: `Reported scoreToPar (${scoreToPar}) is impossible for ${holesPlayed} holes.`,
      });
    }
    if (holesPlayed === 1 && totalStrokes === 1) aces = 1;
    if (scoreToPar <= -2) eagles = 1;
    else if (scoreToPar === -1) birdies = 1;
  }

  // 4. Drive distance physics ceiling check
  if (maxDriveYds < 0 || maxDriveYds > MAX_PHYSICAL_DRIVE_YDS) {
    violations.push({
      reasonCode: "DRIVE_SPEED_EXCEEDED",
      severity: "CRITICAL",
      details: `Reported drive of ${maxDriveYds} yds exceeds physics ceiling (${MAX_PHYSICAL_DRIVE_YDS} yds).`,
    });
  }

  // 5. Elapsed round duration anti-speedhack check
  if (roundDurationSec < holesPlayed * MIN_SECONDS_PER_HOLE) {
    violations.push({
      reasonCode: "DURATION_ANOMALY",
      severity: "HIGH",
      details: `Round completed in ${roundDurationSec}s across ${holesPlayed} holes (< ${MIN_SECONDS_PER_HOLE}s/hole).`,
    });
  }

  const isClean = violations.length === 0;
  let scoreBanner = "PAR";
  if (aces > 0) scoreBanner = "HOLE IN ONE!";
  else if (computedScoreToPar <= -2) scoreBanner = `EAGLE! ${computedScoreToPar}`;
  else if (computedScoreToPar === -1) scoreBanner = "BIRDIE! -1";
  else if (computedScoreToPar === 0) scoreBanner = "EVEN PAR";
  else if (computedScoreToPar === 1) scoreBanner = "BOGEY +1";
  else scoreBanner = `+${computedScoreToPar}`;

  return {
    isValid: isClean,
    verificationStatus: isClean ? "VERIFIED" : "FLAGGED",
    violations,
    holesPlayed,
    totalStrokes,
    scoreToPar: computedScoreToPar,
    maxDriveYds: Math.max(0, Math.min(maxDriveYds, MAX_PHYSICAL_DRIVE_YDS)),
    roundDurationSec: Math.max(0, Math.round(roundDurationSec)),
    birdies,
    eagles,
    aces,
    scoreBanner,
    holeStrokes,
    holePars,
  };
}

/**
 * Calculates official server-controlled match rewards and XP (never trusting client coin/XP numbers).
 */
function calculateOfficialMatchRewards(verifiedScore, mode, rewardConfig = DEFAULT_REWARD_CONFIG) {
  const cfg = { ...DEFAULT_REWARD_CONFIG, ...(rewardConfig || {}) };
  const baseCoins = verifiedScore.holesPlayed * Number(cfg.holeCompletionBaseCoins || 60);
  const birdieCoins = verifiedScore.birdies * Number(cfg.birdieBonusCoins || 75);
  const eagleCoins = verifiedScore.eagles * Number(cfg.eagleBonusCoins || 200);
  const aceCoins = verifiedScore.aces * Number(cfg.aceBonusCoins || 750);
  const underParBonus = verifiedScore.scoreToPar < 0 ? Math.abs(verifiedScore.scoreToPar) * 40 : 0;

  const modeMultiplier = mode === "TOURNAMENT" ? 1.25 : mode === "MATCH_PLAY" ? 1.15 : 1.0;
  const coinsEarned = Math.min(
    50000,
    Math.max(25, Math.round((baseCoins + birdieCoins + eagleCoins + aceCoins + underParBonus) * modeMultiplier))
  );

  const xpEarned = Math.min(
    50000,
    Math.max(
      50,
      verifiedScore.holesPlayed * 80 +
        verifiedScore.birdies * 60 +
        verifiedScore.eagles * 150 +
        verifiedScore.aces * 500
    )
  );

  const trophiesDelta =
    mode === "TOURNAMENT" || mode === "MATCH_PLAY"
      ? verifiedScore.scoreToPar <= 0
        ? Math.max(3, Math.abs(verifiedScore.scoreToPar) * 2 + 4)
        : 1
      : verifiedScore.scoreToPar < 0
      ? 2
      : 0;

  return {
    coinsEarned,
    xpEarned,
    trophiesDelta,
  };
}

/**
 * Calculates official tournament standings and server-controlled prize distribution.
 */
function calculateTournamentStandingsAndPrizes(entries, tournamentDoc, rewardConfig = DEFAULT_REWARD_CONFIG) {
  const cfg = { ...DEFAULT_REWARD_CONFIG, ...(rewardConfig || {}) };
  const prizePool = Number(tournamentDoc?.prizeCoins || 2500);
  const trophyPool = Number(tournamentDoc?.prizeTrophies || 25);

  const validEntries = (entries || []).filter(
    (e) => e && e.verificationStatus !== "FLAGGED" && Number(e.strokes) >= Number(e.holesCompleted || 1)
  );

  const sorted = [...validEntries].sort((a, b) => {
    if (a.scoreToPar !== b.scoreToPar) return a.scoreToPar - b.scoreToPar;
    if (a.strokes !== b.strokes) return a.strokes - b.strokes;
    return String(a.userId).localeCompare(String(b.userId));
  });

  return sorted.map((entry, idx) => {
    const rankPosition = idx + 1;
    let rewardCoinsAwarded = 0;
    let rewardTrophiesAwarded = 0;
    if (rankPosition === 1) {
      rewardCoinsAwarded = Math.round(prizePool * Number(cfg.tournamentFirstRatio || 0.5));
      rewardTrophiesAwarded = trophyPool;
    } else if (rankPosition === 2) {
      rewardCoinsAwarded = Math.round(prizePool * Number(cfg.tournamentSecondRatio || 0.3));
      rewardTrophiesAwarded = Math.max(5, Math.round(trophyPool * 0.6));
    } else if (rankPosition === 3) {
      rewardCoinsAwarded = Math.round(prizePool * Number(cfg.tournamentThirdRatio || 0.2));
      rewardTrophiesAwarded = Math.max(3, Math.round(trophyPool * 0.35));
    } else if (rankPosition <= 10) {
      rewardCoinsAwarded = Math.max(100, Math.round(prizePool * 0.05));
      rewardTrophiesAwarded = 2;
    }
    return {
      ...entry,
      rankPosition,
      rewardCoinsAwarded: Math.min(50000, rewardCoinsAwarded),
      rewardTrophiesAwarded,
    };
  });
}

/**
 * Verifies whether a player meets the authoritative server criteria to unlock/claim an achievement.
 */
function verifyAchievementEligibility(achievementId, playerStats, playerDoc) {
  const spec = OFFICIAL_ACHIEVEMENT_CATALOG[achievementId];
  if (!spec) {
    throw new BackendServiceError(
      "NOT_FOUND",
      404,
      `Unknown official achievement ID: ${achievementId}`
    );
  }
  const eligible = spec.check(playerStats || {}, playerDoc || {});
  return {
    eligible,
    spec,
  };
}

module.exports = {
  DEFAULT_REWARD_CONFIG,
  OFFICIAL_ACHIEVEMENT_CATALOG,
  MAX_PHYSICAL_DRIVE_YDS,
  MIN_SECONDS_PER_HOLE,
  BackendServiceError,
  verifyAuthAndOwnership,
  verifyAdminAccess,
  evaluateRateLimit,
  computeSubmissionHash,
  verifyGolfScoreAndAntiCheat,
  calculateOfficialMatchRewards,
  calculateTournamentStandingsAndPrizes,
  verifyAchievementEligibility,
};
