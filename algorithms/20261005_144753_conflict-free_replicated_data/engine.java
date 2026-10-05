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

        if (counter.value < 0) {
            throw new IllegalArgumentException("Cannot update with negative counter value.");
        }

        if (localCounter == remoteCounter) {
            throw new IllegalStateException("Cannot update when counters are equal.");
        }

        if (counter.value != counter.getValue()) {
            throw new IllegalStateException("Cannot update with inconsistent counter values.");
        }

        localCounter.compareAndSet(counter.getValue(), counter.getValue() + 1);
        remoteCounter.compareAndSet(counter.getValue(), counter.getValue() + 1);
    }
}
