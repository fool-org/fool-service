<script setup lang="ts">
import { computed } from "vue";
import type { ListDataValue } from "./api";
import { isEnumField, isReadonlyField } from "./viewWorkflow";

const props = withDefaults(
  defineProps<{
    field: ListDataValue;
    modelValue: string;
    options?: { label: string; value: string }[];
    readonlyValue?: string;
  }>(),
  {
    options: () => [],
    readonlyValue: ""
  }
);

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

const value = computed({
  get: () => props.modelValue,
  set: (next) => emit("update:modelValue", next)
});
</script>

<template>
  <input v-if="isReadonlyField(field)" :value="readonlyValue || modelValue" disabled />
  <select v-else-if="isEnumField(field)" v-model="value">
    <option v-for="option in options" :key="option.value" :value="option.value">
      {{ option.label }}
    </option>
  </select>
  <input v-else v-model="value" />
</template>
