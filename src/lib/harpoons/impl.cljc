(ns harpoons.impl
  #?(:cljs (:require-macros harpoons.impl)))

(defmacro non-nil
  "Return the leftmost non-nil value.

  The evaluation is short-circuiting and each form is evaluated at most once.

  Examples:

  ```clojure
  (non-nil)            ; => nil
  (non-nil nil true)   ; => true
  (non-nil false true) ; => false
  ```"
  {:added "0.1"
   :doc/format :markdown}
  ([] nil)
  ([x] x)
  ([x & xs]
   `(let [v# ~x]
      (if (some? v#) v# (non-nil ~@xs)))))
