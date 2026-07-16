import { ref } from "vue";
import type { GetEnumResult, ListDataValue } from "./api";
import { postApi } from "./api";
import { buildGetEnumRequest } from "./payload";
import type { WorkflowActionRunner } from "./useViewDataWorkflow";
import type { SelectOption } from "./viewShell";
import {
  fieldModelId,
  isEnumField,
  legacyEnumName,
  legacyEnumValue,
  legacyEnumValues
} from "./viewWorkflow";

export function useFieldEnums(runAction: WorkflowActionRunner) {
  const enumOptions = ref<Record<string, SelectOption[]>>({});

  async function loadFieldEnums(fields: ListDataValue[]) {
    for (const field of fields.filter(isEnumField)) {
      const modelId = String(fieldModelId(field));
      if (enumOptions.value[modelId]) {
        continue;
      }
      const response = await runAction(
        "field-enums",
        () => postApi<GetEnumResult>("/api/v1/data/getenums", buildGetEnumRequest({ modelId })),
        { silentTransport: true }
      );
      if (response) {
        enumOptions.value = {
          ...enumOptions.value,
          [modelId]: legacyEnumValues(response.data).map((item) => ({
            label: legacyEnumName(item) || legacyEnumValue(item),
            value: legacyEnumValue(item)
          }))
        };
      }
    }
  }

  return {
    enumOptions,
    loadFieldEnums
  };
}
