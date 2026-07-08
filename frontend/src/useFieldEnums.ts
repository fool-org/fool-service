import { ref, type Ref } from "vue";
import type { CommonResponse, GetEnumResult, ListDataValue } from "./api";
import { postApi } from "./api";
import { buildGetEnumRequest } from "./payload";
import type { SelectOption } from "./viewShell";
import {
  fieldModelId,
  isEnumField,
  legacyEnumName,
  legacyEnumValue,
  legacyEnumValues
} from "./viewWorkflow";

export type FieldEnumActionRunner = <T>(
  label: string,
  action: () => Promise<CommonResponse<T>>
) => Promise<CommonResponse<T> | null>;

export function useFieldEnums(token: Ref<string>, runAction: FieldEnumActionRunner) {
  const enumOptions = ref<Record<string, SelectOption[]>>({});

  async function loadFieldEnums(fields: ListDataValue[]) {
    for (const field of fields.filter(isEnumField)) {
      const modelId = String(fieldModelId(field));
      if (enumOptions.value[modelId]) {
        continue;
      }
      const response = await runAction("field-enums", () =>
        postApi<GetEnumResult>("/api/v1/data/getenums", buildGetEnumRequest({ token: token.value, modelId }))
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
