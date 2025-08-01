# 🎴 Smart Contact Cards - Intelligente Kontakt-UI

**Phase:** 1 - Foundation  
**Tag:** 3 der Woche 1  
**Status:** 🎯 Ready for Implementation  

## 🧭 Navigation

**← Zurück:** [Frontend Foundation](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/FRONTEND_FOUNDATION.md)  
**→ Nächster:** [Mobile Contact Actions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/MOBILE_CONTACT_ACTIONS.md)  
**↑ Übergeordnet:** [Step 3 Main Guide](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md)  

## 🎯 Vision: Kontakte auf einen Blick

**Smart Contact Cards** verwandeln statische Kontaktlisten in **dynamische Beziehungs-Dashboards**:

> "Jede Karte erzählt die Geschichte einer Geschäftsbeziehung"

## 🎨 Contact Card Design System

### Card Layout Architecture

```typescript
// types/contactCard.types.ts
export interface ContactCardProps {
  contact: Contact;
  warmth?: RelationshipWarmth;
  onEdit: (contact: Contact) => void;
  onDelete: (contactId: string) => void;
  onSetPrimary: (contactId: string) => void;
  onAssignLocation: (contactId: string) => void;
  onQuickAction?: (action: QuickAction) => void;
}

export interface CardVisualState {
  borderStyle: 'solid' | 'dashed';
  borderWidth: number;
  borderColor: string;
  elevation: number;
  glowEffect?: boolean;
}
```

### Smart Contact Card Component

