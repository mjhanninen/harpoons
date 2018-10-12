(ns pandoras.unstable-bench
  (:require [clojure.set :as set]
            [criterium.core :refer [bench quick-bench with-progress-reporting]]
            [citius.core :refer [compare-perf with-bench-context]]
            [pandoras.unstable :refer :all]))

(defn bench-map-of
  []
  (let [a 1 b 2 c 3 d 4]
    (with-progress-reporting
      (quick-bench (map-of a b c d)))))

;;;
;;; preimage vs. clojure.set/rename-keys
;;;

(defn build-test-set
  [map-size proj-size]
  {:pre [(pos? map-size) (pos? proj-size) (>= map-size proj-size)]}
  (let [m (into {}
                (map (fn [i] [i i]))
                (range map-size))
        p (into {}
                (map (fn [i] [i (-> i (inc) (mod proj-size))]))
                (range proj-size))
        inv-p (into {}
                    (map (juxt val key))
                    p)]
    (map-of m p inv-p)))

(defn run-preimage-bench
  [map-size proj-size]
  (let [{:keys [m p inv-p]} (build-test-set map-size proj-size)]
    (assert (= (preimage m p) (set/rename-keys m inv-p)))
    (with-bench-context ["Clojure.set/rename-keys" "pandoras.unstable/preimage"]
      (compare-perf ""
        (set/rename-keys m inv-p) (preimage m p)))))
