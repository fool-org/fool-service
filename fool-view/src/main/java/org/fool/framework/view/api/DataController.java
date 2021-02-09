package org.fool.framework.view.api;


import org.fool.framework.dto.CommonResponse;
import org.fool.framework.view.adapter.ViewDataAdapter;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.dto.QueryDataRequest;
import org.fool.framework.view.service.DataQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/data")
public class DataController {


    @Autowired
    private DataQueryService dataQueryService;

    @Autowired
    private ViewDataAdapter viewDataAdapter;

    @PostMapping("/query-list")
    @ResponseBody
    public CommonResponse<ListViewResult> queryViewDataList(@RequestBody QueryDataRequest request) {
        return new CommonResponse<ListViewResult>(
                dataQueryService.queryViewDataList(request.getViewName(), request.getFilter(), request.getPageInfo()));
    }

    public void queryViewDetailData() {

    }
}
