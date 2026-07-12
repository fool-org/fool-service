<script setup lang="ts">
import { computed, ref } from "vue";
import Button from "primevue/button";
import Checkbox from "primevue/checkbox";
import InputGroup from "primevue/inputgroup";
import InputText from "primevue/inputtext";
import Listbox from "primevue/listbox";
import Select from "primevue/select";
import Textarea from "primevue/textarea";
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
const checked = computed({
  get: () => fieldInputChecked(props.field, value.value),
  set: (next: boolean) => { value.value = next ? "true" : "false"; }
});
const lookupChoices = computed(() => lookupOptions.value.map((item) => ({
  id: inputQueryItemId(item) || inputQueryItemText(item),
  label: inputQueryItemText(item) || inputQueryItemId(item),
  item
})));

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
      lookupError.value = response.message || "查找失败。";
    }
  } catch (error) {
    lookupError.value = error instanceof Error ? error.message : "查找失败。";
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
</script>

<template>
  <InputText v-if="isReadonlyField(field)" :model-value="readonlyValue || modelValue" disabled fluid />
  <Select v-else-if="isEnumField(field)" v-model="value" :options="options" option-label="label" option-value="value" fluid />
  <div v-else-if="isLookupField(field)" class="metadata-lookup">
    <InputGroup>
      <InputText v-model="lookupTerm" :placeholder="readonlyValue || modelValue" fluid @keyup.enter="searchLookup" />
      <Button type="button" label="查找" icon="pi pi-search" :loading="lookupPending" :disabled="lookupDisabled || lookupPending || !lookupTerm.trim()" @click="searchLookup" />
    </InputGroup>
    <Listbox
      v-if="lookupChoices.length"
      :options="lookupChoices"
      option-label="label"
      data-key="id"
      class="metadata-lookup-options"
      @change="selectLookup($event.value.item)"
    />
    <small v-if="lookupError" class="metadata-lookup-error">{{ lookupError }}</small>
  </div>
  <Textarea v-else-if="isMultilineField(field)" v-model="value" rows="4" auto-resize fluid />
  <span v-else-if="fieldInputType(field) === 'checkbox'" class="checkbox-field">
    <Checkbox v-model="checked" binary />
    <span>{{ checked ? "是" : "否" }}</span>
  </span>
  <InputText
    v-else
    :type="fieldInputType(field)"
    :model-value="fieldInputValue(field, value)"
    fluid
    @update:model-value="value = String($event ?? '')"
  />
</template>
