import java.util.Arrays;
import java.util.Scanner;

public class EtcdTest {
    public static void main(String[] args) {
        Etcd etcd = new Etcd();

        System.out.println("Initializing Etcd...");
        etcd.put("key1", "value1");
        etcd.put("key2", "value2");
        etcd.put("key3", "value3");

        System.out.println("Testing Etcd...");
        System.out.println(etcd.get("key1")); // Output: value1
        System.out.println(etcd.get("key2")); // Output: value2
        if (etcd.containsKey("key3")) {
            System.out.println("Key exists");
        } else {
            System.out.println("Key does not exist");
        }

        System.out.println("Removing key2...");
        etcd.remove("key2");
        System.out.println(etcd.get("key2")); // Output: null
    }
}

// === FILE: main.java ===
