(ns integration.parenthesin.db.jdbc-hikari-test
  (:require [clojure.test :as clojure.test]
            [com.stuartsierra.component :as component]
            [integration.parenthesin.util :as util]
            [parenthesin.components.config.aero :as components.config]
            [parenthesin.components.db.jdbc-hikari :as components.db]
            [parenthesin.helpers.state-flow.db :as state-flow.db]
            [schema.test :as schema.test]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]))

(clojure.test/use-fixtures :once schema.test/validate-schemas)

(defflow
  flow-integration-database-test
  {:init (util/start-system!
          {:config (components.config/new-config)
           :database (component/using (components.db/new-database) [:config])})
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "creates a table, insert data and checks return in the database"
    (state-flow.db/execute! ["create table if not exists address (
                               id serial primary key,
                               name varchar(32),
                               email varchar(255))"])

    (state-flow.db/execute! ["insert into address(name,email)
                               values('Sam Campos de Milho','sammilhoso@email.com')"])

    (match? [#:address{:id 1
                       :name "Sam Campos de Milho"
                       :email "sammilhoso@email.com"}]
            (state-flow.db/execute! ["select * from address"]))))
