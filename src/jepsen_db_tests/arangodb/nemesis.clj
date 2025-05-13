(ns jepsen-db-tests.arangodb.nemesis
  (:require [jepsen-db-tests.utils.multi-nemesis :refer [multi-nemesis]]))

(defn nemesis [] (multi-nemesis))
