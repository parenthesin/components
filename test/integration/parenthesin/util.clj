(ns integration.parenthesin.util
  (:require [com.stuartsierra.component :as component]
            [malli.dev :as dev]
            [parenthesin.helpers.logs :as logs]
            [pg-embedded-clj.core :as pg-emb]))

(defn- create-and-start-components! [components]
  (->> components
       (reduce (fn [acc [k v]] (into acc [k v])) [])
       (apply component/system-map)
       component/start-system))

(defn start-system!
  [components]
  (fn []
    (dev/start!)
    (logs/setup :debug :auto)
    (pg-emb/init-pg)
    (create-and-start-components! components)))

(defn stop-system!
  [system]
  (component/stop-system system)
  (pg-emb/halt-pg!)
  (dev/stop!))
