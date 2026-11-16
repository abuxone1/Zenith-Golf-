"use strict";

/**
 * Zenith Golf 2.0 – Secure Game Services Backend
 * Apex Dynamics Game Studios
 *
 * Separate Server-Side Firebase Cloud Functions v2 API.
 * All service account credentials remain strictly server-side via Firebase Admin Application Default Credentials.
 */

const {
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
} = require("./src/handlers");
const { BackendServiceError } = require("./src/validators");

let adminApp = null;
function getAdminServices() {
  if (!adminApp) {
    const admin = require("firebase-admin");
    if (admin.apps.length === 0) {
      admin.initializeApp();
    }
    adminApp = admin;
  }
  const databaseId =
    process.env.FIRESTORE_DATABASE_ID ||
    "ai-studio-android-golfaize-b0151fe9-347c-4ccd-bf6a-0f57f3da720e";
  const { getFirestore } = require("firebase-admin/firestore");
  return {
    auth: adminApp.auth(),
    db: getFirestore(adminApp.app(), databaseId),
  };
}

/**
 * Extracts and verifies the Firebase Authentication Bearer ID token from an HTTP request.
 */
async function authenticateHttpRequest(req, authService) {
  const header = req.headers?.authorization || req.headers?.Authorization || "";
  if (!header.startsWith("Bearer ")) {
    throw new BackendServiceError(
      "UNAUTHENTICATED",
      401,
      "Missing Authorization: Bearer <Firebase_ID_Token> header."
    );
  }
  const idToken = header.slice("Bearer ".length).trim();
  if (!idToken) {
    throw new BackendServiceError("UNAUTHENTICATED", 401, "Empty Bearer token.");
  }
  try {
    const decoded = await authService.verifyIdToken(idToken, true);
    return {
      uid: decoded.uid,
      token: decoded,
    };
  } catch (err) {
    throw new BackendServiceError(
      "UNAUTHENTICATED",
      401,
      `Invalid or expired Firebase ID token: ${err.message}`
    );
  }
}

/**
 * Unified HTTP REST dispatcher supporting all player and Apex Dynamics admin routes:
 * - POST /v1/matches/submit
 * - POST /v1/tournaments/register
 * - POST /v1/tournaments/submit-score
 * - POST /v1/achievements/claim
 * - POST /v1/challenges/claim
 * - POST /v1/admin/tournaments
 * - POST /v1/admin/rewards/config
 * - POST /v1/admin/challenges/publish
 * - POST /v1/admin/anti-cheat/review
 * - GET  /v1/admin/tournaments/stats
 */
async function dispatchHttpRequest(req, res, customServices = null) {
  try {
    const services = customServices || getAdminServices();
    const authContext = await authenticateHttpRequest(req, services.auth);
    const path = (req.path || req.url || "").split("?")[0];
    const method = (req.method || "POST").toUpperCase();
    const body = req.body || {};

    let result;
    if (method === "POST" && path === "/v1/matches/submit") {
      result = await submitPlayerMatchScore(services.db, authContext, body);
    } else if (method === "POST" && path === "/v1/tournaments/register") {
      result = await registerPlayerForTournament(services.db, authContext, body);
    } else if (method === "POST" && path === "/v1/tournaments/submit-score") {
      result = await submitTournamentEntryScore(services.db, authContext, body);
    } else if (method === "POST" && path === "/v1/achievements/claim") {
      result = await verifyAndClaimAchievement(services.db, authContext, body);
    } else if (method === "POST" && path === "/v1/challenges/claim") {
      result = await validateAndClaimDailyChallenge(services.db, authContext, body);
    } else if (method === "POST" && path === "/v1/admin/tournaments") {
      result = await adminCreateOrScheduleTournament(services.db, authContext, body);
    } else if (method === "POST" && path === "/v1/admin/rewards/config") {
      result = await adminConfigureRewards(services.db, authContext, body);
    } else if (method === "POST" && path === "/v1/admin/challenges/publish") {
      result = await adminPublishChallenge(services.db, authContext, body);
    } else if (method === "POST" && path === "/v1/admin/anti-cheat/review") {
      result = await adminReviewSuspiciousMatch(services.db, authContext, body);
    } else if ((method === "GET" || method === "POST") && path === "/v1/admin/tournaments/stats") {
      const payload = method === "GET" ? req.query || body : body;
      result = await adminGetTournamentStatistics(services.db, authContext, payload);
    } else {
      throw new BackendServiceError("NOT_FOUND", 404, `Unknown endpoint: ${method} ${path}`);
    }

    res.status(200).json({ ok: true, data: result });
  } catch (err) {
    const status = err instanceof BackendServiceError ? err.status : 500;
    const code = err instanceof BackendServiceError ? err.code : "INTERNAL";
    res.status(status).json({
      ok: false,
      error: {
        code,
        message: err.message || "Unexpected server error",
        details: err.details || null,
      },
    });
  }
}

// Export Cloud Functions v2 wrappers if firebase-functions is installed in the runtime,
// plus direct handler exports for local emulator & integration testing.
let cloudFunctionExports = {};
try {
  const { onRequest, onCall, HttpsError } = require("firebase-functions/v2/https");
  const wrapCallable = (handlerFn) =>
    onCall({ region: "europe-west2", enforceAppCheck: false }, async (request) => {
      try {
        const services = getAdminServices();
        return await handlerFn(services.db, request.auth, request.data || {});
      } catch (err) {
        if (err instanceof BackendServiceError) {
          throw new HttpsError("failed-precondition", err.message, {
            code: err.code,
            details: err.details,
          });
        }
        throw new HttpsError("internal", err.message || "Internal error");
      }
    });

  cloudFunctionExports = {
    zenithGameServicesApi: onRequest({ region: "europe-west2", cors: true }, (req, res) =>
      dispatchHttpRequest(req, res)
    ),
    submitMatchScore: wrapCallable(submitPlayerMatchScore),
    registerForTournament: wrapCallable(registerPlayerForTournament),
    submitTournamentScore: wrapCallable(submitTournamentEntryScore),
    claimAchievement: wrapCallable(verifyAndClaimAchievement),
    claimDailyChallenge: wrapCallable(validateAndClaimDailyChallenge),
    adminCreateTournament: wrapCallable(adminCreateOrScheduleTournament),
    adminConfigureRewards: wrapCallable(adminConfigureRewards),
    adminPublishChallenge: wrapCallable(adminPublishChallenge),
    adminReviewSuspiciousMatch: wrapCallable(adminReviewSuspiciousMatch),
    adminGetTournamentStats: wrapCallable(adminGetTournamentStatistics),
  };
} catch (_ignore) {
  // Running in lightweight test environment without firebase-functions runtime
}

module.exports = {
  ...cloudFunctionExports,
  dispatchHttpRequest,
  authenticateHttpRequest,
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
