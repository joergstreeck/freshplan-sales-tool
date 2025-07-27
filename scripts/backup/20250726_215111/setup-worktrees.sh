#!/usr/bin/env bash
set -e

echo "🚀 Setting up FreshPlan Worktrees for parallel development..."

cd /Users/joergstreeck/freshplan-sales-tool

# 1) Backend-Worktree (Branch existiert schon)
echo "📦 Creating Backend worktree..."
git worktree add ../freshplan-backend feature/user-management

# 2) Frontend-Worktree (neuer Branch von main)
echo "🎨 Creating Frontend worktree..."
git worktree add ../freshplan-frontend -b feature/react-migration main

# 3) Testing-Worktree (neuer Branch von main)
echo "🧪 Creating Testing worktree..."
git worktree add ../freshplan-testing -b feature/expand-test-coverage main

# Kontrolle
echo "✅ Worktree setup complete!"
echo ""
echo "Current worktrees:"
git worktree list