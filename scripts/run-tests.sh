#!/bin/bash

set -euo pipefail

[[ "${CI-}" == "true" ]] && set -x

cd "$(git rev-parse --show-toplevel)"

clj -A:test:1.7
clj -A:test:1.8
clj -A:test:1.9
clj -A:test:1.10
