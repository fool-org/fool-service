<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import Button from "primevue/button";
import type {
  AgentCapability,
  AgentDraft,
  AgentProviderInfo,
  AgentSession,
  AgentTurnResult
} from "./api";
import { getApi, postApi } from "./api";

const providers = ref<AgentProviderInfo[]>([]);
const capabilities = ref<AgentCapability[]>([]);
const selectedProvider = ref("local");
const session = ref<AgentSession | null>(null);
const draft = ref<AgentDraft | null>(null);
const input = ref("");
const busy = ref(false);
const errorMessage = ref("");
const replySource = ref("");
const actionRequestId = ref("");

const currentCapability = computed(() =>
  capabilities.value.find((item) => item.id === session.value?.currentCapability) || capabilities.value[0]
);
const canAdvance = computed(() => Boolean(
  session.value?.status === "ACTIVE" && draft.value
));
const configuredProviderCount = computed(() => providers.value.filter((provider) => provider.configured).length);

onMounted(() => void loadCatalogs());

async function loadCatalogs() {
  busy.value = true;
  errorMessage.value = "";
  try {
    const [providerResponse, capabilityResponse] = await Promise.all([
      getApi<AgentProviderInfo[]>("/api/v1/agent/providers"),
      getApi<AgentCapability[]>("/api/v1/agent/capabilities")
    ]);
    providers.value = providerResponse.data;
    capabilities.value = capabilityResponse.data;
    selectedProvider.value = providers.value.find((provider) => provider.configured && provider.defaultProvider)?.id
      || providers.value.find((provider) => provider.configured)?.id
      || "local";
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : String(error);
  } finally {
    busy.value = false;
  }
}

async function sendMessage() {
  const content = input.value.trim();
  if (!content || busy.value) return;
  busy.value = true;
  errorMessage.value = "";
  try {
    if (!session.value) {
      session.value = (await postApi<AgentSession>("/api/v1/agent/sessions", {
        title: content.slice(0, 60)
      })).data;
    }
    const response = await postApi<AgentTurnResult>(
      `/api/v1/agent/sessions/${session.value.id}/messages`,
      {
        capability: session.value.currentCapability,
        content,
        provider: selectedProvider.value
      }
    );
    session.value = response.data.session;
    draft.value = response.data.draft;
    replySource.value = `${response.data.provider} / ${response.data.model}`;
    actionRequestId.value = response.data.actionRequestId || "";
    input.value = "";
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : String(error);
  } finally {
    busy.value = false;
  }
}

async function advanceCapability() {
  if (!session.value || !canAdvance.value || busy.value) return;
  busy.value = true;
  errorMessage.value = "";
  try {
    session.value = (await postApi<AgentSession>(
      `/api/v1/agent/sessions/${session.value.id}/advance`,
      {}
    )).data;
    draft.value = null;
    replySource.value = "";
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : String(error);
  } finally {
    busy.value = false;
  }
}

function newConversation() {
  session.value = null;
  draft.value = null;
  input.value = "";
  errorMessage.value = "";
  replySource.value = "";
  actionRequestId.value = "";
}

function roleLabel(role: string) {
  if (role === "USER") return "你";
  if (role === "AGENT") return "AI 助手";
  return "系统";
}
</script>

<template>
  <section class="agent-chat-page" aria-label="AI 配置助手">
    <header class="agent-chat-header">
      <div>
        <p class="agent-chat-eyebrow">Fool Service Agent</p>
        <h1>AI 配置助手</h1>
        <p>按报表、视图、模型、数据源和自动化顺序生成可审核的配置草案。</p>
      </div>
      <Button label="新会话" severity="secondary" outlined :disabled="busy || !session" @click="newConversation" />
    </header>

    <div class="agent-stage-list" aria-label="配置阶段">
      <span
        v-for="capability in capabilities"
        :key="capability.id"
        :class="{ active: capability.id === currentCapability?.id }"
      >
        {{ capability.displayName }}
      </span>
    </div>

    <div class="agent-chat-layout">
      <div class="agent-conversation">
        <div class="agent-provider-bar">
          <label>
            模型服务
            <select v-model="selectedProvider" :disabled="busy">
              <option value="local">本地规则（未调用模型）</option>
              <option
                v-for="provider in providers"
                :key="provider.id"
                :value="provider.id"
                :disabled="!provider.configured"
              >
                {{ provider.displayName }} · {{ provider.model }}{{ provider.configured ? "" : "（未配置）" }}
              </option>
            </select>
          </label>
          <span v-if="!configuredProviderCount" class="agent-provider-note">
            设置 DEEPSEEK_API_KEY 或 OPENAI_API_KEY 后重启即可启用模型。
          </span>
          <span v-else-if="replySource" class="agent-provider-note">最近回复：{{ replySource }}</span>
        </div>

        <div class="agent-messages" aria-live="polite">
          <div v-if="!session" class="agent-empty-state">
            <strong>从一个配置目标开始</strong>
            <span>例如：为订单数据配置按客户分组的月度报表。</span>
          </div>
          <article
            v-for="message in session?.messages || []"
            :key="message.id"
            class="agent-message"
            :class="`agent-message-${message.role.toLowerCase()}`"
          >
            <strong>{{ roleLabel(message.role) }}</strong>
            <p>{{ message.content }}</p>
          </article>
        </div>

        <p v-if="errorMessage" class="agent-chat-error" role="alert">{{ errorMessage }}</p>
        <form class="agent-composer" @submit.prevent="sendMessage">
          <label for="agent-message">给配置助手发送消息</label>
          <textarea
            id="agent-message"
            v-model="input"
            rows="3"
            :disabled="busy || session?.status === 'COMPLETED'"
            placeholder="描述目标；Shift + Enter 换行"
            @keydown.enter.exact.prevent="sendMessage"
          ></textarea>
          <div>
            <span>{{ currentCapability?.intent || "正在加载配置能力…" }}</span>
            <Button type="submit" label="发送" icon="pi pi-send" :loading="busy" :disabled="!input.trim()" />
          </div>
        </form>
      </div>

      <aside class="agent-draft-panel">
        <h2>当前草案</h2>
        <template v-if="draft">
          <span class="agent-risk">{{ draft.riskLevel }}</span>
          <p>{{ draft.summary }}</p>
          <p v-if="actionRequestId" class="agent-action-request">
            已创建受控动作请求 <code>{{ actionRequestId }}</code>；确认、审批和执行不会由模型自动完成。
          </p>
          <h3>验证步骤</h3>
          <ol>
            <li v-for="step in draft.validationSteps" :key="step">{{ step }}</li>
          </ol>
          <Button
            :label="session?.currentCapability === 'event-automation' ? '完成会话' : '进入下一阶段'"
            severity="secondary"
            :disabled="!canAdvance"
            @click="advanceCapability"
          />
        </template>
        <p v-else>助手回复后，这里会显示服务端生成的结构化草案与验证步骤。</p>
      </aside>
    </div>
  </section>
