# Phase 0 Authorization Foundation

## Prompt

Follow `docs/authorization-and-agent-risk-control.md` in order and complete the
Agent-related development.

## Scope

- Completed the design's Phase 0 identity, deny-by-default authorization,
  policy-version, audit, credential-upgrade, and Agent owner/scope foundation.
- Preserved old request-body token compatibility at the authentication filter
  while removing raw token trust and persistence from Agent controllers and
  services.
- Kept all medium/high actions disabled; this slice does not claim Phase 1 or
  action-state-machine completion.

## Changes

- Added low-level authentication/authorization/audit value objects and SPI in
  `fool-common`.
- Added opaque hashed-token storage with idle/absolute TTL, rotation/revocation,
  bearer/body compatibility filtering, effective subject resolution, JDBC
  policy decisions, policy versions, and security auditing in `fool-auth`.
- Added BCrypt credential migration on successful legacy-password login.
- Migrated Agent sessions from raw token ownership to fixed
  owner/app/database scope and an authentication-session reference.
- Added migration `016-authorization-and-agent-control.sql` for credentials,
  policy, audit, reserved action-state tables, and Agent scope columns.
- Added frontend bearer-header propagation and recursive request/response log
  redaction for token, password, credential, and connection fields.
- Updated `tasks.md` and `docs/agent-sessions.md` to reflect the accepted Phase
  0 boundary.

## Validation

- `docker run --rm ... maven:3.9-eclipse-temurin-17 mvn -pl fool-auth,fool-agent -am -DskipTests package` — passed.
- Focused `TokenService`, authorization filter/service, Agent session/provider
  tests — 26 passed, 0 failed.
- `SensitiveLogSanitizerTest` — 2 passed, 0 failed.
- Full Java 17 `mvn -pl business-application -am -DskipTests package` — passed.
- `python scripts/check_repo_harness.py` — passed.
- `git diff --check` — passed before documentation/evidence updates and is
  rerun in the next phase validation.

## Runtime Evidence

- `artifacts/runs/20260715-phase0-authorization/runtime-evidence.md`
- `db-migrate-1` replayed migrations through `016` and exited with code 0.
- Protected unauthenticated HTTP returned 401; authenticated Agent capability
  and session requests returned 200.
- MySQL, Redis, response payload, and recent-log checks found no raw bearer
  token persistence or disclosure.

## Risks

- Body-token compatibility deliberately remains until Phase 4; bearer/body
  disagreement fails authentication.
- Phase 0 authorizes `agent.use` but does not yet apply row/field policy to the
  underlying View, report, query, or metadata reads. Those routes remain the
  Phase 1 work and no medium/high action is enabled.
- The local host Java is 8, so Java 17 validation uses the repository's Docker
  build runtime.

## Follow-Ups

- Implement Phase 1 resource resolution plus consistent row/field enforcement
  across UI, report, and Agent read paths.
- Add model-outbound classification, masking, provider allowlists, and strict
  ActionIntent parsing before external model execution can carry scoped data.

## Linked Commits or PRs

- None yet; the active goal continues into Phase 1 in the current worktree.
