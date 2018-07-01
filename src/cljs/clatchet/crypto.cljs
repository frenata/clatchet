(ns clatchet.crypto
  "Cryptography functions that build from sjcl primitives."
  (:require
   [sjcl]))

;;; DH Ratchet

(defonce curves (js->clj sjcl.ecc.curves :keywordize-keys true))

(defn- gen-keypair [curve]
  (sjcl.ecc.elGamal.generateKeys (curve curves)))

(defn- serialize-keypair
  "Only the public key needs to be serialized for transmission."
  [pair]
  {:pub (-> pair .-pub .serialize)
   :sec (-> pair .-sec)})

(defn- deserialize-foreign
  "Deserialize a foreign key."
  [curve foreign]
  (sjcl.ecc.deserialize (clj->js {:point foreign
                                  :curve curve
                                  :secretKey false
                                  :type "elGamal"})))

(def ->keypair
  "For a given elliptic curve, generate a keypair
  and serialize (the public half)."
  (comp serialize-keypair gen-keypair))

(defn hash-keys
  "Hashes a foreign public key against a secret key."
  [curve keypair foreign]
  (let [sec     (:sec keypair)
        foreign (deserialize-foreign curve foreign)]

    (.dh sec foreign)))

;;; Symmetric Ratchet

(defn- mk-slices
  "Transform a list of sizes into a list of slice arguments.
  [8 4 2] -> '([0 8] [8 12] [12 14])"
  ([ranges] (mk-slices ranges 0))
  ([[r & ranges] start]
   (if r
     (cons [start (+ start r)]
           (lazy-seq (mk-slices ranges (+ start r)))))))

(defn- hkdf
  "Extract pseudorandom material from a hash.
  Slices up material into specified lengths."
  [hash size root salt ranges]
  (let [prk (sjcl.misc.hkdf hash size root salt)
        slices (mk-slices ranges)]
    (map (fn [[s e]] (.slice prk s e)) slices)))

(defn update-chain
  "Update a ratchet chain with a new hash."
  [hash name root]
  (hkdf hash 512 root "update chain" [8 8]))

(defn- hmac
  [key]
  (sjcl.misc.hmac. key sjcl.hash.sha512))

(defn ratchet
  "Symmetric ratchet"
  [chain-key]
  (let [kdf (hmac chain-key)]
    ;; TODO hmac encryption seems to expand the key size; bug?
    {:chain-key (.encrypt kdf "new chain")
     :msg-key   (.encrypt kdf "new msg")}))

;;; {En|De}cryption

(defn- json->clj
  [text]
  (js->clj (.parse js/JSON text) :keywordize-keys true))

(defn- clj->json
  [data]
  (.stringify js/JSON (clj->js data)))

(defn- crypt
  "Prepare necessary keys for {en|de}cryption."
  [key]
  (let [[crypt-key auth-key iv] (hkdf key 640 0 "hkdf encryption" [8 8 4])
        params (clj->js {:iv iv})]

    {:crypt-key crypt-key
     :auth-key auth-key
     :params params}))

(defn encrypt
  "Encrypt the plaintext under the send message key."
  [plaintext key]
  (let [{:keys [crypt-key params]} (crypt key)]
    (-> (sjcl.encrypt crypt-key plaintext params)
        json->clj
        :ct)))

(defn decrypt
  "Decrypt the ciphertext under the receive message key."
  [ciphertext key]
  (let [{:keys [crypt-key params]} (crypt key)
        cipher (clj->json {:ct ciphertext})]
    (sjcl.decrypt crypt-key cipher params)))
