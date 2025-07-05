# CRM Complete Master Plan

## Vision
Wir bauen ein integriertes "Super-CRM", das den gesamten Vertriebsprozess abbildet und nahtlos mit unseren Systemen (Xentral, E-Mail) kommuniziert.

## Architektur-Prinzipien

1. **Customer 360°**: Der Kunde steht im Mittelpunkt.

2. **Sales Excellence**: Wir bilden den gesamten Vertriebsprozess ab (Opportunities, Aktivitäten).

3. **Automated Operations**: Wir automatisieren Prozesse wie die Dokumenten-Erstellung.

4. **Integrated Ecosystem**: Das CRM ist keine Insel, sondern via API und "BCC-to-CRM" mit Xentral und E-Mail verbunden.

5. **Event-Driven**: Wichtige Ereignisse (z.B. "Partner aktiviert") lösen Events aus, auf die andere Module reagieren können.

## Kern-Geschäftslogik

1. **Partner-Status**: Ein Kunde kann "Partner" sein, basierend auf einer PartnerContract-Entität.

2. **Rabattplan**: Der Partner-Status schaltet spezifische Rabatte frei.

3. **Dokumenten-Generierung**: Die Partnerschaftsvereinbarung wird automatisch mit Kundendaten befüllt und als PDF generiert.

## Finale Roadmap

### Phase 1: Das operative Vertriebs-Fundament (Unser aktueller Fokus)

- **Kundenmanagement (100% fertig & qualitätsgesichert)**: Inklusive Partner-Logik und Dokumenten-Generierung.
- **Opportunity & Deal Management**: Verwaltung von Verkaufschancen.
- **Aktivitäten & Aufgaben Management**: Protokollierung der Vertriebsaktivitäten.

### Phase 2: Integration & Kontext

- Anbindung an Xentral (Produkte, Aufträge).
- Umsetzung des Kommunikations-Hubs ("BCC-to-CRM").
- Implementierung des Contract-Moduls.

### Phase 3: Intelligenz & Automatisierung

- Advanced Analytics, Reporting und Workflows.