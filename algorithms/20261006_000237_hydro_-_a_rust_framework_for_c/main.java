package hydro;

import java.util.Scanner;

public class HydroMain {
    public static void main(String[] args) {
        HydroEngine engine = new HydroEngine();

        HydroNode senderNode = new HydroNode(HydroType.SENDER);
        HydroNode receiverNode = new HydroNode(HydroType.RECEIVER);

        engine.addNode(HydroType.NODE, senderNode);
        engine.addNode(HydroType.NODE, receiverNode);

        engine.sendMessage(HydroType.RECEIVER, HydroType.MESSAGE_TYPE);

        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter message type: ");
        String messageType = scanner.nextLine();

        engine.sendMessage(HydroType.RECEIVER, HydroType.MESSAGE_TYPE);
    }
}
