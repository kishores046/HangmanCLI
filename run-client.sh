#!/bin/bash

cd "$(dirname "$(realpath "$0")")" || exit 1
java -cp "out:lib/*" client.GameClient
