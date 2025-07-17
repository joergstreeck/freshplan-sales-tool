# 🏗️ STRUKTURBEREINIGUNG PHASE 1: HYBRID-MIGRATION

**Erstellt:** 17.07.2025 19:45  
**Zweck:** Phase 1 Detailplan - Claude-optimiert unter 200 Zeilen  
**Prerequisite:** [KOMPAKT Plan](./2025-07-17_STRUKTURBEREINIGUNG_KOMPAKT.md) gelesen  

---

## 🎯 PHASE 1 ZIEL

**Alle Features auf bewährte FC-009/FC-010 Hybrid-Struktur migrieren**

### **Bewährte Struktur:**
```
/docs/features/ACTIVE/XX_feature/
├── FC-XXX_KOMPAKT.md         # <150 Zeilen - 15-Min Produktivität
├── IMPLEMENTATION_GUIDE.md    # <300 Zeilen - Copy-paste Code
├── DECISION_LOG.md           # <200 Zeilen - Offene Entscheidungen
└── MIGRATION_PLAN.md         # <200 Zeilen - Bei Legacy-Integration
```

---

## 🔄 1.1: UI Foundation Module erstellen

### **Schritt 1: Struktur schaffen**
```bash
mkdir -p docs/features/ACTIVE/05_ui_foundation
```

### **Schritt 2: KOMPAKT-Dokumente erstellen**
**4 Module à <150 Zeilen:**
- `M1_NAVIGATION_KOMPAKT.md` ✅ (130 Zeilen)
- `M2_QUICK_CREATE_KOMPAKT.md` ✅ (132 Zeilen)
- `M3_SALES_COCKPIT_KOMPAKT.md` ✅ (146 Zeilen)
- `M7_SETTINGS_KOMPAKT.md` ✅ (128 Zeilen)

### **Schritt 3: Struktur-Dokumente**
- `IMPLEMENTATION_GUIDE.md` ✅ (103 Zeilen)
- `DECISION_LOG.md` ⚠️ (250 Zeilen - ZU LANG!)
- `ENHANCEMENT_ROADMAP.md` ✅ (118 Zeilen)

---

## 🗂️ 1.2: Legacy FC-002 archivieren

### **Schritt 1: Archiv erstellen**
```bash
mkdir -p docs/features/LEGACY/FC-002
mv docs/features/FC-002-*.md docs/features/LEGACY/FC-002/
```

### **Schritt 2: Verweis erstellen**
```bash
echo "📁 Archiviert → siehe /docs/features/LEGACY/FC-002/" > docs/features/FC-002_ARCHIVIERT.md
```

---

## 🌟 1.3: FUTURE VISION strukturieren

### **Schritt 1: Vision-Ordner**
```bash
mkdir -p docs/features/VISION
```

### **Schritt 2: Aufteilen nach Zeithorizont**
- **2025 Q3-Q4:** Kurze Roadmap <100 Zeilen
- **2026:** Mittelfristige Vision <100 Zeilen  
- **2027+:** Langzeit-Vision <100 Zeilen

---

## ✅ PHASE 1 SUCCESS CRITERIA

**Nach Phase 1 kann Claude:**
1. ✅ Alle UI Foundation Module in <150 Zeilen finden
2. ✅ KOMPAKT-Dokumente in 15 Min verstehen
3. ✅ Implementation Guide für Copy-Paste nutzen
4. ✅ Decision Log ohne Kontext-Overflow lesen

**KRITISCH:** Alle Dokumente unter Claude-Kontext-Limit!

---

## 🔗 NÄCHSTE SCHRITTE

**Nach Phase 1:**
- [Phase 2 Plan](./2025-07-17_STRUKTURBEREINIGUNG_PHASE2_PLAN.md)
- [Phase 3 Plan](./2025-07-17_STRUKTURBEREINIGUNG_PHASE3_PLAN.md)

**Bei Problemen:**
- [Kompakt-Übersicht](./2025-07-17_STRUKTURBEREINIGUNG_KOMPAKT.md)