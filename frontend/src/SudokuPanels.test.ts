import { ref } from "vue";
import { describe, expect, it, vi } from "vitest";
import appSource from "./App.vue?raw";
import sudokuPanelsSource from "./SudokuPanels.vue?raw";
import { useSudokuPanels } from "./useSudokuPanels";
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

  it("loads Group metadata without querying rows for the Group View", async () => {
    const loadViewById = vi.fn(async () => ({
      code: 0,
      message: "",
      data: { ViewId: 104, Items: [] }
    }));
    const loadViewDataById = vi.fn();
    const workflow = useSudokuPanels({
      enabled: ref(true),
      loadViewById,
      loadViewDataById,
      panels: ref([{ ListViewId: 104, ViewFile: "./includes/Group" }]),
      runAction: vi.fn(),
      token: ref("token")
    });

    await workflow.loadPanels();

    expect(loadViewById).toHaveBeenCalledWith(104, "sudoku-group", { silentTransport: true });
    expect(loadViewDataById).not.toHaveBeenCalled();
    expect(workflow.panelData.value[104]).toEqual({
      view: { ViewId: 104, Items: [] },
      data: null
    });
  });
});
