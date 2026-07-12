import { describe, expect, it } from "vitest";
import { buildAddedDetailItem, buildAddedItemProperty, buildDeletedItemProperty, groupItems } from "./viewWorkflow";
import { usePendingChildChanges } from "./usePendingChildChanges";

const group = {
  prpId: "items",
  properties: [{ prpId: "itemId" }, { prpId: "itemName" }],
  items: [{ dataId: "2001", values: [] }]
};

describe("pending child changes", () => {
  it("renders local additions and persisted deletions even when the count is unchanged", () => {
    const pending = usePendingChildChanges();
    const added = buildAddedDetailItem(group, "2002", { itemId: "2002", itemName: "New" });
    pending.add(group, added, buildAddedItemProperty(group, "2002", { itemId: "2002", itemName: "New" }));
    pending.stage(buildDeletedItemProperty(group, group.items[0]));

    expect(groupItems(pending.renderGroups([group])[0]).map((item) => item.dataId)).toEqual(["2002"]);
  });

  it("discards an unsaved addition without leaving an AddedItems payload", () => {
    const pending = usePendingChildChanges();
    const added = buildAddedDetailItem(group, "2002", { itemId: "2002", itemName: "New" });
    pending.add(group, added, buildAddedItemProperty(group, "2002", { itemId: "2002", itemName: "New" }));

    expect(pending.removeAdded(group, added)).toBe(true);
    expect(pending.itemProperties.value).toEqual([]);
    expect(groupItems(pending.renderGroups([group])[0]).map((item) => item.dataId)).toEqual(["2001"]);
  });
});
