(ns jepsen-db-tests.couchbase.client
  (:require [jepsen.client :as client]))

(defrecord CouchbaseClient [bucket]
  client/Client
  (setup!    [_ test node]
    ;; Initialize SDK connection to Couchbase node
    )
  (invoke!   [_ test op]
    (case (:f op)
      :read  (assoc op :type :ok :value (str "value-of-" (:value op)))
      :write (assoc op :type :ok)
      op))
  (teardown! [_ test]
    ;; Close SDK connection
    ))

(defn client [] (->CouchbaseClient "jepsen"))
(defn db     [] nil)  ;; No separate DB process here
