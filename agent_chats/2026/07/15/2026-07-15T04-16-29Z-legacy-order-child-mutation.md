# Legacy Order Child Mutation Acceptance

## Prompt

Continue aligning old layout, style, and interactions in atomic commits, with
View-first rendering and no concrete business DTO binding.

## Scope

- Used OrderDetail Views 102/101 to exercise the shared detail collection.
- Performed real browser candidate selection, staged parent save, and staged
  child deletion at desktop and mobile widths.
- Rechecked OrderList operation rendering against old `querylistdata.js`
  before deciding whether a model-operation request should run.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T04-16-29Z-legacy-order-child-mutation.md`

## Validation

- Authorized browser acceptance with local CAPTCHA and `admin/admin`.
- `python scripts/runtime_doctor.py` (69 checks passed after cleanup).
- `PYTHONPATH=scripts python scripts/legacy_view_matrix.py` (118/118 passed).
- `python scripts/check_repo_harness.py` (passed).
- Application code, frontend source, and backend source were unchanged, so
  frontend and Maven test suites were not rerun.

## Runtime Evidence

- Seeded temporary order `9915071501`, child `9915071511` (`CDX-KEEP`), and
  unowned child `9915071512` (`CDX-MOVE`).
- Desktop `/view102/9915071501` -> `Items` -> `增加` loaded candidate View 101;
  selecting `CDX-MOVE` and saving persisted its owner as `9915071501`.
- Mobile 390x844 acceptance deleted `CDX-MOVE` from the collection and saved;
  MySQL confirmed that dedicated child id no longer existed.
- Observed endpoints: `getreaditemview`, `querydatadetail`, `getlistview`,
  `querydata`, and `saveobj`.
- OrderList displayed inert metadata names `删除` / `保存`, exposed zero
  operation buttons, and sent zero `runoperation` requests. This matches old
  Web behavior because both operations have `ViewId=0`.
- Mobile document/viewport width was 390/390. Browser runtime/log event count
  was zero.
- Cleanup removed the temporary order and both dedicated item ids. Their final
  combined MySQL count was zero.
- Visible evidence:
  - `artifacts/runs/20260715-legacy-order-child-mutation/desktop-child-added.png`
    (`653707793b2139468b7b967bceef385a55015a2368ad0c820721ba0c4d86943f`).
  - `artifacts/runs/20260715-legacy-order-child-mutation/mobile-child-deleted.png`
    (`13c68a9836a1f54cb8999f6c39b8275cd3c48553a371d7b9ef1175fc73941380`).
  - `artifacts/runs/20260715-legacy-order-child-mutation/mobile-inert-model-operations.png`
    (`ae6fc0e634e36b4079545358c7bfbf2d83852292c44211ddfbc4bb9998bbf291`).

## Risks And Follow-ups

- The browser flow covers the collection's candidate-add and staged-delete
  paths. The shared runtime doctor separately covers child update persistence.
- Seeded OrderList operations intentionally have no target View id, so making
  them executable in the Vue list would diverge from old Web behavior.
- Screenshot files are ignored by Git; paths and hashes are recorded above.
- `docs/superpowers/` remains unrelated and untracked; it was not edited or
  staged.
