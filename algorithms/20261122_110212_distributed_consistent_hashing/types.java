package consistenthash;

import java.util.Objects;

/**
 * Represents a physical node in the consistent hashing ring.
 */
public final class Node {
    private final String identifier;

    public Node(String identifier) {
        this.identifier = Objects.requireNonNull(identifier, "identifier");
    }

    public String getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;
        return identifier.equals(node.identifier);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public String toString() {
        return "Node{" + "identifier='" + identifier + '\'' + '}';
    }
}
