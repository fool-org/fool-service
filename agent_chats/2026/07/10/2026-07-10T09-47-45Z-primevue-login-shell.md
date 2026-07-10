# PrimeVue Login And Shell

## Prompt

- Apply the approved PrimeVue/Nora visual system to the login page and
  authenticated application shell.
- Preserve authentication, menu, message polling, message navigation, and
  logout behavior.

## Scope

- Replaced login controls with PrimeVue Card, form controls, Message, and
  Buttons while retaining CAPTCHA and submit/reset semantics.
- Replaced shell status, user actions, notification count, and message overlay
  with Tag, Badge, Button, and Popover components.
- Added the responsive Drawer navigation below 1024px while keeping the same
  menu ViewId handlers.
- Updated the shell palette, spacing, brand treatment, and responsive layout.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/LoginPanel.vue`
- `frontend/src/ShellActions.vue`
- `frontend/src/style.css`
- `tasks.md`
- `agent_chats/2026/07/10/2026-07-10T09-47-45Z-primevue-login-shell.md`

## Validation

- `cd frontend && npm test && npm run build`
  - 130 tests passed; the Vite production build passed.

## Runtime Artifacts

- Final browser artifacts are collected after all component phases and the
  Docker frontend rebuild.

## Skipped Checks

- Authenticated browser interaction is deferred until the full interface uses
  one visual system.

## Risks And Follow-Ups

- The Vite build reports one 508.31 kB application chunk after PrimeVue
  imports. The final phase must split vendor code or otherwise clear the
  warning without adding another framework.
