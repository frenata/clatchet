(ns clatchet.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::plaintext
 (fn [db _]
   (:plaintext db)))

(re-frame/reg-sub
 ::ciphertext
 (fn [db _]
   (:ciphertext db)))

(re-frame/reg-sub
 ::foreign-key
 (fn [db _]
   (get-in db [:foreign-key :text])))

(re-frame/reg-sub
 ::hash
 (fn [db _]
   (:hash db)))

(re-frame/reg-sub
 ::public-key
 (fn [db _]
   (some-> db
           :keypair
           :pub
           .-point)))
