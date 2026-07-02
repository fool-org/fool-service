package org.fool.framework.common.data.graphic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class GraphicTest {
    @Test
    public void tracksEdgesAndTopNodeLikeLegacyGraphic() {
        Graphic<String> graphic = new Graphic<>();

        graphic.addEdge("module", "model");
        graphic.addEdge("module", "model");
        graphic.addEdge("model", "view");

        assertEquals("module", graphic.getTopNode());
        assertEquals(3, graphic.getNodes().size());
        assertEquals(1, graphic.get("model").getPointIn().size());

        graphic.remove("module");

        assertEquals("model", graphic.getTopNode());
        assertNull(graphic.get("module"));
        assertEquals(0, graphic.get("model").getPointIn().size());
    }

    @Test
    public void returnsSelfLoopNodeWhenNoPlainTopExists() {
        Graphic<String> graphic = new Graphic<>();

        graphic.addEdge("module", "module");

        assertEquals("module", graphic.getTopNode());
    }

    @Test(expected = java.util.NoSuchElementException.class)
    public void removeMissingNodeFailsLikeLegacyFirstLookup() {
        new Graphic<String>().remove("missing");
    }
}
