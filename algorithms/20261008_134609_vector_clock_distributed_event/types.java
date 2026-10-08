package vector_clock;

import java.util.HashMap;

public class VectorClock {
    private HashMap<String, Integer> clock;

    public VectorClock() {
        this.clock = new HashMap<>();
    }

    public void update(String entity, int value) {
        clock.put(entity, value);
    }

    public int getValue(String entity) {
        return clock.get(entity);
    }
}
