package org.fool.framework.common.data;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class IControllerTest {
    @Test
    public void exposesLegacyControllerCrudSurface() {
        RecordingController controller = new RecordingController();

        controller.create("created");
        controller.update("updated");
        controller.delete("deleted");

        assertEquals("1001", controller.get("1001"));
        assertArrayEquals(new Object[]{"1:20"}, controller.getList(1, 20));
        assertEquals(List.of("create:created", "update:updated", "delete:deleted"), controller.calls);
    }

    private static class RecordingController implements IController {
        private final List<String> calls = new ArrayList<>();

        @Override
        public Object get(Object id) {
            return id;
        }

        @Override
        public Object[] getList(int page, int count) {
            return new Object[]{page + ":" + count};
        }

        @Override
        public void create(Object object) {
            calls.add("create:" + object);
        }

        @Override
        public void update(Object object) {
            calls.add("update:" + object);
        }

        @Override
        public void delete(Object object) {
            calls.add("delete:" + object);
        }
    }
}
