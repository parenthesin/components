# Components

This document provides a full introduction and usage example of each component implemented here. Let's start with it!

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

## http/clj-http
To provide an HTTP client, that's the right component, using [clj-http](https://github.com/dakrone/clj-http) as the main library defined as *an idiomatic clojure http client wrapping the [apache client](https://hc.apache.org/)*. The implementation of this component is divided into two main options: an `Http` component that handles real requests and an `HttpMock` component to help you build some integration tests easily, mocking your requests.

A `request-fn` is also defined as a function to perform HTTP requests expecting a map containing the keys `:url` and `:method`. You can see an example of the implementation of this component below:
```clojure
(defn- build-system-map []
  (component/system-map
   ;; some other components
   :http (http/new-http)))
```

An example of usage for this component is:
```clojure
(require '[parenthesin.components.http.clj-http :as components.http])

(->> {:url "https://api.coindesk.com/v1/bpi/currentprice.json"
	  :as :json
	  :methhod :get}
	  (components.http/request http) ;; http is your component!
	  :body)
```
> Then you can easily make general requests and parse these results as you want!

## routers
We've two implementations of `routers`, and both perform a spec/schema validation for your requests to improve your development in general. As you know, type hints and type inference in Clojure are optional, but we also highly recommend it to provide production-ready code!

### reitit-malli
To create our router and specifically start routing our app we use [reitit](https://github.com/metosin/reitit), *a fast data-driven routing library for Clojure/Script* with bi-directional routing and also have a [pluggable coercion](https://cljdoc.org/d/metosin/reitit/0.7.2/doc/coercion/coercion-explained). In this component for example we're using [malli](https://github.com/metosin/malli), *high-performance data-driven data specification library for Clojure/Script.*

This component implements a simple router with some details:
- Exceptions are logged in by default with the log helper;
- [muuntaja](https://github.com/metosin/muuntaja) is used for fast HTTP API format negotiation, encoding and decoding;
- [reitit-swagger](https://github.com/metosin/reitit/tree/master/modules/reitit-swagger) is implemented by default, so you also have a Swagger interface to interact with (and you can configure it with multiple tags if you want to);
- [reitit-pedestal](https://github.com/metosin/reitit/tree/master/modules/reitit-pedestal) is used for routing interceptors and [reitit-ring](https://github.com/metosin/reitit/tree/master/modules/reitit-ring) to manage routes and creating some handlers;

You can previously define some routes to start your interaction like:
```clojure
(require '[reitit.swagger :as swagger])

(def routes
  [["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "example"
                            :description "small example of usage"}}
           :handler (swagger/create-swagger-handler)}}]

   ["/something"
    {:swagger {:tags ["something"]}}

    ["/internal"
     {:get {:summary "get all wallet entries and current total"
            :responses {200 {:body [:map [:something string?]]}
                        500 {:body :string}}
            :handler ports.http-in/some-handler}}]]])
```
> You can just define a vector of values and map your entries!

And now, you can build your system map to perform a proper router initialization!
```clojure
(defn- build-system-map []
  (component/system-map
   ;; some other components
   :router (router/new-router routes))) ;; routes are defined above!
```
> That's it! You only have to pass the `routes` defined above as this amazing vector.

### reitit-schema
Very similar to `reitit-malli` but using [schema](https://github.com/plumatic/schema) for declarative data description and validation. The main difference between *malli* and *schema* is that *schema* uses macros instead of *annotations* as *malli*! So when you have to define a new schema, you have to import the library manually and use its macro.

This component implements a simple router with some details:
- Exceptions are logged in by default with the log helper;
- [muuntaja](https://github.com/metosin/muuntaja) is used for fast HTTP API format negotiation, encoding and decoding;
- [reitit-swagger](https://github.com/metosin/reitit/tree/master/modules/reitit-swagger) is implemented by default, so you also have a Swagger interface to interact with (and you can configure it with multiple tags if you want to);
- [reitit-pedestal](https://github.com/metosin/reitit/tree/master/modules/reitit-pedestal) is used for routing interceptors and [reitit-ring](https://github.com/metosin/reitit/tree/master/modules/reitit-ring) to manage routes and create some handlers;
- The only difference in this implementation in comparison to `reitit-malli` is the `coercion` library used: `reitit.schema/coercion` instead of `reitit.malli/coercion`

You can previously define some routes to start your interaction like:
```clojure
(require '[reitit.swagger :as swagger])
(require '[schema.core :as s])

(s/defschema Something
  {:something s/Str})

(def routes
  [["/swagger.json"
    {:get {:no-doc true
           :swagger {:info {:title "example"
                            :description "small example of usage"}}
           :handler (swagger/create-swagger-handler)}}]

   ["/something"
    {:swagger {:tags ["something"]}}

    ["/internal"
     {:get {:summary "get all wallet entries and current total"
            :responses {200 {:body Something}
                        500 {:body s/Str}}
            :handler ports.http-in/some-handler}}]]])
```
> You have to define your schema using the macro `defschema`!

And now, you can build your system map to perform a proper router initialization!
```clojure
(defn- build-system-map []
  (component/system-map
   ;; some other components
   :router (router/new-router routes))) ;; routes are defined above!
```
> That's it! You only have to pass the `routes` defined above as this amazing vector.

## server/reitit-pedestal-jetty
This component directly depends on one of each type of the components defined previously (config, db, http, router) and starts a web server with all components injected in the HTTP context. The web port comes from the `resources/config.edn` like the example below:
```clojure
{:webserver/port #long #or [#env PORT 3001]}
```
> In the example above the web server port will be loaded in your environment or 3001 by default!

Using [reitit](https://github.com/metosin/reitit) and [pedestal](https://github.com/pedestal/pedestal) to perform a secure and healthy web server initialization, we have two main functions managing our environment:
- `prod-init`: receives a service map and a router, starting the server with the default interceptors from `io.pedestal.http`;
- `dev-init`: receives a service map and a router, starting the server with the default and dev interceptors from `io.pedestal.http` and setting up manually the `secure-headers` and `allowed-origins`;

By default, the base service uses `:jetty` for server type, setting `join` to true, defining some secure headers for `content-security-policy-settings`, and also configuring `allowed-origins` if necessary. Logs are also configured to server start and stop.

To build your system map to perform a proper web server initialization we can define:
```clojure
;; remember: this component directly depends on one of each type of the component
(defn- build-system-map []
  (component/system-map
   :config (config/new-config)
   :http (http/new-http)
   :router (router/new-router routes/routes)
   :database (component/using (database/new-database) [:config])
   :webserver (component/using (webserver/new-webserver) [:config :http :router :database])))
```

And now to start completely your system you can define a function:
```clojure
;; this atom will manage our system state
(def system-atom (atom nil))

(defn start-system! [system-map]
  (->> system-map
       component/start
       (reset! system-atom)))

;; then, to start your system
(start-system! (build-system-map))
```

And that's it! Now you can properly understand the base of each component - opening possibilities to start building your Clojure services! Open a parentheses and deep dive into this world.

If you want, you can check more about some [helpers](helpers.md) (like the logs helper mentioned above)!
