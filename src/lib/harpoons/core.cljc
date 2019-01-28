(ns harpoons.core
  (:require [harpoons.impl :refer [non-nil]])
  #?(:cljs (:require-macros harpoons.core)))

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

  ```clojure
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

(defmacro <>-some
  "Start an inner \"and\" short-circuiting context within a diamond-threading context.

  Embeds a short-circuiting diamond-threading context that returns the first
  nil value.  Seeds the embedded context with the threaded value from the
  enclosing diamond-threading context.  See also: `harpoons.core/some-<>`."
  {:added "0.1"
   :forms ['(<>-some threaded-form*)]
   :doc/format :markdown
   :style/indent 0}
  [& clauses]
  `(some-<> ~'<> ~@clauses))

(defmacro <>-non-nil
  "Start an inner \"or\" short-circuiting context within a diamond-threading context.

  Embeds a short-circuiting diamond-threading context that returns the first
  non-nil value.  Seeds the embedded context with the threaded value from the
  enclosing diamond-threading context.  See also: `harpoons.core/non-nil-<>`."
  {:added "0.1"
   :forms ['(<>-non-nil threaded-form*)]
   :doc/format :markdown
   :style/indent 0}
  [& clauses]
  `(non-nil-<> ~'<> ~@clauses))

(defmacro <>-cond
  "Start an inner conditional context within a diamond-threading context.

  Embeds a conditional diamond-threading context.  Seeds the embedded context
  with the threaded value from the enclosing diamond-threading context.  See
  also: `harpoons.core/cond-<>`."
  {:added "0.1"
   :forms ['(cond-<> clause*)]
   :doc/format :markdown
   :style/indent 0}
  [& clauses]
  `(cond-<> ~'<> ~@clauses))

(defmacro <>-bind
  "Bind the threaded value within a diamond-threading scope.

  Binds the value threaded in the outer diamond-threading scope to the symbols
  in `binding-form` destructuring the value as necessary.  Continues the
  diamond-threading scope by evaluating the `forms` successively
  threading the result of the previous evaluation into the next form at
  locations marked by the diamond symbol `<>`."
  {:added "0.1"
   :forms '[(<>-bind binding-form forms*)]
   :doc/format :markdown
   :style/indent 1}
  [binding-form & forms]
  `(let [~binding-form ~'<>]
     (-<> ~'<> ~@forms)))

(defmacro <>-let
  "Bind the value from the diamond-threading scode and evaluate the body forms.

  Binds the value threaded in the outer diamond-threading scope to the symbols
  in `binding-form` destructuring the value as necessary.  Evaluates the forms
  in `body` returning the value of the last.

  Note that the value threaded in the outer diamond threading scope remain
  bound to the symbol `<>`."
  {:added "0.1"
   :forms '[(<>-bind binding-form body-forms*)]
   :doc/format :markdown
   :style/indent 1}
  [binding-form & body]
  `(let [~binding-form ~'<>]
     ~@body))

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
  "Run a side-effect within a diamond-threading scope.

  Evaluates `body-forms` for their side effects and returns the value bound to
  the simple symbol `<>`.

  Example:

  ```clojure
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

(defmacro >-some
  "Start an inner \"and\" short-circuiting context within a left-threading context.

  Embeds a short-circuiting left-threading context that returns the first nil
  value.  Seeds the embedded context with the threaded value from the
  enclosing left-threading context.  See also: `clojure.core/some->`.

  Note: This is effectively an alias for `clojure.core/some->`."
  {:added "0.1"
   :forms ['(>-some expr threaded-form*)]
   :doc/format :markdown
   :style/indent 0}
  [expr & threaded-forms]
  `(some-> ~expr ~@threaded-forms))

(defmacro >-non-nil
  "Start an inner \"or\" short-circuiting context within a left-threading context.

  Embeds a short-circuiting left-threading context that returns the first
  non-nil value.  Seeds the embedded context with the threaded value from the
  enclosing left-threading context.  See also: `harpoons.core/non-nil->`.

  Note: This is effectively an alias for `harpoons.core/non-nil->`."
  {:added "0.1"
   :forms ['(>-non-nil expr threaded-form*)]
   :doc/format :markdown
   :style/indent 0}
  [expr & threaded-forms]
  `(non-nil-> ~expr ~@threaded-forms))

(defmacro >-cond
  "Start an inner conditional context within a left-threading context.

  Embeds a conditional left-threading context.  Seeds the embedded context
  with the threaded value from the enclosing left-threading context.  See
  also: `clojure.core/cond->`.

  Note: This is effectively an alias for `clojure.core/cond->`."
  {:added "0.1"
   :forms ['(>-cond expr clause*)]
   :doc/format :markdown
   :style/indent 0}
  [expr & clauses]
  `(cond-> ~expr ~@clauses))

(defmacro >-bind
  "Bind the threaded value within a left-threading scope.

  Binds the value threaded in the outer left-threading scope to the symbols in
  `binding-form` destructuring the value as necessary.  Continues the
  left-threading scope by evaluating `forms` successively and threading the
  result from the previous evaluation through the first argument position
  within the next form."
  {:added "0.1"
   :forms '[(>-bind threaded-value binding-form forms*)]
   :doc/format :markdown
   :style/indent 1}
  [threaded-value binding-form & forms]
  `(let [value# ~threaded-value
         ~binding-form value#]
     (-> value# ~@forms)))

(defmacro >-let
  "Bind the value from the left-threading scode and evaluate the body forms.

  Binds the value threaded in the outer left-threading scope to the symbols in
  `binding-form` destructuring the value as necessary.  Evaluates the forms in
  `body` returning the value of the last."
  {:added "0.1"
   :forms '[(>-let threaded-value binding-form body*)]
   :doc/format :markdown
   :style/indent 1}
  [threaded-value binding-form & body]
  `(let [~binding-form ~threaded-value]
     ~@body))

(defmacro >-do
  {:added "0.1"
   :doc/format :markdown
   :style/indent 0}
  [expr & body]
  `(-<> ~expr (<>-do ~@body)))

(defmacro >-fx!
  "Run a side-effect within a left-threading scope.

   Evaluates `forms` for their side effect and returns `expr`. The value of
  `expr` is bound to `<>` and, thus, is accessible to `forms`. The expression
  `expr` is evaluated once.

  Example:

  ```clojure
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

;;;
;;; Right threading
;;;

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

(defmacro >>-some
  "Start an inner \"and\" short-circuiting context within a right-threading context.

  Embeds a short-circuiting right-threading context that returns the first nil
  value.  Seeds the embedded context with the threaded value from the
  enclosing right-threading context.  See also: `clojure.core/some->>`."
  {:added "0.1"
   :forms ['(>>-some threaded-form* expr)]
   :doc/format :markdown
   :style/indent 0}
  [& threaded-forms-and-expr]
  `(some->> ~(last threaded-forms-and-expr)
     ~@(butlast threaded-forms-and-expr)))

(defmacro >>-non-nil
  "Start an inner \"or\" short-circuiting context within a right-threading context.

  Embeds a short-circuiting right-threading context that returns the first
  non-nil value.  Seeds the embedded context with the threaded value from the
  enclosing right-threading context.  See also: `harpoons.core/non-nil->>`."
  {:added "0.1"
   :forms ['(>>-non-nil threaded-form* expr)]
   :doc/format :markdown
   :style/indent 0}
  [& threaded-forms-and-expr]
  `(non-nil->> ~(last threaded-forms-and-expr)
     ~@(butlast threaded-forms-and-expr)))

(defmacro >>-cond
  "Start an inner conditional context within a right-threading context.

  Embeds a conditional right-threading context.  Seeds the embedded context
  with the threaded value from the enclosing right-threading context.  See
  also: `clojure.core/cond->>`."
  {:added "0.1"
   :forms ['(>>-cond clause* expr)]
   :doc/format :markdown
   :style/indent 0}
  [& clauses-and-expr]
  `(cond-> ~(last clauses-and-expr)
     ~@(butlast clauses-and-expr)))

(defmacro >>-bind
  "Bind the threaded value within a right-threading context.

  Binds the value threaded in the outer right-threading scope to the symbols
  in `binding-form` destructuring the value as necessary.  Continues the
  right-threading scope by evaluating `forms` successively and threading the
  result from the previous evaluation through the last argument position within
  the next form."
  {:added "0.1"
   :forms '[(>>-bind binding-form forms* threaded-value)]
   :doc/format :markdown
   :style/indent 1}
  [binding-form & forms-and-threaded-value]
  `(let [value# ~(last forms-and-threaded-value)
         ~binding-form value#]
     (->> value# ~@(butlast forms-and-threaded-value))))

(defmacro >>-let
  "Bind the value from the right-threading scode and evaluate the body forms.

  Binds the value threaded in the outer right-threading scope to the symbols
  in `binding-form` destructuring the value as necessary.  Evaluates the forms
  in `body` returning the value of the last."
  {:added "0.1"
   :forms '[(>>-let binding-form body* threaded-value)]
   :doc/format :markdown
   :style/indent 1}
  [binding-form & body-and-threaded-value]
  `(let [~binding-form ~(last body-and-threaded-value)]
     ~@(butlast body-and-threaded-value)))

(defmacro >>-do
  {:added "0.1"
   :doc/format :markdown
   :style/indent 0}
  [& body-and-expr]
  `(-<> ~(last body-and-expr)
     (<>-do ~@(butlast body-and-expr))))

(defmacro >>-fx!
  "Run a side-effect within a right-threading scope.

  Evaluates `forms` for their side effects and returns `expr`. The value of
  `expr` is bound to `<>` and, thus, is accessible to `forms`. The expression
  `expr` is evaluated once.

  Examples:

  ```clojure
  (= (->> (range 5)
       (>>-fx! (prn :before <>)) ; prints \":before (0 1 2 3 4)\"
       (filter even?)
       (>>-fx! (prn :after <>))) ; prints \":after (0 2 4)\"
     '(0 2 4))                   ; => true
  ```"
  {:added "0.1"
   :forms '[(>>-fx! forms* expr)]
   :doc/format :markdown
   :style/indent 0}
  ([expr]
   expr)
  ([form & forms-and-expr]
   `(-<> ~(last forms-and-expr) (<>-fx! ~form ~@(butlast forms-and-expr)))))

;;;
;;; Bridges
;;;

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

  **Example:** Listing the subjects belonging to categories A and B from the
  first trial of the study.

  ```clojure
  (-> study
    :trials
    (get 0)
    :subjects
    (>-<>
      (group-by :category <>)
      (concat (:a <>) (:b <>))))
  ```

  Note that `>-<>` is essentially equivalent to `-<>`.  It differs only by its
  `:style/indent` meta-data that guides more advanced editors in laying out
  the source code."
  {:added "0.1"
   :doc/format :markdown
   :style/indent 0}
  [expr & body]
  `(-<> ~expr ~@body))

(defmacro >>->
  "Bridge between an outer `(->> ...)` and inner `(-> ...)` threading scopes."
  {:added "0.1"
   :doc/format :markdown
   :style/indent 0}
  [& forms]
  `(-> ~(last forms) ~@(butlast forms)))

(defmacro >>-<>
  "Bridge between an outer `(->> ...)` and inner `(-<> ...)` threading scopes."
  {:added "0.1"
   :doc/format :markdown
   :style/indent 0}
  [& forms]
  `(-<> ~(last forms) ~@(butlast forms)))

(defmacro <>->
  "Bridge between an outer `(-<> ...)` and inner `(-> ...)` threading scopes.

  Note that the entry value of the inner `(-> ...)` threading scope is bound
  to the simple symbol `<>` throughout the inner scope.  For example:

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
  to the simple symbol `<>` throughout the inner scope.  For example:

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
