# Harpoons

[![Clojars Project](https://img.shields.io/clojars/v/harpoons.svg)](https://clojars.org/harpoons) [![CircleCI](https://circleci.com/gh/mjhanninen/harpoons.svg?style=svg)](https://circleci.com/gh/mjhanninen/harpoons)

**Harpoons** adds new threading macros that supplement and compose well with
the ones already existing in `clojure.core`.

Harpoons works with both Clojure and Clojurescript and travels light without
any dependencies.

## Why?

There are situations in which the core threading macros are cumbersome to
use. For example, logging intermediate values can be awkward:

```clojure
(-> foo
  (do-some-processing bar baz)
  ;; Would like to log the intermediate value here!
  (do-some-more-processing xyzzy yzxxz zxyyx))
```

With the `harpoons.core/>-fx!` macro you can inject side-effects, like
logging, without interfering with the threaded value:

```clojure
(-> foo
  (do-some-processing bar baz)
  (>-fx!
    (log/infof "after some processing %s" <>))
  (do-some-more-processing xyzzy yzxxz zxyyx))
```

The macro binds `<>` to the threaded value allowing you to make the injected
logic as complex as you want:

```clojure
(-> foo
  (do-some-processing bar baz)
  (>-fx!
    (launch missiles)
    (when (> (:frob <>) critical-frob)
      (log/warningf "critical frob level %d detected for specimen %s"
                    (:frob <>) (:id <>))
  (do-some-more-processing xyzzy yzxxz zxyyx))
```

Of course, at some point it is more sensible to lift the injected logic into a
function of its own.

## Why not?

Threading macros can get out of hands rather quickly. Using steroids can make
the situation worse.

## Documentation

See: https://mjhanninen.github.io/harpoons/

## Influence

To give credit where it is due the following libraries have served as sources
of inspiration for this one:

- [swiss-arrows](https://github.com/rplevy/swiss-arrows) by [Robert
  Levy](https://github.com/rplevy)

## License

Copyright © 2018-19 Matti Hänninen

Distributed under the Eclipse Public License either version 1.0 or (at your
option) any later version.
