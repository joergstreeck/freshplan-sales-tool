#!/bin/bash
cd backend
export MAVEN_OPTS="-Dmaven.multiModuleProjectDirectory=$PWD"
./mvnw quarkus:dev