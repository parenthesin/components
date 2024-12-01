(ns integration.parenthesin.malli.router-test
  (:require [clojure.test :refer [use-fixtures]]
            [com.stuartsierra.component :as component]
            [integration.parenthesin.util :as util]
            [parenthesin.components.config.aero :as components.config]
            [parenthesin.components.db.jdbc-hikari :as components.db]
            [parenthesin.components.http.clj-http :as components.http]
            [parenthesin.components.router.reitit-malli :as components.router]
            [parenthesin.components.server.reitit-pedestal-jetty :as components.webserver]
            [parenthesin.helpers.malli :as helpers.malli]
            [parenthesin.helpers.state-flow.server.pedestal :as state-flow.server]
            [state-flow.api :refer [defflow]]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.core :as state-flow :refer [flow]]))

(use-fixtures :once helpers.malli/with-instrumentation)

(def test-routes
  [["/plus"
    {:get {:summary "plus with spec query parameters"
           :parameters {:query [:map
                                [:x :int]
                                [:y :int]]}
           :responses {200 {:body [:map [:total :int]]}}
           :handler (fn [{{{:keys [x y]} :query} :parameters}]
                      {:status 200
                       :body {:total (+ x y)}})}
     :post {:summary "plus with spec body parameters"
            :parameters {:body [:map
                                [:x :int]
                                [:y :int]]}
            :responses {200 {:body [:map [:total :int]]}}
            :handler (fn [{{{:keys [x y]} :body} :parameters}]
                       {:status 200
                        :body {:total (+ x y)}})}}]])

(defflow
  flow-integration-webserver-test
  {:init (util/start-system!
          {:config (components.config/new-config)
           :http (components.http/new-http-mock {})
           :database (component/using (components.db/new-database) [:config])
           :router (components.router/new-router test-routes)
           :webserver (component/using (components.webserver/new-webserver)
                                       [:config :http :router :database])})
   :cleanup util/stop-system!
   :fail-fast? true}
  (flow "should interact test-routes"
    (flow "should sum the get params x & y via get"
      (match? {:status 200
               :body {:total 7}}
              (state-flow.server/request! {:method  :get
                                           :uri     (str "/plus?x=" 3 "&y=" 4)})))
    (flow "should sum the body x & y via post"
      (match? {:status 200
               :body {:total 7}}
              (state-flow.server/request! {:method  :post
                                           :uri     "/plus"
                                           :body    {:x 4
                                                     :y 3}})))))
