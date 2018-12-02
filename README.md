# Harpoons

(More) composable Clojure threading macros.

## Synopsis

| | `>` | `>>` | `<>` |
|:--- |:---:|:---:|:---:|
| Start threading context | `(-> expr forms*)` | `(->> expr forms*)` | `(-<> expr forms*)` |
| Short-circuit at the first nil value | `(some-> expr forms*)` | `(some->> expr forms*` | `(some-<> expr forms*)` |
| Short-circuit at the first non-nil value | `(non-nil-> epxr forms*)` | `(non-nil->> expr forms*)` | `(non-nil-<> expr forms*)` |
| Conditional threading | `(cond-> expr {test form}*)` | `(cond->> expr {test form}*)` | `(cond-<> expr {test form}*)` |
| Evaluate the forms, return the value of the last one back to the threading context | `(>-do forms*)` | `(>>-do forms*)` | `(<>-do forms*)` |
| Evaluate forms for side-effects, don't alter the threaded value | `(>-fx! forms*)` | (`>>-fx! forms*)` | `(<>-fx! forms*)` |
| Bridge to an inner `>` threading context | | `(>>-> forms*)` | `(<>-> forms*)` |
| Bridge to an inner `>>` threading context | `(>->> forms*)` | | `(<>->> forms*)` |
| Bridge to an inner `<>` threading context | `(>-<> forms*)` | `(>>-<> forms*)` | |

## Usage

Merge the following dependency data into your
[**deps.edn**](https://clojure.org/reference/deps_and_cli) file:

```clojure
{:deps {codebrutale/harpoons {:git/url "https://github.com/codebrutale/harpoons.git"
                              :sha "<insert commit SHA here>"}}}
```

and then import the library in your namespace declaration like so:

```clojure
(:require '[harpoons.core :refer :all])
```

## Influence

To give credit where it is due the following libraries that have served as
sources of inspiration for this one:

- [swiss-arrows](https://github.com/rplevy/swiss-arrows) by [Robert
  Levy](https://github.com/rplevy)

## License

Copyright © 2018 Code Brutale

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.
