import { describe, expect, it } from "vitest";
import legacyErrorDialogSource from "./LegacyErrorDialog.vue?raw";
import legacyPaginationSource from "./LegacyPagination.vue?raw";
import listDataTableSource from "./ListDataTable.vue?raw";
import viewListPanelSource from "./ViewListPanel.vue?raw";

describe("ViewListPanel legacy interactions", () => {
  it("uses the old plain-text query input", () => {
    expect(viewListPanelSource).toContain('class="list-query-input" type="text"');
    expect(viewListPanelSource).not.toContain('class="list-query-input" type="search"');
  });

  it("keeps row navigation and pagination active during requests", () => {
    const tableSource = viewListPanelSource.slice(
      viewListPanelSource.indexOf("<ListDataTable"),
      viewListPanelSource.indexOf("<LegacyChartPanel")
    );
    const paginationSource = viewListPanelSource.slice(
      viewListPanelSource.indexOf("<LegacyPagination"),
      viewListPanelSource.indexOf("</article>")
    );

    expect(tableSource).not.toContain(':disabled="disabled"');
    expect(paginationSource).not.toContain(':disabled="disabled"');
    expect(listDataTableSource).toContain("disabled?: boolean");
    expect(listDataTableSource).toContain("disabled: false");
    expect(legacyPaginationSource).not.toContain("disabled");
    expect(viewListPanelSource).not.toContain("disabled: boolean");
    expect(viewListPanelSource).not.toMatch(/<SudokuPanels[^>]*:disabled=/);
  });

  it("keeps unconfigured metadata columns readable through horizontal scrolling", () => {
    expect(listDataTableSource).toContain("columnMinimumWidth(column)");
    expect(listDataTableSource).toContain(':table-style="{ minWidth: `${tableMinimumWidth}px` }"');
    expect(listDataTableSource).toContain(":style=\"{ minWidth: '72px' }\"");
  });

  it("opens shared View errors in the legacy dialog", () => {
    expect(viewListPanelSource).toContain('<LegacyErrorDialog :message="errorMessage"');
    expect(viewListPanelSource).not.toContain('<Message v-if="errorMessage"');
    expect(legacyErrorDialogSource).toContain('header="发生错误"');
    expect(legacyErrorDialogSource).toContain('label="关闭" severity="secondary" outlined');
  });

  it("renders report and create commands only from effective action hints", () => {
    expect(viewListPanelSource).toContain('v-if="canPreviewReport"');
    expect(viewListPanelSource).toContain("v-for=\"operation in canCreate ? createItems : []\"");
  });
});
