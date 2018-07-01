(ns clatchet.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [clatchet.db :as db]
   [clatchet.views :as views]
   [clatchet.config :as config]))

(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))

(defn mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn ^:export init []
  (re-frame/dispatch-sync [::db/initialize])
  (dev-setup)
  (mount-root))
