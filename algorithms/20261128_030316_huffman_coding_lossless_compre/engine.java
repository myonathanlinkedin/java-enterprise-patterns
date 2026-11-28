package huffman;

import java.util.*;
import java.io.*;

/**
 * Core Huffman compression / decompression engine.
 * All methods are pure and deterministic.
 */
public final class HuffmanEngine {

    /** Private constructor – all members are static. */
    private HuffmanEngine() {}

    /** Node of the Huffman tree. */
    private static final class Node implements Comparable<Node> {
        final char ch;          // valid only if leaf
        final int freq;
        final Node left, right;

        Node(char ch, int freq) {
            this.ch = ch;
            this.freq = freq;
            this.left = null;
            this.right = null;
        }

        Node(Node left, Node right) {
            this.ch = '\0';
            this.freq = left.freq + right.freq;
            this.left = left;
            this.right = right;
        }

        boolean isLeaf() {
            return left == null && right == null;
        }

        @Override
        public int compareTo(Node o) {
            return Integer.compare(this.freq, o.freq);
        }
    }

    /** Helper to write bits into a byte array. */
    private static final class BitWriter {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();
        private int currentByte = 0;
        private int numBitsFilled = 0; // [0,7]

        void writeBit(int bit) {
            currentByte = (currentByte << 1) | (bit & 1);
            numBitsFilled++;
            if (numBitsFilled == 8) {
                out.write(currentByte);
                currentByte = 0;
                numBitsFilled = 0;
            }
        }

        void writeBits(String bits) {
            for (int i = 0; i < bits.length(); i++) {
                writeBit(bits.charAt(i) == '1' ? 1 : 0);
            }
        }

        /** Write a 16‑bit unsigned char (big‑endian). */
        void writeChar(char c) {
            writeBit((c >>> 15) & 1);
            writeBit((c >>> 14) & 1);
            writeBit((c >>> 13) & 1);
            writeBit((c >>> 12) & 1);
            writeBit((c >>> 11) & 1);
            writeBit((c >>> 10) & 1);
            writeBit((c >>> 9) & 1);
            writeBit((c >>> 8) & 1);
            writeBit((c >>> 7) & 1);
            writeBit((c >>> 6) & 1);
            writeBit((c >>> 5) & 1);
            writeBit((c >>> 4) & 1);
            writeBit((c >>> 3) & 1);
            writeBit((c >>> 2) & 1);
            writeBit((c >>> 1) & 1);
            writeBit(c & 1);
        }

        /** Flush remaining bits (if any) padding with zeros. */
        byte[] toByteArray() {
            if (numBitsFilled > 0) {
                currentByte <<= (8 - numBitsFilled);
                out.write(currentByte);
                currentByte = 0;
                numBitsFilled = 0;
            }
            return out.toByteArray();
        }

        /** Number of padding bits added to the last byte (0‑7). */
        int paddingBits() {
            return (numBitsFilled == 0) ? 0 : (8 - numBitsFilled);
        }
    }

    /** Helper to read bits from a byte array. */
    private static final class BitReader {
        private final byte[] data;
        private int bytePos = 0;
        private int bitPos = 0; // next bit to read in current byte (0‑7)

        BitReader(byte[] data) {
            this.data = data;
        }

        /** Returns -1 if no more bits are available. */
        int readBit() {
            if (bytePos >= data.length) {
                return -1;
            }
            int bit = (data[bytePos] >>> (7 - bitPos)) & 1;
            bitPos++;
            if (bitPos == 8) {
                bitPos = 0;
                bytePos++;
            }
            return bit;
        }

        /** Reads a 16‑bit char (big‑endian). Returns -1 if insufficient bits. */
        int readChar() {
            int value = 0;
            for (int i = 0; i < 16; i++) {
                int b = readBit();
                if (b == -1) {
                    return -1;
                }
                value = (value << 1) | b;
            }
            return value;
        }
    }

    /** Build Huffman tree from character frequencies. */
    private static Node buildTree(int[] freq) {
        PriorityQueue<Node> pq = new PriorityQueue<>();
        for (int i = 0; i < freq.length; i++) {
            if (freq[i] > 0) {
                pq.add(new Node((char) i, freq[i]));
            }
        }
        if (pq.isEmpty()) {
            return null; // empty input
        }
        while (pq.size() > 1) {
            Node a = pq.poll();
            Node b = pq.poll();
            pq.add(new Node(a, b));
        }
        return pq.poll();
    }

