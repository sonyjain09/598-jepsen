## Jepsen Test Framework

A generic Jepsen-based chaos-testing framework. Drop in one plugin per database (ArangoDB, Couchbase, FoundationDB, etc.)—each providing a setup script and client driver—and run a standardized 5-phase experiment that injects network partitions, node‐kill faults, and measures linearizability, stale-read rate, latencies, throughput, and recovery times.

---

## Experiment Setup

We run a **5-phase workload** against an N-node cluster (typically 5 nodes):

1. **Normal operation (60 s)**  
   Clients issue a mixed 50 % reads / 50 % writes at 500 ops/s over a 50 000-key space.

2. **Random-halves partition (120 s)**  
   The `partition-random-halves` nemesis splits the cluster into two equal halves while clients continue issuing operations.

3. **Recovery (75 s)**  
   The nemesis heals the partition; we measure how quickly the system returns to baseline performance.

4. **Node kill & restart (120 s)**  
   The `node-start-stopper` nemesis selects one random node, kills the database process (SIGKILL), then restarts it via your service manager. Clients continue issuing operations throughout.

5. **Final recovery (75 s)**  
   The nemesis heals the node-kill; we measure recovery time and system stability before ending the test.

All operations and fault events are timestamped and logged. Once complete, the framework automatically computes and visualizes:

- **Linearizability violations**  
- **Stale-read rate** (reads returning values older than the latest write)  
- **Latency distributions** (p50, p99, max)  
- **Throughput over time**  
- **Recovery times** (overlayed on the partition and kill/restart timeline)

---

## Installation & Prerequisites

1. **Cluster Nodes**  
   - SSH access enabled  
   - **JDK 8+**, **Leiningen 2.8+** (manages Clojure and dependencies)  
   - Network connectivity on SSH port and database-specific ports  

2. **Clone the Repository**  
   ```bash
   git clone https://your.git/jepsen-test-framework.git
   cd jepsen-test-framework

## Running the Tests

Invoke the shared harness via Leiningen:

lein run \
  --test  core \
  --target <db> \
  --nodes node1,node2,node3,node4,node5 \
  --time 480