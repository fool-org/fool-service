import { computed, ref } from "vue";
import type { QueryDataDetailDataItem, QueryDataDetailItemGroup, SaveItemProperty } from "./api";
import {
  groupItems,
  groupKey,
  itemDataId,
  mergeItemPropertyChange,
  removeAddedItemPropertyChange,
  withGroupItems
} from "./viewWorkflow";

export function usePendingChildChanges() {
  const itemProperties = ref<SaveItemProperty[]>([]);
  const addedDetailItems = ref<Record<string, QueryDataDetailDataItem[]>>({});
  const deletedItemKeys = computed(() => new Set(
    itemProperties.value.flatMap((property) =>
      (property.delteItems || []).map((item) => `${property.key}:${item.itemId || ""}`)
    )
  ));

  function renderGroups(groups: QueryDataDetailItemGroup[]) {
    return groups.map((group) => {
      const key = groupKey(group);
      const storedItems = groupItems(group);
      const items = [...storedItems, ...(addedDetailItems.value[key] || [])];
      const visibleItems = items.filter((item) => !deletedItemKeys.value.has(`${key}:${itemDataId(item)}`));
      const unchanged = visibleItems.length === storedItems.length
        && visibleItems.every((item, index) => item === storedItems[index]);
      return unchanged ? group : withGroupItems(group, visibleItems);
    });
  }

  function stage(change: SaveItemProperty) {
    itemProperties.value = mergeItemPropertyChange(itemProperties.value, change);
  }

  function add(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem, change: SaveItemProperty) {
    const key = groupKey(group);
    const itemId = itemDataId(item);
    stage(change);
    addedDetailItems.value = {
      ...addedDetailItems.value,
      [key]: [...(addedDetailItems.value[key] || []).filter((candidate) => itemDataId(candidate) !== itemId), item]
    };
  }

  function isAdded(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) {
    return (addedDetailItems.value[groupKey(group)] || [])
      .some((candidate) => itemDataId(candidate) === itemDataId(item));
  }

  function replaceAdded(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) {
    const key = groupKey(group);
    addedDetailItems.value = {
      ...addedDetailItems.value,
      [key]: (addedDetailItems.value[key] || [])
        .map((candidate) => itemDataId(candidate) === itemDataId(item) ? item : candidate)
    };
  }

  function removeAdded(group: QueryDataDetailItemGroup, item: QueryDataDetailDataItem) {
    const key = groupKey(group);
    const itemId = itemDataId(item);
    if (!isAdded(group, item)) return false;
    addedDetailItems.value = {
      ...addedDetailItems.value,
      [key]: addedDetailItems.value[key].filter((candidate) => itemDataId(candidate) !== itemId)
    };
    itemProperties.value = removeAddedItemPropertyChange(itemProperties.value, key, itemId);
    return true;
  }

  function clear() {
    itemProperties.value = [];
    addedDetailItems.value = {};
  }

  return { add, clear, isAdded, itemProperties, removeAdded, renderGroups, replaceAdded, stage };
}
