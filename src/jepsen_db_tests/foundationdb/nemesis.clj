(ns jepsen-db-tests.foundationdb.nemesis
  (:require [jepsen-db-tests.utils.multi-nemesis :refer [multi-nemesis]]))

(defn nemesis [] (multi-nemesis))
