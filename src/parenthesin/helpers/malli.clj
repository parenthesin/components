(ns parenthesin.helpers.malli
  (:require [malli.dev.pretty :as pretty]
            [malli.instrument :as mi]))

(defn start! []
  (mi/collect! {:ns (all-ns)})
  (with-out-str (mi/instrument! {:report (pretty/thrower)})))

(defn stop! []
  (with-out-str (mi/unstrument!)))

(defn with-intrumentation
  "Wraps f ensuring there has malli collect and instrument started before running it"
  [f]
  (start!)
  (f)
  (stop!))
