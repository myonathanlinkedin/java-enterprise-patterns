package huffman;

import java.util.Objects;

/**
 * Immutable container for the result of a Huffman compression.
 * It holds the serialized tree, the encoded payload and the number of padding bits
 * added to the last byte of the payload.
 */
public final class CompressedData {
    public final byte[] tree;   // serialized Huffman tree (pre‑order, 1 = leaf, 0 = internal)
    public final byte[] data;   // encoded bits packed into bytes
    public final int padding;   // number of unused bits in the last byte of {@code data} (0‑7)

    public CompressedData(byte[] tree, byte[] data, int padding) {
        this.tree = Objects.requireNonNull(tree);
        this.data = Objects.requireNonNull(data);
        if (padding < 0 || padding > 7) {
            throw new IllegalArgumentException("Padding must be between 0 and 7 inclusive");
        }
        this.padding = padding;
    }
}
