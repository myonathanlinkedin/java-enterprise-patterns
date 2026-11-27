import java.util.*;

public class BuddyAllocator {
    private final int totalSize;
    private final int maxOrder; // order of totalSize (log2)
    private final List<Set<Integer>> freeSets; // per order free block offsets

    public BuddyAllocator(int totalSize) {
        if (totalSize <= 0 || (totalSize & (totalSize - 1)) != 0) {
            throw new IllegalArgumentException("totalSize must be power of two and >0");
        }
        this.totalSize = totalSize;
        this.maxOrder = log2(totalSize);
        this.freeSets = new ArrayList<>(maxOrder + 1);
        for (int i = 0; i <= maxOrder; i++) {
            freeSets.add(new HashSet<>());
        }
        // initially whole memory is free at maxOrder
        freeSets.get(maxOrder).add(0);
    }

    /**
     * Allocate a block of at least the requested size.
     * @param size requested size in bytes
     * @return offset of allocated block, or -1 if allocation fails
     */
    public int allocate(int size) {
        if (size <= 0) {
            return -1;
        }
        int order = orderForSize(size);
        if (order > maxOrder) {
            return -1; // request larger than total pool
        }
        int curOrder = order;
        while (curOrder <= maxOrder && freeSets.get(curOrder).isEmpty()) {
            curOrder++;
        }
        if (curOrder > maxOrder) {
            return -1; // no suitable block
        }
        // take a block from curOrder and split down
        int offset = takeBlock(curOrder);
        while (curOrder > order) {
            curOrder--;
            int buddy = offset + (1 << curOrder);
            freeSets.get(curOrder).add(buddy);
        }
        return offset;
    }

    /**
     * Free a previously allocated block.
     * @param offset offset returned by allocate
     * @param size original requested size (must be same as used in allocate)
     */
    public void free(int offset, int size) {
        if (offset < 0 || offset >= totalSize) {
            throw new IllegalArgumentException("Invalid offset");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("Invalid size");
        }
        int order = orderForSize(size);
        int curOffset = offset;
        int curOrder = order;
        while (curOrder <= maxOrder) {
            int buddy = buddyOf(curOffset, curOrder);
            Set<Integer> set = freeSets.get(curOrder);
            if (set.remove(buddy)) {
                // buddy was free, coalesce
                curOffset = Math.min(curOffset, buddy);
                curOrder++;
            } else {
                break;
            }
        }
        freeSets.get(curOrder).add(curOffset);
    }

    // Helper: obtain and remove an arbitrary block from given order
    private int takeBlock(int order) {
        Set<Integer> set = freeSets.get(order);
        Iterator<Integer> it = set.iterator();
        int offset = it.next();
        it.remove();
        return offset;
    }

    // Compute smallest order such that blockSize >= size
    private int orderForSize(int size) {
        if (size <= 0) {
            return -1;
        }
        int rounded = roundUpToPowerOfTwo(size);
        return log2(rounded);
    }

    private static int roundUpToPowerOfTwo(int n) {
        if (n <= 1) {
            return 1;
        }
        int highest = Integer.highestOneBit(n);
        return (n == highest) ? n : highest << 1;
    }

    private static int log2(int n) {
        return 31 - Integer.numberOfLeadingZeros(n);
    }

    private static int buddyOf(int offset, int order) {
        return offset ^ (1 << order);
    }

    // For testing/debugging: expose internal state size per order
    int freeCount(int order) {
        if (order < 0 || order > maxOrder) {
            throw new IllegalArgumentException();
        }
        return freeSets.get(order).size();
    }
}
