import java.util.concurrent.locks.ReentrantLock;

public class OptimisticList<T extends Comparable<T>> {
    private final Node<T> head;

    public OptimisticList() {
        // sentinel nodes with null values
        head = new Node<>(null);
        head.next = new Node<>(null); // tail sentinel
    }

    // Node definition
    private static class Node<E> {
        final E value;
        volatile Node<E> next;
        final ReentrantLock lock = new ReentrantLock();
        volatile boolean marked = false; // logical deletion flag

        Node(E value) {
            this.value = value;
        }
    }

    // Public API ---------------------------------------------------------

    public boolean contains(T value) {
        Node<T> pred, curr;
        while (true) {
            pred = head;
            curr = pred.next;
            // traverse without locks
            while (curr.value != null && curr.value.compareTo(value) < 0) {
                pred = curr;
                curr = curr.next;
            }
            // at this point, curr is first node >= value or tail
            // validate that pred and curr are still adjacent and not marked
            if (validate(pred, curr)) {
                return (curr.value != null && curr.value.equals(value) && !curr.marked);
            }
            // else retry
        }
    }

    public boolean add(T value) {
        while (true) {
            Node<T> pred = head;
            Node<T> curr = pred.next;
            // locate position
            while (curr.value != null && curr.value.compareTo(value) < 0) {
                pred = curr;
                curr = curr.next;
            }
            pred.lock.lock();
            try {
                curr.lock.lock();
                try {
                    if (!validate(pred, curr)) {
                        continue; // retry
                    }
                    if (curr.value != null && curr.value.equals(value) && !curr.marked) {
                        return false; // already present
                    }
                    // insert new node between pred and curr
                    Node<T> node = new Node<>(value);
                    node.next = curr;
                    pred.next = node;
                    return true;
                } finally {
                    curr.lock.unlock();
                }
            } finally {
                pred.lock.unlock();
            }
        }
    }

    public boolean remove(T value) {
        while (true) {
            Node<T> pred = head;
            Node<T> curr = pred.next;
            // locate node
            while (curr.value != null && curr.value.compareTo(value) < 0) {
                pred = curr;
                curr = curr.next;
            }
            pred.lock.lock();
            try {
                curr.lock.lock();
                try {
                    if (!validate(pred, curr)) {
                        continue; // retry
                    }
                    if (curr.value == null || !curr.value.equals(value) || curr.marked) {
                        return false; // not present
                    }
                    // logical deletion
                    curr.marked = true;
                    // physical removal
                    pred.next = curr.next;
                    return true;
                } finally {
                    curr.lock.unlock();
                }
            } finally {
                pred.lock.unlock();
            }
        }
    }

    // Validation ensures that pred and curr are still adjacent and not marked
    private boolean validate(Node<T> pred, Node<T> curr) {
        return !pred.marked && !curr.marked && pred.next == curr;
    }

    // For testing: dump list contents into an array (sorted, without marked nodes)
    public java.util.List<T> toList() {
        java.util.ArrayList<T> list = new java.util.ArrayList<>();
        Node<T> curr = head.next;
        while (curr.value != null) {
            if (!curr.marked) {
                list.add(curr.value);
            }
            curr = curr.next;
        }
        return list;
    }
}
