#!/bin/bash
# Session Tracker für intelligente Unterbrechungs-Dokumentation

TRACKER_FILE=".session-tracker.json"

case "$1" in
  "start")
    echo "{
      \"status\": \"WORKING\",
      \"start_time\": \"$(date -Iseconds)\",
      \"last_update\": \"$(date -Iseconds)\",
      \"current_todo\": \"\",
      \"current_file\": \"\",
      \"current_line\": \"\",
      \"mental_context\": \"\"
    }" > $TRACKER_FILE
    echo "✅ Session tracking started"
    ;;
  "update")
    # Update current position
    jq --arg todo "$2" --arg file "$3" --arg line "$4" --arg context "$5" '
      .current_todo = $todo |
      .current_file = $file |
      .current_line = $line |
      .mental_context = $context |
      .last_update = now | strftime("%Y-%m-%dT%H:%M:%S%z")
    ' $TRACKER_FILE > tmp.$$.json && mv tmp.$$.json $TRACKER_FILE
    echo "✅ Session state updated"
    ;;
  "interrupt")
    # Mark as interrupted
    jq '.status = "INTERRUPTED" | .interrupt_time = now | strftime("%Y-%m-%dT%H:%M:%S%z")' \
       $TRACKER_FILE > tmp.$$.json && mv tmp.$$.json $TRACKER_FILE
    echo "🚨 Session marked as INTERRUPTED"
    ;;
  "complete")
    jq '.status = "COMPLETED" | .end_time = now | strftime("%Y-%m-%dT%H:%M:%S%z")' \
       $TRACKER_FILE > tmp.$$.json && mv tmp.$$.json $TRACKER_FILE
    echo "✅ Session completed"
    ;;
  "status")
    if [[ -f $TRACKER_FILE ]]; then
      cat $TRACKER_FILE | jq .
    else
      echo "❌ No active session tracking"
    fi
    ;;
  *)
    echo "Usage: $0 {start|update|interrupt|complete|status}"
    echo "  update: $0 update TODO-XXX /path/file.ext 123 'mental context'"
    ;;
esac