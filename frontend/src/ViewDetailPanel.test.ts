import { describe, expect, it } from "vitest";
import viewDetailPanelSource from "./ViewDetailPanel.vue?raw";

describe("ViewDetailPanel legacy interactions", () => {
  it("uses the old plain-text candidate query input", () => {
    const pickerSource = viewDetailPanelSource.slice(
      viewDetailPanelSource.indexOf('class="candidate-query-toolbar"'),
      viewDetailPanelSource.indexOf('class="table-wrap detail-picker-results"')
    );

    expect(pickerSource).toContain('type="text"');
    expect(pickerSource).not.toContain('type="search"');
  });

  it("keeps candidate-dialog commands active during requests", () => {
    const pickerSource = viewDetailPanelSource.slice(
      viewDetailPanelSource.indexOf('class="detail-picker-content"'),
      viewDetailPanelSource.indexOf('class="table-wrap detail-items-table"')
    );

    expect(viewDetailPanelSource).not.toContain(':closable="!pending"');
    expect(viewDetailPanelSource).not.toContain(':dismissable-mask="!pending"');
    expect(viewDetailPanelSource).toContain("dismissable-mask");
    expect(pickerSource).not.toContain(':disabled="pending"');
    expect(pickerSource).not.toContain(':disabled=');
  });

  it("renders the old inert candidate confirmation command", () => {
    const dialogStart = viewDetailPanelSource.indexOf('class="detail-picker-dialog"');
    const footerStart = viewDetailPanelSource.indexOf("<template #footer>", dialogStart);
    const footerSource = viewDetailPanelSource.slice(footerStart, viewDetailPanelSource.indexOf("</template>", footerStart));

    expect(footerSource).toContain('label="取消"');
    expect(footerSource).toContain('label="确定"');
    expect(footerSource.match(/@click=/g)).toHaveLength(1);
  });

  it("leaves legacy detail collection tabs inactive until selected", () => {
    expect(viewDetailPanelSource).toContain('activeGroupKey.value = ""');
    expect(viewDetailPanelSource).toContain('if (!keys.includes(activeGroupKey.value)) activeGroupKey.value = ""');
    expect(viewDetailPanelSource).not.toContain("keys[0]");
  });

  it("renders read values as normal text instead of invented emphasis", () => {
    expect(viewDetailPanelSource).toContain('class="detail-field-label"');
    expect(viewDetailPanelSource).toContain('class="detail-field-value"');
    expect(viewDetailPanelSource).not.toContain('<strong>{{ fieldDisplayValue(item)');
  });

  it("restores item.jade's schema-only value placeholder", () => {
    expect(viewDetailPanelSource).toContain('schemaOnly ? "你好" : fieldDisplayValue(item) || "\\u00a0"');
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

  it("reuses the shared legacy error dialog", () => {
    expect(viewDetailPanelSource).toContain('<LegacyErrorDialog :message="errorMessage"');
    expect(viewDetailPanelSource).toContain('@dismiss="emit(\'dismissError\')"');
    expect(viewDetailPanelSource).not.toContain('header="发生错误"');
  });
});
