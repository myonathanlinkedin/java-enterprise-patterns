# Sliding Window Rate Limiter with Distributed Token Bucket in Java

Self-contained **Sliding Window Rate Limiter with Distributed Token Bucket** algorithmic primitive written in idiomatic **Java**. Built from scratch using standard library constructs with zero external dependencies.

## Implementation Details

* **Category**: `Distributed Consensus & State Machine`
* **Data Structure Foundation**: `Append-Only State Log & Version Matrix`
* **Allocation Pattern**: Buffer boundaries and collection indices are explicitly validated to prevent out-of-bounds access.
* **Invariant Integrity**: State transitions follow clear ordering guarantees with explicit validation at each phase.

## Performance Characteristics

* **Time**: `O(log N) or O(1)` average, with `O(1)` best-case response under ideal conditions.
* **Space**: `O(N) state log` memory usage.

## Test Harness

To compile and execute the test assertions for this module:

```bash
javac main.java && java main
```

---

*Source code released under the MIT License • [@myonathanlinkedin](https://github.com/myonathanlinkedin)*
