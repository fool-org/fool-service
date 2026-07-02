package org.fool.framework.view.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.common.dynamic.IDynamicData;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.model.service.ModelDataService;
import org.fool.framework.view.adapter.ViewDataAdapter;
import org.fool.framework.view.dto.SaveObjRequest;
import org.fool.framework.view.model.View;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DataQueryServiceSaveObjTest {
    @Test
    public void saveLegacyObjectWritesSimplePropertiesToDynamicData() {
        DaoService daoService = mock(DaoService.class);
        ModelDataService modelDataService = mock(ModelDataService.class);
        DataQueryService service = new DataQueryService();
        ReflectionTestUtils.setField(service, "daoService", daoService);
        ReflectionTestUtils.setField(service, "modelDataService", modelDataService);
        ReflectionTestUtils.setField(service, "viewAdapter", mock(ViewDataAdapter.class));

        View view = new View();
        view.setViewModel("Order");
        Model order = model();
        when(daoService.getOneDetailByKey(View.class, "100")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(order);
        when(modelDataService.saveData(any(IDynamicData.class))).thenReturn(true);
        SaveObjRequest request = new SaveObjRequest();
        SaveObjRequest.SaveObject saveObj = new SaveObjRequest.SaveObject();
        saveObj.setId("1001");
        saveObj.setViewID("100");
        saveObj.setPropertyies(List.of(
                new SaveObjRequest.SaveKeypair("symbol", "ETH-USDT"),
                new SaveObjRequest.SaveKeypair("state", "OPEN")));
        request.setSaveObj(saveObj);

        service.saveLegacyObject(request);

        ArgumentCaptor<IDynamicData> dataCaptor = ArgumentCaptor.forClass(IDynamicData.class);
        verify(modelDataService).saveData(dataCaptor.capture());
        IDynamicData data = dataCaptor.getValue();
        assertEquals("1001", data.getId());
        assertEquals("1001", data.get("orderId"));
        assertEquals("ETH-USDT", data.get("symbol"));
        assertEquals("OPEN", data.get("state"));
    }

    private static Model model() {
        Model model = new Model();
        model.setName("Order");
        model.setTableName("market_order");
        Property orderId = property("orderId", "order_id", PropertyType.Long);
        model.setIdProperty(orderId);
        model.setProperties(List.of(
                orderId,
                property("symbol", "order_symbol", PropertyType.String),
                property("state", "order_state", PropertyType.String)));
        return model;
    }

    private static Property property(String name, String column, PropertyType type) {
        Property property = new Property();
        property.setName(name);
        property.setColumn(column);
        property.setPropertyType(type);
        property.setIsCollection(false);
        return property;
    }
}
