# 🎨 FC-033: Visual Customer Cards - TECH CONCEPT

**Feature-Code:** FC-033  
**Feature-Name:** Visual Customer Cards  
**Kategorie:** Visual & UX Enhancement  
**Priorität:** MEDIUM  
**Geschätzter Aufwand:** 3 Tage  
**Status:** 📋 PLANNED - Tech Concept verfügbar  

---

## 🧠 CLAUDE WORKING SECTION (15-Min Context Chunk)

### ⚡ SOFORT STARTEN (2 Minuten):
```bash
# 1. Design System Components erstellen
cd frontend/src/components/cards
mkdir CustomerVisualCard && cd CustomerVisualCard

# 2. Material-UI Card mit Custom Styling
touch CustomerVisualCard.tsx CustomerCard.stories.tsx CustomerCard.test.tsx
```

### 📋 IMPLEMENTIERUNGS-CHECKLISTE:
- [ ] **Phase 1:** Visual Card Component (Tag 1)
- [ ] **Phase 2:** Photo Management & Avatar System (Tag 2)  
- [ ] **Phase 3:** Interactive Elements & Analytics (Tag 3)

---

## 🎯 FEATURE OVERVIEW

### Was ist Visual Customer Cards?
Modernisierte, visuell ansprechende Kundenkarten mit Fotos, Status-Indikatoren, Quick Actions und kompakter Informationsdarstellung für bessere Kundenübersicht.

### Business Value
- **Schnellere Kundenerkennung** durch visuelle Elemente
- **Verbesserte UX** durch moderne Card-basierte Layouts  
- **Höhere Produktivität** durch integrierte Quick Actions
- **Bessere Kundenbeziehungen** durch personalisierte Darstellung

### Erfolgsmetriken
- 40% schnellere Kundenerkennung in Listen
- 25% mehr Kundeninteraktionen durch Quick Actions
- Verbesserte User Satisfaction Scores für Kundenübersicht

---

## 🏗️ TECHNISCHE ARCHITEKTUR

### Tech Stack
- **Frontend:** React 18 + TypeScript + Material-UI v5
- **Styling:** CSS-in-JS mit MUI styled() API + Freshfoodz CI
- **Photos:** React Image mit Fallback zu Initials Avatars
- **Icons:** MUI Icons + Custom SVGs für Status
- **Animation:** Framer Motion für Micro-Interactions

### Component Architecture
```typescript
CustomerVisualCard/
├── CustomerVisualCard.tsx      # Main Card Component
├── CustomerAvatar.tsx          # Avatar with Photo/Initials
├── CustomerStatus.tsx          # Status Indicator Badge
├── QuickActionsFAB.tsx         # Floating Action Menu
├── CustomerMetrics.tsx         # Compact Metrics Display
├── hooks/
│   ├── useCustomerPhoto.ts     # Photo Management Hook
│   └── useQuickActions.ts      # Actions Handler Hook
└── styles/
    └── CustomerCard.styles.ts  # Styled Components
```

### Database Schema Extensions
```sql
-- Customer Photo Management
ALTER TABLE customers ADD COLUMN IF NOT EXISTS photo_url VARCHAR(500);
ALTER TABLE customers ADD COLUMN IF NOT EXISTS photo_upload_date TIMESTAMP;
ALTER TABLE customers ADD COLUMN IF NOT EXISTS has_custom_avatar BOOLEAN DEFAULT FALSE;

-- Customer Visual Preferences
CREATE TABLE IF NOT EXISTS customer_visual_settings (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    customer_id UUID NOT NULL REFERENCES customers(id) ON DELETE CASCADE,
    card_color_theme VARCHAR(20) DEFAULT 'default',
    priority_level INTEGER DEFAULT 0,
    visual_tags JSONB DEFAULT '[]',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

---

## 🎨 DESIGN SYSTEM IMPLEMENTATION

### Freshfoodz CI Compliance
```typescript
// Theme Integration
const visualCardTheme = {
  primary: '#94C456',      // Freshfoodz Grün
  secondary: '#004F7B',    // Freshfoodz Blau
  fonts: {
    headline: 'Antonio Bold',
    body: 'Poppins'
  },
  shadows: {
    card: '0 2px 8px rgba(0, 79, 123, 0.12)',
    hover: '0 4px 16px rgba(148, 196, 86, 0.16)'
  }
};

