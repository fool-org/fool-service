# Candidate Query Layout Parity

## Prompt

Continue aligning old-page layout, style, and interaction behavior, with every
change committed atomically. Keep files controlled and reuse code.

## Scope

- Compare the candidate query form's placement with `detailView.jade`.
- Reuse the main list's compact query geometry without changing data flow.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/style.css`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T13-03-07Z-candidate-query-layout.md`

## Legacy Evidence

- `../FoolFrame/src/Web/views/detailView.jade` lines 132-135 render a
  `navbar-form navbar-right` with one text input and compact Find button.
- Vue rendered a visible `查询条件` label and two equal-width controls across
  the dialog, making the query row substantially taller and wider.
- The old templates consistently intend `输入条件` as the query input prompt;
  Vue retains that prompt plus an accessible `查询条件` name.

## Implementation

- Candidate query controls now share the existing flex geometry selectors
  used by the main list instead of duplicating layout declarations.
- Desktop input width is 240px and right-aligned with the compact Find button;
  the input expands on narrow screens while the command wraps below it.
- Existing keyword state, button query, Enter query, and pending state remain
  unchanged.

## Validation

- Focused `payload.test.ts`: 82 tests passed.
- Full frontend suite: 153 tests passed.
- `npm run build`: Vue type-check and Vite production build passed.
- Compose frontend build passed with image manifest
  `sha256:cc014a510365e4ed975ff2c24d35d087bb25cdfee73c9b9630da643d5fc48233`.
- Compose frontend/backend smoke requests returned HTTP 200; MySQL and Redis
  remained healthy and `db-migrate` remained `Exited (0)`.
- Authenticated desktop `/view100/1001`: the input measured 240px, Find 54.5px,
  the toolbar right gap was zero, and no visible query label remained.
- At 390px, the dialog measured 358px, input measured 318.5px, Find wrapped
  below it, page `scrollWidth` equaled 390, and Enter still returned one row.

## Runtime Evidence

- `artifacts/runs/20260713-candidate-query-layout/desktop.jpg`
- `artifacts/runs/20260713-candidate-query-layout/mobile.jpg`

## Risks And Follow-Ups

- The responsive wrap deliberately improves the unusable old desktop-only
  form on narrow screens while retaining control order and commands.
- The CAPTCHA-backed runtime doctor was not rerun because it would generate a
  new CAPTCHA; the existing authenticated browser session covered this slice.
