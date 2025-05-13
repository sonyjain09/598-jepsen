(ns jepsen-db-tests.foundationdb.client
  (:require [jepsen.client :as client]
            [clojure.tools.logging :refer [info error]])
  (:import [com.apple.foundationdb FDB Database Transaction]
           [java.nio.charset StandardCharsets]))

(defrecord FDBClient [^Database db]
  client/Client

  (setup! [_ test node]
    (info "Connecting to FoundationDB")
    (let [fdb (FDB/selectAPIVersion 620)
          db (.open fdb)]
      (->FDBClient db)))

  (invoke! [this test op]
    (let [k (.getBytes (str (:value op)) StandardCharsets/UTF_8)
          v (.getBytes (str (:value op)) StandardCharsets/UTF_8)
          f (:f op)
          db (:db this)]
      (try
        (case f
          :read
          (let [res (-> db
                        (.read (reify java.util.function.Function
                                 (apply [_ tr]
                                   (.get tr k)))))
                val (when res (String. (.join res) StandardCharsets/UTF_8))]
            (assoc op :type :ok :value val))

          :write
          (do (.run db (reify java.util.function.Function
                        (apply [_ tr]
                          (.set ^Transaction tr k v)
                          nil)))
              (assoc op :type :ok))

          op)
        (catch Exception e
          (error e "FDB op failed")
          (assoc op :type :fail :error (.getMessage e))))))

  (teardown! [_ test]
    (info "FDB client done")))

(defn client [] (->FDBClient nil))
(defn db [] nil)
