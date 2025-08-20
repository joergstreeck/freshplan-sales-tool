#!/bin/bash
# Run only core tests from the list
# This is a workaround for JUnit 5 tag filtering issues with Quarkus

echo "Running core tests only..."

# Read test classes from file and create comma-separated list
TEST_CLASSES=$(cat core-tests.txt | tr '\n' ',' | sed 's/,$//')

# Run Maven with explicit test list
./mvnw clean test \
  -Dtest="$TEST_CLASSES" \
  -DfailIfNoTests=false \
  "$@"