(ns jepsen.core.client
  (:require [jepsen.client :as client]
            [clojure.string :as str]))

(defprotocol DBClient
  (open!     [this test node])
  (invoke!   [this test op])
  (teardown! [this test]))

(defn make
  "Loads plugin client and returns an instance."
  [test node]
  (let [ns-name  (symbol (str "jepsen.plugins."
                              (:target test)
                              ".client"))
        _        (require ns-name)
        factory  (ns-resolve ns-name '->Client)]
    (factory test node)))
