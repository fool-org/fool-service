package org.fool.framework.model.service;


import lombok.extern.slf4j.Slf4j;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.query.IQueryFilter;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Slf4j
public class ModelDataService {

    public Model getModel(String modelId) {
        return null;
    }

    public IDynamicData getOneData(String modelId, String dataId) {

        return null;
    }

    public List<IDynamicData> getDataList(String modelId, IQueryFilter queryAndArgs, List<Property> properties) {
        return null;
    }

    public Boolean saveData(IDynamicData data) {
        return true;
    }

    public Boolean saveDataList(List<IDynamicData> dataList) {
        return true;
    }

    public Boolean deleteData(IDynamicData data) {
        return true;
    }

    public Boolean createData(IDynamicData data) {
        return true;
    }

    public IDynamicData initData(Model model) {
        return null;
    }

}
