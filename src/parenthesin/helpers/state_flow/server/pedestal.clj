(ns parenthesin.helpers.state-flow.server.pedestal
  (:require [cheshire.core :as json]
            [clojure.string :as string]
            [io.pedestal.test :as pt]
            [ring.util.codec :as codec]
            [state-flow.api :as state-flow.api]
            [state-flow.core :as state-flow :refer [flow]]))

(defn- do-request [service-fn verb route body headers]
  (let [headers-with-default (merge {"Content-Type" "application/json"} headers)
        content-type (get headers-with-default "Content-Type")
        encoded-body (case content-type
                       "application/json" (json/encode body)
                       "application/x-www-form-urlencoded" (codec/form-encode body)
                       body)]
    (pt/response-for service-fn verb route :headers headers-with-default :body encoded-body)))

(defn- parsed-response
  [{:keys [headers body] :as request}]
  (if (string/includes? (get headers "Content-Type") "application/json")
    (assoc request :body (json/decode body true))
    request))

(defn request!
  "Flow that make http request using `:io.pedestal.http/service-fn`, not a fully
  featured http client, currently only supports content type `application/json`
  and `application/x-www-form-urlencoded`."
  [{:keys [method uri body headers]}]
  (flow "makes http request"
    [service-fn (state-flow.api/get-state (comp :io.pedestal.http/service-fn :webserver :webserver))]
    (-> service-fn
        (do-request method uri body headers)
        parsed-response
        state-flow.api/return)))
