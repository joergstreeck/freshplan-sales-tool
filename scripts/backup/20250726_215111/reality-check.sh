#!/bin/bash

# Reality Check Script - Vergleiche Plan vs. Code Implementation
# Usage: ./scripts/reality-check.sh FC-XXX

set -e

FEATURE_CODE=$1
if [ -z "$FEATURE_CODE" ]; then
    echo "❌ Usage: ./scripts/reality-check.sh FC-XXX"
    exit 1
fi

echo "🔍 REALITY CHECK für $FEATURE_CODE"
echo "================================="
echo

# Find the technical concept document
TECH_CONCEPT=$(find docs -name "*${FEATURE_CODE}*" -o -name "*$(echo $FEATURE_CODE | cut -d'-' -f2)*" | head -1)

if [ -z "$TECH_CONCEPT" ]; then
    echo "❌ Kein technisches Konzept für $FEATURE_CODE gefunden"
    exit 1
fi

echo "📋 Technisches Konzept: $TECH_CONCEPT"
echo

# Extract planned components from technical concept
echo "📝 PLAN vs CODE Analyse:"
echo "========================"

# Check Backend Components
echo
echo "🏗️  BACKEND COMPONENTS:"
echo "----------------------"

# Check if Opportunity Entity exists (for M4)
if [[ "$FEATURE_CODE" == *"M4"* ]] || [[ "$FEATURE_CODE" == *"002"* ]]; then
    OPPORTUNITY_ENTITY="backend/src/main/java/de/freshplan/domain/opportunity/entity/Opportunity.java"
    if [ -f "$OPPORTUNITY_ENTITY" ]; then
        echo "✅ Opportunity Entity: IMPLEMENTIERT"
    else
        echo "❌ Opportunity Entity: FEHLT"
    fi

    OPPORTUNITY_SERVICE="backend/src/main/java/de/freshplan/domain/opportunity/service/OpportunityService.java"
    if [ -f "$OPPORTUNITY_SERVICE" ]; then
        echo "✅ Opportunity Service: IMPLEMENTIERT"
    else
        echo "❌ Opportunity Service: FEHLT"
    fi

    OPPORTUNITY_RESOURCE="backend/src/main/java/de/freshplan/api/resources/OpportunityResource.java"
    if [ -f "$OPPORTUNITY_RESOURCE" ]; then
        echo "✅ Opportunity API Resource: IMPLEMENTIERT"
    else
        echo "❌ Opportunity API Resource: FEHLT"
    fi

    OPPORTUNITY_REPOSITORY="backend/src/main/java/de/freshplan/domain/opportunity/repository/OpportunityRepository.java"
    if [ -f "$OPPORTUNITY_REPOSITORY" ]; then
        echo "✅ Opportunity Repository: IMPLEMENTIERT"
    else
        echo "❌ Opportunity Repository: FEHLT"
    fi

    # Check DTOs
    OPPORTUNITY_DTOS="backend/src/main/java/de/freshplan/domain/opportunity/service/dto/"
    if [ -d "$OPPORTUNITY_DTOS" ]; then
        DTO_COUNT=$(find "$OPPORTUNITY_DTOS" -name "*.java" | wc -l)
        echo "✅ Opportunity DTOs: $DTO_COUNT Dateien implementiert"
    else
        echo "❌ Opportunity DTOs: FEHLEN"
    fi
fi

# Check Tests
echo
echo "🧪 TESTS:"
echo "--------"

BACKEND_TESTS="backend/src/test/java/de/freshplan/domain/opportunity/"
if [ -d "$BACKEND_TESTS" ]; then
    TEST_COUNT=$(find "$BACKEND_TESTS" -name "*Test.java" | wc -l)
    echo "✅ Backend Tests: $TEST_COUNT Test-Klassen"
else
    echo "❌ Backend Tests: FEHLEN"
fi

# Check Frontend Components (if applicable)
echo
echo "🌐 FRONTEND COMPONENTS:"
echo "----------------------"

if [[ "$FEATURE_CODE" == *"M4"* ]] || [[ "$FEATURE_CODE" == *"002"* ]]; then
    OPPORTUNITY_FRONTEND="frontend/src/features/opportunity/"
    if [ -d "$OPPORTUNITY_FRONTEND" ]; then
        COMPONENT_COUNT=$(find "$OPPORTUNITY_FRONTEND" -name "*.tsx" | wc -l)
        echo "✅ Frontend Opportunity Components: $COMPONENT_COUNT Komponenten"
    else
        echo "❌ Frontend Opportunity Components: FEHLEN"
    fi
fi

# Database Migration Check
echo
echo "💾 DATABASE:"
echo "------------"

DB_MIGRATIONS="backend/src/main/resources/db/migration/"
if [ -d "$DB_MIGRATIONS" ]; then
    OPPORTUNITY_MIGRATION=$(find "$DB_MIGRATIONS" -name "*opportunity*" | head -1)
    if [ ! -z "$OPPORTUNITY_MIGRATION" ]; then
        echo "✅ Database Migration: $(basename $OPPORTUNITY_MIGRATION)"
    else
        echo "❌ Database Migration für Opportunity: FEHLT"
    fi
else
    echo "❌ Database Migrations Ordner: FEHLT"
fi

# API Documentation Check
echo
echo "📚 DOKUMENTATION:"
echo "----------------"

API_DOCS="docs/technical/API_CONTRACT.md"
if [ -f "$API_DOCS" ]; then
    if grep -q "opportunity" "$API_DOCS"; then
        echo "✅ API Dokumentation: Opportunity Endpoints dokumentiert"
    else
        echo "⚠️  API Dokumentation: Opportunity Endpoints noch nicht dokumentiert"
    fi
else
    echo "❌ API Dokumentation: FEHLT"
fi

# Compilation Check
echo
echo "⚙️  BUILD STATUS:"
echo "----------------"

cd backend
if ./mvnw compile -q > /dev/null 2>&1; then
    echo "✅ Backend Compilation: ERFOLGREICH"
else
    echo "❌ Backend Compilation: FEHLSCHLÄGE"
fi
cd ..

cd frontend
if npm run build > /dev/null 2>&1; then
    echo "✅ Frontend Build: ERFOLGREICH"
else
    echo "❌ Frontend Build: FEHLSCHLÄGE"
fi
cd ..

echo
echo "📊 ZUSAMMENFASSUNG:"
echo "=================="
echo "Reality Check für $FEATURE_CODE abgeschlossen."
echo "Prüfe die obigen Ergebnisse und implementiere fehlende Komponenten."
echo