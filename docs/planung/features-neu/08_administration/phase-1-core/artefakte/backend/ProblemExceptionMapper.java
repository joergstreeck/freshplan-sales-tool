package de.freshplan.admin.common;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import jakarta.ws.rs.WebApplicationException;

@Provider
public class ProblemExceptionMapper implements ExceptionMapper<Throwable> {
  @Override
  public Response toResponse(Throwable ex) {
    int status = 500;
    if (ex instanceof WebApplicationException) {
      status = ((WebApplicationException)ex).getResponse().getStatus();
    }
    var body = java.util.Map.of(
      "type", "about:blank",
      "title", ex.getClass().getSimpleName(),
      "status", status,
      "detail", ex.getMessage()==null? "": ex.getMessage()
    );
    return Response.status(status).type(MediaType.APPLICATION_PROBLEM_JSON).entity(body).build();
  }
}
