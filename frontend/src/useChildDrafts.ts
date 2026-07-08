import { ref } from "vue";
import type { ListDataValue, QueryDataDetailDataItem, QueryDataDetailItemGroup } from "./api";
import {
  buildFieldDrafts,
  buildItemDrafts,
  detailItemValues,
  draftFieldValue,
  emptyGroupDraft,
  groupKey,
  itemKey,
  withDraftFieldValue
} from "./viewWorkflow";

export function useChildDrafts() {
  const childDrafts = ref<Record<string, Record<string, string>>>({});
  const newChildDrafts = ref<Record<string, Record<string, string>>>({});

  function newChildDraftValue(group: QueryDataDetailItemGroup, field: ListDataValue) {
    return draftFieldValue(newChildDrafts.value, groupKey(group), field);
  }

  function setNewChildDraftValue(group: QueryDataDetailItemGroup, field: ListDataValue, value: string) {
    const key = groupKey(group);
    newChildDrafts.value = withDraftFieldValue(newChildDrafts.value, key, emptyGroupDraft(group), field, value);
  }

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
      buildFieldDrafts(detailItemValues(item)),
      field,
      value
    );
  }

  function syncChildDrafts(groups: QueryDataDetailItemGroup[]) {
    childDrafts.value = buildItemDrafts(groups);
    newChildDrafts.value = groups.reduce<Record<string, Record<string, string>>>((drafts, group) => {
      const key = groupKey(group);
      drafts[key] = newChildDrafts.value[key] || emptyGroupDraft(group);
      return drafts;
    }, {});
  }

  return {
    childDrafts,
    newChildDrafts,
    childDraftValue,
    newChildDraftValue,
    setChildDraftValue,
    setNewChildDraftValue,
    syncChildDrafts
  };
}
