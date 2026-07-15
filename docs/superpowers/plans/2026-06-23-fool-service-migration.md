# Fool Service Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [x]`) syntax for tracking.

**Goal:** Run `fool-service` through Docker, continue the Java/Spring migration from `../FoolFrame`, and replace the old Node/Jade/Angular web layer with Vue.

**Architecture:** Keep the existing Maven multi-module backend as the source of truth for migrated framework logic. Add a Docker Compose development runtime with MySQL, Redis, Spring Boot, and a Vue frontend that calls the Spring REST endpoints. Track source parity against `../FoolFrame/src/Server` and `../FoolFrame/src/Web` so future migration work is explicit.

**Tech Stack:** Java 17, Spring Boot 2.7.4, Maven, MySQL 8, Redis 7, Docker Compose, Vue 3, Vite, TypeScript.

**Completion:** All plan steps were revalidated and closed on 2026-07-15. The
versioned completion boundary, source inventory, reopen rules, and current
commands live in `docs/migration/foolframe-parity.md`,
`docs/migration/foolframe-server-assets.md`, and `docs/validation.md`.

---

### Task 1: Dockerized Backend Build Baseline

**Files:**
- Modify: `pom.xml`
- Modify: `.gitignore`
- Create: `Dockerfile`
- Create: `.dockerignore`
- Create: `docker-compose.yml`
- Create: `business-application/src/main/resources/application-docker.yml`

- [x] **Step 1: Verify current Docker/JDK build problem**

Run:

```bash
docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package
```

Expected before the fix: Maven resolves dependencies through `https://jcenter.bintray.com`, making clean Docker builds slow and fragile.

- [x] **Step 2: Remove obsolete Maven repository override**

Delete the root `pom.xml` `<repositories>` block that points at `https://jcenter.bintray.com/`. Maven Central is already the default and contains Spring Boot 2.7.4 dependencies.

- [x] **Step 3: Ignore local Docker Maven cache**

Add `.m2-docker/` to `.gitignore` so repeated Docker builds can use a local cache without polluting git status.

- [x] **Step 4: Add backend image build**

Create `Dockerfile` with a Maven/JDK 17 build stage and a JRE runtime stage that runs `business-application-1.0-SNAPSHOT-exec.jar`.

- [x] **Step 5: Add Compose runtime**

Create `docker-compose.yml` with `mysql`, `redis`, `backend`, and later `frontend` services. Backend exposes port `8080` and depends on MySQL/Redis health checks.

- [x] **Step 6: Add Docker Spring profile**

Create `business-application/src/main/resources/application-docker.yml` with datasource and Redis host names matching Compose service names.

- [x] **Step 7: Verify Docker build**

Run:

```bash
docker compose build backend
```

Expected: backend image builds successfully.

### Task 2: Backend Migration Parity Inventory

**Files:**
- Create: `docs/migration/foolframe-parity.md`
- Test: backend compile and focused unit tests

- [x] **Step 1: Record source mapping**

Map `../FoolFrame/src/Server/SCPB01-Soway.Data` to `fool-common`, `SCPB02-Soway.DB` to `fool-dao`, `SCPB05-Soway.Model` to `fool-model`, `SWDQ01-Soway.Query` to `fool-query`, `Soway.Server` to `fool-view`, and `SWUA*` to `fool-auth`.

- [x] **Step 2: Record incomplete modules**

Mark `fool-reflect`, `fool-dynamic`, and `fool-restapi` as not wired into the root Maven reactor yet. Note whether each has source code, POM only, or legacy counterpart.

- [x] **Step 3: Compile and identify migration blockers**

Run:

```bash
docker run --rm -v "$PWD":/workspace -v "$PWD/.m2-docker":/root/.m2 -w /workspace maven:3.9-eclipse-temurin-17 mvn -DskipTests package
```

Expected: either successful compile or a concrete blocker list in `docs/migration/foolframe-parity.md`.

### Task 3: Vue Frontend Replacement

**Files:**
- Create: `frontend/package.json`
- Create: `frontend/index.html`
- Create: `frontend/vite.config.ts`
- Create: `frontend/tsconfig.json`
- Create: `frontend/src/main.ts`
- Create: `frontend/src/App.vue`
- Create: `frontend/src/api.ts`
- Create: `frontend/src/style.css`
- Create: `frontend/Dockerfile`

- [x] **Step 1: Build Vue shell**

Create a Vue 3 + Vite + TypeScript app under `frontend/`.

- [x] **Step 2: Implement legacy Web workflow replacement**

Replace the old `../FoolFrame/src/Web` pages with a usable app shell containing login/profile, auth menu, view definition lookup, and data query panels backed by `/api/v1/auth/*`, `/api/v1/view/get-view`, and `/api/v1/data/query-list`.

- [x] **Step 3: Add API proxy**

Configure Vite dev proxy and Nginx production proxy so `/api` routes reach the backend service.

- [x] **Step 4: Verify frontend build**

Run:

```bash
docker compose build frontend
```

Expected: frontend image builds successfully.

### Task 4: End-To-End Docker Runtime

**Files:**
- Modify: `docker-compose.yml`
- Modify: `README.md`

- [x] **Step 1: Start Compose**

Run:

```bash
docker compose up -d --build
```

Expected: MySQL, Redis, backend, and frontend containers are healthy or running.

- [x] **Step 2: Verify backend health**

Run:

```bash
curl -i http://localhost:8080/
```

Expected: Spring Boot responds. A 404 is acceptable if no root route exists.

- [x] **Step 3: Verify frontend**

Open `http://localhost:5173` for dev or `http://localhost:8081` for Compose frontend and confirm the Vue app renders and can send API requests.

- [x] **Step 4: Document usage**

Update `README.md` with Docker startup, service ports, and known migration gaps.
