(ns graphqlize.lacinia.core
  (:require [com.walmartlabs.lacinia.executor :as executor]
            [com.walmartlabs.lacinia.resolve :as lacinia-resolve]
            [com.walmartlabs.lacinia.schema :as lacinia-schema]
            [com.walmartlabs.lacinia.util :as lacinia-util]
            [graphqlize.lacinia.enum :as l-enum]
            [graphqlize.lacinia.eql :as l-eql]
            [graphqlize.lacinia.input-object :as l-ip-obj]
            [graphqlize.lacinia.object :as l-obj]
            [graphqlize.lacinia.query :as l-query]
            [graphqlize.lacinia.scalar :as l-scalar]
            [honeyeql.core :as heql]
            [honeyeql.db :as heql-db]
            [honeyeql.debug :refer [trace> trace>>]]
            [honeyeql.meta-data :as heql-md]))

(defn- hql-resolver [db-adapter heql-query-fn]
  ^{:tag lacinia-resolve/ResolverResult}
  (fn [context args _]
    ;(trace>> :resolver1 [(executor/selections-tree context) args])
    (let [sel-tree (executor/selections-tree context)
          eql      (-> (:heql-meta-data db-adapter)
                       heql-md/namespace-idents
                       ;(trace> :resolver)
                       (l-eql/generate sel-tree args))]
      (trace>> :lacinia-resolver {:selections-tree sel-tree
                                  :args            args
                                  :eql eql})
      (try
        (->> (heql-query-fn db-adapter eql)
             lacinia-resolve/resolve-as)
        (catch Throwable e
          (trace>> :heql-error e)
          (lacinia-resolve/resolve-as nil (lacinia-util/as-error-map e)))))))

(defn- resolvers [db-adapter]
  {:graphqlize/query-by-primary-key (hql-resolver db-adapter heql/query-single)
   :graphqlize/collection-query     (hql-resolver db-adapter heql/query)
   :graphqlize/collection-delete     (hql-resolver db-adapter heql/delete)
   :graphqlize/collection-update    (hql-resolver db-adapter heql/update)
   :graphqlize/collection-insert    (hql-resolver db-adapter heql/insert)})

(defn schema [db-spec]
  (let [db-adapter     (heql-db/initialize db-spec {:attr/return-as :naming-convention/unqualified-camel-case
                                                    :eql/mode               :eql.mode/strict})
        heql-meta-data (:heql-meta-data db-adapter)
        gql-schema     {:objects (l-obj/generate heql-meta-data)
                        :queries (l-query/generate heql-meta-data)
                        :mutations (l-query/mutations heql-meta-data)
                        :scalars (l-scalar/generate)
                        :enums (l-enum/generate heql-meta-data)
                        :input-objects (l-ip-obj/generate heql-meta-data)}]
    (lacinia-schema/compile
     (lacinia-util/attach-resolvers gql-schema (resolvers db-adapter)))))
