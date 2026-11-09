package net.bdew.ae2stuff.items.visualiser;

import io.netty.buffer.Unpooled;
import net.bdew.ae2stuff.items.visualiser.VisualisationData.VLink;
import net.bdew.ae2stuff.items.visualiser.VisualisationData.VNode;
import net.minecraft.network.PacketBuffer;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisualisationDataTest {
    // Vanilla rejects custom payloads from the server above this size
    private static final int MAX_PAYLOAD = 1048576;

    private static VisualisationData roundTrip(VisualisationData data) {
        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        try {
            data.write(buf);
            VisualisationData result = VisualisationData.read(buf);
            assertEquals(0, buf.readableBytes(), "read must consume exactly what write produced");
            return result;
        } finally {
            buf.release();
        }
    }

    @Test
    void emptyRoundTrip() {
        VisualisationData result = roundTrip(new VisualisationData());
        assertTrue(result.nodes.isEmpty());
        assertTrue(result.links.isEmpty());
    }

    @Test
    void roundTripKeepsNodesAndLinks() {
        VNode a = new VNode(-30000000, 0, 29999999, VisualisationData.NODE_DENSE);
        VNode b = new VNode(1, 255, -1, VisualisationData.NODE_MISSING);
        VNode c = new VNode(5, -64, 5, VisualisationData.NODE_DENSE | VisualisationData.NODE_MISSING);
        VNode d = new VNode(7, 8, 9, 0);
        List<VNode> nodes = Arrays.asList(a, b, c, d);
        List<VLink> links = Arrays.asList(
                new VLink(a, b, 0, 0),
                new VLink(a, c, 127, VisualisationData.LINK_DENSE),
                new VLink(a, d, 128, VisualisationData.LINK_COMPRESSED),
                new VLink(c, b, 2048, VisualisationData.LINK_DENSE | VisualisationData.LINK_COMPRESSED));

        VisualisationData result = roundTrip(new VisualisationData(nodes, links));

        assertEquals(nodes, result.nodes);
        assertEquals(links.size(), result.links.size());
        for (int i = 0; i < links.size(); i++) {
            VLink expected = links.get(i);
            VLink actual = result.links.get(i);
            assertEquals(expected.node1, actual.node1);
            assertEquals(expected.node2, actual.node2);
            assertEquals(expected.channels, actual.channels, "channels of link " + i);
            assertEquals(expected.flags, actual.flags, "flags of link " + i);
        }
        assertTrue(result.nodes.get(2).has(VisualisationData.NODE_MISSING));
        assertFalse(result.nodes.get(3).has(VisualisationData.NODE_DENSE));
        assertTrue(result.links.get(1).has(VisualisationData.LINK_DENSE));
    }

    @Test
    void sharedNodesStayShared() {
        VNode hub = new VNode(0, 64, 0, VisualisationData.NODE_DENSE);
        List<VNode> nodes = new ArrayList<>();
        List<VLink> links = new ArrayList<>();
        nodes.add(hub);
        for (int i = 1; i <= 5; i++) {
            VNode leaf = new VNode(i, 64, 0, 0);
            nodes.add(leaf);
            links.add(new VLink(hub, leaf, i, 0));
        }

        VisualisationData result = roundTrip(new VisualisationData(nodes, links));

        assertEquals(nodes.size(), result.nodes.size());
        VNode decodedHub = result.nodes.get(0);
        for (int i = 0; i < links.size(); i++) {
            VLink link = result.links.get(i);
            assertSame(decodedHub, link.node1, "all links must point to the one decoded hub node");
            assertSame(result.nodes.get(i + 1), link.node2);
        }
    }

    @Test
    void maximumSnapshotFitsInOnePacket() {
        List<VNode> nodes = new ArrayList<>(VisualisationData.MAX_NODES);
        for (int i = 0; i < VisualisationData.MAX_NODES; i++)
            nodes.add(new VNode(-30000000 + i, 255, 30000000 - i, VisualisationData.NODE_DENSE | VisualisationData.NODE_MISSING));
        List<VLink> links = new ArrayList<>(VisualisationData.MAX_LINKS);
        for (int i = 0; i < VisualisationData.MAX_LINKS; i++) {
            // Integer.MAX_VALUE is the longest possible var int, an upper bound for any real channel count
            links.add(new VLink(nodes.get(i % nodes.size()), nodes.get((i + 1) % nodes.size()), Integer.MAX_VALUE,
                    VisualisationData.LINK_DENSE | VisualisationData.LINK_COMPRESSED));
        }

        PacketBuffer buf = new PacketBuffer(Unpooled.buffer());
        try {
            new VisualisationData(nodes, links).write(buf);
            int size = buf.readableBytes();
            assertTrue(size < MAX_PAYLOAD, "snapshot is " + size + " bytes");

            VisualisationData result = VisualisationData.read(buf);
            assertEquals(VisualisationData.MAX_NODES, result.nodes.size());
            assertEquals(VisualisationData.MAX_LINKS, result.links.size());
            assertEquals(Integer.MAX_VALUE, result.links.get(VisualisationData.MAX_LINKS - 1).channels);
        } finally {
            buf.release();
        }
    }
}
