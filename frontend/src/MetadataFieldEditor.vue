<script setup lang="ts">
import { computed, ref } from "vue";
import type { InputQueryItem, InputQueryResult, ListDataValue } from "./api";
import { postApi } from "./api";
import { buildInputQueryRequest } from "./payload";
import {
  fieldInputChecked,
  fieldInputType,
  fieldInputValue,
  fieldKey,
  inputQueryItemId,
  inputQueryItemText,
  isEnumField,
  isLookupField,
  isMultilineField,
  isReadonlyField,
  legacyInputQueryItems
} from "./viewWorkflow";

const props = withDefaults(
  defineProps<{
    field: ListDataValue;
    modelValue: string;
    options?: { label: string; value: string }[];
    token?: string;
    viewId?: number;
    objectId?: string;
    ownerId?: string;
    isAdded?: boolean;
    lookupDisabled?: boolean;
    readonlyValue?: string;
  }>(),
  {
    isAdded: false,
    lookupDisabled: false,
    objectId: "",
    ownerId: "",
    options: () => [],
    readonlyValue: "",
    token: "",
    viewId: 0
  }
);

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

const lookupError = ref("");
const lookupOptions = ref<InputQueryItem[]>([]);
const lookupPending = ref(false);
const lookupTerm = ref("");

const value = computed({
  get: () => props.modelValue,
  set: (next) => emit("update:modelValue", next)
});

async function searchLookup() {
  lookupPending.value = true;
  lookupError.value = "";
  try {
    const response = await postApi<InputQueryResult>(
      "/api/v1/data/inputquery",
      buildInputQueryRequest({
        token: props.token,
        viewId: props.viewId,
        viewItemId: fieldKey(props.field),
        text: lookupTerm.value,
        objID: props.objectId,
        ownerId: props.ownerId,
        isAdded: props.isAdded
      })
    );
    lookupOptions.value = legacyInputQueryItems(response.data);
    if (response.code !== 0) {
      lookupError.value = response.message || "Lookup failed.";
    }
  } catch (error) {
    lookupError.value = error instanceof Error ? error.message : "Lookup failed.";
  } finally {
    lookupPending.value = false;
  }
}

function selectLookup(item: InputQueryItem) {
  const id = inputQueryItemId(item);
  emit("update:modelValue", id);
  lookupTerm.value = inputQueryItemText(item) || id;
  lookupOptions.value = [];
}

function updateValue(event: Event) {
  if (event.target instanceof HTMLInputElement || event.target instanceof HTMLTextAreaElement) {
    value.value = event.target instanceof HTMLInputElement && event.target.type === "checkbox"
      ? (event.target.checked ? "true" : "false")
      : event.target.value;
  }
}
</script>

<template>
  <input v-if="isReadonlyField(field)" :value="readonlyValue || modelValue" disabled />
  <select v-else-if="isEnumField(field)" v-model="value">
    <option v-for="option in options" :key="option.value" :value="option.value">
      {{ option.label }}
    </option>
  </select>
  <div v-else-if="isLookupField(field)" class="metadata-lookup">
    <div class="metadata-lookup-row">
      <input v-model="lookupTerm" :placeholder="readonlyValue || modelValue" />
      <button type="button" :disabled="lookupDisabled || lookupPending || !lookupTerm.trim()" @click="searchLookup">
        Search
      </button>
    </div>
    <div v-if="lookupOptions.length" class="metadata-lookup-options">
      <button
        v-for="option in lookupOptions"
        :key="inputQueryItemId(option) || inputQueryItemText(option)"
        type="button"
        @click="selectLookup(option)"
      >
        {{ inputQueryItemText(option) || inputQueryItemId(option) }}
      </button>
    </div>
    <small v-if="lookupError" class="metadata-lookup-error">{{ lookupError }}</small>
    <small v-if="modelValue">{{ modelValue }}</small>
  </div>
  <textarea v-else-if="isMultilineField(field)" :value="value" rows="4" @input="updateValue"></textarea>
  <input
    v-else
    :checked="fieldInputChecked(field, value)"
    :type="fieldInputType(field)"
    :value="fieldInputValue(field, value)"
    @input="updateValue"
  />
</template>
