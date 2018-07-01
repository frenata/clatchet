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
 (fn-traced [db [_ name]] (assoc db :plaintext name)))

(rf/reg-event-db
 ::set-ciphertext
 (fn-traced [db [_ name]] (assoc db :ciphertext name)))

(rf/reg-event-db
 ::set-foreign-key
 (fn-traced [db [_ name]] (assoc-in db [:foreign-key :text] name)))

(rf/reg-event-fx
 ::init-ratchet
 [(rf/inject-cofx ::cofx/gen-keypair)]
 (fn-traced [cofx _]
            (let [db (:db cofx)
                  pair (::cofx/gen-keypair cofx)
                  foreign (get-in db [:foreign-key :text])
                  hash (crypto/hash-keys pair foreign)
                  [root send-chain] (crypto/update-chain hash "send" (:root db))]

              {:db (assoc db
                          :keypair pair
                          :hash hash
                          :root root
                          :send-chain send-chain)})))

(rf/reg-event-fx
 ::recv-fk
 [(rf/inject-cofx ::cofx/gen-keypair)]
 (fn-traced [cofx _]
            ;; hash new fk against existing pair
            ;; ratchet recieve
            ;; generate new pair
            ;; hash fk against new pair
            ;; ratchet send
            (let [db (:db cofx)
                  foreign (get-in db [:foreign-key :text])
                  hash (crypto/hash-keys (:keypair db) foreign)
                  [root recv-chain] (crypto/update-chain hash "recv" (:root db))
                  pair (::cofx/gen-keypair cofx)
                  hash (crypto/hash-keys pair foreign)
                  [root send-chain] (crypto/update-chain hash "send" root)]

              {:db (assoc db
                          :keypair pair
                          :hash hash
                          :root root
                          :send-chain send-chain
                          :recv-chain recv-chain)})))

(rf/reg-event-db
 ::hash-keys
 (fn-traced [db [_ foreign]]
            (let [keypair (:keypair db)
                  hash (crypto/hash-keys keypair foreign)]
              (assoc db :hash hash))))

(rf/reg-event-fx
 ::gen-keypair
 [(rf/inject-cofx ::cofx/gen-keypair)]
 (fn-traced [cofx _]
            (let [db   (:db cofx)
                  pair (::cofx/gen-keypair cofx)]

              {:db (assoc db :keypair pair)})))

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
