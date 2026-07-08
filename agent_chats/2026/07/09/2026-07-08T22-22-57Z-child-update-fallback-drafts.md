# Child Update Fallback Drafts

## Prompt

Continue the View-first Vue migration, avoiding concrete business DTO binding
and reusing existing helpers.

## Scope

- Keep child update fallback drafts tied to rendered child group View columns.
- Do not change backend save semantics or child delete behavior in this slice.

## Changes

- Added a source guard proving `updateDetailItem` does not fallback through
  `buildFieldDrafts(detailItemValues(item))`.
- Reused `buildGroupItemDrafts(group, item)` in the App child update save path.
- Updated migration parity docs and task state.

## Validation

- RED: `npm test -- --run src/payload.test.ts` failed because the update path
  still used `buildFieldDrafts(detailItemValues(item))`.
- GREEN: `npm test -- --run src/payload.test.ts`.
- PASS: `npm test`.
- PASS: `npm run build`.
- PASS: `python scripts/check_repo_harness.py`.
- PASS: `git diff --check`.
- PASS: `docker compose build frontend`.
- PASS: `docker compose up -d frontend`.
- PASS: `python scripts/runtime_doctor.py`.

## Runtime Evidence

- `curl -fsS http://localhost:8081/` loaded frontend bundle
  `index-aEi0UznD.js`.
- `docker compose ps` showed backend, rebuilt frontend, MySQL, and Redis
  running; MySQL and Redis were healthy.

## Risks

- This is frontend save-path wiring only. It leaves the existing child delete
  payload behavior unchanged.

## Follow-ups

- Continue the remaining migration work from `docs/migration/foolframe-parity.md`.
