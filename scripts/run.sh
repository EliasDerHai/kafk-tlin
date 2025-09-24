#!/usr/bin/env bash

set -euo pipefail

# working dir independent
cd "$(git rev-parse --show-toplevel)"

echo "Starting Docker services..."
docker compose up -d

echo "Waiting for Kafka to be ready..."
sleep 10  # or use a proper health check

echo "Starting ping-app in background..."
(cd src/apps/ping-app && ./gradlew run 2>&1 | sed 's/^/[PING] /') &
PING_PID=$!

echo "Starting pong-app in background..."
(cd src/apps/pong-app && ./gradlew run 2>&1 | sed 's/^/[PONG] /') &
PONG_PID=$!

echo "Both apps started. PIDs: ping=$PING_PID, pong=$PONG_PID"
echo "Press Ctrl+C to stop both apps"

# Wait for interrupt and cleanup
trap 'kill $PING_PID $PONG_PID; exit' INT
wait
