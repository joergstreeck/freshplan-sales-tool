#!/bin/bash
# setup-pre-commit.sh

echo "Setting up pre-commit hooks..."

# Install pre-commit if not available
if ! command -v pre-commit &> /dev/null; then
    echo "Installing pre-commit..."
    pip install pre-commit
fi

# Install hooks
pre-commit install

echo "✅ Pre-commit hooks installed!"
echo ""
echo "Hooks will prevent:"
echo "  - DELETE FROM customers patterns"
echo "  - New untagged tests"
echo "  - New SEED references"
echo "  - Missing isTestData flags"
echo ""
echo "To bypass hooks (emergency only): git commit --no-verify"