// Card Layout Variants
type CardVariant = 'compact' | 'standard' | 'detailed' | 'grid';
type CustomerPriority = 'vip' | 'high' | 'normal' | 'prospect';
```

### Visual Card Component
```typescript
interface CustomerVisualCardProps {
  customer: Customer;
  variant?: CardVariant;
  showQuickActions?: boolean;
  onCustomerClick?: (customer: Customer) => void;
  onActionClick?: (action: string, customer: Customer) => void;
}

export const CustomerVisualCard: React.FC<CustomerVisualCardProps> = ({
  customer,
  variant = 'standard',
  showQuickActions = true,
  onCustomerClick,
  onActionClick
}) => {
  const { photoUrl, isLoading: photoLoading } = useCustomerPhoto(customer.id);
  const { availableActions } = useQuickActions(customer);

  return (
    <StyledCustomerCard
      variant={variant}
      priority={customer.priority}
      onClick={() => onCustomerClick?.(customer)}
    >
      {/* Avatar Section */}
      <CardHeader>
        <CustomerAvatar 
          customer={customer}
          photoUrl={photoUrl}
          size={variant === 'compact' ? 'small' : 'medium'}
        />
        <CustomerStatus status={customer.status} />
      </CardHeader>

      {/* Customer Info */}
      <CardContent>
        <Typography variant="h6" fontFamily="Antonio Bold">
          {customer.companyName}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {customer.contactPerson} • {customer.location}
        </Typography>
        
        {variant !== 'compact' && (
          <CustomerMetrics customer={customer} />
        )}
      </CardContent>

      {/* Quick Actions */}
      {showQuickActions && (
        <CardActions>
          <QuickActionsFAB 
            actions={availableActions}
            onAction={(action) => onActionClick?.(action, customer)}
          />
        </CardActions>
      )}
    </StyledCustomerCard>
  );
};
```

### Photo Management System
```typescript
// Customer Photo Hook
export const useCustomerPhoto = (customerId: string) => {
  const [photoUrl, setPhotoUrl] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const loadPhoto = async () => {
      try {
        // 1. Check for custom uploaded photo
        const customPhoto = await customerApi.getPhoto(customerId);
        if (customPhoto) {
          setPhotoUrl(customPhoto.url);
          return;
        }

        // 2. Generate Gravatar URL from email
        const customer = await customerApi.getById(customerId);
        if (customer.email) {
          const gravatarUrl = generateGravatarUrl(customer.email);
          setPhotoUrl(gravatarUrl);
          return;
        }

        // 3. Fallback to initials avatar
        setPhotoUrl(null);
      } catch (error) {
        console.warn('Failed to load customer photo:', error);
        setPhotoUrl(null);
      } finally {
        setIsLoading(false);
      }
    };

    loadPhoto();
  }, [customerId]);

  return { photoUrl, isLoading };
};

// Avatar Component with Fallbacks
export const CustomerAvatar: React.FC<CustomerAvatarProps> = ({
  customer,
  photoUrl,
  size = 'medium'
}) => {
  const [imageError, setImageError] = useState(false);
  
  if (photoUrl && !imageError) {
    return (
      <Avatar 
        src={photoUrl}
        alt={customer.contactPerson}
        sx={{ width: size === 'small' ? 40 : 56, height: size === 'small' ? 40 : 56 }}
        onError={() => setImageError(true)}
      />
    );
  }

  // Fallback: Initials Avatar
  const initials = getCustomerInitials(customer);
  const backgroundColor = getCustomerColor(customer.id);
  
  return (
    <Avatar 
      sx={{ 
        width: size === 'small' ? 40 : 56, 
        height: size === 'small' ? 40 : 56,
        backgroundColor,
        fontFamily: 'Antonio Bold'
      }}
    >
      {initials}
    </Avatar>
  );
};
```

---

## 🔧 BACKEND INTEGRATION

### Photo Upload Service
```java
@ApplicationScoped
public class CustomerPhotoService {
    
    @Inject
    S3FileService s3FileService;
    
