import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        Actor alice = new Actor("Alice");
        Actor bob = new Actor("Bob");
        Actor charlie = new Actor("Charlie");

        System.out.println("Enter message to send to Bob:");
        String message = scanner.nextLine();

        bob.sendMessage(alice.getName(), message);
        charlie.sendMessage(alice.getName(), message);
    }
}
