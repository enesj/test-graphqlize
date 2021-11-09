(ns app.server
  (:require
    [hikari-cp.core :as hikari]
    [io.pedestal.http :as http]
    [com.walmartlabs.lacinia.pedestal :as lacinia-pedestal]
    [graphqlize.lacinia.core :as l]
    [honeysql.core :as sql]
    [honeysql.helpers :refer :all :as helpers]
    [mount.core :as mount :refer [defstate]]
    [vlaaad.reveal :as reveal]))

(def db-spec (hikari/make-datasource {:adapter           "postgresql"
                                      :database-name     "assessments"
                                      :server-name       "localhost"
                                      :port-number       5432
                                      :maximum-pool-size 1
                                      :username          "enesj"
                                      :password          "610Pg"}))

(def lacinia-schema (l/schema db-spec))

(defn service [] (->
                   (lacinia-pedestal/service-map lacinia-schema {:graphiql true
                                                                 :port     8080})
                   (assoc ::http/resource-path "/static")
                   (assoc ::http/allowed-origins {:creds true
                                                  :allowed-origins ["http://localhost:3000"
                                                                    "http://localhost:8080"]})))

(defn serve-gql
  []
  (-> (service)
      http/create-server
      http/start))


(defstate app
          :start (serve-gql)
          :stop (http/stop app))

(defn go []
  (mount/start)
  :ready)

(defn stop []
  (mount/stop)
  :stopped)

;(add-tap (reveal/ui))
#_ (tap> {:vlaaad.reveal/command '(clear-output)})

(defn -main []
      (http/start serve-gql))


