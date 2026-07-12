# Restore Authenticated Footer

## Prompt

Continue aligning old page layout, style, and interaction behavior while
keeping View-first metadata binding and atomic commits.

## Scope

- Compare the authenticated shell with FoolFrame `default.jade`.
- Restore the copyright footer from loaded application metadata.
- Reuse the existing `getmain.App` payload and Pascal/camel helper pattern.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/style.css`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T15-28-02Z-restore-authenticated-footer.md`

## Validation

- `cd frontend && npm test -- --run src/viewWorkflow.test.ts src/payload.test.ts`:
  129 tests passed.
- `cd frontend && npm test -- --run && npm run build`: 152 tests passed and
  the production build completed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`:
  final frontend image built and restarted; manifest
  `sha256:a83d6773b1dcc32ad4b088cf3b9eb5174415e20d681d6d4fd1355b284b65620c`.
- `python scripts/runtime_doctor.py`: passed, including `getmain` legacy
  AppInfo aliases and the complete auth/View/data/report/message smoke surface.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: frontend/backend/MySQL/Redis running; `db-migrate`
  exited successfully with code 0.
- `git diff --check`: passed.

## Source Evidence

- FoolFrame `default.jade` renders `&copy; data.App.AppPowerBy` after every
  authenticated page body.
- Docker `getmain.App` already exposes both `appPowerBy` and `AppPowerBy` from
  the migrated application catalog.
- Vue now reads that metadata through `legacyAppPowerBy`; no company name,
  business DTO, or additional request is introduced.

## Risks And Follow-Ups

- Current-build authenticated desktop/mobile visual confirmation of the footer
  remains pending fresh authorization to read and fill the current CAPTCHA.
