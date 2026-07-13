# Align Operation Result Confirm

## Prompt

Continue old-page interaction parity and keep every behavior in an atomic
commit.

## Scope

- Compare old `operation.js`, `showerror.js`, and `default.jade` with the Vue
  operation-result dialog.
- Restore the old `确定` footer command without changing operation requests,
  result copy, or dismiss behavior.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/13/2026-07-13T08-01-15Z-align-operation-result-confirm.md`

## Validation

- `cd frontend && npm test -- --run src/payload.test.ts`: 82 tests passed.
- `cd frontend && npm test -- --run && npm run build`: 153 tests passed and
  the production build completed; `ViewDetailPanel` is 72.84 kB.
- `BUILDX_BUILDER=desktop-linux docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: recreated and started the
  frontend from image
  `sha256:55fb41f6d1c0664f27ee4ff0d5d98c7d2342efbacbf2d7203a27b3b6778f46bd`.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: frontend/backend/MySQL/Redis running; `db-migrate`
  exited successfully with code 0.
- `git diff --check`: passed before delivery record creation.

## Source Evidence

- Old `operation.js` calls `showdetailinfo`, which renders `#info-dialog`.
- Old `default.jade` gives that dialog a single `确定` footer command.
- Vue already matched the title, success/failure summary, return message, and
  dismissal event; only the command label differed.

## Risks And Follow-Ups

- Visible operation-result dismissal and Sudoku refresh clicking remain
  pending a newly authorized browser CAPTCHA.
- The complete runtime doctor was not rerun because it would consume another
  CAPTCHA beyond the preceding authorization.
