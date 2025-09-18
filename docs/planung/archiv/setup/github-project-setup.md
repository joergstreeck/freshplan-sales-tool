# GitHub Project Board Setup - FreshPlan 2.0

## Recommended Columns

1. **📋 Backlog** - All unplanned work
2. **🎯 Sprint 0** - Current sprint (Walking Skeleton)
3. **🚧 In Progress** - Actively being worked on
4. **👀 In Review** - PR opened, awaiting review
5. **✅ Done** - Merged to main
6. **🚫 Blocked** - Waiting for external dependency

## Recommended Labels

### Priority
- `priority: critical` (🔴 Red) - Must be done ASAP
- `priority: high` (🟠 Orange) - Important for current sprint
- `priority: medium` (🟡 Yellow) - Should be done soon
- `priority: low` (🟢 Green) - Nice to have

### Type
- `type: feature` (💡 Light Blue) - New functionality
- `type: bug` (🐛 Red) - Something isn't working
- `type: tech-debt` (🏗️ Gray) - Code improvements
- `type: docs` (📚 Blue) - Documentation only
- `type: infrastructure` (🔧 Purple) - CI/CD, Docker, AWS

### Component
- `component: frontend` (⚛️ Cyan) - React app
- `component: backend` (☕ Brown) - Quarkus API
- `component: legacy` (📦 Gray) - Old codebase
- `component: infrastructure` (☁️ Purple) - AWS/Docker

### Status
- `status: needs-design` (🎨 Pink) - UX/UI work needed
- `status: needs-discussion` (💬 Yellow) - Team input required
- `status: ready` (✅ Green) - Ready to implement

### Effort
- `effort: XS` (1-2 hours)
- `effort: S` (2-4 hours)
- `effort: M` (1-2 days)
- `effort: L` (3-5 days)
- `effort: XL` (1+ week)

## Automation Rules

1. When PR is opened → Move to "In Review"
2. When PR is merged → Move to "Done"
3. When issue is assigned → Move to "In Progress"
4. When "blocked" label added → Move to "Blocked"

## Issue Templates

### Feature Request
```markdown
## Description
Brief description of the feature

## Acceptance Criteria
- [ ] Criterion 1
- [ ] Criterion 2

## Technical Notes
Any implementation details

## Definition of Done
- [ ] Unit tests written
- [ ] Documentation updated
- [ ] PR reviewed
```

### Bug Report
```markdown
## Description
What's broken?

## Steps to Reproduce
1. Step 1
2. Step 2

## Expected Behavior
What should happen

## Actual Behavior
What actually happens

## Environment
- Browser:
- OS:
- Version:
```