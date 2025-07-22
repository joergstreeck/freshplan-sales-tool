# 🎛️ M3 SALES COCKPIT IMPLEMENTATION GUIDE

**Fokus:** Nur Sales Cockpit Enhancement  
**Zeilen:** <200 für optimale Claude-Arbeitsweise  
**Prerequisite:** [M3_TECH_CONCEPT.md](../M3_TECH_CONCEPT.md) gelesen  
**Zurück zur Übersicht:** [IMPLEMENTATION_GUIDE.md](../IMPLEMENTATION_GUIDE.md)  

---

## 🚀 PHASE 1: KI-INTEGRATION (Tag 1-2)

### 1.1: AI Priority Engine Service

```typescript
// services/aiPriorityEngine.ts
interface PriorityScore {
  taskId: string;
  score: number;        // 0-100
  factors: {
    deadline: number;   // Tage bis Deadline
    value: number;      // Geschätzter Deal-Wert  
    effort: number;     // Geschätzter Aufwand
    success: number;    // Win-Probability
  };
  reasoning: string;    // KI-Begründung
}

export class AIPriorityEngine {
  async calculatePriority(tasks: Task[]): Promise<PriorityScore[]> {
    const prompt = `
    Bewerte diese ${tasks.length} Sales-Aufgaben nach Priorität.
    Berücksichtige: Deadline, Deal-Wert, Aufwand, Erfolgswahrscheinlichkeit.
    
    Aufgaben: ${JSON.stringify(tasks)}
    
    Antwort als JSON Array mit: taskId, score (0-100), reasoning
    `;
    
    const response = await openai.createCompletion({
      model: "gpt-4",
      prompt,
      max_tokens: 2000
    });
    
    return JSON.parse(response.data.choices[0].text);
  }
}
```

### 1.2: Bestehende Hook erweitern

```typescript
// hooks/useSalesCockpit.ts (BESTEHEND ERWEITERN!)
import { AIPriorityEngine } from '../services/aiPriorityEngine';

export const useSalesCockpit = () => {
  const [aiEnabled, setAiEnabled] = useState(true);
  const priorityEngine = new AIPriorityEngine();
  
  const { data: cockpitData, refetch } = useQuery({
    queryKey: ['salesCockpit'],
    queryFn: async () => {
      const rawData = await salesCockpitService.getCockpitData();
      
      // NEU: KI-Priorisierung hinzufügen
      if (aiEnabled) {
        const priorities = await priorityEngine.calculatePriority(rawData.myDayItems);
        rawData.myDayItems = rawData.myDayItems
          .map(item => ({...item, aiScore: priorities.find(p => p.taskId === item.id)?.score}))
          .sort((a, b) => (b.aiScore || 0) - (a.aiScore || 0));
      }
      
      return rawData;
    },
    refetchInterval: 5 * 60 * 1000 // 5 Min Auto-Refresh
  });
  
  return { cockpitData, refetch, aiEnabled, setAiEnabled };
};
```

---

## 📊 PHASE 2: DATA INTEGRATION (Tag 3-4)

### 2.1: FocusListColumn erweitern

```typescript
// components/FocusListColumnMUI.tsx (BESTEHEND ERWEITERN!)
import { useCustomers } from '../../customer/customerQueries';
import { useOpportunities } from '../../opportunity/opportunityQueries';

export const FocusListColumnMUI: React.FC = () => {
  const { focusListItems } = useSalesCockpit();
  
  const { data: hotCustomers } = useCustomers({ 
    filter: { status: 'hot', assignedTo: currentUser.id } 
  });
  
  const { data: activeOpportunities } = useOpportunities({ 
    filter: { stage: ['negotiation', 'proposal'], assignedTo: currentUser.id } 
  });

  const enhancedFocusItems = useMemo(() => [
    ...focusListItems, // Bestehende Items
    ...hotCustomers?.map(customer => ({
      id: `customer-${customer.id}`,
      type: 'customer',
      title: customer.companyName,
      subtitle: `${customer.industry} • ${customer.potentialValue}€`,
      priority: customer.aiScore || 'medium',
      actions: [
        { label: 'Anrufen', action: () => initiateCall(customer.phone) },
        { label: 'E-Mail', action: () => composeEmail(customer.email) },
        { label: 'Calculator', action: () => openCalculatorForCustomer(customer.id) }
      ]
    })) || []
  ], [focusListItems, hotCustomers]);

  return (
    <Box sx={{ height: '100%', display: 'flex', flexDirection: 'column' }}>
      <Typography variant="h6" sx={{ p: 2 }}>
        Fokus-Liste {enhancedFocusItems.length > 0 && `(${enhancedFocusItems.length})`}
      </Typography>
      
      <Box sx={{ flex: 1, overflow: 'auto' }}>
        {enhancedFocusItems.map(item => (
          <FocusListItem 
            key={item.id} 
            item={item}
            onActionClick={(action) => action.action()} 
          />
        ))}
      </Box>
    </Box>
  );
};
```

---

## ⚡ PHASE 3: CALCULATOR INTEGRATION (Tag 5)

### 3.1: ActionCenter erweitern

```typescript
// components/ActionCenterColumnMUI.tsx (BESTEHEND ERWEITERN!)
import { CalculatorModal } from '../../calculator/components/CalculatorModal';

export const ActionCenterColumnMUI: React.FC = () => {
  const [calculatorOpen, setCalculatorOpen] = useState(false);
  const [calculatorContext, setCalculatorContext] = useState<CalculatorContext | null>(null);

  const quickActions = [
    {
      id: 'calculator',
      icon: <CalculateIcon />,
      label: 'Preisberechnung',
      color: 'primary',
      shortcut: 'Cmd+K',
      action: () => {
        setCalculatorContext({ 
          source: 'cockpit',
          prefilledData: getActiveCustomerData() 
        });
        setCalculatorOpen(true);
      }
    }
  ];

  return (
    <Box sx={{ height: '100%' }}>
      <ActionGrid actions={quickActions} />
      
      <CalculatorModal
        open={calculatorOpen}
        onClose={() => setCalculatorOpen(false)}
        context={calculatorContext}
        onSave={(result) => {
          addToFocusList({
            type: 'quote',
            title: `Angebot für ${result.customerName}`,
            value: result.totalPrice,
            actions: [
              { label: 'PDF senden', action: () => sendQuotePDF(result) },
              { label: 'Follow-up', action: () => scheduleFollowUp(result) }
            ]
          });
          setCalculatorOpen(false);
        }}
      />
    </Box>
  );
};
```

---

## 🎯 SUCCESS CRITERIA

**Nach M3 Enhancement:**
1. ✅ KI-Priorisierung in MyDay Column
2. ✅ Echte Customer/Opportunity Daten in FocusList
3. ✅ Calculator direkt aus ActionCenter
4. ✅ <200ms Column-Updates
5. ✅ 95+ Lighthouse Score

**Geschätzter Aufwand:** 5 Tage
**Voraussetzung:** Entscheidung D1 (KI-Provider) geklärt

---

## 📞 NÄCHSTE SCHRITTE

1. **Entscheidung D1 klären** - OpenAI vs. Local Model
2. **AIPriorityEngine implementieren** - Service Layer
3. **useSalesCockpit erweitern** - KI-Integration
4. **FocusListColumn erweitern** - Echte Daten
5. **ActionCenter erweitern** - Calculator Integration

**WICHTIG:** Bestehende Exzellenz erweitern, nicht neu erfinden!