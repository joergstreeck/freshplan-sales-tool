package de.freshplan.crm.email.workers

import de.freshplan.crm.email.models.*
import de.freshplan.crm.email.services.SuppressionService
import de.freshplan.crm.events.EventOutboxService
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.transaction.Transactional
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response

/**
 * Bounce-Handler für Deliverability-Monitoring
 *
 * Features:
 * - Idempotency-Key-basierte Duplicate-Prevention
 * - HARD/SOFT-Bounce-Classification
 * - Automatische Suppression-List-Management
 * - Event-Publishing für Monitoring
 * - Thread-Integration für Context
 */
@ApplicationScoped
@Path("/email/bounce")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
class BounceHandler @Inject constructor(
    private val bounceRepository: EmailBounceRepository,
    private val suppressionService: SuppressionService,
    private val eventOutboxService: EventOutboxService,
    private val config: BounceConfig
) {

    companion object {
        private val logger = LoggerFactory.getLogger(BounceHandler::class.java)

        // SMTP-Codes für Bounce-Classification
        private val HARD_BOUNCE_CODES = setOf("550", "551", "552", "553", "554")
        private val SOFT_BOUNCE_CODES = setOf("421", "450", "451", "452", "471", "472")
    }

    @POST
    @Transactional
    fun handleBounce(
        @HeaderParam("X-Correlation-Id") correlationId: String?,
        @HeaderParam("Idempotency-Key") idempotencyKey: String?,
        bounceRequest: BounceRequest
    ): Response {

        logger.info(
            "Processing bounce: {} -> {} ({})",
            bounceRequest.recipient,
            bounceRequest.type,
            bounceRequest.reason
        )

        try {
            // Idempotency-Check: Verhindere Duplicate-Processing
            if (idempotencyKey != null) {
                val existingBounce = bounceRepository.findByIdempotencyKey(idempotencyKey)
                if (existingBounce != null) {
                    logger.debug("Bounce already processed: idempotencyKey=$idempotencyKey")
                    return Response.status(409)
                        .entity(mapOf(
                            "error" to "Bounce already processed",
                            "existingBounceId" to existingBounce.id
                        ))
                        .build()
                }
            }

            // Bounce-Validierung
            val validatedBounce = validateAndNormalizeBounce(bounceRequest)

            // Bounce persistieren
            val bounceRecord = EmailBounce(
                id = UUID.randomUUID(),
                type = validatedBounce.type,
                reason = validatedBounce.reason,
                smtpCode = validatedBounce.smtpCode,
                provider = validatedBounce.provider ?: "unknown",
                messageId = validatedBounce.messageId,
                threadId = validatedBounce.threadId,
                recipient = validatedBounce.recipient,
                occurredAt = validatedBounce.occurredAt,
                receivedAt = LocalDateTime.now(),
                idempotencyKey = idempotencyKey?.let { UUID.fromString(it) }
            )

            bounceRepository.save(bounceRecord)

            // Suppression-Logic für Hard-Bounces
            val suppressionApplied = handleSuppressionLogic(bounceRecord)

            // Event für Monitoring/Analytics
            publishBounceEvent(bounceRecord, suppressionApplied)

            // Response
            val response = BounceProcessedResponse(
                id = bounceRecord.id,
                type = bounceRecord.type,
                recipient = bounceRecord.recipient,
                processed = true,
                suppressionApplied = suppressionApplied,
                createdAt = bounceRecord.receivedAt
            )

            logger.info("Bounce processed successfully: ${bounceRecord.id}")
            return Response.status(202).entity(response).build()

        } catch (exception: IllegalArgumentException) {
            logger.warn("Invalid bounce data: {}", exception.message)
            return Response.status(400)
                .entity(mapOf("error" to exception.message))
                .build()
        } catch (exception: Exception) {
            logger.error("Failed to process bounce", exception)
            return Response.status(500)
                .entity(mapOf("error" to "Internal server error"))
                .build()
        }
    }

    /**
     * Validierung und Normalisierung der Bounce-Daten
     */
    private fun validateAndNormalizeBounce(request: BounceRequest): BounceRequest {
        // Email-Format validieren
        if (!isValidEmail(request.recipient)) {
            throw IllegalArgumentException("Invalid recipient email format")
        }

        // Bounce-Type normalisieren
        val normalizedType = when {
            request.type == BounceType.HARD -> BounceType.HARD
            request.type == BounceType.SOFT -> BounceType.SOFT
            // Auto-Classification basierend auf SMTP-Code
            request.smtpCode in HARD_BOUNCE_CODES -> BounceType.HARD
            request.smtpCode in SOFT_BOUNCE_CODES -> BounceType.SOFT
            else -> request.type // Behalte Original-Classification
        }

        // Reason normalisieren (kürzen falls zu lang)
        val normalizedReason = if (request.reason.length > 500) {
            request.reason.take(497) + "..."
        } else {
            request.reason
        }

        return request.copy(
            type = normalizedType,
            reason = normalizedReason,
            recipient = request.recipient.lowercase() // Email normalisieren
        )
    }

    /**
     * Suppression-Logic für Hard-Bounces
     */
    @Transactional
    fun handleSuppressionLogic(bounce: EmailBounce): Boolean {
        if (bounce.type != BounceType.HARD) {
            return false // Nur Hard-Bounces suppression
        }

        if (!config.enableAutomaticSuppression) {
            logger.debug("Automatic suppression disabled, skipping for ${bounce.recipient}")
            return false
        }

        try {
            // Prüfe ob bereits auf Suppression-Liste
            if (suppressionService.isSuppressed(bounce.recipient)) {
                logger.debug("Recipient already suppressed: ${bounce.recipient}")
                return true
            }

            // Auf Suppression-Liste setzen
            suppressionService.addToSuppressionList(
                email = bounce.recipient,
                reason = "Hard bounce: ${bounce.reason}",
                bounceId = bounce.id
            )

            logger.info("Added to suppression list: ${bounce.recipient}")
            return true

        } catch (exception: Exception) {
            logger.error("Failed to add to suppression list: ${bounce.recipient}", exception)
            return false
        }
    }

    /**
     * Event für Monitoring/Analytics publishen
     */
    private fun publishBounceEvent(bounce: EmailBounce, suppressionApplied: Boolean) {
        try {
            val eventData = mapOf(
                "bounceId" to bounce.id,
                "recipient" to bounce.recipient,
                "type" to bounce.type.name,
                "reason" to bounce.reason,
                "smtpCode" to bounce.smtpCode,
                "provider" to bounce.provider,
                "messageId" to bounce.messageId,
                "threadId" to bounce.threadId,
                "suppressionApplied" to suppressionApplied,
                "occurredAt" to bounce.occurredAt
            )

            eventOutboxService.publish(
                topic = "email.bounce",
                payload = eventData,
                idempotencyKey = "bounce-${bounce.id}-${bounce.receivedAt}"
            )

        } catch (exception: Exception) {
            logger.error("Failed to publish bounce event", exception)
            // Event-Publishing-Fehler sollen Bounce-Processing nicht blockieren
        }
    }

    /**
     * Simple Email-Validierung
     */
    private fun isValidEmail(email: String): Boolean {
        return email.matches(Regex("^[A-Za-z0-9+_.-]+@([A-Za-z0-9.-]+\\.[A-Za-z]{2,})$"))
    }

    @GET
    @Path("/stats")
    fun getBounceStats(
        @HeaderParam("X-Correlation-Id") correlationId: String?,
        @QueryParam("provider") provider: String?
    ): Response {
        try {
            val stats = bounceRepository.calculateBounceStats(
                provider = provider,
                days = 7
            )

            return Response.ok(stats).build()

        } catch (exception: Exception) {
            logger.error("Failed to retrieve bounce stats", exception)
            return Response.status(500)
                .entity(mapOf("error" to "Failed to retrieve stats"))
                .build()
        }
    }
}

// Request/Response Data Classes
data class BounceRequest(
    val type: BounceType,
    val reason: String,
    val smtpCode: String? = null,
    val provider: String? = null,
    val messageId: String,
    val threadId: UUID? = null,
    val recipient: String,
    val occurredAt: LocalDateTime
)

data class BounceProcessedResponse(
    val id: UUID,
    val type: BounceType,
    val recipient: String,
    val processed: Boolean,
    val suppressionApplied: Boolean,
    val createdAt: LocalDateTime
)

// Entity
data class EmailBounce(
    val id: UUID,
    val type: BounceType,
    val reason: String,
    val smtpCode: String?,
    val provider: String,
    val messageId: String,
    val threadId: UUID?,
    val recipient: String,
    val occurredAt: LocalDateTime,
    val receivedAt: LocalDateTime,
    val idempotencyKey: UUID?
)

enum class BounceType {
    HARD, SOFT
}

// Configuration
data class BounceConfig(
    val enableAutomaticSuppression: Boolean = true,
    val hardBounceThreshold: Int = 1, // Anzahl Hard-Bounces bis Suppression
    val softBounceThreshold: Int = 5  // Anzahl Soft-Bounces bis Suppression
)