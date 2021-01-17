package org.fool.framework.view.api;


import org.fool.framework.view.dto.QueryDataRequest;
import org.fool.framework.view.service.DataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/data")
public class DataController {


    @Autowired
    private DataService dataService;

    @PostMapping("/query-list")
    @ResponseBody
    public void queryViewDataList(@RequestBody QueryDataRequest request) {

        dataService.queryViewDataList(request.getViewName(), request.getFilter(), request.getPageInfo());
    }

    public void queryViewDetailData() {

    }
}
