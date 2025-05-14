(ns jepsen.plugins.foundationdb.client
  (:require [jepsen.client :as client]
            [jepsen.util   :as util]
            [clojure.tools.logging :refer [info error]])
  (:import (com.apple.foundationdb FDB)
           (com.apple.foundationdb.subspace Subspace)
           (com.apple.foundationdb.tuple Tuple)
           (java.nio ByteBuffer)))

(defrecord FDBClient [test node fdb db subspace]
  client/Client

  (open! [_ _]
    (info "→ Initializing FoundationDB API and opening DB")
    ;; Must select an API version before loading the library
    (let [fdb     (FDB/selectAPIVersion 720)   ;; 7.2.0 API :contentReference[oaicite:1]{index=1}
          db      (.open fdb)
          ;; Use a simple tuple-based subspace for keys of form [“kv”, key]
          subspace (Subspace. (Tuple/fromBytes (.pack (Tuple. (into-array Object ["kv"]))))) ]
      (info "→ Connected; using subspace 'kv'")
      (->FDBClient test node fdb db subspace)))

  (invoke! [_ _ op]
    (try
      (case (:f op)

        :read
        (let [key      (long (:key op))
              ;; Pack the tuple [“kv”, key] into a byte[] key
              packed   (.pack (.add (Tuple. (into-array Object ["kv"])) key))
              result   (.get (.get db (ByteBuffer/wrap packed)))]
          (assoc op
                 :type  :ok
                 :value (when result
                          ;; unpack returns [“kv”, key, value], so index 2
                          (let [unpacked (.unpack (Tuple.) result)]
                            (.getUInt unpacked 2)))))

        :write
        (let [key      (long (:key op))
              value    (long (:value op))
              packed   (.pack (-> (Tuple.)
                                  (.add "kv")
                                  (.add key)
                                  (.add value)))]
          (.run db (reify com.apple.foundationdb.TransactionFunction
                     (applyTo [_ tr]
                       (.set tr (ByteBuffer/wrap (.pack (-> (Tuple.)
                                                            (.add "kv")
                                                            (.add key))))
                              (ByteBuffer/wrap (.pack (Tuple/fromBytes (.pack (Tuple. (into-array Object [value])))))))
                       nil)))
          (assoc op :type :ok :value value))

        ;; Pass through nemesis events
        (assoc op :type :info))

      (catch Exception e
        (error e "FoundationDB op failed:" op)
        (assoc op :type :fail :error (.getMessage e)))))


  (teardown! [_ _]
    (info "→ Closing FoundationDB connections")
    ;; No explicit close needed for FDB API, but we can null references
    (when fdb
      nil))))

(defn ->Client
  "Factory for FoundationDB client."
  [test node]
  (map->FDBClient {:test test :node node}))