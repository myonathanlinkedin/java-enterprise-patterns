# Sliding Window Rate Limiter with Distributed Token Bucket in Java

High-performance **Sliding Window Rate Limiter with Distributed Token Bucket** primitive implemented in idiomatic **Java**. Built from scratch using standard library constructs with zero external dependencies.

## Implementation Details

* **Category**: `Distributed Consensus & State Machine`
* **Data Structure Foundation**: `Append-Only State Log & Version Matrix`
* **Allocation Pattern**: Buffer boundaries are strictly verified to prevent out-of-bounds access and memory leak hazards.
* **Invariant Integrity**: State transitions adhere to strict ordering guarantees with explicit synchronization fences where necessary.

## Performance Characteristics

* **Time**: `$O(\log N) or O(1)$` average, with `$O(1)$` best-case response under ideal conditions.
* **Space**: `$O(N) state log$` memory usage.

## Test Harness

To compile and execute the test assertions for this module:

```bash
javac main.java && java main
```

---

*Source code released under the MIT License • [@myonathanlinkedin](https://github.com/myonathanlinkedin)*