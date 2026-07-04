package org.fool.framework.view.api;

import io.swagger.annotations.ApiOperation;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.view.adapter.ViewAdapter;
import org.fool.framework.view.dto.ListViewInfo;
import org.fool.framework.view.dto.ReadItemViewInfo;
import org.fool.framework.view.dto.ViewDataRequest;
import org.fool.framework.view.service.ViewDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/view")
public class ViewController {


    @Autowired
    private ViewDataService viewDataService;
    @Autowired
    private ViewAdapter viewAdapter;

    @ResponseBody
    @PostMapping("/get-view")
    @ApiOperation("得到视图的定义")
    public CommonResponse<ListViewInfo> getViewData(@RequestBody ViewDataRequest request) {
        String viewId = request.getViewId() == null ? request.getViewName() : request.getViewId().toString();
        return new CommonResponse<>(viewAdapter.getViewInfo(viewDataService.getViewData(viewId, request.getToken())));
    }

    @ResponseBody
    @PostMapping("/getlistview")
    @ApiOperation("得到旧版视图定义")
    public CommonResponse<ListViewInfo> getListView(@RequestBody ViewDataRequest request) {
        String viewId = request.getViewId() == null ? null : request.getViewId().toString();
        return new CommonResponse<>(viewAdapter.getViewInfo(viewDataService.getViewData(viewId, request.getToken())));
    }

    @ResponseBody
    @PostMapping("/getreaditemview")
    @ApiOperation("得到旧版只读详情视图")
    public CommonResponse<ReadItemViewInfo> getReadItemView(@RequestBody ViewDataRequest request) {
        String viewId = request.getViewId() == null ? null : request.getViewId().toString();
        String token = request.getToken();
        return new CommonResponse<>(viewAdapter.getReadItemView(
                viewDataService.getViewData(viewId, token),
                childViewId -> viewDataService.getViewData(childViewId.toString(), token)));
    }
}
