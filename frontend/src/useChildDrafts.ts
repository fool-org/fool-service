import { ref } from "vue";
import type { ListDataValue, QueryDataDetailDataItem, QueryDataDetailItemGroup } from "./api";
import {
  buildGroupItemDrafts,
  buildItemDrafts,
  draftFieldValue,
  itemKey,
  withDraftFieldValue
} from "./viewWorkflow";

export function useChildDrafts() {
  const childDrafts = ref<Record<string, Record<string, string>>>({});

  function childDraftValue(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem, field: ListDataValue) {
    return draftFieldValue(childDrafts.value, itemKey(group, item), field);
  }

  function setChildDraftValue(
    group: QueryDataDetailItemGroup,
    item: QueryDataDetailDataItem,
    field: ListDataValue,
    value: string
  ) {
    const key = itemKey(group, item);
    childDrafts.value = withDraftFieldValue(
      childDrafts.value,
      key,
      buildGroupItemDrafts(group, item),
      field,
      value
    );
  }

  function syncChildDrafts(groups: QueryDataDetailItemGroup[]) {
    childDrafts.value = buildItemDrafts(groups);
  }

  return {
    childDrafts,
    childDraftValue,
    setChildDraftValue,
    syncChildDrafts
  };
}
