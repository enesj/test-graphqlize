# app/graphqlize

1. Create Postgres database (eg. "assessments")
2. There are two backup files in `/resources/SQL/assessments/`  folder (data.sql and schema.sql)
3. Execute first schema.sql and then data.sql in public schema of this DB
4. This will populate the db-schema with tables and other objects, and seed the tables with data.
5. Fill this map in `src/app/server.clj` namespace:
   `(def db-spec (hikari/make-datasource {:adapter           "postgresql"
   :database-name     "assessments"
   :server-name       "localhost"
   :port-number       5432
   :maximum-pool-size 1
   :username          "..."
   :password          "..."}))`
   with your credentials, db and server name.
6. Run `clj -M:dev:test` from project folder
7. Type `(go)` on `user=>` prompt
   ---->>
   `[main] INFO org.eclipse.jetty.server.Server - Started @10079ms
   :ready`
8. If Graphiql interface is shown on http://localhost:8080/ you are ready to test `meta-dashboard` application
