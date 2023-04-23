(ns build
  (:refer-clojure :exclude [test])
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as dd]))

(def default-lib 'com.github.parenthesin/components)
(def default-version "0.0.1-SNAPSHOT")
(def class-dir "target/classes")

(defn- jar-opts
  [{:keys [lib version jar-file] :as opts}]
  (let [actual-lib (or lib default-lib)
        actual-version (or version default-version)
        actual-jar-file (or jar-file (format "target/%s-%s.jar"
                                             actual-lib
                                             actual-version))]
    (assoc opts
           :lib actual-lib
           :version actual-version
           :jar-file actual-jar-file
           :scm {:tag (str "v" actual-version)}
           :basis (b/create-basis {})
           :class-dir class-dir
           :target "target"
           :src-dirs ["src"])))

(defn jar "Build Jar." [opts]
  (b/delete {:path "target"})
  (let [{:keys [jar-file lib version] :as opts} (jar-opts opts)]
    (println "\nWriting pom.xml for" lib "on version" version)
    (b/write-pom opts)
    (println "\nCopying source from" class-dir)
    (b/copy-dir {:src-dirs ["resources" "src"] :target-dir class-dir})
    (println "\nBuilding JAR on" jar-file)
    (b/jar opts))
  opts)

(defn deploy "Deploy the Jar to Clojars." [opts]
  (let [{:keys [jar-file version] :as opts} (jar-opts opts)
        artifact (b/resolve-path jar-file)
        pom-file (b/pom-path (select-keys opts [:lib :class-dir]))]
    (println "\nDeploying JAR" artifact "for pom" pom-file "on version" version)
    (dd/deploy {:installer :remote :artifact artifact :pom-file pom-file}))
  opts)
