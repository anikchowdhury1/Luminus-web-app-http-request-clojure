(defproject bibliotheque "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[luminus-log4j "0.1.3"]
                 [org.clojure/clojure "1.10.0"]
                 [selmer "1.12.12"]
                 [markdown-clj "0.9.89"]
                 [ring-middleware-format "0.7.3"]
                 [metosin/ring-http-response "0.9.1"]
                 [bouncer "1.0.0"]
                 [org.webjars.bower/tether "1.4.3"]
                 [org.webjars/bootstrap "4.0.0-beta.3"]
                 [org.webjars/font-awesome "5.0.2"]
                 [org.webjars/jquery "3.2.1"]
                 [org.clojure/tools.logging "0.3.1"]
                 [org.clojure/data.csv "0.1.4"]
                 [yogthos/config "1.1.5"]
                 [org.jsoup/jsoup "1.9.2"]
                 [clj-http "3.10.0"]
                 [clj-time "0.14.2"]
                 [compojure "1.6.1"]
                 [cheshire "5.8.1"]
                 [org.clojure/core.async "0.4.500"]
                 [com.taoensso/timbre "4.10.0"]
                 [funcool/struct "1.1.0"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [mount "0.1.11"]
                 [migratus "0.9.8"]
                 [cprop "0.1.8"]
                 [org.clojure/tools.cli "0.3.5"]
                 [luminus-nrepl "0.1.4"]
                 [luminus-migrations "0.6.3"]
                 [conman "0.5.8"]
                 [org.postgresql/postgresql "9.4-1206-jdbc4"]
                 [org.webjars/webjars-locator-jboss-vfs "0.1.0"]
                 [luminus/ring-ttl-session "0.3.2"]
                 [luminus-immutant "0.2.4"]]

  :min-lein-version "2.0.0"

  :jvm-opts ["-server" "-Dconf=.lein-env"]
  :source-paths ["src/clj"]
  :resource-paths ["resources"]
  :target-path "target/%s/"
  :main ^:skip-aot bibliotheque.core
  :migratus {:store :database
             :db ~(get (System/getenv) "DATABASE_URL")
             :migration-dir "migrations"}


  :plugins [[lein-cprop "1.0.1"]
            [migratus-lein "0.7.0"]
            [lein-immutant "2.1.0"]]

  :profiles
  {:uberjar {
             :omit-source true
             :aot :all
             :jar-name "web-vendor-campaign-library.jar"
             :uberjar-name "web-vendor-campaign.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/test :profiles/test]

   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn"]
                  :dependencies [[prone "1.1.1"]
                                 [ring/ring-mock "0.3.2"]
                                 [ring/ring-devel "1.6.3"]
                                 [pjstadig/humane-test-output "0.8.0"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.19.0"]]
                  
                  :source-paths ["env/dev/clj" "test/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:resource-paths ["env/dev/resources" "env/test/resources"]}
   :profiles/dev {}
   :profiles/test {}})