    /** Generate code map (character → bit string). */
    private static void buildCodeMap(Node node, String prefix, Map<Character, String> map) {
        if (node.isLeaf()) {
            // Special case: single symbol tree – assign "0" if prefix empty
            map.put(node.ch, prefix.isEmpty() ? "0" : prefix);
        } else {
            buildCodeMap(node.left, prefix + '0', map);
            buildCodeMap(node.right, prefix + '1', map);
        }
    }

    /** Serialize tree into bits (pre‑order). */
    private static void serializeTree(Node node, BitWriter bw) {
        if (node.isLeaf()) {
            bw.writeBit(1);
            bw.writeChar(node.ch);
        } else {
            bw.writeBit(0);
            serializeTree(node.left, bw);
            serializeTree(node.right, bw);
        }
    }

    /** Deserialize tree from bits. */
    private static Node deserializeTree(BitReader br) {
        int flag = br.readBit();
        if (flag == -1) {
            throw new IllegalStateException("Unexpected end of tree data");
        }
        if (flag == 1) {
            int ch = br.readChar();
            if (ch == -1) {
                throw new IllegalStateException("Unexpected end while reading leaf character");
            }
            return new Node((char) ch, 0);
        } else {
            Node left = deserializeTree(br);
            Node right = deserializeTree(br);
            return new Node(left, right);
        }
    }

    /** Public API – compress a UTF‑16 Java {@code String}. */
    public static CompressedData compress(String text) {
        Objects.requireNonNull(text);
        // 1. Frequency table
        int[] freq = new int[65536];
        for (int i = 0; i < text.length(); i++) {
            freq[text.charAt(i)]++;
        }

        // 2. Build Huffman tree
        Node root = buildTree(freq);
        if (root == null) {
            // Empty input – return empty structures
            return new CompressedData(new byte[0], new byte[0], 0);
        }

        // 3. Build code map
        Map<Character, String> codeMap = new HashMap<>();
        buildCodeMap(root, "", codeMap);

        // 4. Serialize tree
        BitWriter treeWriter = new BitWriter();
        serializeTree(root, treeWriter);
        byte[] treeBytes = treeWriter.toByteArray();

        // 5. Encode payload
        BitWriter dataWriter = new BitWriter();
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            String code = codeMap.get(ch);
            dataWriter.writeBits(code);
        }
        byte[] dataBytes = dataWriter.toByteArray();
        int padding = dataWriter.paddingBits();

        return new CompressedData(treeBytes, dataBytes, padding);
    }

    /** Public API – decompress a {@link CompressedData} back to the original {@code String}. */
    public static String decompress(CompressedData cd) {
        Objects.requireNonNull(cd);
        if (cd.tree.length == 0 && cd.data.length == 0) {
            return "";
        }

        // 1. Rebuild Huffman tree
        BitReader treeReader = new BitReader(cd.tree);
        Node root = deserializeTree(treeReader);
        if (root == null) {
            throw new IllegalStateException("Deserialized tree is null");
        }

        // 2. Decode payload
        StringBuilder sb = new StringBuilder();
        BitReader dataReader = new BitReader(cd.data);
        int totalBits = cd.data.length * 8 - cd.padding;
        int bitsRead = 0;

        if (root.isLeaf()) {
            // Special case: only one symbol – each bit corresponds to that symbol
            char onlyChar = root.ch;
            int repetitions = totalBits; // each bit (even if zero) represents one occurrence
            for (int i = 0; i < repetitions; i++) {
                sb.append(onlyChar);
            }
            return sb.toString();
        }

        while (bitsRead < totalBits) {
            Node cur = root;
            while (!cur.isLeaf()) {
                int bit = dataReader.readBit();
                if (bit == -1) {
                    throw new IllegalStateException("Ran out of bits while decoding");
                }
                bitsRead++;
                cur = (bit == 0) ? cur.left : cur.right;
            }
            sb.append(cur.ch);
        }
        return sb.toString();
    }
}
