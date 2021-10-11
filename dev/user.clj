(ns user
  (:require [app.server :as server]))

(defn go []
  (in-ns 'app.server)
  (server/go))

