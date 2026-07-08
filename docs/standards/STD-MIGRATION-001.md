# STD-MIGRATION-001: FoolFrame Migration Parity Stays Traceable

## Intent

The migration from legacy FoolFrame workflows should remain explicit: what has
been replaced, what is deferred, what command proves parity, and what runtime
gap remains.

## Scope

- `docs/migration/foolframe-parity.md`
- Backend migration code and tests
- Frontend replacement workflows
- Docker/runtime smoke checks that prove migrated paths

## Automatic Enforcement

`docs/migration/foolframe-parity.md review and focused tests` are required for
migration changes. `python scripts/check_repo_harness.py` verifies that the
parity document remains discoverable, references the key command families, and
keeps required remaining-work markers for the active FoolFrame migration areas.

## Evidence

Evidence includes migration parity notes, focused Maven or frontend test output,
Docker smoke output, and runtime artifact paths when a migrated workflow is
validated through the local stack.

## Repair Path

Update the parity document in the same change that migrates, defers, or
re-scopes a legacy workflow. Escalate to Docker smoke when a change crosses
frontend, backend, database, or runtime wiring.

## Revision Trigger

Revise this standard when a new legacy workflow category is added, when a
migration acceptance command changes, or when a production/runtime issue shows
the parity document is not specific enough.
