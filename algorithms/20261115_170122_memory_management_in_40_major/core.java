import java.util.*;

/**
 * Core data structures and algorithms for a memory management survey.
 * Implements a deterministic, in-memory simulation of a paged virtual memory system
 * with a Least Recently Used (LRU) page replacement policy.
 *
 * Time Complexity:
 * - Page Fault Handling: O(1) average (using LinkedHashMap for LRU)
 * - Simulation: O(N) where N is the number of memory references
 * Space Complexity:
 * - O(F) where F is the number of physical frames
 */
public class core {

    public static class MemoryStats {
        public int totalReferences;
        public int pageFaults;
        public int hits;
        public int evictions;

        public double getHitRate() {
            if (totalReferences == 0) return 0.0;
            return (double) hits / totalReferences;
        }

        public double getFaultRate() {
            if (totalReferences == 0) return 0.0;
            return (double) pageFaults / totalReferences;
        }

        @Override
        public String toString() {
            return String.format("Stats{refs=%d, faults=%d, hits=%d, evictions=%d, hitRate=%.4f}",
                    totalReferences, pageFaults, hits, evictions, getHitRate());
        }
    }

    /**
     * Simulates a paged memory system with LRU replacement.
     * Uses LinkedHashMap to maintain access order for O(1) LRU operations.
     */
    public static class LRUPagedMemory {
        private final int numFrames;
        private final Map<Integer, Integer> pageTable; // Page Number -> Last Access Time
        private int currentTime;
        private final MemoryStats stats;

        public LRUPagedMemory(int numFrames) {
            if (numFrames <= 0) throw new IllegalArgumentException("Frames must be positive");
            this.numFrames = numFrames;
            this.pageTable = new LinkedHashMap<>();
            this.currentTime = 0;
            this.stats = new MemoryStats();
        }

        /**
         * Processes a memory reference to a specific page.
         * @param page The virtual page number being accessed
         * @return true if page fault occurred, false if hit
         */
        public boolean accessPage(int page) {
            stats.totalReferences++;
            currentTime++;

            if (pageTable.containsKey(page)) {
                // Page Hit: Update access time to current time
                pageTable.put(page, currentTime);
                stats.hits++;
                return false;
            } else {
                // Page Fault
                stats.pageFaults++;

                if (pageTable.size() < numFrames) {
                    // Empty frame available
                    pageTable.put(page, currentTime);
                } else {
                    // Evict LRU page (first key in LinkedHashMap)
                    Iterator<Integer> it = pageTable.keySet().iterator();
                    int lruPage = it.next();
                    it.remove();
                    stats.evictions++;
                    pageTable.put(page, currentTime);
                }
                return true;
            }
        }

        public MemoryStats getStats() {
            return stats;
        }

        public int getNumFrames() {
            return numFrames;
        }

        public int getCurrentResidentPages() {
            return pageTable.size();
        }
    }

    /**
     * Simulates a simple first-fit memory allocator for fixed-size blocks.
     * Models contiguous memory allocation with fragmentation tracking.
     */
    public static class FirstFitAllocator {
        private final int totalMemory;
        private final int blockSize;
        private final List<Integer> freeBlocks; // Indices of free blocks
        private final boolean[] allocated;

        public FirstFitAllocator(int totalMemory, int blockSize) {
            if (totalMemory <= 0 || blockSize <= 0) throw new IllegalArgumentException("Invalid dimensions");
            if (totalMemory % blockSize != 0) throw new IllegalArgumentException("Memory must be multiple of block size");
            this.totalMemory = totalMemory;
            this.blockSize = blockSize;
            this.allocated = new boolean[totalMemory / blockSize];
            this.freeBlocks = new ArrayList<>();
            for (int i = 0; i < allocated.length; i++) {
                freeBlocks.add(i);
            }
        }

        /**
         * Allocates a block of memory.
         * @return index of allocated block, or -1 if allocation failed
         */
        public int allocate() {
            if (freeBlocks.isEmpty()) return -1;
            int blockIndex = freeBlocks.get(0); // First fit
            freeBlocks.remove(0);
            allocated[blockIndex] = true;
            return blockIndex;
        }

        /**
         * Deallocates a block of memory.
         * @param blockIndex The block to free
         * @return true if successfully freed, false if invalid
         */
        public boolean deallocate(int blockIndex) {
            if (blockIndex < 0 || blockIndex >= allocated.length) return false;
            if (!allocated[blockIndex]) return false;
            allocated[blockIndex] = false;
            // Insert in sorted position to maintain first-fit order
            int pos = Collections.binarySearch(freeBlocks, blockIndex);
            if (pos < 0) pos = -(pos + 1);
            freeBlocks.add(pos, blockIndex);
            return true;
        }

        public int getFreeBlocks() {
            return freeBlocks.size();
        }

        public int getAllocatedBlocks() {
            return allocated.length - freeBlocks.size();
        }

        public double getFragmentation() {
            int totalBlocks = allocated.length;
            if (totalBlocks == 0) return 0.0;
            return (double) freeBlocks.size() / totalBlocks;
        }
    }

    /**
     * Analyzes a sequence of memory references and computes statistics
     * for different frame counts, simulating the Belady's Anomaly detection.
     */
    public static Map<Integer, MemoryStats> analyzeReferenceString(int[] referenceString, int[] frameCounts) {
        Map<Integer, MemoryStats> results = new HashMap<>();
        for (int frames : frameCounts) {
            LRUPagedMemory memory = new LRUPagedMemory(frames);
            for (int page : referenceString) {
                memory.accessPage(page);
            }
            results.put(frames, memory.getStats());
        }
        return results;
    }
}
