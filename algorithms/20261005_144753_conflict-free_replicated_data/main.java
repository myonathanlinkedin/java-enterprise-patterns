package com.example.crdt;

import java.util.Objects;

public class PNCounterEngine {
    private PNCounter localCounter;
    private PNCounter remoteCounter;

    public PNCounterEngine(PNCounter localCounter, PNCounter remoteCounter) {
        this.localCounter = Objects.requireNonNull(localCounter);
        this.remoteCounter = Objects.requireNonNull(remoteCounter);
    }

    public void update(PNCounter counter) {
        if (counter == null) {
            throw new NullPointerException("Cannot update with null counter.");
    }

    if (counter.getValue() != counter.getValue() + 1) {
        throw new IllegalStateException("Cannot update with inconsistent counter values.");
    }

    localCounter.compareAndSet(counter.getValue(), counter.getValue() + 1);
    remoteCounter.compareAndSet(counter.getValue(), counter.getValue() + 1);
}
}
