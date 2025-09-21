package de.freshplan.performance.http;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * ETag/If-None-Match support.
 * Usage in resources:
 *   String etag = EtagSupport.sha256Hex(payloadJsonOrKey);
 *   return EtagSupport.conditional(etag, ifNoneMatch, () -> Response.ok(payload).tag(new EntityTag(etag)).build());
 */
@ApplicationScoped
public class EtagSupport {

  public static String sha256Hex(String input) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hash);
    } catch (Exception e) { throw new RuntimeException(e); }
  }

  public static Response conditional(String etag, String ifNoneMatch, java.util.function.Supplier<Response> supplier) {
    if (ifNoneMatch != null && !ifNoneMatch.isBlank() && ifNoneMatch.equals(etag)) {
      return Response.notModified().tag(new EntityTag(etag)).build();
    }
    Response r = supplier.get();
    return Response.fromResponse(r).tag(new EntityTag(etag)).build();
  }
}