```typescript
// components/SmartContactCard.tsx
import React from 'react';
import {
  Card,
  CardContent,
  CardActions,
  Typography,
  Box,
  Chip,
  IconButton,
  Menu,
  MenuItem,
  Avatar,
  Tooltip,
  LinearProgress
} from '@mui/material';
import {
  MoreVert as MoreVertIcon,
  Star as StarIcon,
  StarBorder as StarBorderIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  LocationOn as LocationIcon,
  Phone as PhoneIcon,
  Email as EmailIcon,
  WhatsApp as WhatsAppIcon,
  Cake as CakeIcon
} from '@mui/icons-material';
import { WarmthIndicator } from './WarmthIndicator';
import { QuickActionBar } from './QuickActionBar';
import type { Contact, RelationshipWarmth } from '../types';

export const SmartContactCard: React.FC<ContactCardProps> = ({
  contact,
  warmth,
  onEdit,
  onDelete,
  onSetPrimary,
  onAssignLocation,
  onQuickAction
}) => {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);
  const [isHovered, setIsHovered] = React.useState(false);
  
  // Visual State basierend auf Kontext
  const getCardVisualState = (): CardVisualState => {
    const state: CardVisualState = {
      borderStyle: 'solid',
      borderWidth: 1,
      borderColor: 'divider',
      elevation: 1
    };
    
    // Primary Contact Highlighting
    if (contact.isPrimary) {
      state.borderWidth = 2;
      state.borderColor = 'primary.main';
      state.glowEffect = true;
    }
    
    // Warmth-based Border Color
    if (warmth) {
      const warmthColors = {
        HOT: '#FF4444',
        WARM: '#FF8800',
        COOLING: '#FFBB00',
        COLD: '#666666'
      };
      state.borderColor = warmthColors[warmth.temperature] || state.borderColor;
    }
    
    // Birthday Highlighting
    if (isBirthdayUpcoming(contact.birthday)) {
      state.glowEffect = true;
    }
    
    return state;
  };
  
  const visualState = getCardVisualState();
  
  const getInitials = () => {
    return `${contact.firstName[0]}${contact.lastName[0]}`.toUpperCase();
  };
  
  const getAvatarColor = () => {
    // Consistent color based on name
    const colors = ['#94C456', '#004F7B', '#FF8800', '#2196F3', '#9C27B0'];
    const index = (contact.firstName.charCodeAt(0) + contact.lastName.charCodeAt(0)) % colors.length;
    return colors[index];
  };
  
  const getDecisionLevelChip = () => {
    const config = {
      executive: { label: 'Geschäftsführung', color: 'error' as const },
      manager: { label: 'Abteilungsleitung', color: 'warning' as const },
      operational: { label: 'Operativ', color: 'info' as const },
      influencer: { label: 'Beeinflusser', color: 'default' as const }
    };
    
    const level = config[contact.decisionLevel || 'influencer'];
    return (
      <Chip
        label={level.label}
        size="small"
        color={level.color}
        sx={{ fontWeight: 'medium' }}
      />
    );
  };
  
  const isBirthdayUpcoming = (birthday?: string): boolean => {
    if (!birthday) return false;
    const today = new Date();
    const birthdayDate = new Date(birthday);
    birthdayDate.setFullYear(today.getFullYear());
    const daysUntil = Math.floor((birthdayDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
    return daysUntil >= 0 && daysUntil <= 7;
  };
  
  return (
    <Card
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      sx={{
        position: 'relative',
        border: `${visualState.borderWidth}px ${visualState.borderStyle}`,
        borderColor: visualState.borderColor,
        transition: 'all 0.3s cubic-bezier(0.4, 0, 0.2, 1)',
        cursor: 'pointer',
        overflow: 'visible',
        ...(isHovered && {
          transform: 'translateY(-4px)',
          boxShadow: 6,
        }),
        ...(visualState.glowEffect && {
          '&::before': {
            content: '""',
            position: 'absolute',
            top: -2,
            left: -2,
            right: -2,
            bottom: -2,
            background: `linear-gradient(45deg, ${visualState.borderColor}, transparent)`,
            borderRadius: 'inherit',
            opacity: 0.3,
            animation: 'pulse 2s infinite'
          }
        })
      }}
    >
      {/* Status Badges */}
      <Box sx={{ position: 'absolute', top: 8, right: 8, display: 'flex', gap: 0.5 }}>
        {contact.isPrimary && (
          <Tooltip title="Hauptkontakt">
            <Chip
              icon={<StarIcon />}
              label="Primary"
              color="primary"
              size="small"
              sx={{ fontWeight: 'bold' }}
            />
          </Tooltip>
        )}
        {isBirthdayUpcoming(contact.birthday) && (
          <Tooltip title={`Geburtstag in ${getDaysUntilBirthday(contact.birthday)} Tagen`}>
            <Chip
              icon={<CakeIcon />}
              label="🎂"
              color="secondary"
              size="small"
            />
          </Tooltip>
        )}
      </Box>
      
      <CardContent sx={{ pb: 1 }}>
        {/* Header Section */}
        <Box display="flex" alignItems="flex-start" gap={2}>
          <Avatar
            sx={{
              bgcolor: getAvatarColor(),
              width: 64,
              height: 64,
              fontSize: '1.5rem',
              fontWeight: 'bold'
            }}
          >
            {getInitials()}
          </Avatar>
          
          <Box flex={1}>
            <Typography variant="h6" component="div" sx={{ fontWeight: 'bold' }}>
              {contact.salutation} {contact.title} {contact.firstName} {contact.lastName}
            </Typography>
            
            <Typography color="text.secondary" variant="body2">
              {contact.position || 'Position nicht angegeben'}
            </Typography>
            
            <Box display="flex" gap={1} mt={1} flexWrap="wrap">
              {getDecisionLevelChip()}
              {contact.assignedLocationName && (
                <Chip
                  icon={<LocationIcon />}
                  label={contact.assignedLocationName}
                  size="small"
                  variant="outlined"
                />
              )}
            </Box>
          </Box>
          
          <IconButton
            onClick={(e) => setAnchorEl(e.currentTarget)}
            size="small"
          >
            <MoreVertIcon />
          </IconButton>
        </Box>
        
        {/* Contact Info Section */}
        <Box mt={2} display="flex" flexDirection="column" gap={0.5}>
          {contact.email && (
            <Box display="flex" alignItems="center" gap={1}>
              <EmailIcon fontSize="small" color="action" />
              <Typography variant="body2" noWrap>
                {contact.email}
              </Typography>
            </Box>
          )}
          
          {contact.phone && (
            <Box display="flex" alignItems="center" gap={1}>
              <PhoneIcon fontSize="small" color="action" />
              <Typography variant="body2">
                {contact.phone}
              </Typography>
            </Box>
          )}
          
          {contact.mobile && (
            <Box display="flex" alignItems="center" gap={1}>
              <WhatsAppIcon fontSize="small" color="action" />
              <Typography variant="body2">
                {contact.mobile}
              </Typography>
            </Box>
          )}
        </Box>
        
        {/* Warmth Indicator */}
        {warmth && (
          <Box mt={2}>
            <WarmthIndicator warmth={warmth} size="small" showDetails={isHovered} />
          </Box>
        )}
      </CardContent>
      
      {/* Quick Actions (visible on hover) */}
      {isHovered && onQuickAction && (
        <CardActions sx={{ justifyContent: 'center', py: 1 }}>
          <QuickActionBar
            contact={contact}
            onAction={onQuickAction}
            variant="compact"
          />
        </CardActions>
      )}
      
      {/* Context Menu */}
      <Menu
        anchorEl={anchorEl}
        open={Boolean(anchorEl)}
        onClose={() => setAnchorEl(null)}
        PaperProps={{
          sx: { minWidth: 200 }
        }}
      >
        <MenuItem onClick={() => {
          onEdit(contact);
          setAnchorEl(null);
        }}>
          <EditIcon fontSize="small" sx={{ mr: 1 }} />
          Bearbeiten
        </MenuItem>
        
        {!contact.isPrimary && (
          <MenuItem onClick={() => {
            onSetPrimary(contact.id);
            setAnchorEl(null);
          }}>
            <StarBorderIcon fontSize="small" sx={{ mr: 1 }} />
            Als Hauptkontakt
          </MenuItem>
        )}
        
        <MenuItem onClick={() => {
          onAssignLocation(contact.id);
          setAnchorEl(null);
        }}>
          <LocationIcon fontSize="small" sx={{ mr: 1 }} />
          Filiale zuordnen
        </MenuItem>
        
        <MenuItem
          onClick={() => {
            if (confirm(`Möchten Sie ${contact.firstName} ${contact.lastName} wirklich löschen?`)) {
              onDelete(contact.id);
            }
            setAnchorEl(null);
          }}
          sx={{ color: 'error.main' }}
        >
          <DeleteIcon fontSize="small" sx={{ mr: 1 }} />
          Löschen
        </MenuItem>
      </Menu>
    </Card>
  );
};
```

