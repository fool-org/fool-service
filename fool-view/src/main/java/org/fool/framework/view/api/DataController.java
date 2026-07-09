package org.fool.framework.view.api;


import org.fool.framework.dto.CommonResponse;
import org.fool.framework.dto.CommonException;
import org.fool.framework.dao.PageNavigator;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.service.ModelDataService;
import org.fool.framework.view.adapter.ViewDataAdapter;
import org.fool.framework.view.common.ErrorCode;
import org.fool.framework.view.dto.GetEnumRequest;
import org.fool.framework.view.dto.GetEnumResult;
import org.fool.framework.view.dto.InputQueryRequest;
import org.fool.framework.view.dto.InputQueryResult;
import org.fool.framework.view.dto.LegacyInitNewRequest;
import org.fool.framework.view.dto.LegacyQueryDataRequest;
import org.fool.framework.view.dto.LegacyQueryDataDetailRequest;
import org.fool.framework.view.dto.LegacyRunOperationRequest;
import org.fool.framework.view.dto.LegacyRunOperationResult;
import org.fool.framework.view.dto.LegacySaveNewObjRequest;
import org.fool.framework.view.dto.ListViewResult;
import org.fool.framework.view.dto.QueryDataDetailResult;
import org.fool.framework.view.dto.QueryDataRequest;
import org.fool.framework.view.dto.SaveObjRequest;
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
                        requireViewId(request.getViewId()),
                        request.getFilter(),
                        request.getPageInfo(),
                        request.getKeyword()));
    }

    @PostMapping({"/querydata", "/querylist"})
    @ResponseBody
    public CommonResponse<ListViewResult> queryData(@RequestBody LegacyQueryDataRequest request) {
        PageNavigator pageInfo = new PageNavigator();
        pageInfo.setPageSize(request.getPageSize() == null ? 0 : request.getPageSize());
        pageInfo.setPageIndex(request.getPageIndex() == null ? 0 : request.getPageIndex());
        String viewId = request.getViewId() == null ? null : request.getViewId().toString();
        return new CommonResponse<>(dataQueryService.queryLegacyViewData(
                viewId,
                pageInfo,
                request.getQueryFilter(),
                request.getKeyword()));
    }

    @PostMapping("/querydatadetail")
    @ResponseBody
    public CommonResponse<QueryDataDetailResult> queryDataDetail(@RequestBody LegacyQueryDataDetailRequest request) {
        String viewId = request.getViewId() == null ? null : request.getViewId().toString();
        String objId = request.getObjId() == null ? null : request.getObjId().toString();
        return new CommonResponse<>(dataQueryService.queryLegacyViewDataDetail(
                viewId, objId, request.getIdExp(), request.getToken()));
    }

    @PostMapping("/initnew")
    @ResponseBody
    public CommonResponse<QueryDataDetailResult> initNew(@RequestBody LegacyInitNewRequest request) {
        String viewId = request.getViewId() == null ? null : request.getViewId().toString();
        return new CommonResponse<>(dataQueryService.initLegacyNewObject(viewId, request.getParentObjId()));
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

    @PostMapping("/inputquery")
    @ResponseBody
    public CommonResponse<InputQueryResult> inputQuery(@RequestBody InputQueryRequest request) {
        return new CommonResponse<>(dataQueryService.inputQuery(request));
    }

    @PostMapping({"/saveobj", "/save"})
    @ResponseBody
    public CommonResponse<Void> saveObj(@RequestBody SaveObjRequest request) {
        dataQueryService.saveLegacyObject(request);
        return new CommonResponse<>((Void) null);
    }

    @PostMapping({"/savenewobj", "/new"})
    @ResponseBody
    public CommonResponse<Void> saveNewObj(@RequestBody LegacySaveNewObjRequest request) {
        dataQueryService.saveLegacyNewObject(request);
        return new CommonResponse<>((Void) null);
    }

    @PostMapping({"/runoperation", "/exoperation"})
    @ResponseBody
    public CommonResponse<LegacyRunOperationResult> runOperation(@RequestBody LegacyRunOperationRequest request) {
        return new CommonResponse<>(dataQueryService.runLegacyOperation(request));
    }

    private static String requireViewId(Long viewId) {
        if (viewId == null) {
            throw new CommonException(ErrorCode.VIEW_NOT_FOUND, "ViewId is required");
        }
        return viewId.toString();
    }

}