    @Inject
    CustomerRepository customerRepository;
    
    public CustomerPhotoResponse uploadPhoto(
        UUID customerId, 
        MultipartFormDataInput photoData
    ) {
        // 1. Validate image (max 5MB, JPG/PNG only)
        validatePhotoFile(photoData);
        
        // 2. Resize to standard dimensions (200x200)
        BufferedImage resizedImage = resizeImage(photoData.getFormDataMap());
        
        // 3. Upload to S3 with customer-specific path
        String s3Key = String.format("customer-photos/%s.jpg", customerId);
        String photoUrl = s3FileService.uploadImage(s3Key, resizedImage);
        
        // 4. Update customer record
        Customer customer = customerRepository.findById(customerId);
        customer.setPhotoUrl(photoUrl);
        customer.setPhotoUploadDate(LocalDateTime.now());
        customer.setHasCustomAvatar(true);
        customerRepository.persist(customer);
        
        return CustomerPhotoResponse.builder()
            .photoUrl(photoUrl)
            .uploadedAt(LocalDateTime.now())
            .build();
    }
    
    public Optional<CustomerPhotoResponse> getPhoto(UUID customerId) {
        Customer customer = customerRepository.findById(customerId);
        if (customer != null && customer.getPhotoUrl() != null) {
            return Optional.of(CustomerPhotoResponse.builder()
                .photoUrl(customer.getPhotoUrl())
                .uploadedAt(customer.getPhotoUploadDate())
                .build());
        }
        return Optional.empty();
    }
}
```

### REST Endpoints
```java
@Path("/api/customers/{customerId}/photo")
@Tag(name = "Customer Photos")
public class CustomerPhotoResource {
    