## 📱 Responsive Grid System

### Smart Grid Hook

```typescript
// hooks/useContactGrid.ts
import { useTheme } from '@mui/material/styles';
import { useMediaQuery } from '@mui/material';

export const useContactGrid = () => {
  const theme = useTheme();
  const isXs = useMediaQuery(theme.breakpoints.down('sm'));
  const isSm = useMediaQuery(theme.breakpoints.between('sm', 'md'));
  const isMd = useMediaQuery(theme.breakpoints.between('md', 'lg'));
  const isLg = useMediaQuery(theme.breakpoints.between('lg', 'xl'));
  
  const getGridConfig = () => {
    if (isXs) return { columns: 1, spacing: 1 };
    if (isSm) return { columns: 2, spacing: 2 };
    if (isMd) return { columns: 3, spacing: 2 };
    if (isLg) return { columns: 4, spacing: 3 };
    return { columns: 5, spacing: 3 }; // xl and up
  };
  
  const config = getGridConfig();
  
  return {
    ...config,
    gridProps: {
      container: true,
      spacing: config.spacing,
      sx: {
        // Ensure equal height cards
        '& > .MuiGrid-item': {
          display: 'flex',
          '& > *': {
            flex: 1
          }
        }
      }
    }
  };
};
```

### Contact Grid Container

```typescript
// components/ContactGridContainer.tsx
export const ContactGridContainer: React.FC<{
  contacts: Contact[];
  warmthData?: Map<string, RelationshipWarmth>;
  onContactAction: (action: ContactAction) => void;
}> = ({ contacts, warmthData, onContactAction }) => {
  const { gridProps } = useContactGrid();
  const [viewMode, setViewMode] = useState<'grid' | 'list'>('grid');
  
  // Smart Sorting
  const sortedContacts = useMemo(() => {
    return [...contacts].sort((a, b) => {
      // Primary contacts first
      if (a.isPrimary && !b.isPrimary) return -1;
      if (!a.isPrimary && b.isPrimary) return 1;
      
      // Then by warmth (if available)
      const warmthA = warmthData?.get(a.id);
      const warmthB = warmthData?.get(b.id);
      if (warmthA && warmthB) {
        return warmthB.score - warmthA.score;
      }
      
      // Finally alphabetically
      return `${a.lastName} ${a.firstName}`.localeCompare(`${b.lastName} ${b.firstName}`);
    });
  }, [contacts, warmthData]);
  
  if (contacts.length === 0) {
    return <EmptyContactState onAddContact={() => onContactAction({ type: 'add' })} />;
  }
  
  return (
    <Box>
      {/* View Mode Toggle */}
      <Box display="flex" justifyContent="flex-end" mb={2}>
        <ToggleButtonGroup
          value={viewMode}
          exclusive
          onChange={(_, value) => value && setViewMode(value)}
          size="small"
        >
          <ToggleButton value="grid">
            <GridViewIcon />
          </ToggleButton>
          <ToggleButton value="list">
            <ListIcon />
          </ToggleButton>
        </ToggleButtonGroup>
      </Box>
      
      {viewMode === 'grid' ? (
        <Grid {...gridProps}>
          {sortedContacts.map(contact => (
            <Grid item key={contact.id} xs={12} sm={6} md={4} lg={3}>
              <SmartContactCard
                contact={contact}
                warmth={warmthData?.get(contact.id)}
                onEdit={(c) => onContactAction({ type: 'edit', contact: c })}
                onDelete={(id) => onContactAction({ type: 'delete', contactId: id })}
                onSetPrimary={(id) => onContactAction({ type: 'setPrimary', contactId: id })}
                onAssignLocation={(id) => onContactAction({ type: 'assignLocation', contactId: id })}
                onQuickAction={(action) => onContactAction({ type: 'quickAction', action })}
              />
            </Grid>
          ))}
        </Grid>
      ) : (
        <ContactListView
          contacts={sortedContacts}
          warmthData={warmthData}
          onContactAction={onContactAction}
        />
      )}
    </Box>
  );
};
```

