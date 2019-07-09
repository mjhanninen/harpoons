(ns harpoons.core-test
  (:require [clojure.test :refer [deftest testing is]]
            [harpoons.core :refer :all]))

(deftest -<>-tests

  (testing "Entering expression is evaluated only once"
    (let [a (atom 0)]
      (is (= (-<> (swap! a inc)
               [<> <> <> <> <> <> <>]
               (apply + <>))
             7))))

  (testing "Basic data structures work naturally"
    (let [s (gensym)]
      (is (= (-<> s
               {:l <> :r <>}
               [<> <>]
               #{<>})
             #{[{:l s :r s} {:l s :r s}]}))))

  (testing "Diamond can be used in function position"
    (letfn [(thrice [x]
              [x x x])]
      (is (= (-<> thrice
               (<> <>))
             [thrice thrice thrice]))))

  ;; The `as->` macro seems to have few surprising limitations; consider
  ;; reimplementing from scratch.
  #_
  (comment
    ;; `as->` requires at least one argument
    (testing "No arguments results in nil"
      (is (nil? (-<>))))
    ;; `as->` doesn't preserve the metadata
    (testing "Metadata is preserved"
      (is (contains? (meta (-<> ^:foo {} identity)) :foo)))))

(deftest some-<>-tests

  (testing "Empty body results in nil"
    (let [s (gensym)]
      (is (= (some-<> s <> <>) s))
      (is (= (some-<> s <> <>) s)))))

(deftest cond-<>-tests (is false))
(deftest non-nill-<>-tests (is false))
(deftest <>-some-tests (is false))
(deftest <>-non-nil-tests (is false))
(deftest <>-cond-tests (is false))
(deftest <>-cond-tests (is false))
(deftest <>-bind-tests (is false))
(deftest <>-let-tests (is false))
(deftest <>-do-tests (is false))
(deftest <>-fx!-tests (is false))

(deftest non-nil->-tests

  (testing "No expression results in nil"
    (is (nil? (non-nil-> 42))))

  (is false))

(deftest >-some-tests (is false))
(deftest >-non-nil-tests (is false))
(deftest >-cond-tests (is false))
(deftest >-bind-tests (is false))
(deftest >-let-tests (is false))
(deftest >-do-tests (is false))
(deftest >-fx!-tests (is false))

(deftest non-nil->>-tests (is false))
(deftest >>-some-tests (is false))
(deftest >>-non-nil-tests (is false))
(deftest >>-cond-tests (is false))
(deftest >>-bind-tests (is false))
(deftest >>-let-tests (is false))
(deftest >>-do-tests (is false))
(deftest >>-fx!-tests (is false))

(deftest >->>-tests (is false))
(deftest >-<>-tests (is false))
(deftest >>->-tests (is false))
(deftest >>-<>-tests (is false))
(deftest <>->-tests (is false))
(deftest <>->>-tests (is false))
