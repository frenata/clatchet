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

(defn- deserialize-foreign
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
  [keypair foreign]
  (let [sec     (:sec keypair)
        foreign (deserialize-foreign foreign)]

    (.dh sec foreign)))

(defn- mk-slices
  "Transform a list of sizes into a list of slice arguments.
  [8 4 2] -> '([0 8] [8 12] [12 14])"
  ([ranges] (mk-slices ranges 0))
  ([[r & ranges] start]
   (if r
     (cons [start (+ start r)]
           (lazy-seq (mk-slices ranges (+ start r)))))))

(defn- hkdf
  "Extract and slice up a pseudorandom key from a hash."
  [hash size root salt ranges]
  (let [prk (sjcl.misc.hkdf hash size root salt)
        slices (mk-slices ranges)]
    (println slices)
    (map (fn [[s e]] (.slice prk s e)) slices)))

(defn update-chain
  "Update a ratchet chain."
  [hash name root]
  (hkdf hash 512 root "update chain" [8 8]))
