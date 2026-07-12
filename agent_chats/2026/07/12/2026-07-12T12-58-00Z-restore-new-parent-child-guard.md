# Restore New Parent Child Guard

## Prompt

Keep old layout and interaction logic aligned while allowing visual
modernization, and commit each behavior atomically.

## Scope

- Render View-defined child tabs while the parent object is new.
- Route all child-add paths through one new-parent guard.
- Show the old success-style `操作提示` modal and exact message.
- Prevent candidate, child-route, and local-add work before the parent is saved.

## Changed Files

- `frontend/src/App.vue`
- `frontend/src/ViewDetailPanel.vue`
- `frontend/src/payload.test.ts`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/12/2026-07-12T12-58-00Z-restore-new-parent-child-guard.md`

## Validation

- `cd frontend && npm test -- --run`: 10 files, 151 tests passed, including
  new-parent child rendering, exact info-modal content, and shared guard usage.
- `cd frontend && npm run build`: TypeScript and Vite production build passed.
- `docker compose build frontend`: passed.
- `docker compose up -d --no-deps frontend`: current frontend deployed on port
  8081.
- `python scripts/runtime_doctor.py`: all checks passed, including `/new100`,
  owner-aware new routes, and surrounding child save/query surfaces.
- `python scripts/check_repo_harness.py`: passed.
- `docker compose ps -a`: backend/frontend are up, MySQL/Redis are healthy, and
  `db-migrate` is `Exited (0)`.

## Source Evidence

- `detailView.jade` renders `view.Data.Items` independently of the parent `opt`.
- `detailview.js additem(...)` checks `opt == 'new'` before every add branch and
  calls `showerror.showinfo('请先保存当前内容，再新建子项')`.
- `showerror.js` and `default.jade` render that call as `操作提示`, `操作成功`,
  the message, and a `确定` close button.

## Risks And Follow-Ups

- Runtime browser acceptance needs a fresh CAPTCHA confirmation.
