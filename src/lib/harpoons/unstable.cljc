(ns harpoons.core
  #?(:cljs (:require-macros harpoons.core)))

(defmacro ^:private non-nil
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

;;;
;;; Diamond threading
;;;

(defmacro -<>
  "Thread the results through the diamonds in `forms`.

  Threads the results of successive evaluations through the `forms` by placing
  the result from the preceding form into the next form at locations marked by
  the diamond symbol `<>`.  Initially the value of `expr` is bound to `<>`
  before the first form is evaluated.

  Note that `(-<> expr ...)` is equivalent to `(as-> expr <> ...)`.

  Examples:

  ```clojure
  > (-<> )
  [:foo :bar :baz]
  ```"
  {:added "0.1"
   :doc/format :markdown
   :style/indent 1}
  [expr & forms]
  `(as-> ~expr ~'<> ~@forms))

(defmacro some-<>
  "Thread the results through the diamonds in `forms` short-circuiting at nil."
  {:added "0.1"
   :forms ['(some-<> expr forms*)]
   :doc/format :markdown
   :style/indent 1}
  [expr & forms]
  (let [steps (for [f forms] `(when (some? ~'<>) ~f))]
    (if (seq steps)
      `(let [~'<> ~expr
             ~@(interleave (repeat '<>) (butlast steps))]
         ~(last steps))
      expr)))

(defmacro cond-<>
  "Evaluate `clauses` conditionally threading the results through the diamonds.

  ```
  (cond-<> 10
    false (+ <> 1)  ; ignored
    (= <> 10) (* <> <>))
  ; => 100
  ```"
  {:added "0.1"
   :forms ['(cond-<> expr clauses*)]
   :doc/format :markdown
   :style/indent 1}
  [expr & clauses]
  (let [steps (for [[? f] (partition 2 clauses)] `(if ~? ~f ~'<>))]
    (if (seq steps)
      `(let [~'<> ~expr
             ~@(interleave (repeat '<>) (butlast steps))]
         ~(last steps))
      expr)))

(defmacro non-nil-<>
  "Returns the first non-nil evaluation of `forms` with `expr` bound to `<>`."
  {:added "0.1"
   :forms ['(non-nil-<> expr form*)]
   :doc/format :markdown
   :style/indent 1}
  [expr & forms]
  `(let [~'<> ~expr]
     (non-nil ~@forms)))

(defmacro <>-do
  "Evaluate forms returning the last value within a diamond-threading scope.

  Note that this an alias for `do`.  You might use this for the sake of
  syntactic consistency.  However in general you should prefer `do` over
  `<>-do`."
  {:added "0.1"
   :doc/format :markdown
   :style/indent 0}
  [& body]
  `(do ~@body))

(defmacro <>-fx!
  "Run a side-fxect within a diamond-threading scope.

  Evaluates `body-forms` for their side fxects and returns the value bound to
  the simple symbol `<>`.

  Example:

  ```
  (-<> (range 2 4)
    (<>-fx! (prn :before <>)) ; \":before (2 3)\"
    (for [x <> y <>] (* x y))
    (<>-fx! (prn :after <>))  ; \":after (4 6 6 9)\"
    (= <> '(4 6 6 9)))
  ; => true
  ```"
  {:added "0.1"
   :forms '[(<>-fx! body-forms*)]
   :doc/format :markdown
   :style/indent 0}
  [& body-forms]
  `(<>-do ~@body-forms ~'<>))

;;;
;;; Left threading
;;;

(defmacro non-nil->
  "Return the first non-nil evaluation of `forms` with `expr` as the leftmost argument.

  Threads `expr` syntactically through the left-hand side of `forms` and
  evaluates the forms in a short-circuiting manner returning the first non-nil
  result, if any.  The expression `expr` is evaluated at most once.

  Examples:

  ```clojure
  (non-nil-> true)                              ; => nil
  (non-nil-> nil identity nil? boolean)         ; => true
  (non-nil-> nil identity boolean nil?)         ; => false
  (non-nil-> {:foo 1, :bar 2} :xyzzy :bar :foo) ; => 2
  (non-nil-> {:foo 1, :bar 2}
    (get :xyzzy)
    (get :bar)
    (get :foo))                                ; => 2
  ```"
  {:added "0.1"
   :doc/format :markdown
   :style/indent 1}
  [expr & forms]
  (let [v (gensym)]
    `(let [~v ~expr]
       (non-nil ~@(map (fn [form]
                         (if (seq? form)
                           (let [f `(~(first form) ~v ~@(next form))]
                             (if-let [m (meta form)]
                               (with-meta f m)
                               f))
                           (list form v)))
                       forms)))))

(defmacro >-do [expr & body]
  {:added "0.1"
   :doc/format :markdown
   :style/indent 0}
  `(-<> ~expr (<>-do ~@body)))

(defmacro >-fx!
  "Run a side-fxect within a left-threading scope.

   Evaluates `forms` for their side fxect and returns `expr`. The value of
  `expr` is bound to `<>` and, thus, is accessible to `forms`. The expression
  `expr` is evaluated once.

  Example:

  ```
  (= (-> {:foo 42}
       (>-fx! (prn :before <>)) ; \":before {:foo 42}\"
       (assoc :bar 3.14)
       (>-fx! (prn :after <>))) ; \":after {:foo 42, :bar 3.14}\"
     {:foo 42, :bar 3.14})       ; => true
  ```"
  {:added "0.1"
   :forms '[(>-fx! expr forms*)]
   :doc/format :markdown
   :style/indent 0}
  [expr & body]
  `(-<> ~expr (<>-fx! ~@body)))

(defmacro >->>
  "Bridge between an outer left-threading and inner right-threading scopes.

  It is notable that this is essentially the same as the `(->> ...)` threading
  macro.  You should use this version only when it is justified by emphasizing
  the bridge point or by maintaining consistency with other bridge points.
  Otherwise just use the standard `(->> ...)`.

  Example:

  ```clojure
  (-> monster
    :inventory
    (>->>
      (filter #(> (:weight %) 10.0))
      (sort-by :weight)
      (take 5)))
  ```"
  {:added "0.1"
   :doc/format :markdown
   :style/indent 0}
  [expr & forms]
  `(->> ~expr ~@forms))

(defmacro >-<>
  "Bridge between an outer left-threading and inner diamond-threading scopes.

  Example:

  ```clojure
  (-> my-map
      :key
      :subkey
      (>-<> (/ <> 7.5)                 ; hourly
            (- 100.0 <>)               ; sales margin
            (filter #(> % <>) foos)))
  ```"
  {:added "0.1"
   :doc/format :markdown
   :style/indent 0}
  [expr & body]
  `(-<> ~expr ~@body))

(defmacro non-nil->>
  "Return the first non-nil evaluation of `forms` with `expr` as the rightmost argument.

  Threads `expr` syntactically through the right-hand side of `forms` and
  evaluates the forms in a short-circuiting manner returning the first non-nil
  result, if any.  The expression `expr` is evaluated at most once."
  {:added "0.1"
   :doc/format :markdown
   :style/indent 1}
  [expr & forms]
  (let [v (gensym)]
    `(let [~v ~expr]
       (non-nil ~@(map (fn [form]
                         (if (seq? form)
                           (let [f `(~(first form) ~@(next form) ~v)]
                             (if-let [m (meta form)]
                               (with-meta f m)
                               f))
                           (list form v)))
                       forms)))))

(defmacro >>-do
  ""
  {:added "0.1"
   :doc/format :markdown
   :style/indent 0}
  [& body-and-expr]
  `(-<> ~(last body-and-expr)
     (<>-do ~@(butlast body-and-expr))))

(defmacro >>-fx!
  "Run a side-fxect within a right-threading scope.

  Evaluates `forms` for their side fxect and returns `expr`. The value of
  `expr` is bound to `<>` and, thus, is accessible to `forms`. The expression
  `expr` is evaluated once.

  Examples:

  ```
  (= (->> (range 5)
       (>>-fx! (prn :before <>)) ; prints \":before (0 1 2 3 4)\"
       (filter even?)
       (>>-fx! (prn :after <>))) ; prints \":after (0 2 4)\"
     '(0 2 4))                    ; => true
  ```"
  {:added "0.1"
   :forms '[(>>-fx! forms* expr)]
   :doc/format :markdown
   :style/indent 0}
  ([expr]
   expr)
  ([body-form & body-and-expr]
   `(-<> ~(last body-and-expr) (<>-fx! ~body-form ~@(butlast body-and-expr)))))

;;;
;;; Bridges
;;;

(defmacro >>->
  "Bridge between an outer `(->> ...)` and inner `(-> ...)` threading scopes."
  {:added "0.1"
   :doc/format :markdown
   :style/indent 0}
  [& body]
  `(-> ~(last body) ~@(butlast body)))

(defmacro >>-<>
  "Bridge between an outer `(->> ...)` and inner `(-<> ...)` threading scopes."
  {:added "0.1"
   :doc/format :markdown
   :style/indent 0}
  [& body]
  `(-<> ~(last body) ~@(butlast body)))

(defmacro <>->
  "Bridge between an outer `(-<> ...)` and inner `(-> ...)` threading scopes.

  Note that the entry value of the inner `(-> ...)` threading scope is bound
  to the simple symbol `<>` is throughout the whole scope.  For example:

  ```clojure
  (-<> 3
    (* <> 4)    ; => 12
    (<>->
      (- 2)     ; => 10
      (* <>)))  ; 12 still bound to <> => 120
  ```"
  {:added "0.1"
   :doc/format :markdown
   :style/indent 0}
  [& forms]
  `(-> ~'<> ~@forms))

(defmacro <>->>
  "Bridge between an outer `(-<> ...)` and inner `(->> ...)` threading scopes.

  Note that the entry value of the inner `(->> ...)` threading scope is bound
  to the simple symbol `<>` is throughout the whole scope.  For example:

  ```clojure
  (-<> 3
    (* <> 4)    ; => 12
    (<>->>
      (- 2)     ; => -10
      (* <>)))  ; 12 still bound to <> => -120
  ```"
  {:added "0.1"
   :doc/format :markdown
   :style/indent 0}
  [& forms]
  `(->> ~'<> ~@forms))
