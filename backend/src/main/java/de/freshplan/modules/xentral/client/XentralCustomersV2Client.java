package de.freshplan.modules.xentral.client;

import de.freshplan.modules.xentral.dto.v2.XentralV2CustomerResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Quarkus REST Client for Xentral v2 Customers API.
 *
 * <p>Endpoint: https://644b6ff97320d.xentral.biz/api/v2/customers
 *
 * <p>Authentication: Bearer Token (PAT) via Authorization header
 *
 * <p>Configuration in application.properties:
 *
 * <pre>
 * quarkus.rest-client.xentral-customers-v2.url=${xentral.api.base-url}
 * quarkus.rest-client.xentral-customers-v2.scope=jakarta.enterprise.context.ApplicationScoped
 * </pre>
 *
 * <p>Sprint: 2.1.7.2 - D2b Quarkus REST Clients
 *
 * @see XentralV2CustomerResponse
 */
@RegisterRestClient(configKey = "xentral-customers-v2")
@Path("/api/v2/customers")
@Produces(MediaType.APPLICATION_JSON)
public interface XentralCustomersV2Client {

  /**
   * Get all customers (paginated).
   *
   * @param authorization Bearer token (format: "Bearer {token}")
   * @param page page number (default: 1)
   * @param pageSize number of items per page (default: 50, max: 100)
   * @return paginated customer response
   */
  @GET
  XentralV2CustomerResponse getCustomers(
      @HeaderParam("Authorization") String authorization,
      @QueryParam("page") Integer page,
      @QueryParam("page_size") Integer pageSize);

  /**
   * Get a single customer by ID.
   *
   * @param authorization Bearer token (format: "Bearer {token}")
   * @param customerId customer ID from Xentral
   * @return customer response (single item in data array)
   */
  @GET
  @Path("/{id}")
  XentralV2CustomerResponse getCustomerById(
      @HeaderParam("Authorization") String authorization, @PathParam("id") String customerId);

  /**
   * Get customers filtered by sales representative.
   *
   * <p>Note: Xentral API may not support direct sales_rep filtering. If not available, fetch all
   * customers and filter in XentralV1V2ApiAdapter.
   *
   * @param authorization Bearer token (format: "Bearer {token}")
   * @param salesRepId sales rep ID from Xentral
   * @param page page number
   * @param pageSize items per page
   * @return filtered customer response
   */
  @GET
  XentralV2CustomerResponse getCustomersBySalesRep(
      @HeaderParam("Authorization") String authorization,
      @QueryParam("sales_rep_id") String salesRepId,
      @QueryParam("page") Integer page,
      @QueryParam("page_size") Integer pageSize);
}
