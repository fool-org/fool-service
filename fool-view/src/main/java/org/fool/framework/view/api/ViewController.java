package org.fool.framework.view.api;

import io.swagger.annotations.ApiOperation;
import org.fool.framework.dto.CommonException;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.view.adapter.ViewAdapter;
import org.fool.framework.view.common.ErrorCode;
import org.fool.framework.view.dto.ListViewInfo;
import org.fool.framework.view.dto.ReadItemViewInfo;
import org.fool.framework.view.dto.ViewDataRequest;
import org.fool.framework.view.service.ViewDataService;
import org.fool.framework.view.service.ReadAuthorizationEnforcer;
import org.fool.framework.view.model.View;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/view")
public class ViewController {


    @Autowired
    private ViewDataService viewDataService;
    @Autowired
    private ViewAdapter viewAdapter;
    @Autowired
    private ReadAuthorizationEnforcer authorizationEnforcer;

    @ResponseBody
    @PostMapping("/get-view")
    @ApiOperation("得到视图的定义")
    public CommonResponse<ListViewInfo> getViewData(@RequestBody ViewDataRequest request) {
        String viewId = requireViewId(request.getViewId());
        return new CommonResponse<>(viewAdapter.getViewInfo(authorizedView(viewId)));
    }

    @ResponseBody
    @PostMapping("/getlistview")
    @ApiOperation("得到旧版视图定义")
    public CommonResponse<ListViewInfo> getListView(@RequestBody ViewDataRequest request) {
        String viewId = requireViewId(request.getViewId());
        return new CommonResponse<>(viewAdapter.getViewInfo(authorizedView(viewId)));
    }

    @ResponseBody
    @PostMapping("/getreaditemview")
    @ApiOperation("得到旧版只读详情视图")
    public CommonResponse<ReadItemViewInfo> getReadItemView(@RequestBody ViewDataRequest request) {
        String viewId = requireViewId(request.getViewId());
        return new CommonResponse<>(viewAdapter.getReadItemView(
                authorizedView(viewId),
                childViewId -> authorizedView(childViewId.toString())));
    }

    private View authorizedView(String viewId) {
        var policy = authorizationEnforcer.requireView("view.read", viewId);
        return authorizationEnforcer.constrainView(viewDataService.getViewData(viewId), policy);
    }

    private static String requireViewId(Long viewId) {
        if (viewId == null) {
            throw new CommonException(ErrorCode.VIEW_NOT_FOUND, "ViewId is required");
        }
        return viewId.toString();
    }
}
