# Quantitative Verification of Infinite-State Networks

Self-contained **Quantitative Verification of Infinite-State Networks** algorithmic primitive written in idiomatic **Java**. Built from scratch using standard library constructs with zero external dependencies.

### Core Highlights
* **Language & Standard**: Modern `Java` standard library conventions.
* **Architecture Pattern**: Designed for `Algorithmic Engineering` using `Standard Memory Primitives`.
* **Runtime Overhead**: Contiguous memory layouts and standard collections are favored for straightforward iteration and access.
* **Concurrency & Safety**: State consistency is verified after mutations through assertion test coverage.

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

<sub>Standard Java reference implementation • Maintained by [@myonathanlinkedin](https://github.com/myonathanlinkedin)</sub>