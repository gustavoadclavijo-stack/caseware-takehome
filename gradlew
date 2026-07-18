#!/usr/bin/env bash
set -e
DIRNAME=$(dirname "$0")
java -jar "$DIRNAME/gradle/wrapper/gradle-wrapper.jar" "$@"
