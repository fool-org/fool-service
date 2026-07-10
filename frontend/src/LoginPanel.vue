<script setup lang="ts">
import { computed, ref, watch } from "vue";
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

    <form class="login-form" aria-label="Sign in" @submit.prevent="submit">
      <label>
        User ID
        <input v-model="userId" autocomplete="username" required />
      </label>
      <label>
        Password
        <input v-model="password" autocomplete="current-password" required type="password" />
      </label>
      <label v-if="databases.length">
        Database
        <select v-model="dbId" required>
          <option v-for="database in databases" :key="databaseId(database)" :value="databaseId(database)">
            {{ databaseName(database) }}
          </option>
        </select>
      </label>
      <label>
        Check code
        <span class="captcha-row">
          <input v-model="checkCodeValue" autocomplete="one-time-code" maxlength="8" required />
          <img v-if="captchaImage" alt="Check code" :src="`data:image/jpeg;base64,${captchaImage}`" />
          <button type="button" :disabled="pending" @click="emit('refresh')">Refresh</button>
        </span>
      </label>
      <input name="check-code-key" type="hidden" :value="captchaKey" />

      <p v-if="errorMessage" class="login-error" role="alert">{{ errorMessage }}</p>

      <div class="login-actions">
        <button class="primary" type="submit" :disabled="pending || !captchaKey">
          {{ pending ? "Please wait..." : "Sign in" }}
        </button>
        <button type="button" :disabled="pending" @click="reset">Reset</button>
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
  display: grid;
  min-height: 100vh;
  grid-template-rows: 1fr auto 1fr;
  place-items: center;
  gap: 28px;
  background: #f3f6f8;
  padding: 32px 20px;
  color: #17212b;
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
  border-radius: 6px;
}

.login-brand > span {
  background: #17212b;
  color: #ffffff;
  font-size: 1.45rem;
  font-weight: 800;
}

.login-brand > img {
  object-fit: contain;
}

.login-brand p {
  margin: 16px 0 4px;
  color: #647484;
  font-size: 0.82rem;
}

.login-brand h1 {
  margin: 0;
  font-size: 1.75rem;
  letter-spacing: 0;
}

.login-form {
  display: grid;
  width: min(100%, 390px);
  gap: 14px;
  border: 1px solid #d7e0e8;
  border-radius: 6px;
  background: #ffffff;
  padding: 24px;
  box-shadow: 0 14px 34px rgba(23, 33, 43, 0.08);
}

.login-form label {
  display: grid;
  gap: 6px;
  color: #465563;
  font-size: 0.82rem;
  font-weight: 700;
}

.login-form input,
.login-form select,
.login-form button {
  min-height: 42px;
}

.login-form input,
.login-form select {
  min-width: 0;
  border: 1px solid #cbd5df;
  border-radius: 5px;
  background: #ffffff;
  padding: 0 10px;
  color: #17212b;
  font: inherit;
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
  border: 1px solid #d7e0e8;
  object-fit: cover;
}

.login-actions {
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 8px;
}

.login-actions button,
.captcha-row button {
  border: 1px solid #cbd5df;
  border-radius: 5px;
  background: #ffffff;
  padding: 0 12px;
  color: #263646;
  font-weight: 700;
}

.login-actions button.primary {
  border-color: #0f766e;
  background: #0f766e;
  color: #ffffff;
}

.login-error {
  margin: 0;
  color: #b42318;
  font-size: 0.84rem;
}

.login-page footer {
  display: flex;
  align-self: start;
  gap: 12px;
  color: #647484;
  font-size: 0.78rem;
}

.login-page footer a {
  color: #0f766e;
}

@media (max-width: 520px) {
  .login-page {
    padding: 24px 14px;
  }

  .login-form {
    padding: 18px;
  }

  .captcha-row {
    grid-template-columns: minmax(0, 1fr) 100px;
  }

  .captcha-row button {
    grid-column: 1 / -1;
  }
}
</style>
