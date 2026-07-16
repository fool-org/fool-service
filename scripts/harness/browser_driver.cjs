#!/usr/bin/env node
"use strict";

const childProcess = require("node:child_process");
const crypto = require("node:crypto");
const fs = require("node:fs");
const http = require("node:http");
const net = require("node:net");
const os = require("node:os");
const path = require("node:path");

const ROLE_NAMES = ["ordinary", "departmentAdmin", "approver", "systemAdmin"];
const FIXTURE_USERS = {
  ordinary: "phase4-ordinary",
  departmentAdmin: "phase4-dept-admin",
  approver: "phase4-approver",
  systemAdmin: "phase4-sysadmin",
};
const READ_ACTIONS = ["view.discover", "view.query", "view.read"];
const VIEW_PATH = "/view100";
const VIEW_ID = "100";
const API_PREFIX = "/api/";
const TIMEOUT_MS = 20_000;

class HarnessError extends Error {
  constructor(code, status = "FAIL") {
    super(code);
    this.code = code;
    this.status = status;
  }
}

function roleResult() {
  return {
    status: "NOT_RUN",
    rowIds: [],
    effectiveActions: [],
    uiAssertions: {},
    networkAssertions: {},
    screenshot: "",
  };
}

function emptyResult() {
  return {
    schemaVersion: 1,
    status: "FAIL",
    engine: "chrome-cdp",
    roles: {
      ordinary: roleResult(),
      departmentAdmin: roleResult(),
      approver: roleResult(),
      systemAdmin: roleResult(),
    },
    highRisk: {
      actionRequestId: "",
      createdStatus: "",
      previewStatus: "",
      selfApproval: { status: 0, reason: "" },
      approval: { status: 0, finalStatus: "", bodyKeys: [] },
      executeWithoutStepUp: { status: 0, reason: "", requestStatus: "" },
      executed: false,
    },
    network: [],
    console: [],
    screenshots: [],
    security: { dynamicSecretScan: "NOT_RUN", checkedSecretCount: 0 },
    chrome: { exited: false, profileRemoved: false },
  };
}

function assertion(condition, code) {
  if (!condition) throw new HarnessError(code);
}

function safeCode(error) {
  return error instanceof HarnessError ? error.code : "BROWSER_DRIVER_FAILED";
}

function addSecret(secrets, value) {
  if (typeof value === "string" && value.length > 0) secrets.add(value);
}

function collectCredentialSecrets(config, secrets) {
  for (const role of ROLE_NAMES) {
    addSecret(secrets, config?.credentials?.[role]?.password);
  }
}

function replaceSecrets(text, secrets) {
  let safe = String(text ?? "");
  for (const secret of [...secrets].sort((a, b) => b.length - a.length)) {
    safe = safe.split(secret).join("[REDACTED]");
  }
  return safe;
}

