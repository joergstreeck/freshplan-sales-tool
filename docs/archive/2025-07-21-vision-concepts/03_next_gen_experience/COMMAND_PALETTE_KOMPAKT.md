# Command Palette - Spotlight für FreshPlan ⚡

**Feature Code:** V-UX-002  
**Feature-Typ:** 🎨 FRONTEND  
**Geschätzter Aufwand:** 10-12 Tage  
**Priorität:** VISION - Power User Feature  
**ROI:** 50% schnellere Navigation für Power User  

---

## 🎯 PROBLEM & LÖSUNG IN 30 SEKUNDEN

**Problem:** Klick, klick, klick... wo war nochmal die Funktion?  
**Lösung:** CMD+K → Tippen → Enter → Fertig!  
**Impact:** Alles erreichbar in <3 Sekunden  

---

## ⌨️ COMMAND EXAMPLES

```
"kunde müller"          → Öffnet Kunde Müller
"neue opportunity"      → Erstellt neue Opportunity  
"anruf thomas"         → Startet Anruf mit Thomas
"report pipeline"      → Öffnet Pipeline Report
"email follow up"      → Erstellt Follow-up E-Mail
"goto settings"        → Navigiert zu Einstellungen
"toggle dark"          → Schaltet Dark Mode um
```

---

## 🏃 IMPLEMENTATION KONZEPT

### Command Palette Core
```typescript
// Command System
interface Command {
  id: string;
  title: string;
  subtitle?: string;
  icon?: React.ReactNode;
  keywords: string[];
  category: CommandCategory;
  action: () => void | Promise<void>;
  shortcut?: string;
  available?: () => boolean;
}

export const CommandPalette: React.FC = () => {
  const [open, setOpen] = useState(false);
  const [search, setSearch] = useState('');
  const [results, setResults] = useState<Command[]>([]);
  
  // Global Shortcut
  useHotkeys('cmd+k, ctrl+k', () => setOpen(true));
  
  // Fuzzy Search mit Fuse.js
  const fuse = useMemo(() => new Fuse(getAllCommands(), {
    keys: ['title', 'keywords'],
    threshold: 0.3,
    includeScore: true
  }), []);
  
  useEffect(() => {
    if (search) {
      const searchResults = fuse.search(search);
      setResults(searchResults.map(r => r.item));
    } else {
      setResults(getRecentCommands());
    }
  }, [search]);
  
  return (
    <Dialog 
      open={open} 
      onClose={() => setOpen(false)}
      maxWidth="sm"
      fullWidth
      PaperProps={{
        sx: {
          position: 'fixed',
          top: '20%',
          transform: 'translateY(0)',
          borderRadius: 2,
          overflow: 'hidden'
        }
      }}
    >
      <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
        <TextField
          fullWidth
          autoFocus
          placeholder="Suche nach Kunden, Aktionen, Einstellungen..."
          value={search}
          onChange={(e) => setSearch(e.target.value)}
          onKeyDown={handleKeyNavigation}
          InputProps={{
            startAdornment: <SearchIcon />,
            endAdornment: (
              <Typography variant="caption" color="text.secondary">
                ESC zum Schließen
              </Typography>
            )
          }}
        />
      </Box>
      
      <List sx={{ maxHeight: 400, overflow: 'auto' }}>
        {results.map((command, index) => (
          <CommandItem
            key={command.id}
            command={command}
            selected={index === selectedIndex}
            onClick={() => executeCommand(command)}
          />
        ))}
      </List>
    </Dialog>
  );
};
```

### Dynamic Command Registry
```typescript
// Command Registry
class CommandRegistry {
  private commands = new Map<string, Command>();
  private contextCommands = new Map<string, Command[]>();
  
  register(command: Command) {
    this.commands.set(command.id, command);
  }
  
  registerContextual(context: string, commands: Command[]) {
    this.contextCommands.set(context, commands);
  }
  
  getAvailableCommands(context?: string): Command[] {
    const base = Array.from(this.commands.values())
      .filter(cmd => !cmd.available || cmd.available());
    
    if (context && this.contextCommands.has(context)) {
      return [...base, ...this.contextCommands.get(context)!];
    }
    
    return base;
  }
}

// Context-Aware Commands
export const useContextCommands = () => {
  const location = useLocation();
  const { selectedCustomer } = useCustomerContext();
  
  useEffect(() => {
    if (selectedCustomer) {
      commandRegistry.registerContextual('customer', [
        {
          id: 'call-customer',
          title: `Anruf ${selectedCustomer.name}`,
          icon: <PhoneIcon />,
          action: () => startCall(selectedCustomer.phone)
        },
        {
          id: 'email-customer',
          title: `E-Mail an ${selectedCustomer.name}`,
          icon: <EmailIcon />,
          action: () => composeEmail(selectedCustomer.email)
        }
      ]);
    }
  }, [selectedCustomer]);
};
```

### AI-Powered Commands
```typescript
// Natural Language Processing
export const AICommandParser = () => {
  const parseNaturalCommand = async (input: string): Promise<Command[]> => {
    // Local Intent Detection
    const intent = detectIntent(input);
    
    if (intent.confidence > 0.8) {
      return mapIntentToCommands(intent);
    }
    
    // Fallback to AI
    const response = await openai.createCompletion({
      model: 'gpt-3.5-turbo',
      prompt: `Parse this CRM command: "${input}"
      
      Available actions:
      - create: customer, opportunity, task, appointment
      - search: customers, opportunities, activities
      - navigate: dashboard, reports, settings
      - action: call, email, schedule
      
      Return JSON: { action: string, entity: string, parameters: {} }`,
      max_tokens: 100
    });
    
    const parsed = JSON.parse(response.data.choices[0].text);
    return generateCommandFromAI(parsed);
  };
};
```

---

## 🔗 INTEGRATION FEATURES

**Search Integration:**
- Customers, Opportunities, Activities
- Reports & Dashboards
- Settings & Preferences
- Recent Items

**Action Types:**
- Navigation Commands
- Create Commands
- Quick Actions
- Toggle Settings
- Run Reports

---

## ⚡ POWER FEATURES

1. **Smart Suggestions:** Lernt häufige Commands
2. **Alias System:** "opp" = "opportunity"
3. **Multi-Step:** "new customer thomas müller"
4. **Calculations:** "calc 15000 * 0.03"

---

## 📊 SUCCESS METRICS

- **Usage:** 100+ Commands/Tag bei Power Users
- **Speed:** Durchschnittlich 2.5s pro Aktion
- **Discovery:** 30% mehr Feature-Nutzung
- **Satisfaction:** "Kann nicht mehr ohne!"

---

## 🚀 IMPLEMENTATION PHASES

**Phase 1:** Basic Command Palette + Navigation  
**Phase 2:** CRUD Commands + Search  
**Phase 3:** Context-Aware Commands  
**Phase 4:** AI Natural Language  

---

**Command Reference:** [COMMAND_REFERENCE.md](./COMMAND_REFERENCE.md)  
**Keyboard Shortcuts:** [SHORTCUTS_GUIDE.md](./SHORTCUTS_GUIDE.md)