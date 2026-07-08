# Child Draft View Columns

## Prompt

Continue the FoolFrame migration with the frontend bound to rendered View
metadata first. Avoid concrete business DTO coupling and keep code reuse tight.

## Scope

- Keep existing child-row draft state tied to rendered child group View columns.
- Do not change backend save semantics or child delete behavior in this slice.

## Changes

- Added a regression test proving child drafts ignore DTO-only child row values.
- Added `buildGroupItemDrafts` so existing child drafts are keyed by
  `groupColumns(group)` and only merge matching `querydatadetail` values.
- Reused the same helper for the first child edit path in `useChildDrafts`.
- Updated migration parity docs and task state.

## Validation

- RED: `npm test -- --run src/useChildDrafts.test.ts` failed because
  `dtoOnly=leak` entered `childDrafts`.
- GREEN: `npm test -- --run src/useChildDrafts.test.ts`.
- PASS: `npm test`.
- PASS: `npm run build`.
- PASS: `python scripts/check_repo_harness.py`.
- PASS: `git diff --check`.
- PASS: `docker compose build frontend`.
- PASS: `docker compose up -d frontend`.
- PASS: `python scripts/runtime_doctor.py`.

## Runtime Evidence

- `curl -fsS http://localhost:8081/` loaded frontend bundle
  `index-BQsRuzHk.js`.
- `docker compose ps` showed backend, rebuilt frontend, MySQL, and Redis
  running; MySQL and Redis were healthy.

## Risks

- This is frontend editor-state scope only. It does not change backend
  `querydatadetail` output or the minimal child delete payload path.

## Follow-ups

- Continue the remaining migration work from `docs/migration/foolframe-parity.md`.
