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
 (fn-traced [cofx _]
            (let [db (:db cofx)
                  obj (crypto/deserialize-foreign (get-in db [:foreign-key :text]))]

              {:db (assoc-in db [:foreign-key :obj] obj)
               :dispatch [::hash-keys obj]})))

(rf/reg-event-db
 ::hash-keys
 (fn-traced [db [_ foreign]]
            (let [keypair (:keypair db)]
              (assoc db :hash (crypto/hash-keys keypair foreign)))))

(rf/reg-event-fx
 ::gen-keypair
 [(rf/inject-cofx ::cofx/gen-keypair)]
 (fn-traced [cofx _]
            (let [db   (:db cofx)
                  pair (::cofx/gen-keypair cofx)]

              {:db (assoc db :keypair pair)})))
