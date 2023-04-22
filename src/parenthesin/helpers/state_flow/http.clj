(ns parenthesin.helpers.state-flow.http
  (:require [parenthesin.components.http.clj-http :as components.http]
            [state-flow.api :as state-flow.api]
            [state-flow.core :as state-flow :refer [flow]]))

(defn set-http-out-responses!
  [responses]
  (flow "set http-out mock responses"
    [http (state-flow.api/get-state (comp :http :webserver))]
    (-> responses
        (components.http/reset-responses! http)
        state-flow.api/return)))

(defn http-out-requests
  ([]
   (http-out-requests identity))
  ([afn]
   (flow "retrieves http request"
     (state-flow.api/get-state (comp afn deref :requests :http)))))

(defn request!
  [commands]
  (flow "makes request using http component"
    [http (state-flow.api/get-state :http)]
    (-> http
        (components.http/request commands)
        state-flow.api/return)))
