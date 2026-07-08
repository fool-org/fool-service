# Vue Detail Groups View First

## Prompt

Continue the FoolFrame Docker/Vue migration, keep commits atomic, maximize
reuse, and keep View rendering ahead of data/DTO binding.

## Scope

- Traced the Vue detail render helper that merges loaded read-item View
  metadata with `querydatadetail` child-group data.
- Stopped `querydatadetail` groups that are missing from read-item
  `DetailViews` from being appended into rendered Vue child sections.
- Kept DTO groups as value sources only for child groups declared by the
  rendered read-item View.
- Updated migration docs and task state.

## Changed Files

- `frontend/src/viewWorkflow.ts`
- `frontend/src/viewWorkflow.test.ts`
- `docs/migration/foolframe-parity.md`
- `tasks.md`

## Validation

- Red: `npm test -- viewWorkflow.test.ts` failed because a DTO-only child
  group was appended, producing two rendered child groups instead of one.
- Green: `npm test -- viewWorkflow.test.ts` passed.
- Frontend: `npm test` passed.
- Build: `npm run build` passed.
- Harness: `python scripts/check_repo_harness.py` passed.
- Whitespace: `git diff --check` passed.

## Runtime Evidence

`docker compose ps` showed MySQL, Redis, backend, and frontend up. No Docker
runtime doctor was used for this slice. The change is covered by the frontend
View workflow helper test and does not require a backend container rebuild.

## Risks

- If a legacy read-item View omits a child section that operators still expect,
  the section will now stay hidden until the metadata migration installs the
  missing `DetailViews` entry. That matches the View-first rendering contract.

## Follow-ups

- Continue checking remaining Vue detail/save paths for data-driven structure
  fallbacks before broadening runtime parity.
