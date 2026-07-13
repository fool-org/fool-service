# Candidate Cancel Copy Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically.

## Scope

- Align the candidate dialog's functional footer action with old Web copy.
- Preserve the existing immediate row-selection workflow.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T12-59-46Z-candidate-cancel.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/detailView.jade` line 146 labels the dismissing
  footer button `取消` and wires it through Bootstrap's modal dismissal.
- Its adjacent `确定` button has no callback; selection instead happens from a
  row action and immediately closes the modal in `detailview.js`.
- Vue already matched immediate row selection but invented the footer label
  `关闭`.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image manifest
  `sha256:218f3a46085bad352bb45765707136ecfc0e329b08d97f04e03aa059b863b512`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated `/view100/1001`: the dialog exposed one `取消` action; invoking
  it closed the dialog while the child table remained at three rows.
- At 390px, the dialog measured 358px wide and page `scrollWidth` equaled 390.

## Runtime Evidence

- `artifacts/runs/20260713-candidate-cancel/desktop.jpg`
- `artifacts/runs/20260713-candidate-cancel/mobile.jpg`

## Risks And Follow-Ups

- This is a copy-only binding change; candidate selection, pending-state
  disabling, and data mutation paths are unchanged.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
