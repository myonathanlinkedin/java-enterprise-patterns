import java.util.HashMap;
import java.util.Map;

public class PNcounter {
    private Map<String, Long> counterMap;

    public PNcounter() {
        counterMap = new HashMap<>();
    }

    public void increment(String key) {
        counterMap.put(key, counterMap.getOrDefault(key, 0L) + 1L);
    }

    public Long get(String key) {
        return counterMap.get(key);
    }

    public void merge(PNcounter other) {
        counterMap.putAll(other.counterMap);
    }
}
