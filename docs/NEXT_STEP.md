# 🧭 NEXT STEP NAVIGATION

**Zweck:** Immer nur EINEN klaren nächsten Schritt für Claude
**Update:** Bei jeder Unterbrechung oder Fortschritt

---

## 🎯 JETZT GERADE:

**FC-005 SPRINT 2 CODE INTEGRATION ABGESCHLOSSEN ✅**

**Stand 07.08.2025 19:41:**
- ✅ **Sprint 2 Code:** Erfolgreich in fc-005 Branch integriert
- ✅ **Database Schema:** Contact Entity auf customer_contacts Tabelle migriert
- ✅ **Migration Cleanup:** Duplikat-Migrationen entfernt (V100, V104, V106)
- ✅ **Test Results:** 881/888 Tests bestanden (99.2% Pass Rate)
- ⚠️ **Verbleibend:** 7 ContactRepository Tests mit Foreign Key Problemen
- 🚨 **Status:** INTEGRATION ERFOLGREICH, STABILISIERUNG BENÖTIGT

**🚀 NÄCHSTER SCHRITT:**

**Service stabilisieren und vollständige Funktionalität verifizieren**

```bash
cd /Users/joergstreeck/freshplan-sales-tool

# 1. Backend Service neu starten
pkill -f quarkus
cd backend && ./mvnw quarkus:dev -Dquarkus.http.port=8080

# 2. API-Funktionalität verifizieren
curl http://localhost:8080/api/ping
curl http://localhost:8080/q/health

# 3. Frontend testen
cd ../frontend
npm run dev
# Browser: http://localhost:5173

# 4. Die 7 fehlgeschlagenen Tests beheben
cd ../backend
./mvnw test -Dtest=ContactRepositoryTest -q
# Foreign Key Probleme analysieren und fixen

# 5. Vollständige Test-Suite
./mvnw test
```

**UNTERBROCHEN BEI:**
- Backend Service läuft, aber API zeigt HTML Error Page
- TODO: Service neu starten und API-Funktionalität verifizieren

**WICHTIGE DOKUMENTE:**
- Übergabe: `/docs/claude-work/daily-work/2025-08-07/2025-08-07_HANDOVER_19-41.md` ⭐
- Contact Entity: `backend/src/main/java/de/freshplan/domain/customer/entity/Contact.java`
- Migration Status: V121 als nächste verfügbare Migration  
- Sprint 2 Integration: Erfolgreich abgeschlossen

**ABGESCHLOSSENE FEATURES:**
- ✅ Sprint 2 Code Integration (100%)
- ✅ Database Schema Migration (100%)
- ✅ Migration Cleanup (100%)
- ✅ Contact Entity Refactoring (100%)

**OFFENE PRIORITÄTEN:**
1. Service Stabilisierung (30min)
2. Frontend Integration Test (30min)
3. 7 ContactRepository Test Fixes (1-2h)

---

## ⚠️ VOR JEDER IMPLEMENTATION - REALITY CHECK PFLICHT:
```bash
# Backend Service Status prüfen:
curl http://localhost:8080/api/ping
# Sollte: JSON Response statt HTML Error

# Test Status prüfen:
cd backend && ./mvnw test | tail -10
# Sollte: 881/888 Tests bestanden zeigen

# Database Migration Status:
./scripts/get-next-migration.sh
# Sollte: V121 als nächste Migration anzeigen
```

---

## 📊 AKTUELLER STATUS:
```
🟢 Sprint 2 Code Integration: ✅ ERFOLGREICH
🟢 Database Schema Migration: ✅ ABGESCHLOSSEN  
🟢 Migration Cleanup: ✅ DURCHGEFÜHRT
🟢 Test Suite: ✅ 881/888 BESTANDEN (99.2%)
⚠️ Service Stabilität: 🔄 RESTART BENÖTIGT
```

**Status:**
- FC-005 Sprint 2 Integration: ✅ ABGESCHLOSSEN
- Backend Tests: ✅ 99.2% bestanden  
- Contact Entity Migration: ✅ ERFOLGREICH
- Database Schema: ✅ KONSISTENT
- Verbleibende Fixes: ⚠️ 7 Tests ContactRepository