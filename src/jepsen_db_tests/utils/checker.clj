(ns jepsen-db-tests.utils.checker
  (:require [jepsen.checker             :as checker]
            [jepsen.checker.linearizable :as linear]
            [jepsen.checker.performance  :as perf]
            [jepsen.checker.timeline     :as timeline]))

(def strong
  "Strict linearizability + performance + timeline"
  (checker/compose
    {:linear      linear/linearizable
     :performance perf/performance
     :timeline    timeline/timeline}))
