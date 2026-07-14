# Legacy Message Empty Target

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Compare old `default.jade` / `showerror.js` message-detail availability with
  the Vue system-message dialog.
- Restore the no-target `查看详细` placeholder without changing target-View
  navigation.
- Reuse the shared message View adapter and existing handler.

## Changed Files

- `frontend/src/ShellActions.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T00-31-47Z-legacy-message-empty-target.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/default.jade` always renders `查看详细` as an
  active `href='#'` anchor inside the system-message modal.
- `../FoolFrame/src/Web/public/javascripts/app/showerror.js` replaces that href
  only when `view` is nonempty and nonzero; otherwise the placeholder remains
  `#` and does not dismiss the modal.
- Vue disabled the command whenever the shared adapter resolved no target View.

## Implementation

- Removed the View-dependent disabled binding and its one-use predicate.
- Added one early return to the existing click handler, before dismiss/open
  emits, so no-target clicks keep the dialog open while target clicks preserve
  the existing View-first navigation.
- Added a source contract for always-available presentation and the no-target
  guard. The component shrank from 118 to 114 lines.

## Validation

- Focused `npm test -- payload.test.ts`: 82 tests passed before the final
  stricter disabled-source assertion; the full suite below covers final state.
- Full `npm test`: 14 files and 178 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `docker compose up -d --build` passed. Compose retained the prior frontend
  container image, so `docker compose up -d --force-recreate --no-deps frontend`
  explicitly attached image
  `sha256:4765222fedd4d9f671e93aa4ba4004ed4f906ba1038254c34539a3d983050f04`.
- Backend `/test` returned 465 bytes; MySQL/Redis were healthy and `db-migrate`
  was `Exited (0)`.
- `python scripts/check_repo_harness.py` and `git diff --check` passed before
  the evidence update; final checks are recorded by the completed commit.
- A deterministic null-View/null-object `SW_SYS_MSG` row was marked from state
  0 to 1 by the authenticated Vue polling path.
- Desktop rendered one enabled `查看详细` command in a 211.90px dialog.
- At 390x844 the dialog stayed between x=89.05 and x=300.95, with viewport and
  document widths both 390px. Clicking the command retained one dialog and the
  unchanged `http://localhost:8081/` URL. Browser console errors remained empty.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-message-empty-target/message-empty-target-mobile.png`

## Skipped Checks And Risks

- The full old FoolFrame application was not booted; checked-in Jade and
  JavaScript supplied the old interaction contract.
- The existing authenticated session was reused, so no fresh CAPTCHA was
  generated or read for this frontend-only slice.
- The seeded `2099-01-02` message rendered as `1970-01-01 00:00:02`, exposing
  a separate existing message-time alias/parsing mismatch. It is intentionally
  excluded from this atomic interaction slice and should be corrected next.
