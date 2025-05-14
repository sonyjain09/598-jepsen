(ns jepsen.plugins.couchbase.client
  (:require [jepsen.client :as client]
            [clojure.tools.logging :refer [info error]])
  (:import (com.couchbase.client.java CouchbaseCluster Bucket)
           (com.couchbase.client.java.document JsonDocument)
           (com.couchbase.client.java.document.json JsonObject)))

(defrecord CouchClient [test node ^CouchbaseCluster cluster ^Bucket bucket]
  client/Client

  (open! [_ _]
    (info "→ Connecting to Couchbase cluster at localhost")
    ;; Connect to the local node
    (let [cluster (CouchbaseCluster/create (into-array String ["127.0.0.1"]))
          bucket  (.openBucket cluster "jepsen" "password")]
      (info "→ Connected; using bucket 'jepsen'")
      (->CouchClient test node cluster bucket)))

  (invoke! [_ _ op]
    (try
      (case (:f op)
        :read
        (let [key (:key op)
              doc (.get bucket (str key))]
          (if doc
            (assoc op :type :ok
                      :value (.getString (.content ^JsonDocument doc) "value"))
            (assoc op :type :ok
                      :value nil)))

        :write
        (let [key   (:key op)
              value (:value op)
              content (doto (JsonObject/create)
                        (.put "value" (str value)))
              doc    (JsonDocument/create (str key) content)]
          (.upsert bucket doc)
          (assoc op :type :ok :value value))

        ;; nemesis/control events or unrecognized ops
        (assoc op :type :info))

      (catch Exception e
        (error e "Couchbase op failed:" op)
        (assoc op :type :fail :error (.getMessage e)))))

  (teardown! [_ _]
    (info "→ Closing Couchbase connections")
    (when bucket
      (.close bucket))
    (when cluster
      (.disconnect cluster))))

(defn ->Client
  "Factory for CouchClient."
  [test node]
  (map->CouchClient {:test test :node node}))