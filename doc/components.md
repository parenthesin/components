# Components

This document aims to provide you with a full introduction and usage example of each component implemented here. Let's start with it!

## config/aero

This component is responsible for reading and parsing configuration files from our environment based on a `resources/config.edn` file or getting the current [profile](https://github.com/juxt/aero#profile) on environment var `SYSTEM_ENV`. The used library is defined as `A small library for explicit, intentful configuration` and you can configure multiple environment variables for each specific usage.

Look below for an example of `resources/config.edn` file:
```clojure
{:webserver/port #long #or [#env PORT 3001]

 :database {:dbtype "postgres"
            :dbname #or [#env DB-NAME "postgres"]
            :username #or [#env DB-USER "postgres"]
            :password #or [#env DB-PASS "postgres"]
            :host #or [#env DB-HOST "localhost"]
            :port #or [#env DB-PORT 5432]}}
```
We define a `:webserver/port` for example, that can come from a defined `PORT` in our environment, or if it's not defined, it will assume as `3001`. The same logic is valid for each field in the database configuration.

To start our component we can start building our system map:
```clojure
(defn- build-system-map []
  (component/system-map
   ;; some other components
   :config (config/new-config)))
```
> It's normal to see this component as the first one on the system map because it will start with every configuration that comes from our environment.

The `dev` profile is the default for `SYSTEM_ENV`, so if you want to set up another profile you can define your `SYSTEM_ENV` to a new value and a new environment will be configured.

## db/jdbc-hikari
To set up our database that's the component that we use (and it depends on your [connection info data](https://github.com/parenthesin/components/blob/main/test/resources/config.edn#L3) previously configured). This component is using two main libraries to manage our database: [next-jdbc](https://github.com/seancorfield/next-jdbc) and [HikariCP](https://github.com/brettwooldridge/HikariCP).

### next-jdbc
This library is responsible for managing our access to our database (based on JDBC databases). It is *a new low-level Clojure wrapper for JDBC-based access to databases.* You can check more about [here](https://cljdoc.org/d/com.github.seancorfield/next.jdbc/1.3.955/doc/readme).

This API is based on using qualified keywords and transducers having a friendly layout to use and handling your SQL queries. If you want to there's also a helper to manage your SQL queries, turning Clojure data structures into SQL called as [honeysql](https://github.com/seancorfield/honeysql) (both of these libraries are maintained by [Sean Corfield](https://github.com/seancorfield)). You can create really awesome SQL queries and use it with the component like the example below:
```clojure
(require '[honey.sql :as sql])
(require '[honey.sql.helpers :as sql.helpers])
(require '[parenthesin.components.db.jdbc-hikari :as components.database])

(->> (-> (sql.helpers/insert-into :verycooltable)
         (sql.helpers/values [{:something "value"}])
         (sql.helpers/returning :*)
         sql/format)j
     (components.database/execute db))
```

### HikariCP
Described as *a solid, high-performance, JDBC connection pool at last*, HikariCP manages our connection pool provided by our configuration previously, being a very light library for its amazing usage (at roughly 165Kb). You can check more about these optimizations [here](https://github.com/brettwooldridge/HikariCP/wiki/Down-the-Rabbit-Hole).

When the database component is started, a data source will be initialized based on previously configured environment variables for the database, starting a pool using the HikariCP data source.

### How to use it?
You can build your system map using this component after a `config/aero` load (if you're using it for your environment management). Look at the example below of the component startup:
```clojure
(defn- build-system-map []
  (component/system-map
   :config (config/new-config)
   ;; some other components
   :database (component/using (database/new-database) [:config])))
```
> By default your component will try to start using the configuration provided and then if it is not properly set up, it will use the default configuration.

To run any query you can follow the example above for `honeysql` where you can build an SQL query and execute it with `components.database/execute` function from [parenthesin.components.db.jdbc-hikari](https://github.com/parenthesin/components/blob/main/src/parenthesin/components/db/jdbc_hikari.clj).
