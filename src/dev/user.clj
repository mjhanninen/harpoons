(ns user
  (:require
   [criterium.core :as criterium :refer [bench]]
   [clojure.test.check :refer [quick-check]]
   [clojure.test.check.generators :as gen]
   [clojure.test.check.properties :as prop]))
