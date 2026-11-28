package huffman;

public class Main {
    public static void main(String[] args) {
        // Basic sanity checks
        assert roundTrip("") : "Empty string failed";
        assert roundTrip("a") : "Single character failed";
        assert roundTrip("aaaaaa") : "Repeated single character failed";
        assert roundTrip("The quick brown fox jumps over the lazy dog.") : "Pangram failed";

        // Larger test with all BMP characters (limited to a subset for speed)
        StringBuilder sbAll = new StringBuilder();
        for (char c = 32; c < 127; c++) { // printable ASCII range
            sbAll.append(c);
        }
        String allAscii = sbAll.toString();
        assert roundTrip(allAscii) : "All ASCII printable characters failed";

        // Verify compression ratio on a typical sentence
        String sample = "this is an example for huffman encoding. huffman coding is optimal for symbol frequencies.";
        CompressedData cd = HuffmanEngine.compress(sample);
        // Rough sanity: compressed payload should not be larger than raw UTF‑16 (2 bytes per char)
        int rawSize = sample.length() * 2;
        int compressedSize = cd.tree.length + cd.data.length;
        assert compressedSize <= rawSize : "Compression did not reduce size as expected";

        System.out.println("All tests passed.");
    }

    /** Helper that compresses and then decompresses, checking equality. */
    private static boolean roundTrip(String original) {
        CompressedData cd = HuffmanEngine.compress(original);
        String recovered = HuffmanEngine.decompress(cd);
        return original.equals(recovered);
    }
}
