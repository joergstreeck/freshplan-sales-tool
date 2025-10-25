package de.freshplan.modules.xentral.client;

import de.freshplan.modules.xentral.dto.v1.XentralV1InvoiceBalance;
import de.freshplan.modules.xentral.dto.v1.XentralV1InvoiceResponse;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * Quarkus REST Client for Xentral v1 Invoices API.
 *
 * <p>Endpoints: - GET /api/v1/invoices (list invoices) - GET /api/v1/invoices/{id}/balance (payment
 * details)
 *
 * <p>Used to calculate financial metrics: - totalRevenue = SUM(balance.paid) - averageDaysToPay =
 * AVG(DAYS.between(invoice.date, balance.paymentDate)) - lastOrderDate = MAX(invoice.date)
 *
 * <p>Configuration in application.properties:
 *
 * <pre>
 * quarkus.rest-client.xentral-invoices-v1.url=${xentral.api.base-url}
 * quarkus.rest-client.xentral-invoices-v1.scope=jakarta.enterprise.context.ApplicationScoped
 * </pre>
 *
 * <p>Sprint: 2.1.7.2 - D2b Quarkus REST Clients
 *
 * @see XentralV1InvoiceResponse
 * @see XentralV1InvoiceBalance
 */
@RegisterRestClient(configKey = "xentral-invoices-v1")
@Path("/api/v1/invoices")
@Produces(MediaType.APPLICATION_JSON)
public interface XentralInvoicesV1Client {

  /**
   * Get all invoices for a specific customer.
   *
   * @param authorization Bearer token (format: "Bearer {token}")
   * @param customerId filter by customer ID
   * @param page page number (default: 1)
   * @param perPage items per page (default: 50)
   * @return paginated invoice response
   */
  @GET
  XentralV1InvoiceResponse getInvoicesByCustomer(
      @HeaderParam("Authorization") String authorization,
      @QueryParam("customer_id") String customerId,
      @QueryParam("page") Integer page,
      @QueryParam("per_page") Integer perPage);

  /**
   * Get balance details for a specific invoice.
   *
   * <p>Contains payment information: total, paid, open, paymentDate
   *
   * @param authorization Bearer token (format: "Bearer {token}")
   * @param invoiceId invoice ID from Xentral
   * @return balance details
   */
  @GET
  @Path("/{id}/balance")
  XentralV1InvoiceBalance getInvoiceBalance(
      @HeaderParam("Authorization") String authorization, @PathParam("id") String invoiceId);

  /**
   * Get a single invoice by ID.
   *
   * @param authorization Bearer token (format: "Bearer {token}")
   * @param invoiceId invoice ID from Xentral
   * @return invoice response (single item)
   */
  @GET
  @Path("/{id}")
  XentralV1InvoiceResponse getInvoiceById(
      @HeaderParam("Authorization") String authorization, @PathParam("id") String invoiceId);
}
