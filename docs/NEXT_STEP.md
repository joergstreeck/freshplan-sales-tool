# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE: 

**STEP 3 IMPLEMENTATION GUIDES FERTIG!**

**Stand 31.07.2025 21:00:**
- ✅ Team-Diskussion zu Contact Management analysiert
- ✅ Pragmatische Architektur-Entscheidung getroffen (CRUD statt Event Sourcing)
- ✅ Step 3 Architecture Decision dokumentiert
- ✅ Step 3 Implementation Guides (5 Tage) erstellt
- ✅ Alle Navigation Links korrekt gesetzt
- 🔄 TODO-34: Frontend Implementation kann beginnen

**🚀 NÄCHSTER SCHRITT:**

**BACKEND CONTACT ENTITY IMPLEMENTIEREN (Tag 1)**
- Contact JPA Entity erstellen
- Repository mit Custom Queries
- Migration V7 schreiben
- Service Layer implementieren

**Konkrete Befehle:**
```bash
# Backend starten
cd backend
./mvnw quarkus:dev

# Contact Entity erstellen
mkdir -p src/main/java/de/freshplan/domain/customer/entity
touch src/main/java/de/freshplan/domain/customer/entity/Contact.java

# Migration erstellen
touch src/main/resources/db/migration/V7__create_contacts_table.sql
```

**Alternative: Frontend Foundation (Tag 2)**
- Contact Types definieren
- Store erweitern
- API Service implementieren

**UNTERBROCHEN BEI:**
- Step 3 Planung abgeschlossen
- Implementation kann beginnen
- Backend oder Frontend als nächstes

---

## 📊 SPRINT 2 STATUS:
- Step 1 (Basis & Filialstruktur): ✅
- Step 2 (Herausforderungen & Potenzial): ✅ mit segmentierter Kalkulation
- Step 3 (Ansprechpartner): 📋 Planung fertig, Implementation TODO-34
- Step 4 (Angebot & Services): ✅ Frontend fertig
- Backend Integration: 🔄 Ausstehend (5 TODOs)
- Contact Vision: ✅ Dokumentiert mit pragmatischem Ansatz

## 🔗 WICHTIGE DOKUMENTE:
- [Step 3 Architecture Decision](/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/STEP3_ARCHITECTURE_DECISION.md)
- [Step 3 Implementation Guide](/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/README.md)
- [Backend Contact Entity Guide](/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/BACKEND_CONTACT.md)