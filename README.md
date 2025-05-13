# 598-jepsen

# Jepsen-DB-Tests

This project runs **sequential fault-injection experiments** against Couchbase, ArangoDB, and FoundationDB.  
Each test injects three nemeses in order:

1. **Cross-region partition** (150 s) → **heal**  
2. **Kill a node** (150 s) → **restart**  
3. **Network partition** (150 s) → **heal**

Between each fault there is a recovery window (60 s after the first two, 75 s after the final), and a short warm-up (60 s). Total ≈ 11 minutes per run.

---

## Prerequisites

- **Control node** (where you’ll run Jepsen):  
  - Java 8+, Leiningen  
  - SSH key access to all cluster nodes  
- **Cluster nodes** (3–5 per database):  
  - Ubuntu 22.04 or CentOS 7+  
  - Passwordless SSH from control node  

---

## 1. Bootstrap Control Node

```bash
# Install Leiningen (if needed)
curl -fsSL https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > ~/bin/lein
chmod +x ~/bin/lein

# Clone & fetch deps
git clone <your-repo-url> jepsen-db-tests
cd jepsen-db-tests
lein deps
