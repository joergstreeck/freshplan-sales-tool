# Audit Script Fix - Pattern Bug behoben

## Problem
Das audit-testdata-v2.sh Script hatte einen Pattern-Bug:
- `\b` word boundary funktioniert nicht mit ripgrep's PCRE mode (`rg -P`)
- Dies führte dazu, dass `new Customer()` nicht korrekt erkannt wurde

## Lösung
Statt komplexer PCRE Patterns mit word boundaries oder negativem Lookahead:
1. Einfache Regex-Suche mit ripgrep
2. Filterung der False Positives mit grep -v
3. Expliziter Ausschluss von CustomerBuilder, CustomerContact, etc.

## Geänderter Code
```bash
# Alt (funktioniert nicht):
rg -P 'new\s+Customer\b\s*\(' 

# Neu (funktioniert):
rg 'new\s+Customer\s*\(' | grep -v 'CustomerBuilder\|CustomerContact\|...'
```

## Test-Ergebnis
Das Script findet jetzt korrekt:
- 5+ Vorkommen von `new Customer()` in Tests
- 376 direkte persist() Aufrufe
- Andere Pattern-Violations

## Version
audit-testdata-v2.sh wurde zu v2.1 aktualisiert