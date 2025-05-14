(ns jepsen.core.core
  (:require [jepsen.core    :refer [Test]]
            [jepsen.control :as c]
            [jepsen.core.runner :refer [*opts*]]
            [jepsen.core.client :as client-factory]
            [jepsen.core.generator :as gen]
            [jepsen.core.nemesis :as nem]
            [jepsen.core.checker :as chk]))

(defn setup! [test node]
  (c/su
    (c/exec node (str "bash resources/scripts/" (:target *opts*) "-setup.sh")))
  test)

(defn teardown! [test node]
  ;; plugin can define additional teardown if needed
  test)

(def test
  (reify Test
    (setup!    [_ test node]    (setup! test node))
    (teardown! [_ test node]    (teardown! test node))
    (client    [_ test node]    (client-factory/make test node))
    (generator [_ test]         (gen/workload test))
    (nemesis   [_ test]         (nem/nemesis))
    (checker   [_ test]         (chk/linearizable-with-stale))
    (model     [_ test]         (chk/kv-model))))
