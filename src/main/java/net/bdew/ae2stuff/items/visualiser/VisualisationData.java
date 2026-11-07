package net.bdew.ae2stuff.items.visualiser;

import net.minecraft.network.PacketBuffer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Snapshot of a network that is sent to players holding a visualiser.
 */
public final class VisualisationData {
    public static final int NODE_DENSE = 1;
    public static final int NODE_MISSING = 1 << 1;

    public static final int LINK_DENSE = 1;
    public static final int LINK_COMPRESSED = 1 << 1;

    public final List<VNode> nodes;
    public final List<VLink> links;

    public VisualisationData() {
        this(Collections.emptyList(), Collections.emptyList());
    }

    public VisualisationData(List<VNode> nodes, List<VLink> links) {
        this.nodes = nodes;
        this.links = links;
    }

    public void write(PacketBuffer buf) {
        buf.writeInt(nodes.size());
        buf.writeInt(links.size());
        Map<VNode, Integer> nodeIndexes = new HashMap<>();
        for (int i = 0; i < nodes.size(); i++) {
            VNode node = nodes.get(i);
            nodeIndexes.put(node, i);
            buf.writeInt(node.x);
            buf.writeInt(node.y);
            buf.writeInt(node.z);
            buf.writeByte(node.flags);
        }
        for (VLink link : links) {
            buf.writeInt(nodeIndexes.get(link.node1));
            buf.writeInt(nodeIndexes.get(link.node2));
            buf.writeByte(link.channels);
            buf.writeByte(link.flags);
        }
    }

    public static VisualisationData read(PacketBuffer buf) {
        int nodeCount = buf.readInt();
        int linkCount = buf.readInt();
        List<VNode> nodes = new ArrayList<>(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            int x = buf.readInt();
            int y = buf.readInt();
            int z = buf.readInt();
            nodes.add(new VNode(x, y, z, buf.readByte()));
        }
        List<VLink> links = new ArrayList<>(linkCount);
        for (int i = 0; i < linkCount; i++) {
            VNode node1 = nodes.get(buf.readInt());
            VNode node2 = nodes.get(buf.readInt());
            byte channels = buf.readByte();
            links.add(new VLink(node1, node2, channels, buf.readByte()));
        }
        return new VisualisationData(nodes, links);
    }

    public static final class VNode {
        public final int x;
        public final int y;
        public final int z;
        public final int flags;

        public VNode(int x, int y, int z, int flags) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.flags = flags;
        }

        public boolean has(int flag) {
            return (flags & flag) != 0;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof VNode))
                return false;
            VNode that = (VNode) other;
            return x == that.x && y == that.y && z == that.z && flags == that.flags;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, y, z, flags);
        }
    }

    public static final class VLink {
        public final VNode node1;
        public final VNode node2;
        public final byte channels;
        public final int flags;

        public VLink(VNode node1, VNode node2, byte channels, int flags) {
            this.node1 = node1;
            this.node2 = node2;
            this.channels = channels;
            this.flags = flags;
        }

        public boolean has(int flag) {
            return (flags & flag) != 0;
        }
    }
}
