<script setup lang="ts">
import { ref } from "vue";
import type { MessageInfo } from "./api";
import {
  legacyMessageContent,
  legacyMessageId,
  legacyMessageResultKey,
  legacyMessageResultView,
  legacyMessageTime
} from "./viewWorkflow";

defineProps<{
  errorMessage: string;
  messages: MessageInfo[];
  pending: boolean;
  userName: string;
}>();
const emit = defineEmits<{
  logout: [];
  openMessage: [message: MessageInfo];
  refresh: [];
}>();
const open = ref(false);

function canOpenMessage(message: MessageInfo) {
  return legacyMessageResultView(message) > 0;
}

function openMessage(message: MessageInfo) {
  open.value = false;
  emit("openMessage", message);
}
</script>

<template>
  <div class="shell-actions">
    <button
      type="button"
      :aria-expanded="open"
      :class="{ active: open }"
      :disabled="pending"
      @click="open = !open"
    >
      Messages
      <strong v-if="messages.length" class="message-count">{{ messages.length }}</strong>
    </button>
    <span class="shell-user">{{ userName || "Signed in" }}</span>
    <button type="button" :disabled="pending" @click="emit('logout')">Sign out</button>

    <section v-if="open" class="message-popover" aria-label="Messages">
      <header>
        <h2>Messages</h2>
        <div>
          <button type="button" :disabled="pending" @click="emit('refresh')">Refresh</button>
          <button type="button" title="Close messages" @click="open = false">&times;</button>
        </div>
      </header>
      <p v-if="errorMessage" class="shell-error">{{ errorMessage }}</p>
      <ul v-if="messages.length">
        <li v-for="message in messages" :key="legacyMessageId(message)">
          <time>{{ legacyMessageTime(message) }}</time>
          <p>{{ legacyMessageContent(message) }}</p>
          <button v-if="canOpenMessage(message)" type="button" @click="openMessage(message)">
            Open {{ legacyMessageResultKey(message) }}
          </button>
        </li>
      </ul>
      <p v-else class="empty-message">No messages.</p>
    </section>
  </div>
</template>

<style scoped>
.shell-actions {
  position: relative;
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  justify-content: flex-end;
}

.shell-actions > button {
  min-height: 34px;
  padding: 0 10px;
}

.shell-actions > button.active {
  border-color: #0f766e;
  color: #0f766e;
}

.message-count {
  display: inline-grid;
  min-width: 20px;
  height: 20px;
  margin-left: 6px;
  place-items: center;
  border-radius: 10px;
  background: #b42318;
  color: #ffffff;
  font-size: 0.72rem;
}

.shell-user {
  max-width: 180px;
  overflow: hidden;
  color: #465563;
  font-size: 0.84rem;
  font-weight: 750;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-popover {
  position: absolute;
  z-index: 20;
  top: calc(100% + 8px);
  right: 0;
  width: min(380px, calc(100vw - 32px));
  border: 1px solid #cbd5df;
  border-radius: 6px;
  background: #ffffff;
  box-shadow: 0 14px 32px rgba(23, 33, 43, 0.16);
}

.message-popover header {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e2e8f0;
  padding: 10px 12px;
}

.message-popover header div {
  display: flex;
  gap: 6px;
}

.message-popover h2 {
  margin: 0;
  font-size: 0.94rem;
}

.message-popover header button {
  min-height: 32px;
  padding: 0 9px;
}

.message-popover ul {
  max-height: 320px;
  margin: 0;
  overflow: auto;
  padding: 0;
  list-style: none;
}

.message-popover li {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 4px 10px;
  border-bottom: 1px solid #e2e8f0;
  padding: 11px 12px;
}

.message-popover time {
  color: #647484;
  font-size: 0.72rem;
}

.message-popover p {
  grid-column: 1;
  margin: 0;
  overflow-wrap: anywhere;
}

.message-popover li button {
  grid-column: 2;
  grid-row: 1 / span 2;
  align-self: center;
}

.shell-error,
.empty-message {
  padding: 12px;
  color: #647484;
}

.shell-error {
  color: #b42318;
}

@media (max-width: 640px) {
  .shell-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .message-popover {
    right: auto;
    left: 0;
    width: calc(100vw - 28px);
  }
}
</style>
