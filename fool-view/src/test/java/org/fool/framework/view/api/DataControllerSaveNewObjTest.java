package org.fool.framework.view.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.view.dto.LegacySaveNewObjRequest;
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

public class DataControllerSaveNewObjTest {
    @Test
    public void saveNewObjPassesLegacyRequestToService() throws Exception {
        DataQueryService dataQueryService = mock(DataQueryService.class);
        DataController controller = new DataController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "dataQueryService", dataQueryService);
        LegacySaveNewObjRequest request = new LegacySaveNewObjRequest();
        SaveObjRequest.SaveObject saveObj = new SaveObjRequest.SaveObject();
        saveObj.setViewID("100");
        request.setSaveObj(saveObj);

        CommonResponse<Void> response = controller.saveNewObj(request);

        verify(dataQueryService).saveLegacyNewObject(request);
        assertEquals(0, response.getCode());
        assertNull(response.getData());
    }

    @Test
    public void saveNewObjAcceptsLegacyWebNewPayloadAliases() throws Exception {
        LegacySaveNewObjRequest request = new ObjectMapper().readValue(
                "{"
                        + "\"obj\":{\"Id\":\"1002\",\"ViewID\":\"100\"},"
                        + "\"ownerviewid\":\"200\","
                        + "\"ownerid\":\"5001\","
                        + "\"prpid\":\"items\""
                        + "}",
                LegacySaveNewObjRequest.class);

        assertEquals("1002", request.getSaveObj().getId());
        assertEquals("100", request.getSaveObj().getViewID());
        assertEquals("200", request.getOwnerViewId());
        assertEquals("5001", request.getOwnerId());
        assertEquals("items", request.getProperty());
    }

    @Test
    public void saveNewObjAlsoExposesLegacyWebNewRoute() throws Exception {
        var mapping = DataController.class
                .getMethod("saveNewObj", LegacySaveNewObjRequest.class)
                .getAnnotation(org.springframework.web.bind.annotation.PostMapping.class);

        assertTrue(List.of(mapping.value()).contains("/new"));
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
