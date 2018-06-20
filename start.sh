#!/usr/bin/env bash

WYA_ENV="production"

killall java
rm -rf out
rm -rf build

./gradlew fatJar
java -jar build/libs/wya-all-1.0-SNAPSHOT.jar > logs.log 2>&1 &

SERVER_PID=$!


# tail -f /proc/${SERVER_PID}/fd/1
