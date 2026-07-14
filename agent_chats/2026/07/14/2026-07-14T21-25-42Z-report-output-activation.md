# Report Output Activation Parity

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Traced FoolFrame `mkreport.initquery` and its separate candidate `change`
  handler before changing Vue.
- Kept the first loaded report candidate selected but deferred its output
  methods until an actual candidate change, matching the old DOM timing.
- Preserved explicit Add for multiple real types, automatic Add for one real
  type, and Add no-op for zero types.
- Added only a component-local activation flag; no report DTO, shared business
  state, request shape, abstraction, or dependency was added.

## Changed Files

- `frontend/src/ReportOutputSelector.vue`
- `frontend/src/ViewReportPanel.test.ts`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T21-25-42Z-report-output-activation.md`

## Validation

- `cd frontend && npx vitest run src/ViewReportPanel.test.ts`
  (1 file, 6 tests passed)
- `cd frontend && npm test` (19 files, 198 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- Built exact implementation commit `31e0174d` from a clean archive as image
  `sha256:45e35c7e3cb2c85007f5a77fe08e01abf78a61306e8eea5179c285426b76cc06`.
  The image contains entry bundle `assets/index-5IiJ9C5u.js` and report chunk
  `ViewReportPanel-DLsR73qh.js`.
- Runtime checks returned HTTP 200 from the exact frontend image on port 8081
  and the concurrently running backend smoke container on port 8080.
- Authorized isolated-browser acceptance read the local CAPTCHA and logged in
  with `admin/admin`. An HTTP-200 `getmkqview` response supplied candidates with
  two, zero, and one real output types.
- Initial setup selected the two-type candidate but rendered zero output-method
  options and zero selected outputs. Changing to the zero-type candidate kept
  both lists empty, and Add remained a no-op.
- Changing to the two-type candidate exposed two methods; explicit Add produced
  one selected output. Changing to the one-type candidate exposed one method
  and automatically produced the second selected output. Repeating zero/one
  changes did not duplicate it.
- Report Cancel closed the setup dialog. Logout returned HTTP 200.
- Post-acceptance database checks retained 8 `market_order` rows and 4
  `market_order_item` rows. Order 1001 remained `BTC-USDT`, state `0`, customer
  `3001`, amount `0.2500000000`, and price `62500.0000000000`.

## Risks And Follow-ups

- The three output-type cardinalities were exercised with a synthetic HTTP-200
  report-metadata response because seeded metadata does not cover all branches;
  authentication, routing, rendering, commands, and logout remained live.
- The backend container belongs to concurrent initialization smoke work, so this
  slice reused it without rebuilding or changing that unrelated work.
- Concurrent Agent Session, installation, app-manage, Maven, and item-route
  work remains unrelated and unstaged.
