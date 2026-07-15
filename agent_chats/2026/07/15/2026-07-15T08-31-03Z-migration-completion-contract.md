# FoolFrame Migration Completion Contract

## Prompt

Continue closing out and complete the FoolFrame migration.

## Scope

- Re-audited the original Docker/Java/Vue migration plan, the adjacent
  `../FoolFrame` source, the current parity document, task board, running
  Docker stack, and validation commands.
- Locked the production boundary at 469 compiled Server C# sources, 25 old
  Express routes, 25 public `IDataService` operations, 43 old application
  frontend assets, and 118 imported Views.
- Added a static migration completion contract that compares the live adjacent
  legacy checkout with the current Spring route aliases, Vue page checks,
  coverage documents, and View matrix.
- Marked the defined migration scope complete while keeping explicit reopen
  rules for concrete source/runtime drift.
- Preserved unrelated, pre-existing Agent authorization design changes in
  `docs/agent-sessions.md`, `docs/authorization-and-agent-risk-control.md`, and
  the Agent section of `tasks.md`.

## Changed Files

- `AGENTS.md`
- `README.md`
- `docs/migration/foolframe-parity.md`
- `docs/migration/foolframe-server-assets.md`
- `docs/superpowers/plans/2026-06-23-fool-service-migration.md`
- `docs/validation.md`
- `scripts/legacy_migration_contract.py`
- `scripts/legacy_migration_contract_test.py`
- `scripts/check_repo_harness.py`
- `scripts/check_repo_harness_test.py`
- `tasks.md` (migration-closeout section only)
- `agent_chats/2026/07/15/2026-07-15T08-31-03Z-migration-completion-contract.md`

## Validation

- `python scripts/legacy_migration_contract.py --require-legacy` — passed;
  469 C# sources, 25 Web routes, 25 `IDataService` operations, and 118 Views.
- `PYTHONPATH=scripts python -m unittest scripts/legacy_migration_contract_test.py`
  — 3 tests passed.
- `python scripts/check_repo_harness_test.py` — 11 tests passed.
- `python scripts/runtime_doctor_test.py` — 50 tests passed.
- `python scripts/check_repo_harness.py` — passed.
- `PYTHONPATH=scripts python scripts/legacy_view_matrix.py` — 118/118 passed.
- `python scripts/runtime_doctor.py` — every Docker/schema/auth/View/data/
  report/message check passed; `db-migrate` remained `Exited (0)`.
- `cd frontend && npm test -- --run` — 227 tests passed.
- `cd frontend && npm run build` — production build passed.
- Java 17 Compose-network Maven reactor — all 16 modules and their tests passed
  with `BUILD SUCCESS` in the same closeout run.
- `python -m py_compile scripts/legacy_migration_contract.py scripts/check_repo_harness.py`
  — passed.
- `git diff --check` — passed.

## Runtime Evidence

- Running backend: `fool-service-backend:latest`, image
  `sha256:df0aa623e72f96023bc03cf79140edffa2e4b560588f2bb7bccfe77a716bc2bc`,
  created `2026-07-15T05:36:22Z`.
- Running frontend: `fool-service-frontend:latest`, image
  `sha256:1a590ca9b517a9941961ac166eb2aa67aa2cf1e066584c494f263a31b498c5d8`,
  created `2026-07-15T07:49:00Z`.
- Runtime catalog: 118 Views (`chart=1`, `detail=57`, `list=58`,
  `map-panel=1`, `sudoku=1`), 118/118 metadata/data checks passed.

## Skipped Or Downgraded Checks

- A fresh `docker compose build backend frontend` was attempted. Docker
  BuildKit remained blocked while loading Docker Hub metadata for the Maven,
  Temurin, Node, and Nginx base images and produced no Dockerfile/build-stage
  failure; the hanging registry request was terminated.
- The residual packaging risk is bounded by the current same-day backend and
  frontend images, the successful Java 17 Maven reactor, the successful Vue
  production build, and the fully passing runtime doctor. Re-run the same
  Compose build when Docker Hub metadata is responsive.

## Risks And Follow-ups

- Local `main` was already seven commits ahead of `origin/main`; this closeout
  is not remote delivery until its scoped commit is pushed.
- New Agent authorization/security work remains open in `tasks.md`, but it is
  post-migration product development and is explicitly outside the closed
  FoolFrame migration contract.
- Any future legacy snapshot change, newly reachable unsupported metadata,
  reproducible old/new behavior mismatch, or failing migration gate reopens
  migration status automatically.
