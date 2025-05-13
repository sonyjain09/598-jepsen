(ns jepsen-db-tests.couchbase.client
  (:require [jepsen.client :as client]
            [clojure.tools.logging :refer [info error]])
  (:import [com.couchbase.client.java Cluster Bucket Collection]
           [com.couchbase.client.java.env ClusterEnvironment]
           [com.couchbase.client.core.env TimeoutConfig]
           [com.couchbase.client.java.kv GetReplicaResult MutationResult]
           [java.time Duration]))

(defrecord CouchbaseClient [^Cluster cluster ^Collection collection]
  client/Client

  (setup! [_ test node]
    (info "Connecting to Couchbase node" node)
    (let [cluster (Cluster/connect (str "couchbase://" node) "Admin" "password")
          bucket (.bucket cluster "jepsen")]
      (.waitUntilReady bucket (Duration/ofSeconds 10))
      (let [collection (.defaultCollection bucket)]
        (->CouchbaseClient cluster collection))))

  (invoke! [this test op]
    (let [k (str (:value op))
          v (str (:value op))
          f (:f op)
          collection (:collection this)]
      (try
        (case f
          :read
          (let [res (.getAnyReplica collection k)]
            (if res
              (assoc op :type :ok :value (.toString (.contentAsObject res)))
              (assoc op :type :ok :value nil)))

          :write
          (do (.upsert collection k v)
              (assoc op :type :ok))

          op)
        (catch Exception e
          (error e "Couchbase op failed")
          (assoc op :type :fail :error (.getMessage e))))))

  (teardown! [this test]
    (info "Disconnecting Couchbase")
    (.disconnect (:cluster this))))

(defn client [] (->CouchbaseClient nil nil))
(defn db [] nil)
