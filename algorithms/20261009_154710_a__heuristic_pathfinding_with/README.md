# A* Heuristic Pathfinding with Dynamic Obstacle Cost

Self-contained **A* Heuristic Pathfinding with Dynamic Obstacle Cost** algorithmic primitive written in idiomatic **Java**. Built from scratch using standard library constructs with zero external dependencies.

### Core Highlights
* **Language & Standard**: Modern `Java` standard library conventions.
* **Architecture Pattern**: Designed for `Algorithmic Engineering` using `Standard Memory Primitives`.
* **Runtime Overhead**: Buffer boundaries and collection indices are explicitly validated to prevent out-of-bounds access.
* **Concurrency & Safety**: State transitions follow clear ordering guarantees with explicit validation at each phase.

---

### Complexity Analysis

| Dimension | Bound |
| :--- | :--- |
| **Time (Best Case)** | `O(1)` |
| **Time (Worst Case)** | `O(N log N)` |
| **Auxiliary Space** | `O(N)` |

---

### Test Suite Execution

Self-contained verification drivers are embedded directly in `main.java` to validate happy paths, boundary inputs, and invariant preservation.

```bash
javac main.java && java main
```

---

*Part of the Polyglot Systems Lab • Maintained by [@myonathanlinkedin](https://github.com/myonathanlinkedin)*