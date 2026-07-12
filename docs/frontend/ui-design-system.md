# Vue UI Design System

This document is the source of truth for the Fool Service Vue interface.
`tasks.md` tracks delivery status; migration behavior remains documented in
`docs/migration/foolframe-parity.md`.

## Direction

- PrimeVue 4 styled mode with the Nora preset.
- Modern enterprise visual language: indigo actions, slate surfaces, compact
  data density, and clear focus states.
- Light theme only for the first release. The token structure may support a
  later dark preset, but the application does not expose a theme switch.
- Preserve the View-first routes, DTOs, request payloads, component events,
  and legacy deep-link behavior. This is a presentation upgrade, not a new
  workflow.

## Tokens

| Role | Value |
| --- | --- |
| Primary | `#4F46E5` |
| Primary hover | `#4338CA` |
| Primary active | `#3730A3` |
| Page background | `#F8FAFC` |
| Surface | `#FFFFFF` |
| Border | `#E2E8F0` |
| Text | `#0F172A` |
| Muted text | `#64748B` |
| Success | `#059669` |
| Warning | `#D97706` |
| Danger | `#DC2626` |

- Controls use an 8px radius and a compact 38px target height.
- Cards use a 12px radius and restrained elevation.
- Table rows target 44px while allowing wrapped metadata values.
- Keyboard focus uses a visible 2px primary ring.
- The system font stack remains the only font dependency.

## Component Policy

- Import PrimeVue components directly from their package paths. Do not add an
  auto-import plugin or a second component framework.
- PrimeVue owns buttons, form controls, data tables, pagination, messages,
  tabs, badges, popovers, drawers, and tags.
- Project CSS owns the application shell layout, responsive breakpoints,
  View-specific composition, and the existing SVG chart and Leaflet map.
- `ListDataTable` remains the shared View-row renderer; PrimeVue DataTable is
  an implementation detail behind its existing props and events.
- Dynamic model fields continue to emit legacy string values. UI controls must
  not change save payload types or lookup request timing.

## Responsive Contract

- Desktop (`>= 1024px`) follows the old Web shell with AppInfo branding,
  Home/TopMenu/SubMenu navigation, and user actions in one top header. List and
  standalone detail routes each use the full workspace width.
- Narrow layouts keep AppInfo/user actions in the header and use a Drawer for
  the same Home/TopMenu/SubMenu entries.
- At 390x844 the document body must not overflow horizontally. Wide tables may
  scroll inside their DataTable container.
- Detail fields, child-add controls, and report conditions collapse to one
  column below 640px; list/action toolbars wrap, while captcha, messages,
  menus, and buttons must stay inside the viewport.

## Acceptance

- `cd frontend && npm test && npm run build`
- `docker compose up -d --build && python scripts/runtime_doctor.py`
- `python scripts/check_repo_harness.py`
- Authenticated browser acceptance at 1440x900 and 390x844 covering the
  default list, search, detail, create/save, child rows, lookup, operation,
  report, message, chart, Sudoku, map, logout, and legacy detail/new routes.
- Screenshot and console evidence lives under
  `artifacts/runs/<run_id>/ui-upgrade/`; delivery summaries reference the
  artifact path without committing generated images.
