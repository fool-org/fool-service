package org.fool.framework.view.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fool.framework.dto.CommonResponse;
import org.fool.framework.model.model.EnumValue;
import org.fool.framework.model.model.Model;
import org.fool.framework.model.service.ModelDataService;
import org.fool.framework.view.dto.GetEnumRequest;
import org.fool.framework.view.dto.GetEnumResult;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DataControllerEnumTest {
    @Test
    public void getEnumRequestReadsLegacyModelIdFields() throws Exception {
        ObjectMapper mapper = new ObjectMapper();

        assertEquals("state", mapper.readValue("{\"ModelId\":\"state\"}", GetEnumRequest.class).getModelId());
        assertEquals("state", mapper.readValue("{\"ModelID\":\"state\"}", GetEnumRequest.class).getModelId());
        assertEquals("state", mapper.readValue("{\"modelid\":\"state\"}", GetEnumRequest.class).getModelId());
    }

    @Test
    public void getEnumResultWritesLegacyEnumValuesFields() throws Exception {
        GetEnumResult result = new GetEnumResult();
        result.setEnumValues(List.of(GetEnumResult.Value.from(enumValue("Open", "1"))));

        String json = new ObjectMapper().writeValueAsString(result);

        assertTrue(json.contains("\"enumValues\""));
        assertTrue(json.contains("\"EnumValues\""));
        assertTrue(json.contains("\"name\":\"Open\""));
        assertTrue(json.contains("\"Name\":\"Open\""));
        assertTrue(json.contains("\"value\":1"));
        assertTrue(json.contains("\"Value\":1"));
    }

    @Test
    public void getEnumsReturnsLegacyEnumValues() throws Exception {
        Model model = new Model();
        model.setEnumValues(List.of(enumValue("Open", "1"), enumValue("Closed", "2")));
        ModelDataService modelDataService = mock(ModelDataService.class);
        when(modelDataService.getModel("state")).thenReturn(model);

        DataController controller = new DataController();
        org.fool.framework.view.TestReadAuthorization.install(controller);
        setField(controller, "modelDataService", modelDataService);
        GetEnumRequest request = new GetEnumRequest();
        request.setModelId("state");

        CommonResponse<GetEnumResult> response = controller.getEnums(request);

        assertEquals(0, response.getCode());
        assertEquals(2, response.getData().getEnumValues().size());
        assertEquals("Open", response.getData().getEnumValues().get(0).getName());
        assertEquals(Integer.valueOf(1), response.getData().getEnumValues().get(0).getValue());
        assertEquals("Closed", response.getData().getEnumValues().get(1).getName());
        assertEquals(Integer.valueOf(2), response.getData().getEnumValues().get(1).getValue());
    }

    @Test
    public void getEnumsAlsoExposesLegacyWebGetEnumRoute() throws Exception {
        var mapping = DataController.class
                .getMethod("getEnums", GetEnumRequest.class)
                .getAnnotation(org.springframework.web.bind.annotation.PostMapping.class);

        assertTrue(List.of(mapping.value()).contains("/getenum"));
    }

    private static EnumValue enumValue(String name, String value) {
        EnumValue enumValue = new EnumValue();
        enumValue.setName(name);
        enumValue.setValue(value);
        return enumValue;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }
}
