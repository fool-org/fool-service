<script setup lang="ts">
import { computed, ref, watch } from "vue";
import Button from "primevue/button";
import InputText from "primevue/inputtext";
import Message from "primevue/message";
import Select from "primevue/select";
import type { CheckCodeResult, LegacyInitAppResult, LegacyStoreBaseInfo } from "./api";
import { legacyCheckCodeImage, legacyCheckCodeKey, legacyInitAppCheckCode } from "./viewWorkflow";

const props = defineProps<{
  appInfo?: LegacyInitAppResult;
  checkCode?: CheckCodeResult;
  errorMessage: string;
  pending: boolean;
}>();
const emit = defineEmits<{
  refresh: [];
  submit: [userId: string, password: string, dbId: string, checkCode: string];
}>();

const userId = ref("");
const password = ref("");
const dbId = ref("");
const checkCodeValue = ref("");

function text(...values: unknown[]) {
  const value = values.find((item) => typeof item === "string" && item.trim());
  return typeof value === "string" ? value.trim() : "";
}

function databaseId(database: LegacyStoreBaseInfo) {
  return text(database.dbId, database.DbId);
}

function databaseName(database: LegacyStoreBaseInfo) {
  return text(database.dbName, database.DbName, databaseId(database));
}

const appName = computed(() => text(props.appInfo?.appName, props.appInfo?.AppName, "Fool Service"));
const appImage = computed(() => text(props.appInfo?.appImg, props.appInfo?.AppImg));
const appVersion = computed(() => text(props.appInfo?.appVersion, props.appInfo?.AppVersion));
const appPowerBy = computed(() => text(props.appInfo?.appPowerBy, props.appInfo?.AppPowerBy));
const appUrl = computed(() => text(props.appInfo?.appUrl, props.appInfo?.AppUrl));
const databases = computed(() => props.appInfo?.dbs ?? props.appInfo?.Dbs ?? []);
const databaseOptions = computed(() => databases.value.map((database) => ({
  label: databaseName(database),
  value: databaseId(database)
})));
const effectiveCheckCode = computed(() => props.checkCode ?? legacyInitAppCheckCode(props.appInfo));
const captchaKey = computed(() => legacyCheckCodeKey(effectiveCheckCode.value));
const captchaImage = computed(() => legacyCheckCodeImage(effectiveCheckCode.value));

watch(databases, (items) => {
  if (!items.some((item) => databaseId(item) === dbId.value)) {
    dbId.value = items[0] ? databaseId(items[0]) : "";
  }
}, { immediate: true });

watch(captchaKey, () => {
  checkCodeValue.value = "";
});

function submit() {
  emit("submit", userId.value.trim(), password.value, dbId.value, checkCodeValue.value.trim());
}
</script>

<template>
  <main class="login-page">
    <header class="login-brand">
      <img v-if="appImage" :alt="appName" :src="appImage" />
      <h1>{{ appName }}</h1>
    </header>

    <form class="login-form" aria-label="登录" @submit.prevent="submit">
      <InputText v-model="userId" aria-label="用户名" autocomplete="username" placeholder="用户名" required fluid />
      <InputText v-model="password" aria-label="密码" autocomplete="current-password" placeholder="密码" required type="password" fluid />
      <Select
        v-if="databases.length > 1"
        v-model="dbId"
        aria-label="数据库"
        :options="databaseOptions"
        option-label="label"
        option-value="value"
        placeholder="数据库"
        required
        fluid
      />
      <InputText v-model="checkCodeValue" aria-label="验证码" autocomplete="one-time-code" maxlength="8" placeholder="验证码" required fluid />
      <div class="captcha-preview">
        <img v-if="captchaImage" alt="验证码" :src="`data:image/jpeg;base64,${captchaImage}`" />
        <Button type="button" label="刷新" severity="secondary" text :disabled="pending" @click="emit('refresh')" />
      </div>
      <input name="check-code-key" type="hidden" :value="captchaKey" />

      <Message v-if="errorMessage" severity="error" :closable="false">{{ errorMessage }}</Message>

      <div class="login-actions">
        <Button type="submit" :label="pending ? '登录中...' : '登录'" :loading="pending" :disabled="pending || !captchaKey" />
        <Button type="button" label="重置" severity="secondary" :disabled="pending" @click="emit('refresh')" />
      </div>
    </form>

    <footer v-if="appVersion || appPowerBy">
      <span>{{ appVersion }}</span>
      <a v-if="appPowerBy && appUrl" :href="appUrl" rel="noreferrer" target="_blank">{{ appPowerBy }}</a>
      <span v-else>{{ appPowerBy }}</span>
    </footer>
  </main>
</template>

<style scoped>
.login-page {
  display: flex;
  flex-direction: column;
  justify-content: center;
  min-height: 100vh;
  align-items: center;
  gap: 18px;
  padding: 24px 16px;
  background: #ffffff;
  color: #333333;
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
  font-size: 2rem;
  font-weight: 500;
  letter-spacing: 0;
}

.login-form {
  display: grid;
  width: min(100%, 240px);
  gap: 8px;
}

.login-form :deep(.p-inputtext),
.login-form :deep(.p-select) {
  width: 100%;
}

.captcha-preview {
  display: flex;
  min-height: 40px;
  justify-content: flex-end;
  gap: 8px;
  align-items: center;
}

.captcha-preview img {
  width: 100px;
  height: 40px;
  border: 1px solid #cccccc;
  border-radius: 4px;
  object-fit: cover;
}

.login-actions {
  display: grid;
  gap: 8px;
}

.login-actions :deep(.p-button) {
  width: 100%;
}

.login-page footer {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  color: #666666;
  font-size: 0.8rem;
  text-align: center;
}

.login-page footer a {
  color: #337ab7;
}

@media (max-width: 520px) {
  .login-page {
    padding: 24px 14px;
  }
}
</style>
