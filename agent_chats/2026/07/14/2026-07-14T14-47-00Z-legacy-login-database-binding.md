# Legacy Login Database Binding

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Trace database binding from old `index.jade` into `login.js` before changing
  Vue.
- Remove frontend behavior and configuration absent from the old login View.
- Reuse the existing initapp adapter and reduce component size.

## Changed Files

- `frontend/src/LoginPanel.vue`
- `frontend/src/App.vue`
- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T14-47-00Z-legacy-login-database-binding.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/index.jade` emits a hidden `dbid` only inside
  `if data.Dbs.length == 1` and renders no database picker.
- `../FoolFrame/src/Web/public/javascripts/app/login.js` forwards only the
  template-bound `$scope.dbid` in the login request.
- Vue rendered a multi-database Select, defaulted it to the first item, and kept
  a shell-level `car_wash` fallback independent of initapp.

## Implementation

- `legacyInitAppDbId` now returns an id only for an exactly-one database list.
- `LoginPanel` computes the submission id through that existing adapter and no
  longer imports Select or owns database options and selection state.
- The shell database ref starts empty and is replaced directly from initapp.
- `LoginPanel.vue` shrank from 215 to 189 lines. No component, dependency, or
  concrete business DTO state was added.

## Validation

- Focused `npm test -- payload.test.ts viewWorkflow.test.ts`: 129 tests passed.
- Full `npm test`: 179 tests passed across 14 files.
- `npm run build`: Vue type-check and Vite production build passed.
- `docker compose build frontend` and no-dependency forced frontend recreation
  passed with image
  `sha256:f42ba03afdc7e2aec2ed5513e9197d4a31ba5622d1557f8aa2f52be03333e045`.
- Compose retained backend/frontend/Redis/MySQL running and `db-migrate` at
  `Exited (0)`; backend `/test` passed.
- Repository harness and `git diff --check` passed before the atomic commit.
- A versioned Docker browser entry loaded the current bundle and rendered no
  database select/combobox while initapp exposed exactly one `car_wash` entry.
- Authorized `admin/admin` login with a freshly read local CAPTCHA reached
  `/main`. The backend success log recorded `DbId=car_wash`, and the View-first
  shell rendered Order List.
- Logout returned to the same three-input/no-picker page at 1280px. Document
  width stayed 1280px and the clean browser run ended without errors.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-login-database-binding/clean-login.png`
- `artifacts/runs/20260714-legacy-login-database-binding/authenticated-clean.png`

## Skipped Checks And Risks

- The full old FoolFrame application was not booted; checked-in Jade and
  JavaScript supply the old binding contract.
