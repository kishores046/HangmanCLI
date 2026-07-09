#!/bin/bash

cd "$(dirname "$(realpath "$0")")" || exit 1
mkdir logs
java -Djava.util.logging.config.file=out/logging.properties -cp "out:lib/*" service.GameServer
