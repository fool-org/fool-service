package org.fool.framework.application;

import org.fool.framework.agent.service.AgentReadPreviewProvider;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.view.dto.ListDataItem;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.service.DataQueryService;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AgentReadPreviewAdapter implements AgentReadPreviewProvider {
    private final DataQueryService dataQueryService;

    public AgentReadPreviewAdapter(DataQueryService dataQueryService) {
        this.dataQueryService = dataQueryService;
    }

    @Override
    public ReadPreview preview(String viewId, int pageSize) {
        try {
            PageNavigator page = new PageNavigator();
            page.setPageIndex(1);
            page.setPageSize(Math.max(1, Math.min(pageSize, 10)));
            ListViewResult result = dataQueryService.queryReportViewData(viewId, page, null, null);
            List<Map<String, Object>> rows = result == null || result.getItems() == null
                    ? List.of()
                    : result.getItems().stream().map(this::row).toList();
            return new ReadPreview(
                    "passed",
                    null,
                    result == null || result.getTotalItem() == null ? rows.size() : result.getTotalItem(),
                    result == null ? List.of() : result.getCols(),
                    rows);
        } catch (RuntimeException ex) {
            return ReadPreview.unavailable("Authorized runtime preview failed.");
        }
    }

    private Map<String, Object> row(ListDataItem item) {
        Map<String, Object> row = new LinkedHashMap<>();
        if (item != null && item.getValues() != null) {
            row.putAll(item.getValues());
        }
        return row;
    }
}
