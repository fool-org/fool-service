# Modern visual skin

## Prompt

Make the current Fool Service interface more attractive and proceed with the
first visual-improvement pass.

## Scope

- Modernize the existing Vue/PrimeVue presentation without adding dependencies.
- Preserve legacy routes, payloads, action ordering, and pending-state behavior.
- Avoid the dirty `App.vue`, `AgentChatPage.vue`, and authorization work.

## Changes

- Added a shared modern product skin for the shell, navigation, panels, tables,
  dialogs, pagination, empty states, and responsive layout.
- Reworked the login presentation into a focused card with improved spacing,
  input sizing, CAPTCHA grouping, and mobile behavior.
- Recorded the intentional visual-parity exception in the migration source of
  truth.

## Validation

- `cd frontend && npm test` — passed, 23 test files and 234 tests.
- `cd frontend && npm run build` — passed, including TypeScript validation and
  a 301-module Vite production build.
- `python scripts/legacy_migration_contract.py --require-legacy` — passed.
- `python scripts/check_repo_harness.py` — passed.
- `git diff --check` — passed.

## Runtime Evidence

- Preview frontend: `http://127.0.0.1:5173/`
- Preview HTML responded successfully and proxies `/api` to the existing backend.
- `curl http://localhost:8080/test` — passed with live order data.
- The existing `http://localhost:8081/` container was intentionally left
  untouched so unrelated dirty frontend work was not deployed with this slice.

## Risks

- This intentionally supersedes legacy Bootstrap pixel parity for the default
  Vue skin while keeping functional parity unchanged.

## Follow-ups

- Visually check the login, list, detail, report, and mobile layouts.
- Consider modernizing the AI assistant color tokens after its current dirty
  feature work lands.
