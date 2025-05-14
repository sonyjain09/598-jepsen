(ns jepsen.plugins.couchbase.setup
  (:require [jepsen.control :as c]
            [jepsen.logging :refer [info]]))

(defn setup!
  "Installs & bootstraps Couchbase Server on the node.  
   Expects `resources/scripts/couchbase-setup.sh` to exist."
  [test node]
  (info test node "→ Installing Couchbase via shell script")
  (c/su
    ;; Run your installation & cluster-init script
    (c/exec node "bash resources/scripts/couchbase-setup.sh"))
  ;; Wait a moment for the cluster to stabilize
  (Thread/sleep 10000)
  test)
