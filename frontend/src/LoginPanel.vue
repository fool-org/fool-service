<script setup lang="ts">
import { computed, ref, watch } from "vue";
import Button from "primevue/button";
import Dialog from "primevue/dialog";
import InputText from "primevue/inputtext";
import type { CheckCodeResult, LegacyInitAppResult } from "./api";
import {
  legacyCheckCodeImage,
  legacyCheckCodeKey,
  legacyInitAppCheckCode,
  legacyInitAppDbId
} from "./viewWorkflow";

const props = defineProps<{
  appInfo?: LegacyInitAppResult;
  checkCode?: CheckCodeResult;
  errorCode: string;
  errorMessage: string;
}>();
const emit = defineEmits<{
  dismissError: [];
  refresh: [];
  submit: [userId: string, password: string, dbId: string, checkCode: string];
}>();

const userId = ref("");
const password = ref("");
const checkCodeValue = ref("");
const resetDialogVisible = ref(false);

function text(...values: unknown[]) {
  const value = values.find((item) => typeof item === "string" && item.trim());
  return typeof value === "string" ? value.trim() : "";
}

const appName = computed(() => text(props.appInfo?.appName, props.appInfo?.AppName, "Fool Service"));
const appImage = computed(() => text(props.appInfo?.appImg, props.appInfo?.AppImg));
const appVersion = computed(() => text(props.appInfo?.appVersion, props.appInfo?.AppVersion));
const appPowerBy = computed(() => text(props.appInfo?.appPowerBy, props.appInfo?.AppPowerBy));
const appUrl = computed(() => text(props.appInfo?.appUrl, props.appInfo?.AppUrl));
const appHref = computed(() => appUrl.value && !appUrl.value.includes("://") ? `http://${appUrl.value}` : appUrl.value);
const dbId = computed(() => legacyInitAppDbId(props.appInfo));
const effectiveCheckCode = computed(() => props.checkCode ?? legacyInitAppCheckCode(props.appInfo));
const captchaKey = computed(() => legacyCheckCodeKey(effectiveCheckCode.value));
const captchaImage = computed(() => legacyCheckCodeImage(effectiveCheckCode.value));

watch(captchaKey, () => {
  checkCodeValue.value = "";
});

function submit() {
  emit("submit", userId.value, password.value, dbId.value, checkCodeValue.value);
}

function dismissLoginDialog() {
  if (resetDialogVisible.value) {
    resetDialogVisible.value = false;
    emit("refresh");
    return;
  }
  emit("dismissError");
}
</script>

<template>
  <main class="login-page">
    <section class="login-card">
      <header class="login-brand">
        <img v-if="appImage" :alt="appName" :src="appImage" />
        <h1>{{ appName }}</h1>
        <p>欢迎回来，请登录后继续。</p>
      </header>

      <form class="login-form" aria-label="登录" @submit.prevent="submit">
        <InputText v-model="userId" aria-label="用户名" autocomplete="username" placeholder="用户名" fluid />
        <InputText v-model="password" aria-label="密码" autocomplete="current-password" placeholder="密码" type="password" fluid />
        <InputText v-model="checkCodeValue" aria-label="验证码" autocomplete="one-time-code" placeholder="验证码" fluid />
        <div class="captcha-preview">
          <img v-if="captchaImage" alt="验证码" :src="`data:image/jpeg;base64,${captchaImage}`" />
          <Button type="button" label="刷新" severity="secondary" text @click="emit('refresh')" />
        </div>
        <input name="check-code-key" type="hidden" :value="captchaKey" />

        <div class="login-actions">
          <Button type="submit" label="登录" :disabled="!captchaKey" />
          <Button type="button" label="重置" severity="secondary" @click="resetDialogVisible = true" />
        </div>
      </form>

      <footer v-if="appVersion || appPowerBy">
        <span>{{ appVersion }}</span>
        <a v-if="appPowerBy && appHref" :href="appHref" rel="noreferrer" target="_blank">{{ appPowerBy }}</a>
        <span v-else>{{ appPowerBy }}</span>
      </footer>
    </section>

    <Dialog
      v-if="errorMessage || resetDialogVisible"
      :visible="true"
      modal
      header="发生错误"
      :closable="false"
      :draggable="false"
      @update:visible="(visible) => { if (!visible) dismissLoginDialog() }"
    >
      <p v-if="errorCode">错误代码:{{ errorCode }}</p>
      <p v-if="errorMessage">{{ errorCode ? `错误信息:${errorMessage}` : errorMessage }}</p>
      <template #footer>
        <Button type="button" label="关闭" severity="secondary" outlined @click="dismissLoginDialog" />
      </template>
    </Dialog>

  </main>
</template>

<style scoped>
.login-page {
  display: grid;
  place-items: center;
  min-height: 100vh;
  padding: 24px 16px;
  background:
    radial-gradient(circle at 15% 15%, rgba(51, 122, 183, 0.18), transparent 28rem),
    radial-gradient(circle at 85% 85%, rgba(125, 211, 252, 0.2), transparent 24rem),
    #f4f7fb;
  color: #0f172a;
}

.login-card {
  display: grid;
  width: min(100%, 420px);
  gap: 24px;
  border: 1px solid rgba(226, 232, 240, 0.92);
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.94);
  box-shadow: 0 24px 70px rgba(15, 23, 42, 0.12);
  padding: 32px;
  backdrop-filter: blur(16px);
}

.login-brand {
  text-align: center;
}

.login-brand > img {
  display: block;
  width: min(200px, 100%);
  max-height: 120px;
  margin: 0 auto;
  object-fit: contain;
}

.login-brand h1 {
  margin: 12px 0 0;
  font-size: 1.8rem;
  font-weight: 750;
  letter-spacing: -0.02em;
}

.login-brand p {
  margin: 7px 0 0;
  color: #64748b;
  font-size: 0.9rem;
}

.login-form {
  display: grid;
  width: 100%;
  gap: 12px;
}

.login-form :deep(.p-inputtext),
.login-form :deep(.p-select) {
  width: 100%;
}

.login-form :deep(.p-inputtext) {
  height: 44px;
  border-radius: 10px;
  padding-inline: 13px;
}

.captcha-preview {
  display: flex;
  min-height: 52px;
  justify-content: space-between;
  gap: 8px;
  align-items: center;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  background: #f8fafc;
  padding: 5px 8px;
}

.captcha-preview img {
  width: 100px;
  height: 40px;
  border: 1px solid #cbd5e1;
  border-radius: 7px;
  object-fit: cover;
}

.login-actions {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
}

.login-actions :deep(.p-button) {
  width: 100%;
}

.login-page footer {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #64748b;
  font-size: 0.8rem;
  text-align: center;
}

.login-page footer a {
  color: #337ab7;
}

@media (max-width: 520px) {
  .login-page {
    padding: 14px;
  }

  .login-card {
    border-radius: 16px;
    padding: 24px 20px;
  }
}
</style>
