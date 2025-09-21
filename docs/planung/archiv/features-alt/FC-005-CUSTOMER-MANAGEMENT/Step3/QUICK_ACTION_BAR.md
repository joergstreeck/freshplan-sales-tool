# ⚡ Quick Action Bar - Schnellaktionen für Kontakte

**Status:** 📋 GEPLANT  
**Komponente von:** Smart Contact Cards  
**Letzte Aktualisierung:** 08.08.2025  
**Claude-Ready:** ✅ Vollständig navigierbar

## 🧭 NAVIGATION FÜR CLAUDE

**← Parent:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/SMART_CONTACT_CARDS.md`  
**→ Verwandt:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/MOBILE_CONTACT_ACTIONS.md`  
**↑ Übergeordnet:** `/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/README.md`

## ⚡ Quick Implementation

```bash
# CLAUDE: Erstelle diese Komponente als Teil von Smart Contact Cards
cd /Users/joergstreeck/freshplan-sales-tool
touch frontend/src/features/customers/components/contacts/QuickActionBar.tsx
```

## 💻 VOLLSTÄNDIGE IMPLEMENTIERUNG

**Datei:** `frontend/src/features/customers/components/contacts/QuickActionBar.tsx`

```typescript
// QuickActionBar.tsx - Schnellaktionen für Kontakte
import React, { useState } from 'react';
import {
  Box,
  IconButton,
  SpeedDial,
  SpeedDialAction,
  SpeedDialIcon,
  Tooltip,
  Badge,
  Menu,
  MenuItem,
  ListItemIcon,
  ListItemText,
  Divider
} from '@mui/material';
import {
  Phone as PhoneIcon,
  Email as EmailIcon,
  WhatsApp as WhatsAppIcon,
  Event as EventIcon,
  Note as NoteIcon,
  LocationOn as LocationIcon,
  AttachMoney as MoneyIcon,
  Assignment as TaskIcon,
  MoreVert as MoreIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Star as StarIcon,
  ContentCopy as CopyIcon,
  Share as ShareIcon,
  History as HistoryIcon,
  Videocam as VideoIcon,
  Chat as ChatIcon
} from '@mui/icons-material';
import { CustomerContact } from '../../types/contact.types';

export interface QuickAction {
  type: 'call' | 'email' | 'whatsapp' | 'meeting' | 'note' | 'task' | 'opportunity' | 'edit' | 'delete' | 'copy' | 'share';
  contactId: string;
  data?: any;
}

export interface QuickActionBarProps {
  contact: CustomerContact;
  onAction: (action: QuickAction) => void;
  variant?: 'compact' | 'expanded' | 'floating';
  showLabels?: boolean;
  disabled?: boolean;
}

export const QuickActionBar: React.FC<QuickActionBarProps> = ({
  contact,
  onAction,
  variant = 'compact',
  showLabels = false,
  disabled = false
}) => {
  const [moreMenuAnchor, setMoreMenuAnchor] = useState<null | HTMLElement>(null);
  const [speedDialOpen, setSpeedDialOpen] = useState(false);

  // Primäre Aktionen (immer sichtbar)
  const primaryActions = [
    {
      icon: PhoneIcon,
      label: 'Anrufen',
      action: 'call' as const,
      color: '#4CAF50',
      disabled: !contact.phone && !contact.mobile,
      badge: contact.lastContactDate ? 0 : 1 // Badge wenn noch nie kontaktiert
    },
    {
      icon: EmailIcon,
      label: 'E-Mail',
      action: 'email' as const,
      color: '#2196F3',
      disabled: !contact.email
    },
    {
      icon: WhatsAppIcon,
      label: 'WhatsApp',
      action: 'whatsapp' as const,
      color: '#25D366',
      disabled: !contact.mobile
    },
    {
      icon: EventIcon,
      label: 'Termin',
      action: 'meeting' as const,
      color: '#FF9800'
    }
  ];

  // Sekundäre Aktionen (im Menü)
  const secondaryActions = [
    {
      icon: NoteIcon,
      label: 'Notiz hinzufügen',
      action: 'note' as const
    },
    {
      icon: TaskIcon,
      label: 'Aufgabe erstellen',
      action: 'task' as const
    },
    {
      icon: MoneyIcon,
      label: 'Opportunity erstellen',
      action: 'opportunity' as const
    },
    {
      icon: VideoIcon,
      label: 'Video-Call',
      action: 'call' as const,
      data: { type: 'video' }
    },
    {
      icon: ChatIcon,
      label: 'Chat starten',
      action: 'note' as const,
      data: { type: 'chat' }
    }
  ];

  // Utility Aktionen
  const utilityActions = [
    {
      icon: CopyIcon,
      label: 'Kontakt kopieren',
      action: 'copy' as const
    },
    {
      icon: ShareIcon,
      label: 'Kontakt teilen',
      action: 'share' as const
    },
    {
      icon: HistoryIcon,
      label: 'Historie anzeigen',
      action: 'note' as const,
      data: { showHistory: true }
    }
  ];

  const handleAction = (actionType: QuickAction['type'], data?: any) => {
    onAction({
      type: actionType,
      contactId: contact.id,
      data
    });
    setMoreMenuAnchor(null);
    setSpeedDialOpen(false);
  };

  // Compact Variant (für Card-Ansicht)
  if (variant === 'compact') {
    return (
      <Box display="flex" gap={0.5} alignItems="center">
        {primaryActions.slice(0, 3).map((action) => (
          <Tooltip key={action.action} title={action.label}>
            <span>
              <IconButton
                size="small"
                disabled={disabled || action.disabled}
                onClick={() => handleAction(action.action)}
                sx={{
                  color: action.color,
                  '&:hover': {
                    backgroundColor: `${action.color}15`
                  }
                }}
              >
                <Badge
                  badgeContent={action.badge}
                  color="error"
                  variant="dot"
                  invisible={!action.badge}
                >
                  <action.icon fontSize="small" />
                </Badge>
              </IconButton>
            </span>
          </Tooltip>
        ))}
        
        <IconButton
          size="small"
          onClick={(e) => setMoreMenuAnchor(e.currentTarget)}
          disabled={disabled}
        >
          <MoreIcon fontSize="small" />
        </IconButton>
        
        <Menu
          anchorEl={moreMenuAnchor}
          open={Boolean(moreMenuAnchor)}
          onClose={() => setMoreMenuAnchor(null)}
          PaperProps={{
            sx: { minWidth: 200 }
          }}
        >
          {primaryActions.slice(3).concat(secondaryActions).map((action, index) => (
            <MenuItem
              key={`${action.action}-${index}`}
              onClick={() => handleAction(action.action, action.data)}
              disabled={action.disabled}
            >
              <ListItemIcon>
                <action.icon fontSize="small" />
              </ListItemIcon>
              <ListItemText>{action.label}</ListItemText>
            </MenuItem>
          ))}
          
          <Divider />
          
          {utilityActions.map((action, index) => (
            <MenuItem
              key={`${action.action}-${index}`}
              onClick={() => handleAction(action.action, action.data)}
            >
              <ListItemIcon>
                <action.icon fontSize="small" />
              </ListItemIcon>
              <ListItemText>{action.label}</ListItemText>
            </MenuItem>
          ))}
        </Menu>
      </Box>
    );
  }

  // Expanded Variant (für Detail-Ansicht)
  if (variant === 'expanded') {
    return (
      <Box display="flex" gap={1} flexWrap="wrap">
        {primaryActions.concat(secondaryActions.slice(0, 2)).map((action) => (
          <Tooltip key={action.action} title={action.disabled ? 'Nicht verfügbar' : ''}>
            <span>
              <IconButton
                disabled={disabled || action.disabled}
                onClick={() => handleAction(action.action)}
                sx={{
                  border: '1px solid',
                  borderColor: 'divider',
                  color: action.color || 'primary.main',
                  '&:hover': {
                    backgroundColor: action.color ? `${action.color}15` : 'action.hover',
                    borderColor: action.color || 'primary.main'
                  }
                }}
              >
                <action.icon />
              </IconButton>
            </span>
          </Tooltip>
        ))}
      </Box>
    );
  }

  // Floating Variant (SpeedDial für Mobile)
  return (
    <SpeedDial
      ariaLabel="Kontakt-Aktionen"
      sx={{ position: 'fixed', bottom: 16, right: 16 }}
      icon={<SpeedDialIcon />}
      open={speedDialOpen}
      onOpen={() => setSpeedDialOpen(true)}
      onClose={() => setSpeedDialOpen(false)}
    >
      {primaryActions.concat(secondaryActions).map((action) => (
        <SpeedDialAction
          key={action.action}
          icon={<action.icon />}
          tooltipTitle={action.label}
          tooltipOpen
          onClick={() => handleAction(action.action, action.data)}
          FabProps={{
            disabled: action.disabled,
            sx: {
              backgroundColor: action.color || 'primary.main',
              color: 'white',
              '&:hover': {
                backgroundColor: action.color || 'primary.dark'
              }
            }
          }}
        />
      ))}
    </SpeedDial>
  );
};

// Export für bessere Testbarkeit
export const QuickActionBarTestIds = {
  container: 'quick-action-bar',
  phoneButton: 'quick-action-phone',
  emailButton: 'quick-action-email',
  whatsappButton: 'quick-action-whatsapp',
  moreButton: 'quick-action-more',
  moreMenu: 'quick-action-menu'
};
```

