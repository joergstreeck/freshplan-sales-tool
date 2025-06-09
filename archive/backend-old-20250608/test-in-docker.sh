#!/bin/bash
# Run tests in Docker container with Java

echo "🧪 Running tests in Docker container..."

docker run --rm \
  -v "$(pwd)":/workspace \
  -w /workspace \
  maven:3.9-eclipse-temurin-17 \
  mvn clean test

echo "✅ Tests completed!"