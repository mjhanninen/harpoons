#!/bin/bash
set -euo pipefail
[[ "${CI-}" == "true" ]] && set -x
cd "$(git rev-parse --show-toplevel)"
[[ -e target/ ]] || mkdir target
clojure -A:build
