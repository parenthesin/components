(ns parenthesin.helpers.migrations
  (:require [migratus.core :as migratus]
            [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [parenthesin.components.config.aero :as config.aero])
  (:gen-class))

(defn get-connection
  ([]
   (get-connection {}))
  ([input-map]
   (let [{:keys [username] :as db-spec} (-> (config.aero/read-config input-map) :database)
         jdbc-url (connection/jdbc-url (assoc db-spec :user username))]
     (jdbc/get-connection {:jdbcUrl jdbc-url}))))

(def configuration
  {:store         :database
   :migration-dir "migrations/"})

(defn configuration-with-db
  ([]
   (configuration-with-db {}))
  ([input-map]
   (assoc configuration :db {:connection (get-connection input-map)})))

(defn init
  [config]
  (migratus/init config))

(defn migrate
  [config]
  (migratus/migrate config))

(defn up
  [config & args]
  (migratus/up config args))

(defn down
  [config & args]
  (migratus/down config args))

(defn create
  [config migration-name]
  (migratus/create config migration-name))

(defn rollback
  [config]
  (migratus/rollback config))

(defn pending-list
  [config]
  (migratus/pending-list config))

(defn migrate-until-just-before
  [config & args]
  (migratus/migrate-until-just-before config args))

(defn -main
  [& args]
  (let [arg (first args)]
    (cond
      (= arg "init") (init (configuration-with-db))
      (= arg "migrate") (migrate (configuration-with-db))
      (= arg "up") (up (configuration-with-db) (rest args))
      (= arg "down") (down (configuration-with-db) (rest args))
      (= arg "create") (create configuration (second args))
      (= arg "rollback") (rollback (configuration-with-db))
      (= arg "pending-list") (pending-list (configuration-with-db))
      (= arg "until-just-before") (migrate-until-just-before (configuration-with-db)
                                                             (rest args))
      :else
      (throw (Exception. (str "Command not found " arg))))))
