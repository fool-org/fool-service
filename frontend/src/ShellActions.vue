<script setup lang="ts">
import Button from "primevue/button";
import Dialog from "primevue/dialog";
import type { MessageInfo } from "./api";
import {
  legacyMessageContent,
  legacyMessageResultView,
  legacyMessageTime
} from "./viewWorkflow";

defineProps<{
  activeMessage: MessageInfo | null;
  userAvatar: string;
  userName: string;
}>();
const emit = defineEmits<{
  dismissMessage: [];
  openMessage: [message: MessageInfo];
}>();

function canOpenMessage(message: MessageInfo) {
  return legacyMessageResultView(message) > 0;
}

function openMessage(message: MessageInfo) {
  emit("dismissMessage");
  emit("openMessage", message);
}
</script>

<template>
  <div class="shell-actions">
    <span class="shell-user">
      <img v-if="userAvatar" class="shell-avatar" :src="userAvatar" alt="" />
      <i v-else class="pi pi-user"></i>
      <span class="shell-user-name">{{ userName || "已登录" }}</span>
    </span>

    <Dialog
      v-if="activeMessage"
      :visible="true"
      modal
      header="系统消息"
      :closable="false"
      :draggable="false"
      @update:visible="(visible) => { if (!visible) emit('dismissMessage') }"
    >
      <p>时间 <span>{{ legacyMessageTime(activeMessage) }}</span></p>
      <p>{{ legacyMessageContent(activeMessage) }}</p>
      <template #footer>
        <Button
          type="button"
          label="查看详细"
          severity="secondary"
          outlined
          :disabled="!canOpenMessage(activeMessage)"
          @click="openMessage(activeMessage)"
        />
        <Button type="button" label="确定" severity="secondary" outlined @click="emit('dismissMessage')" />
      </template>
    </Dialog>
  </div>
</template>

<style scoped>
.shell-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  justify-content: flex-end;
}

.shell-user {
  max-width: 180px;
  overflow: hidden;
  display: inline-flex;
  gap: 0;
  align-items: center;
  color: #333333;
  font-size: 10px;
  font-weight: 400;
  line-height: 1.42857143;
  white-space: nowrap;
}

.shell-user > i,
.shell-avatar {
  flex: 0 0 50px;
  width: 50px;
  height: 50px;
}

.shell-user > i {
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.shell-avatar {
  border-radius: 50%;
  object-fit: cover;
}

.shell-user-name {
  overflow: hidden;
  color: #337ab7;
  padding: 10px 15px;
  text-overflow: ellipsis;
}

@media (max-width: 640px) {
  .shell-actions {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>
