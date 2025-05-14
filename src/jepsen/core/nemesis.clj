(ns jepsen.core.nemesis
  (:require
    [jepsen.nemesis           :as nemesis]
    [jepsen.nemesis.partition :refer [partition-random-halves]]
    [jepsen.nemesis.kill      :refer [node-start-stopper]]
    [jepsen.control           :as c]))

(def nemesis
  (nemesis/compose
    {;; Phase 2: random-halves partition
     #{:pr-start :pr-stop}
     (partition-random-halves)

     ;; Phase 4: true node kill & restart
     #{:kill :restart}
     (node-start-stopper
       ;; targeter: pick a random node each time
       (fn [_ nodes] (rand-nth nodes))

       ;; on :start → kill the DB process (SIGKILL)
       (fn [_ node]
         (c/exec node "pkill -9 -f java"))

       ;; on :stop  → restart the DB (you must have a startup script in your setup!)
       (fn [_ node]
         (c/exec node "systemctl restart mydb-service")))}))