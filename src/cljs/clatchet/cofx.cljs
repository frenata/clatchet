(ns clatchet.cofx
  (:require
   [re-frame.core :as rf]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]
   [clatchet.crypto :as crypto]))

(rf/reg-cofx
 ::gen-keypair
 (fn-traced [cofx _]
   (let [curve (-> cofx :db :curve)]
     (assoc cofx
            ::gen-keypair
            (crypto/->keypair curve)))))
