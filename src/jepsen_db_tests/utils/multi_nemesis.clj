(ns jepsen-db-tests.utils.multi-nemesis
  (:require [jepsen.nemesis          :as nemesis]
            [jepsen.nemesis.partition :as p]
            [jepsen.nemesis.kill      :as k]))

(defn multi-nemesis
  "Composite nemesis:
   • :start-cross-region / :stop-cross-region
   • :kill-node         / :restart-node
   • :start-partition   / :stop-partition"
  []
  (let [cross  (p/partition-random-halves)
        killer (k/kill-random-node)
        netp   (p/partition-majorities-ring)]
    (reify nemesis/Nemesis
      (setup!    [_ test]
        (doseq [n [cross killer netp]]
          (nemesis/setup! n test)))
      (invoke!   [_ test op]
        (case (:f op)
          :start-cross-region (nemesis/invoke! cross   test op)
          :stop-cross-region  (nemesis/invoke! cross   test op)
          :kill-node          (nemesis/invoke! killer test op)
          :restart-node       (nemesis/invoke! killer test op)
          :start-partition    (nemesis/invoke! netp    test op)
          :stop-partition     (nemesis/invoke! netp    test op)
          (assoc op :type :info)))
      (teardown! [_ test]
        (doseq [n [cross killer netp]]
          (nemesis/teardown! n test))))))
