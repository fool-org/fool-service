# Legacy Tree Factory

## Prompt

- Continue migration against `../FoolFrame`, keep Docker running, keep Vue
  frontend, and make timely atomic commits.

## Scope

- Restored legacy tree collection behavior from `SCPB01-Soway.Data/DS/Tree`.
- Initialized `Tree` root node storage and made `Tree` iterable in level order.
- Ensured builder-created `TreeNode` instances have child lists, matching the
  legacy constructor behavior.
- Added `TreeDataFactory` as the legacy `ITreeData` comparison wrapper.

## Changed Files

- `fool-common/src/main/java/org/fool/framework/common/data/tree/Tree.java`
- `fool-common/src/main/java/org/fool/framework/common/data/tree/TreeNode.java`
- `fool-common/src/main/java/org/fool/framework/common/data/tree/TreeDataFactory.java`
- `fool-common/src/test/java/org/fool/framework/common/data/tree/TreeTest.java`
- `docs/migration/foolframe-parity.md`

## Validation

- Red check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-common -Dtest=org.fool.framework.common.data.tree.TreeTest test`
  failed first because `Tree` was not iterable, then because `TreeDataFactory`
  did not exist.
- Green check:
  `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-common -Dtest=org.fool.framework.common.data.tree.TreeTest test`
- `docker run --rm --network fool-service_default -v "$PWD":/workspace -v "$HOME/.m2":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DfailIfNoTests=false -pl fool-common test`
- `python scripts/check_repo_harness.py`
- `git diff --check`
- `curl -sS -m 5 http://localhost:8080/test`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList"}' http://localhost:8080/api/v1/view/get-view`
- `curl -sS -m 5 -H 'Content-Type: application/json' -d '{"viewName":"OrderList","pageInfo":{"pageSize":10,"pageIndex":1},"filter":null}' http://localhost:8080/api/v1/data/query-list`
- `curl -sS -m 5 http://localhost:8081/ | head -5`
- `docker compose ps`

## Skipped Checks

- Full root `mvn test`; this slice only changes `fool-common` tree behavior.

## Risks And Follow-Ups

- `TreeDataFactory` now covers the legacy comparison wrapper, but broader tree
  consumer behavior should continue to be validated through auth/menu and future
  common tests.
