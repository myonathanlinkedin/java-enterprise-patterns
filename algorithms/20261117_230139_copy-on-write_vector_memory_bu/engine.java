package cowvector;

/**
 * Copy‑On‑Write vector implementation. Instances may share the same underlying
 * {@link Buffer} until a mutating operation forces a private copy.
 *
 * @param <T> element type
 */
public final class CowVector<T> {
    /** Shared buffer; never {@code null}. */
    private Buffer<T> buffer;

    /** Constructs an empty vector with default initial capacity. */
    public CowVector() {
        this.buffer = new Buffer<>(8);
    }

    /** Internal constructor used for sharing. */
    private CowVector(Buffer<T> sharedBuffer) {
        this.buffer = sharedBuffer.retain();
    }

    /** Returns the number of elements stored. */
    public int size() {
        return buffer.size;
    }

    /** Retrieves the element at {@code index}. */
    public T get(int index) {
        checkIndex(index);
        return buffer.get(index);
    }

    /** Replaces the element at {@code index} with {@code value}. */
    public void set(int index, T value) {
        checkIndex(index);
        ensureUnique();
        buffer.set(index, value);
    }

    /** Appends {@code value} to the end of the vector. */
    public void push(T value) {
        ensureUnique();
        buffer.ensureCapacity(buffer.size + 1);
        buffer.data[buffer.size++] = value;
    }

    /** Removes and returns the last element; throws if empty. */
    public T pop() {
        if (buffer.size == 0) {
            throw new IllegalStateException("pop from empty vector");
        }
        ensureUnique();
        T val = buffer.get(buffer.size - 1);
        buffer.data[--buffer.size] = null; // help GC
        return val;
    }

    /** Returns a shallow copy of the underlying elements as an array. */
    @SuppressWarnings("unchecked")
    public T[] toArray() {
        Object[] arr = new Object[buffer.size];
        System.arraycopy(buffer.data, 0, arr, 0, buffer.size);
        return (T[]) arr;
    }

    /** Creates a new vector that shares the same buffer (copy‑on‑write). */
    public CowVector<T> share() {
        return new CowVector<>(this.buffer);
    }

    /** Ensures that this instance has exclusive ownership of the buffer. */
    private void ensureUnique() {
        if (buffer.isShared()) {
            Buffer<T> old = buffer;
            buffer = old.copy(); // new buffer with refCount == 1
            old.release();      // decrement old's count
        }
        // else already unique; nothing to do
    }

    /** Validates that {@code index} is within [0, size). */
    private void checkIndex(int index) {
        if (index < 0 || index >= buffer.size) {
            throw new IndexOutOfBoundsException(
                "Index " + index + " out of bounds for size " + buffer.size);
        }
    }
}
