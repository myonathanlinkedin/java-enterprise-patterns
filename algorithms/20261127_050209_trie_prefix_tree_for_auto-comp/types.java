import java.util.HashMap;
import java.util.Map;

public final class TrieNode {
    public final Map<Character, TrieNode> children = new HashMap<>();
    public boolean isWord = false;
    public int frequency = 0;
}
