package de.freshplan.crm.email.workers

import de.freshplan.crm.email.models.*
import de.freshplan.crm.email.services.EmailSender
import de.freshplan.crm.events.EventOutboxService
import io.quarkus.scheduler.Scheduled
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.Semaphore
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject
import javax.transaction.Transactional
import kotlin.math.min
import kotlin.math.pow
import kotlin.random.Random

/**
 * Outbox-Dispatcher für zuverlässige Email-Zustellung
 *
 * Features:
 * - Exponential Backoff mit Jitter
 * - Concurrency-Limiting (maxInFlight)
 * - Dead-Letter-Queue nach N Versuchen
 * - Event-Publishing für Monitoring
 * - TLS-Truststore statt Wildcard-Trust
 */
@ApplicationScoped
class OutboxDispatcher @Inject constructor(
    private val outboxRepository: EmailOutboxRepository,
    private val emailSender: EmailSender, // Truststore-basierte Implementation
    private val eventOutboxService: EventOutboxService,
    private val config: OutboxConfig
) {

    companion object {
        private val logger = LoggerFactory.getLogger(OutboxDispatcher::class.java)
    }

    // Semaphore für maxInFlight-Limiting
    private val concurrencyLimiter = Semaphore(config.maxInFlight)

    @Scheduled(every = "{outbox.dispatcherIntervalSeconds}s")
    fun dispatchPendingEmails() {
        logger.debug("Starting outbox dispatch cycle")

        runBlocking {
            val pendingItems = findPendingItems()
            logger.info("Found ${pendingItems.size} pending outbox items")

            // Parallel processing mit Concurrency-Limit
            pendingItems.map { item ->
                async {
                    concurrencyLimiter.acquire()
                    try {
                        processOutboxItem(item)
                    } finally {
                        concurrencyLimiter.release()
                    }
                }
            }.awaitAll()
        }

        logger.debug("Outbox dispatch cycle completed")
    }

    @Transactional
    fun findPendingItems(): List<EmailOutboxItem> {
        val now = LocalDateTime.now()

        return outboxRepository.findPendingForDispatch(
            statuses = listOf(OutboxStatus.PENDING, OutboxStatus.FAILED),
            readyBefore = now,
            limit = config.batchSize
        )
    }

    private suspend fun processOutboxItem(item: EmailOutboxItem) {
        logger.debug("Processing outbox item ${item.id}")

        try {
            // Status auf SENDING setzen für Concurrency-Safety
            updateItemStatus(item, OutboxStatus.SENDING)

            // Email senden via TLS-Truststore-basierter Sender
            val result = emailSender.sendEmail(
                recipient = item.recipient,
                subject = item.subject,
                bodyMime = item.bodyMime,
                threadId = item.threadId
            )

            when (result.status) {
                EmailSendStatus.SENT -> {
                    handleSuccessfulSend(item, result.messageId)
                }
                EmailSendStatus.FAILED -> {
                    handleFailedSend(item, result.error!!)
                }
                EmailSendStatus.RATE_LIMITED -> {
                    handleRateLimited(item)
                }
            }

        } catch (exception: Exception) {
            logger.error("Unexpected error processing outbox item ${item.id}", exception)
            handleFailedSend(item, "Unexpected error: ${exception.message}")
        }
    }

    @Transactional
    fun handleSuccessfulSend(item: EmailOutboxItem, messageId: String?) {
        logger.info("Email sent successfully: ${item.id} -> ${item.recipient}")

        val updatedItem = item.copy(
            status = OutboxStatus.SENT,
            messageId = messageId,
            nextAttemptAt = null,
            lastError = null,
            updatedAt = LocalDateTime.now()
        )

        outboxRepository.save(updatedItem)

        // Event für Monitoring/Analytics
        publishOutboxEvent("email.sent", updatedItem)
    }

    @Transactional
    fun handleFailedSend(item: EmailOutboxItem, error: String) {
        val newAttempts = item.attempts + 1
        val isDeadLetter = newAttempts >= config.deadLetterAfterAttempts

        logger.warn(
            "Email send failed: ${item.id} -> ${item.recipient} " +
            "(attempt $newAttempts/${config.deadLetterAfterAttempts}): $error"
        )

        val updatedItem = if (isDeadLetter) {
            // Dead Letter Queue
            item.copy(
                status = OutboxStatus.DEAD,
                attempts = newAttempts,
                lastError = error,
                nextAttemptAt = null,
                updatedAt = LocalDateTime.now()
            )
        } else {
            // Exponential Backoff mit Jitter
            val backoffMs = calculateBackoffDelay(newAttempts)
            val nextAttempt = LocalDateTime.now().plus(backoffMs, ChronoUnit.MILLIS)

            item.copy(
                status = OutboxStatus.FAILED,
                attempts = newAttempts,
                lastError = error,
                nextAttemptAt = nextAttempt,
                updatedAt = LocalDateTime.now()
            )
        }

        outboxRepository.save(updatedItem)

        // Events für Monitoring
        val eventType = if (isDeadLetter) "email.dead_letter" else "email.failed"
        publishOutboxEvent(eventType, updatedItem)
    }

    @Transactional
    fun handleRateLimited(item: EmailOutboxItem) {
        logger.debug("Rate limited, rescheduling: ${item.id}")

        // Kurzer Delay bei Rate-Limiting (nicht als Failed-Attempt zählen)
        val nextAttempt = LocalDateTime.now().plus(config.rateLimitDelaySeconds, ChronoUnit.SECONDS)

        val updatedItem = item.copy(
            status = OutboxStatus.PENDING,
            nextAttemptAt = nextAttempt,
            updatedAt = LocalDateTime.now()
        )

        outboxRepository.save(updatedItem)
    }

    @Transactional
    fun updateItemStatus(item: EmailOutboxItem, status: OutboxStatus) {
        val updatedItem = item.copy(
            status = status,
            updatedAt = LocalDateTime.now()
        )
        outboxRepository.save(updatedItem)
    }

    /**
     * Exponential Backoff mit Jitter
     *
     * Formula: baseMs * (multiplier ^ attempt) + jitter
     * Jitter: ± (jitterPct/100 * delay)
     */
    private fun calculateBackoffDelay(attempt: Int): Long {
        val baseDelay = config.retryBackoff.baseMs
        val multiplier = config.retryBackoff.multiplier
        val maxDelay = config.retryBackoff.maxMs
        val jitterPct = config.retryBackoff.jitterPct

        // Exponential Backoff
        val exponentialDelay = (baseDelay * multiplier.pow(attempt - 1)).toLong()
        val cappedDelay = min(exponentialDelay, maxDelay)

        // Jitter: ± jitterPct%
        val jitterRange = (cappedDelay * jitterPct / 100).toLong()
        val jitter = Random.nextLong(-jitterRange, jitterRange + 1)

        return cappedDelay + jitter
    }

    private fun publishOutboxEvent(eventType: String, item: EmailOutboxItem) {
        try {
            val eventData = mapOf(
                "outboxId" to item.id,
                "recipient" to item.recipient,
                "subject" to item.subject,
                "status" to item.status.name,
                "attempts" to item.attempts,
                "threadId" to item.threadId
            )

            eventOutboxService.publish(
                topic = eventType,
                payload = eventData,
                idempotencyKey = "${eventType}-${item.id}-${item.updatedAt}"
            )
        } catch (exception: Exception) {
            logger.error("Failed to publish outbox event", exception)
            // Event-Publishing-Fehler sollen Outbox-Processing nicht blockieren
        }
    }
}

// Configuration Data Classes
data class OutboxConfig(
    val maxInFlight: Int = 20,
    val batchSize: Int = 100,
    val deadLetterAfterAttempts: Int = 8,
    val rateLimitDelaySeconds: Long = 60,
    val retryBackoff: RetryBackoffConfig = RetryBackoffConfig()
)

data class RetryBackoffConfig(
    val baseMs: Long = 2000,        // 2 Sekunden
    val maxMs: Long = 600000,       // 10 Minuten
    val multiplier: Double = 2.0,
    val jitterPct: Int = 30         // ± 30% Jitter
)

// Email Sender Result
data class EmailSendResult(
    val status: EmailSendStatus,
    val messageId: String? = null,
    val error: String? = null
)

enum class EmailSendStatus {
    SENT, FAILED, RATE_LIMITED
}

enum class OutboxStatus {
    PENDING, SENDING, FAILED, SENT, DEAD
}