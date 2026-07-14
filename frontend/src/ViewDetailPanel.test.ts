import { describe, expect, it } from "vitest";
import viewDetailPanelSource from "./ViewDetailPanel.vue?raw";

describe("ViewDetailPanel legacy interactions", () => {
  it("keeps candidate-dialog commands active during requests", () => {
    const pickerSource = viewDetailPanelSource.slice(
      viewDetailPanelSource.indexOf('class="detail-picker-content"'),
      viewDetailPanelSource.indexOf('class="table-wrap detail-items-table"')
    );

    expect(viewDetailPanelSource).not.toContain(':closable="!pending"');
    expect(viewDetailPanelSource).not.toContain(':dismissable-mask="!pending"');
    expect(viewDetailPanelSource).toContain("dismissable-mask");
    expect(pickerSource).not.toContain(':disabled="pending"');
    expect(pickerSource.match(/:disabled="false"/g)).toHaveLength(2);
  });

  it("keeps legacy child collection commands active during requests", () => {
    const collectionSource = viewDetailPanelSource.slice(
      viewDetailPanelSource.indexOf('class="detail-collection-toolbar legacy-button-group"'),
      viewDetailPanelSource.indexOf("</Tabs>")
    );

    expect(collectionSource).toContain('label="增加" icon="pi pi-plus"');
    expect(collectionSource).toContain('@click="toggleDetailItem(group, item)"');
    expect(collectionSource).toContain('label="删除" icon="pi pi-trash"');
    expect(collectionSource).not.toContain(':disabled="pending"');
  });
});
