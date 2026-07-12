# Current Login Browser Acceptance

## Prompt

Continue aligning Vue layout, style, and interaction behavior with FoolFrame,
allowing visual polish while preserving the old workflow, and commit each
change atomically.

## Scope

- Recheck the deployed signed-out page at desktop and 390x844 against current
  source and runtime state.
- Verify viewport containment and horizontal overflow through DOM geometry.
- Verify Reset and Refresh field/CAPTCHA state transitions without reading the
  CAPTCHA content or attempting authentication.

## Changed Files

- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T23-09-32Z-current-login-browser-acceptance.md`
- Runtime artifact (gitignored):
  `artifacts/runs/20260712-login-layout-current/login-dom.json`

## Validation

- Desktop 1440x900: document width equals viewport width; the centered 240px
  form spans x=600..840 and every visible control remains inside the viewport.
- Mobile 390x844: document width equals viewport width; the centered 240px form
  spans x=75..315 and every visible control remains inside the viewport.
- Reset after filling inert test text clears username, password, and CAPTCHA;
  the CAPTCHA image resource changes.
- Refresh after filling inert test text preserves username/password, clears the
  CAPTCHA field, changes the image resource, and keeps mobile scroll width at
  390px.
- No CAPTCHA characters were read, transcribed, or submitted.

## Source Audit

- Report, login, list-row operations, viewWithChart, message polling, field
  editors, and legacy static routes were compared against FoolFrame before this
  browser check. No new source change was justified by those paths.
- `/about` and `/contact` contain only Visual Studio template placeholder text
  in FoolFrame and have no product navigation or business interaction; the Vue
  route fallback remains intentional.

## Risks And Follow-Ups

- Authenticated current-build desktop/mobile acceptance still requires fresh
  permission to read and fill the current local CAPTCHA with `admin/admin`.
