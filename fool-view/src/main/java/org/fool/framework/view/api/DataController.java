package org.fool.framework.view.api;


import org.fool.framework.dto.CommonResponse;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.service.ModelDataService;
import org.fool.framework.view.adapter.ViewDataAdapter;
import org.fool.framework.view.dto.GetEnumRequest;
import org.fool.framework.view.dto.GetEnumResult;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.dto.QueryDataRequest;
import org.fool.framework.view.service.DataQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/api/v1/data")
public class DataController {


    @Autowired
    private DataQueryService dataQueryService;

    @Autowired
    private ViewDataAdapter viewDataAdapter;

    @Autowired
    private ModelDataService modelDataService;

    @PostMapping("/query-list")
    @ResponseBody
    public CommonResponse<ListViewResult> queryViewDataList(@RequestBody QueryDataRequest request) {
        return new CommonResponse<ListViewResult>(
                dataQueryService.queryViewDataList(
                        request.getViewName(),
                        request.getFilter(),
                        request.getPageInfo(),
                        request.getKeyword()));
    }

    @PostMapping("/getenums")
    @ResponseBody
    public CommonResponse<GetEnumResult> getEnums(@RequestBody GetEnumRequest request) {
        GetEnumResult result = new GetEnumResult();
        Model model = modelDataService.getModel(request.getModelId());
        if (model != null && model.getEnumValues() != null) {
            result.setEnumValues(model.getEnumValues().stream()
                    .filter(Objects::nonNull)
                    .map(GetEnumResult.Value::from)
                    .toList());
        }
        return new CommonResponse<>(result);
    }

    public void queryViewDetailData() {

    }
}
