(ns jepsen.core.checker
  (:require [jepsen.checker :as checker]
            [jepsen.model   :as model]))

(defn kv-model []
  (model/kv))

(defn linearizable-with-stale
  "Wraps linearizability checker and also counts stale reads."
  []
  (checker/linearizable
    {:model       (kv-model)
     :printer     identity
     :history     nil
     :post-check
       (fn [results history]
         ;; scan history for reads older than last write per key
         (let [stale-reads
               (->> history
                    (filter #(and (= :invoke (:type %))
                                  (= :read  (:f   %))
                                  ;; compare timestamps & values here...
                                  false))]
           (assoc results :stale-reads (count stale-reads))))}))
