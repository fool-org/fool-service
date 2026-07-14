import { describe, expect, it } from "vitest";
import legacyPaginationSource from "./LegacyPagination.vue?raw";
import listDataTableSource from "./ListDataTable.vue?raw";
import viewListPanelSource from "./ViewListPanel.vue?raw";

describe("ViewListPanel legacy interactions", () => {
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
    expect(viewListPanelSource).toContain('<SudokuPanels v-if="sudokuView" :disabled="disabled"');
  });
});
