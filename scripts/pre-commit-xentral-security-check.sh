#!/bin/bash

# ==============================================================================
# Pre-Commit Hook: Xentral READ-ONLY Security Check
# ==============================================================================
#
# Purpose: Prevents WRITE operations (POST, PUT, PATCH, DELETE) in XentralApiClient
#
# Context:
# - Xentral PAT (Personal Access Token) has WRITE permissions
# - Xentral cannot restrict PAT to READ-ONLY
# - Application-level security: Code MUST be READ-ONLY!
#
# Usage:
#   chmod +x scripts/pre-commit-xentral-security-check.sh
#   ./scripts/pre-commit-xentral-security-check.sh
#
# Installation (Git Hook):
#   echo "./scripts/pre-commit-xentral-security-check.sh" >> .git/hooks/pre-commit
#   chmod +x .git/hooks/pre-commit
#
# Exit Codes:
#   0 = Success (only @GET found)
#   1 = Error (WRITE operations detected)
#
# ==============================================================================

echo "🔒 Xentral Security Check: Verifying READ-ONLY enforcement..."

# Define file to check
XENTRAL_CLIENT="backend/src/main/java/de/freshplan/infrastructure/xentral/XentralApiClient.java"

# Check if file exists
if [ ! -f "$XENTRAL_CLIENT" ]; then
    echo "⚠️  WARNING: XentralApiClient not found (yet) - skipping check"
    echo "   Expected: $XENTRAL_CLIENT"
    exit 0  # Not an error - file might not exist yet
fi

echo "📄 Checking file: $XENTRAL_CLIENT"

# Check for forbidden HTTP methods
FORBIDDEN_METHODS=$(grep -E "@(POST|PUT|PATCH|DELETE)" "$XENTRAL_CLIENT")

if [ -n "$FORBIDDEN_METHODS" ]; then
    echo ""
    echo "❌ ❌ ❌ SECURITY VIOLATION DETECTED ❌ ❌ ❌"
    echo ""
    echo "🚨 XentralApiClient contains WRITE operations!"
    echo ""
    echo "   Xentral API MUST be READ-ONLY (only @GET allowed)!"
    echo ""
    echo "   Found forbidden annotations:"
    echo ""
    grep -n -E "@(POST|PUT|PATCH|DELETE)" "$XENTRAL_CLIENT" | sed 's/^/   /'
    echo ""
    echo "   ✅ Allowed:   @GET (read data)"
    echo "   ❌ Forbidden: @POST, @PUT, @PATCH, @DELETE (write/modify/delete data)"
    echo ""
    echo "   Why READ-ONLY?"
    echo "   - Xentral PAT has WRITE permissions"
    echo "   - Xentral cannot restrict PAT to READ-ONLY"
    echo "   - Application-level security prevents accidental data changes"
    echo ""
    echo "   Fix:"
    echo "   1. Remove all @POST, @PUT, @PATCH, @DELETE annotations from XentralApiClient"
    echo "   2. Use only @GET methods (read data)"
    echo "   3. If you need to CREATE/UPDATE data, do it in FreshPlan DB (not Xentral!)"
    echo ""
    exit 1
fi

# Success
echo "✅ Xentral API is READ-ONLY (only @GET found)"
echo ""

exit 0
