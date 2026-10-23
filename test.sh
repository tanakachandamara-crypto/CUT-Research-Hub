#!/bin/sh
set -eu
cd "$(dirname "$0")"
sh build.sh
mkdir -p build/test-classes
javac --release 17 -encoding UTF-8 -cp build/classes -d build/test-classes test/*.java
java -cp build/classes:build/test-classes HubTests
