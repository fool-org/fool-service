<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import Button from "primevue/button";
import InputText from "primevue/inputtext";
import { getApi, postApi, type ActionRequestView } from "./api";

const requestId = ref(new URLSearchParams(window.location.search).get("actionRequestId") || "");
const request = ref<ActionRequestView | null>(null);
const comment = ref("");
const busy = ref(false);
const errorMessage = ref("");
const infoMessage = ref("");

const approvalSummary = computed(() => request.value
  ? `${request.value.approvalCount}/${request.value.requiredApprovals}`
  : "0/0");

async function run(action: () => Promise<{ data: ActionRequestView }>, message: string) {
  busy.value = true;
  errorMessage.value = "";
  infoMessage.value = "";
  try {
    request.value = (await action()).data;
    infoMessage.value = message;
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : String(error);
  } finally {
    busy.value = false;
  }
}

async function load() {
  const id = requestId.value.trim();
  if (!id) return;
  await run(() => getApi<ActionRequestView>(`/api/v1/actions/${encodeURIComponent(id)}`), "已加载服务端不可变预览。");
}

async function decide(decision: "APPROVE" | "REJECT") {
  if (!request.value) return;
  await run(
    () => postApi<ActionRequestView>(`/api/v1/actions/${request.value!.actionRequestId}/approvals`, {
      decision,
      comment: comment.value
    }),
    decision === "APPROVE" ? "审批已记录。" : "请求已拒绝。"
  );
}

async function execute() {
  if (!request.value) return;
  await run(
    () => postApi<ActionRequestView>(`/api/v1/actions/${request.value!.actionRequestId}/execute`, {}),
    "动作执行完成。"
  );
}

async function cancel() {
  if (!request.value) return;
  await run(
    () => postApi<ActionRequestView>(`/api/v1/actions/${request.value!.actionRequestId}/cancel`, {}),
    "请求已取消。"
  );
}

onMounted(() => {
  if (requestId.value) void load();
});
</script>

<template>
  <section class="action-center" aria-label="受控动作中心">
    <header>
      <p>Fool Service Authorization</p>
      <h1>受控动作中心</h1>
      <span>只展示服务端返回的不可变预览；权限、风险与审批状态不在浏览器计算。</span>
    </header>

    <form class="action-lookup" aria-label="查询动作请求" @submit.prevent="load">
      <label for="action-request-id">动作请求 ID</label>
      <div>
        <InputText id="action-request-id" v-model="requestId" aria-label="动作请求 ID" autocomplete="off" />
        <Button type="submit" label="查询" :loading="busy" :disabled="!requestId.trim()" />
      </div>
    </form>

    <p v-if="errorMessage" class="message error" role="alert">{{ errorMessage }}</p>
    <p v-if="infoMessage" class="message success" role="status">{{ infoMessage }}</p>

    <article v-if="request" class="action-card">
      <div class="action-heading">
        <div>
          <span class="risk">{{ request.riskLevel }}</span>
          <h2>{{ request.action }}</h2>
          <code>{{ request.resourceKey }}</code>
        </div>
        <strong class="status">{{ request.status }}</strong>
      </div>

      <dl>
        <div><dt>影响对象</dt><dd>{{ request.preview.affectedObjectCount ?? "未知" }}</dd></div>
        <div><dt>审批进度</dt><dd>{{ approvalSummary }}</dd></div>
        <div><dt>到期时间</dt><dd>{{ request.preview.previewExpiresAt || request.expiresAt }}</dd></div>
        <div><dt>回滚说明</dt><dd>{{ request.preview.rollbackStrategy || "需人工恢复" }}</dd></div>
      </dl>

      <section aria-label="风险与范围">
        <h3>风险原因</h3>
        <ul><li v-for="reason in request.riskReasons" :key="reason">{{ reason }}</li></ul>
        <h3>有效范围</h3>
        <pre>{{ JSON.stringify(request.preview.effectiveScope || {}, null, 2) }}</pre>
        <h3>字段差异</h3>
        <pre>{{ JSON.stringify(request.preview.fieldDiff || {}, null, 2) }}</pre>
      </section>

      <p v-if="request.owned && request.status === 'AWAITING_APPROVAL'" class="message">
        当前用户是发起人；必须由独立审批人完成审批。
      </p>

      <div v-if="request.approvable" class="approval-controls">
        <label for="approval-comment">审批意见</label>
        <InputText id="approval-comment" v-model="comment" aria-label="审批意见" maxlength="1000" />
        <Button label="批准" :loading="busy" @click="decide('APPROVE')" />
        <Button label="拒绝" severity="danger" outlined :loading="busy" @click="decide('REJECT')" />
      </div>

      <div v-if="request.executable || request.cancellable" class="owner-controls">
        <Button v-if="request.executable" label="执行已批准动作" :loading="busy" @click="execute" />
        <Button v-if="request.cancellable" label="取消请求" severity="secondary" outlined :loading="busy" @click="cancel" />
      </div>
    </article>
  </section>
</template>

<style scoped>
.action-center { display: grid; gap: 18px; width: min(960px, 100%); margin: 0 auto; }
.action-center > header p { margin: 0; color: #2563eb; font-size: .75rem; font-weight: 800; letter-spacing: .12em; text-transform: uppercase; }
.action-center > header h1 { margin: 4px 0; }
.action-center > header span { color: #64748b; }
.action-lookup, .action-card { border: 1px solid #e2e8f0; border-radius: 12px; background: white; padding: 18px; }
.action-lookup { display: grid; gap: 8px; }
.action-lookup > div { display: grid; grid-template-columns: 1fr auto; gap: 10px; }
.action-heading { display: flex; justify-content: space-between; gap: 16px; align-items: start; }
.action-heading h2 { margin: 8px 0 4px; }
.risk, .status { display: inline-block; border-radius: 999px; background: #fff7ed; color: #9a3412; padding: 5px 9px; font-size: .75rem; }
dl { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px; margin: 18px 0; }
dl div { border: 1px solid #e2e8f0; border-radius: 8px; padding: 10px; }
dt { color: #64748b; font-size: .75rem; } dd { margin: 4px 0 0; overflow-wrap: anywhere; }
pre { overflow: auto; border-radius: 8px; background: #f8fafc; padding: 10px; white-space: pre-wrap; }
.message { border-radius: 8px; background: #f8fafc; padding: 10px 12px; }
.message.error { background: #fef2f2; color: #b91c1c; }
.message.success { background: #f0fdf4; color: #166534; }
.approval-controls, .owner-controls { display: flex; gap: 10px; align-items: end; flex-wrap: wrap; margin-top: 16px; }
.approval-controls label { width: 100%; }
.approval-controls :deep(.p-inputtext) { flex: 1 1 260px; }
@media (max-width: 640px) { dl { grid-template-columns: 1fr; } .action-heading { flex-direction: column; } }
</style>
