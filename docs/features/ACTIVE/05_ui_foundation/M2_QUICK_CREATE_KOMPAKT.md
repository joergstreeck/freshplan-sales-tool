# ⚡ M2 QUICK CREATE (KOMPAKT)

**Erstellt:** 17.07.2025 16:40  
**Status:** 🔴 0% FERTIG - Noch nicht implementiert  
**Priorität:** 🔥 HOCH - Produktivitäts-Feature  

---

## 🧠 WAS WIR BAUEN

**Problem:** Keine schnelle Erstellung von Kunden/Opportunities  
**Lösung:** Floating Action Button + Quick-Create-Modals  
**Warum:** Sales-Team braucht schnelle Erfassung während Telefonaten  

> **Ziel:** Kunde/Opportunity in <30 Sekunden erstellen  
> **Context:** Funktioniert von jeder Seite aus  
> **Integration:** Mit Calculator + Pipeline + ActionCenter  

### 🎯 Feature-Vision:
1. **Floating Action Button** - Immer sichtbar, context-aware (neu)
2. **Quick Customer Modal** - Nur wichtigste Felder (neu)  
3. **Quick Opportunity Modal** - Mit Auto-Suggestions (neu)
4. **Smart Defaults** - Basierend auf User-Kontext (neu)

---

## 🚀 SOFORT LOSLEGEN (15 MIN)

### 1. **FAB-Position testen:**
```bash
# Verfügbare Layout-Integration:
cat frontend/src/components/layout/MainLayoutV2.tsx | grep -A5 -B5 "children"

# MUI FAB-Komponente:
grep -r "Fab" frontend/src/ | head -5
# → Noch keine FAB-Integration gefunden
```

### 2. **Quick Create Modal planen:**
```typescript
// Quick Create Modal Interface:
interface QuickCreateModalProps {
  type: 'customer' | 'opportunity' | 'task';
  context?: {
    customerId?: string;
    currentPage?: string;
    prefillData?: any;
  };
  onSuccess: (created: any) => void;
  onCancel: () => void;
}

// Smart Defaults basierend auf Context:
const getSmartDefaults = (context: QuickCreateContext) => {
  if (context.currentPage === 'cockpit') {
    return { priority: 'high', assignedTo: currentUser.id };
  }
  // ... weitere Context-Logik
};
```

### 3. **Integration-Punkte definieren:**
```bash
# Wo überall Quick Create verfügbar sein soll:
echo "- Sales Cockpit (/cockpit)"
echo "- Customer Liste (/customers)"
echo "- Pipeline (/pipeline)"
echo "- Calculator (nach Berechnung)"
echo "- Überall via FAB"
```

**Geschätzt: 3-4 Tage für vollständige Quick Create Integration**

---

## 📋 WAS IST FERTIG?

❌ **Floating Action Button** - Noch nicht implementiert  
❌ **Quick Create Modals** - Noch nicht implementiert  
❌ **Smart Defaults** - Noch nicht implementiert  
❌ **Context Integration** - Noch nicht implementiert  
❌ **Form Validation** - Noch nicht implementiert  

**🎯 KOMPLETT NEUE ENTWICKLUNG ERFORDERLICH!**

---

## 🚨 WAS BRAUCHEN WIR FÜR QUICK CREATE?

✅ **FAB Integration** → Floating Action Button in MainLayoutV2  
✅ **Modal Framework** → Quick Create Modal-Komponenten  
✅ **Smart Forms** → Context-aware Formulare mit Validation  
✅ **API Integration** → POST /api/customers, /api/opportunities  
✅ **Success Handling** → Redirect + Notification nach Erstellung  

---

## 🔗 VOLLSTÄNDIGE DETAILS

**Direkter Implementation Guide:** [M2_QUICK_CREATE_GUIDE.md](./guides/M2_QUICK_CREATE_GUIDE.md)
- Phase 1: Floating Action Button (Tag 1)
- Phase 2: Quick Create Modals (Tag 2-3)
- Phase 3: Smart Defaults (Tag 4)
- Vollständige Code-Beispiele + Copy-paste ready

**Navigation:** [IMPLEMENTATION_GUIDE.md](./IMPLEMENTATION_GUIDE.md) (Übersicht aller Module)

**Entscheidungen:** [DECISION_LOG.md](./DECISION_LOG.md)
- FAB-Position: Bottom-Right vs. Context-sensitive?
- Modal-Style: Fullscreen vs. Centered vs. Slide-up?
- Validation: Client-side vs. Server-side vs. Hybrid?

---

## 📞 NÄCHSTE SCHRITTE

1. **FAB Integration** - Floating Action Button in MainLayoutV2
2. **Modal Framework** - Quick Create Modal-Komponenten entwickeln  
3. **Smart Forms** - Context-aware Formulare mit Validation
4. **API Integration** - Backend-Endpoints für Quick Create

**WICHTIG:** Komplett neue Entwicklung - keine bestehende Basis!

**KRITISCH:** Status ist 0% fertig - vollständige Neuentwicklung erforderlich!