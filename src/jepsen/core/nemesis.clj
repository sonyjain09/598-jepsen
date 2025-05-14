(ns jepsen.core.nemesis
  (:require [jepsen.nemesis           :as nemesis]
            [jepsen.nemesis.partition :refer [partition-random-halves]]
            [jepsen.nemesis.partition :refer [partition-one]]
            [jepsen.nemesis.kill      :refer [node-start-stopper]]
            [jepsen.control           :as c]))

(def nemesis
  (nemesis/compose
    {#{:pr-start :pr-stop}
     (partition-random-halves)

     { :split-start :split-stop }
     (partition-one)

     ;; optional crash tests
     #{:kill :restart}
     (node-start-stopper
       (fn [_ nodes] (rand-nth nodes))
       (fn [_ node]  (c/exec node "pkill -STOP -f java"))
       (fn [_ node]  (c/exec node "pkill -CONT -f java")))}))
