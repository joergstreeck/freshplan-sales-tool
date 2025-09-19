package de.freshplan.communication.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;
import static org.assertj.core.api.Assertions.*;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.concurrent.*;

/**
 * Integration Tests for Communication Module with TestContainers
 * Tests complete flow: Database, Event Bus, Email Processing, SLA Engine
 */
@QuarkusTest
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CommunicationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:15-alpine"))
            .withDatabaseName("freshplan_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("sql-schemas/communication_core.sql")
            .withCommand("postgres", "-c", "max_connections=200");

    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.5.0"))
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true");

    @Container
    static MockSMTPContainer smtp = new MockSMTPContainer();

    @Inject
    EntityManager em;

    @Inject
    CommunicationRepository repository;

    @Inject
    EmailOutboxProcessor outboxProcessor;

    @Inject
    SLAEngine slaEngine;

    private String testCustomerId;
    private String testThreadId;
    private static final String TEST_TERRITORY = "BER";
    private static final String TEST_USER = "sales@freshfoodz.de";

    @BeforeAll
    static void setupContainers() {
        System.setProperty("quarkus.datasource.jdbc.url", postgres.getJdbcUrl());
        System.setProperty("quarkus.datasource.username", postgres.getUsername());
        System.setProperty("quarkus.datasource.password", postgres.getPassword());
        System.setProperty("kafka.bootstrap.servers", kafka.getBootstrapServers());
        System.setProperty("mail.smtp.host", smtp.getHost());
        System.setProperty("mail.smtp.port", String.valueOf(smtp.getPort()));
    }

    @BeforeEach
    void setup() {
        testCustomerId = UUID.randomUUID().toString();
    }

    @Nested
    @DisplayName("Database Integration Tests")
    class DatabaseIntegration {

        @Test
        @Order(1)
        @DisplayName("Should create thread with RLS policies")
        @Transactional
        void shouldCreateThreadWithRLS() {
            // Given
            Thread thread = Thread.builder()
                .customerId(testCustomerId)
                .territory(TEST_TERRITORY)
                .channel(Thread.Channel.EMAIL)
                .subject("Integration Test Thread")
                .status(Thread.Status.OPEN)
                .createdBy(TEST_USER)
                .build();

            // When
            em.persist(thread);
            em.flush();
            testThreadId = thread.getId();

            // Then - Verify RLS policies
            String rlsQuery = """
                SELECT COUNT(*) FROM communication_threads
                WHERE id = ?1
                AND territory = ANY(current_setting('app.territories')::text[])
                """;

            // Set territory context
            em.createNativeQuery("SET app.territories = '{BER}'").executeUpdate();
            Long countWithAccess = (Long) em.createNativeQuery(rlsQuery)
                .setParameter(1, testThreadId)
                .getSingleResult();
            assertThat(countWithAccess).isEqualTo(1L);

            // Try different territory
            em.createNativeQuery("SET app.territories = '{MUC}'").executeUpdate();
            Long countNoAccess = (Long) em.createNativeQuery(rlsQuery)
                .setParameter(1, testThreadId)
                .getSingleResult();
            assertThat(countNoAccess).isZero();
        }

        @Test
        @Order(2)
        @DisplayName("Should handle concurrent updates with version control")
        void shouldHandleConcurrentUpdates() throws Exception {
            // Create thread
            Thread thread = createTestThread();
            Long initialVersion = thread.getVersion();

            // Simulate concurrent updates
            ExecutorService executor = Executors.newFixedThreadPool(5);
            List<Future<Boolean>> futures = new ArrayList<>();

            for (int i = 0; i < 5; i++) {
                final int index = i;
                futures.add(executor.submit(() -> {
                    try {
                        // Each thread tries to update
                        return updateThreadConcurrently(thread.getId(), initialVersion, index);
                    } catch (Exception e) {
                        return false;
                    }
                }));
            }

            // Wait for all updates
            List<Boolean> results = new ArrayList<>();
            for (Future<Boolean> future : futures) {
                results.add(future.get(5, TimeUnit.SECONDS));
            }

            // Only one should succeed
            long successCount = results.stream().filter(r -> r).count();
            assertThat(successCount).isEqualTo(1L);

            // Verify final version
            Thread updated = em.find(Thread.class, thread.getId());
            assertThat(updated.getVersion()).isGreaterThan(initialVersion);

            executor.shutdown();
        }

        @Test
        @Order(3)
        @DisplayName("Should enforce database constraints")
        void shouldEnforceDatabaseConstraints() {
            // Test unique constraint on message_id
            Message msg1 = new Message();
            msg1.setThreadId(testThreadId);
            msg1.setMessageId("<unique@test.com>");
            msg1.setSender(TEST_USER);

            Message msg2 = new Message();
            msg2.setThreadId(testThreadId);
            msg2.setMessageId("<unique@test.com>"); // Same message ID
            msg2.setSender(TEST_USER);

            assertThatThrownBy(() -> {
                em.persist(msg1);
                em.persist(msg2);
                em.flush();
            }).hasMessageContaining("duplicate key value violates unique constraint");
        }

        @Test
        @Order(4)
        @DisplayName("Should cascade delete messages with thread")
        @Transactional
        void shouldCascadeDeleteMessagesWithThread() {
            // Create thread with messages
            Thread thread = createTestThread();
            Message msg1 = createMessage(thread.getId(), "Message 1");
            Message msg2 = createMessage(thread.getId(), "Message 2");

            em.persist(msg1);
            em.persist(msg2);
            em.flush();

            // Delete thread
            em.remove(thread);
            em.flush();

            // Verify messages are deleted
            List<Message> messages = em.createQuery(
                "SELECT m FROM Message m WHERE m.threadId = :threadId", Message.class)
                .setParameter("threadId", thread.getId())
                .getResultList();

            assertThat(messages).isEmpty();
        }
    }

    @Nested
    @DisplayName("Email Outbox Processing")
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class EmailOutboxProcessing {

        @Test
        @Order(5)
        @DisplayName("Should process outbox emails with retry logic")
        void shouldProcessOutboxEmailsWithRetry() throws Exception {
            // Create outbox email
            OutboxEmail email = new OutboxEmail();
            email.setThreadId(testThreadId);
            email.setMessageId(UUID.randomUUID().toString());
            email.setRecipient("customer@restaurant.de");
            email.setSubject("Test Email");
            email.setBody("Test content");
            email.setStatus(OutboxEmail.Status.PENDING);
            email.setCreatedAt(OffsetDateTime.now());

            em.persist(email);
            em.flush();

            // Simulate SMTP failure first
            smtp.simulateFailure(true);

            // Process outbox (should fail and retry)
            outboxProcessor.processOutbox();

            // Verify retry scheduled
            OutboxEmail updated = em.find(OutboxEmail.class, email.getId());
            assertThat(updated.getStatus()).isEqualTo(OutboxEmail.Status.RETRY);
            assertThat(updated.getRetryCount()).isEqualTo(1);
            assertThat(updated.getNextRetryAt()).isNotNull();

            // Fix SMTP and retry
            smtp.simulateFailure(false);
            Thread.sleep(1000); // Wait for retry time

            outboxProcessor.processOutbox();

            // Verify sent
            OutboxEmail sent = em.find(OutboxEmail.class, email.getId());
            assertThat(sent.getStatus()).isEqualTo(OutboxEmail.Status.SENT);
            assertThat(sent.getSentAt()).isNotNull();

            // Verify SMTP received
            assertThat(smtp.getReceivedEmails()).hasSize(1);
            assertThat(smtp.getReceivedEmails().get(0).getRecipient())
                .isEqualTo("customer@restaurant.de");
        }

        @Test
        @Order(6)
        @DisplayName("Should handle bounce events")
        void shouldHandleBounceEvents() {
            // Create sent email
            OutboxEmail email = new OutboxEmail();
            email.setMessageId("bounce-test-123");
            email.setRecipient("invalid@nonexistent.com");
            email.setStatus(OutboxEmail.Status.SENT);
            email.setSentAt(OffsetDateTime.now());

            em.persist(email);
            em.flush();

            // Simulate bounce webhook
            BounceEvent bounce = new BounceEvent();
            bounce.setMessageId(email.getMessageId());
            bounce.setBounceType(BounceEvent.Type.HARD);
            bounce.setReason("Mailbox does not exist");
            bounce.setBouncedAt(OffsetDateTime.now());

            // Process bounce
            repository.processBounceEvent(bounce);

            // Verify email marked as bounced
            OutboxEmail bounced = em.find(OutboxEmail.class, email.getId());
            assertThat(bounced.getStatus()).isEqualTo(OutboxEmail.Status.BOUNCED);
            assertThat(bounced.getBounceType()).isEqualTo(BounceEvent.Type.HARD);

            // Verify recipient blacklisted for hard bounces
            boolean isBlacklisted = repository.isEmailBlacklisted("invalid@nonexistent.com");
            assertThat(isBlacklisted).isTrue();
        }

        @Test
        @Order(7)
        @DisplayName("Should respect exponential backoff for retries")
        void shouldRespectExponentialBackoff() {
            OutboxEmail email = new OutboxEmail();
            email.setRecipient("test@test.de");
            email.setRetryCount(0);

            // Calculate retry times
            OffsetDateTime retry1 = email.calculateNextRetryTime();
            email.setRetryCount(1);
            OffsetDateTime retry2 = email.calculateNextRetryTime();
            email.setRetryCount(2);
            OffsetDateTime retry3 = email.calculateNextRetryTime();

            // Verify exponential backoff
            long diff1 = java.time.Duration.between(OffsetDateTime.now(), retry1).toMinutes();
            long diff2 = java.time.Duration.between(OffsetDateTime.now(), retry2).toMinutes();
            long diff3 = java.time.Duration.between(OffsetDateTime.now(), retry3).toMinutes();

            assertThat(diff1).isLessThan(diff2);
            assertThat(diff2).isLessThan(diff3);
            // Should be roughly 1, 2, 4 minutes
            assertThat(diff1).isBetween(0L, 2L);
            assertThat(diff2).isBetween(1L, 3L);
            assertThat(diff3).isBetween(3L, 5L);
        }
    }

    @Nested
    @DisplayName("SLA Engine Integration")
    class SLAEngineIntegration {

        @Test
        @Order(8)
        @DisplayName("Should schedule T+3 and T+7 follow-ups")
        @Transactional
        void shouldScheduleFollowUps() {
            // Trigger sample delivered event
            OffsetDateTime deliveredAt = OffsetDateTime.now();
            slaEngine.onSampleDelivered(testCustomerId, TEST_TERRITORY, deliveredAt);

            // Verify T+3 task scheduled
            List<SLATask> tasks = em.createQuery(
                "SELECT t FROM SLATask t WHERE t.customerId = :customerId", SLATask.class)
                .setParameter("customerId", testCustomerId)
                .getResultList();

            assertThat(tasks).hasSize(2);

            // Check T+3
            SLATask t3Task = tasks.stream()
                .filter(t -> t.getType().equals("follow_up_t3"))
                .findFirst().orElse(null);
            assertThat(t3Task).isNotNull();
            assertThat(t3Task.getDueAt()).isAfter(deliveredAt.plusDays(2));
            assertThat(t3Task.getDueAt()).isBefore(deliveredAt.plusDays(4));

            // Check T+7
            SLATask t7Task = tasks.stream()
                .filter(t -> t.getType().equals("follow_up_t7"))
                .findFirst().orElse(null);
            assertThat(t7Task).isNotNull();
            assertThat(t7Task.getDueAt()).isAfter(deliveredAt.plusDays(6));
            assertThat(t7Task.getDueAt()).isBefore(deliveredAt.plusDays(8));
        }

        @Test
        @Order(9)
        @DisplayName("Should process due SLA tasks")
        void shouldProcessDueSLATasks() {
            // Create overdue task
            SLATask task = new SLATask();
            task.setCustomerId(testCustomerId);
            task.setTerritory(TEST_TERRITORY);
            task.setType("follow_up_t3");
            task.setDueAt(OffsetDateTime.now().minusHours(1));
            task.setStatus(SLATask.Status.PENDING);

            em.persist(task);
            em.flush();

            // Process SLA tasks
            int processed = slaEngine.processDueTasks();

            // Verify processed
            assertThat(processed).isEqualTo(1);

            SLATask updated = em.find(SLATask.class, task.getId());
            assertThat(updated.getStatus()).isEqualTo(SLATask.Status.COMPLETED);
            assertThat(updated.getProcessedAt()).isNotNull();

            // Verify follow-up thread created
            List<Thread> threads = em.createQuery(
                "SELECT t FROM Thread t WHERE t.customerId = :customerId " +
                "AND t.subject LIKE '%Follow-up T+3%'", Thread.class)
                .setParameter("customerId", testCustomerId)
                .getResultList();

            assertThat(threads).hasSize(1);
        }

        @Test
        @Order(10)
        @DisplayName("Should escalate overdue tasks")
        void shouldEscalateOverdueTasks() {
            // Create severely overdue task
            SLATask task = new SLATask();
            task.setCustomerId(testCustomerId);
            task.setType("critical_response");
            task.setDueAt(OffsetDateTime.now().minusDays(3));
            task.setStatus(SLATask.Status.PENDING);
            task.setPriority(SLATask.Priority.HIGH);

            em.persist(task);
            em.flush();

            // Process escalations
            slaEngine.processEscalations();

            // Verify escalation
            SLATask escalated = em.find(SLATask.class, task.getId());
            assertThat(escalated.getStatus()).isEqualTo(SLATask.Status.ESCALATED);
            assertThat(escalated.getEscalatedTo()).isNotNull();

            // Verify notification sent
            assertThat(smtp.getReceivedEmails())
                .anyMatch(e -> e.getSubject().contains("SLA Escalation"));
        }
    }

    @Nested
    @DisplayName("Event Bus Integration")
    class EventBusIntegration {

        @Test
        @Order(11)
        @DisplayName("Should publish thread events to Kafka")
        void shouldPublishThreadEventsToKafka() throws Exception {
            // Create Kafka consumer
            KafkaConsumer<String, String> consumer = createKafkaConsumer();
            consumer.subscribe(List.of("communication.threads"));

            // Create thread (should trigger event)
            Thread thread = createTestThread();
            em.persist(thread);
            em.flush();

            // Poll for event
            ConsumerRecords<String, String> records = consumer.poll(
                java.time.Duration.ofSeconds(5));

            assertThat(records.count()).isGreaterThan(0);

            ConsumerRecord<String, String> record = records.iterator().next();
            assertThat(record.key()).isEqualTo(thread.getId());
            assertThat(record.value()).contains("\"event\":\"thread.created\"");
            assertThat(record.value()).contains(testCustomerId);

            consumer.close();
        }

        @Test
        @Order(12)
        @DisplayName("Should consume cross-module events")
        void shouldConsumeCrossModuleEvents() throws Exception {
            // Publish sample delivered event from Module 02
            KafkaProducer<String, String> producer = createKafkaProducer();

            String event = """
                {
                    "event": "sample.delivered",
                    "customerId": "%s",
                    "territory": "%s",
                    "deliveredAt": "%s",
                    "products": ["Gulasch", "Bolognese"]
                }
                """.formatted(testCustomerId, TEST_TERRITORY, OffsetDateTime.now());

            producer.send(new ProducerRecord<>("samples.events", testCustomerId, event));
            producer.flush();

            // Wait for processing
            Thread.sleep(2000);

            // Verify SLA tasks created
            List<SLATask> tasks = em.createQuery(
                "SELECT t FROM SLATask t WHERE t.customerId = :customerId", SLATask.class)
                .setParameter("customerId", testCustomerId)
                .getResultList();

            assertThat(tasks).isNotEmpty();
            assertThat(tasks).anyMatch(t -> t.getType().equals("sample_follow_up"));

            producer.close();
        }
    }

    @Nested
    @DisplayName("API Integration Tests")
    @TestSecurity(user = TEST_USER, roles = {"sales"})
    @JwtSecurity(claims = {
        @Claim(key = "territories", value = "[\"BER\", \"MUC\"]"),
        @Claim(key = "sub", value = TEST_USER)
    })
    class APIIntegration {

        @Test
        @Order(13)
        @DisplayName("Should enforce ABAC with territory scoping")
        void shouldEnforceABACWithTerritoryScoping() {
            // Create threads in different territories
            String berThreadId = createThreadInTerritory("BER");
            String hamThreadId = createThreadInTerritory("HAM");

            // Can access BER thread
            given()
                .header("X-Territories", "BER")
                .when()
                .get("/api/comm/threads/{id}", berThreadId)
                .then()
                .statusCode(200);

            // Cannot access HAM thread
            given()
                .header("X-Territories", "BER")
                .when()
                .get("/api/comm/threads/{id}", hamThreadId)
                .then()
                .statusCode(403);
        }

        @Test
        @Order(14)
        @DisplayName("Should handle ETag concurrency control")
        void shouldHandleETagConcurrencyControl() {
            String threadId = createThreadInTerritory("BER");

            // Get thread with ETag
            String etag = given()
                .header("X-Territories", "BER")
                .when()
                .get("/api/comm/threads/{id}", threadId)
                .then()
                .statusCode(200)
                .extract()
                .header("ETag");

            // Reply with correct ETag
            given()
                .header("X-Territories", "BER")
                .header("If-Match", etag)
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "body": "Reply with correct ETag"
                    }
                    """)
                .when()
                .post("/api/comm/threads/{id}/reply", threadId)
                .then()
                .statusCode(201);

            // Reply with wrong ETag
            given()
                .header("X-Territories", "BER")
                .header("If-Match", "\"v999\"")
                .contentType(ContentType.JSON)
                .body("""
                    {
                        "body": "Reply with wrong ETag"
                    }
                    """)
                .when()
                .post("/api/comm/threads/{id}/reply", threadId)
                .then()
                .statusCode(412)
                .body("detail", containsString("ETag mismatch"));
        }

        @Test
        @Order(15)
        @DisplayName("Should paginate with cursor")
        void shouldPaginateWithCursor() {
            // Create multiple threads
            for (int i = 0; i < 25; i++) {
                createThreadInTerritory("BER");
            }

            // First page
            Map<String, Object> page1 = given()
                .header("X-Territories", "BER")
                .queryParam("limit", 10)
                .when()
                .get("/api/comm/threads")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Map.class);

            assertThat((List<?>) page1.get("items")).hasSize(10);
            assertThat(page1.get("nextCursor")).isNotNull();

            // Next page with cursor
            Map<String, Object> page2 = given()
                .header("X-Territories", "BER")
                .queryParam("cursor", page1.get("nextCursor"))
                .queryParam("limit", 10)
                .when()
                .get("/api/comm/threads")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .as(Map.class);

            assertThat((List<?>) page2.get("items")).hasSize(10);

            // Verify no duplicates between pages
            List<String> ids1 = ((List<Map<String, Object>>) page1.get("items"))
                .stream().map(m -> (String) m.get("id")).toList();
            List<String> ids2 = ((List<Map<String, Object>>) page2.get("items"))
                .stream().map(m -> (String) m.get("id")).toList();

            assertThat(ids1).doesNotContainAnyElementsOf(ids2);
        }
    }

    // Helper Methods

    @Transactional
    Thread createTestThread() {
        Thread thread = Thread.builder()
            .customerId(testCustomerId)
            .territory(TEST_TERRITORY)
            .channel(Thread.Channel.EMAIL)
            .subject("Test Thread")
            .status(Thread.Status.OPEN)
            .createdBy(TEST_USER)
            .build();
        em.persist(thread);
        return thread;
    }

    Message createMessage(String threadId, String body) {
        Message msg = new Message();
        msg.setThreadId(threadId);
        msg.setSender(TEST_USER);
        msg.setBody(body);
        msg.setChannel(Message.Channel.EMAIL);
        msg.setStatus(Message.Status.SENT);
        return msg;
    }

    @Transactional
    boolean updateThreadConcurrently(String threadId, Long expectedVersion, int index) {
        try {
            Thread thread = em.find(Thread.class, threadId);
            if (thread.getVersion().equals(expectedVersion)) {
                thread.incrementMessageCount();
                thread.setLastMessageAt(OffsetDateTime.now());
                em.merge(thread);
                em.flush();
                return true;
            }
        } catch (Exception e) {
            // Expected for concurrent updates
        }
        return false;
    }

    String createThreadInTerritory(String territory) {
        return given()
            .header("X-Territories", territory)
            .contentType(ContentType.JSON)
            .body("""
                {
                    "customerId": "%s",
                    "territory": "%s",
                    "channel": "EMAIL",
                    "subject": "Test Thread %s",
                    "body": "Test content"
                }
                """.formatted(UUID.randomUUID(), territory, territory))
            .when()
            .post("/api/comm/threads")
            .then()
            .statusCode(201)
            .extract()
            .jsonPath()
            .getString("id");
    }

    KafkaConsumer<String, String> createKafkaConsumer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafka.getBootstrapServers());
        props.put("group.id", "test-group");
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("auto.offset.reset", "earliest");
        return new KafkaConsumer<>(props);
    }

    KafkaProducer<String, String> createKafkaProducer() {
        Properties props = new Properties();
        props.put("bootstrap.servers", kafka.getBootstrapServers());
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(props);
    }
}

/**
 * Mock SMTP Container for testing email functionality
 */
class MockSMTPContainer extends GenericContainer<MockSMTPContainer> {
    private final List<Email> receivedEmails = new ArrayList<>();
    private boolean simulateFailure = false;

    public MockSMTPContainer() {
        super(DockerImageName.parse("mailhog/mailhog:latest"));
        withExposedPorts(1025, 8025);
    }

    public void simulateFailure(boolean fail) {
        this.simulateFailure = fail;
    }

    public List<Email> getReceivedEmails() {
        return receivedEmails;
    }

    public int getPort() {
        return getMappedPort(1025);
    }

    static class Email {
        String recipient;
        String subject;
        String body;
        // getters/setters...
    }
}