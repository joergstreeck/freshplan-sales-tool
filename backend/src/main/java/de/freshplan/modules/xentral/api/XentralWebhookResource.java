package de.freshplan.modules.xentral.api;

import de.freshplan.modules.xentral.dto.XentralOrderWebhookRequest;
import de.freshplan.modules.xentral.service.XentralOrderEventHandler;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Xentral Webhook REST Resource
 *
 * <p>Sprint 2.1.7.2: Xentral Webhook Integration (D7)
 *
 * <p>Provides REST API endpoint for receiving Xentral ERP webhook notifications.
 *
 * <p>Endpoints:
 *
 * <ul>
 *   <li>POST /api/webhooks/xentral/order-delivered - Handle "Order Delivered" event
 * </ul>
 *
 * <p>Security: PUBLIC endpoint (called by Xentral ERP) - No @RolesAllowed annotation
 *
 * <p>Future Enhancement: Add webhook signature verification for security
 *
 * @author FreshPlan Team
 * @since 2.0.0
 */
@Path("/api/webhooks/xentral")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class XentralWebhookResource {

  private static final Logger logger = LoggerFactory.getLogger(XentralWebhookResource.class);

  @Inject XentralOrderEventHandler orderEventHandler;

  /**
   * Handle "Order Delivered" webhook from Xentral
   *
   * <p>Called by Xentral ERP when an order has been delivered. Automatically activates PROSPECT
   * customers on their first order.
   *
   * <p>Expected JSON payload:
   *
   * <pre>
   * {
   *   "xentralCustomerId": "12345",
   *   "orderNumber": "ORD-2025-001",
   *   "deliveryDate": "2025-01-24"
   * }
   * </pre>
   *
   * @param request Webhook request containing order details
   * @return 200 OK if processed successfully, 400 if validation fails, 500 if processing fails
   */
  @POST
  @Path("/order-delivered")
  public Response handleOrderDelivered(@Valid XentralOrderWebhookRequest request) {
    logger.info(
        "Received Xentral Webhook: order-delivered [customer={}, order={}, date={}]",
        request.xentralCustomerId(),
        request.orderNumber(),
        request.deliveryDate());

    try {
      // Additional DTO validation (beyond Jakarta validation)
      request.validate();

      // Delegate to business logic handler
      orderEventHandler.handleOrderDelivered(
          request.xentralCustomerId(), request.orderNumber(), request.deliveryDate());

      logger.info(
          "Successfully processed Xentral Webhook: order-delivered [order={}]",
          request.orderNumber());

      return Response.ok()
          .entity(
              "{\"success\": true, \"message\": \"Order delivered event processed successfully\"}")
          .build();

    } catch (IllegalArgumentException e) {
      // Validation errors
      logger.warn(
          "Validation error in Xentral Webhook: order-delivered [order={}] - {}",
          request.orderNumber(),
          e.getMessage());

      return Response.status(Response.Status.BAD_REQUEST)
          .entity("{\"success\": false, \"error\": \"" + e.getMessage() + "\"}")
          .build();

    } catch (Exception e) {
      // Unexpected errors
      logger.error(
          "Error processing Xentral Webhook: order-delivered [order={}]", request.orderNumber(), e);

      return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity(
              "{\"success\": false, \"error\": \"Internal server error: " + e.getMessage() + "\"}")
          .build();
    }
  }
}
