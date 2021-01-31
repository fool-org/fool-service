package org.fool.framework.view.api;

import io.swagger.annotations.ApiOperation;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.view.adapter.ViewAdapter;
import org.fool.framework.view.dto.ListViewInfo;
import org.fool.framework.view.dto.ViewDataRequest;
import org.fool.framework.view.service.ViewDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/view")
public class ViewController {


    @Autowired
    private ViewDataService viewDataService;
    @Autowired
    private ViewAdapter viewAdapter;

    @ResponseBody
    @RequestMapping("/get-vew")
    @ApiOperation("得到视图的定义")
    public CommonResponse<ListViewInfo> getViewData(@RequestBody ViewDataRequest request) {
        return new CommonResponse<>(viewAdapter.getViewInfo(viewDataService.getViewData(request.getViewName(), request.getToken())));
    }
}
