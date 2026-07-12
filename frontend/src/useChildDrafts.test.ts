import { describe, expect, it } from "vitest";
import { useChildDrafts } from "./useChildDrafts";

describe("useChildDrafts", () => {
  it("keeps child draft reads stable before sync and writes through defaults", () => {
    const drafts = useChildDrafts();
    const group = {
      prpId: "items",
      properties: [
        { prpId: "itemId", fmtValue: "" },
        { prpId: "itemName", fmtValue: "" }
      ],
      items: [
        {
          dataId: "2001",
          values: [
            { prpId: "itemId", objId: "2001", fmtValue: "2001" },
            { prpId: "itemName", objId: "Old", fmtValue: "Old" }
          ]
        }
      ]
    };

    drafts.syncChildDrafts([group]);
    expect(drafts.childDraftValue(group, group.items[0], group.properties[1])).toBe("Old");

    drafts.setChildDraftValue(group, group.items[0], group.properties[1], "Updated");
    expect(drafts.childDraftValue(group, group.items[0], group.properties[1])).toBe("Updated");
  });

  it("builds existing child drafts from rendered group columns instead of data DTO fields", () => {
    const drafts = useChildDrafts();
    const group = {
      prpId: "items",
      properties: [
        { prpId: "itemId", editType: "ReadOnly" },
        { prpId: "itemName" }
      ],
      items: [
        {
          dataId: "2001",
          values: [
            { prpId: "itemId", objId: "2001", editType: "ReadOnly" },
            { prpId: "itemName", fmtValue: "Old item" },
            { prpId: "dtoOnly", fmtValue: "leak" }
          ]
        }
      ]
    };

    drafts.syncChildDrafts([group]);

    expect(drafts.childDrafts.value["items:2001"]).toEqual({
      itemId: "2001",
      itemName: "Old item"
    });

    const freshDrafts = useChildDrafts();
    freshDrafts.setChildDraftValue(group, group.items[0], group.properties[1], "Updated item");

    expect(freshDrafts.childDrafts.value["items:2001"]).toEqual({
      itemId: "2001",
      itemName: "Updated item"
    });
  });
});
