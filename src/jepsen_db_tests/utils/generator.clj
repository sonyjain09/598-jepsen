(ns jepsen-db-tests.utils.generator
  (:require [jepsen.generator :as gen]
            [clojure.edn       :as edn]
            [clojure.java.io   :as io]))

(def workload-files
  {:read-heavy  "resources/workloads/read-heavy.edn"
   :balanced    "resources/workloads/balanced.edn"
   :write-heavy "resources/workloads/write-heavy.edn"})

(defn workload
  "1) Warmup             (60s)
   2) Cross-region       (150s) → heal → recovery (60s)
   3) Kill node          (150s) → restart → recovery (60s)
   4) Network partition  (150s) → heal → final recovery (75s)
   Total ≈ 645s (~10.75 min)"
  [which]
  (let [{:keys [rate read-ratio write-ratio]}
        (edn/read-string (slurp (workload-files which)))
        ops (concat (repeat (long (* rate read-ratio))  {:type :invoke :f :read})
                    (repeat (long (* rate write-ratio)) {:type :invoke :f :write}))]
    (gen/phases
      (gen/log   "Warmup")
      (gen/time-limit 60  (gen/mix ops))

      (gen/log   "Cross-region")
      (gen/nemesis (gen/once {:type :info :f :start-cross-region}))
      (gen/time-limit 150 (gen/mix ops))
      (gen/nemesis (gen/once {:type :info :f :stop-cross-region}))
      (gen/log   "Recovery after cross-region")
      (gen/time-limit 60  (gen/mix ops))

      (gen/log   "Kill node")
      (gen/nemesis (gen/once {:type :info :f :kill-node}))
      (gen/time-limit 150 (gen/mix ops))
      (gen/nemesis (gen/once {:type :info :f :restart-node}))
      (gen/log   "Recovery after kill")
      (gen/time-limit 60  (gen/mix ops))

      (gen/log   "Network partition")
      (gen/nemesis (gen/once {:type :info :f :start-partition}))
      (gen/time-limit 150 (gen/mix ops))
      (gen/nemesis (gen/once {:type :info :f :stop-partition}))
      (gen/log   "Final recovery")
      (gen/time-limit 75  (gen/mix ops)))))
