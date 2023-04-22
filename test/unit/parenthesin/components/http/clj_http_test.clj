(ns unit.parenthesin.components.http.clj-http-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [com.stuartsierra.component :as component]
            [matcher-combinators.test :refer [match?]]
            [parenthesin.components.http.clj-http :as http.clj-http]
            [schema.test :as schema.test]))

(use-fixtures :once schema.test/validate-schemas)

(defn- create-and-start-system!
  [{:keys [http]}]
  (component/start-system
   (component/system-map :http http)))

(deftest http-mock-component-test
  (testing "HttpMock should return mocked reponses and log requests in the atom"
    (let [system (create-and-start-system!
                  {:http (http.clj-http/new-http-mock
                          {"https://duckduckgo.com" {:status 200}})})]

      (is (match? {:status 200}
                  (http.clj-http/request (:http system) {:url "https://duckduckgo.com"})))

      (is (match? {:status 500}
                  (http.clj-http/request (:http system) {:url "https://google.com"})))

      (is (match? ["https://duckduckgo.com"
                   "https://google.com"]
                  (map :url (deref (get-in system [:http :requests]))))))))
