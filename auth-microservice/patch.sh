#!/bin/bash
set -euxo pipefail

# pip3 install jsonpatch

jsonpatch realm.json patch-docker.json --indent=2 > realm-docker.json

jq -s '.' realm.json realm-docker.json > realm-all.json