## 🎨 Empty State Pattern

```typescript
// components/EmptyContactState.tsx
export const EmptyContactState: React.FC<{
  onAddContact: () => void;
}> = ({ onAddContact }) => {
  return (
    <Box
      sx={{
        textAlign: 'center',
        py: 8,
        px: 3,
        bgcolor: 'background.paper',
        borderRadius: 2,
        border: '2px dashed',
        borderColor: 'divider',
        transition: 'all 0.3s ease',
        '&:hover': {
          borderColor: 'primary.main',
          bgcolor: 'action.hover'
        }
      }}
    >
      <Avatar
        sx={{
          width: 80,
          height: 80,
          bgcolor: 'primary.light',
          margin: '0 auto',
          mb: 3
        }}
      >
        <PersonAddIcon sx={{ fontSize: 40 }} />
      </Avatar>
      
      <Typography variant="h6" gutterBottom>
        Noch keine Ansprechpartner vorhanden
      </Typography>
      
      <Typography variant="body2" color="text.secondary" paragraph>
        Fügen Sie den ersten Kontakt hinzu, um die Beziehungspflege zu starten
      </Typography>
      
      <Button
        variant="contained"
        startIcon={<AddIcon />}
        onClick={onAddContact}
        size="large"
        sx={{ mt: 2 }}
      >
        Ersten Ansprechpartner hinzufügen
      </Button>
    </Box>
  );
};
```

## 🎯 Visual Feedback & Animations

```css
/* styles/contactCard.module.css */
@keyframes pulse {
  0% {
    opacity: 0.3;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.02);
  }
  100% {
    opacity: 0.3;
    transform: scale(1);
  }
}

.contactCard {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.contactCard:hover {
  transform: translateY(-4px);
}

.primaryGlow {
  position: relative;
}

.primaryGlow::after {
  content: '';
  position: absolute;
  top: -4px;
  left: -4px;
  right: -4px;
  bottom: -4px;
  background: linear-gradient(45deg, #94C456, transparent);
  border-radius: inherit;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.primaryGlow:hover::after {
  opacity: 0.3;
}
```

## 🧪 Testing

```typescript
// __tests__/SmartContactCard.test.tsx
describe('SmartContactCard', () => {
  it('should highlight primary contacts', () => {
    const primaryContact = createMockContact({ isPrimary: true });
    render(<SmartContactCard contact={primaryContact} {...mockProps} />);
    
    const card = screen.getByRole('article');
    expect(card).toHaveStyle({ borderWidth: '2px' });
    expect(screen.getByText('Primary')).toBeInTheDocument();
  });
  
  it('should show birthday indicator when upcoming', () => {
    const birthdayContact = createMockContact({
      birthday: addDays(new Date(), 3).toISOString()
    });
    render(<SmartContactCard contact={birthdayContact} {...mockProps} />);
    
    expect(screen.getByText('🎂')).toBeInTheDocument();
  });
  
  it('should display warmth indicator when data available', () => {
    const warmth: RelationshipWarmth = {
      temperature: 'HOT',
      score: 85,
      suggestions: []
    };
    render(<SmartContactCard contact={mockContact} warmth={warmth} {...mockProps} />);
    
    expect(screen.getByTestId('warmth-indicator')).toBeInTheDocument();
  });
});
```

## 🎯 Success Metrics

### Performance:
- **Render Time:** < 16ms per card
- **Grid Reflow:** < 100ms on resize
- **Animation FPS:** 60fps constant

### UX:
- **Visual Hierarchy:** Clear primary/secondary distinction
- **Information Density:** All key info visible
- **Interaction Feedback:** < 100ms response

---

**Nächster Schritt:** [→ Mobile Contact Actions](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/MOBILE_CONTACT_ACTIONS.md)

**Smart Cards = Beziehungen visualisiert! 🎴✨**