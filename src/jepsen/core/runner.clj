(ns jepsen.core.runner
  (:require [jepsen.cli        :as cli]
            [jepsen.runner     :refer [run!]]))

(defn -main [& args]
  (cli/run!
    {:main     jepsen.runner/run!
     :version  "0.1.0-SNAPSHOT"} 
    args))
