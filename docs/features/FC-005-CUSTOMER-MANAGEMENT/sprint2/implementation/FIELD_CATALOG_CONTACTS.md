# 👥 Field Catalog - Strukturierte Kontakte

**Sprint:** 2  
**Status:** 🆕 Step 3 Felder  
**Kategorie:** Ansprechpartner & Beziehungsaufbau  

---

## 📍 Navigation
**← Teil 2:** [Field Catalog Services](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_SERVICES.md)  
**← Teil 1:** [Field Catalog Complete](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_COMPLETE.md)  
**↑ Sprint 2:** [Sprint 2 Overview](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/README.md)  

---

## 👤 Kontakt Basis-Felder

```json
{
  "contactBase": {
    "salutation": {
      "key": "salutation",
      "label": "Anrede",
      "fieldType": "select",
      "required": true,
      "category": "contact",
      "displayCondition": "always",
      "validBranches": ["all"],
      "options": [
        { "value": "herr", "label": "Herr" },
        { "value": "frau", "label": "Frau" },
        { "value": "divers", "label": "Divers" },
        { "value": "keine", "label": "Keine Anrede" }
      ],
      "sizeHint": "klein"
    },
    "title": {
      "key": "title",
      "label": "Titel",
      "fieldType": "text",
      "required": false,
      "category": "contact",
      "displayCondition": "always",
      "validBranches": ["all"],
      "placeholder": "z.B. Dr., Prof.",
      "maxLength": 50,
      "sizeHint": "kompakt"
    },
    "firstName": {
      "key": "firstName",
      "label": "Vorname",
      "fieldType": "text",
      "required": true,
      "category": "contact",
      "displayCondition": "always",
      "validBranches": ["all"],
      "maxLength": 100,
      "sizeHint": "mittel"
    },
    "lastName": {
      "key": "lastName",
      "label": "Nachname",
      "fieldType": "text",
      "required": true,
      "category": "contact",
      "displayCondition": "always",
      "validBranches": ["all"],
      "maxLength": 100,
      "sizeHint": "mittel"
    },
    "position": {
      "key": "position",
      "label": "Position/Funktion",
      "fieldType": "text",
      "required": false,
      "category": "contact",
      "displayCondition": "always",
      "validBranches": ["all"],
      "placeholder": "z.B. Geschäftsführer, F&B Manager",
      "salesRelevance": "HIGH",
      "sizeHint": "groß"
    },
    "decisionLevel": {
      "key": "decisionLevel",
      "label": "Entscheider-Ebene",
      "fieldType": "select",
      "required": false,
      "category": "contact",
      "displayCondition": "always",
      "validBranches": ["all"],
      "options": [
        { "value": "decision_maker", "label": "Entscheider" },
        { "value": "influencer", "label": "Beeinflusser" },
        { "value": "user", "label": "Anwender" },
        { "value": "gatekeeper", "label": "Gatekeeper" }
      ],
      "salesRelevance": "CRITICAL",
      "helpText": "Rolle im Kaufentscheidungsprozess"
    }
  }
}
```

---

## 📞 Kontaktmöglichkeiten

```json
{
  "contactMethods": {
    "emailBusiness": {
      "key": "emailBusiness",
      "label": "E-Mail geschäftlich",
      "fieldType": "email",
      "required": true,
      "category": "contact",
      "displayCondition": "always",
      "validBranches": ["all"],
      "sizeHint": "groß"
    },
    "phoneBusiness": {
      "key": "phoneBusiness",
      "label": "Telefon geschäftlich",
      "fieldType": "text",
      "required": false,
      "category": "contact",
      "displayCondition": "always",
      "validBranches": ["all"],
      "placeholder": "+49 30 123456",
      "sizeHint": "mittel"
    },
    "phoneMobile": {
      "key": "phoneMobile",
      "label": "Mobilnummer",
      "fieldType": "text",
      "required": false,
      "category": "contact",
      "displayCondition": "always",
      "validBranches": ["all"],
      "placeholder": "+49 151 12345678",
      "salesRelevance": "HIGH",
      "helpText": "Für dringende Rückfragen",
      "sizeHint": "mittel"
    },
    "preferredChannel": {
      "key": "preferredChannel",
      "label": "Bevorzugter Kommunikationskanal",
      "fieldType": "select",
      "required": false,
      "category": "contact",
      "displayCondition": "always",
      "validBranches": ["all"],
      "options": [
        { "value": "email", "label": "E-Mail" },
        { "value": "phone", "label": "Telefon" },
        { "value": "mobile", "label": "Mobil/WhatsApp" },
        { "value": "personal", "label": "Persönlich" }
      ],
      "salesRelevance": "HIGH"
    },
    "bestCallTime": {
      "key": "bestCallTime",
      "label": "Beste Erreichbarkeit",
      "fieldType": "select",
      "required": false,
      "category": "contact",
      "displayCondition": "preferredChannel === 'phone' || preferredChannel === 'mobile'",
      "validBranches": ["all"],
      "options": [
        { "value": "morning", "label": "Vormittags (8-12 Uhr)" },
        { "value": "afternoon", "label": "Nachmittags (12-17 Uhr)" },
        { "value": "evening", "label": "Abends (17-20 Uhr)" },
        { "value": "flexible", "label": "Flexibel" }
      ]
    }
  }
}
```

