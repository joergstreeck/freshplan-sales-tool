#!/bin/bash

echo "🔧 Fixing all lint errors..."

# 1. Fix QuickLogDialog.tsx - Replace any with unknown
sed -i '' 's/Record<string, any>/Record<string, unknown>/g' src/components/communication/QuickLogDialog.tsx
sed -i '' 's/formData: any/formData: Record<string, unknown>/g' src/components/communication/QuickLogDialog.tsx

# 2. Fix ReplyComposer.tsx
sed -i '' 's/onChange: (value: any)/onChange: (value: string)/g' src/components/communication/ReplyComposer.tsx

# 3. Fix ThreadDetail.tsx - Remove unused Paper
sed -i '' 's/, Paper//g' src/components/communication/ThreadDetail.tsx
sed -i '' 's/Paper, //g' src/components/communication/ThreadDetail.tsx

# 4. Fix BreadcrumbNavigation.test.tsx
sed -i '' 's/as any)/as jest.Mock)/g' src/components/layout/__tests__/BreadcrumbNavigation.test.tsx

# 5. Fix navigation.config.ts - Remove unused icons
sed -i '' '/^import ApiIcon/d' src/config/navigation.config.ts
sed -i '' '/^import BugReportIcon/d' src/config/navigation.config.ts
sed -i '' '/^import IntegrationInstructionsIcon/d' src/config/navigation.config.ts

# 6. Fix MyDayColumn components
sed -i '' 's/(item: any)/(item: unknown)/g' src/features/cockpit/components/MyDayColumn.tsx
sed -i '' 's/(item: any)/(item: unknown)/g' src/features/cockpit/components/MyDayColumnMUI.tsx

# 7. Fix CustomerListHeader - wrap in useMemo
sed -i '' '/const handleAddCustomer/,/};/c\
  const handleAddCustomer = React.useMemo(() => onAddCustomer || (() => console.log("Add customer")), [onAddCustomer]);' src/features/customers/components/CustomerListHeader.tsx

# 8. Fix UserFormMUI - Remove unused FormGroup
sed -i '' '/FormGroup,/d' src/features/users/UserFormMUI.tsx

# 9. Fix useCommunication.ts - Remove ThreadItem
sed -i '' 's/import type { CommunicationFilter, ThreadItem }/import type { CommunicationFilter }/g' src/hooks/useCommunication.ts

# 10. Fix useKeyboardNavigation - Add dependencies
sed -i '' 's/\[navigate\]/[navigate, activeMenuId, closeAllSubmenus, expandedMenuId]/g' src/hooks/useKeyboardNavigation.ts

# 11. Fix settings/api.ts
sed -i '' 's/} as any/} as Record<string, unknown>/g' src/lib/settings/api.ts

# 12. Fix AdminDashboard - Remove unused imports
sed -i '' '/IconButton,/d' src/pages/AdminDashboard.tsx
sed -i '' '/Tooltip,/d' src/pages/AdminDashboard.tsx
sed -i '' '/^import StorageIcon/d' src/pages/AdminDashboard.tsx
sed -i '' '/^import SpeedIcon/d' src/pages/AdminDashboard.tsx
sed -i '' '/^import BackupIcon/d' src/pages/AdminDashboard.tsx
sed -i '' '/^import OpenInNewIcon/d' src/pages/AdminDashboard.tsx
# Comment out unused state
sed -i '' 's/const \[expandedCategory, setExpandedCategory\]/\/\/ const [expandedCategory, setExpandedCategory]/g' src/pages/AdminDashboard.tsx

# 13. Fix ApiStatusPage
sed -i '' '/IconButton,/d' src/pages/ApiStatusPage.tsx
sed -i '' '/Tooltip,/d' src/pages/ApiStatusPage.tsx

# 14. Fix AuswertungenDashboard
sed -i '' '/^import AssessmentIcon/d' src/pages/AuswertungenDashboard.tsx

# 15. Fix HelpCenterPage
sed -i '' '/ListItemIcon,/d' src/pages/HelpCenterPage.tsx

# 16. Fix HelpSystemDemoPageV2
sed -i '' '/CardActions,/d' src/pages/HelpSystemDemoPageV2.tsx
sed -i '' '/Stack,/d' src/pages/HelpSystemDemoPageV2.tsx
sed -i '' '/Divider,/d' src/pages/HelpSystemDemoPageV2.tsx
sed -i '' '/List,/d' src/pages/HelpSystemDemoPageV2.tsx
sed -i '' '/ListItem,/d' src/pages/HelpSystemDemoPageV2.tsx
sed -i '' '/ListItemIcon,/d' src/pages/HelpSystemDemoPageV2.tsx
sed -i '' '/ListItemText,/d' src/pages/HelpSystemDemoPageV2.tsx
sed -i '' '/IconButton,/d' src/pages/HelpSystemDemoPageV2.tsx

# 17. Fix KommunikationDashboard
sed -i '' 's/, Paper}/}/g' src/pages/KommunikationDashboard.tsx
sed -i '' 's/{Box, Typography, Grid, Paper}/{Box, Typography, Grid}/g' src/pages/KommunikationDashboard.tsx

# 18. Fix KundenmanagementDashboard
sed -i '' '/AvatarGroup,/d' src/pages/KundenmanagementDashboard.tsx

# 19. Fix NeukundengewinnungDashboard
sed -i '' '/^import CheckCircleIcon/d' src/pages/NeukundengewinnungDashboard.tsx
sed -i '' '/^import PendingIcon/d' src/pages/NeukundengewinnungDashboard.tsx

# 20. Fix placeholders/index.tsx
sed -i '' '/^import LeaderboardIcon/d' src/pages/placeholders/index.tsx
sed -i '' '/^import HelpIcon/d' src/pages/placeholders/index.tsx

# 21. Fix providers.tsx - Comment out unused lazy imports
sed -i '' 's/const SettingsPage =/\/\/ const SettingsPage =/g' src/providers.tsx
sed -i '' 's/const HelpSystemDemoPage =/\/\/ const HelpSystemDemoPage =/g' src/providers.tsx

# 22. Fix useCommunication types
sed -i '' 's/import type { CommunicationFilter, ThreadItem }/import type { CommunicationFilter }/g' src/types/useCommunication.ts

# 23. Fix navigationHelpers - Replace any
sed -i '' 's/(acc: any, curr: any)/(acc: Record<string, unknown>, curr: Record<string, unknown>)/g' src/utils/navigationHelpers.ts

# 24. Fix secureStorage
sed -i '' 's/): any {/): unknown {/g' src/utils/secureStorage.ts

echo "✅ All lint fixes applied!"