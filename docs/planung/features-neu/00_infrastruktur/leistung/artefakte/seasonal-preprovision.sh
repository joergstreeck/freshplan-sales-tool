#!/usr/bin/env bash
# Usage: ./seasonal-preprovision.sh <deployment> <factor> <namespace>
# Example: ./seasonal-preprovision.sh api 2.0 prod

set -euo pipefail
DEPLOY=${1:-api}
FACTOR=${2:-1.5}
NS=${3:-prod}

CURRENT=$(kubectl -n "$NS" get deploy "$DEPLOY" -o jsonpath='{.spec.replicas}')
TARGET=$(python3 - <<EOF
import math, os
print(math.ceil(float(os.environ['CURRENT'])*float(os.environ['FACTOR'])))
EOF
)
echo "Scaling ${DEPLOY} from ${CURRENT} to ${TARGET} replicas (factor ${FACTOR})"
kubectl -n "$NS" scale deploy "$DEPLOY" --replicas="$TARGET"
