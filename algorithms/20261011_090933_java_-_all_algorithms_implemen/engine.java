import java.util.HashSet;
import java.util.Set;

/**
 * Engine class providing auxiliary, pure‑functional utilities for {@link DisjointSet}.
 * All methods are deterministic and side‑effect free.
 */
public final class DisjointSetEngine {

    private DisjointSetEngine() {
        // Prevent instantiation
    }

    /**
     * Counts the number of distinct connected components currently represented
     * by the supplied {@link DisjointSet}.
     *
     * @param ds a fully initialised DisjointSet instance
     * @return number of unique roots
     * @throws NullPointerException if {@code ds} is {@code null}
     */
    public static int countComponents(DisjointSet ds) {
        if (ds == null) {
            throw new NullPointerException("DisjointSet reference cannot be null");
        }
        Set<Integer> roots = new HashSet<>();
        for (int i = 0; i < ds.size(); i++) {
            roots.add(ds.find(i));
        }
        return roots.size();
    }

    /**
     * Returns a boolean array where {@code result[i]} is {@code true}
     * iff element {@code i} belongs to the same component as {@code representative}.
     *
     * @param ds               the DisjointSet instance
     * @param representative  an element whose component we want to query
     * @return boolean array of membership flags
     * @throws NullPointerException      if {@code ds} is {@code null}
     * @throws IndexOutOfBoundsException if {@code representative} is invalid
     */
    public static boolean[] componentMask(DisjointSet ds, int representative) {
        if (ds == null) {
            throw new NullPointerException("DisjointSet reference cannot be null");
        }
        int root = ds.find(representative);
        boolean[] mask = new boolean[ds.size()];
        for (int i = 0; i < ds.size(); i++) {
            mask[i] = ds.find(i) == root;
        }
        return mask;
    }
}
