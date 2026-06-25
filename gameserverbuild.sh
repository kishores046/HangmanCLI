#!/bin/bash
cd "$(dirname "$(realpath "$0")")" || exit 1

rm -rf out
mkdir -p out

javac -cp "src:lib/*" \
	      -d out \
	            $(find src -name "*.java")

cp src/resources/*.properties out/

echo "Build completed successfully."
