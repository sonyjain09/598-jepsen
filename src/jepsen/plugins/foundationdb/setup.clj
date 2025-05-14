(ns jepsen.plugins.foundationdb.setup
  (:require [jepsen.control :as c]
            [jepsen.logging :refer [info]]))

(defn setup!
  "Installs and bootstraps FoundationDB on each node.
   Assumes resources/scripts/foundationdb-setup.sh has created
   /etc/foundationdb/fdb.cluster with a valid token and coordinators."
  [test node]
  (info test node "→ Installing FoundationDB via shell script")
  (c/su
    ;; Run the RPM install + cluster file placement script
    (c/exec node "bash resources/scripts/foundationdb-setup.sh")
    ;; Ensure the cluster file is in place
    (c/exec node "chown fdb:fdb /etc/foundationdb/fdb.cluster")
    ;; Start the FoundationDB service
    (c/exec node "systemctl enable foundationdb")
    (c/exec node "systemctl start foundationdb"))
  ;; Wait for the service to be healthy
  (Thread/sleep 10000)
  test)