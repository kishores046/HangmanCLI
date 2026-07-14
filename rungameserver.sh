#!/bin/bash

set -e

cd "$(dirname "$(realpath "$0")")" || exit 1

java \
  -Djava.util.logging.config.file=target/classes/logging.properties \
  -cp "target/classes:target/dependency/*" \
  service.GameTcpServer