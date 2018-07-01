(ns clatchet.subs
  (:require
   [re-frame.core :as rf]))

(rf/reg-sub
 ::plaintext
 (fn [db _]
   (:plaintext db)))

(rf/reg-sub
 ::ciphertext
 (fn [db _]
   (:ciphertext db)))

(rf/reg-sub
 ::foreign-key
 (fn [db _]
   (:foreign-key db)))

#_(rf/reg-sub
 ::hash
 (fn [db _]
   (:hash db)))

(rf/reg-sub
 ::public-key
 (fn [db _]
   (some-> db
           :keypair
           :pub
           .-point)))
