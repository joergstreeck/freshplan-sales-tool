#!/bin/bash
# FreshPlan Team Aliases
# Source this file in your ~/.zshrc or ~/.bashrc

# Project paths
export FRESHPLAN_ROOT="/Users/joergstreeck/freshplan-sales-tool"

# Navigation aliases
alias fp="cd $FRESHPLAN_ROOT"
alias fpb="cd $FRESHPLAN_ROOT/backend"
alias fpf="cd $FRESHPLAN_ROOT/frontend"
alias fpd="cd $FRESHPLAN_ROOT/docs"

# Backend Management
alias backend="$FRESHPLAN_ROOT/scripts/backend-manager.sh"
alias backend-start="$FRESHPLAN_ROOT/scripts/backend-manager.sh start"
alias backend-stop="$FRESHPLAN_ROOT/scripts/backend-manager.sh stop"
alias backend-status="$FRESHPLAN_ROOT/scripts/backend-manager.sh status"
alias backend-logs="$FRESHPLAN_ROOT/scripts/backend-manager.sh logs"
alias backend-restart="$FRESHPLAN_ROOT/scripts/backend-manager.sh restart"

# Quick Health Checks
alias health="curl -s http://localhost:8080/q/health | jq '.'"
alias api-test="curl -s http://localhost:8080/api/customers | jq '.'"
alias backend-check="curl -s http://localhost:8080/q/health && echo 'Backend OK' || echo 'Backend DOWN'"

# Development Workflow
alias fresh-start="cd $FRESHPLAN_ROOT && ./scripts/session-start.sh"
alias fresh-clean="cd $FRESHPLAN_ROOT && ./scripts/quick-cleanup.sh"
alias fresh-diagnose="cd $FRESHPLAN_ROOT && ./scripts/diagnose-problems.sh"

# Logs
alias logs-backend="tail -f $FRESHPLAN_ROOT/logs/backend.log"
alias logs-frontend="tail -f $FRESHPLAN_ROOT/logs/frontend.log"

# Java Environment
alias java-check="java -version && echo '' && mvn -version"
alias java-fix="export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home && export PATH=\$JAVA_HOME/bin:\$PATH && echo 'Java 17 activated'"

# Git shortcuts
alias gs="git status"
alias gl="git log --oneline -10"
alias gb="git branch"

# Quick tests
alias test-backend="cd $FRESHPLAN_ROOT/backend && mvn test"
alias test-frontend="cd $FRESHPLAN_ROOT/frontend && npm test"

echo "✅ FreshPlan team aliases loaded"
echo "💡 Usage examples:"
echo "   fresh-start      # Start development session"
echo "   backend-status   # Check backend status"
echo "   fresh-diagnose   # Diagnose problems"
echo "   health          # API health check"