package aether;

import java.util.Map;
import java.util.TreeMap;
import java.util.HashMap;

/**
 * Simulated shared memory (the "Aether") that can be partitioned among multiple programs.
 * Provides deterministic allocation, deallocation, and safe read/write operations.
 */
public class AetherFramework {
    private final AetherMemory memory;
    private int nextProgramId = 1;

    public AetherFramework(int totalMemoryBytes) {
        if (totalMemoryBytes <= 0) {
            throw new IllegalArgumentException("Memory size must be positive");
        }
        this.memory = new AetherMemory(totalMemoryBytes);
    }

    /** Creates a new logical program that can allocate blocks inside the Aether. */
    public Program createProgram() {
        return new Program(nextProgramId++, memory);
    }

    /** Core simulated memory manager. */
    private static final class AetherMemory {
        private final byte[] memory;
        /** Free blocks keyed by start address, value = size. */
        private final TreeMap<Integer, Integer> freeBlocks = new TreeMap<>();
        /** Allocated blocks keyed by unique block id. */
        private final Map<Integer, MemoryBlock> allocated = new HashMap<>();
        private int nextBlockId = 1;

        AetherMemory(int size) {
            this.memory = new byte[size];
            freeBlocks.put(0, size); // whole memory is initially free
        }

        /** Allocate a block for a given program. */
        synchronized MemoryBlock allocate(int programId, int size) {
            if (size <= 0) {
                throw new IllegalArgumentException("Allocation size must be positive");
            }
            // Find first free block that fits (first-fit)
            for (Map.Entry<Integer, Integer> entry : freeBlocks.entrySet()) {
                int start = entry.getKey();
                int freeSize = entry.getValue();
                if (freeSize >= size) {
                    // Allocate from this block
                    int remaining = freeSize - size;
                    freeBlocks.remove(start);
                    if (remaining > 0) {
                        freeBlocks.put(start + size, remaining);
                    }
                    int blockId = nextBlockId++;
                    MemoryBlock block = new MemoryBlock(blockId, programId, start, size);
                    allocated.put(blockId, block);
                    return block;
                }
            }
            throw new IllegalArgumentException("Insufficient memory to allocate " + size + " bytes");
        }

        /** Release a previously allocated block. */
        synchronized void free(MemoryBlock block) {
            if (block == null) {
                throw new IllegalArgumentException("Block cannot be null");
            }
            MemoryBlock stored = allocated.remove(block.getId());
            if (stored == null) {
                throw new IllegalArgumentException("Block already freed or unknown");
            }
            // Coalesce with neighboring free blocks
            int start = block.getStart();
            int size = block.getSize();

            // Check lower neighbor
            Map.Entry<Integer, Integer> lower = freeBlocks.lowerEntry(start);
            if (lower != null && lower.getKey() + lower.getValue() == start) {
                start = lower.getKey();
                size += lower.getValue();
                freeBlocks.remove(lower.getKey());
            }

            // Check higher neighbor
            Map.Entry<Integer, Integer> higher = freeBlocks.ceilingEntry(start + size);
            if (higher != null && start + size == higher.getKey()) {
                size += higher.getValue();
                freeBlocks.remove(higher.getKey());
            }

            freeBlocks.put(start, size);
        }

        /** Write data into a block at a given offset. */
        synchronized void write(MemoryBlock block, int offset, byte[] data) {
            validateAccess(block, offset, data.length);
            System.arraycopy(data, 0, memory, block.getStart() + offset, data.length);
        }

        /** Read data from a block at a given offset. */
        synchronized byte[] read(MemoryBlock block, int offset, int length) {
            validateAccess(block, offset, length);
            byte[] out = new byte[length];
            System.arraycopy(memory, block.getStart() + offset, out, 0, length);
            return out;
        }

        private void validateAccess(MemoryBlock block, int offset, int length) {
            if (block == null) {
                throw new IllegalArgumentException("Block cannot be null");
            }
            if (offset < 0 || length < 0 || offset + length > block.getSize()) {
                throw new IndexOutOfBoundsException("Access out of block bounds");
            }
        }
    }

    /** Represents a logical program that interacts with the Aether. */
    public static final class Program {
        private final int id;
        private final AetherMemory memory;

        Program(int id, AetherMemory memory) {
            this.id = id;
            this.memory = memory;
        }

        public int getId() {
            return id;
        }

        public MemoryBlock allocate(int size) {
            return memory.allocate(id, size);
        }

        public void free(MemoryBlock block) {
            if (block.getProgramId() != id) {
                throw new IllegalArgumentException("Program cannot free block owned by another program");
            }
            memory.free(block);
        }

        public void write(MemoryBlock block, int offset, byte[] data) {
            if (block.getProgramId() != id) {
                throw new IllegalArgumentException("Program cannot write to block owned by another program");
            }
            memory.write(block, offset, data);
        }

        public byte[] read(MemoryBlock block, int offset, int length) {
            if (block.getProgramId() != id) {
                throw new IllegalArgumentException("Program cannot read from block owned by another program");
            }
            return memory.read(block, offset, length);
        }
    }

    /** Immutable handle to an allocated memory region. */
    public static final class MemoryBlock {
        private final int id;
        private final int programId;
        private final int start;
        private final int size;

        MemoryBlock(int id, int programId, int start, int size) {
            this.id = id;
            this.programId = programId;
            this.start = start;
            this.size = size;
        }

        public int getId() {
            return id;
        }

        public int getProgramId() {
            return programId;
        }

        public int getStart() {
            return start;
        }

        public int getSize() {
            return size;
        }
    }
}
