package hydro;

public class HydroNode {
    private HydroType type;

    public HydroNode(HydroType type) {
        this.type = type;
    }

    public HydroType getType() {
        return type;
    }

    public void sendMessage(HydroType destinationType, HydroType messageType) {
        HydroNode destinationNode = HydroEngine.getNode(destinationType);
        destinationNode.sendMessage(messageType);
    }
}
