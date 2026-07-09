package org.fool.framework.view.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SaveObjRequestTest {
    @Test
    public void requestReadsLegacyPascalSaveObjectPayload() throws Exception {
        SaveObjRequest request = new ObjectMapper().readValue(
                "{"
                        + "\"Token\":\"token-1\","
                        + "\"SaveObj\":{"
                        + "\"Id\":\"1001\","
                        + "\"ViewID\":\"100\","
                        + "\"ParentId\":\"5001\","
                        + "\"Model\":\"Order\","
                        + "\"Propertyies\":[{\"Key\":\"symbol\",\"Value\":\"BTC-USDT\"}],"
                        + "\"Itemproperties\":[{\"Key\":\"items\","
                        + "\"Items\":[{\"ItemId\":\"2001\",\"IsExist\":true,\"Propertyies\":[{\"Key\":\"itemName\",\"Value\":\"Updated\"}]}],"
                        + "\"AddedItems\":[{\"ItemId\":\"2002\",\"IsExist\":true}],"
                        + "\"DelteItems\":[{\"ItemId\":\"2003\",\"IsExist\":true}]"
                        + "}]}}",
                SaveObjRequest.class);

        assertEquals("token-1", request.getToken());
        assertEquals("1001", request.getSaveObj().getId());
        assertEquals("100", request.getSaveObj().getViewID());
        assertEquals("5001", request.getSaveObj().getParentId());
        assertEquals("Order", request.getSaveObj().getModel());
        assertEquals("symbol", request.getSaveObj().getPropertyies().get(0).getKey());
        assertEquals("BTC-USDT", request.getSaveObj().getPropertyies().get(0).getValue());
        SaveObjRequest.ItemProperty items = request.getSaveObj().getItemproperties().get(0);
        assertEquals("items", items.getKey());
        assertEquals("2001", items.getItems().get(0).getItemId());
        assertTrue(items.getItems().get(0).isExist());
        assertEquals("itemName", items.getItems().get(0).getPropertyies().get(0).getKey());
        assertEquals("2002", items.getAddedItems().get(0).getItemId());
        assertEquals("2003", items.getDelteItems().get(0).getItemId());
    }

    @Test
    public void requestReadsLegacyWebSaveObjectPayload() throws Exception {
        SaveObjRequest request = new ObjectMapper().readValue(
                "{"
                        + "\"obj\":{"
                        + "\"Id\":\"1001\","
                        + "\"ViewID\":\"100\","
                        + "\"Propertyies\":[{\"Key\":\"symbol\",\"Value\":\"BTC-USDT\"}]"
                        + "}}",
                SaveObjRequest.class);

        assertEquals("1001", request.getSaveObj().getId());
        assertEquals("100", request.getSaveObj().getViewID());
        assertEquals("symbol", request.getSaveObj().getPropertyies().get(0).getKey());
    }

    @Test
    public void itemReadsLegacyIsExistField() throws Exception {
        SaveObjRequest.Item item = new ObjectMapper().readValue(
                "{\"isExist\":true,\"itemId\":\"2003\"}",
                SaveObjRequest.Item.class);

        assertTrue(item.isExist());
    }
}
