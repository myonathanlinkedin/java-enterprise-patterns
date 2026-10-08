package vector_clock;

import java.util.HashMap;

public class VectorClockDemo {
    public static void main(String[] args) {
        VectorClockEngine engine = new VectorClockEngine();

        engine.update("entity1", 1);
        engine.update("entity2", 1);
        engine.update("entity3", 1);

        System.out.println("Vector Clock for entities:");
        System.out.println("entity1: " + engine.getValue("entity1"));
        System.out.println("entity2: " + engine.getValue("entity2"));
        System.out.println("entity3: " + engine.getValue("entity3"));
    }
}

// Output:
// Vector Clock for entities:
// entity1: 1
// entity2: 1
// entity3: 1
