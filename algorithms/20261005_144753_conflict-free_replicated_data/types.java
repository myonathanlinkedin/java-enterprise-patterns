package com.example.crdt;

public interface PNCounter {
    int getValue();
    void compareAndSet(int newValue);
}
