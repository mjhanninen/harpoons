(ns pandoras.unstable-test
  (:require [clojure.set :as set]
            [clojure.test :refer [deftest testing is]]
            [clojure.test.check.clojure-test :refer [defspec]]
            [clojure.test.check.generators :as gen]
            [clojure.test.check.properties :as prop]
            [pandoras.unstable :refer :all]))

(deftest void-tests
  (testing "always returns nil"
    (is (= (void) nil))
    (is (= (void 1) nil))
    (is (= (void 1 2) nil))
    (is (= (void 1 2 3) nil))))

(deftest non-nil-tests
  (testing "(non-nil x) should be x for all x"
    (is (= (non-nil nil) nil))
    (is (= (non-nil true) true))
    (is (= (non-nil false) false))
    (is (= (non-nil 1) 1))
    (is (= (non-nil "") ""))
    (is (= (non-nil 'sym) 'sym))
    (is (= (non-nil :kw) :kw))
    (is (= (non-nil '()) '()))
    (is (= (non-nil []) []))
    (is (= (non-nil {}) {}))
    (is (= (non-nil #{}) #{}))
    (let [e (Exception. "")]
      (is (= (non-nil e) e))))
  (testing "stops at first non-nil x"
    (is (= (non-nil nil nil :reached) :reached))
    (is (= (non-nil nil true :not-reached) true))
    (is (= (non-nil nil false :not-reached) false))
    (is (= (non-nil nil 1 :not-reached) 1))
    (is (= (non-nil nil "" :not-reached) ""))
    (is (= (non-nil nil 'sym :not-reached) 'sym))
    (is (= (non-nil nil :kw :not-reached) :kw))
    (is (= (non-nil nil '() :not-reached) '()))
    (is (= (non-nil nil [] :not-reached) []))
    (is (= (non-nil nil {} :not-reached) {}))
    (is (= (non-nil nil #{} :not-reached) #{}))
    (let [e (Exception. "")]
      (is (= (non-nil e :not-reached) e))))
  (testing "works over different arities"
    (is (= (non-nil nil 2) 2))
    (is (= (non-nil nil nil 3) 3))
    (is (= (non-nil nil nil nil nil nil nil nil nil
                    nil nil nil nil nil nil nil nil
                    nil nil nil nil nil nil nil nil
                    nil nil nil nil nil nil nil nil
                    nil nil nil nil nil nil nil nil
                    nil nil nil nil nil nil nil nil
                    nil nil nil nil nil nil nil nil
                    nil nil nil nil nil nil nil nil
                    65) 65)))
  (testing "preserves meta"
    ;; Apparently testing macros don't preserve meta :/
    (letfn [(helper [n]
              (meta (case n
                      1 (non-nil ^:foo [])
                      2 (non-nil nil ^:foo [])
                      3 (non-nil nil nil ^:foo []))))]
      (is (= (helper 1) {:foo true}))
      (is (= (helper 2) {:foo true}))
      (is (= (helper 3) {:foo true}))))
  (testing "evaluates minimally"
    (let [s (atom [])
          a-nil (fn [x] (swap! s conj x) nil)
          a-non-nil (fn [x] (swap! s conj x) @s)]
      (is (= (non-nil (a-nil 1) (a-nil 2) (a-nil 3)
                      (a-non-nil 4)
                      (a-nil 4) (a-nil 5) (a-nil 6))
             [1 2 3 4])))))

(deftest select->tests
  (testing "(select-> x) should always be nil"
    (is (= (select-> nil) nil))
    (is (= (select-> true) nil))
    (is (= (select-> false) nil))
    (is (= (select-> 1) nil))
    (is (= (select-> "") nil))
    (is (= (select-> 'sym) nil))
    (is (= (select-> :kw) nil))
    (is (= (select-> '()) nil))
    (is (= (select-> []) nil))
    (is (= (select-> {}) nil))
    (is (= (select-> #{}) nil))
    (let [e (Exception. "")]
      (is (= (select-> e) nil))))
  (testing "works over different arities and threads left"
    (let [a-nil (constantly nil)]
      (is (= (select-> 1
               (vector :a :b :c))
             [1 :a :b :c]))
      (is (= (select-> 2
               a-nil
               (vector :a :b :c))
             [2 :a :b :c]))
      (is (= (select-> 3
               a-nil a-nil
               (vector :a :b :c))
             [3 :a :b :c]))
      (is (= (select-> 65
               a-nil a-nil a-nil a-nil a-nil a-nil a-nil a-nil
               a-nil a-nil a-nil a-nil a-nil a-nil a-nil a-nil
               a-nil a-nil a-nil a-nil a-nil a-nil a-nil a-nil
               a-nil a-nil a-nil a-nil a-nil a-nil a-nil a-nil
               a-nil a-nil a-nil a-nil a-nil a-nil a-nil a-nil
               a-nil a-nil a-nil a-nil a-nil a-nil a-nil a-nil
               a-nil a-nil a-nil a-nil a-nil a-nil a-nil a-nil
               a-nil a-nil a-nil a-nil a-nil a-nil a-nil a-nil
               (vector :a :b :c))
             [65 :a :b :c]))))
  (testing "preserves meta"
    (let [f (fn [] ^:foo [])]
      (is (= (select-> (f) meta) {:foo true}))))
  (testing "evaluates minimally"
    (let [s (atom {:arg 0 :fn []})
          f (fn [] (-> (swap! s update :arg inc) :arg))
          a-nil (fn [_ k] (swap! s update :fn conj k) nil)
          a-non-nil (fn [_ k] (swap! s update :fn conj k) @s)]
      (is (= (select-> (f)
               (a-nil :a)
               (a-nil :b)
               (a-non-nil :c)
               (a-non-nil :d)
               (a-nil :e))
             {:arg 1
              :fn [:a :b :c]})))))

(deftest select->>tests
  (testing "(select->> x) should always be nil"
    (is (= (select->> nil) nil))
    (is (= (select->> true) nil))
    (is (= (select->> false) nil))
    (is (= (select->> 1) nil))
    (is (= (select->> "") nil))
    (is (= (select->> 'sym) nil))
    (is (= (select->> :kw) nil))
    (is (= (select->> '()) nil))
    (is (= (select->> []) nil))
    (is (= (select->> {}) nil))
    (is (= (select->> #{}) nil))
    (let [e (Exception. "")]
      (is (= (select->> e) nil))))
  (testing "works over different arities and threads left"
    (let [a-nil (constantly nil)]
      (is (= (select->> 1
               (vector :a :b :c))
             [:a :b :c 1]))
      (is (= (select->> 2
               a-nil
               (vector :a :b :c))
             [:a :b :c 2]))
      (is (= (select->> 3
               a-nil a-nil
               (vector :a :b :c))
             [:a :b :c 3]))
      (is (= (select->> 65
               a-nil a-nil a-nil a-nil a-nil a-nil a-nil a-nil
               a-nil a-nil a-nil a-nil a-nil a-nil a-nil a-nil
               a-nil a-nil a-nil a-nil a-nil a-nil a-nil a-nil
               a-nil a-nil a-nil a-nil a-nil a-nil a-nil a-nil
               a-nil a-nil a-nil a-nil a-nil a-nil a-nil a-nil
               a-nil a-nil a-nil a-nil a-nil a-nil a-nil a-nil
               a-nil a-nil a-nil a-nil a-nil a-nil a-nil a-nil
               a-nil a-nil a-nil a-nil a-nil a-nil a-nil a-nil
               (vector :a :b :c))
             [:a :b :c 65]))))
  (testing "preserves meta"
    (let [f (fn [] ^:foo [])]
      (is (= (select->> (f) meta) {:foo true}))))
  (testing "evaluates minimally"
    (let [s (atom {:arg 0 :fn []})
          f (fn [] (->> (swap! s update :arg inc) :arg))
          a-nil (fn [k _] (swap! s update :fn conj k) nil)
          a-non-nil (fn [k _] (swap! s update :fn conj k) @s)]
      (is (= (select->> (f)
               (a-nil :a)
               (a-nil :b)
               (a-non-nil :c)
               (a-non-nil :d)
               (a-nil :e))
             {:arg 1
              :fn [:a :b :c]})))))

;;;
;;; preimage
;;;

(deftest test-preimage
  (testing "missing keys are ignored"
    (is (= (preimage {} {:a :b}) {}))
    (is (= (preimage {:a 1} {:a :b}) {:a 1})))
  (testing "allows permutation"
    (is (= (preimage {:a 1, :b 2, :c 3}
                     {:a :b, :b :c, :c :a})
           {:a 2, :b 3, :c 1})))
  (testing "allows \"surjective preimage\""
    (is (= (preimage {:a 1, :b 2} {:a :a, :c :a}) {:a 1, :b 2, :c 1}))
    (is (= (preimage {:a 1, :b 2} {:c :a, :d :a}) {:b 2, :c 1, :d 1}))))

(defn- splits-gen
  [max-size]
  (gen/bind (gen/choose 0 max-size)
            (fn [n]
              (let [n1 (gen/generate (gen/choose 0 n))
                    n2 (gen/generate (gen/choose 0 (- n n1)))
                    n3 (- n n1 n2)]
                (gen/return (gen/generate (gen/shuffle [n1 n2 n3])))))))

(defn- bijective-cases-gen
  [key-gen val-gen max-size]
  (gen/bind (gen/such-that (fn [[n _ _]]
                             (not= n 1))
                           (splits-gen max-size))
            (fn [[n-perm n-trans n-unchanged]]
              (let [n (+ n-perm
                         n-trans
                         n-trans
                         n-unchanged)
                    m (gen/generate (gen/map key-gen
                                             val-gen
                                             {:num-elements n
                                              :max-tries 100}))
                    tmp (gen/generate (gen/shuffle (keys m)))
                    [perm-src tmp] (split-at n-perm tmp)
                    perm-dst (if (seq perm-src)
                               (concat (rest perm-src) [(first perm-src)])
                               perm-src)
                    [trans-src tmp] (split-at n-trans tmp)
                    trans-dst (take n-trans tmp)]
                (gen/return [(apply dissoc m trans-dst)
                             (zipmap (concat perm-src trans-src)
                                     (concat perm-dst trans-dst))
                             (count perm-src)
                             (count trans-src)])))))

(defspec spec-bijective-cases-gen
  (prop/for-all [[m f n-perm n-trans] (bijective-cases-gen gen/nat gen/nat 100)]
    (let [ks (set (keys m))
          xs (set (keys f))
          ys (set (vals f))]
      (and (set/subset? xs ks)
           (= (count xs)
              (count ys)
              (+ n-perm n-trans))
           (= (count (set/intersection xs ys))
              n-perm)))))

(defspec spec-preimage-corresponds-rename-keys-in-bijective-case-1
  (prop/for-all [[m f _ _] (bijective-cases-gen gen/any gen/any 10)]
    (let [invf (into {} (map (juxt val key)) f)]
      (= (preimage m invf)
         (set/rename-keys m f)))))

(defspec spec-preimage-corresponds-rename-keys-in-bijective-case-2
  (prop/for-all [[m f _ _] (bijective-cases-gen gen/nat gen/nat 1000)]
    (let [invf (into {} (map (juxt val key)) f)]
      (= (preimage m invf)
         (set/rename-keys m f)))))
