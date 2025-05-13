(ns jepsen-db-tests.core
  (:require [clojure.tools.cli                :refer [parse-opts]]
            [jepsen-db-tests.couchbase.test   :as cb]
            [jepsen-db-tests.arangodb.test    :as arango]
            [jepsen-db-tests.foundationdb.test :as fdb]))

(def cli-options
  [["-d" "--db NAME"      "Database: couchbase | arangodb | foundationdb"
    :parse-fn keyword]
   ["-h" "--help"         "Show help"]])

(defn usage []
  (println "Usage: lein run -- -d <couchbase|arangodb|foundationdb>"))

(defn -main [& args]
  (let [{:keys [options errors]} (parse-opts args cli-options)]
    (cond
      (:help options) (usage)
      errors          (println "Errors:" errors)
      :else
      (case (:db options)
        :couchbase    (cb/run-test {})
        :arangodb     (arango/run-test {})
        :foundationdb (fdb/run-test {})
        (usage)))))
