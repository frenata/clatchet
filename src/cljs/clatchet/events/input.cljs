(ns clatchet.events.input
  (:require
   [re-frame.core :as rf]
   [day8.re-frame.tracing :refer-macros [fn-traced defn-traced]]))

(rf/reg-event-db
 ::plaintext
 (fn-traced [db [_ txt]] (assoc db :plaintext txt)))

(rf/reg-event-db
 ::ciphertext
 (fn-traced [db [_ txt]] (assoc db :ciphertext txt)))

(rf/reg-event-db
 ::foreign-key
 (fn-traced [db [_ txt]] (assoc db :foreign-key txt)))
