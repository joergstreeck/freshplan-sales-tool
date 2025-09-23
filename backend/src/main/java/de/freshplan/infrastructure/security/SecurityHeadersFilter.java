package de.freshplan.infrastructure.security;

import jakarta.annotation.Priority;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;

/**
 * Security Headers Filter for Sprint 1.3 Security Gates.
 * Adds security-relevant HTTP headers to all responses.
 * Part of FP-231: Security Gates Enforcement.
 */
@Provider
@Priority(2000)
public class SecurityHeadersFilter implements ContainerResponseFilter {

    @ConfigProperty(name = "app.security.hsts.enabled", defaultValue = "false")
    boolean hstsEnabled;

    @ConfigProperty(name = "app.security.hsts.max-age", defaultValue = "15552000")
    long hstsMaxAge;

    @ConfigProperty(name = "app.security.frame-options", defaultValue = "DENY")
    String frameOptions;

    @ConfigProperty(name = "quarkus.profile")
    String activeProfile;

    @Override
    public void filter(ContainerRequestContext requestContext,
                      ContainerResponseContext responseContext) throws IOException {

        var headers = responseContext.getHeaders();

        // Prevent MIME type sniffing
        headers.putSingle("X-Content-Type-Options", "nosniff");

        // Control referrer information
        headers.putSingle("Referrer-Policy", "strict-origin-when-cross-origin");

        // Restrict browser features
        headers.putSingle("Permissions-Policy",
            "geolocation=(), microphone=(), camera=(), payment=()");

        // Clickjacking protection
        headers.putSingle("X-Frame-Options", frameOptions);

        // Content Security Policy - conservative default
        // Can be refined later via Settings Registry
        headers.putSingle("Content-Security-Policy", """
            default-src 'self'; \
            img-src 'self' data: https:; \
            script-src 'self'; \
            style-src 'self' 'unsafe-inline'; \
            font-src 'self' data:; \
            connect-src 'self'; \
            frame-ancestors 'none'""".replaceAll("\\s+", " ").trim());

        // HSTS only in production and over HTTPS
        if (hstsEnabled && isProdProfile() && isHttpsRequest(requestContext)) {
            headers.putSingle("Strict-Transport-Security",
                "max-age=" + hstsMaxAge + "; includeSubDomains");
        }
    }

    private boolean isProdProfile() {
        return "prod".equals(activeProfile);
    }

    private boolean isHttpsRequest(ContainerRequestContext requestContext) {
        String scheme = requestContext.getUriInfo().getRequestUri().getScheme();
        String xForwardedProto = requestContext.getHeaderString("X-Forwarded-Proto");
        return "https".equalsIgnoreCase(scheme) ||
               "https".equalsIgnoreCase(xForwardedProto);
    }
}