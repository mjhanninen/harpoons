(ns docs
  (:refer-clojure :exclude [newline])
  (:require [clojure.java.shell :as sh]
            [clojure.string :as str]
            [hiccup.core :refer [h]]
            [hiccup.page :refer [html5]]))

(def newline "\n")

(def ellipsis "&hellip;")

(defn inline-sym
  [name]
  [:code (h name)])

(defn metaform
  ([form-name]
   (metaform form-name nil))
  ([form-name index]
   (cond-> [:span.metaform (h form-name)]
     (some? index) (conj [:sub (h (str index))]))))

(defn hot
  [sym-name]
  [:span.code-block__hot (h sym-name)])

(defn snippet
  [form-name init-name body-names style]
  (if (= style :hidden)
    [:pre.code-block__snippet.code-block__snippet--phantom]
    [:pre.code-block__snippet
     (cond-> {}
       (= style :dimmed) (assoc :class "code-block--existing"))
     [:code
      "(" (hot form-name) (when init-name
                               (list " " (metaform init-name))) newline
      "  " (->> body-names
             (map #(metaform % 1))
             (interpose " ")) newline
      "  " ellipsis newline
      "  " (->> body-names
             (map #(metaform % "n"))
             (interpose " "))] ")"]))

(defn extrapolate
  [name context-sym]
  (str/replace name #"\*" context-sym))

(defn code-block
  ([form-stem init-name body-names]
   (code-block form-stem init-name body-names {}))
  ([form-stem init-name body-names styles]
   [:div.code-block
    (snippet (extrapolate form-stem ">")
             init-name
             body-names
             (get styles :> :normal))
    (snippet (extrapolate form-stem ">>")
             init-name
             body-names
             (get styles :>> :normal))
    (snippet (extrapolate form-stem "<>")
             init-name
             body-names
             (get styles :<> :normal))]))

(defn page
  []
  [:html
   [:head
    [:meta {:charset "utf-8"}]
    [:title "Harpoons - Composable Clojure threading macros"]
    [:link {:href "https://fonts.googleapis.com/css?family=IBM+Plex+Mono|IBM+Plex+Sans:400,700|IBM+Plex+Serif:400i"
            :rel "stylesheet"}]
    [:link {:href "style.css"
            :rel "stylesheet"}]]
   [:body
    [:main
     [:h1 "Harpoons"]
     [:p
      [:b "Harpoons"]
      " is a Clojure and Clojurescript library providing a set of new
       threading macros that complement and compose well with the existing
       core threading macros."]
     [:section
      [:h2 "Starting threading context"]
      [:p "Start a normal threading context:"]
      (code-block "-*" "expr" ["threaded-form"]
                  {:> :dimmed, :>> :dimmed})
      [:p "Start a short-circuiting threading context that exits at the first
           nil value:"]
      (code-block "some-*" "expr" ["threaded-form"]
                  {:> :dimmed, :>> :dimmed})
      [:p "Start a short-circuiting threading context that exits at the first
           non-nil value:"]
      (code-block "non-nil-*" "expr" ["threaded-form"])
      [:p "Start a conditional threading context:"]
      (code-block "cond-*" "expr" ["test" "threaded-form"]
                  {:> :dimmed, :>> :dimmed})]
     [:section
      [:h2 "Modifying threading context"]
      [:p "Convert the enclosing threading context into a corresponding
           short-circuiting context that exits at the first nil value:"]
      (code-block "*-some" nil ["threaded-form"])
      [:p "Convert the enclosing threading context into a corresponding
           short-circuiting context that exits at the first non-nil value:"]
      (code-block "*-non-nil" nil ["threaded-form"])
      [:p "Convert the enclosing threading context into a corresponding
           conditional threading context:"]
      (code-block "*-cond" nil ["test" "threaded-form"])
      [:p
       "Bind the threaded value to the symbols in "
       (metaform "binding-form")
       " destructuring the value as necessary:"]
      (code-block "*-bind" "binding-form" ["threaded-form"])]
     [:section
      [:h2 "Evaluating body within threading context"]
      [:p
       "Bind the threaded value to the symbols in "
       (metaform "binding-form")
       " destructuring the value as necessary. Evaluate the body forms
        returning the value of the last form back to the enclosing threading
        context:"]
      (code-block "*-let" "binding-form" ["body-form"])
      [:p
       "Evaluate the body forms returning the value of the last form back to
        the enclosing threading context. The threaded value is bount to the "
       (inline-sym "<>")
       " symbol throughout the scope of the body:"]
      (code-block "*-do" nil ["body-form"])
      [:p
       "Evaluate the body forms for the side-effects only without altering the
        threaded value in the enclosing threading context. The threaded value
        is bound to the "
       (inline-sym "<>")
       " symbol throughout the scope of the body:"]
      (code-block "*-fx!" nil ["body-form"])]
     [:section
      [:h2 "Bridging between different threading contexts"]
      [:p
       "Bridge to an inner "
       (inline-sym ">")
       " threading context.  Note that when bridging from an outer "
       (inline-sym "<>")
       " context the value threaded in the context remain bound to the "
       (inline-sym "<>")
       " symbol and, thus, is accessible throughout the scope of the bridging
       form:"]
      (code-block "*->" nil ["threaded-form"] {:> :hidden})
      [:p
       "Bridge to an inner "
       (inline-sym ">>")
       " threading context.  Note that when bridging from an outer "
       (inline-sym "<>")
       " context the value threaded in the context remain bound to the "
       (inline-sym "<>")
       " symbol and, thus, is accessible throughout the scope of the bridging
       form:"]
      (code-block "*->>" nil ["threaded-form"] {:>> :hidden})
      [:p "Bridge to an inner " (inline-sym "<>") " threading context."]
      (code-block "*-<>" nil ["threaded-form"] {:<> :hidden})]]]])

(defn pass-through-tidy
  [input]
  (:out (sh/sh "tidy"
               "-quiet"
               "-indent"
               "-utf8"
               "--omit-optional-tags" "yes"
               "--uppercase-attributes" "no"
               "--uppercase-tags" "no"
               "--tidy-mark" "no"
               :in input :in-enc "UTF-8" :out-enc "UTF-8")))

(defn render-index
  []
  (->> (page)
    html5
    #_pass-through-tidy
    (spit "docs/index.html")))
