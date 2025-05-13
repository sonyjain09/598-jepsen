# Jepsen-DB-Tests

A Jepsen-based fault-injection suite for **Couchbase**, **ArangoDB** and **FoundationDB**, each configured in its strongest consistency mode.  
Run sequential fault scenarios—cross-region partition, node kill/restart and network partition—interleaved with recovery windows, and verify linearizability, performance and failure-timeline behavior under load.

---

## What’s in this project

- **`resources/scripts/…-setup.sh`**  
  Bootstrap scripts that install and configure each database on a cluster node in “strong” mode: replication, durability and write-concern settings tuned for maximal safety.  
- **`resources/workloads/*.edn`**  
  Three workload profiles (read-heavy, balanced, write-heavy) at 500 ops/s over 50 000 keys.  
- **`src/jepsen_db_tests/…`**  
  Clojure sources that define:
  - A **composite nemesis** (cross-region split, kill/restart, network partition).
  - A **generator** that drives warm-up, fault phases and recoveries in sequence.
  - A **checker** that enforces strict linearizability and collects perf/timeline data.
  - Three parallel test runners—one per database—that share the same nemesis/generator/checker scaffolding.
- **`project.clj`**  
  Leiningen project file pulling in Jepsen and required Clojure libraries.

---

## Installation & Setup

### 1. Control Node

1. Install **Java 8+** and **Leiningen**  
   ```bash
   curl -fsSL https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein -o ~/bin/lein  
   chmod +x ~/bin/lein

