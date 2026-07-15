# ItemView Route State Reset

## Prompt

Continue aligning old page layout, style, and interaction logic in atomic
commits, then report the remaining migration work.

## Scope

- Ran a representative authenticated frontend completion replay after the
  43-file old Web asset audit.
- Found that history navigation from `/new122` to `/itemview100` retained the
  previous new-object state and rendered an edit grid titled `OrderList -新建`.
- Reset pending edits and `isCreatingObject` in the shared ItemView route entry
  before loading metadata.
- Added no View-id branch, component, endpoint, data merge, or business DTO.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/payload.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T07-49-38Z-itemview-route-reset.md`

## Validation

- `cd frontend && npm test` (21 files, 227 tests passed).
- `cd frontend && npm run build` (passed).
- `PYTHONPATH=scripts python scripts/legacy_view_matrix.py` (118/118 passed).
- `python scripts/runtime_doctor.py` (69 checks passed).
- `python scripts/check_repo_harness.py` (passed).
- `git diff --check` (passed).

## Runtime Evidence

- Authorized `admin/admin` browser replay covered chart `/view100`, report
  `/view101`, Sudoku `/view103`, detail edit `/view112/1`, new `/new122`, and
  metadata-only `/itemview100` at 1280x900 and 390x844.
- The failing pre-fix transition rendered `OrderList -新建`, an edit grid, and
  no read values. After the fix, both viewports rendered `你好` and kept zero
  initially active child panels.
- Chart and map surfaces had nonzero geometry; the report exposed `输出`,
  `条件`, and `保存报表`; detail Edit/Save states matched the old workflow.
- Browser errors, internal request failures, write endpoints, and horizontal
  overflow were all zero. MySQL retained admin id 1, eight orders, and four
  order items; order 1001 remained `BTC-USDT`.
- Browser summary:
  `artifacts/runs/20260715-frontend-completion/summary.json`
  (`495015bdf215564d8665ad19a245c3704ee4b166d092d8098cc75925bf4af996`).
- Before/after evidence:
  `desktop-itemview-failure.png`
  (`324129cb793cc2437f9cd390bf97a8cfc54df024787a7bea6c1c602414661bba`),
  `desktop-itemview-aligned.png`
  (`b4b4d7da91e5e4e8eec4b28dd5afbd17fd7ba5eed4cdc3bd8f7bed6b1c72ed18`),
  and `mobile-itemview-aligned.png`
  (`a757e87f66fdf1a15b17f3af5ba12d68c97ed93326977e9baccd5c5370b4fb93`).
- Host and container `index.html` SHA-256 matched at
  `56a8d7f1ce88578d079ba3ef9a727b2c2184a646d26abe5441d45a220e2208a9`.
- Runtime frontend image:
  `sha256:1a590ca9b517a9941961ac166eb2aa67aa2cf1e066584c494f263a31b498c5d8`.

## Skipped Or Downgraded Checks

- `docker compose build frontend` remained blocked by the local Buildx
  activity-file permission. The clean validated `dist` was injected into a
  fresh Nginx image and the Compose frontend was force-recreated.

## Risks And Follow-ups

- Screenshot and JSON runtime artifacts are ignored by Git; their paths and
  hashes are recorded above.
- Unrelated authorization/session documents and `docs/superpowers/` remain
  untouched and must not be staged with this commit.
