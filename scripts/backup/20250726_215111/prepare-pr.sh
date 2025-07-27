#!/bin/bash

# 📋 PR Preparation Script
# Bereitet alles für einen Pull Request vor

echo "🚀 FreshPlan PR Preparation"
echo "==========================="

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Get current branch
CURRENT_BRANCH=$(git branch --show-current)
echo -e "Current branch: ${BLUE}$CURRENT_BRANCH${NC}"

# 1. Run Quality Checks
echo -e "\n🏆 Running quality checks..."
if ./scripts/quality-check.sh; then
    echo -e "${GREEN}✅ Quality checks passed${NC}"
else
    echo -e "${YELLOW}⚠️  Fix quality issues first${NC}"
    exit 1
fi

# 2. Generate Test Report
echo -e "\n📊 Generating test report..."
if [ -d "frontend" ]; then
    cd frontend
    npm run test:coverage --silent > ../test-report-frontend.txt 2>&1
    echo "Frontend coverage saved to test-report-frontend.txt"
    cd ..
fi

if [ -d "backend" ]; then
    cd backend
    ./mvnw test jacoco:report -q
    echo "Backend coverage in backend/target/site/jacoco/index.html"
    cd ..
fi

# 3. Create PR Template
echo -e "\n📝 Creating PR template..."
cat > pr-template.md << EOF
## 📋 PR: $CURRENT_BRANCH

### Summary
[Brief description of what this PR does]

### 🎯 What's Changed
- [ ] Feature/Module implemented
- [ ] Tests added
- [ ] Documentation updated

### ✅ Quality Checklist
- [ ] **Tests:** Unit coverage > 80%
- [ ] **Integration Tests:** All passing
- [ ] **Browser Tests:** Chrome ✅ Firefox ✅ Safari ✅
- [ ] **Code Review:** Pass 1 ✅ Pass 2 ✅
- [ ] **Performance:** Bundle < 200KB, API < 200ms
- [ ] **Security:** No vulnerabilities
- [ ] **Formatting:** All checks green

### 📸 Screenshots
[Add screenshots for UI changes]

### 🔗 Related Issues
Closes #[issue-number]

### 📝 Testing Instructions
1. Pull this branch
2. Run \`npm install\` and \`./mvnw clean install\`
3. Start services with \`./scripts/start-services.sh\`
4. Test [specific feature]

### 🚀 Deployment Notes
[Any special deployment considerations]

---
Generated with \`./scripts/prepare-pr.sh\`
EOF

echo -e "${GREEN}✅ PR template created: pr-template.md${NC}"

# 4. Check for uncommitted changes
echo -e "\n🔍 Checking git status..."
if [ -n "$(git status --porcelain)" ]; then
    echo -e "${YELLOW}⚠️  You have uncommitted changes:${NC}"
    git status --short
    echo -e "\nCommit or stash them before creating PR"
fi

# 5. Show next steps
echo -e "\n${BLUE}📋 Next Steps:${NC}"
echo "1. Review and edit pr-template.md"
echo "2. Push branch: git push origin $CURRENT_BRANCH"
echo "3. Create PR on GitHub using the template"
echo "4. Request review from team"

# 6. Open browser (optional)
echo -e "\n❓ Open GitHub to create PR? (y/n)"
read -r response
if [[ "$response" =~ ^[Yy]$ ]]; then
    REPO_URL=$(git remote get-url origin | sed 's/\.git$//')
    if [[ "$REPO_URL" =~ github.com ]]; then
        PR_URL="$REPO_URL/compare/$CURRENT_BRANCH?expand=1"
        echo "Opening $PR_URL"
        open "$PR_URL" 2>/dev/null || xdg-open "$PR_URL" 2>/dev/null || echo "Please open: $PR_URL"
    fi
fi

echo -e "\n${GREEN}✅ PR preparation complete!${NC}"