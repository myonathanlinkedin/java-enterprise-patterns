package hydro;

import java.util.HashMap;
import java.util.Map;

public class HydroEngine {
    private static Map<HydroType, HydroNode> nodes = new HashMap<>();

    public static HydroNode getNode(HydroType nodeType) {
        HydroNode node = nodes.get(nodeType);
        if (node == null) {
            nodes.put(nodeType, new HydroNode(nodeType));
        }
        return node;
    }
}
