(ns parenthesin.components.server.reitit-pedestal-jetty
  (:require [com.stuartsierra.component :as component]
            [io.pedestal.http :as server]
            [io.pedestal.interceptor :refer [interceptor]]
            [parenthesin.helpers.logs :as logs]
            [reitit.pedestal :as pedestal]))

(defn- add-system [service]
  (interceptor {:enter (fn [context]
                         (assoc-in context
                                   [:request :components] service))}))

(defn system-interceptors
  "Extend to service's interceptors to include one to inject the components
  into the request object"
  [service-map service]
  (update-in service-map
             [::server/interceptors]
             #(vec (->> % (cons (add-system service))))))

(defn base-service [port allowed-origins]
  (let [base {::server/port port
              ::server/type :jetty
              ::server/host "0.0.0.0"
              ::server/join? true
              ;; no pedestal routes
              ::server/routes []
              ;; allow serving the swagger-ui styles & scripts from self
              ::server/secure-headers {:content-security-policy-settings
                                       {:default-src "'self'"
                                        :style-src "'self' 'unsafe-inline'"
                                        :script-src "'self' 'unsafe-inline'"
                                        :img-src "'self' 'unsafe-inline' data: https://validator.swagger.io"}}}]
    (if allowed-origins
      (assoc base ::server/allowed-origins {:creds true :allowed-origins allowed-origins})
      base)))

(defn dev-init [service-map router]
  (-> service-map
      (merge {:env                     :dev
              ;; do not block thread that starts web server
              ::server/join?           false
              ;; Content Security Policy (CSP) is mostly turned off in dev mode
              ::server/secure-headers  {:content-security-policy-settings {:object-src "none"}}
              ;; Allow any origin in dev mode
              ::server/allowed-origins {:creds true :allowed-origins (constantly true)}})
      ;; Wire up interceptor chains
      (server/default-interceptors)
      (pedestal/replace-last-interceptor router)
      (server/dev-interceptors)))

(defn prod-init [service-map router]
  (-> service-map
      (merge {:env :prod})
      (server/default-interceptors)
      (pedestal/replace-last-interceptor router)))

(defrecord WebServer [config router]
  component/Lifecycle
  (start [this]
    (let [{:webserver/keys [port allowed-origins]
           :keys [env]} (:config config)
          init-fn (if (= env :dev) dev-init prod-init)]
      (logs/log :info :webserver :start {:env env :port port})
      (assoc this :webserver
             (-> (base-service port allowed-origins)
                 (init-fn (:router router))
                 (system-interceptors this)
                 (server/create-server)
                 (server/start)))))

  (stop [this]
    (logs/log :info :webserver :stop)
    (server/stop (:webserver this))
    (dissoc this :webserver)
    this))

(defn new-webserver []
  (map->WebServer {}))

