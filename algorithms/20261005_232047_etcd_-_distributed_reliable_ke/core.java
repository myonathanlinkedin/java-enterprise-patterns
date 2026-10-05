import java.util.concurrent.ConcurrentHashMap;

public class Etcd {
    private static ConcurrentHashMap<String, Object> etcdMap = new ConcurrentHashMap<>();

    public static void put(String key, Object value) {
        etcdMap.put(key, value);
    }

    public static Object get(String key) {
        return etcdMap.get(key);
    }

    public static boolean containsKey(String key) {
        return etcdMap.containsKey(key);
    }

    public static void remove(String key) {
        etcdMap.remove(key);
    }
}
