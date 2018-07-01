(ns clatchet.db
  (:require
   [sjcl]))

(def default-db
  {:curve sjcl.ecc.curves.c192
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
