(ns jepsen-db-tests.arangodb.test
  (:require [jepsen.core                       :as jepsen]
            [jepsen-db-tests.arangodb.client   :as arango-client]
            [jepsen-db-tests.arangodb.nemesis  :as arango-nem]
            [jepsen-db-tests.utils.generator   :as gen]
            [jepsen-db-tests.utils.checker     :as checker]))

(defn arangodb-test [opts]
  (merge {:name      "arangodb-multi-nemesis"
          :os        jepsen.os.debian/os
          :db        (arango-client/db)
          :client    (arango-client/client)
          :nemesis   (arango-nem/nemesis)
          :generator (gen/workload :balanced)
          :checker   (checker/strong)}
         opts))

(defn run-test [opts]
  (jepsen/run! (arangodb-test opts)))
