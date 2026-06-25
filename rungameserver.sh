#!/bin/bash

cd "$(dirname "$(realpath "$0")")" || exit 1

java -Dprofile=wsl -cp "out:lib/*" service.GameServer

