#!/bin/bash
set -euo pipefail
[[ "${CI-}" == "true" ]] && set -x
cd "$(git rev-parse --show-toplevel)"
clojure -A:test:1.7
clojure -A:test:1.8
clojure -A:test:1.9
clojure -A:test:1.10
