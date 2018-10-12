(defproject codebrutale/harpoons "0.1.0-SNAPSHOT"
  :description "Composable threading macros"
  :url "https://github.com/codebrutale/harpoons"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]]
  :source-paths ["src/lib"]
  :test-paths ["src/test"]
  :plugins [[lein-codox "0.10.5"]]
  :codox {:source-paths ["src/lib"]}
  :profiles {:dev {:dependencies [[citius "0.2.4"]
                                  [criterium "0.4.4"]
                                  [org.clojure/test.check "0.9.0"]]
                   :source-paths ["src/dev" "src/test" "src/bench"]}
             :1.7 {:dependencies [[org.clojure/clojure "1.7.0"]]}
             :1.8 {:dependencies [[org.clojure/clojure "1.8.0"]]}}
  :aliases {"with-test-profiles" ["with-profiles" "dev:dev,1.8:dev,1.7"]
            "test-all" ["with-test-profiles" "test"]})
