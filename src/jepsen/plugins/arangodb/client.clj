(ns jepsen.plugins.arangodb.client
  (:require [jepsen.client :as client]
            [jepsen.util   :as util]
            [clojure.tools.logging :refer [info error]])
  (:import (com.arangodb ArangoDB
                        ArangoDB$Builder)
           (com.arangodb.entity BaseDocument)))

(defrecord ArangoClient [^ArangoDB conn ^com.arangodb.ArangoDatabase db]
  client/Client

  (open!  [_ _]
    (info "Connecting to ArangoDB at localhost:8529")
    (let [builder (doto (ArangoDB$Builder.)
                    ;; round-robin across any coordinator nodes
                    (.acquireHostList true)
                    ;; default host/port; adjust if different
                    (.host "127.0.0.1" 8529))
          conn    (.build builder)
          db      (.db conn "jepsen")]
      ;; ensure the collection exists
      (.createCollection db "kv" (util/unmap {:replicationFactor 3
                                              :writeConcern     3}))
      (info "→ Connected; using database 'jepsen' and collection 'kv'")
      (->ArangoClient conn db)))

  (invoke! [_ _ op]
    (try
      (case (:f op)

        :read
        (let [key   (str (:key op))
              doc   (.getDocument (.collection db "kv") key BaseDocument)]
          (assoc op
                 :type  :ok
                 :value (some-> doc (.getProperties) (get "value"))))

        :write
        (let [key   (str (:key op))
              value (:value op)
              doc   (doto (BaseDocument.) 
                      (.setKey key)
                      (.addAttribute "value" value))]
          (.insertDocument (.collection db "kv") doc)
          (assoc op :type :ok, :value value))

        ;; any other op (e.g. nemesis events)
        (assoc op :type :info))

      (catch Exception e
        (error e "ArangoDB op failed:" op)
        (assoc op :type :fail, :error (.getMessage e)))))


  (teardown! [_ _]
    (info "Closing ArangoDB connection")
    (.shutdown conn)))

(defn ->Client
  "Factory for ArangoClient."
  [test node]
  (map->ArangoClient {}))