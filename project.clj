(defproject jepsen-db-tests "0.1.0-SNAPSHOT"
  :description "Jepsen experiments (Couchbase, ArangoDB, FoundationDB) with multi-nemesis sequencing"
  :dependencies [[org.clojure/clojure       "1.11.1"]
                 [jepsen                    "0.3.4"]
                 [org.clojure/tools.logging "1.2.4"]
                 [org.clojure/tools.cli     "1.0.214"]]
  :main jepsen-db-tests.core)
