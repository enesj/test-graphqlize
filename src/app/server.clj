(ns app.server
  (:require
    [hikari-cp.core :as hikari]
    [io.pedestal.http :as server]
    [com.walmartlabs.lacinia.pedestal :as lacinia-pedestal]
    [graphqlize.lacinia.core :as l]))

(def db-spec (hikari/make-datasource {:adapter           "postgresql"
                                      :database-name     "postgres"
                                      :server-name       "localhost"
                                      :port-number       5432
                                      :maximum-pool-size 1
                                      :username          "enesj"
                                      :password          "610Pg"}))

(def lacinia-schema (l/schema db-spec))

(def service (assoc
               (lacinia-pedestal/service-map lacinia-schema {:graphiql true
                                                             :port     8080})
               ::server/resource-path "/static"))

(defonce runnable-service (server/create-server service))

(.addShutdownHook (Runtime/getRuntime) (Thread. (fn []
                                                    (server/stop runnable-service)
                                                    (.close db-spec))))

(defn -main []
      (server/start runnable-service))
