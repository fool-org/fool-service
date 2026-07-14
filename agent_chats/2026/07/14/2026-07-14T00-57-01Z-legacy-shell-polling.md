# Legacy Shell Polling

## Prompt

Continue aligning old-page layout, style, and interaction behavior, committing
every change atomically while retaining View-first rendering and code reuse.

## Scope

- Compare old `default.jade` user rendering and `message.js` timer registration
  with the Vue authenticated-shell startup path.
- Remove the invented immediate message request and repeated user-info poll.
- Reuse the initial `getmain` View-shell response for user presentation.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T00-57-01Z-legacy-shell-polling.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/default.jade` renders `data.User.UserAvtarUrl`
  and `data.User.UserName` from the server-provided main-page model.
- `../FoolFrame/src/Web/public/javascripts/app/message.js` only registers its
  `getmsg` callback with `timer.reg(..., 15)`.
- `../FoolFrame/src/Web/public/javascripts/app/timer.js` invokes registered
  callbacks only after their interval counter reaches the configured value.
- Vue previously ignored `getmain.User`, immediately ran a combined shell
  refresh after login, and repeated both `getuserinfo` and `getmsg` every 15
  seconds.

## Implementation

- Pointed the existing user-name and avatar adapters at `getmain` data and
  removed the duplicate user-response ref and import.
- Replaced the combined status refresh with one best-effort message poll that
  only calls `getmsg` and retains the existing in-flight guard.
- Removed the startup poll so authenticated-shell entry only starts the timer.
  `App.vue` shrank by two lines while eliminating one response state and one
  repeated request path.
- Strengthened source-contract tests for main-response user binding and the
  absence of an immediate poll or frontend `getuserinfo` call.

## Validation

- Focused `npm test -- payload.test.ts`: 82 tests passed.
- `docker compose build frontend` and forced frontend recreation passed with
  image `sha256:3d78d766dddb77ff8af1c2948504965b36619f66b289d34e5f2ddc85966d1d48`.
- A fresh locally authorized CAPTCHA and `admin/admin` login displayed `Admin`
  immediately from `getmain`.
- The seeded system-message dialog was absent about 1.5 and 8.5 seconds after
  login, appeared after the first 15-second interval, and changed its database
  state from 0 to 1 through the real polling path.
- At 1280px the viewport and document widths both remained 1280px and browser
  console errors were empty. The test session was logged out and its message
  row was deleted.
- Full frontend tests/build, repository harness, Compose health, and
  `git diff --check` passed before the atomic commit.

## Runtime Evidence

- `artifacts/runs/20260714-legacy-shell-polling/message-first-poll-desktop.png`

## Skipped Checks And Risks

- The full old FoolFrame application was not booted; checked-in Jade and
  JavaScript supplied the old startup and polling contract.
- The final browser viewport capability remained at 1280px instead of the
  requested 390px. This slice changes request timing and user data source only;
  the responsive shell markup and CSS were not changed, and their 390px state
  was validated by the immediately preceding message slices.
