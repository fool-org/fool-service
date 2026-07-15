import { ref } from "vue";
import { describe, expect, it, vi } from "vitest";
import appSource from "./App.vue?raw";
import sudokuPanelsSource from "./SudokuPanels.vue?raw";
import { type SudokuPanelResult, useSudokuPanels } from "./useSudokuPanels";
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

  it("exposes the legacy updating text until a List query succeeds", async () => {
    let resolveLoad!: (result: SudokuPanelResult) => void;
    const loadViewDataById = vi.fn(() => new Promise<SudokuPanelResult>((resolve) => { resolveLoad = resolve; }));
    const workflow = useSudokuPanels({
      enabled: ref(true),
      loadViewById: vi.fn(),
      loadViewDataById,
      panels: ref([]),
      runAction: vi.fn(),
      token: ref("token")
    });
    const panel = { ListViewId: 100, ViewFile: "./includes/List" };

    const refresh = workflow.refreshPanel(panel);
    expect(workflow.panelUpdating.value[100]).toBe(true);
    resolveLoad({ view: { ViewId: 100 }, data: { AutoFreshTime: 0 } });
    await refresh;

    expect(workflow.panelUpdating.value[100]).toBe(false);
    expect(sudokuPanelsSource).toContain('return "更新中.."');
    expect(appSource).toContain(':panel-updating="sudokuPanelUpdating"');
    expect(viewListPanelSource).toContain(':panel-updating="panelUpdating"');
  });

  it("keeps the legacy updating text after a List transport failure", async () => {
    const workflow = useSudokuPanels({
      enabled: ref(true),
      loadViewById: vi.fn(),
      loadViewDataById: vi.fn(async () => ({ view: { ViewId: 100 }, data: null })),
      panels: ref([]),
      runAction: vi.fn(),
      token: ref("token")
    });

    await workflow.refreshPanel({ ListViewId: 100, ViewFile: "./includes/List" });

    expect(workflow.panelUpdating.value[100]).toBe(true);
  });
});
