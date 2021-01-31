package org.fool.framework.view.api;


import org.fool.framework.view.dto.QueryDataRequest;
import org.fool.framework.view.service.DataQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/data")
public class DataController {


    @Autowired
    private DataQueryService dataQueryService;

    @PostMapping("/query-list")
    @ResponseBody
    public void queryViewDataList(@RequestBody QueryDataRequest request) {

        dataQueryService.queryViewDataList(request.getViewName(), request.getFilter(), request.getPageInfo());
    }

    public void queryViewDetailData() {

    }
}
