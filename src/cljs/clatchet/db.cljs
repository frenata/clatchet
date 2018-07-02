(ns clatchet.db
  (:require
   [re-frame.core :as rf]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]))

(def default-db
  {:curve :c521
   :keypair nil
   :foreign-key nil
   :hash nil
   :root nil
   :recv-chain nil
   :send-chain nil
   :recv-msg-key nil
   :send-msg-key nil
   :plaintext "re-frame"
   :ciphertext ""})

(rf/reg-event-db
 ::initialize
 (fn [_ _]
   default-db))
