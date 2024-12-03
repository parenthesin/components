# Helpers

This document provides a full introduction and usage example of each helper implemented here. Let's start with it!

## state-flow
Integration tests are really important in our development cycle, so this helper aims to help you with [state-flow](https://github.com/nubank/state-flow), an integration testing framework using a state monad in the backend for building and composing flows. You can write multiple flows to build your integration test. By default there are some helpers for `state-flow`, so let's deep dive into each one!

### server/pedestal
The goal of this helper is to extract `io.pedestal.http/service-fn` from `state-flow` context and call `io.pedestal.test/response-for` to simulate HTTP requests.

This helper implements a `request!` function that receives a map with `method`, `uri`, `body`, and `headers` (same for composing a request in pedestal) and starts a flow that *makes* an HTTP request and parses the response. You can see an example of this example [here](https://github.com/parenthesin/components/blob/main/test/integration/parenthesin/schema/system_test.clj#L79) and the context down below:
```clojure
;; inside your test
;; don't forget to import corretly the libraries
(flow "should match the response"
  (match? {:status 200
           :body "yay!"}
          (state-flow.server/request! {:method :post
                                       :uri    "/some/url"
                                       :body   {:something "yay"}})))
```
> This example also uses the [matchers-combinators](https://github.com/nubank/matcher-combinators/) library to validate integration tests.

By default this implementation for `request!` isn't fully prepared to do all HTTP client functionalities. You can see an [implementation example below](#http) for multipart uploads.
### db
This helper exposes a function to directly execute SQL commands on the `state-flow` context database called `execute!`. An example of this implementation is provided [here](https://github.com/parenthesin/components/blob/main/test/integration/parenthesin/db/jdbc_hikari_test.clj) and an example of usage is:
```clojure
(defflow flow-integration-database-test
  {:init (util/start-system!
          {:config (components.config/new-config)
           :database (component/using (components.db/new-database) [:config])})
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "just a simple example"
    (match? [#:something{:id 1
                         :name "cool example"
                         :email "example@email.com"}]
            (state-flow.db/execute! ["select * from something"]))))
```

### http
This helper exposes some functions to set and get an HTTP mock state with `set-http-out-responses!` that receive some responses for mocking, `http-out-requests` to retrieve some HTTP request and `request!` to make an HTTP request based on an HTTP state. You can see an example of implementation [here](https://github.com/parenthesin/components/blob/main/test/integration/parenthesin/http/clj_http_test.clj) and down below:
```clojure

(defflow flow-integration-database-test
  {:init (util/start-system!
          {:config (components.config/new-config)
           :http (http.clj-http/new-http-mock {"https://duckduckgo.com" {:status 200}})
           :webserver (component/using (components.webserver/new-webserver) [:config :http])})
   :cleanup util/stop-system!
   :fail-fast? true}
   (flow "start a system with http mock"
     (state-flow.http/set-http-out-responses! {"https://goosegoosego.com"
                                               {:body {:msg "quack?"}
                                                :status 200}})

     (flow "do request in new existing configured mock response"
       (match? {:status 200
                :body {:msg "quack?"}}
               (state-flow.http/request! {:method :get
                                       :url "https://goosegoosego.com"})))))
```
> This example set a mock for `http-out-request` to validate a request.

In the above example we saw how we can make simple HTTP requests and mock some responses easily, but handling some more complex HTTP client functionalities - like handling multipart upload - can be a little different and we recomend an custom wrapper over [clj-http](https://github.com/dakrone/clj-http) implementation. Look at the example below:
```clojure
(defn clj-http-request!
  [{:keys [uri body multipart] :as opts}]
  (flow "makes http request"
    (-> (cond-> opts
          body (assoc :form-params body)
          (not multipart) (assoc :content-type :json))
        (assoc :as :json
               :url (str "http://localhost:3001" uri))
        clj-http.client/request
        state-flow.api/return)))
```
> This implementation has the same interface of the `request!` in our helpers, but using `clj-http` behind the scenes, this will grant you an fully fledged http client for your integrations tests, but that still uses the components and mocks.

The following here is showing how to deal with multipart upload parameters, handling `http` requests, and returning a state-flow monad for specific usage.

You can see below an example of the implementation of a multipart parameter route for reitit. There we define which elements we can receive, and handle.
```clojure
; example of a reitit route configuration that accepts multipart uploads
["/files" {:swagger {:tags ["files"]}}
    ["/upload"
     {:post {:summary    "upload a file"
             :parameters {:multipart [:map-of :string [:or :string malli/temp-file-part]]}
             :responses  {200 {:body :any}}
             :handler    (fn [{{multipart :multipart} :parameters}]
                           {:status 200
                            :body   {:multipart multipart}})}}]]
```

And then if you want to use it with the custom `clj-http-request!` fn in a flow, you can simply implement it like that:
```clojure
(flow "should accept multipart upload"
    (match? {:status 200 :body any?}
            (clj-http-request! {:uri "/files/upload"
                                :as :json
                                :method :post
                                :multipart [{:name "fiale" :content "Eggplants"}
                                            {:name "file.jpg" :content (clojure.java.io/file "/Users/rafael.delboni/Downloads/images.jpg")}
                                            {:name "file.csv" :content (clojure.java.io/file "/Users/rafael.delboni/Downloads/file.csv")}]})))
```
> In this example, we're handling multipart as some different files loaded from our disk, so remember to have these files correctly loaded!

## logs
Logging is essential to all services, so `components` also implement a helper for logs! This helper uses [timbre](https://github.com/taoensso/timbre), a pure Clojure/Script logging library to provide logging in general with an easy-to-configure interface with pure Clojure data (and that just works).

By default, this helper implements a setup function that receives two parameters: level and stream (to set a `min-level` and configure a stream into an [appender](https://taoensso.github.io/timbre/taoensso.timbre.appenders.core.html)). This setup can be called into a `start-system!` function like the example above:
```clojure
;; your previous imports
(require '[parenthesin.helpers.logs :as logs])

(defn start-system! [system-map]
  ;; your previous implementations
  (logs/setup :info :auto))
```

This helper also implements a macro wrapping `timbre/log!` for better level context and more. To log something you can follow the example:
```clojure
(require '[parenthesin.helpers.logs :as logs])

(logs/log :info :something :received "something!")
```
> You can first of all pass the level of the log followed by extra arguments to be used in this log information.

## malli
This helper implements both start and stop functions to instrumentation with [malli](github.com/metosin/malli) to enable runtime validation of arguments and return values. This helps a lot with general testing using `clojure.text/use-fixtures`. You can see an example of usage below:
```clojure
(require '[parenthesin.helpers.malli :as helpers.malli])

(use-fixtures :once helpers.malli/with-instrumentation)
```
> The `with-instrumentation` function wraps `f` ensuring there has malli collect and instrument started before running it.

## migrations
This helper is a wrapper over [migratus](https://github.com/yogthos/migratus) to create a simple CLI-based API depending on `aero` and `jdbc` to read and connect to the database. The idea is to manage your migrations easily on your service, implementing also functions that can be used inside your `system-start` like the example below:
```clojure
(require '[parenthesin.helpers.migrations :as migrations])

(defn start-system! [system-mapa]
  ;; some other implementations
  (migrations/migrate (migrations/configuration-with-db)))
```
> This function `configuration-with-db` will get your connection from a map!

You can also use some CLI commands like `clj -M:migratus create migration-name`, `clj -M:migratus migrate` and `clj -M:migratus rollback` for example. Remember to define this alias on your `deps.edn`:
```clojure
{:paths ["..."]
 :deps {com.github.parenthesin/components {:mvn/version "0.3.1"}}
 :aliases {:migratus {:main-opts ["-m" "parenthesin.helpers.migrations"]}}}
```

And that's it! Now you can use some helpers also implemented here in `parenthesin/components`! Hope this was helpful and see you next time!

Go [back to main page](intro.md).
