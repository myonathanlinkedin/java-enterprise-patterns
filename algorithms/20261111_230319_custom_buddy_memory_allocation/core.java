import java.util.*;
public class BuddyAllocator {
    private final int totalSize;
    private final int minBlockSize;
    private final java.util.Map<Integer, java.util.List<Integer>> freeLists;
    private final java.util.Map<Integer, Integer> allocationMap;

    public BuddyAllocator(int totalSize, int minBlockSize) {
        if (totalSize <= 0 || minBlockSize <= 0) {
            throw new IllegalArgumentException("Sizes must be positive");
        }
        if (!isPowerOfTwo(totalSize) || !isPowerOfTwo(minBlockSize)) {
            throw new IllegalArgumentException("Sizes must be powers of two");
        }
        if (minBlockSize > totalSize) {
            throw new IllegalArgumentException("minBlockSize > totalSize");
        }
        this.totalSize = totalSize;
        this.minBlockSize = minBlockSize;
        this.freeLists = new java.util.HashMap<>();
        this.allocationMap = new java.util.HashMap<>();
        freeLists.put(totalSize, new java.util.ArrayList<>());
        freeLists.get(totalSize).add(0);
    }

    public int allocate(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("size must be positive");
        }
        int blockSize = Math.max(minBlockSize, nextPowerOfTwo(size));
        if (blockSize > totalSize) {
            return -1;
        }
        int currentSize = blockSize;
        while (currentSize <= totalSize) {
            java.util.List<Integer> list = freeLists.get(currentSize);
            if (list != null && !list.isEmpty()) {
                int offset = list.remove(list.size() - 1);
                while (currentSize > blockSize) {
                    currentSize /= 2;
                    int buddyOffset = offset + currentSize;
                    freeLists.computeIfAbsent(currentSize, k -> new java.util.ArrayList<>()).add(buddyOffset);
                }
                allocationMap.put(offset, blockSize);
                return offset;
            }
            currentSize *= 2;
        }
        return -1;
    }

    public boolean free(int offset, int size) {
        Integer blockSizeObj = allocationMap.remove(offset);
        if (blockSizeObj == null || blockSizeObj != size) {
            return false;
        }
        int currentSize = size;
        int currentOffset = offset;
        while (currentSize < totalSize) {
            int buddyOffset = currentOffset ^ currentSize;
            java.util.List<Integer> list = freeLists.get(currentSize);
            if (list != null && list.remove((Integer) buddyOffset)) {
                currentOffset = Math.min(currentOffset, buddyOffset);
                currentSize *= 2;
            } else {
                break;
            }
        }
        freeLists.computeIfAbsent(currentSize, k -> new java.util.ArrayList<>()).add(currentOffset);
        return true;
    }

    public int getFreeMemory() {
        int free = 0;
        for (java.util.Map.Entry<Integer, java.util.List<Integer>> e : freeLists.entrySet()) {
            free += e.getKey() * e.getValue().size();
        }
        return free;
    }

    public int getAllocatedMemory() {
        int allocated = 0;
        for (int size : allocationMap.values()) {
            allocated += size;
        }
        return allocated;
    }

    private static boolean isPowerOfTwo(int x) {
        return (x & (x - 1)) == 0;
    }

    private static int nextPowerOfTwo(int x) {
        if (x <= 1) return 1;
        int v = x - 1;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        return v + 1;
    }
}
