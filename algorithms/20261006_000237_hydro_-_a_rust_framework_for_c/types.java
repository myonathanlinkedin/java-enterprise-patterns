package hydro;

public class HydroType {
    public enum NodeType {
        NODE,
        CLIENT,
        SERVER
    }

    public enum MessageType {
        INITIALIZATION,
        INFORMATION,
        CONTROL
    }
}
