(ns clatchet.views
  (:require
   [re-frame.core :as rf]
   [clatchet.subs :as subs]
   [clatchet.events :as events]))

(def <-sub (comp deref re-frame.core/subscribe vector))
(def ->evt (comp re-frame.core/dispatch vector))

(defn plaintext []
  (let [plain (<-sub ::subs/plaintext)]
    [:div
     [:label "plaintext"]
     [:input {:on-change #(->evt ::events/set-plaintext
                                 (-> % .-target .-value))
              :value plain}]
     [:button {:on-click #(->evt ::events/encrypt)}
      "encrypt"]
     ]))

(defn ciphertext []
  (let [cipher (<-sub ::subs/ciphertext)]
    [:div
     [:label "ciphertext"]
     [:input {:on-change #(->evt ::events/set-ciphertext
                                 (-> % .-target .-value))
              :value cipher}]
     [:button {:on-click #(->evt ::events/decrypt)}
      "decrypt"]
     ]))

(defn foreign-key []
  (let [foreign (<-sub ::subs/foreign-key)]
    [:div
     [:label "foreign key"]
     [:input {:on-change #(->evt ::events/set-foreign-key
                                 (-> % .-target .-value))
              :value foreign}]
     [:button {:on-click #(->evt ::events/init-ratchet)}
      "init"]
     [:button {:on-click #(->evt ::events/recv-fk)}
      "recv fk"]
     ]))

(defn control-panel []
  [:div
   [:button
    {:on-click #(->evt ::events/gen-keypair)}
    "Generate Key Pair"]
   [:button
    {:on-click #(->evt ::events/ratchet :send)}
    "Ratchet Send"]
   [:button
    {:on-click #(->evt ::events/ratchet :recv)}
    "Ratchet Recv"]
   ]
  )

(defn internals []
  [:div
   [:pre
    (with-out-str (println (<-sub ::subs/public-key)))]
   #_[:pre
    (with-out-str (println (<-sub ::subs/hash)))]
   ])

(defn main-panel []
  [:div
   [control-panel]
   [:hr]
   [internals]
   [:hr]
   [foreign-key]
   [plaintext]
   [ciphertext]])
