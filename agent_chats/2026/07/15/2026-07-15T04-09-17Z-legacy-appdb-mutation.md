# Legacy ApplicationDatabase Mutation Acceptance

## Prompt

Continue aligning old layout, style, and interactions in atomic commits, with
View-first rendering and no concrete business DTO binding.

## Scope

- Audited FoolFrame `AutoViewFactory` and current operation metadata first.
- Confirmed all 56 imported generated list pairs use `新建` / `编辑` only as
  result-View navigation; they do not execute a delete model operation.
- Used ApplicationDatabase Views 123/122 as a reversible imported scalar-write
  representative because `DB_AppDB` has an exact dedicated cleanup key.
- Performed real browser create and update requests at desktop and mobile.

## Changed Files

- `docs/migration/foolframe-parity.md`
- `tasks.md`
- `agent_chats/2026/07/15/2026-07-15T04-09-17Z-legacy-appdb-mutation.md`

## Validation

- Authorized browser acceptance with local CAPTCHA and `admin/admin`.
- `python scripts/runtime_doctor.py` (69 checks passed after cleanup).
- Application code, frontend source, and backend source were unchanged, so
  frontend and Maven test suites were not rerun.

## Runtime Evidence

- Desktop `/view123` -> `新建` -> `/new122` persisted
  `appId=99150715, dbNo=CDX-NEW` through `savenewobj`.
- Mobile `/view122/1784088536767` -> `编辑` persisted `dbNo=CDX-EDIT`
  through `saveobj`.
- Observed endpoints: `getlistview`, `querydata`, `getreaditemview`,
  `querydatadetail`, `initnew`, `savenewobj`, and `saveobj`.
- Desktop document/viewport width was 1280/1280; mobile was 390/390.
- Browser runtime/log event count was zero.
- Cleanup deleted the dedicated `App_Id=99150715` row. `DB_AppDB` afterward
  contained only the original `App_Id=1, DBNo=01, SysId=1` row.
- Visible evidence:
  - `artifacts/runs/20260715-legacy-appdb-mutation/desktop-new.png`
    (`ec29baab7289f0b2d181fcc5a563be15f2c16c067fee7f68ce6dfb0a87e0a072`).
  - `artifacts/runs/20260715-legacy-appdb-mutation/mobile-edit.png`
    (`304facf133f6c10e2f0beb04d47b06c4e2daadc78ef68aa16d0c8faaa7c15b38`).

## Risks And Follow-ups

- This proves shared scalar new/edit/save behavior, not child collection
  mutation or model-operation execution.
- Next, use isolated Order/OrderItem rows for child add/edit/delete and the
  two real OrderList model operations, restoring both tables afterward.
- Screenshot files are ignored by Git; paths and hashes are recorded above.
- `docs/superpowers/` remains unrelated and untracked; it was not edited or
  staged.
