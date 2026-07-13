# Legacy Dialog Chrome Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files small and reuse shared code.

## Scope

- Restore Bootstrap modal frame, section spacing, dividers, and title scale.
- Preserve each dialog's width, content, commands, and state behavior.

## Changed Files

- `frontend/src/style.css`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T14-26-00Z-legacy-dialog-chrome.md`

## Legacy Evidence

- Bootstrap 3 modal content uses a 1px translucent border, 6px radius,
  background clipping, and 0x5x15px 0.5-black shadow.
- Modal header, body, and footer use 15px padding; header/footer use 1px
  `#e5e5e5` dividers.
- `.modal-title` is 18px, weight 500, at Bootstrap's 1.42857 line height.
- PrimeVue dialogs retained 20px spacing and omitted the old section dividers.

## Implementation

- Added one global `.p-dialog` chrome block because every Dialog in this
  frontend migrates an old Bootstrap modal.
- Kept report content grid/flex overrides and candidate/report width rules.
- Removed the obsolete report-only 1.05rem title override so the shared 18px
  title contract applies consistently.
- Kept the browser/PrimeVue focus-visible outline on the close button for
  keyboard accessibility.

## Validation

- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Direct frontend image build and forced Compose recreation passed. Deployed
  image id:
  `sha256:e300fada93b3b1f2761afa0ff1fa251eaf92b1cd3d4eb72c37b018651467a469`.
- Authenticated `/view102/1001` candidate computed 15px header/body/footer
  padding, `#e5e5e5` 1px dividers, 18px/500/25.714px title typography, 1px
  translucent frame, 6px radius, and the old 5x15px shadow.
- Desktop and 390x844 screenshots show stable dialog width, no title/control
  overlap, and unchanged responsive query/footer placement.
- Repository harness validation passed; frontend `/` and frontend-proxied
  `/test` returned HTTP 200. MySQL and Redis remained healthy and `db-migrate`
  remained `Exited (0)`.

## Runtime Evidence

- `artifacts/runs/20260713-legacy-dialog-chrome/candidate-desktop.png`
- `artifacts/runs/20260713-legacy-dialog-chrome/candidate-mobile.png`

## Risks And Follow-Ups

- This is intentionally global because the current frontend has no non-legacy
  Dialog surface; a future native Vue dialog should opt out explicitly.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
