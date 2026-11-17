package cowvector;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Internal buffer that holds the actual array data and a reference count.
 * The buffer is mutable only within this package; external users interact
 * through {@link CowVector}.
 *
 * @param <T> element type
 */
final class Buffer<T> {
    /** Underlying storage; may contain nulls beyond {@code size}. */
    Object[] data;
    /** Number of valid elements in {@code data}. */
    int size;
    /** Reference count for copy‑on‑write sharing. */
    final AtomicInteger refCount = new AtomicInteger(1);

    Buffer(int capacity) {
        this.data = new Object[capacity];
        this.size = 0;
    }

    Buffer(Object[] src, int size) {
        this.data = src;
        this.size = size;
    }

    /** Increments the reference count and returns this buffer. */
    Buffer<T> retain() {
        refCount.incrementAndGet();
        return this;
    }

    /**
     * Decrements the reference count. If it reaches zero the buffer becomes
     * eligible for garbage collection.
     */
    void release() {
        refCount.decrementAndGet();
    }

    /** @return true if more than one owner shares this buffer. */
    boolean isShared() {
        return refCount.get() > 1;
    }

    /** Creates a shallow copy of the underlying array with the same size. */
    Buffer<T> copy() {
        Object[] newData = new Object[data.length];
        System.arraycopy(this.data, 0, newData, 0, this.size);
        return new Buffer<>(newData, this.size);
    }

    /** Ensures capacity for at least {@code minCapacity} elements. */
    void ensureCapacity(int minCapacity) {
        if (minCapacity <= data.length) {
            return;
        }
        int newCap = Math.max(data.length * 2, minCapacity);
        Object[] newData = new Object[newCap];
        System.arraycopy(data, 0, newData, 0, size);
        data = newData;
    }

    /** Retrieves element at {@code index} without bounds checking. */
    @SuppressWarnings("unchecked")
    T get(int index) {
        return (T) data[index];
    }

    /** Stores {@code value} at {@code index} without bounds checking. */
    void set(int index, T value) {
        data[index] = value;
    }
}
