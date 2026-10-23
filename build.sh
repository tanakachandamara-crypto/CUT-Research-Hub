#!/bin/sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes
javac --release 17 -encoding UTF-8 -d build/classes src/App.java src/core/*.java src/model/*.java src/service/*.java src/gui/*.java
jar --create --file CUTResearchHub.jar --main-class App -C build/classes .
echo "Build complete. Run sh run.sh to start CUT Research Hub."
