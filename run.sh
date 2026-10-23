#!/bin/sh
set -eu
cd "$(dirname "$0")"
if [ ! -f CUTResearchHub.jar ]; then sh build.sh; fi
exec java -jar CUTResearchHub.jar