---

## 🤝 Beziehungsaufbau

```json
{
  "relationshipBuilding": {
    "birthday": {
      "key": "birthday",
      "label": "Geburtstag",
      "fieldType": "date",
      "required": false,
      "category": "relationship",
      "displayCondition": "always",
      "validBranches": ["all"],
      "helpText": "Für persönliche Glückwünsche",
      "privacyNote": "Optional - nur mit Einverständnis"
    },
    "contactNotes": {
      "key": "contactNotes",
      "label": "Persönliche Notizen",
      "fieldType": "textarea",
      "required": false,
      "category": "relationship",
      "displayCondition": "always",
      "validBranches": ["all"],
      "placeholder": "z.B. Hobbys, Interessen, Gesprächsthemen",
      "maxLength": 500,
      "rows": 3,
      "helpText": "Für besseren Beziehungsaufbau"
    },
    "contactTags": {
      "key": "contactTags",
      "label": "Tags",
      "fieldType": "multiselect",
      "required": false,
      "category": "relationship",
      "displayCondition": "always",
      "validBranches": ["all"],
      "options": [
        { "value": "vip", "label": "VIP" },
        { "value": "champion", "label": "Champion/Fürsprecher" },
        { "value": "skeptical", "label": "Skeptisch" },
        { "value": "technical", "label": "Technisch versiert" },
        { "value": "price_sensitive", "label": "Preissensitiv" },
        { "value": "quality_focused", "label": "Qualitätsfokussiert" },
        { "value": "innovation_open", "label": "Innovationsoffen" }
      ],
      "allowCustom": true,
      "salesRelevance": "MEDIUM"
    },
    "lastContactDate": {
      "key": "lastContactDate",
      "label": "Letzter Kontakt",
      "fieldType": "date",
      "required": false,
      "category": "relationship",
      "displayCondition": "always",
      "validBranches": ["all"],
      "autoUpdate": true,
      "helpText": "Wird automatisch aktualisiert"
    },
    "nextContactDate": {
      "key": "nextContactDate",
      "label": "Nächster geplanter Kontakt",
      "fieldType": "date",
      "required": false,
      "category": "relationship",
      "displayCondition": "always",
      "validBranches": ["all"],
      "triggerTask": true,
      "salesRelevance": "HIGH"
    }
  }
}
```

---

## 🏢 Standort-Zuordnung

```json
{
  "locationAssignment": {
    "assignedLocation": {
      "key": "assignedLocation",
      "label": "Zugeordneter Standort",
      "fieldType": "select",
      "required": true,
      "category": "location",
      "displayCondition": "always",
      "validBranches": ["all"],
      "dynamicOptions": "customer.locations",
      "defaultValue": "mainLocation",
      "helpText": "Zu welchem Standort gehört dieser Kontakt?"
    },
    "isMainContact": {
      "key": "isMainContact",
      "label": "Hauptansprechpartner für Standort",
      "fieldType": "checkbox",
      "required": false,
      "category": "location",
      "displayCondition": "always",
      "validBranches": ["all"],
      "salesRelevance": "HIGH"
    }
  }
}
```

---

## 📱 Social Media (Optional)

```json
{
  "socialMedia": {
    "linkedIn": {
      "key": "linkedIn",
      "label": "LinkedIn Profil",
      "fieldType": "url",
      "required": false,
      "category": "social",
      "displayCondition": "always",
      "validBranches": ["all"],
      "placeholder": "https://linkedin.com/in/...",
      "helpText": "Für Business-Networking"
    },
    "xing": {
      "key": "xing",
      "label": "XING Profil",
      "fieldType": "url",
      "required": false,
      "category": "social",
      "displayCondition": "always",
      "validBranches": ["all"],
      "placeholder": "https://xing.com/profile/..."
    }
  }
}
```

---

## 🔄 Migrations-Hinweis

Für bestehende Kontakte:
1. `contactPerson` → `firstName` + `lastName`
2. `email` → `emailBusiness`
3. `phone` → `phoneBusiness`
4. Neue Felder bekommen Default-Werte

---

**← Zurück:** [Field Catalog Services](/Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/implementation/FIELD_CATALOG_SERVICES.md)