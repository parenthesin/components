(ns parenthesin.components.db.jdbc-hikari
  (:require [com.stuartsierra.component :as component]
            [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [parenthesin.helpers.logs :as logs])
  (:import (com.zaxxer.hikari HikariDataSource)))

(defprotocol DatabaseProvider
  (execute [self sql-params]
    [self sql-params opts]
    "Low-level API to execute a command in the database"))

(defrecord Database [config ^HikariDataSource datasource]
  component/Lifecycle
  (start [this]
    (let [db-spec (get-in config [:config :database])
          jdbc-url (connection/jdbc-url (dissoc db-spec :username :password))
          db-auth (select-keys db-spec [:username :password])]
      (logs/log :info :database :start jdbc-url)
      (if datasource
        this
        (assoc this :datasource (connection/->pool HikariDataSource (assoc db-auth :jdbcUrl jdbc-url))))))

  (stop [this]
    (logs/log :info :database :stop)
    (if datasource
      (do
        (.close datasource)
        (assoc this :datasource nil))
      this))

  DatabaseProvider
  (execute [this sql-params]
    (jdbc/execute! (:datasource this) sql-params))
  (execute [this sql-params opts]
    (jdbc/execute! (:datasource this) sql-params opts)))

(defn new-database []
  (map->Database {}))
