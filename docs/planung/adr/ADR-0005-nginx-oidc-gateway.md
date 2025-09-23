# ADR-0005: Nginx+OIDC Gateway statt Kong/Envoy

**Status:** Akzeptiert
**Datum:** 10.09.2025
**Autor:** Development Team

## Kontext

Für das API Gateway des FreshPlan Sales Tools stehen verschiedene Optionen zur Verfügung:
- Nginx + OIDC (minimal)
- Kong (full-featured)
- Envoy + Istio (service mesh)
- AWS API Gateway (managed)

Für ein internes Tool mit 5-50 Nutzern muss die richtige Balance zwischen Features und Komplexität gefunden werden.

## Entscheidung

Wir implementieren **Nginx + OIDC Gateway** mit:
- Nginx als Reverse Proxy
- OAuth2/OIDC Authentication
- Basic Rate Limiting
- SSL Termination
- **Kong/Envoy als optionale spätere Erweiterung**

## Begründung

### Pro Nginx + OIDC:
- **YAGNI-Prinzip:** Minimale Features für internen Use-Case ausreichend
- **Budget-effizient:** Keine Lizenz- oder Hosting-Kosten für Advanced Features
- **Schnelle Implementation:** Bewährte Nginx-Konfiguration
- **Team-Expertise:** Nginx-Know-how bereits vorhanden
- **Einfaches Monitoring:** Standard Nginx-Logs und Metriken

### Contra Kong/Envoy:
- **Over-Engineering:** Advanced Features (Plugin-System, Service-Discovery) nicht benötigt
- **Komplexität:** Zusätzliche Lernkurve für Team
- **Infrastruktur:** Mehr Moving Parts, mehr Monitoring
- **Kosten:** Kong Enterprise oder Envoy-Management-Overhead

## Konsequenzen

### Positive:
- Minimale Infrastruktur-Komplexität
- Sofort produktiv (keine lange Einarbeitung)
- Bewährte Stabilität von Nginx
- Einfache SSL-Konfiguration + Let's Encrypt

### Negative:
- Weniger Advanced Gateway-Features verfügbar
- Bei komplexeren Anforderungen später Migration nötig
- Limitierte Plugin-Ecosystem vs Kong

### Mitigationen:
- Architektur Gateway-agnostic gestalten
- Bei steigenden Anforderungen (>50 Nutzer) Kong/Envoy evaluieren
- Monitoring für Gateway-Performance implementieren

## Implementation Details

### Nginx-Konfiguration:
```nginx
server {
    listen 443 ssl;
    server_name freshplan.freshfoodz.de;

    # OIDC Authentication
    auth_request /auth;

    # Rate Limiting
    limit_req zone=api burst=20 nodelay;

    # Backend Routing
    location /api/ {
        proxy_pass http://backend:8080/;
        proxy_set_header Authorization $http_authorization;
    }

    location /auth {
        internal;
        proxy_pass http://oidc-provider:9090/auth;
    }
}
```

### Features (Phase 1):
- **Authentication:** OAuth2/OIDC via Keycloak
- **Rate Limiting:** Basic per-IP limits
- **SSL:** Let's Encrypt certificates
- **Logging:** Standard access + error logs

### Migration-Path (bei Bedarf):
1. **Phase 2 (>50 Nutzer):** Kong für Advanced Features
2. **Phase 3 (Microservices):** Envoy + Service Mesh
3. **Phase 4 (Multi-Tenant):** AWS API Gateway

## Alternativen

1. **Kong:** Abgelehnt wegen Over-Engineering für aktuellen Scope
2. **Envoy + Istio:** Abgelehnt wegen Komplexität ohne Microservices
3. **AWS API Gateway:** Abgelehnt wegen Vendor-Lock-in + Kosten
4. **Traefik:** Erwogen, aber Nginx-Expertise bereits vorhanden

## Compliance

- **Authentication:** OIDC-Standards erfüllt
- **SSL:** TLS 1.3, A+ Rating angestrebt
- **Rate Limiting:** DoS-Protection aktiv
- **Monitoring:** Gateway-Performance-Dashboards
- **Backup-Plan:** Migration-Path zu Kong dokumentiert