    @Inject
    CustomerPhotoService photoService;
    
    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadPhoto(
        @PathParam("customerId") UUID customerId,
        MultipartFormDataInput photoData
    ) {
        CustomerPhotoResponse response = photoService.uploadPhoto(customerId, photoData);
        return Response.ok(response).build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPhoto(@PathParam("customerId") UUID customerId) {
        return photoService.getPhoto(customerId)
            .map(photo -> Response.ok(photo).build())
            .orElse(Response.status(404).build());
    }
    
    @DELETE
    public Response deletePhoto(@PathParam("customerId") UUID customerId) {
        photoService.deletePhoto(customerId);
        return Response.noContent().build();
    }
}
```

---

## 🎛️ QUICK ACTIONS SYSTEM

### Action Configuration
```typescript
// Quick Actions Definition
interface QuickAction {
  id: string;
  label: string;
  icon: string;
  color: 'primary' | 'secondary' | 'success' | 'warning';
  requiresPermission?: string;
  disabled?: (customer: Customer) => boolean;
}

const CUSTOMER_QUICK_ACTIONS: QuickAction[] = [
  {
    id: 'call',
    label: 'Anrufen',
    icon: 'phone',
    color: 'primary',
    disabled: (customer) => !customer.phone
  },
  {
    id: 'email',
    label: 'E-Mail',
    icon: 'email',
    color: 'secondary',
    disabled: (customer) => !customer.email
  },
  {
    id: 'opportunity',
    label: 'Chance erstellen',
    icon: 'add_business',
    color: 'success',
    requiresPermission: 'opportunity:create'
  },
  {
    id: 'visit',
    label: 'Besuch planen',
    icon: 'event',
    color: 'warning'
  }
];

// Quick Actions Hook
export const useQuickActions = (customer: Customer) => {
  const { permissions } = useAuth();
  
  const availableActions = useMemo(() => {
    return CUSTOMER_QUICK_ACTIONS.filter(action => {
      // Check permissions
      if (action.requiresPermission && !permissions.includes(action.requiresPermission)) {
        return false;
      }
      
      // Check disabled state
      if (action.disabled && action.disabled(customer)) {
        return false;
      }
      
      return true;
    });
  }, [customer, permissions]);
  
  const executeAction = useCallback(async (actionId: string) => {
    switch (actionId) {
      case 'call':
        window.open(`tel:${customer.phone}`);
        break;
      case 'email':
        window.open(`mailto:${customer.email}`);
        break;
      case 'opportunity':
        // Navigate to opportunity creation with pre-filled customer
        navigate(`/opportunities/new?customerId=${customer.id}`);
        break;
      case 'visit':
        // Open visit planning modal
        openVisitPlanningModal(customer);
        break;
    }
  }, [customer]);
  
  return { availableActions, executeAction };
};
```

---

## 📊 ANALYTICS & METRICS

### Visual Card Metrics
```typescript
// Customer Metrics Component
export const CustomerMetrics: React.FC<{ customer: Customer }> = ({ customer }) => {
  const metrics = useCustomerMetrics(customer.id);
  
  return (
    <Box sx={{ mt: 1 }}>
      <Grid container spacing={1}>
        <Grid item xs={4}>
          <MetricChip 
            icon="euro"
            value={formatCurrency(metrics.totalValue)}
            label="Umsatz"
            color="success"
          />
        </Grid>
        <Grid item xs={4}>
          <MetricChip 
            icon="trending_up"
            value={metrics.opportunityCount}
            label="Chancen"
            color="primary"
          />
        </Grid>
        <Grid item xs={4}>
          <MetricChip 
            icon="schedule"
            value={formatRelativeTime(metrics.lastContact)}
            label="Kontakt"
            color="secondary"
          />
        </Grid>
      </Grid>
    </Box>
  );
};

// Metrics Hook
export const useCustomerMetrics = (customerId: string) => {
  return useQuery({
    queryKey: ['customer-metrics', customerId],
    queryFn: () => customerApi.getMetrics(customerId),
    staleTime: 5 * 60 * 1000 // 5 minutes
  });
};
```

### Backend Metrics Endpoint
```java
@GET
@Path("/{customerId}/metrics")
@Produces(MediaType.APPLICATION_JSON)
public CustomerMetricsResponse getCustomerMetrics(@PathParam("customerId") UUID customerId) {
    return CustomerMetricsResponse.builder()
        .totalValue(calculateTotalCustomerValue(customerId))
        .opportunityCount(countActiveOpportunities(customerId))
        .lastContact(getLastContactDate(customerId))
        .activityScore(calculateActivityScore(customerId))
        .build();
}
```

---

## 🧪 TESTING STRATEGY

### Component Tests
```typescript
// CustomerVisualCard.test.tsx
describe('CustomerVisualCard', () => {
  test('renders customer information correctly', () => {
    const mockCustomer = createMockCustomer();
    render(<CustomerVisualCard customer={mockCustomer} />);
    
    expect(screen.getByText(mockCustomer.companyName)).toBeInTheDocument();
    expect(screen.getByText(mockCustomer.contactPerson)).toBeInTheDocument();
  });
  
  test('displays photo when available', async () => {
    const mockCustomer = createMockCustomer({ photoUrl: 'https://example.com/photo.jpg' });
    render(<CustomerVisualCard customer={mockCustomer} />);
    
    const avatar = await screen.findByRole('img');
    expect(avatar).toHaveAttribute('src', 'https://example.com/photo.jpg');
  });
  
  test('shows initials avatar when no photo', () => {
    const mockCustomer = createMockCustomer({ 
      contactPerson: 'John Doe',
      photoUrl: null 
    });
    render(<CustomerVisualCard customer={mockCustomer} />);
    
    expect(screen.getByText('JD')).toBeInTheDocument();
  });
  
  test('executes quick actions correctly', async () => {
    const mockCustomer = createMockCustomer();
    const onActionClick = jest.fn();
    
    render(
      <CustomerVisualCard 
        customer={mockCustomer} 
        onActionClick={onActionClick}
      />
    );
    
    const callButton = screen.getByLabelText('Anrufen');
    fireEvent.click(callButton);
    
    expect(onActionClick).toHaveBeenCalledWith('call', mockCustomer);
  });
});
```

### Visual Regression Tests
```typescript
// Storybook Stories for Visual Testing
export default {
  title: 'Customer/VisualCard',
  component: CustomerVisualCard,
  argTypes: {
    variant: { control: 'select', options: ['compact', 'standard', 'detailed'] }
  }
} as ComponentMeta<typeof CustomerVisualCard>;

export const Standard = Template.bind({});
Standard.args = {
  customer: mockCustomers.standard,
  variant: 'standard'
};

export const WithPhoto = Template.bind({});
WithPhoto.args = {
  customer: mockCustomers.withPhoto,
  variant: 'standard'
};

export const VIPCustomer = Template.bind({});
VIPCustomer.args = {
  customer: mockCustomers.vip,
  variant: 'detailed'
};
```

---

## 🚀 DEPLOYMENT & ROLLOUT

### Rollout Strategy
1. **Phase 1:** Storybook Component Development & Testing
2. **Phase 2:** Integration in Customer List Views (A/B Test)
3. **Phase 3:** Dashboard & Search Results Integration
4. **Phase 4:** Mobile Responsive Optimization

### Performance Considerations
- **Image Optimization:** WebP format with JPG fallback
- **Lazy Loading:** React.lazy() for card components
- **Virtual Scrolling:** For large customer lists
- **Cache Strategy:** 5-minute cache for customer metrics

### Migration Plan
```typescript
// Feature Flag for Gradual Rollout
const useVisualCards = useFeatureFlag('visual-customer-cards');

return (
  <CustomerList>
    {customers.map(customer => 
      useVisualCards ? (
        <CustomerVisualCard key={customer.id} customer={customer} />
      ) : (
        <CustomerRowLegacy key={customer.id} customer={customer} />
      )
    )}
  </CustomerList>
);
```

---

## 📈 SUCCESS METRICS & KPIs

### User Experience Metrics
- **Recognition Speed:** Time to identify correct customer (-40% target)
- **Interaction Rate:** Quick actions usage (+25% target)
- **User Satisfaction:** NPS score for customer overview (+15 points)

### Technical Metrics  
- **Load Performance:** Card rendering <100ms
- **Memory Usage:** <50MB additional for 1000 cards
- **Error Rate:** <0.1% for photo loading failures

### Business Impact
- **Sales Efficiency:** Faster customer identification leads to more calls
- **Data Quality:** Photo uploads improve customer data completeness
- **User Adoption:** Visual appeal increases system usage

---

## 🔗 NAVIGATION & DEPENDENCIES

### 🧭 VOLLSTÄNDIGE FEATURE-NAVIGATION (40 Features)

#### 🟢 ACTIVE Features (9)
- [FC-008 Security Foundation](/docs/features/ACTIVE/01_security_foundation/FC-008_KOMPAKT.md)
- [M4 Opportunity Pipeline](/docs/features/ACTIVE/02_opportunity_pipeline/M4_KOMPAKT.md)
- [M8 Calculator Modal](/docs/features/ACTIVE/03_calculator_modal/M8_KOMPAKT.md)
- [FC-009 Permissions System](/docs/features/ACTIVE/04_permissions_system/FC-009_KOMPAKT.md)
- [M1 Navigation](/docs/features/ACTIVE/05_ui_foundation/M1_NAVIGATION_KOMPAKT.md)
- [M2 Quick Create](/docs/features/ACTIVE/05_ui_foundation/M2_QUICK_CREATE_KOMPAKT.md)
- [M3 Sales Cockpit](/docs/features/ACTIVE/05_ui_foundation/M3_SALES_COCKPIT_KOMPAKT.md)
- [M7 Settings](/docs/features/ACTIVE/05_ui_foundation/M7_SETTINGS_KOMPAKT.md)

#### 🔵 PLANNED Features (31)
- [FC-003 E-Mail Integration](/docs/features/PLANNED/06_email_integration/FC-003_TECH_CONCEPT.md)
- [FC-004 Verkäuferschutz](/docs/features/PLANNED/07_verkaeuferschutz/FC-004_TECH_CONCEPT.md)
- [FC-005 Xentral Integration](/docs/features/PLANNED/08_xentral_integration/FC-005_KOMPAKT.md)
- [FC-006 Mobile App](/docs/features/PLANNED/09_mobile_app/FC-006_TECH_CONCEPT.md)
- [FC-007 Chef-Dashboard](/docs/features/PLANNED/10_chef_dashboard/FC-007_TECH_CONCEPT.md)
- [FC-010 Customer Import](/docs/features/PLANNED/11_customer_import/FC-010_TECH_CONCEPT.md)
- [M5 Customer Refactor](/docs/features/PLANNED/12_customer_refactor_m5/M5_KOMPAKT.md)
- [M6 Analytics Module](/docs/features/PLANNED/13_analytics_m6/M6_KOMPAKT.md)
- [FC-012 Team Communication](/docs/features/PLANNED/14_team_communication/FC-012_KOMPAKT.md)
- [FC-013 Duplicate Detection](/docs/features/PLANNED/15_duplicate_detection/FC-013_KOMPAKT.md)
- [FC-014 Activity Timeline](/docs/features/PLANNED/16_activity_timeline/FC-014_KOMPAKT.md)
- [FC-015 Deal Loss Analysis](/docs/features/PLANNED/17_deal_loss_analysis/FC-015_KOMPAKT.md)
- [FC-016 Opportunity Cloning](/docs/features/PLANNED/18_opportunity_cloning/FC-016_KOMPAKT.md)
- [FC-017 Sales Gamification](/docs/features/PLANNED/99_sales_gamification/FC-017_KOMPAKT.md)
- [FC-018 Mobile PWA](/docs/features/PLANNED/09_mobile_app/FC-018_MOBILE_FIELD_SALES.md)
- [FC-019 Advanced Sales Metrics](/docs/features/PLANNED/19_advanced_metrics/FC-019_KOMPAKT.md)
- [FC-020 Quick Wins](/docs/features/PLANNED/20_quick_wins/FC-020_TECH_CONCEPT.md)
- [FC-021 Integration Hub](/docs/features/PLANNED/21_integration_hub/FC-021_TECH_CONCEPT.md)
- [FC-022 Mobile Light](/docs/features/PLANNED/22_mobile_light/FC-022_KOMPAKT.md)
- [FC-023 Event Sourcing](/docs/features/PLANNED/23_event_sourcing/FC-023_TECH_CONCEPT.md)
- [FC-024 File Management](/docs/features/PLANNED/24_file_management/FC-024_TECH_CONCEPT.md)
- [FC-025 DSGVO Compliance](/docs/features/PLANNED/25_dsgvo_compliance/FC-025_TECH_CONCEPT.md)
- [FC-026 Analytics Platform](/docs/features/PLANNED/26_analytics_platform/FC-026_TECH_CONCEPT.md)
- [FC-027 Magic Moments](/docs/features/PLANNED/27_magic_moments/FC-027_KOMPAKT.md)
- [FC-028 WhatsApp Business](/docs/features/PLANNED/28_whatsapp_integration/FC-028_KOMPAKT.md)
- [FC-029 Voice-First Interface](/docs/features/PLANNED/29_voice_first/FC-029_TECH_CONCEPT.md)
- [FC-030 One-Tap Actions](/docs/features/PLANNED/30_one_tap_actions/FC-030_TECH_CONCEPT.md)
- [FC-031 Smart Templates](/docs/features/PLANNED/31_smart_templates/FC-031_TECH_CONCEPT.md)
- [FC-032 Offline-First](/docs/features/PLANNED/32_offline_first/FC-032_TECH_CONCEPT.md)
- **→ FC-033 Visual Customer Cards** ← **SIE SIND HIER**
- [FC-034 Instant Insights](#) ← **NÄCHSTES TECH CONCEPT**
- [FC-035 Social Selling Helper](#) ← **SESSION 14**
- [FC-036 Beziehungsmanagement](#) ← **SESSION 14**

### 📋 ABHÄNGIGKEITEN
- **Benötigt:** FC-024 File Management (Foto-Upload)
- **Ergänzt:** M3 Sales Cockpit (Card Integration)
- **Basis für:** FC-034 Instant Insights (Visual Context)

---

**⏱️ GESCHÄTZTE IMPLEMENTIERUNGSZEIT:** 3 Tage  
**🎯 BUSINESS IMPACT:** Hoch (bessere UX, schnellere Kundenerkennung)  
**🔧 TECHNISCHE KOMPLEXITÄT:** Mittel (Photo Management, Card Design)  
**📊 ROI:** Break-even nach 2 Wochen durch Effizienzsteigerung