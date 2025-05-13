(ns jepsen-db-tests.foundationdb.test
  (:require [jepsen.core                             :as jepsen]
            [jepsen-db-tests.foundationdb.client      :as fdb-client]
            [jepsen-db-tests.foundationdb.nemesis     :as fdb-nem]
            [jepsen-db-tests.utils.generator          :as gen]
            [jepsen-db-tests.utils.checker            :as checker]))

(defn foundationdb-test [opts]
  (merge {:name      "foundationdb-multi-nemesis"
          :os        jepsen.os.debian/os
          :db        (fdb-client/db)
          :client    (fdb-client/client)
          :nemesis   (fdb-nem/nemesis)
          :generator (gen/workload :balanced)
          :checker   (checker/strong)}
         opts))

(defn run-test [opts]
  (jepsen/run! (foundationdb-test opts)))
