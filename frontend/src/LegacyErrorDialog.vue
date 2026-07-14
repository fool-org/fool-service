<script setup lang="ts">
import Button from "primevue/button";
import Dialog from "primevue/dialog";

defineProps<{ message?: string }>();
const emit = defineEmits<{ dismiss: [] }>();
</script>

<template>
  <Dialog
    v-if="message"
    :visible="true"
    modal
    header="发生错误"
    :closable="false"
    :draggable="false"
    @update:visible="(visible) => { if (!visible) emit('dismiss') }"
  >
    <p>{{ message }}</p>
    <template #footer>
      <Button type="button" label="关闭" severity="secondary" outlined @click="emit('dismiss')" />
    </template>
  </Dialog>
</template>
