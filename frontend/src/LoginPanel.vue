<script setup lang="ts">
import { computed, ref, watch } from "vue";
import Button from "primevue/button";
import Card from "primevue/card";
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
const appTitle = computed(() => {
  const value = text(props.appInfo?.appTitle, props.appInfo?.AppTitle);
  return value === appName.value ? "" : value;
});
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

function reset() {
  userId.value = "";
  password.value = "";
  checkCodeValue.value = "";
  dbId.value = databases.value[0] ? databaseId(databases.value[0]) : "";
}

function submit() {
  emit("submit", userId.value.trim(), password.value, dbId.value, checkCodeValue.value.trim());
}
</script>

<template>
  <main class="login-page">
    <header class="login-brand">
      <img v-if="appImage" :alt="appName" :src="appImage" />
      <span v-else aria-hidden="true">F</span>
      <p v-if="appTitle">{{ appTitle }}</p>
      <h1>{{ appName }}</h1>
    </header>

    <Card class="login-card">
      <template #title>Welcome back</template>
      <template #subtitle>Sign in to continue to {{ appName }}</template>
      <template #content>
        <form class="login-form" aria-label="Sign in" @submit.prevent="submit">
          <label>
            User ID
            <InputText v-model="userId" autocomplete="username" required fluid />
          </label>
          <label>
            Password
            <InputText v-model="password" autocomplete="current-password" required type="password" fluid />
          </label>
          <label v-if="databases.length">
            Database
            <Select
              v-model="dbId"
              :options="databaseOptions"
              option-label="label"
              option-value="value"
              required
              fluid
            />
          </label>
          <label>
            Check code
            <span class="captcha-row">
              <InputText v-model="checkCodeValue" autocomplete="one-time-code" maxlength="8" required fluid />
              <img v-if="captchaImage" alt="Check code" :src="`data:image/jpeg;base64,${captchaImage}`" />
              <Button type="button" label="Refresh" icon="pi pi-refresh" severity="secondary" outlined :disabled="pending" @click="emit('refresh')" />
            </span>
          </label>
          <input name="check-code-key" type="hidden" :value="captchaKey" />

          <Message v-if="errorMessage" severity="error" :closable="false">{{ errorMessage }}</Message>

          <div class="login-actions">
            <Button type="submit" :label="pending ? 'Please wait...' : 'Sign in'" icon="pi pi-sign-in" :loading="pending" :disabled="pending || !captchaKey" />
            <Button type="button" label="Reset" severity="secondary" outlined :disabled="pending" @click="reset" />
          </div>
        </form>
      </template>
    </Card>

    <footer v-if="appVersion || appPowerBy">
      <span>{{ appVersion }}</span>
      <a v-if="appPowerBy && appUrl" :href="appUrl" rel="noreferrer" target="_blank">{{ appPowerBy }}</a>
      <span v-else>{{ appPowerBy }}</span>
    </footer>
  </main>
</template>

<style scoped>
.login-page {
  display: grid;
  min-height: 100vh;
  grid-template-rows: 1fr auto 1fr;
  place-items: center;
  gap: 28px;
  background:
    radial-gradient(circle at 50% 0%, rgba(79, 70, 229, 0.13), transparent 36%),
    #f8fafc;
  padding: 32px 20px;
  color: #0f172a;
}

.login-brand {
  align-self: end;
  text-align: center;
}

.login-brand > span,
.login-brand > img {
  display: inline-grid;
  width: 56px;
  height: 56px;
  place-items: center;
  border-radius: 16px;
}

.login-brand > span {
  background: linear-gradient(135deg, #4f46e5, #7c3aed);
  color: #ffffff;
  font-size: 1.45rem;
  font-weight: 800;
}

.login-brand > img {
  object-fit: contain;
}

.login-brand p {
  margin: 16px 0 4px;
  color: #64748b;
  font-size: 0.82rem;
}

.login-brand h1 {
  margin: 0;
  font-size: 1.75rem;
  letter-spacing: 0;
}

.login-card {
  width: min(100%, 390px);
  border: 1px solid #e2e8f0;
  box-shadow: 0 24px 60px rgba(15, 23, 42, 0.12);
}

.login-form {
  display: grid;
  gap: 16px;
}

.login-form label {
  display: grid;
  gap: 6px;
  color: #334155;
  font-size: 0.82rem;
  font-weight: 700;
}

.captcha-row {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 100px auto;
  gap: 8px;
  align-items: center;
}

.captcha-row img {
  width: 100px;
  height: 40px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  object-fit: cover;
}

.login-actions {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
}

.login-page footer {
  display: flex;
  align-self: start;
  gap: 12px;
  color: #64748b;
  font-size: 0.78rem;
}

.login-page footer a {
  color: #4f46e5;
}

@media (max-width: 520px) {
  .login-page {
    padding: 24px 14px;
  }

  .captcha-row {
    grid-template-columns: minmax(0, 1fr) 100px;
  }

  .captcha-row button {
    grid-column: 1 / -1;
  }
}
</style>
