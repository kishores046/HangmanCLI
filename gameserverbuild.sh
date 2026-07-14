#!/bin/bash

set -e

cd "$(dirname "$(realpath "$0")")" || exit 1

mvn clean package

echo "Build completed successfully."