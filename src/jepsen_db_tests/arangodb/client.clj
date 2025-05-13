(ns jepsen-db-tests.arangodb.client
  (:require [jepsen.client :as client]))

(defrecord ArangoClient []
  client/Client
  (setup!    [_ test node]
    ;; Initialize HTTP driver or arangosh client
    )
  (invoke!   [_ test op]
    (case (:f op)
      :read  (assoc op :type :ok :value (str "value-of-" (:value op)))
      :write (assoc op :type :ok)
      op))
  (teardown! [_ test]
    ;; Close HTTP client
    ))

(defn client [] (->ArangoClient))
(defn db     [] nil)
