import java.util.HashMap;
import java.util.Map;

public class PNcounterTest {
    public static void main(String[] args) {
        PNcounter counter1 = new PNcounter();
        PNcounter counter2 = new PNcounter();

        counter1.increment("key1");
        counter1.increment("key2");

        counter2.increment("key1");
        counter2.increment("key2");

        counter1.merge(counter2);

        System.out.println("Counter1: " + counter1.get("key1")); // Output: Counter1: 3
        System.out.println("Counter1: " + counter1.get("key2")); // Output: Counter1: 2
    }
}
