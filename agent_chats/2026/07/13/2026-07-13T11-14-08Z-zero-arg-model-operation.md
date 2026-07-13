# Zero Argument Model Operation

## Prompt

Continue old-page layout, style, and interaction parity with atomic commits;
authorization was granted for the current local browser CAPTCHA.

## Scope

- Complete visible authenticated acceptance for Sudoku refresh and operation
  result dismissal.
- Diagnose the seeded detail Save failure found during that replay.
- Preserve legacy same-model operation behavior when nullable `ArgModel`
  metadata is hydrated as zero.
- Rebuild the Compose backend and replay the exact failed operation.

## Changed Files

- `fool-view/src/main/java/org/fool/framework/view/service/DataQueryService.java`
- `fool-view/src/test/java/org/fool/framework/view/service/DataQueryServiceRunOperationTest.java`
- `tasks.md`
- `docs/migration/foolframe-parity.md`
- `agent_chats/2026/07/13/2026-07-13T11-14-08Z-zero-arg-model-operation.md`

## Root Cause

- The seeded operation correctly exposed model operation `7002` and Vue sent
  `ViewId=100`, `ObjectId=1002`, and `OperationId=7002`.
- The legacy nullable `SW_MODEL_OPERATION_ARGMODEL` value was hydrated as `0`.
- `runLegacyOperation` treated every non-null argument-model id as a cross-model
  command, attempted to load model `0`, and returned the default failed result
  before reaching the normal update path.

## Validation

- Focused Maven test for zero argument-model handling: 1 passed.
- `mvn -DfailIfNoTests=false -pl fool-view -am test` in the Java 17 Maven
  container: 187 `fool-view` tests passed; dependency reactor build succeeded.
- `BUILDX_BUILDER=desktop-linux docker compose build backend`: passed; image
  manifest `sha256:3832d20318e7e020949f2a6c17bf54296f2036f45e9f307c8bfe42b0a9f333ee`.
- `docker compose up -d --no-deps backend` and `curl http://localhost:8080/test`:
  passed.
- Authenticated browser `/view103`: clicking the `Orders List` refresh changed
  its timestamp from `11:05:45` to `11:06:13`, retained five rows, and kept the
  route unchanged.
- Authenticated browser `/view100/1002`: before the fix, Save returned
  `操作失败`; after rebuilding the backend it returned `操作成功` / `保存成功`, the
  `确定` command closed the dialog, and the route stayed unchanged.
- The complete `market_order` row for order `1002` was byte-for-byte identical
  before and after the idempotent successful Save.
- Browser console errors: none.

## Runtime Evidence

- `artifacts/runs/20260713-final-visible-interactions/sudoku-after-refresh.png`
- `artifacts/runs/20260713-final-visible-interactions/operation-before-fix.png`
- `artifacts/runs/20260713-final-visible-interactions/operation-after-fix-success.png`

## Risks And Follow-Ups

- The complete CAPTCHA-backed runtime doctor was not rerun because it would
  generate and consume a separate CAPTCHA beyond this browser authorization.
