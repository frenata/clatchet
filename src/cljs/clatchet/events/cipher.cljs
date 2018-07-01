(ns clatchet.events.cipher
  (:require
   [re-frame.core :as rf]
   [clatchet.crypto :as crypto]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]))

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

#_(rf/reg-event-db
 ::just-encrypt
 (fn-traced [db _]
            (assoc db
                   :ciphertext
                   (crypto/encrypt (:plaintext db) (:send-msg-key db)))))

#_(rf/reg-event-db
 ::just-decrypt
 (fn-traced [db _]
            (assoc db
                   :plaintext
                   (crypto/decrypt (:ciphertext db) (:recv-msg-key db)))))
