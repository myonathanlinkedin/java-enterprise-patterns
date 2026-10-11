import java.util.Scanner;

public class Actor {
    private String name;
    private String mailbox;

    public Actor(String name) {
        this.name = name;
        this.mailbox = "";
    }

    public String getName() {
        return name;
    }

    public String getMailbox() {
        return mailbox;
    }

    public void sendMessage(String recipient, String message) {
        mailbox += sender() + ": " + message + "\n";
    }

    public String sender() {
        return name;
    }
}
