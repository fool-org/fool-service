package org.fool.framework.view.api;

import org.fool.framework.dto.CommonResponse;
import org.fool.framework.view.dto.SaveObjRequest;
import org.fool.framework.view.service.DataQueryService;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DataControllerSaveObjTest {
    @Test
    public void saveObjPassesLegacyRequestToService() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        DataController controller = new DataController();
        setField(controller, "dataQueryService", dataQueryService);
        SaveObjRequest request = new SaveObjRequest();
        SaveObjRequest.SaveObject saveObj = new SaveObjRequest.SaveObject();
        saveObj.setId("1001");
        saveObj.setViewID("100");
        request.setSaveObj(saveObj);

        CommonResponse<Void> response = controller.saveObj(request);

        verify(dataQueryService).saveLegacyObject(request);
        assertEquals(0, response.getCode());
        assertNull(response.getData());
    }

    @Test
    public void saveObjAlsoExposesLegacyWebSaveRoute() throws Exception {
        var mapping = DataController.class
                .getMethod("saveObj", SaveObjRequest.class)
                .getAnnotation(org.springframework.web.bind.annotation.PostMapping.class);

        assertTrue(List.of(mapping.value()).contains("/save"));
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
