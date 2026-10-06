package hydro;

import java.util.HashMap;
import java.util.Map;

public class HydroEngine {
    private static final Map<NodeType, HydroNode> nodes = new HashMap<>();

    public static void addNode(NodeType nodeType, HydroNode node) {
        nodes.put(nodeType, node);
    }

    public static HydroNode getNode(NodeType nodeType) {
        return nodes.get(nodeType);
    }

    public static void sendMessage(NodeType destinationNodeType, MessageType messageType) {
        HydroNode destinationNode = getNode(destinationNodeType);
        if (destinationNode != null) {
            HydroNode senderNode = HydroEngine.getNode(HydroType.SENDER);
            senderNode.sendMessage(destinationNode, messageType);
        }
    }
}
