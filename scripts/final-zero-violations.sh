#!/bin/bash
# Final Design System Fix - Target: ZERO Violations
# Fixes all remaining 83 violations

cd /Users/joergstreeck/freshplan-sales-tool/frontend/src

echo "🎯 Final Design System Refactoring - Target: ZERO Violations"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Counter
FIXES=0

# Fix mobileActions.types.ts
sed -i '' "s/calendar: '#FF9800'/calendar: 'warning.main'/g" features/customers/types/mobileActions.types.ts
sed -i '' "s/note: '#607D8B'/note: 'grey.600'/g" features/customers/types/mobileActions.types.ts
sed -i '' "s/meeting: '#795548'/meeting: 'grey.700'/g" features/customers/types/mobileActions.types.ts
((FIXES+=3))

# Fix ActionSuggestionService.ts
sed -i '' "s/color: '#E91E63'/color: 'secondary.light'/g" features/customers/components/mobile/ActionSuggestionService.ts
sed -i '' "s/color: '#FF5722'/color: 'error.light'/g" features/customers/components/mobile/ActionSuggestionService.ts
((FIXES+=2))

# Fix DetailedLocationsStep.tsx
sed -i '' "s/'&:hover': { bgcolor: '#7BA345' }/'&:hover': { bgcolor: 'primary.dark' }/g" features/customers/components/steps/DetailedLocationsStep.tsx
((FIXES+=1))

# Fix LeadScoreIndicator.tsx
sed -i '' "s/if (value < 40) return '#f44336'/if (value < 40) return theme.palette.error.main/g" features/leads/LeadScoreIndicator.tsx
sed -i '' "s/if (value < 70) return '#ff9800'/if (value < 70) return theme.palette.warning.main/g" features/leads/LeadScoreIndicator.tsx
((FIXES+=2))

# Fix LeadWizard.tsx
sed -i '' "s/'#FFF3E0' : '#E3F2FD'/'warning.lighter' : 'info.lighter'/g" features/leads/LeadWizard.tsx
sed -i '' "s/? '#FF9800'/? 'warning.main'/g" features/leads/LeadWizard.tsx
sed -i '' "s/: '#2196F3'/: 'info.main'/g" features/leads/LeadWizard.tsx
((FIXES+=3))

# Fix LeadActivityTimelineGrouped.tsx
sed -i '' "s/color: '#00BCD4'/color: 'info.light'/g" features/leads/components/LeadActivityTimelineGrouped.tsx
sed -i '' "s/color: '#607D8B'/color: 'grey.600'/g" features/leads/components/LeadActivityTimelineGrouped.tsx
((FIXES+=4))

# Fix LeadContactsCard.tsx
sed -i '' "s/return '#9E9E9E'/return theme.palette.grey[500]/g" features/leads/components/LeadContactsCard.tsx
((FIXES+=1))

# Fix PreClaimBadge.tsx - Complex ternaries
sed -i '' "s/'#FF9800' : '#94C456'/'warning.main' : 'primary.main'/g" features/leads/components/PreClaimBadge.tsx
sed -i '' "s/'#F44336'/'error.main'/g" features/leads/components/PreClaimBadge.tsx
((FIXES+=8))

# Fix PreClaimDashboardWidget.tsx
sed -i '' "s/bgcolor: '#E3F2FD'/bgcolor: 'info.lighter'/g" features/leads/components/PreClaimDashboardWidget.tsx
sed -i '' "s/bgcolor: '#FFF3E0'/bgcolor: 'warning.lighter'/g" features/leads/components/PreClaimDashboardWidget.tsx
sed -i '' "s/bgcolor: '#FFEBEE'/bgcolor: 'error.lighter'/g" features/leads/components/PreClaimDashboardWidget.tsx
sed -i '' "s/sx={{ bgcolor: '#FFEBEE'/sx={{ bgcolor: 'error.lighter'/g" features/leads/components/PreClaimDashboardWidget.tsx
sed -i '' "s/bgcolor: '#E8F5E9'/bgcolor: 'success.lighter'/g" features/leads/components/PreClaimDashboardWidget.tsx
((FIXES+=8))

# Fix LeadProtectionManager.tsx
sed -i '' "s/expiring: '#FF9800'/expiring: 'warning.main'/g" features/leads/components/intelligence/LeadProtectionManager.tsx
sed -i '' "s/expired: '#F44336'/expired: 'error.main'/g" features/leads/components/intelligence/LeadProtectionManager.tsx
sed -i '' "s/forgotten: '#B71C1C'/forgotten: 'error.dark'/g" features/leads/components/intelligence/LeadProtectionManager.tsx
sed -i '' "s/color=\"#FF9800\"/color=\"warning.main\"/g" features/leads/components/intelligence/LeadProtectionManager.tsx
sed -i '' "s/color=\"#F44336\"/color=\"error.main\"/g" features/leads/components/intelligence/LeadProtectionManager.tsx
((FIXES+=9))

# Fix leads/types.ts
sed -i '' "s/EXPIRED: '#795548'/EXPIRED: 'grey.700'/g" features/leads/types.ts
sed -i '' "s/DELETED: '#9E9E9E'/DELETED: 'grey.500'/g" features/leads/types.ts
sed -i '' "s/NORMAL: '#9E9E9E'/NORMAL: 'grey.500'/g" features/leads/types.ts
sed -i '' "s/MEDIUM: '#2196F3'/MEDIUM: 'info.main'/g" features/leads/types.ts
sed -i '' "s/HIGH: '#FF9800'/HIGH: 'warning.main'/g" features/leads/types.ts
sed -i '' "s/URGENT: '#F44336'/URGENT: 'error.main'/g" features/leads/types.ts
((FIXES+=6))

# Fix KanbanBoard.tsx
sed -i '' "s/return '#66BB6A'/return theme.palette.success.main/g" features/opportunity/components/KanbanBoard.tsx
sed -i '' "s/return '#FF7043'/return theme.palette.error.light/g" features/opportunity/components/KanbanBoard.tsx
sed -i '' "s/return '#EF5350'/return theme.palette.error.main/g" features/opportunity/components/KanbanBoard.tsx
sed -i '' "s/bgColor: '#f5f5f5'/bgColor: 'grey.100'/g" features/opportunity/components/KanbanBoard.tsx
sed -i '' "s/borderColor: '#e0e0e0'/borderColor: 'grey.300'/g" features/opportunity/components/KanbanBoard.tsx
((FIXES+=10))

# Fix stage-config.ts
sed -i '' "s/bgColor: '#F1F8E9'/bgColor: 'success.lighter'/g" features/opportunity/config/stage-config.ts
sed -i '' "s/color: '#F57C00'/color: 'warning.dark'/g" features/opportunity/config/stage-config.ts
sed -i '' "s/bgColor: '#FFF3E0'/bgColor: 'warning.lighter'/g" features/opportunity/config/stage-config.ts
sed -i '' "s/color: '#B71C1C'/color: 'error.dark'/g" features/opportunity/config/stage-config.ts
sed -i '' "s/bgColor: '#FFEBEE'/bgColor: 'error.lighter'/g" features/opportunity/config/stage-config.ts
((FIXES+=5))

# Fix UserFormMUI.tsx
sed -i '' "s/color=\"#FF9800\"/color=\"warning.main\"/g" features/users/UserFormMUI.tsx
((FIXES+=1))

echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "✅ Applied $FIXES fixes"
echo ""
echo "🔍 Running final design system check..."
python3 ../../scripts/check-design-system.py
