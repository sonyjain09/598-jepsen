(ns jepsen.core.generator
  (:require [jepsen.generator :as gen]))

(defn workload
  "5-phase workload with 50 000 keys, 500 ops/sec, 50% reads/50% writes."
  [test]
  (let [key-count 50000
        ;; interval between ops = 1 / ops-per-sec
        dt         (/ 1.0 500)
        ;; one thread’s op generator
        thread-gen (fn []
                     (if (< (rand) 0.5)
                       {:f   :read
                        :key (rand-int key-count)}
                       {:f     :write
                        :key   (rand-int key-count)
                        :value (rand-int key-count)}))
        ;; apply the per-thread generator, then throttle at dt
        client-gen (-> (gen/each-thread thread-gen)
                       (gen/delay-til dt))
        ;; now schedule it across your 5 phases
        limit      (partial gen/time-limit (:time test))]
    (gen/phases
      {:name   :normal
       :during (limit  60 client-gen)}

      {:name   :pr
       :start  (gen/once {:f :pr-start})
       :during (limit 120 client-gen)
       :stop   (gen/once {:f :pr-stop})}

      {:name   :pr-heal
       :during (limit  75 client-gen)}

      {:name   :split
       :start  (gen/once {:f :split-start})
       :during (limit 120 client-gen)
       :stop   (gen/once {:f :split-stop})}

      {:name   :split-heal
       :during (limit  75 client-gen)
       :stop   (gen/once {:info :stop-test})})))