<script setup lang="ts">
import { ref } from "vue";
import Badge from "primevue/badge";
import Button from "primevue/button";
import Message from "primevue/message";
import Popover from "primevue/popover";
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
const messagePopover = ref();

function toggleMessages(event: Event) {
  messagePopover.value?.toggle(event);
}

function canOpenMessage(message: MessageInfo) {
  return legacyMessageResultView(message) > 0;
}

function openMessage(message: MessageInfo) {
  messagePopover.value?.hide();
  emit("openMessage", message);
}
</script>

<template>
  <div class="shell-actions">
    <Button
      :aria-expanded="open"
      :disabled="pending"
      label="Messages"
      icon="pi pi-bell"
      severity="secondary"
      text
      @click="toggleMessages"
    >
      <Badge v-if="messages.length" :value="messages.length" severity="danger" />
    </Button>
    <span class="shell-user"><i class="pi pi-user"></i>{{ userName || "Signed in" }}</span>
    <Button type="button" label="Sign out" icon="pi pi-sign-out" severity="secondary" text :disabled="pending" @click="emit('logout')" />

    <Popover ref="messagePopover" class="message-popover" aria-label="Messages" @show="open = true" @hide="open = false">
      <header>
        <h2>Messages</h2>
        <div>
          <Button type="button" label="Refresh" icon="pi pi-refresh" size="small" severity="secondary" text :disabled="pending" @click="emit('refresh')" />
          <Button type="button" icon="pi pi-times" size="small" severity="secondary" text title="Close messages" aria-label="Close messages" @click="messagePopover?.hide()" />
        </div>
      </header>
      <Message v-if="errorMessage" severity="error" :closable="false">{{ errorMessage }}</Message>
      <ul v-if="messages.length">
        <li v-for="message in messages" :key="legacyMessageId(message)">
          <time>{{ legacyMessageTime(message) }}</time>
          <p>{{ legacyMessageContent(message) }}</p>
          <Button
            v-if="canOpenMessage(message)"
            type="button"
            :label="`Open ${legacyMessageResultKey(message)}`"
            icon="pi pi-arrow-up-right"
            size="small"
            text
            @click="openMessage(message)"
          />
        </li>
      </ul>
      <p v-else class="empty-message">No messages.</p>
    </Popover>
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

.shell-actions :deep(.p-button .p-badge) {
  margin-left: 6px;
}

.shell-user {
  max-width: 180px;
  overflow: hidden;
  display: inline-flex;
  gap: 7px;
  align-items: center;
  color: #334155;
  font-size: 0.84rem;
  font-weight: 750;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-popover {
  width: min(380px, calc(100vw - 32px));
}

.message-popover header {
  display: flex;
  gap: 12px;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e2e8f0;
  padding-bottom: 10px;
}

.message-popover header div {
  display: flex;
  gap: 6px;
}

.message-popover h2 {
  margin: 0;
  font-size: 0.94rem;
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
  color: #64748b;
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

.empty-message {
  padding: 12px;
  color: #64748b;
}

@media (max-width: 640px) {
  .shell-actions {
    width: 100%;
    justify-content: flex-start;
  }

}
</style>
