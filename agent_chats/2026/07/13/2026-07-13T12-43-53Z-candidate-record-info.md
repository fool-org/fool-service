# Candidate Record Feedback Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically.

## Scope

- Compare the old select-existing dialog's initial record feedback against the
  deployed detail page.
- Restore the exact pre-query text without changing query timing or totals.

## Changed Files

- `frontend/src/useChildCandidates.ts`
- `frontend/src/useChildCandidates.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T12-43-53Z-candidate-record-info.md`

## Legacy Evidence

- `views/detailView.jade` initializes the candidate dialog's `#info` element
  with `记录数未知,请查询`.
- The Vue helper rendered `记录未知 请查询`, dropping `数` and the comma.
- Both implementations replace the initial text with `共N条记录` after a
  candidate query returns.

## Validation

- Focused `useChildCandidates.test.ts`: 3 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image manifest
  `sha256:ba7a2d3cb705fe2757cda4ab0b16bdb6ed2c554358504a6c154cdd8c0c5312e2`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated desktop `/view100/1001`: opening `增加` produced one candidate
  dialog with `记录数未知,请查询` and no candidate table before Find.
- At 390x844, the dialog measured 358px wide, retained the exact text, page
  `scrollWidth` equaled 390, and browser logs were empty.

## Runtime Evidence

- `artifacts/runs/20260713-candidate-record-info/desktop.jpg`
- `artifacts/runs/20260713-candidate-record-info/mobile.jpg`

## Risks And Follow-Ups

- This is a shared helper, so every select-existing group receives the same old
  initial wording; queried zero/nonzero totals remain covered by unit tests.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
