package com.example.crm.email

import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import jakarta.transaction.Transactional
import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import java.time.OffsetDateTime
import java.util.*

/**
 * BounceHandler
 * - Nimmt normalisierte Bounces an (HARD/SOFT)
 * - Persistiert in email_bounce (idempotent via Idempotency-Key oder recipient+occurredAt)
 * - Triggert Suppression/Unsubscribe-Policy (optional)
 * - Publiziert Event in event_outbox (email.bounce)
 */
@Path("/email/bounce")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class BounceResource {

    @Inject lateinit var service: BounceService

    @POST
    fun post(@HeaderParam("Idempotency-Key") idemKey: String?, dto: BounceIn): Response {
        val accepted = service.acceptBounce(dto, idemKey)
        return if (accepted) Response.accepted().build() else Response.status(409).build()
    }
}

data class BounceIn(
    val type: BounceType,
    val recipient: String,
    val occurredAt: OffsetDateTime,
    val reason: String? = null,
    val smtpCode: String? = null,
    val provider: String? = null,
    val messageId: String? = null,
    val threadId: UUID? = null
)

enum class BounceType { HARD, SOFT }

@ApplicationScoped
class BounceService {

    @Inject lateinit var repo: BounceRepository
    @Inject lateinit var suppression: SuppressionService
    @Inject lateinit var events: EventPublisher

    @Transactional
    fun acceptBounce(incoming: BounceIn, idemKey: String?): Boolean {
        if (repo.existsByRecipientAndOccurredAt(incoming.recipient, incoming.occurredAt)) {
            return false
        }
        if (idemKey != null && repo.existsByIdempotencyKey(idemKey)) {
            return false
        }
        val id = repo.insert(incoming, idemKey)
        // Optional: Suppression für HARD-Bounce
        if (incoming.type == BounceType.HARD) {
            suppression.suppress(incoming.recipient, reason = incoming.reason ?: "hard-bounce")
        }
        events.publish("email.bounce", mapOf(
            "bounceId" to id.toString(),
            "type" to incoming.type.name,
            "recipient" to incoming.recipient,
            "smtpCode" to incoming.smtpCode,
            "provider" to incoming.provider,
            "messageId" to incoming.messageId,
            "threadId" to incoming.threadId
        ))
        return true
    }
}

@ApplicationScoped
class BounceRepository {

    fun existsByRecipientAndOccurredAt(recipient: String, occurredAt: OffsetDateTime): Boolean {
        // SELECT 1 FROM email_bounce WHERE recipient=:recipient AND occurred_at=:occurredAt
        return false
    }

    fun existsByIdempotencyKey(key: String): Boolean {
        // SELECT 1 FROM email_bounce WHERE idempotency_key=:key
        return false
    }

    fun insert(incoming: BounceIn, idemKey: String?): UUID {
        val id = UUID.randomUUID()
        // INSERT INTO email_bounce(id,type,reason,smtp_code,provider,message_id,thread_id,recipient,occurred_at,idempotency_key)
        // VALUES (:id, :type, :reason, :smtp, :provider, :msgId, :threadId, :recipient, :occurredAt, :idemKey)
        return id
    }
}

@ApplicationScoped
class SuppressionService {
    fun suppress(email: String, reason: String) {
        // INSERT INTO unsubscribe(email, reason) ON CONFLICT(email) DO NOTHING
    }
}
