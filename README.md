<p align="center"><img src="docs/harpoons-summary.svg"></p>

## Usage

Merge the following dependency data into your
[**deps.edn**](https://clojure.org/reference/deps_and_cli) file:

```clojure
{:deps {harpoons {:git/url "https://github.com/mjhanninen/harpoons.git"
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

Copyright © 2018-19 Matti Hänninen

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.
