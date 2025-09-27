package com.example.crm.email

import io.smallrye.mutiny.Uni
import io.quarkus.scheduler.Scheduled
import jakarta.enterprise.context.ApplicationScoped
import jakarta.inject.Inject
import org.jboss.logging.Logger
import java.time.Duration
import java.time.OffsetDateTime
import java.util.*

/**
 * OutboxDispatcher
 * - Holt Pending/Failed Mails aus email_outbox (next_attempt_at <= now)
 * - Versendet via EmailSender (SMTP, Truststore-basiert)
 * - Aktualisiert Status/Attempts, berechnet Backoff
 * - Publiziert Events in event_outbox (email.sent|email.failed)
 * - Respektiert maxInFlight-Konfiguration
 */
@ApplicationScoped
class OutboxDispatcher {

    @Inject lateinit var repo: EmailOutboxRepository
    @Inject lateinit var sender: EmailSender
    @Inject lateinit var events: EventPublisher
    @Inject lateinit var cfg: EmailConfig

    private val log: Logger = Logger.getLogger(OutboxDispatcher::class.java)

    @Scheduled(every = "10s", concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    fun tick() {
        val now = OffsetDateTime.now()
        val batch = repo.fetchDueBatch(cfg.outbox.maxInFlight, now)
        if (batch.isEmpty()) return

        batch.forEach { item ->
            try {
                repo.markSending(item.id)
                val messageId = sender.send(item) // wirft Exception bei Fehlern
                repo.markSent(item.id, messageId)
                events.publish(
                    topic = "email.sent",
                    payload = mapOf(
                        "outboxId" to item.id.toString(),
                        "messageId" to messageId,
                        "recipient" to item.recipient,
                        "threadId" to item.threadId
                    )
                )
            } catch (ex: Exception) {
                val attempts = item.attempts + 1
                val (status, nextAttemptAt) = backoff(attempts, cfg.outbox.deadLetterAfterAttempts)
                repo.markFailed(item.id, attempts, status, nextAttemptAt, ex.message)
                events.publish(
                    topic = "email.failed",
                    payload = mapOf(
                        "outboxId" to item.id.toString(),
                        "recipient" to item.recipient,
                        "error" to (ex.message ?: "unknown")
                    )
                )
                log.warnf(ex, "Outbox send failed id=%s attempts=%d", item.id, attempts)
            }
        }
    }

    private fun backoff(attempts: Int, deadAfter: Int): Pair<String, OffsetDateTime?> {
        if (attempts >= deadAfter) return "DEAD" to null
        val base = cfg.outbox.retryBackoff.baseMs
        val max = cfg.outbox.retryBackoff.maxMs
        val multiplier = cfg.outbox.retryBackoff.multiplier
        val jitterPct = cfg.outbox.retryBackoff.jitterPct

        val exp = (base * Math.pow(multiplier, (attempts - 1).toDouble())).toLong().coerceAtMost(max)
        val jitter = (exp * (jitterPct / 100.0)).toLong()
        val delayMs = exp + (-(jitter)..jitter).random()
        return "FAILED" to OffsetDateTime.now().plusSeconds(delayMs / 1000)
    }

    private fun IntRange.random(): Long {
        val rnd = Random()
        val bound = (this.last - this.first + 1)
        return (this.first + rnd.nextInt(bound)).toLong()
    }
}

// --- EmailSender (SMTP, Truststore) ----------------------------------------

@ApplicationScoped
class EmailSender @Inject constructor(private val cfg: EmailConfig, private val trust: TruststoreProvider) {

    fun send(item: OutboxItem): String {
        // Pseudocode: Erzeuge JavaMail Session mit Truststore aus TruststoreProvider
        // Setze SMTP-Properties (host, port, SSL, timeouts), authentifiziere, sende MIME-Message.
        // Rückgabe: Message-ID aus Transport
        // WICHTIG: Kein Wildcard-Trust; SSLContext aus TruststoreProvider verwenden.
        return UUID.randomUUID().toString()
    }
}

// --- Config (Auszug) --------------------------------------------------------

data class EmailConfig(
    val outbox: OutboxCfg,
) {
    data class OutboxCfg(
        val maxInFlight: Int = 20,
        val deadLetterAfterAttempts: Int = 8,
        val retryBackoff: BackoffCfg = BackoffCfg()
    )
    data class BackoffCfg(
        val baseMs: Long = 2000,
        val maxMs: Long = 600000,
        val multiplier: Double = 2.0,
        val jitterPct: Int = 30
    )
}

// --- Repositories & Models (vereinfachte Interfaces) ------------------------

data class OutboxItem(
    val id: UUID,
    val threadId: UUID?,
    val recipient: String,
    val subject: String,
    val bodyMime: String,
    val attempts: Int
)

@ApplicationScoped
class EmailOutboxRepository {

    fun fetchDueBatch(limit: Int, now: OffsetDateTime): List<OutboxItem> {
        // SELECT ... FROM email_outbox WHERE status IN ('PENDING','FAILED') AND (next_attempt_at IS NULL OR next_attempt_at <= now) ORDER BY created_at ASC LIMIT :limit
        return emptyList()
    }

    fun markSending(id: UUID) {
        // UPDATE email_outbox SET status='SENDING', updated_at=now() WHERE id=:id
    }

    fun markSent(id: UUID, messageId: String) {
        // UPDATE email_outbox SET status='SENT', message_id=:messageId, updated_at=now() WHERE id=:id
    }

    fun markFailed(id: UUID, attempts: Int, status: String, nextAttemptAt: OffsetDateTime?, error: String?) {
        // UPDATE email_outbox SET status=:status, attempts=:attempts, next_attempt_at=:nextAttemptAt, last_error=:error, updated_at=now() WHERE id=:id
    }
}

@ApplicationScoped
class EventPublisher {
    fun publish(topic: String, payload: Map<String, Any?>) {
        // Insert into event_outbox(topic, payload, status='PENDING', ...)
    }
}

@ApplicationScoped
class TruststoreProvider {
    fun sslContextFromTruststore(): Any {
        // Lädt p12-Truststore von Pfad/Secret; erzeugt SSLContext für SMTP/IMAP
        return Any()
    }
}
