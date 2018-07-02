(ns clatchet.events.control
  (:require
   [re-frame.core :as rf]
   [clatchet.cofx :as cofx]
   [clatchet.crypto :as crypto]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]))

(rf/reg-event-fx
 ::init-ratchet
 [(rf/inject-cofx ::cofx/gen-keypair)]
 (fn-traced [cofx _]
            (let [db                (:db cofx)
                  pair              (::cofx/gen-keypair cofx)
                  foreign           (:foreign-key db)
                  hash              (crypto/hash-keys (:curve db) pair foreign)
                  [root send-chain] (crypto/update-chain hash nil)]

              {:db (assoc db
                          :keypair pair
                          :hash hash
                          :root root
                          :send-chain send-chain)})))

(rf/reg-event-fx
 ::recv-fk
 [(rf/inject-cofx ::cofx/gen-keypair)]
 (fn-traced [cofx _]
            (let [db                (:db cofx)
                  foreign           (:foreign-key db)
                  hash              (crypto/hash-keys (:curve db)
                                                      (:keypair db)
                                                      foreign)
                  [root recv-chain] (crypto/update-chain hash (:root db))
                  ;; generate new pair and hash again
                  pair              (::cofx/gen-keypair cofx)
                  hash              (crypto/hash-keys (:curve db)
                                                      pair
                                                      foreign)
                  [root send-chain] (crypto/update-chain hash root)]

              {:db (assoc db
                          :keypair pair
                          :hash hash
                          :root root
                          :send-chain send-chain
                          :recv-chain recv-chain)})))
(rf/reg-event-fx
 ::gen-keypair
 [(rf/inject-cofx ::cofx/gen-keypair)]
 (fn-traced [cofx _]
            (let [db   (:db cofx)
                  pair (::cofx/gen-keypair cofx)]

              {:db (assoc db
                          :keypair pair
                          :root nil)})))

#_(defn- extend-keyword
  [word extension]
  (-> word
      name
      (str extension)
      keyword))

#_(rf/reg-event-db
 ::ratchet
 (fn-traced [db [_ which]]
            (let [chain (extend-keyword which "-chain")
                  msg   (extend-keyword which "-msg-key")
                  keys  (crypto/ratchet (chain db))]

              (assoc db
                     chain (:chain-key keys)
                     msg   (:msg-key   keys)))))
