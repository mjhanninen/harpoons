(ns harpoons.core-test
  (:require [clojure.test :refer [deftest testing is are]]
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
             [thrice thrice thrice])))))

(deftest ^:ignore -<>-extra-tests

  ;; The `as->` macro seems to have some limitations; consider implementing
  ;; `-<>` from scratch.  The following **currently failing** tests document
  ;; the shortcomings.

  (testing "No arguments results in nil"
    #_(is (nil? (-<>)))
    (is (macroexpand-1 '(-<>))))

  ;; `as->` doesn't preserve the metadata
  (testing "Metadata is preserved"
    (is (contains? (meta (-<> ^:foo {} identity)) :foo))))

(deftest some-<>-tests

  (testing "Empty body results in nil"

    (let [s (gensym)]
      (is (= s (some-<> s)))
      (is (= s (some-<> s <>)))
      (is (= nil (some-<> s nil)))
      (is (= s (some-<> s <> <>)))
      (is (= nil (some-<> s nil <>)))
      (is (= :other (some-<> s :other <>))))))

(deftest cond-<>-tests

  (testing "evaluate consequent forms iff condition satisfied"
    (are [x y] (= x y)
      [] (cond-<> [])
      [1] (cond-<> []
            true (conj <> 1))
      [] (cond-<> []
           false (conj <> 1))
      [1 2] (cond-<> []
              true (conj <> 1)
              true (conj <> 2))
      [2] (cond-<> []
            false (conj <> 1)
            true (conj <> 2))
      [1] (cond-<> []
            true (conj <> 1)
            false (conj <> 2))
      [] (cond-<> []
           false (conj <> 1)
           false (conj <> 2))))

  (testing "honor Clojure truth and falsehood"
    (is (= [:kw 'sym "str" true 0 1 () [] {} #{}]
         (cond-<> []
           :kw (conj <> :kw)
           'sym (conj <> 'sym)
           "str" (conj <> "str")
           true (conj <> true)
           false (conj <> false)
           0 (conj <> 0)
           1 (conj <> 1)
           nil (conj <> nil)
           () (conj <> ())
           [] (conj <> [])
           {} (conj <> {})
           #{} (conj <> #{})))))

  (testing "threaded value can be used in condition"
    (is (= #{:foo :bar :baz}
           (cond-<> #{:foo}
             (<> :foo) (conj <> :bar)
             (<> :bar) (conj <> :baz)))))

  (testing "evaluate consequent forms at most once"
    (is (= [[1 1 2] [1 1 2] 3]
           (let [eval-count (atom 0)]
             (cond-<> (swap! eval-count inc)
               <> [<> <> (swap! eval-count inc)]
               (not <>) [<> <> (swap! eval-count inc)]
               <> [<> <> (swap! eval-count inc)])))))

  (testing "threaded value can be used as a body and argument"
    (are [x y] (= y x)
      identity (cond-<> identity
                 true (<> <>))
      identity (cond-<> identity
                 true (apply <> [<>]))
      true (cond-<> identity
             (<> <>) true))))

(deftest ^:missing non-nill-<>-tests (is false))
(deftest ^:missing <>-some-tests (is false))
(deftest ^:missing <>-non-nil-tests (is false))
(deftest ^:missing <>-cond-tests (is false))
(deftest ^:missing <>-cond-tests (is false))
(deftest ^:missing <>-bind-tests (is false))
(deftest ^:missing <>-let-tests (is false))
(deftest ^:missing <>-do-tests (is false))
(deftest ^:missing <>-fx!-tests (is false))

(deftest ^:missing non-nil->-tests

  (testing "No expression results in nil"
    (is (nil? (non-nil-> 42))))

  (is false))

(deftest ^:missing >-some-tests (is false))
(deftest ^:missing >-non-nil-tests (is false))
(deftest ^:missing >-cond-tests (is false))
(deftest ^:missing >-bind-tests (is false))
(deftest ^:missing >-let-tests (is false))
(deftest ^:missing >-do-tests (is false))
(deftest ^:missing >-fx!-tests (is false))

(deftest ^:missing non-nil->>-tests (is false))
(deftest ^:missing >>-some-tests (is false))
(deftest ^:missing >>-non-nil-tests (is false))
(deftest ^:missing >>-cond-tests (is false))
(deftest ^:missing >>-bind-tests (is false))
(deftest ^:missing >>-let-tests (is false))
(deftest ^:missing >>-do-tests (is false))
(deftest ^:missing >>-fx!-tests (is false))

(deftest ^:missing >->>-tests (is false))
(deftest ^:missing >-<>-tests (is false))
(deftest ^:missing >>->-tests (is false))
(deftest ^:missing >>-<>-tests (is false))
(deftest ^:missing <>->-tests (is false))
(deftest ^:missing <>->>-tests (is false))