## 🎨 USAGE BEISPIELE

### In Contact Card (Compact)
```typescript
<QuickActionBar
  contact={contact}
  onAction={handleQuickAction}
  variant="compact"
/>
```

### In Detail View (Expanded)
```typescript
<QuickActionBar
  contact={contact}
  onAction={handleQuickAction}
  variant="expanded"
  showLabels={true}
/>
```

### Mobile View (Floating)
```typescript
<QuickActionBar
  contact={contact}
  onAction={handleQuickAction}
  variant="floating"
/>
```

## 🔗 ACTION HANDLER BEISPIEL

```typescript
const handleQuickAction = async (action: QuickAction) => {
  switch (action.type) {
    case 'call':
      window.location.href = `tel:${contact.phone || contact.mobile}`;
      await logInteraction({
        contactId: action.contactId,
        type: 'CALL',
        direction: 'outgoing'
      });
      break;
      
    case 'email':
      window.location.href = `mailto:${contact.email}`;
      break;
      
    case 'whatsapp':
      window.open(`https://wa.me/${contact.mobile.replace(/\D/g, '')}`);
      break;
      
    case 'meeting':
      openMeetingScheduler(action.contactId);
      break;
      
    case 'note':
      openNoteDialog(action.contactId);
      break;
      
    default:
      console.log('Unhandled action:', action);
  }
};
```

## 📱 MOBILE OPTIMIZATION

- Touch-Targets: Minimum 44x44px
- Haptic Feedback auf Mobile Devices
- SpeedDial für Floating Action Button
- Swipe-to-reveal auf Cards

## 🧪 TEST CASES

```typescript
describe('QuickActionBar', () => {
  it('should disable phone action when no phone number', () => {
    const contact = { ...mockContact, phone: null, mobile: null };
    render(<QuickActionBar contact={contact} onAction={jest.fn()} />);
    
    const phoneButton = screen.getByTestId('quick-action-phone');
    expect(phoneButton).toBeDisabled();
  });
  
  it('should trigger action callback with correct data', () => {
    const onAction = jest.fn();
    render(<QuickActionBar contact={mockContact} onAction={onAction} />);
    
    fireEvent.click(screen.getByTestId('quick-action-email'));
    
    expect(onAction).toHaveBeenCalledWith({
      type: 'email',
      contactId: mockContact.id,
      data: undefined
    });
  });
});
```

---

**Status:** BEREIT FÜR IMPLEMENTIERUNG  
**Geschätzte Zeit:** 20 Minuten  
**Integration in:** SmartContactCard.tsx  
**Mobile-Ready:** ✅ Ja