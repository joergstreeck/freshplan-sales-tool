package de.freshplan.modules.xentral.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Quarkus REST Client for Xentral v1 Employees API.
 *
 * <p>Endpoint: /api/v1/employees
 *
 * <p>Used for Sales Rep Mapping (D6): - Match FreshPlan users to Xentral employees via email -
 * Auto-sync job runs daily at 2:00
 *
 * <p>Configuration in application.properties:
 *
 * <pre>
 * quarkus.rest-client.xentral-employees-v1.url=${xentral.api.base-url}
 * quarkus.rest-client.xentral-employees-v1.scope=jakarta.enterprise.context.ApplicationScoped
 * </pre>
 *
 * <p>Sprint: 2.1.7.2 - D2b Quarkus REST Clients
 */
@RegisterRestClient(configKey = "xentral-employees-v1")
@Path("/api/v1/employees")
@Produces(MediaType.APPLICATION_JSON)
public interface XentralEmployeesV1Client {

  /**
   * Get all employees (paginated).
   *
   * @param authorization Bearer token (format: "Bearer {token}")
   * @param page page number (default: 1)
   * @param perPage items per page (default: 50)
   * @return employee response
   */
  @GET
  XentralV1EmployeeResponse getEmployees(
      @HeaderParam("Authorization") String authorization,
      @QueryParam("page") Integer page,
      @QueryParam("per_page") Integer perPage);

  /**
   * Get a single employee by ID.
   *
   * @param authorization Bearer token (format: "Bearer {token}")
   * @param employeeId employee ID from Xentral
   * @return employee response (single item)
   */
  @GET
  @Path("/{id}")
  XentralV1EmployeeResponse getEmployeeById(
      @HeaderParam("Authorization") String authorization, @PathParam("id") String employeeId);

  /** Response wrapper for v1 employees endpoint. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  record XentralV1EmployeeResponse(List<XentralV1Employee> data, Meta meta) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Meta(Integer total, Integer page, Integer perPage) {}
  }

  /** Employee entity from v1 API. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  record XentralV1Employee(
      String id,
      String firstName,
      String lastName,
      String email,
      String role,
      String department) {
    public String getFullName() {
      return firstName + " " + lastName;
    }
  }
}
