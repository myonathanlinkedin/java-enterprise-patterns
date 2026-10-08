package vector_clock;

import java.util.HashMap;

public class VectorClockEngine {
    private VectorClock vectorClock;

    public VectorClockEngine() {
        vectorClock = new VectorClock();
    }

    public void update(String entity, int value) {
        vectorClock.update(entity, value);
    }

    public int getValue(String entity) {
        return vectorClock.getValue(entity);
    }
}