function redactText(text, secrets) {
  return replaceSecrets(text, secrets)
    .replace(/Bearer\s+[^\s"'<>]+/gi, "Bearer [REDACTED]")
    .replace(
      /(["']?(?:password|passWord|checkCodeKey|checkCode|authorization|token)["']?\s*[:=]\s*)["']?[^"',\s}<]+/gi,
      "$1[REDACTED]",
    )
    .replace(/data:image\/[^;]+;base64,[A-Za-z0-9+/=]+/gi, "data:image/[REDACTED]")
    .replace(/\s+/g, " ")
    .trim()
    .slice(0, 500);
}

function topLevelBodyKeys(postData) {
  if (!postData) return [];
  try {
    const value = JSON.parse(postData);
    return value && typeof value === "object" && !Array.isArray(value)
      ? Object.keys(value).sort()
      : [];
  } catch {
    return [];
  }
}

function collectCaptchaSecrets(value, secrets, seen = new Set()) {
  if (!value || typeof value !== "object" || seen.has(value)) return;
  seen.add(value);
  const key = value.key ?? value.Key;
  const code = value.code ?? value.Code;
  if (
    typeof key === "string"
    && key.length >= 8
    && typeof code === "string"
    && code.length >= 3
    && code.length <= 12
  ) {
    addSecret(secrets, key);
    addSecret(secrets, code);
  }
  for (const child of Object.values(value)) collectCaptchaSecrets(child, secrets, seen);
}

function validateConfig(config) {
  assertion(config && config.schemaVersion === 1, "CONFIG_SCHEMA_INVALID");
  const run = config.run;
  assertion(run && typeof run === "object", "CONFIG_RUN_INVALID");
  const baseUrl = new URL(run.baseUrl);
  assertion(baseUrl.protocol === "http:", "CONFIG_BASE_URL_INVALID");
  assertion(
    ["127.0.0.1", "localhost", "::1", "[::1]"].includes(baseUrl.hostname),
    "CONFIG_BASE_URL_NOT_LOCAL",
  );
  assertion(!baseUrl.username && !baseUrl.password, "CONFIG_BASE_URL_CREDENTIALS_FORBIDDEN");
  assertion(Number.isInteger(run.debugPort) && run.debugPort >= 1024 && run.debugPort <= 65535,
    "CONFIG_DEBUG_PORT_INVALID");
  assertion(typeof run.runId === "string" && /^[A-Za-z0-9._-]{1,100}$/.test(run.runId),
    "CONFIG_RUN_ID_INVALID");
  assertion(path.isAbsolute(run.profileDir) && path.isAbsolute(run.artifactDir),
    "CONFIG_PATH_INVALID");
  const profile = path.resolve(run.profileDir);
  assertion(path.basename(profile).includes(run.runId), "CONFIG_PROFILE_RUN_ID_MISMATCH");
  assertion(profile !== path.parse(profile).root && profile !== process.cwd(),
    "CONFIG_PROFILE_PATH_UNSAFE");
  const temporaryRoots = [
    path.resolve(os.tmpdir()),
    path.resolve("/tmp"),
    path.resolve("/private/tmp"),
    path.resolve("/private/var/folders"),
  ];
  assertion(
    temporaryRoots.some((root) => profile.startsWith(`${root}${path.sep}`)),
    "CONFIG_PROFILE_NOT_TEMPORARY",
  );
  if (fs.existsSync(profile)) {
    assertion(fs.statSync(profile).isDirectory(), "CONFIG_PROFILE_NOT_DIRECTORY");
    assertion(fs.readdirSync(profile).length === 0, "CONFIG_PROFILE_NOT_EMPTY");
  }
  for (const role of ROLE_NAMES) {
    const credential = config.credentials?.[role];
    assertion(
      credential
      && credential.username === FIXTURE_USERS[role]
      && typeof credential.password === "string"
      && credential.password.length > 0,
      `CONFIG_${role.toUpperCase()}_CREDENTIAL_INVALID`,
    );
  }
  return {
    baseUrl: baseUrl.origin,
    debugPort: run.debugPort,
    profileDir: profile,
    artifactDir: path.resolve(run.artifactDir),
    runId: run.runId,
  };
}

function readStdin() {
  return new Promise((resolve, reject) => {
    let input = "";
    process.stdin.setEncoding("utf8");
    process.stdin.on("data", (chunk) => {
      input += chunk;
      if (input.length > 1_048_576) reject(new HarnessError("CONFIG_TOO_LARGE"));
    });
    process.stdin.on("end", () => resolve(input));
    process.stdin.on("error", () => reject(new HarnessError("CONFIG_READ_FAILED")));
  });
}

function checkPortFree(port) {
  return new Promise((resolve) => {
    const server = net.createServer();
    server.unref();
    server.once("error", () => resolve(false));
    server.listen({ host: "127.0.0.1", port, exclusive: true }, () => {
      server.close(() => resolve(true));
    });
  });
}

function httpReady(url, timeout = 3_000) {
  return new Promise((resolve) => {
    const request = http.get(url, { timeout }, (response) => {
      response.resume();
      resolve(response.statusCode >= 200 && response.statusCode < 500);
    });
    request.once("timeout", () => {
      request.destroy();
      resolve(false);
    });
    request.once("error", () => resolve(false));
  });
}

async function waitForCdp(port, chromeProcess) {
  const endpoint = `http://127.0.0.1:${port}`;
  const deadline = Date.now() + 15_000;
  while (Date.now() < deadline) {
    if (chromeProcess.exitCode !== null) break;
    if (await httpReady(`${endpoint}/json/version`, 750)) return endpoint;
    await new Promise((resolve) => setTimeout(resolve, 150));
  }
  throw new HarnessError("CHROME_CDP_UNAVAILABLE", "ENVIRONMENT_UNAVAILABLE");
}

function chromeExecutable() {
  const candidates = process.platform === "darwin"
    ? [
      "/Applications/Google Chrome.app/Contents/MacOS/Google Chrome",
      "/Applications/Google Chrome for Testing.app/Contents/MacOS/Google Chrome for Testing",
    ]
    : [
      "/usr/bin/google-chrome",
      "/usr/bin/google-chrome-stable",
      "/usr/bin/chromium",
      "/usr/bin/chromium-browser",
    ];
  return candidates.find((candidate) => fs.existsSync(candidate)) || "";
}

function launchChrome(executable, run) {
  fs.mkdirSync(run.profileDir, { recursive: true, mode: 0o700 });
  return childProcess.spawn(executable, [
    "--headless=new",
    `--remote-debugging-port=${run.debugPort}`,
    "--remote-debugging-address=127.0.0.1",
    `--user-data-dir=${run.profileDir}`,
    "--no-first-run",
    "--no-default-browser-check",
    "--disable-background-networking",
    "--disable-component-update",
    "--disable-sync",
    "--disable-dev-shm-usage",
    "--disable-features=Translate,OptimizationHints,MediaRouter",
    "--password-store=basic",
    "--use-mock-keychain",
    "about:blank",
  ], { stdio: "ignore" });
}

async function stopChrome(browser, chromeProcess) {
  try {
    if (browser) await browser.close();
  } catch {
    // The process is terminated below if the CDP close did not finish.
  }
  if (!chromeProcess || chromeProcess.exitCode !== null) return true;
  chromeProcess.kill("SIGTERM");
  const exited = await Promise.race([
    new Promise((resolve) => chromeProcess.once("exit", () => resolve(true))),
    new Promise((resolve) => setTimeout(() => resolve(false), 2_000)),
  ]);
  if (!exited && chromeProcess.exitCode === null) chromeProcess.kill("SIGKILL");
  if (chromeProcess.exitCode !== null) return true;
  return Promise.race([
    new Promise((resolve) => chromeProcess.once("exit", () => resolve(true))),
    new Promise((resolve) => setTimeout(() => resolve(false), 2_000)),
  ]);
}

async function flushPending(pending) {
  while (pending.length) {
    const batch = pending.splice(0);
    await Promise.allSettled(batch);
  }
}

async function newRolePage(browser, run, role, result, secrets, pendingCaptures) {
  const context = await browser.newContext({
    viewport: { width: 1440, height: 1000 },
    locale: "zh-CN",
  });
  const page = await context.newPage();
  const observations = { getMainSeen: false, getMainBodyEmpty: true };
  page.setDefaultTimeout(TIMEOUT_MS);
  page.setDefaultNavigationTimeout(TIMEOUT_MS);
  const traceId = `${run.runId}-${role}`;
  await page.route("**/api/**", async (route) => {
    const request = route.request();
    const url = new URL(request.url());
    if (url.origin !== run.baseUrl || !url.pathname.startsWith(API_PREFIX)) {
      await route.continue();
      return;
    }
    await route.continue({
      headers: {
        ...request.headers(),
        "X-Trace-Id": traceId,
        "x-request-id": traceId,
      },
    });
  });
  page.on("response", (response) => {
    const request = response.request();
    let url;
    try {
      url = new URL(response.url());
    } catch {
      return;
    }
    if (url.origin !== run.baseUrl || !url.pathname.startsWith(API_PREFIX)) return;
    const entry = {
      role,
      method: request.method(),
      path: url.pathname,
      status: response.status(),
      traceId,
    };
    const bodyKeys = topLevelBodyKeys(request.postData());
    const sensitiveRequest = [
      "/api/v1/auth/initapp",
      "/api/v1/auth/getcheckcode",
      "/api/v1/auth/loginv2",
      "/api/v1/auth/step-up",
    ].includes(url.pathname);
    if (url.pathname === "/api/v1/auth/getmain") {
      const bodyEmpty = emptyJsonObject(request.postData());
      observations.getMainSeen = true;
      observations.getMainBodyEmpty = observations.getMainBodyEmpty
        && bodyEmpty;
      entry.bodyEmpty = bodyEmpty;
      entry.bodyKeys = [];
    } else if (!sensitiveRequest && bodyKeys.length) {
      entry.bodyKeys = bodyKeys;
    }
    result.network.push(entry);
    if (["/api/v1/auth/initapp", "/api/v1/auth/getcheckcode"].includes(url.pathname)) {
      const capture = response.json()
        .then((payload) => collectCaptchaSecrets(payload, secrets))
        .catch(() => {});
      pendingCaptures.push(capture);
    }
  });
  page.on("console", (message) => {
    if (!["warning", "error"].includes(message.type())) return;
    result.console.push({
      role,
      type: message.type(),
      text: redactText(message.text(), secrets),
    });
  });
  page.on("pageerror", () => {
    result.console.push({ role, type: "error", text: "PAGE_RUNTIME_ERROR" });
  });
  return { context, page, observations };
}

function emptyJsonObject(postData) {
  try {
    const value = JSON.parse(postData || "");
    return value && typeof value === "object" && !Array.isArray(value)
      && Object.keys(value).length === 0;
  } catch {
    return false;
  }
}

function commonData(response, code) {
  assertion(response.status === 200, `${code}_HTTP`);
  assertion(response.body && response.body.code === 0, `${code}_APPLICATION`);
  return response.body.data;
}

async function api(page, method, requestPath, body, extraHeaders = {}) {
  return page.evaluate(async ({ method: requestMethod, requestPath: apiPath, body: payload, extraHeaders: headers }) => {
    const requestHeaders = { ...headers };
    const token = localStorage.getItem("fool-service-token") || "";
    if (token) requestHeaders.Authorization = `Bearer ${token}`;
    const init = { method: requestMethod, headers: requestHeaders };
    if (payload !== undefined) {
      requestHeaders["Content-Type"] = "application/json";
      init.body = JSON.stringify(payload);
    }
    const response = await fetch(apiPath, init);
    let parsed = null;
    try {
      parsed = await response.json();
    } catch {
      parsed = null;
    }
    return { status: response.status, body: parsed };
  }, { method, requestPath, body, extraHeaders });
}

function responseReason(response) {
  const candidate = response?.body?.message
    ?? response?.body?.reasonCode
    ?? response?.body?.data?.reasonCode
    ?? "";
  return typeof candidate === "string" && /^[A-Z0-9_]{3,80}$/.test(candidate)
    ? candidate
    : "UNEXPECTED_ERROR";
}

async function uiLogin(page, run, credential, secrets) {
  await page.goto(`${run.baseUrl}${VIEW_PATH}`, { waitUntil: "domcontentloaded" });
  await page.getByLabel("用户名").waitFor();
  await page.waitForFunction(
    () => (document.querySelector('input[name="check-code-key"]')?.value || "").length >= 8,
  );
  const responsePromise = page.waitForResponse((response) => {
    const url = new URL(response.url());
    return url.origin === run.baseUrl && url.pathname === "/api/v1/auth/getcheckcode";
  });
  await page.getByRole("button", { name: "刷新", exact: true }).click();
  const captchaResponse = await responsePromise;
  const payload = await captchaResponse.json();
  collectCaptchaSecrets(payload, secrets);
  const captcha = findCaptcha(payload);
  assertion(captcha, "LOGIN_CAPTCHA_RESPONSE_INVALID");
  await page.waitForFunction(
    (expectedKey) =>
      document.querySelector('input[name="check-code-key"]')?.value === expectedKey,
    captcha.key,
  );
  const hiddenKey = await page.locator('input[name="check-code-key"]').inputValue();
  assertion(hiddenKey === captcha.key, "LOGIN_CAPTCHA_UI_MISMATCH");
  await page.getByLabel("用户名").fill(credential.username);
  await page.getByLabel("密码").fill(credential.password);
  await page.getByLabel("验证码").fill(captcha.code);
  const shellReadyPromise = page.waitForResponse((response) => {
    const url = new URL(response.url());
    return url.origin === run.baseUrl
      && url.pathname === "/api/v1/auth/getmain"
      && response.status() === 200;
  });
  await page.getByRole("button", { name: "登录", exact: true }).click();
  await page.waitForFunction(() => Boolean(localStorage.getItem("fool-service-token")));
  await shellReadyPromise;
  const token = await page.evaluate(() => localStorage.getItem("fool-service-token") || "");
  assertion(token.length > 0, "LOGIN_TOKEN_MISSING");
  addSecret(secrets, token);
  const queryReadyPromise = page.waitForResponse((response) => {
    const url = new URL(response.url());
    return url.origin === run.baseUrl
      && url.pathname === "/api/v1/data/querydata"
      && response.request().method() === "POST";
  });
  await page.goto(`${run.baseUrl}${VIEW_PATH}`, { waitUntil: "domcontentloaded" });
  assertion((await queryReadyPromise).status() === 200, "VIEW_QUERY_HTTP_FAILED");
  await page.locator(
    ".metadata-data-table tbody tr:not(.legacy-filler-row)",
  ).first().waitFor();
}

function findCaptcha(value, seen = new Set()) {
  if (!value || typeof value !== "object" || seen.has(value)) return null;
  seen.add(value);
  const key = value.key ?? value.Key;
  const code = value.code ?? value.Code;
  if (
    typeof key === "string"
    && key.length >= 8
    && typeof code === "string"
    && code.length >= 3
    && code.length <= 12
  ) {
    return { key, code };
  }
  for (const child of Object.values(value)) {
    const found = findCaptcha(child, seen);
    if (found) return found;
  }
  return null;
}

async function tableRows(page) {
  return page.locator(".metadata-data-table tbody tr:not(.legacy-filler-row)").evaluateAll((rows) =>
    rows.map((row) => [...row.querySelectorAll("td")]
      .map((cell) => (cell.textContent || "").trim())
      .filter(Boolean)),
  );
}

function rowIdsFromCells(rows) {
  const known = new Set(["1001", "1002"]);
  return rows.map((cells) => cells.find((cell) => known.has(cell)) || cells[0] || "")
    .filter(Boolean);
}

function effectiveActionsFrom(data) {
  const actions = Array.isArray(data?.actions) ? data.actions : [];
  return actions.map((item) => item?.action).filter((item) => typeof item === "string").sort();
}

async function validateViewRole(page, role, actionRequestId, result, observations) {
  const target = result.roles[role];
  const rows = await tableRows(page);
  const rowIds = rowIdsFromCells(rows);
  target.rowIds = rowIds;
  const effective = await api(
    page,
    "GET",
    `/api/v1/authz/effective-actions?resourceType=View&resourceId=${VIEW_ID}`,
  );
  const effectiveActions = effectiveActionsFrom(commonData(effective, `${role}_EFFECTIVE_ACTIONS`));
  target.effectiveActions = effectiveActions;
  const provider = await api(page, "GET", "/api/v1/agent/providers");
  const directSave = await api(page, "POST", "/api/v1/data/saveobj", {});
  const actionRead = ["ordinary", "departmentAdmin"].includes(role)
    ? await api(page, "GET", `/api/v1/actions/${actionRequestId}`)
    : null;
  if (actionRead) {
    target.networkAssertions = {
      actionReadStatus: actionRead.status,
      actionReadReason: responseReason(actionRead),
      actionReadOwned: actionRead.body?.data?.owned === true,
      actionReadApprovable: actionRead.body?.data?.approvable === true,
    };
  }
  const aiCount = await page.getByRole("button", { name: "AI 助手", exact: true }).count();
  const actionCenterCount = await page.getByRole("button", { name: "动作中心", exact: true }).count();
  assertion(observations.getMainSeen && observations.getMainBodyEmpty,
    `${role.toUpperCase()}_GETMAIN_BODY_TOKEN_PRESENT`);

  if (role === "ordinary") {
    assertion(rowIds.length === 1 && rowIds[0] === "1001", "ORDINARY_ROW_SCOPE_FAILED");
    assertion(JSON.stringify(effectiveActions) === JSON.stringify(READ_ACTIONS),
      "ORDINARY_EFFECTIVE_ACTIONS_FAILED");
    assertion(aiCount === 0, "ORDINARY_AGENT_UI_VISIBLE");
    assertion(provider.status === 403, "ORDINARY_PROVIDER_NOT_DENIED");
    assertion(actionRead.status === 403, "ORDINARY_ACTION_READ_NOT_DENIED");
  } else if (role === "departmentAdmin") {
    assertion(rowIds.length === 1 && rowIds[0] === "1002", "DEPARTMENT_ROW_SCOPE_FAILED");
    assertion(JSON.stringify(effectiveActions) === JSON.stringify(READ_ACTIONS),
      "DEPARTMENT_EFFECTIVE_ACTIONS_FAILED");
    assertion(aiCount === 0, "DEPARTMENT_AGENT_UI_VISIBLE");
    assertion(provider.status === 403, "DEPARTMENT_PROVIDER_NOT_DENIED");
    assertion(actionRead.status === 403, "DEPARTMENT_ACTION_READ_NOT_DENIED");
  } else if (role === "approver") {
    assertion(rowIds.includes("1001") && rowIds.includes("1002"), "APPROVER_ROW_SCOPE_FAILED");
    assertion(JSON.stringify(effectiveActions) === JSON.stringify(READ_ACTIONS),
      "APPROVER_EFFECTIVE_ACTIONS_FAILED");
    assertion(aiCount === 0, "APPROVER_AGENT_UI_VISIBLE");
    assertion(provider.status === 403, "APPROVER_PROVIDER_NOT_DENIED");
  } else {
    assertion(rowIds.includes("1001") && rowIds.includes("1002"), "SYSTEM_ADMIN_ROW_SCOPE_FAILED");
    assertion(
      ["data.create", "data.update", "report.preview"]
        .every((action) => effectiveActions.includes(action)),
      "SYSTEM_ADMIN_EFFECTIVE_ACTIONS_MISSING",
    );
    assertion(aiCount > 0, "SYSTEM_ADMIN_AGENT_UI_HIDDEN");
    assertion(provider.status === 200, "SYSTEM_ADMIN_PROVIDER_NOT_ALLOWED");
  }

  assertion(actionCenterCount > 0, `${role.toUpperCase()}_ACTION_CENTER_HIDDEN`);
  assertion(
    directSave.status === 403 && responseReason(directSave) === "ACTION_WORKFLOW_REQUIRED",
    `${role.toUpperCase()}_DIRECT_SAVE_NOT_GUARDED`,
  );
  target.uiAssertions = {
    login: "PASS",
    rowScope: "PASS",
    agentEntry: aiCount > 0 ? "VISIBLE" : "HIDDEN",
    actionCenter: "VISIBLE",
  };
  target.networkAssertions = {
    ...target.networkAssertions,
    effectiveActionsStatus: effective.status,
    getMainBodyEmpty: observations.getMainBodyEmpty,
    providerStatus: provider.status,
    directSaveStatus: directSave.status,
    directSaveReason: responseReason(directSave),
  };
}

async function prepareHighRisk(page, run, credential, result) {
  const stepUp = await api(page, "POST", "/api/v1/auth/step-up", {
    password: credential.password,
  });
  commonData(stepUp, "HIGH_STEP_UP");
  const requests = [
    {
      SaveObj: {
        Id: "1001",
        ViewID: VIEW_ID,
        Propertyies: [{ Key: "symbol", Value: "BTC-USDT" }],
        Itemproperties: [],
      },
    },
    {
      SaveObj: {
        Id: "1002",
        ViewID: VIEW_ID,
        Propertyies: [{ Key: "symbol", Value: "ETH-USDT" }],
        Itemproperties: [],
      },
    },
  ];
  const created = await api(page, "POST", "/api/v1/actions", {
    schemaVersion: 1,
    action: "data.update",
    resource: { type: "view", id: VIEW_ID },
    arguments: { requests },
    rationale: "Browser role matrix same-value bounded bulk preview",
  }, {
    "Idempotency-Key": `browser-role-${run.runId}-${crypto.randomUUID()}`,
    "X-Action-Source": "UI",
  });
  const createdData = commonData(created, "HIGH_CREATE");
  assertion(typeof createdData?.actionRequestId === "string", "HIGH_ACTION_ID_MISSING");
  assertion(createdData.status === "DRAFT", "HIGH_CREATED_STATUS_INVALID");
  result.highRisk.actionRequestId = createdData.actionRequestId;
  result.highRisk.createdStatus = createdData.status;
  const preview = await api(
    page,
    "POST",
    `/api/v1/actions/${createdData.actionRequestId}/preview`,
    {},
  );
  const previewData = commonData(preview, "HIGH_PREVIEW");
  assertion(
    previewData.riskLevel === "HIGH"
    && previewData.status === "AWAITING_APPROVAL"
    && previewData.preview?.affectedObjectCount === 2,
    "HIGH_PREVIEW_GATE_INVALID",
  );
  result.highRisk.previewStatus = previewData.status;
}

async function openActionCenter(page, actionRequestId, run) {
  await page.getByRole("button", { name: "动作中心", exact: true }).first().click();
  await page.getByRole("heading", { name: "受控动作中心", exact: true }).waitFor();
  await page.getByLabel("动作请求 ID").fill(actionRequestId);
  const responsePromise = page.waitForResponse((response) => {
    const url = new URL(response.url());
    return url.origin === run.baseUrl
      && url.pathname === `/api/v1/actions/${actionRequestId}`
      && response.request().method() === "GET";
  });
  await page.getByRole("button", { name: "查询", exact: true }).click();
  const response = await responsePromise;
  assertion(response.status() === 200, "ACTION_CENTER_LOAD_FAILED");
  await page.locator(".action-card").waitFor();
}

async function ownerSelfApproval(page, actionRequestId, result) {
  assertion(await page.locator(".risk").textContent() === "HIGH", "OWNER_RISK_UI_INVALID");
  assertion(await page.locator(".status").textContent() === "AWAITING_APPROVAL",
    "OWNER_STATUS_UI_INVALID");
  assertion(
    await page.getByText("当前用户是发起人；必须由独立审批人完成审批。", { exact: true }).count() > 0,
    "OWNER_INDEPENDENT_APPROVAL_NOTICE_MISSING",
  );
  assertion(await page.getByRole("button", { name: "批准", exact: true }).count() === 0,
    "OWNER_APPROVAL_BUTTON_VISIBLE");
  assertion(await page.getByRole("button", { name: "执行已批准动作", exact: true }).count() === 0,
    "OWNER_EXECUTE_BUTTON_VISIBLE_TOO_EARLY");
  assertion(await page.getByRole("button", { name: "取消请求", exact: true }).count() > 0,
    "OWNER_CANCEL_BUTTON_HIDDEN");
  const denied = await api(
    page,
    "POST",
    `/api/v1/actions/${actionRequestId}/approvals`,
    { decision: "APPROVE", comment: "owner self-approval denial check" },
  );
  result.highRisk.selfApproval = {
    status: denied.status,
    reason: responseReason(denied),
  };
  assertion(
    denied.status === 403 && responseReason(denied) === "SELF_APPROVAL_FORBIDDEN",
    "OWNER_SELF_APPROVAL_NOT_DENIED",
  );
}

async function approverUiApproval(page, actionRequestId, run, result) {
  assertion(await page.locator(".risk").textContent() === "HIGH", "APPROVER_RISK_UI_INVALID");
  assertion(await page.locator(".status").textContent() === "AWAITING_APPROVAL",
    "APPROVER_STATUS_UI_INVALID");
  assertion(await page.getByRole("button", { name: "批准", exact: true }).count() > 0,
    "APPROVER_APPROVAL_BUTTON_HIDDEN");
  assertion(await page.getByRole("button", { name: "执行已批准动作", exact: true }).count() === 0,
    "APPROVER_EXECUTE_BUTTON_VISIBLE");
  assertion(await page.getByRole("button", { name: "取消请求", exact: true }).count() === 0,
    "APPROVER_CANCEL_BUTTON_VISIBLE");
  assertion(
    await page.getByText("当前用户是发起人；必须由独立审批人完成审批。", { exact: true }).count() === 0,
    "APPROVER_OWNER_NOTICE_VISIBLE",
  );
  await page.getByLabel("审批意见").fill(`browser role approval ${run.runId}`);
  const responsePromise = page.waitForResponse((response) => {
    const url = new URL(response.url());
    return url.origin === run.baseUrl
      && url.pathname === `/api/v1/actions/${actionRequestId}/approvals`
      && response.request().method() === "POST";
  });
  await page.getByRole("button", { name: "批准", exact: true }).click();
  const response = await responsePromise;
  const responseBody = await response.json().catch(() => null);
  const finalStatus = responseBody?.data?.status ?? "";
  const bodyKeys = topLevelBodyKeys(response.request().postData());
  result.highRisk.approval = {
    status: response.status(),
    finalStatus,
    bodyKeys,
  };
  assertion(
    response.status() === 200
    && finalStatus === "APPROVED"
    && JSON.stringify(bodyKeys) === JSON.stringify(["comment", "decision"]),
    "APPROVER_UI_APPROVAL_FAILED",
  );
  await page.getByText("审批已记录。", { exact: true }).waitFor();
  assertion(await page.locator(".status").textContent() === "APPROVED",
    "APPROVER_APPROVED_STATUS_UI_INVALID");
}

async function ownerExecuteWithoutStepUp(page, actionRequestId, run, result) {
  const reloadPromise = page.waitForResponse((response) => {
    const url = new URL(response.url());
    return url.origin === run.baseUrl
      && url.pathname === `/api/v1/actions/${actionRequestId}`
      && response.request().method() === "GET";
  });
  await page.getByRole("button", { name: "查询", exact: true }).click();
  assertion((await reloadPromise).status() === 200, "OWNER_APPROVED_RELOAD_FAILED");
  await page.getByRole("button", { name: "执行已批准动作", exact: true }).waitFor();
  assertion(await page.locator(".status").textContent() === "APPROVED",
    "OWNER_APPROVED_STATUS_UI_INVALID");
  const executePromise = page.waitForResponse((response) => {
    const url = new URL(response.url());
    return url.origin === run.baseUrl
      && url.pathname === `/api/v1/actions/${actionRequestId}/execute`
      && response.request().method() === "POST";
  });
  await page.getByRole("button", { name: "执行已批准动作", exact: true }).click();
  const executeResponse = await executePromise;
  const executeBody = await executeResponse.json().catch(() => null);
  const denied = { status: executeResponse.status(), body: executeBody };
  await page.getByRole("alert").waitFor();
  assertion(await page.getByRole("alert").textContent() === "STEP_UP_REQUIRED",
    "OWNER_STEP_UP_ERROR_UI_INVALID");
  const current = await api(page, "GET", `/api/v1/actions/${actionRequestId}`);
  const currentData = commonData(current, "OWNER_ACTION_RECHECK");
  result.highRisk.executeWithoutStepUp = {
    status: denied.status,
    reason: responseReason(denied),
    requestStatus: currentData.status ?? "",
  };
  assertion(
    denied.status === 403
    && responseReason(denied) === "STEP_UP_REQUIRED"
    && currentData.status === "APPROVED",
    "OWNER_EXECUTE_WITHOUT_STEP_UP_NOT_DENIED",
  );
}

async function maskedScreenshot(page, run, result, name) {
  await page.evaluate(() => {
    for (const row of document.querySelectorAll("table tbody tr:not(.legacy-filler-row)")) {
      const cells = [...row.querySelectorAll("td")];
      if (cells[0] && !["1001", "1002"].includes((cells[0].textContent || "").trim())) {
        cells[0].textContent = "[masked]";
      }
      for (const cell of cells.slice(1)) cell.textContent = "[masked]";
    }
    for (const element of document.querySelectorAll(".action-center pre")) {
      element.textContent = "[masked]";
    }
    const comment = document.querySelector("#approval-comment");
    if (comment) {
      comment.value = "";
      comment.setAttribute("placeholder", "[masked]");
    }
    for (const element of document.querySelectorAll("canvas, svg, img")) {
      element.style.visibility = "hidden";
    }
  });
  const directory = path.join(run.artifactDir, "screenshots");
  fs.mkdirSync(directory, { recursive: true, mode: 0o700 });
  const destination = path.join(directory, `${name}.png`);
  await page.screenshot({ path: destination, fullPage: true });
  const relative = path.relative(run.artifactDir, destination).split(path.sep).join("/");
  result.screenshots.push(relative);
  return relative;
}

function artifactFiles(root) {
  if (!fs.existsSync(root)) return [];
  const files = [];
  const visit = (current) => {
    for (const entry of fs.readdirSync(current, { withFileTypes: true })) {
      const candidate = path.join(current, entry.name);
      if (entry.isDirectory()) visit(candidate);
      else if (entry.isFile()) files.push(candidate);
    }
  };
  visit(root);
  return files;
}

function scanArtifacts(root, secrets) {
  const leaked = [];
  for (const file of artifactFiles(root)) {
    const content = fs.readFileSync(file);
    if ([...secrets].some((secret) => content.indexOf(Buffer.from(secret, "utf8")) !== -1)) {
      leaked.push(file);
    }
  }
  return leaked;
}

async function runHarness(config, result, secrets) {
  const run = validateConfig(config);
  collectCredentialSecrets(config, secrets);
  let browser = null;
  let chromeProcess = null;
  let activeRole = "";
  const pendingCaptures = [];
  const contexts = [];
  try {
    if (!await httpReady(run.baseUrl)) {
      throw new HarnessError("FRONTEND_UNAVAILABLE", "ENVIRONMENT_UNAVAILABLE");
    }
    if (!await checkPortFree(run.debugPort)) {
      throw new HarnessError("DEBUG_PORT_OCCUPIED", "ENVIRONMENT_UNAVAILABLE");
    }
    const executable = chromeExecutable();
    if (!executable) {
      throw new HarnessError("SYSTEM_CHROME_NOT_FOUND", "ENVIRONMENT_UNAVAILABLE");
    }
    const playwrightPath = path.resolve(__dirname, "../../frontend/node_modules/playwright-core");
    if (!fs.existsSync(playwrightPath)) {
      throw new HarnessError("PLAYWRIGHT_CORE_NOT_INSTALLED", "ENVIRONMENT_UNAVAILABLE");
    }
    fs.mkdirSync(run.artifactDir, { recursive: true, mode: 0o700 });
    const { chromium } = require(playwrightPath);
    chromeProcess = launchChrome(executable, run);
    const endpoint = await waitForCdp(run.debugPort, chromeProcess);
    browser = await chromium.connectOverCDP(endpoint);

    activeRole = "systemAdmin";
    const setup = await newRolePage(
      browser, run, "systemAdmin", result, secrets, pendingCaptures,
    );
    contexts.push(setup.context);
    await uiLogin(setup.page, run, config.credentials.systemAdmin, secrets);
    await prepareHighRisk(setup.page, run, config.credentials.systemAdmin, result);
    await setup.context.close();
    contexts.splice(contexts.indexOf(setup.context), 1);

    for (const role of ["ordinary", "departmentAdmin"]) {
      activeRole = role;
      const session = await newRolePage(browser, run, role, result, secrets, pendingCaptures);
      contexts.push(session.context);
      try {
        await uiLogin(session.page, run, config.credentials[role], secrets);
        await validateViewRole(
          session.page,
          role,
          result.highRisk.actionRequestId,
          result,
          session.observations,
        );
        const screenshot = await maskedScreenshot(
          session.page, run, result, `${role}-view100`,
        );
        result.roles[role].screenshot = screenshot;
        result.roles[role].status = "PASS";
      } catch (error) {
        result.roles[role].status = "FAIL";
        throw error;
      } finally {
        await session.context.close();
        contexts.splice(contexts.indexOf(session.context), 1);
      }
    }

    activeRole = "systemAdmin";
    const owner = await newRolePage(
      browser, run, "systemAdmin", result, secrets, pendingCaptures,
    );
    contexts.push(owner.context);
    await uiLogin(owner.page, run, config.credentials.systemAdmin, secrets);
    await validateViewRole(
      owner.page,
      "systemAdmin",
      result.highRisk.actionRequestId,
      result,
      owner.observations,
    );
    await maskedScreenshot(
      owner.page, run, result, "systemAdmin-view100",
    );
    await openActionCenter(owner.page, result.highRisk.actionRequestId, run);
    await ownerSelfApproval(owner.page, result.highRisk.actionRequestId, result);
    await maskedScreenshot(owner.page, run, result, "systemAdmin-awaiting-independent-approval");

    activeRole = "approver";
    const approver = await newRolePage(
      browser, run, "approver", result, secrets, pendingCaptures,
    );
    contexts.push(approver.context);
    try {
      await uiLogin(approver.page, run, config.credentials.approver, secrets);
      await validateViewRole(
        approver.page,
        "approver",
        result.highRisk.actionRequestId,
        result,
        approver.observations,
      );
      await maskedScreenshot(
        approver.page, run, result, "approver-view100",
      );
      await openActionCenter(approver.page, result.highRisk.actionRequestId, run);
      await maskedScreenshot(approver.page, run, result, "approver-before-approval");
      await approverUiApproval(approver.page, result.highRisk.actionRequestId, run, result);
      result.roles.approver.screenshot = await maskedScreenshot(
        approver.page, run, result, "approver-action-approved",
      );
      result.roles.approver.uiAssertions.approval = "APPROVED";
      result.roles.approver.status = "PASS";
    } catch (error) {
      result.roles.approver.status = "FAIL";
      throw error;
    } finally {
      await approver.context.close();
      contexts.splice(contexts.indexOf(approver.context), 1);
    }

    activeRole = "systemAdmin";
    try {
      await ownerExecuteWithoutStepUp(owner.page, result.highRisk.actionRequestId, run, result);
      result.roles.systemAdmin.screenshot = await maskedScreenshot(
        owner.page, run, result, "systemAdmin-execute-step-up-denied",
      );
      result.roles.systemAdmin.uiAssertions.ownerIndependentApproval = "PASS";
      result.roles.systemAdmin.uiAssertions.executeRequiresFreshStepUp = "PASS";
      result.roles.systemAdmin.status = "PASS";
    } catch (error) {
      result.roles.systemAdmin.status = "FAIL";
      throw error;
    } finally {
      await owner.context.close();
      contexts.splice(contexts.indexOf(owner.context), 1);
    }
    await flushPending(pendingCaptures);
    result.status = "PASS";
  } catch (error) {
    if (result.roles[activeRole]?.status === "NOT_RUN") result.roles[activeRole].status = "FAIL";
    result.status = error instanceof HarnessError ? error.status : "FAIL";
    result.console.push({ role: "driver", type: "error", text: safeCode(error) });
  } finally {
    for (const context of contexts) {
      try {
        await context.close();
      } catch {
        // Chrome shutdown below is the final cleanup boundary.
      }
    }
    await flushPending(pendingCaptures);
    result.console = result.console.map((entry) => ({
      role: entry.role,
      type: entry.type,
      text: redactText(entry.text, secrets),
    }));
    result.chrome.exited = await stopChrome(browser, chromeProcess);
    try {
      if (fs.existsSync(run.profileDir)) fs.rmSync(run.profileDir, { recursive: true, force: true });
    } catch {
      result.console.push({ role: "driver", type: "error", text: "PROFILE_CLEANUP_FAILED" });
    }
    result.chrome.profileRemoved = !fs.existsSync(run.profileDir);
    const leaked = scanArtifacts(run.artifactDir, secrets);
    if (leaked.length) {
      for (const file of leaked) {
        try {
          fs.unlinkSync(file);
        } catch {
          // The failed scan remains authoritative even if removal is not possible.
        }
      }
      result.screenshots = result.screenshots.filter((relative) =>
        !leaked.includes(path.join(run.artifactDir, relative)));
      for (const role of ROLE_NAMES) {
        if (!result.screenshots.includes(result.roles[role].screenshot)) {
          result.roles[role].screenshot = "";
        }
      }
      result.security.dynamicSecretScan = "FAIL";
      result.status = "FAIL";
    } else {
      result.security.dynamicSecretScan = "PASS";
    }
    result.security.checkedSecretCount = secrets.size;
    if (!result.chrome.exited || !result.chrome.profileRemoved) result.status = "FAIL";
  }
}

function selfTest() {
  const secrets = new Set(["temporary-secret", "captcha-1234"]);
  const redacted = redactText(
    "Authorization: Bearer token-value password=temporary-secret captcha-1234",
    secrets,
  );
  assertion(!redacted.includes("temporary-secret"), "SELF_TEST_SECRET_REDACTION");
  assertion(!redacted.includes("captcha-1234"), "SELF_TEST_CAPTCHA_REDACTION");
  assertion(
    JSON.stringify(topLevelBodyKeys('{"decision":"APPROVE","comment":"ok"}'))
      === JSON.stringify(["comment", "decision"]),
    "SELF_TEST_BODY_KEYS",
  );
  const captchaSecrets = new Set();
  const captcha = findCaptcha({ data: { Key: "captcha-key-123", Code: "4821" } });
  collectCaptchaSecrets({ data: { Key: "captcha-key-123", Code: "4821" } }, captchaSecrets);
  assertion(captcha?.code === "4821" && captchaSecrets.size === 2, "SELF_TEST_CAPTCHA_PARSE");
  return {
    schemaVersion: 1,
    status: "PASS",
    engine: "chrome-cdp",
    selfTest: ["redaction", "bodyKeys", "captcha"],
  };
}

async function main() {
  if (process.argv.slice(2).includes("--self-test")) {
    try {
      process.stdout.write(`${JSON.stringify(selfTest())}\n`);
    } catch {
      process.stdout.write('{"schemaVersion":1,"status":"FAIL","engine":"chrome-cdp","selfTest":[]}\n');
      process.exitCode = 1;
    }
    return;
  }

  const result = emptyResult();
  const secrets = new Set();
  try {
    const input = await readStdin();
    const config = JSON.parse(input);
    collectCredentialSecrets(config, secrets);
    await runHarness(config, result, secrets);
  } catch (error) {
    result.status = error instanceof HarnessError ? error.status : "FAIL";
    result.console.push({ role: "driver", type: "error", text: safeCode(error) });
    result.security.dynamicSecretScan = "NOT_RUN";
    result.security.checkedSecretCount = secrets.size;
  }
  let output = JSON.stringify(result);
  if ([...secrets].some((secret) => output.includes(secret))) {
    result.status = "FAIL";
    result.security.dynamicSecretScan = "FAIL";
    output = JSON.stringify(result);
  }
  process.stdout.write(`${replaceSecrets(output, secrets)}\n`);
  if (result.status === "FAIL") process.exitCode = 1;
  if (result.status === "ENVIRONMENT_UNAVAILABLE") process.exitCode = 2;
}

void main();
