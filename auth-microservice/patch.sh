#!/bin/bash

# Can't have `set -euxo pipefail`.
# Otherwise CircleCI cannot use this script.

# pip3 install jsonpatch

jsonpatch realm.json patch-local.json --indent=2 > realm-local.json
jsonpatch realm.json patch-docker.json --indent=2 > realm-docker.json
jsonpatch realm.json patch-kubernetes.json --indent=2 > realm-kubernetes.json
