import java.util.function.Predicate;

/**
 * Core definitions for Interaction Trees and compositional temporal specifications.
 */
public abstract class InteractionTree<T> {
    /** Advance one step: for Tau nodes returns the child, otherwise returns this. */
    public InteractionTree<T> advance() {
        return this;
    }

    /** True if this node is a Return. */
    public abstract boolean isReturn();

    /** Value of Return node; undefined otherwise. */
    public abstract T getReturnValue();

    /** True if this node is a Vis (visible event). */
    public abstract boolean isVis();

    /** Event label of Vis node; undefined otherwise. */
    public abstract T getEvent();

    /** Continuation after the visible event; undefined otherwise. */
    public abstract InteractionTree<T> getContinuation();

    /** Helper to recursively skip Tau nodes. */
    public InteractionTree<T> skipTau() {
        InteractionTree<T> cur = this;
        while (cur instanceof Tau) {
            cur = ((Tau<T>) cur).child;
        }
        return cur;
    }

    /** Evaluate a temporal specification with a depth limit to avoid non‑termination. */
    public boolean satisfies(TemporalSpec<T> spec, int maxDepth) {
        return spec.eval(this, maxDepth);
    }
}

/** Return node – leaf of the tree containing a value. */
final class Return<T> extends InteractionTree<T> {
    private final T value;

    public Return(T value) {
        this.value = value;
    }

    @Override
    public boolean isReturn() { return true; }

    @Override
    public T getReturnValue() { return value; }

    @Override
    public boolean isVis() { return false; }

    @Override
    public T getEvent() { throw new IllegalStateException("Return has no event"); }

    @Override
    public InteractionTree<T> getContinuation() { throw new IllegalStateException("Return has no continuation"); }
}

/** Tau node – silent step leading to a child subtree. */
final class Tau<T> extends InteractionTree<T> {
    final InteractionTree<T> child;

    public Tau(InteractionTree<T> child) {
        this.child = child;
    }

    @Override
    public InteractionTree<T> advance() {
        return child;
    }

    @Override
    public boolean isReturn() { return false; }

    @Override
    public T getReturnValue() { throw new IllegalStateException("Tau has no return value"); }

    @Override
    public boolean isVis() { return false; }

    @Override
    public T getEvent() { throw new IllegalStateException("Tau has no event"); }

    @Override
    public InteractionTree<T> getContinuation() { throw new IllegalStateException("Tau has no continuation"); }
}

/** Vis node – visible event with a continuation. */
final class Vis<T> extends InteractionTree<T> {
    private final T event;
    private final InteractionTree<T> next;

    public Vis(T event, InteractionTree<T> next) {
        this.event = event;
        this.next = next;
    }

    @Override
    public boolean isReturn() { return false; }

    @Override
    public T getReturnValue() { throw new IllegalStateException("Vis has no return value"); }

    @Override
    public boolean isVis() { return true; }

    @Override
    public T getEvent() { return event; }

    @Override
    public InteractionTree<T> getContinuation() { return next; }
}

/** Abstract temporal specification. */
abstract class TemporalSpec<T> {
    /** Evaluate the specification on a tree with a remaining depth budget. */
    abstract boolean eval(InteractionTree<T> tree, int depth);
}

/** "Always p": predicate p holds on every visible event reachable within depth. */
final class AlwaysSpec<T> extends TemporalSpec<T> {
    private final Predicate<T> pred;

    public AlwaysSpec(Predicate<T> pred) {
        this.pred = pred;
    }

    @Override
    boolean eval(InteractionTree<T> tree, int depth) {
        if (depth < 0) return true; // out of budget, conservatively succeed
        InteractionTree<T> cur = tree.skipTau();
        if (cur.isReturn()) return true;
        if (cur.isVis()) {
            if (!pred.test(cur.getEvent())) return false;
            return eval(cur.getContinuation(), depth - 1);
        }
        // Should not reach here
        return true;
    }
}

/** "Eventually p": there exists a reachable visible event satisfying p within depth. */
final class EventuallySpec<T> extends TemporalSpec<T> {
    private final Predicate<T> pred;

    public EventuallySpec(Predicate<T> pred) {
        this.pred = pred;
    }

    @Override
    boolean eval(InteractionTree<T> tree, int depth) {
        if (depth < 0) return false;
        InteractionTree<T> cur = tree.skipTau();
        if (cur.isReturn()) return false;
        if (cur.isVis()) {
            if (pred.test(cur.getEvent())) return true;
            return eval(cur.getContinuation(), depth - 1);
        }
        return false;
    }
}

/** "p1 Until p2": p1 holds on all events before the first event satisfying p2. */
final class UntilSpec<T> extends TemporalSpec<T> {
    private final Predicate<T> p1;
    private final Predicate<T> p2;

    public UntilSpec(Predicate<T> p1, Predicate<T> p2) {
        this.p1 = p1;
        this.p2 = p2;
    }

    @Override
    boolean eval(InteractionTree<T> tree, int depth) {
        if (depth < 0) return false;
        InteractionTree<T> cur = tree.skipTau();
        if (cur.isReturn()) return false;
        if (cur.isVis()) {
            T ev = cur.getEvent();
            if (p2.test(ev)) return true;
            if (!p1.test(ev)) return false;
            return eval(cur.getContinuation(), depth - 1);
        }
        return false;
    }
}
