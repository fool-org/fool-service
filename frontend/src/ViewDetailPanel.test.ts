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
});
