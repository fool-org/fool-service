# Shell App Branding

## Prompt

- Continue aligning Vue layout, style, and interaction behavior with the old
  FoolFrame Web pages, using an atomic commit for each change.

## Scope

- Read authenticated desktop and Drawer brand name/version from
  `getmain.App.AppName` / `AppVer` through shared Pascal/camel helpers.
- Derive the compact brand mark from the loaded application name.
- Remove the hard-coded `Fool Service` / `FoolFrame migration` pairing.
- Remove the Vue-only Docker backend, MySQL, and Redis status strip from the
  user-facing header and delete its dead `viewShell` constant/CSS.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/viewShell.ts`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T09-19-00Z-shell-app-branding.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 139 tests passed.
  - Vue TypeScript checking and Vite production build passed.
- `docker compose build frontend && docker compose up -d --no-deps frontend`
  - Rebuilt frontend deployed on port 8081.
- `python scripts/runtime_doctor.py`
  - Full runtime checks passed, including `getmain` AppInfo aliases.
- `python scripts/check_repo_harness.py`
  - Repository harness passed.
- `git diff --check` passed.

## Runtime Evidence

- Docker serves the rebuilt AppInfo brand at `http://localhost:8081`.
- Runtime doctor proved authenticated shell AppName/AppVer metadata remains
  available after removing the implementation status strip.

## Skipped Or Downgraded Checks

- Authenticated browser interaction remains gated by a fresh CAPTCHA without
  current solve permission.

## Risks And Follow-Ups

- Final authenticated desktop and 390x844 browser acceptance must prove the
  seeded AppInfo brand/version and the absence of implementation status tags.
- Moving desktop navigation from the sidebar into the old top-header layout is
  intentionally a separate atomic slice.
