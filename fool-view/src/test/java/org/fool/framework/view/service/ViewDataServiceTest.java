package org.fool.framework.view.service;

import org.fool.framework.common.PropertyType;
import org.fool.framework.dao.DaoService;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.model.Property;
import org.fool.framework.view.model.View;
import org.fool.framework.view.model.ViewItem;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ViewDataServiceTest {
    @Test
    public void getViewDataAttachesLegacyModelPropertyMetadataToItems() {
        DaoService daoService = mock(DaoService.class);
        ViewDataService service = new ViewDataService();
        ReflectionTestUtils.setField(service, "daoService", daoService);

        ViewItem item = new ViewItem();
        item.setModelProperty("customer");
        View view = new View();
        view.setViewModel("Order");
        view.setListItems(List.of(item));
        Property property = new Property();
        property.setName("customer");
        property.setPropertyType(PropertyType.BusinessObject);
        Model model = new Model();
        model.setName("Order");
        model.setProperties(List.of(property));

        when(daoService.getOneDetailByKey(View.class, "OrderList")).thenReturn(view);
        when(daoService.getOneDetailByKey(Model.class, "Order")).thenReturn(model);

        View result = service.getViewData("OrderList", "");

        assertSame(property, itemProperty(result.getListItems().get(0)));
    }

    private static Property itemProperty(ViewItem item) {
        try {
            return (Property) item.getClass().getMethod("getProperty").invoke(item);
        } catch (ReflectiveOperationException e) {
            fail("ViewItem should expose legacy property metadata");
            return null;
        }
    }
}
