# Candidate Confirm Placeholder

## Prompt

Continue aligning old page layout, style, and interactions in atomic commits,
keeping files controlled, logic reusable, and rendering View-first.

## Scope

- Compared `detailView.jade` before its data controller and confirmed that the
  select-existing footer visibly renders `取消` followed by an eventless
  `确定` command.
- Restored only that missing visible no-op command in the existing Vue dialog.
- Kept row-level `选择` as the only confirmation path; added no state, request,
  DTO binding, handler, or component.
- Browser-verified the exact implementation commit `7da1b4b9` on the real
  Docker Items candidate flow.

## Changed Files

- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/ViewDetailPanel.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/14/2026-07-14T23-11-58Z-candidate-confirm-placeholder.md`

## Validation

- `cd frontend && npm test -- --run ViewDetailPanel.test.ts payload.test.ts`
  (2 files, 90 tests passed)
- `cd frontend && npm test` (20 files, 213 tests passed)
- `cd frontend && npm run build`
- `python scripts/check_repo_harness.py`
- `python scripts/runtime_doctor.py` (68 checks passed)
- Compose retained backend/frontend/mysql/redis up, MySQL/Redis healthy, and
  `db-migrate` at `Exited (0)`; `/test` and frontend `/` returned HTTP 200.
- Authorized `admin/admin` browser acceptance opened `/view100/1001`, then the
  View-derived Items candidate dialog. It rendered exactly one enabled
  `取消` and one enabled `确定` command in the old order.
- Before and after clicking `确定`, the URL stayed `/view100/1001`, one candidate
  dialog remained visible, and the detail table retained three child rows.
- Nginx access-log count changed from 136 to 137 during the click check; the
  only added request was the scheduled `/api/v1/message/getmsg` poll. No
  candidate query, save, add, or navigation request was emitted.
- At 1280x720, Cancel occupied x=961..1015 and Confirm x=1020..1074 on one row;
  document and viewport widths were both 1280px.
- At 390x844, the dialog occupied x=16..374, both commands stayed on one row,
  and document and viewport widths were both 390px. Browser warnings/errors
  were empty and logout returned HTTP 200.
- Runtime image:
  `sha256:671bda982089ffad5c49c81be527d63131a50880dff8ce2c161dead51a93123b`.
- Visible evidence:
  - `artifacts/runs/20260715-candidate-confirm-placeholder/desktop-before-confirm.png`
    (`1280x720`, SHA-256
    `a7d4b5fdc4ab3207600c0e532ef457489148c09a0bdd506cedf52a0f85d3043f`).
  - `artifacts/runs/20260715-candidate-confirm-placeholder/mobile-after-confirm.png`
    (`390x844`, SHA-256
    `8286f02259fecd70035079e9b6d4f24d867f3b4ee64c7d291451a2b8fad5f2d7`).

## Risks And Follow-ups

- `docker compose up -d --build` could not finish because the local Docker
  builder's registry proxy returned `only one connection allowed` while
  resolving the two already-cached base images. The same host-validated `dist`
  was copied into a temporary container from the existing Nginx image and
  committed for runtime acceptance. Re-run the standard Dockerfile build when
  the local registry proxy recovers.
- Screenshot files are intentionally ignored by Git; their paths and hashes are
  recorded above.
- `docs/superpowers/` remains unrelated and untracked; this slice did not edit
  or stage it.
