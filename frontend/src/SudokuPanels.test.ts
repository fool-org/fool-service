import { describe, expect, it } from "vitest";
import appSource from "./App.vue?raw";
import sudokuPanelsSource from "./SudokuPanels.vue?raw";
import sudokuWorkflowSource from "./useSudokuPanels.ts?raw";
import viewListPanelSource from "./ViewListPanel.vue?raw";

describe("SudokuPanels legacy interactions", () => {
  it("keeps manual and timed refresh active during requests", () => {
    expect(sudokuPanelsSource).not.toContain("disabled");
    expect(viewListPanelSource).not.toContain("disabled: boolean");
    expect(viewListPanelSource).not.toMatch(/<SudokuPanels[^>]*:disabled=/);
    expect(sudokuWorkflowSource).not.toContain("pendingAction");
    expect(sudokuWorkflowSource).toContain("void refreshPanel(panel)");
    expect(appSource).not.toContain(':disabled="Boolean(pendingAction)"');
  });

  it("keeps success-only panel transport failures silent", () => {
    expect(sudokuWorkflowSource).toContain("const silentTransport: WorkflowActionOptions = { silentTransport: true }");
    expect(sudokuWorkflowSource).toContain('loadViewDataById(panelViewId, "sudoku-panel", 5, silentTransport)');
    expect(sudokuWorkflowSource).toContain("loadViewById(panelViewId, label, silentTransport)");
    expect(sudokuWorkflowSource).toContain("silentTransport\n    );");
  });
});
