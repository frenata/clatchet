(ns clatchet.crypto
  "Convenience functions that wrap around sjcl."
  (:require
   [sjcl]))

(defn- gen-keypair
  [curve]
  (sjcl.ecc.elGamal.generateKeys curve))

(defn- serialize-keypair [pair]
  {:pub (-> pair .-pub .serialize)
   :sec (-> pair .-sec)})

(defn deserialize-foreign
  "Deserialize a foreign key."
  [foreign]
  ;; HACK these details should be parameterized
  (sjcl.ecc.deserialize (clj->js {:point foreign
                                  :curve "c192"
                                  :secretKey false
                                  :type "elGamal"})))

(def ->keypair
  "For a given elliptic curve, generate a keypair
  and serialize it."
  (comp serialize-keypair gen-keypair))

(defn hash-keys
  "Hashes a foreign public key against a secret key."
  [keypair foreign-key]
  (-> keypair
      :sec
      (.dh foreign-key)))
