package de.freshplan.api.exception.mapper;

import de.freshplan.domain.user.service.exception.UserNotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

/**
 * JAX-RS exception mapper for UserNotFoundException.
 *
 * <p>Converts UserNotFoundException to a 404 Not Found response with a standardized error format.
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Provider
public class UserNotFoundExceptionMapper implements ExceptionMapper<UserNotFoundException> {

  private static final Logger LOG = Logger.getLogger(UserNotFoundExceptionMapper.class);

  @Context UriInfo uriInfo;

  @Override
  public Response toResponse(UserNotFoundException exception) {
    LOG.debugf("User not found: %s", exception.getMessage());

    ErrorResponse errorResponse =
        ErrorResponse.builder()
            .status(404)
            .error("Not Found")
            .message(exception.getMessage())
            .path(uriInfo.getPath())
            .build();

    return Response.status(Response.Status.NOT_FOUND).entity(errorResponse).build();
  }
}
