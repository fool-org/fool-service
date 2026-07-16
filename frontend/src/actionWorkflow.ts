import { postApi, type ActionIntent, type ActionRequestView } from "./api";

export interface MediumActionOptions {
  source?: "UI" | "CHAT" | "API";
  confirm?: (summary: string, request: ActionRequestView) => boolean | Promise<boolean>;
}

export interface HighActionOptions {
  source?: "UI" | "CHAT" | "API";
}

function idempotencyKey() {
  return typeof crypto !== "undefined" && "randomUUID" in crypto
    ? crypto.randomUUID()
    : `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

export async function executeMediumAction(intent: ActionIntent, options: MediumActionOptions = {}) {
  const created = await postApi<ActionRequestView>("/api/v1/actions", intent, {
    "Idempotency-Key": idempotencyKey(),
    "X-Action-Source": options.source || "UI"
  });
  const id = created.data.actionRequestId;
  const preview = await postApi<ActionRequestView>(`/api/v1/actions/${id}/preview`, {});
  const confirm = options.confirm || ((summary: string) => window.confirm(summary));
  if (!await confirm(previewSummary(preview.data), preview.data)) {
    return postApi<ActionRequestView>(`/api/v1/actions/${id}/cancel`, {});
  }
  await postApi<ActionRequestView>(`/api/v1/actions/${id}/confirm`, {});
  return postApi<ActionRequestView>(`/api/v1/actions/${id}/execute`, {});
}

export async function prepareHighAction(
  intent: ActionIntent,
  password: string,
  options: HighActionOptions = {}
) {
  await postApi<{ stepUpAt: string; expiresAt: string }>("/api/v1/auth/step-up", { password });
  const created = await postApi<ActionRequestView>("/api/v1/actions", intent, {
    "Idempotency-Key": idempotencyKey(),
    "X-Action-Source": options.source || "UI"
  });
  const preview = await postApi<ActionRequestView>(
    `/api/v1/actions/${created.data.actionRequestId}/preview`,
    {}
  );
  if (preview.data.riskLevel !== "HIGH" || preview.data.status !== "AWAITING_APPROVAL") {
    throw new Error("HIGH_ACTION_GATE_MISMATCH");
  }
  return preview;
}

export function approveHighAction(
  actionRequestId: string,
  decision: "APPROVE" | "REJECT",
  comment = ""
) {
  return postApi<ActionRequestView>(`/api/v1/actions/${actionRequestId}/approvals`, { decision, comment });
}

export function executeApprovedAction(actionRequestId: string) {
  return postApi<ActionRequestView>(`/api/v1/actions/${actionRequestId}/execute`, {});
}

export function previewSummary(request: ActionRequestView) {
  const preview = request.preview || {};
  const warnings = preview.warnings?.length ? `\n警告：${preview.warnings.join("；")}` : "";
  return [
    `确认执行：${request.action}`,
    `资源：${request.resourceKey}`,
    `影响对象：${preview.affectedObjectCount ?? "未知"}`,
    `风险：${request.riskLevel}（${request.riskReasons.join("、")}）`,
    `回滚说明：${preview.rollbackStrategy || "需人工恢复"}`,
    `有效期：${preview.previewExpiresAt || request.expiresAt}${warnings}`
  ].join("\n");
}

export function withoutBodyToken<T extends Record<string, unknown>>(request: T): Omit<T, "token" | "Token"> {
  const { token: _token, Token: _legacyToken, ...safe } = request;
  return safe;
}
