(ns clatchet.events
  (:require
   [re-frame.core :as rf]
   [clatchet.db :as db]
   [clatchet.cofx :as cofx]
   [clatchet.crypto :as crypto]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]))

(rf/reg-event-db
 ::initialize-db
 (fn-traced [_ _]
   db/default-db))

(rf/reg-event-db
 ::set-plaintext
 (fn-traced [db [_ txt]] (assoc db :plaintext txt)))

(rf/reg-event-db
 ::set-ciphertext
 (fn-traced [db [_ txt]] (assoc db :ciphertext txt)))

(rf/reg-event-db
 ::set-foreign-key
 (fn-traced [db [_ txt]] (assoc db :foreign-key txt)))

;; resets root
(rf/reg-event-fx
 ::init-ratchet
 [(rf/inject-cofx ::cofx/gen-keypair)]
 (fn-traced [cofx _]
            (let [db (:db cofx)
                  pair (::cofx/gen-keypair cofx)
                  foreign (:foreign-key db)
                  hash (crypto/hash-keys (:curve db) pair foreign)
                  [root send-chain] (crypto/update-chain hash "send" nil)]

              {:db (assoc db
                          :keypair pair
                          :hash hash
                          :root root
                          :send-chain send-chain)})))

(rf/reg-event-fx
 ::recv-fk
 [(rf/inject-cofx ::cofx/gen-keypair)]
 (fn-traced [cofx _]
            (let [db (:db cofx)
                  foreign (:foreign-key db)
                  hash (crypto/hash-keys (:curve db) (:keypair db) foreign)
                  [root recv-chain] (crypto/update-chain hash "recv" (:root db))
                  pair (::cofx/gen-keypair cofx)
                  hash (crypto/hash-keys (:curve db) pair foreign)
                  [root send-chain] (crypto/update-chain hash "send" root)]

              {:db (assoc db
                          :keypair pair
                          :hash hash
                          :root root
                          :send-chain send-chain
                          :recv-chain recv-chain)})))
;; resets root
(rf/reg-event-fx
 ::gen-keypair
 [(rf/inject-cofx ::cofx/gen-keypair)]
 (fn-traced [cofx _]
            (let [db   (:db cofx)
                  pair (::cofx/gen-keypair cofx)]

              {:db (assoc db
                          :keypair pair
                          :root nil)})))

(defn- extend-keyword
  [word extension]
  (-> word
      name
      (str extension)
      keyword))

(rf/reg-event-db
 ::ratchet
 (fn-traced [db [_ which]]
            (let [chain (extend-keyword which "-chain")
                  msg   (extend-keyword which "-msg-key")
                  keys  (crypto/ratchet (chain db))]

              (assoc db
                     chain (:chain-key keys)
                     msg   (:msg-key   keys)))))

(rf/reg-event-db
 ::encrypt
 (fn-traced [db _]
            (let [{:keys [chain-key msg-key]}
                  (crypto/ratchet (:send-chain db))]
              (assoc db
                     :ciphertext
                     (crypto/encrypt (:plaintext db) msg-key)
                     :send-chain chain-key
                     :send-msg-key msg-key))))

(rf/reg-event-db
 ::decrypt
 (fn-traced [db _]
            (let [{:keys [chain-key msg-key]}
                  (crypto/ratchet (:recv-chain db))]
            (assoc db
                   :plaintext
                   (crypto/decrypt (:ciphertext db) msg-key)
                   :recv-chain chain-key
                   :recv-msg-key msg-key))))

;; Cryption without ratchet

(rf/reg-event-db
 ::just-encrypt
 (fn-traced [db _]
            (assoc db
                   :ciphertext
                   (crypto/encrypt (:plaintext db) (:send-msg-key db)))))

(rf/reg-event-db
 ::just-decrypt
 (fn-traced [db _]
            (assoc db
                   :plaintext
                   (crypto/decrypt (:ciphertext db) (:recv-msg-key db)))))
