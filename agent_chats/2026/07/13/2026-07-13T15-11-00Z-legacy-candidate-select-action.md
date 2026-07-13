# Legacy Candidate Select Action Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore the candidate row's old default Select action presentation.
- Preserve shared table rendering and select-existing staging.

## Changed Files

- `frontend/src/ListDataTable.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T15-11-00Z-legacy-candidate-select-action.md`

## Legacy Evidence

- `../FoolFrame/src/Web/public/javascripts/app/querylistdata.js` renders its
  configured default row action as `<a class="btn btn-default">`.
- Candidate tables configure that action as Select; metadata View operations
  use separate lightweight anchors.
- The shared Vue table preserved both event paths but rendered candidate Select
  as an unbordered primary text command.

## Implementation

- Changed only `showDefaultAction` from text to secondary outlined.
- Left metadata `rowOperations` as secondary text and retained the shared table,
  events, labels, disabled state, and filler-row guard.
- Added a source-level assertion for the candidate-only branch; no CSS or new
  component was introduced.

## Validation

- Focused frontend suite: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Docker frontend production build and forced Compose recreation passed.
  Deployed image id:
  `sha256:a7a38bf715abc76086b0e8b3bd295b47fbe55c0931d87d4b267fbde23def447f`.
- Authenticated `/view102/1001` candidate query exposed four Select commands,
  all with secondary outlined classes.
- Selecting `2002 Existing fee` closed the dialog and staged one child row in
  the detail table. Reloading without parent Save removed it again, proving no
  persisted mutation.
- Desktop and 390x844 candidate screenshots show stable columns, actions, and
  footer paging without overlap.
- Repository harness validation passed; frontend `/` and frontend-proxied
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-candidate-select-action/candidate-select-desktop.png`
- `artifacts/runs/20260713-legacy-candidate-select-action/candidate-select-mobile.png`

## Risks And Follow-Ups

- Only the candidate default action is bordered; View operations intentionally
  keep the old lighter anchor hierarchy through the separate branch.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
