# PrimeVue Chunking Cleanup

## Prompt

- Commit the current frontend changes before continuing the FoolFrame behavior
  parity work.

## Scope

- Lazy-load the authenticated shell, list, detail, and report components.
- Split Vue and Prime theme dependencies into stable production chunks.
- Align the remaining chart, map, and shared surface colors with the PrimeVue
  interface and remove obsolete native-control CSS.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/LegacyChartPanel.vue`
- `frontend/src/LegacyMapPanel.vue`
- `frontend/src/style.css`
- `frontend/vite.config.ts`
- `agent_chats/2026/07/12/2026-07-12T07-56-08Z-primevue-chunking-cleanup.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 130 tests passed.
  - TypeScript and Vite production build passed.
  - The previous 882.50 kB unsplit application chunk warning is gone; Vue,
    Prime theme, View list/detail/report, ShellActions, and Leaflet emit as
    separate chunks.
- `git diff --check` passed.

## Runtime Evidence

- No runtime artifact was produced for this build-organization and style-only
  slice.

## Risks And Follow-Ups

- Docker/browser replay was not repeated because request/response and event
  behavior did not change.
- FoolFrame page-behavior parity gaps identified in the subsequent audit are
  separate work and are not claimed as resolved by this commit.
