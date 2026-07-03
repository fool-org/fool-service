package org.fool.framework.view.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SaveObjRequestTest {
    @Test
    public void itemReadsLegacyIsExistField() throws Exception {
        SaveObjRequest.Item item = new ObjectMapper().readValue(
                "{\"isExist\":true,\"itemId\":\"2003\"}",
                SaveObjRequest.Item.class);

        assertTrue(item.isExist());
    }
}