</template>

<style scoped>
.agent-chat-page {
  display: grid;
  gap: 18px;
  width: min(1180px, 100%);
  margin: 0 auto;
}

.agent-chat-header,
.agent-provider-bar,
.agent-composer > div {
  display: flex;
  gap: 16px;
  align-items: center;
  justify-content: space-between;
}

.agent-chat-header h1,
.agent-draft-panel h2,
.agent-draft-panel h3 {
  margin: 0;
}

.agent-chat-header p {
  margin: 4px 0 0;
  color: #64748b;
}

.agent-chat-eyebrow {
  color: #2563eb !important;
  font-size: 0.75rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.agent-stage-list {
  display: flex;
  gap: 8px;
  overflow-x: auto;
}

.agent-stage-list span,
.agent-risk {
  border: 1px solid #dbe3ef;
  border-radius: 999px;
  background: #ffffff;
  color: #64748b;
  padding: 7px 12px;
  white-space: nowrap;
}

.agent-stage-list span.active {
  border-color: #2563eb;
  background: #eff6ff;
  color: #1d4ed8;
}

.agent-action-request {
  overflow-wrap: anywhere;
  border-left: 3px solid #f59e0b;
  background: #fffbeb;
  padding: 10px 12px;
  color: #78350f;
}

.agent-chat-layout {
  display: grid;
  grid-template-columns: minmax(0, 2fr) minmax(260px, 1fr);
  gap: 18px;
}

.agent-conversation,
.agent-draft-panel {
  min-width: 0;
  border: 1px solid #e2e8f0;
  border-radius: 12px;
  background: #ffffff;
  box-shadow: 0 8px 24px rgba(15, 23, 42, 0.05);
}

.agent-provider-bar,
.agent-composer,
.agent-draft-panel {
  padding: 16px;
}

.agent-provider-bar {
  border-bottom: 1px solid #e2e8f0;
}

.agent-provider-bar label {
  display: flex;
  flex-direction: row;
  align-items: center;
}

.agent-provider-bar select {
  min-width: 240px;
  border: 1px solid #cbd5e1;
  border-radius: 7px;
  background: #ffffff;
  padding: 8px 10px;
}

.agent-provider-note {
  color: #64748b;
  font-size: 0.8rem;
  text-align: right;
}

.agent-messages {
  display: grid;
  gap: 12px;
  min-height: 360px;
  max-height: 58vh;
  overflow-y: auto;
  padding: 18px;
}

.agent-empty-state {
  display: grid;
  place-content: center;
  gap: 6px;
  color: #64748b;
  text-align: center;
}

.agent-message {
  width: fit-content;
  max-width: 82%;
  border-radius: 12px;
  background: #f1f5f9;
  padding: 11px 14px;
}

.agent-message-user {
  justify-self: end;
  background: #2563eb;
  color: #ffffff;
}

.agent-message-system {
  max-width: 100%;
  background: #fffbeb;
  color: #92400e;
}

.agent-message strong {
  font-size: 0.75rem;
}

.agent-message p {
  margin: 4px 0 0;
  white-space: pre-wrap;
}

.agent-chat-error {
  margin: 0 16px;
  color: #b91c1c;
}

.agent-composer {
  display: grid;
  gap: 9px;
  border-top: 1px solid #e2e8f0;
}

.agent-composer textarea {
  width: 100%;
  resize: vertical;
  border: 1px solid #cbd5e1;
  border-radius: 8px;
  padding: 10px 12px;
}

.agent-composer > div span,
.agent-draft-panel > p,
.agent-draft-panel li {
  color: #64748b;
  font-size: 0.84rem;
}

.agent-draft-panel {
  align-self: start;
}

.agent-draft-panel h3 {
  margin-top: 18px;
  font-size: 0.95rem;
}

.agent-draft-panel ol {
  display: grid;
  gap: 8px;
  padding-left: 20px;
}

.agent-risk {
  display: inline-block;
  margin-top: 14px;
  padding: 4px 8px;
  font-size: 0.72rem;
}

@media (max-width: 860px) {
  .agent-chat-layout {
    grid-template-columns: 1fr;
  }

  .agent-chat-header,
  .agent-provider-bar,
  .agent-composer > div {
    align-items: stretch;
    flex-direction: column;
  }

  .agent-provider-bar label,
  .agent-provider-bar select {
    width: 100%;
  }

  .agent-provider-note {
    text-align: left;
  }
}
</style>
