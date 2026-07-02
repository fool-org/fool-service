package org.fool.framework.common.data;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class DbContextInterfacesTest {
    @Test
    public void dbContextKeepsLegacyCrudSurface() {
        RecordingContext context = new RecordingContext();
        Order order = new Order("1001");

        context.create(order);
        context.save(order);
        context.delete(order);

        assertEquals(List.of(order), context.get());
        assertSame(order, context.getDetail("1001"));
        assertEquals(List.of("create:1001", "save:1001", "delete:1001"), context.calls);
        assertTrue(IDBContext.class.isAssignableFrom(RecordingContext.class));
    }

    @Test
    public void multiDbContextKeepsLegacyCollectionBound() {
        LineCollection lines = new LineCollection();
        RecordingMultiContext context = new RecordingMultiContext(lines);

        assertSame(lines, context.getDetail("lines"));
        assertTrue(IMultiDbContext.class.isAssignableFrom(RecordingMultiContext.class));
    }

    private record Order(String id) {
    }

    private static class RecordingContext implements IDBContext<Order> {
        private Order order;
        private final List<String> calls = new ArrayList<>();

        @Override
        public List<Order> get() {
            return List.of(order);
        }

        @Override
        public void save(Order object) {
            calls.add("save:" + object.id());
        }

        @Override
        public void delete(Order object) {
            calls.add("delete:" + object.id());
        }

        @Override
        public void create(Order object) {
            order = object;
            calls.add("create:" + object.id());
        }

        @Override
        public Order getDetail(Object key) {
            return order;
        }
    }

    private static class LineCollection extends ArrayList<String> {
    }

    private static class RecordingMultiContext implements IMultiDbContext<LineCollection> {
        private final LineCollection lines;

        private RecordingMultiContext(LineCollection lines) {
            this.lines = lines;
        }

        @Override
        public List<LineCollection> get() {
            return List.of(lines);
        }

        @Override
        public void save(LineCollection object) {
        }

        @Override
        public void delete(LineCollection object) {
        }

        @Override
        public void create(LineCollection object) {
        }

        @Override
        public LineCollection getDetail(Object key) {
            return lines;
        }
    }
}
