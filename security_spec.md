# Zenith Golf Security Specification (`security_spec.md`)

## 1. Data Invariants & Relationship Mapping
- **User Profile (`/users/{userId}`)**: Contains career progress and user email (PII). Strictly isolated to the owner (`request.auth.uid == userId`). No other authenticated or unauthenticated user can read or write another user's profile.
- **Tour Pass Progress (`/users/{userId}/tourPass/{docId}`)**: Subcollection belonging to `userId`. Inherits `userId` scope directly; only `request.auth.uid == userId` can read or write. Arrays (`claimedFreeTiers`, `claimedVipTiers`) are bounded to at most 50 integers.
- **Leaderboard Entries (`/leaderboards/{boardId}/entries/{userId}`)**: Contains public non-PII tournament stats (`displayName`, `trophies`, `shootoutHighScore`, `longestDriveYds`). Any authenticated user can query entries where `trophies >= 0`, but only the owner (`request.auth.uid == userId`) can create, update, or delete their own entry.
- **Temporal Integrity**: All `updatedAt` fields must be server timestamps (`is timestamp && <= request.time`).

## 2. The "Dirty Dozen" Adversarial Payloads
1. **Unauthenticated Profile Read**: `unauthDb.collection("users").doc("alice_123").get()` -> Reject
2. **Cross-User PII Profile Read**: `aliceDb.collection("users").doc("bob_456").get()` -> Reject
3. **Identity Spoofing on Profile Create**: `aliceDb.collection("users").doc("alice_123").set({ userId: "bob_456", ... })` -> Reject
4. **Shadow Ghost Field Injection**: `aliceDb.collection("users").doc("alice_123").set({ ..., isAdmin: true })` -> Reject (`hasOnly` guard)
5. **Immutable `userId` Mutation on Update**: `aliceDb.collection("users").doc("alice_123").update({ userId: "bob_456" })` -> Reject
6. **Oversized String Resource Exhaustion**: `displayName` with 500 characters -> Reject (`size() <= 80`)
7. **Negative Currency / Trophy Injection**: `coins: -9999` or `trophies: -5` -> Reject (`>= 0`)
8. **Future Timestamp Spoofing**: `updatedAt: Timestamp.fromMillis(Date.now() + 3600000)` -> Reject (`<= request.time`)
9. **Unbounded Array Injection on Tour Pass**: `claimedFreeTiers` with 60 items -> Reject (`size() <= 50`)
10. **Cross-User Tour Pass Modification**: `bobDb.collection("users/alice_123/tourPass").doc("state").set(...)` -> Reject
11. **Cross-User Leaderboard Spoofing**: `bobDb.collection("leaderboards/global/entries").doc("alice_123").set(...)` -> Reject
12. **Unfiltered User Collection List Query**: `aliceDb.collection("users").get()` without `.where("userId", "==", "alice_123")` -> Reject

## 3. Red Team Audit Report
- **Shadow Update Test**: PASS (`keys().hasOnly(...)` on create and `diff(existing()).affectedKeys().hasOnly(...)` on update block all undeclared fields).
- **PII Blanket Test**: PASS (`/users/{userId}` restricts `get` and `list` strictly to `request.auth.uid == userId` / `resource.data.userId == request.auth.uid`; `/leaderboards` stores no email or PII).
- **Query Trust Test**: PASS (`allow list` on `/users` checks `resource.data.userId == request.auth.uid`, and on `/leaderboards/{boardId}/entries` checks `resource.data.trophies >= 0`).
- **Value Poisoning Test**: PASS (`isValidUserProfile`, `isValidTourPass`, and `isValidLeaderboardEntry` wrap both `create` and `update`).
