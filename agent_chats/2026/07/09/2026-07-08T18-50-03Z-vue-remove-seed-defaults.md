# Vue Remove Seed Defaults

## Prompt

Continue the Docker/FoolFrame/Vue migration and keep the frontend from binding
rendering or manual workflow defaults to concrete business DTOs.

## Scope

- Removed the remaining Vue manual API-tool defaults for seeded enum model
  `102` and new object id `9001`.
- Kept metadata-driven enum loading unchanged; rendered detail fields still
  request enum values from their View field metadata.
- Updated task and migration notes for the removed DTO defaults.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- RED: `cd frontend && npm test -- src/payload.test.ts`
  - Failed at `does not prefill business-specific data DTO fields by default`
    because `enumModelId` was still `"102"` and `saveNewObjId` was still
    `"9001"`.
- GREEN: `cd frontend && npm test -- src/payload.test.ts`
  - `57 passed`.
- GREEN: `cd frontend && npm test`
  - `92 passed`.
- GREEN: `cd frontend && npm run build`
  - `vue-tsc --noEmit && vite build` succeeded.
- GREEN: `python scripts/check_repo_harness.py`
  - Repository harness validation passed.
- GREEN: `git diff --check`
- GREEN: `wc -l frontend/src/App.vue`
  - `1997`.
- GREEN: `docker compose up -d --no-deps --force-recreate --build frontend`
  - Rebuilt and recreated `fool-service-frontend-1`.
- GREEN: `docker compose ps`
  - Frontend recreated and running; MySQL and Redis healthy; backend running.
- GREEN: `curl -sS -o /tmp/fool-service-frontend-smoke.html -w "%{http_code} %{size_download}\n" http://localhost:8081/`
  - `200 399`.
- GREEN: `curl -sS -o /tmp/fool-service-backend-smoke.txt -w "%{http_code} %{size_download}\n" http://localhost:8080/test`
  - `200 465`.

## Risks

- Manual API tools now require explicit model/object ids when used outside the
  View workflow. That is intentional: defaulting to seeded business ids was the
  DTO binding being removed.
