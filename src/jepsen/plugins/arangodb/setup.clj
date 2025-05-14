(ns jepsen.plugins.arangodb.setup
  (:require [jepsen.control :as c]
            [jepsen.logging :refer [info]]))

(defn setup!
  "Installs & bootstraps ArangoDB on `node`. Expects you to have
   placed the CentOS RPM installer and this script:
   resources/scripts/arangodb-setup.sh"
  [test node]
  (info test node "→ Installing ArangoDB via shell script")
  (c/su
    (c/exec node "bash resources/scripts/arangodb-setup.sh"))
  test)