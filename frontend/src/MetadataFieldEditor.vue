<script setup lang="ts">
import { computed, ref, watch } from "vue";
import AutoComplete from "primevue/autocomplete";
import Checkbox from "primevue/checkbox";
import InputText from "primevue/inputtext";
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
    viewName?: string;
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
    viewId: 0,
    viewName: ""
  }
);

const emit = defineEmits<{
  "update:modelValue": [value: string];
}>();

type LookupChoice = {
  id: string;
  label: string;
  item: InputQueryItem;
};

const lookupError = ref("");
const lookupOptions = ref<InputQueryItem[]>([]);
const lookupPending = ref(false);
const lookupTerm = ref<string | LookupChoice>(props.readonlyValue || props.modelValue);

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

watch(() => props.field, () => {
  lookupTerm.value = props.readonlyValue || props.modelValue;
  lookupOptions.value = [];
  lookupError.value = "";
});

async function searchLookup(query: string) {
  lookupPending.value = true;
  lookupError.value = "";
  try {
    const response = await postApi<InputQueryResult>(
      "/api/v1/data/inputquery",
      buildInputQueryRequest({
        token: props.token,
        viewId: props.viewId,
        viewName: props.viewName,
        viewItemId: fieldKey(props.field),
        text: query,
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

function selectLookup(choice: LookupChoice) {
  const id = inputQueryItemId(choice.item);
  emit("update:modelValue", id);
  lookupTerm.value = choice.label;
  lookupOptions.value = [];
}

function updateLookupTerm(term: string | LookupChoice | null) {
  lookupTerm.value = term || "";
  if (term === "" || term === null) emit("update:modelValue", "");
}
</script>

<template>
  <InputText v-if="isReadonlyField(field)" :model-value="readonlyValue || modelValue" disabled fluid />
  <Select v-else-if="isEnumField(field)" v-model="value" :options="options" option-label="label" option-value="value" fluid />
  <div v-else-if="isLookupField(field)" class="metadata-lookup">
    <AutoComplete
      :model-value="lookupTerm"
      :suggestions="lookupChoices"
      option-label="label"
      data-key="id"
      :delay="300"
      :min-length="1"
      :loading="lookupPending"
      :disabled="lookupDisabled"
      force-selection
      fluid
      @complete="searchLookup($event.query)"
      @option-select="selectLookup($event.value)"
      @update:model-value="updateLookupTerm"
    >
      <template #option="{ option }">
        <strong>{{ option.label }}</strong>
        <small>{{ option.id }}</small>
      </template>
      <template #empty>未找到匹配的选项</template>
    </AutoComplete>
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
