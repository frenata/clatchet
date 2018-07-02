(ns clatchet.views
  (:require
   [re-frame.core :as rf]
   [clatchet.subs :as subs]
   [clatchet.events.input :as input]
   [clatchet.events.cipher :as cipher]
   [clatchet.events.control :as control]))

(def <-sub (comp deref re-frame.core/subscribe vector))
(def ->evt (comp re-frame.core/dispatch vector))
(defn- input->evt [v] (fn [x] (->evt v (-> x .-target .-value))))

(defn plaintext []
  (let [plain (<-sub ::subs/plaintext)]
    [:div
     [:label "plaintext"]
     [:input {:on-change (input->evt ::input/plaintext)
              :value plain}]
     [:button {:on-click #(->evt ::cipher/encrypt)}
      "encrypt"]]))

(defn ciphertext []
  (let [cipher (<-sub ::subs/ciphertext)]
    [:div
     [:label "ciphertext"]
     [:input {:on-change (input->evt ::input/ciphertext)
              :value cipher}]
     [:button {:on-click #(->evt ::cipher/decrypt)}
      "decrypt"]]))

(defn foreign-key []
  (let [foreign (<-sub ::subs/foreign-key)]
    [:div
     [:label "foreign key"]
     [:input {:on-change (input->evt ::input/foreign-key)
              :value foreign}]
     [:button {:on-click #(->evt ::control/init-ratchet)}
      "init"]
     [:button {:on-click #(->evt ::control/recv-fk)}
      "recv fk"]]))

(defn control-panel []
  [:div
   [:button
    {:on-click #(->evt ::control/gen-keypair)}
    "Generate Key Pair"]
   #_[:button
    {:on-click #(->evt ::control/ratchet :send)}
    "Ratchet Send"]
   #_[:button
    {:on-click #(->evt ::control/ratchet :recv)}
    "Ratchet Recv"]])

(defn internals []
  [:div
   [:pre
    (with-out-str (println (<-sub ::subs/public-key)))]])

(defn info []
  [:div
   [:ol
    [:li "Alice generates a keypair."]
    [:li "Alice sends her public key to Bob."]
    [:li "Bob runs `init` on Alice's key."]
    [:li "Bob encrypts some plaintext."]
    [:li "Bob sends his public key and ciphertext to Alice."]
    [:li "Alice runs `recv fk` on Bob's key."]
    [:li "Alice decrypts Bob's ciphertext."]]
   [:p "Steps 4-7 repeat, with new public keys being transmitted and received only when the 'polarity' of the conversation changes."]])

(defn main-panel []
  [:div
   [control-panel]
   [:hr]
   [internals]
   [:hr]
   [foreign-key]
   [plaintext]
   [ciphertext]
   [:hr]
   [info]])
