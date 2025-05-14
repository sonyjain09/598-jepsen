(ns jepsen.core.generator
  (:require [jepsen.generator :as gen]))

(defn workload
  [test]
  (let [key-count 50000
        dt         (/ 1.0 500)           ; 500 ops/sec
        client-gen (-> (gen/each-thread
                          (fn []
                            (if (< (rand) 0.5)
                              {:f   :read  :key (rand-int key-count)}
                              {:f   :write :key (rand-int key-count)
                               :value (rand-int key-count)})))
                       (gen/delay-til dt))
        limit      (partial gen/time-limit (:time test))]
    (gen/phases

      ;; 1: normal
      {:name   :normal
       :during (limit 60 client-gen)}

      ;; 2: random-halves
      {:name   :pr
       :start  (gen/once {:f :pr-start})
       :during (limit 120 client-gen)
       :stop   (gen/once {:f :pr-stop})}

      ;; 3: recover
      {:name   :pr-heal
       :during (limit 75 client-gen)}

      ;; 4: node kill & restart
      {:name   :kill
       :start  (gen/once {:f :kill})
       :during (limit 120 client-gen)
       :stop   (gen/once {:f :restart})}

      ;; 5: final recovery & stop
      {:name   :kill-heal
       :during (limit 75 client-gen)
       :stop   (gen/once {:info :stop-test})})))