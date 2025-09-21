package de.freshplan.common;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.*;

@Provider
public class ProblemExceptionMapper implements ExceptionMapper<Throwable> {
  @Override public Response toResponse(Throwable ex) {
    int status = 500;
    if (ex instanceof BadRequestException) status = 400;
    else if (ex instanceof ForbiddenException) status = 403;
    else if (ex instanceof NotFoundException) status = 404;
    else if (ex instanceof ConstraintViolationException) status = 400;
    Map<String,Object> body = new LinkedHashMap<>();
    body.put("type","about:blank"); body.put("title", ex.getClass().getSimpleName());
    body.put("status", status); body.put("detail", ex.getMessage());
    if (ex instanceof ConstraintViolationException cve) {
      var errs = new ArrayList<Map<String,String>>();
      for (ConstraintViolation<?> v : cve.getConstraintViolations()) {
        errs.add(Map.of("field", v.getPropertyPath().toString(), "message", v.getMessage()));
      }
      body.put("errors", errs);
    }
    return Response.status(status).type("application/problem+json").entity(body).build();
  }
}
