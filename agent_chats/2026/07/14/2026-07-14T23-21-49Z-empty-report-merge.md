# Empty Report Merge

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Compared `view.jade` and `mkreport.js` before changing report state.
- Removed the Vue-only `请选择要合并的条件` feedback for a merge click with no
  selected conditions.
- Kept the safe empty merge as a silent no-op instead of reproducing the old
  JavaScript exception.
- Left one-unit/non-contiguous feedback, valid grouping, report metadata, DTOs,
  requests, and component structure unchanged.

## Changed Files

- `frontend/src/reportConditions.ts`
- `frontend/src/reportConditions.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T23-21-49Z-empty-report-merge.md`

## Validation

- `cd frontend && npm test -- --run reportConditions.test.ts ViewReportPanel.test.ts payload.test.ts`
  (3 files, 103 tests passed)
- `cd frontend && npm test` (20 files, 213 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `python scripts/runtime_doctor.py` (68 checks passed)
- Exact implementation commit: `317efc30`.
- Clean runtime image:
  `sha256:1678b94c10769ada34071d57b59331c1aa0a54f98e2b2fc76993f09b6cbe47b2`;
  it contains only current report chunk `ViewReportPanel-zAlcf9cA.js`.
- Authorized `admin/admin` browser acceptance opened `/view101`, then report
  Conditions with zero condition rows. Clicking the unique `合并分组` command
  retained one report dialog, zero rows, no message, and the same route.
- Nginx showed the initial HTTP-200 `getmkqview` at 23:18:14 UTC; every later
  request through the click observation was a scheduled `getmsg` poll. No
  `mkrpt`, `saverpt`, or other report request was emitted.
- The 1280px document exactly matched its viewport, browser warnings/errors
  were empty, and logout returned HTTP 200.
- MySQL retained 8 orders, 4 order items, and order 1001 as `BTC-USDT`, state
  `0`, customer `3001`, amount `0.2500000000`, price `62500.0000000000`.
- Visible evidence:
  `artifacts/runs/20260715-empty-report-merge/empty-merge-no-feedback.png`
  (`1280x720`, SHA-256
  `9766fb10cd0047f3f0988007cc03cfa13715a92e683893a680ba2ae3feaf8149`).

## Risks And Follow-ups

- The local Docker registry proxy still prevented the standard two-base
  Dockerfile build with `only one connection allowed`. Runtime acceptance used
  the already validated host `dist` in a clean existing Nginx image; rerun the
  standard Dockerfile build when that proxy recovers.
- Screenshot artifacts are intentionally ignored by Git; the path and hash are
  recorded above.
- `docs/superpowers/` remains unrelated and untracked; this slice did not edit
  or stage it.
