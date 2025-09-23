#!/bin/bash

# Fix lint errors for PR #101

echo "Fixing lint errors..."

# Fix any types in test files and components
cd frontend

# 1. Fix any types in QuickLogDialog.tsx
sed -i '' 's/Record<string, any>/Record<string, unknown>/g' src/components/communication/QuickLogDialog.tsx

# 2. Fix any type in ReplyComposer.tsx
sed -i '' 's/onChange: (value: any)/onChange: (value: string)/g' src/components/communication/ReplyComposer.tsx

# 3. Remove unused Paper import from ThreadDetail.tsx
sed -i '' '/^import.*Paper.*from.*@mui\/material/d' src/components/communication/ThreadDetail.tsx

# 4. Fix any types in test file
sed -i '' 's/(mockUseNavigate as any)/(mockUseNavigate as jest.Mock)/g' src/components/layout/__tests__/BreadcrumbNavigation.test.tsx

# 5. Remove unused imports from navigation.config.ts
sed -i '' '/^import ApiIcon/d' src/config/navigation.config.ts
sed -i '' '/^import BugReportIcon/d' src/config/navigation.config.ts
sed -i '' '/^import IntegrationInstructionsIcon/d' src/config/navigation.config.ts

# 6. Fix any types in cockpit components
sed -i '' 's/item: any/item: unknown/g' src/features/cockpit/components/MyDayColumn.tsx
sed -i '' 's/item: any/item: unknown/g' src/features/cockpit/components/MyDayColumnMUI.tsx

# 7. Remove unused FormGroup from UserFormMUI.tsx
sed -i '' '/^  FormGroup,$/d' src/features/users/UserFormMUI.tsx

# 8. Remove unused ThreadItem from useCommunication.ts
sed -i '' 's/ThreadItem, //g' src/hooks/useCommunication.ts
sed -i '' 's/, ThreadItem//g' src/hooks/useCommunication.ts

# 9. Remove unused imports from AdminDashboard.tsx
sed -i '' '/^  IconButton,$/d' src/pages/AdminDashboard.tsx
sed -i '' '/^  Tooltip,$/d' src/pages/AdminDashboard.tsx
sed -i '' '/^import StorageIcon/d' src/pages/AdminDashboard.tsx
sed -i '' '/^import SpeedIcon/d' src/pages/AdminDashboard.tsx
sed -i '' '/^import BackupIcon/d' src/pages/AdminDashboard.tsx
sed -i '' '/^import OpenInNewIcon/d' src/pages/AdminDashboard.tsx

# 10. Remove unused imports from ApiStatusPage.tsx
sed -i '' '/^  IconButton,$/d' src/pages/ApiStatusPage.tsx
sed -i '' '/^  Tooltip,$/d' src/pages/ApiStatusPage.tsx

# 11. Remove unused imports from other pages
sed -i '' '/^import AssessmentIcon/d' src/pages/AuswertungenDashboard.tsx
sed -i '' '/^  ListItemIcon,$/d' src/pages/HelpCenterPage.tsx
sed -i '' '/^  CardActions,$/d' src/pages/HelpSystemDemoPageV2.tsx
sed -i '' '/^  Stack,$/d' src/pages/HelpSystemDemoPageV2.tsx
sed -i '' '/^  Divider,$/d' src/pages/HelpSystemDemoPageV2.tsx
sed -i '' '/^  List,$/d' src/pages/HelpSystemDemoPageV2.tsx
sed -i '' '/^  ListItem,$/d' src/pages/HelpSystemDemoPageV2.tsx
sed -i '' '/^  ListItemIcon,$/d' src/pages/HelpSystemDemoPageV2.tsx
sed -i '' '/^  ListItemText,$/d' src/pages/HelpSystemDemoPageV2.tsx
sed -i '' '/^  IconButton,$/d' src/pages/HelpSystemDemoPageV2.tsx

# 12. Fix any types in utils
sed -i '' 's/acc: any, curr: any/acc: unknown, curr: unknown/g' src/utils/navigationHelpers.ts
sed -i '' 's/unknown): any/unknown): unknown/g' src/utils/secureStorage.ts

echo "Fixed most lint errors. Remaining errors need manual fixes:"
echo "- CustomerListHeader.tsx: useMemo for handleAddCustomer"
echo "- useKeyboardNavigation.ts: dependencies in useCallback"
echo "- AdminDashboard.tsx: unused state variables"

npm run lint