# Finalize Frontend Runtime Gate

## Prompt

Continue the old-page parity goal after authorization to let the runtime
doctor generate, read, and use a new local CAPTCHA.

## Scope

- Diagnose and recover the standard Docker frontend image build.
- Recreate the Compose frontend from the formal image rather than an injected
  container filesystem.
- Prove the image contains the tested navigation fix.
- Run the complete CAPTCHA-backed runtime doctor and repository harness.
- Close the remaining frontend acceptance task.

## Changed Files

- `tasks.md`
- `agent_chats/2026/07/13/2026-07-13T07-55-02Z-finalize-frontend-runtime-gate.md`

## Validation

- `BUILDX_BUILDER=desktop-linux docker compose build frontend`: passed. The
  formal image manifest is
  `sha256:dc12d02717ee7e2e13b381177c7c7fc0a14bc6068f322dff7d3f77855b8934f5`.
- `docker compose up -d --no-deps frontend`: recreated and started the
  frontend container from that image.
- `docker inspect fool-service-frontend-1 --format '{{.Image}}'`: the running
  container uses the same manifest.
- Local and container SHA-256 values match for `index.html`
  (`37506d8eacc98784798ae9443ecb3fde9a5294690454d2cca3069c1ec12ba1a4`)
  and `ViewListPanel-Dudryjun.js`
  (`38c4ca77c41580776da45fb5daa07b62bc0e8f119865304d575f7c7de8f38ed8`).
- `python scripts/runtime_doctor.py`: all checks passed, including Compose,
  schema, frontend deep links, CAPTCHA/login, menu, metadata/data, chart,
  Sudoku, detail/new/save/child mutations, BusinessObject lookup, report,
  message, and logout checks.
- `python scripts/check_repo_harness.py`: passed.
- `curl http://localhost:8080/test`: passed.
- `GET /`, `/view100`, and `/view100/1001`: returned HTTP 200 / Vue HTML.

## Runtime Evidence

- Authenticated desktop and mobile browser evidence remains under
  `artifacts/runs/20260713-authenticated-view-parity/`.
- The browser acceptance was run against the same production `dist` bytes now
  proven inside the formal Compose image.

## Root Cause

- The globally selected Buildx instance was another project's
  `ai-video-studio-builder` using the `docker-container` driver.
- Selecting the Docker Desktop context's `desktop-linux` builder for this
  invocation restored base-image resolution, context transfer, and image
  export without changing repository code or global builder state.

## Risks And Follow-Ups

- On this machine, use `BUILDX_BUILDER=desktop-linux` for a deterministic
  Compose build while the other project's builder remains globally selected.
