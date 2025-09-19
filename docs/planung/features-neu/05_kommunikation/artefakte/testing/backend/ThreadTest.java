package de.freshplan.communication.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit Tests for Thread Entity - Domain Logic Validation
 * Coverage Target: 95%+ for critical business logic
 */
class ThreadTest {

    private Thread thread;
    private final String TEST_CUSTOMER_ID = "11111111-1111-1111-1111-111111111111";
    private final String TEST_TERRITORY = "BER";
    private final String TEST_USER = "sales@freshfoodz.de";

    @BeforeEach
    void setUp() {
        thread = new Thread();
        thread.setId(UUID.randomUUID().toString());
        thread.setCustomerId(TEST_CUSTOMER_ID);
        thread.setTerritory(TEST_TERRITORY);
        thread.setCreatedBy(TEST_USER);
        thread.setCreatedAt(OffsetDateTime.now());
    }

    @Nested
    @DisplayName("Thread Creation Tests")
    class ThreadCreation {

        @Test
        @DisplayName("Should create thread with valid data")
        void shouldCreateThreadWithValidData() {
            // Given
            Thread newThread = Thread.builder()
                .customerId(TEST_CUSTOMER_ID)
                .territory(TEST_TERRITORY)
                .channel(Thread.Channel.EMAIL)
                .subject("Sample Follow-up T+3")
                .status(Thread.Status.OPEN)
                .createdBy(TEST_USER)
                .build();

            // Then
            assertThat(newThread).isNotNull();
            assertThat(newThread.getCustomerId()).isEqualTo(TEST_CUSTOMER_ID);
            assertThat(newThread.getTerritory()).isEqualTo(TEST_TERRITORY);
            assertThat(newThread.getChannel()).isEqualTo(Thread.Channel.EMAIL);
            assertThat(newThread.getStatus()).isEqualTo(Thread.Status.OPEN);
            assertThat(newThread.getUnreadCount()).isZero();
            assertThat(newThread.getVersion()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should initialize with default values")
        void shouldInitializeWithDefaultValues() {
            // Given
            Thread defaultThread = new Thread();

            // Then
            assertThat(defaultThread.getStatus()).isEqualTo(Thread.Status.OPEN);
            assertThat(defaultThread.getUnreadCount()).isZero();
            assertThat(defaultThread.getVersion()).isEqualTo(1L);
            assertThat(defaultThread.getMessageCount()).isZero();
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject invalid customer ID")
        void shouldRejectInvalidCustomerId(String customerId) {
            // When/Then
            assertThatThrownBy(() -> {
                Thread.builder()
                    .customerId(customerId)
                    .territory(TEST_TERRITORY)
                    .build()
                    .validate();
            }).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("Customer ID is required");
        }

        @ParameterizedTest
        @ValueSource(strings = {"INVALID", "ber", "Berlin", "DE-BER", ""})
        @DisplayName("Should reject invalid territory codes")
        void shouldRejectInvalidTerritoryCodes(String territory) {
            // When/Then
            assertThatThrownBy(() -> {
                Thread.builder()
                    .customerId(TEST_CUSTOMER_ID)
                    .territory(territory)
                    .build()
                    .validateTerritory();
            }).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("Invalid territory code");
        }
    }

    @Nested
    @DisplayName("Thread Status Management")
    class ThreadStatusManagement {

        @Test
        @DisplayName("Should transition from OPEN to IN_PROGRESS")
        void shouldTransitionFromOpenToInProgress() {
            // Given
            thread.setStatus(Thread.Status.OPEN);

            // When
            thread.markInProgress(TEST_USER);

            // Then
            assertThat(thread.getStatus()).isEqualTo(Thread.Status.IN_PROGRESS);
            assertThat(thread.getAssignedTo()).isEqualTo(TEST_USER);
            assertThat(thread.getAssignedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should transition from IN_PROGRESS to RESOLVED")
        void shouldTransitionFromInProgressToResolved() {
            // Given
            thread.setStatus(Thread.Status.IN_PROGRESS);
            thread.setAssignedTo(TEST_USER);

            // When
            thread.resolve(TEST_USER, "Issue resolved");

            // Then
            assertThat(thread.getStatus()).isEqualTo(Thread.Status.RESOLVED);
            assertThat(thread.getResolvedBy()).isEqualTo(TEST_USER);
            assertThat(thread.getResolvedAt()).isNotNull();
            assertThat(thread.getResolutionNote()).isEqualTo("Issue resolved");
        }

        @Test
        @DisplayName("Should not allow invalid status transitions")
        void shouldNotAllowInvalidStatusTransitions() {
            // Given
            thread.setStatus(Thread.Status.RESOLVED);

            // When/Then
            assertThatThrownBy(() -> thread.markInProgress(TEST_USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot transition from RESOLVED to IN_PROGRESS");
        }

        @Test
        @DisplayName("Should archive old resolved threads")
        void shouldArchiveOldResolvedThreads() {
            // Given
            thread.setStatus(Thread.Status.RESOLVED);
            thread.setResolvedAt(OffsetDateTime.now().minusDays(31));

            // When
            boolean shouldArchive = thread.shouldArchive();

            // Then
            assertThat(shouldArchive).isTrue();
        }
    }

    @Nested
    @DisplayName("Message Count and Unread Management")
    class MessageManagement {

        @Test
        @DisplayName("Should increment message count")
        void shouldIncrementMessageCount() {
            // Given
            int initialCount = thread.getMessageCount();

            // When
            thread.incrementMessageCount();

            // Then
            assertThat(thread.getMessageCount()).isEqualTo(initialCount + 1);
            assertThat(thread.getLastMessageAt()).isNotNull();
            assertThat(thread.getVersion()).isEqualTo(2L);
        }

        @Test
        @DisplayName("Should update unread count")
        void shouldUpdateUnreadCount() {
            // When
            thread.addUnreadMessage();
            thread.addUnreadMessage();
            thread.addUnreadMessage();

            // Then
            assertThat(thread.getUnreadCount()).isEqualTo(3);
        }

        @Test
        @DisplayName("Should mark all as read")
        void shouldMarkAllAsRead() {
            // Given
            thread.setUnreadCount(5);

            // When
            thread.markAllAsRead(TEST_USER);

            // Then
            assertThat(thread.getUnreadCount()).isZero();
            assertThat(thread.getLastReadBy()).isEqualTo(TEST_USER);
            assertThat(thread.getLastReadAt()).isNotNull();
        }

        @Test
        @DisplayName("Should track last activity")
        void shouldTrackLastActivity() {
            // Given
            OffsetDateTime beforeActivity = OffsetDateTime.now();

            // When
            thread.updateLastActivity();

            // Then
            assertThat(thread.getLastActivityAt()).isAfter(beforeActivity);
            assertThat(thread.getVersion()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("ETag and Concurrency Control")
    class ETagConcurrencyControl {

        @Test
        @DisplayName("Should generate ETag from version")
        void shouldGenerateETagFromVersion() {
            // Given
            thread.setVersion(42L);

            // When
            String etag = thread.generateETag();

            // Then
            assertThat(etag).isEqualTo("\"v42\"");
        }

        @Test
        @DisplayName("Should validate matching ETag")
        void shouldValidateMatchingETag() {
            // Given
            thread.setVersion(10L);
            String etag = "\"v10\"";

            // When
            boolean isValid = thread.validateETag(etag);

            // Then
            assertThat(isValid).isTrue();
        }

        @Test
        @DisplayName("Should reject mismatched ETag")
        void shouldRejectMismatchedETag() {
            // Given
            thread.setVersion(10L);
            String wrongEtag = "\"v9\"";

            // When/Then
            assertThatThrownBy(() -> thread.ensureETagMatch(wrongEtag))
                .isInstanceOf(ConcurrentModificationException.class)
                .hasMessageContaining("ETag mismatch");
        }

        @Test
        @DisplayName("Should increment version on update")
        void shouldIncrementVersionOnUpdate() {
            // Given
            long initialVersion = thread.getVersion();

            // When
            thread.updateAndIncrementVersion();

            // Then
            assertThat(thread.getVersion()).isEqualTo(initialVersion + 1);
            assertThat(thread.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Channel-Specific Behavior")
    class ChannelSpecificBehavior {

        @ParameterizedTest
        @EnumSource(Thread.Channel.class)
        @DisplayName("Should handle all channel types")
        void shouldHandleAllChannelTypes(Thread.Channel channel) {
            // When
            thread.setChannel(channel);

            // Then
            assertThat(thread.getChannel()).isEqualTo(channel);
            assertThat(thread.getChannelMetadata()).isNotNull();
        }

        @Test
        @DisplayName("Should store email-specific metadata")
        void shouldStoreEmailSpecificMetadata() {
            // Given
            thread.setChannel(Thread.Channel.EMAIL);

            // When
            thread.setEmailMetadata("from@example.com", "to@example.com", "cc@example.com");

            // Then
            assertThat(thread.getChannelMetadata().get("from")).isEqualTo("from@example.com");
            assertThat(thread.getChannelMetadata().get("to")).isEqualTo("to@example.com");
            assertThat(thread.getChannelMetadata().get("cc")).isEqualTo("cc@example.com");
        }

        @Test
        @DisplayName("Should store phone-specific metadata")
        void shouldStorePhoneSpecificMetadata() {
            // Given
            thread.setChannel(Thread.Channel.PHONE);

            // When
            thread.setPhoneMetadata("+49 30 123456", "5m 30s", "Küchenchef");

            // Then
            assertThat(thread.getChannelMetadata().get("number")).isEqualTo("+49 30 123456");
            assertThat(thread.getChannelMetadata().get("duration")).isEqualTo("5m 30s");
            assertThat(thread.getChannelMetadata().get("contact")).isEqualTo("Küchenchef");
        }

        @Test
        @DisplayName("Should store meeting-specific metadata")
        void shouldStoreMeetingSpecificMetadata() {
            // Given
            thread.setChannel(Thread.Channel.MEETING);

            // When
            thread.setMeetingMetadata("Restaurant Adler", "2025-09-20T10:00:00Z", "Sample tasting");

            // Then
            assertThat(thread.getChannelMetadata().get("location")).isEqualTo("Restaurant Adler");
            assertThat(thread.getChannelMetadata().get("scheduledAt")).isEqualTo("2025-09-20T10:00:00Z");
            assertThat(thread.getChannelMetadata().get("agenda")).isEqualTo("Sample tasting");
        }
    }

    @Nested
    @DisplayName("B2B Food Specific Rules")
    class B2BFoodSpecificRules {

        @Test
        @DisplayName("Should identify sample follow-up threads")
        void shouldIdentifySampleFollowUpThreads() {
            // Given
            thread.setSubject("Sample Follow-up T+3: Restaurant Adler");
            thread.addTag("sample_delivered");

            // When
            boolean isSampleFollowup = thread.isSampleFollowUp();

            // Then
            assertThat(isSampleFollowup).isTrue();
        }

        @Test
        @DisplayName("Should calculate follow-up urgency")
        void shouldCalculateFollowUpUrgency() {
            // Given
            thread.addTag("sample_delivered");
            thread.setCreatedAt(OffsetDateTime.now().minusDays(7));

            // When
            Thread.Urgency urgency = thread.calculateUrgency();

            // Then
            assertThat(urgency).isEqualTo(Thread.Urgency.HIGH);
        }

        @Test
        @DisplayName("Should identify multi-contact threads")
        void shouldIdentifyMultiContactThreads() {
            // Given
            thread.addParticipant("kuechenchef@restaurant.de", "Küchenchef");
            thread.addParticipant("einkauf@restaurant.de", "Einkauf");

            // When
            boolean isMultiContact = thread.isMultiContactThread();

            // Then
            assertThat(isMultiContact).isTrue();
            assertThat(thread.getParticipantRoles()).contains("Küchenchef", "Einkauf");
        }

        @Test
        @DisplayName("Should validate territory boundaries")
        void shouldValidateTerritoryBoundaries() {
            // Given
            thread.setTerritory("BER");
            String userTerritory = "MUC";

            // When
            boolean hasAccess = thread.userHasAccess(userTerritory);

            // Then
            assertThat(hasAccess).isFalse();
        }
    }

    @Nested
    @DisplayName("Thread Priority and SLA")
    class ThreadPrioritySLA {

        @Test
        @DisplayName("Should calculate priority based on customer value")
        void shouldCalculatePriorityBasedOnCustomerValue() {
            // Given
            thread.setCustomerValue(Thread.CustomerValue.HIGH);
            thread.setChannel(Thread.Channel.EMAIL);

            // When
            Thread.Priority priority = thread.calculatePriority();

            // Then
            assertThat(priority).isEqualTo(Thread.Priority.HIGH);
        }

        @Test
        @DisplayName("Should determine SLA deadline")
        void shouldDetermineSLADeadline() {
            // Given
            thread.setChannel(Thread.Channel.EMAIL);
            thread.setCreatedAt(OffsetDateTime.now());
            thread.setPriority(Thread.Priority.HIGH);

            // When
            OffsetDateTime slaDeadline = thread.getSLADeadline();

            // Then
            assertThat(slaDeadline).isAfter(OffsetDateTime.now());
            assertThat(slaDeadline).isBefore(OffsetDateTime.now().plusHours(4));
        }

        @Test
        @DisplayName("Should identify SLA breaches")
        void shouldIdentifySLABreaches() {
            // Given
            thread.setPriority(Thread.Priority.URGENT);
            thread.setCreatedAt(OffsetDateTime.now().minusHours(5));
            thread.setStatus(Thread.Status.OPEN);

            // When
            boolean isBreached = thread.isSLABreached();

            // Then
            assertThat(isBreached).isTrue();
        }
    }

    @Nested
    @DisplayName("Thread Serialization and Equality")
    class SerializationEquality {

        @Test
        @DisplayName("Should implement equals correctly")
        void shouldImplementEqualsCorrectly() {
            // Given
            Thread thread1 = new Thread();
            thread1.setId("123");
            Thread thread2 = new Thread();
            thread2.setId("123");
            Thread thread3 = new Thread();
            thread3.setId("456");

            // Then
            assertThat(thread1).isEqualTo(thread2);
            assertThat(thread1).isNotEqualTo(thread3);
            assertThat(thread1).isNotEqualTo(null);
            assertThat(thread1).isNotEqualTo("not a thread");
        }

        @Test
        @DisplayName("Should implement hashCode correctly")
        void shouldImplementHashCodeCorrectly() {
            // Given
            Thread thread1 = new Thread();
            thread1.setId("123");
            Thread thread2 = new Thread();
            thread2.setId("123");

            // Then
            assertThat(thread1.hashCode()).isEqualTo(thread2.hashCode());
        }

        @Test
        @DisplayName("Should serialize to JSON correctly")
        void shouldSerializeToJsonCorrectly() {
            // Given
            thread.setSubject("Test Subject");
            thread.setChannel(Thread.Channel.EMAIL);

            // When
            String json = thread.toJson();

            // Then
            assertThat(json).contains("\"subject\":\"Test Subject\"");
            assertThat(json).contains("\"channel\":\"EMAIL\"");
            assertThat(json).contains("\"territory\":\"BER\"");
        }
    }
}