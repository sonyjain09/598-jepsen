(ns jepsen-db-tests.couchbase.test
  (:require [jepsen.core                      :as jepsen]
            [jepsen-db-tests.couchbase.client  :as cb-client]
            [jepsen-db-tests.couchbase.nemesis :as cb-nem]
            [jepsen-db-tests.utils.generator   :as gen]
            [jepsen-db-tests.utils.checker     :as checker]))

(defn couchbase-test [opts]
  (merge {:name      "couchbase-multi-nemesis"
          :os        jepsen.os.debian/os
          :db        (cb-client/db)
          :client    (cb-client/client)
          :nemesis   (cb-nem/nemesis)
          :generator (gen/workload :balanced)
          :checker   (checker/strong)}
         opts))

(defn run-test [opts]
  (jepsen/run! (couchbase-test opts)))
