(ns jepsen-db-tests.arangodb.client
  (:require [jepsen.client :as client]
            [clojure.tools.logging :refer [info error]])
  (:import [com.arangodb ArangoDB ArangoDatabase ArangoCollection]
           [com.arangodb.entity BaseDocument]
           [com.arangodb.model DocumentCreateOptions]))

(defrecord ArangoClient [^ArangoDB driver ^ArangoDatabase db ^ArangoCollection coll]
  client/Client

  (setup! [_ test node]
    (info "Connecting to ArangoDB node" node)
    (let [driver (-> (ArangoDB$Builder.)
                     (.host node 8529)
                     (.user "root")
                     (.password "")
                     .build)
          db (.db driver "jepsen")
          coll (.collection db "kv")]
      (->ArangoClient driver db coll)))

  (invoke! [this test op]
    (let [k (str (:value op))
          v (str (:value op))
          f (:f op)
          coll (:coll this)]
      (try
        (case f
          :read
          (let [doc (.getDocument coll k BaseDocument)]
            (assoc op :type :ok :value (when doc (.getAttribute doc "value"))))

          :write
          (let [doc (doto (BaseDocument.)
                      (.setKey k)
                      (.addAttribute "value" v))]
            (.insertDocument coll doc (DocumentCreateOptions.))
            (assoc op :type :ok))

          op)
        (catch Exception e
          (error e "ArangoDB op failed")
          (assoc op :type :fail :error (.getMessage e))))))

  (teardown! [this test]
    (info "Shutting down ArangoDB client")
    (.shutdown (:driver this))))

(defn client [] (->ArangoClient nil nil nil))
(defn db [] nil)
