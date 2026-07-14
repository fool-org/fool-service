# Legacy Message Time

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Compare old `message.js` generated-time handling with the Vue system-message
  adapter.
- Restore both current camel LocalDateTime and legacy Pascal `/Date(ms)/`
  support without binding the dialog to a backend DTO shape.
- Reuse the date parser for existing list refresh-time formatting.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T00-43-04Z-legacy-message-time.md`

## Legacy Evidence

- `../FoolFrame/src/Web/public/javascripts/app/message.js` extracts the epoch
  from `GernerationTime`, creates a local `Date`, and formats it as
  `yyyy-MM-dd hh:mm:ss` before opening the system-message dialog.
- Vue extracted the first digit run from either alias. A camel ISO year was
  therefore treated as epoch milliseconds and rendered as 1970.
- The Docker API exposes camel `gernerationTime` and Pascal `GernerationTime`
  aliases for the same message instant.

## Implementation

- Replaced digit extraction and UTC serialization with one shared date parser
  that accepts ISO/LocalDateTime values and exact legacy `/Date(ms)/` wrappers.
- Formatted valid values through local date fields to match old
  `Date.prototype.format`; invalid server text remains visible unchanged.
- Reused the same parser in `listFreshTime`, removing its duplicate legacy-date
  parsing while keeping rendering behind the existing display adapter.
- Added focused coverage for camel precedence, Pascal fallback, local output,
  and invalid-text preservation.

## Validation

- Focused `npm test -- viewWorkflow.test.ts`: 47 tests passed after correcting
  the old zero-epoch fixture to be independent of the test runner timezone.
- Full `npm test`: 14 files and 179 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- `docker compose build frontend` and forced frontend recreation passed with
  image `sha256:e470df2f8e0c9821a53f076c8baccd29458a136f68b6e8f756fac0a19ff9f7ae`.
- Backend `/test` returned 465 bytes; MySQL/Redis were healthy and `db-migrate`
  was `Exited (0)`.
- A fresh locally authorized CAPTCHA and `admin/admin` login read the actual
  `/api/v1/message/getmsg` aliases; the temporary token was logged out.
- Authenticated Docker polling rendered `2099-01-01 19:04:05` in the dialog at
  390x844. Viewport and document widths were both 390px and browser console
  errors were empty.
- `python scripts/check_repo_harness.py`, final frontend tests/build, and
  `git diff --check` passed before the atomic commit.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-message-time/message-time-mobile.dom.txt`

## Skipped Checks And Risks

- The full old FoolFrame application was not booted; checked-in JavaScript
  supplied the old time-format contract.
- The backend Docker process runs in UTC while MySQL stores the seeded datetime
  under `+08:00`; the API therefore returned `2099-01-01T19:04:05` for the
  seeded `2099-01-02 03:04:05`. Both camel and Pascal aliases describe that same
  instant, and the Vue output now matches the old browser-local semantics.
  Container/JDBC timezone alignment is a separate backend concern and was not
  mixed into this frontend parity commit.
