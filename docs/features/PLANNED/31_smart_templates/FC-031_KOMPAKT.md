# 🎯 FC-031 SMART TEMPLATES (KOMPAKT)

**Erstellt:** 19.07.2025  
**Status:** 📋 READY TO START  
**Feature-Typ:** 🔀 FULLSTACK  
**Priorität:** HIGH - Zeit sparen & Fehler vermeiden  
**Geschätzt:** 1 Tag  

---

## 🧠 WAS WIR BAUEN

**Problem:** Immer wieder gleiche Texte tippen  
**Lösung:** KI erkennt Muster & schlägt vor  
**Value:** 70% schnellere Angebotserstellung  

> **Business Case:** 1 Angebot in 5 statt 15 Minuten

### 🎯 Template-Typen:
- **Branchen-Templates:** Gastro, Hotel, Kantine
- **Anlass-Templates:** Erstangebot, Follow-up, Preiserhöhung
- **Produkt-Templates:** Obst, Gemüse, Spezialitäten
- **Auto-Learning:** System lernt aus Historie

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **Pattern Recognition:**
```typescript
// Erkenne was User will
const smartTemplates = {
  analyze: (context) => {
    // Kunde: Restaurant
    // Letzte Aktion: Preisanfrage
    // → Schlage Gastro-Erstangebot vor
    
    return {
      suggested: 'gastro_first_offer',
      confidence: 0.85,
      alternatives: ['gastro_follow_up', 'generic_offer']
    };
  }
};
```

### 2. **Template Engine:**
```typescript
const templateEngine = {
  // Vordefinierte Templates
  templates: {
    gastro_first_offer: {
      subject: 'Ihr Angebot für frische Qualität',
      body: `Sehr geehrte/r {contact.name},
      
vielen Dank für Ihre Anfrage! 
Gerne erstellen wir Ihnen ein Angebot für:

{auto_product_list}

Unsere Gastro-Vorteile:
- Lieferung ab 5 Uhr morgens
- Flexible Bestellmengen
- 24h Liefergarantie

{price_table}

Beste Grüße,
{user.name}`
    }
  },
  
  // Fülle Template mit Daten
  fill: (templateId, data) => {
    const template = templates[templateId];
    return fillPlaceholders(template, data);
  }
};
```

### 3. **Smart Suggestions UI:**
```typescript
const TemplateSelector = () => {
  const suggestions = useTemplatesSuggestions(context);
  
  return (
    <Card sx={{ mb: 2, border: '2px solid primary.main' }}>
      <CardHeader 
        avatar={<SmartToyIcon />}
        title="Empfohlene Vorlage"
        subheader={`${suggestions[0].confidence * 100}% Trefferquote`}
      />
      <CardContent>
        <Chip 
          label={suggestions[0].name}
          onClick={() => applyTemplate(suggestions[0].id)}
          color="primary"
        />
        <Typography variant="caption" display="block" sx={{ mt: 1 }}>
          Basierend auf: Branche + Historie + Kontext
        </Typography>
      </CardContent>
    </Card>
  );
};
```

---

## 🤖 KI-FEATURES

### Auto-Learning:
```typescript
// System lernt aus Nutzung
const learning = {
  // Track was funktioniert
  trackSuccess: (templateId, customerId, outcome) => {
    analytics.track('template_success', {
      template: templateId,
      customer: customerId,
      converted: outcome === 'won'
    });
  },
  
  // Verbessere Vorschläge
  improveModel: async () => {
    const patterns = await analyzeSuccessfulOffers();
    updateTemplateWeights(patterns);
  }
};
```

### Branchen-Intelligenz:
```typescript
const branchenTemplates = {
  gastro: {
    keywords: ['restaurant', 'gastro', 'küche'],
    products: ['frisches gemüse', 'kräuter', 'salate'],
    tone: 'professionell-freundlich',
    specials: ['Lieferung vor 6 Uhr', 'HACCP-konform']
  },
  
  hotel: {
    keywords: ['hotel', 'übernachtung', 'frühstück'],
    products: ['obst-auswahl', 'säfte', 'buffet-klassiker'],
    tone: 'service-orientiert',
    specials: ['Wochenend-Lieferung', 'Saisonware']
  }
};
```

---

## 📊 TEMPLATE BUILDER

### Visual Editor:
```typescript
const TemplateBuilder = () => (
  <Grid container spacing={2}>
    <Grid item xs={6}>
      {/* Template Editor */}
      <TextField
        multiline
        rows={20}
        value={template}
        onChange={handleEdit}
        placeholder="Verwende {kunde.name} für Platzhalter"
      />
    </Grid>
    <Grid item xs={6}>
      {/* Live Preview */}
      <Paper elevation={3} sx={{ p: 2 }}>
        <Typography variant="h6">Vorschau</Typography>
        <Divider sx={{ my: 1 }} />
        <div dangerouslySetInnerHTML={{ 
          __html: renderTemplate(template, sampleData) 
        }} />
      </Paper>
    </Grid>
  </Grid>
);
```

### Variable System:
```typescript
// Alle verfügbaren Variablen
const variables = {
  customer: ['name', 'company', 'address', 'branch'],
  contact: ['name', 'title', 'email', 'phone'],
  user: ['name', 'email', 'phone', 'signature'],
  products: ['list', 'categories', 'specialOffers'],
  pricing: ['table', 'discount', 'conditions']
};

// Auto-Complete für Variablen
<Autocomplete
  options={getAllVariables()}
  onSelect={(variable) => insertAtCursor(`{${variable}}`)}
/>
```

---

## 🔧 BACKEND INTEGRATION

### Template Storage:
```typescript
// PostgreSQL Schema
CREATE TABLE templates (
  id UUID PRIMARY KEY,
  name VARCHAR(255),
  category VARCHAR(100),
  content JSONB,
  variables TEXT[],
  usage_count INTEGER DEFAULT 0,
  success_rate DECIMAL(3,2),
  created_by UUID REFERENCES users(id)
);

// Quarkus Entity
@Entity
public class Template {
  @Id UUID id;
  String name;
  @Type(JsonBType.class)
  JsonNode content;
  @ElementCollection
  List<String> variables;
}
```

### Analytics:
```typescript
// Track Template Performance
const trackUsage = async (templateId: string, outcome: 'sent' | 'won' | 'lost') => {
  await api.post('/analytics/template-usage', {
    templateId,
    outcome,
    customerId: context.customerId,
    timestamp: new Date()
  });
};
```

---

## 🎯 SUCCESS METRICS

- **Zeit:** Angebot in < 5 Minuten
- **Qualität:** 0 Tippfehler
- **Conversion:** +20% durch bessere Templates
- **Learning:** System wird täglich besser

**Integration:** Tag 14-15 nach Mobile & WhatsApp!