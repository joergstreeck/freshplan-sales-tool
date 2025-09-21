package de.freshplan.communication.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.CsvSource;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.Set;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Unit Tests for Message Entity - Email/Phone/Meeting Message Logic
 * Coverage Target: 95%+ for communication message handling
 */
class MessageTest {

    private Message message;
    private final String TEST_THREAD_ID = "22222222-2222-2222-2222-222222222222";
    private final String TEST_SENDER = "sales@freshfoodz.de";

    @BeforeEach
    void setUp() {
        message = new Message();
        message.setId(UUID.randomUUID().toString());
        message.setThreadId(TEST_THREAD_ID);
        message.setSender(TEST_SENDER);
        message.setCreatedAt(OffsetDateTime.now());
    }

    @Nested
    @DisplayName("Message Creation and Validation")
    class MessageCreation {

        @Test
        @DisplayName("Should create email message with valid data")
        void shouldCreateEmailMessageWithValidData() {
            // Given
            Message email = Message.createEmailMessage(
                TEST_THREAD_ID,
                TEST_SENDER,
                "customer@restaurant.de",
                "Re: Sample Follow-up",
                "Thank you for testing our products...",
                "cc@restaurant.de"
            );

            // Then
            assertThat(email).isNotNull();
            assertThat(email.getChannel()).isEqualTo(Message.Channel.EMAIL);
            assertThat(email.getSubject()).isEqualTo("Re: Sample Follow-up");
            assertThat(email.getBody()).contains("Thank you");
            assertThat(email.getRecipients()).contains("customer@restaurant.de");
            assertThat(email.getCcRecipients()).contains("cc@restaurant.de");
            assertThat(email.getStatus()).isEqualTo(Message.Status.PENDING);
        }

        @Test
        @DisplayName("Should create phone call message")
        void shouldCreatePhoneCallMessage() {
            // Given
            Message phone = Message.createPhoneMessage(
                TEST_THREAD_ID,
                TEST_SENDER,
                "+49 30 123456",
                "5m 30s",
                "Discussed sample feedback with Küchenchef"
            );

            // Then
            assertThat(phone.getChannel()).isEqualTo(Message.Channel.PHONE);
            assertThat(phone.getPhoneNumber()).isEqualTo("+49 30 123456");
            assertThat(phone.getCallDuration()).isEqualTo("5m 30s");
            assertThat(phone.getBody()).contains("Küchenchef");
            assertThat(phone.getStatus()).isEqualTo(Message.Status.COMPLETED);
        }

        @Test
        @DisplayName("Should create meeting notes message")
        void shouldCreateMeetingMessage() {
            // Given
            Message meeting = Message.createMeetingMessage(
                TEST_THREAD_ID,
                TEST_SENDER,
                "Restaurant Adler",
                OffsetDateTime.now(),
                Set.of("kuechenchef@restaurant.de", "einkauf@restaurant.de"),
                "Product presentation and tasting session"
            );

            // Then
            assertThat(meeting.getChannel()).isEqualTo(Message.Channel.MEETING);
            assertThat(meeting.getLocation()).isEqualTo("Restaurant Adler");
            assertThat(meeting.getAttendees()).hasSize(2);
            assertThat(meeting.getBody()).contains("Product presentation");
            assertThat(meeting.getStatus()).isEqualTo(Message.Status.COMPLETED);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("Should reject empty message body")
        void shouldRejectEmptyMessageBody(String body) {
            // When/Then
            assertThatThrownBy(() -> {
                Message.createEmailMessage(
                    TEST_THREAD_ID,
                    TEST_SENDER,
                    "recipient@test.de",
                    "Subject",
                    body,
                    null
                ).validate();
            }).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("Message body cannot be empty");
        }

        @ParameterizedTest
        @ValueSource(strings = {"invalid-email", "@test.de", "test@", "test"})
        @DisplayName("Should validate email addresses")
        void shouldValidateEmailAddresses(String email) {
            // When/Then
            assertThatThrownBy(() -> {
                Message.createEmailMessage(
                    TEST_THREAD_ID,
                    email,
                    "recipient@test.de",
                    "Subject",
                    "Body",
                    null
                ).validateEmailAddresses();
            }).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("Invalid email address");
        }
    }

    @Nested
    @DisplayName("Message Status Management")
    class MessageStatusManagement {

        @Test
        @DisplayName("Should track email sending status")
        void shouldTrackEmailSendingStatus() {
            // Given
            message.setChannel(Message.Channel.EMAIL);
            message.setStatus(Message.Status.PENDING);

            // When
            message.markAsSending();

            // Then
            assertThat(message.getStatus()).isEqualTo(Message.Status.SENDING);
            assertThat(message.getSentAt()).isNull();

            // When
            message.markAsSent();

            // Then
            assertThat(message.getStatus()).isEqualTo(Message.Status.SENT);
            assertThat(message.getSentAt()).isNotNull();
        }

        @Test
        @DisplayName("Should handle email delivery failure")
        void shouldHandleEmailDeliveryFailure() {
            // Given
            message.setChannel(Message.Channel.EMAIL);
            message.setStatus(Message.Status.SENDING);

            // When
            message.markAsFailed("SMTP connection timeout", true);

            // Then
            assertThat(message.getStatus()).isEqualTo(Message.Status.FAILED);
            assertThat(message.getErrorMessage()).contains("SMTP connection timeout");
            assertThat(message.isRetryable()).isTrue();
            assertThat(message.getRetryCount()).isZero();
        }

        @Test
        @DisplayName("Should handle email bounce")
        void shouldHandleEmailBounce() {
            // Given
            message.setChannel(Message.Channel.EMAIL);
            message.setStatus(Message.Status.SENT);

            // When
            message.markAsBounced(Message.BounceType.HARD, "Mailbox does not exist");

            // Then
            assertThat(message.getStatus()).isEqualTo(Message.Status.BOUNCED);
            assertThat(message.getBounceType()).isEqualTo(Message.BounceType.HARD);
            assertThat(message.getBounceReason()).contains("Mailbox does not exist");
            assertThat(message.getBouncedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should track read status")
        void shouldTrackReadStatus() {
            // Given
            message.setStatus(Message.Status.SENT);

            // When
            message.markAsRead("customer@restaurant.de");

            // Then
            assertThat(message.isRead()).isTrue();
            assertThat(message.getReadBy()).isEqualTo("customer@restaurant.de");
            assertThat(message.getReadAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Email Retry Logic")
    class EmailRetryLogic {

        @Test
        @DisplayName("Should increment retry count")
        void shouldIncrementRetryCount() {
            // Given
            message.setChannel(Message.Channel.EMAIL);
            message.setStatus(Message.Status.FAILED);
            message.setRetryable(true);

            // When
            boolean canRetry = message.incrementRetryAndCheck();

            // Then
            assertThat(canRetry).isTrue();
            assertThat(message.getRetryCount()).isEqualTo(1);
            assertThat(message.getNextRetryAt()).isNotNull();
        }

        @Test
        @DisplayName("Should calculate exponential backoff for retries")
        void shouldCalculateExponentialBackoff() {
            // Given
            message.setChannel(Message.Channel.EMAIL);
            message.setRetryCount(0);

            // When
            OffsetDateTime retry1 = message.calculateNextRetryTime();
            message.setRetryCount(1);
            OffsetDateTime retry2 = message.calculateNextRetryTime();
            message.setRetryCount(2);
            OffsetDateTime retry3 = message.calculateNextRetryTime();

            // Then
            assertThat(retry1).isBefore(retry2);
            assertThat(retry2).isBefore(retry3);
            // Exponential: 1min, 2min, 4min, 8min...
        }

        @Test
        @DisplayName("Should stop retrying after max attempts")
        void shouldStopRetryingAfterMaxAttempts() {
            // Given
            message.setChannel(Message.Channel.EMAIL);
            message.setRetryCount(4); // Assuming MAX_RETRIES = 5

            // When
            boolean canRetry = message.incrementRetryAndCheck();

            // Then
            assertThat(canRetry).isTrue();
            assertThat(message.getRetryCount()).isEqualTo(5);

            // When - one more attempt
            boolean canRetryAgain = message.incrementRetryAndCheck();

            // Then
            assertThat(canRetryAgain).isFalse();
            assertThat(message.getStatus()).isEqualTo(Message.Status.FAILED_PERMANENT);
        }
    }

    @Nested
    @DisplayName("Message Content and Attachments")
    class MessageContent {

        @Test
        @DisplayName("Should handle plain text content")
        void shouldHandlePlainTextContent() {
            // Given
            String plainText = "This is a plain text message";

            // When
            message.setBody(plainText);
            message.setContentType("text/plain");

            // Then
            assertThat(message.getBody()).isEqualTo(plainText);
            assertThat(message.getContentType()).isEqualTo("text/plain");
            assertThat(message.isHtml()).isFalse();
        }

        @Test
        @DisplayName("Should handle HTML content")
        void shouldHandleHtmlContent() {
            // Given
            String html = "<p>This is an <strong>HTML</strong> message</p>";

            // When
            message.setBody(html);
            message.setContentType("text/html");

            // Then
            assertThat(message.getBody()).isEqualTo(html);
            assertThat(message.isHtml()).isTrue();
        }

        @Test
        @DisplayName("Should sanitize HTML content")
        void shouldSanitizeHtmlContent() {
            // Given
            String unsafeHtml = "<p>Text</p><script>alert('XSS')</script>";

            // When
            message.setBodyWithSanitization(unsafeHtml);

            // Then
            assertThat(message.getBody()).doesNotContain("<script>");
            assertThat(message.getBody()).contains("<p>Text</p>");
        }

        @Test
        @DisplayName("Should handle attachments")
        void shouldHandleAttachments() {
            // When
            message.addAttachment("product-catalog.pdf", "application/pdf", 2048576L);
            message.addAttachment("price-list.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                512000L);

            // Then
            assertThat(message.getAttachments()).hasSize(2);
            assertThat(message.getTotalAttachmentSize()).isEqualTo(2048576L + 512000L);
            assertThat(message.hasAttachments()).isTrue();
        }

        @Test
        @DisplayName("Should reject oversized attachments")
        void shouldRejectOversizedAttachments() {
            // When/Then
            assertThatThrownBy(() ->
                message.addAttachment("huge-file.zip", "application/zip", 26_214_400L) // 25MB
            ).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("Attachment size exceeds maximum");
        }
    }

    @Nested
    @DisplayName("B2B Food Specific Features")
    class B2BFoodFeatures {

        @Test
        @DisplayName("Should identify sample follow-up messages")
        void shouldIdentifySampleFollowUpMessages() {
            // Given
            message.setSubject("Sample Follow-up T+3");
            message.setBody("How did you like our Cook&Fresh products?");
            message.addTag("sample_followup");

            // When
            boolean isSampleFollowup = message.isSampleFollowUpMessage();

            // Then
            assertThat(isSampleFollowup).isTrue();
        }

        @Test
        @DisplayName("Should track multi-recipient for decision makers")
        void shouldTrackMultiRecipientForDecisionMakers() {
            // Given
            message.addRecipient("kuechenchef@restaurant.de", "Küchenchef");
            message.addRecipient("einkauf@restaurant.de", "Einkauf");
            message.addRecipient("geschaeftsfuehrer@restaurant.de", "Geschäftsführer");

            // When
            Set<String> decisionMakers = message.getDecisionMakerRecipients();

            // Then
            assertThat(decisionMakers).hasSize(3);
            assertThat(message.isMultiContactMessage()).isTrue();
        }

        @ParameterizedTest
        @CsvSource({
            "T+3, 3",
            "T+7, 7",
            "T+14, 14",
            "T+30, 30"
        })
        @DisplayName("Should parse follow-up timing from subject")
        void shouldParseFollowUpTimingFromSubject(String timing, int expectedDays) {
            // Given
            message.setSubject("Sample Follow-up " + timing);

            // When
            int days = message.getFollowUpDays();

            // Then
            assertThat(days).isEqualTo(expectedDays);
        }

        @Test
        @DisplayName("Should identify Cook&Fresh product mentions")
        void shouldIdentifyCookFreshProductMentions() {
            // Given
            message.setBody("We tested your Cook&Fresh Gulasch and Cook&Fresh Bolognese. " +
                          "The kitchen team was impressed with the 40-day shelf life.");

            // When
            List<String> products = message.extractMentionedProducts();

            // Then
            assertThat(products).contains("Gulasch", "Bolognese");
            assertThat(message.containsProductMention()).isTrue();
        }
    }

    @Nested
    @DisplayName("Message Threading and References")
    class MessageThreading {

        @Test
        @DisplayName("Should maintain email thread references")
        void shouldMaintainEmailThreadReferences() {
            // Given
            String messageId = "<123@freshfoodz.de>";
            String inReplyTo = "<456@restaurant.de>";

            // When
            message.setMessageId(messageId);
            message.setInReplyTo(inReplyTo);
            message.addReference("<789@restaurant.de>");
            message.addReference("<456@restaurant.de>");

            // Then
            assertThat(message.getMessageId()).isEqualTo(messageId);
            assertThat(message.getInReplyTo()).isEqualTo(inReplyTo);
            assertThat(message.getReferences()).hasSize(2);
        }

        @Test
        @DisplayName("Should identify reply messages")
        void shouldIdentifyReplyMessages() {
            // Given
            message.setInReplyTo("<original@restaurant.de>");
            message.setSubject("Re: Sample Order");

            // When
            boolean isReply = message.isReply();

            // Then
            assertThat(isReply).isTrue();
        }

        @Test
        @DisplayName("Should identify forwarded messages")
        void shouldIdentifyForwardedMessages() {
            // Given
            message.setSubject("Fwd: Special Offer Cook&Fresh");
            message.setBody("FYI - see below offer from FreshFoodz");

            // When
            boolean isForwarded = message.isForwarded();

            // Then
            assertThat(isForwarded).isTrue();
        }
    }

    @Nested
    @DisplayName("Message Search and Filtering")
    class MessageSearchFiltering {

        @Test
        @DisplayName("Should support full text search")
        void shouldSupportFullTextSearch() {
            // Given
            message.setSubject("Cook&Fresh Sample Delivery");
            message.setBody("Thank you for testing our Gulasch and Bolognese products");

            // When
            boolean matchesGulasch = message.matchesSearchTerm("Gulasch");
            boolean matchesPizza = message.matchesSearchTerm("Pizza");
            boolean matchesCookFresh = message.matchesSearchTerm("cook&fresh");

            // Then
            assertThat(matchesGulasch).isTrue();
            assertThat(matchesPizza).isFalse();
            assertThat(matchesCookFresh).isTrue(); // Case insensitive
        }

        @Test
        @DisplayName("Should filter by date range")
        void shouldFilterByDateRange() {
            // Given
            message.setCreatedAt(OffsetDateTime.now().minusDays(5));
            OffsetDateTime startDate = OffsetDateTime.now().minusDays(7);
            OffsetDateTime endDate = OffsetDateTime.now().minusDays(3);

            // When
            boolean inRange = message.isWithinDateRange(startDate, endDate);

            // Then
            assertThat(inRange).isTrue();
        }

        @Test
        @DisplayName("Should filter by channel")
        void shouldFilterByChannel() {
            // Given
            message.setChannel(Message.Channel.EMAIL);

            // When
            boolean matchesEmail = message.matchesChannel(Message.Channel.EMAIL);
            boolean matchesPhone = message.matchesChannel(Message.Channel.PHONE);

            // Then
            assertThat(matchesEmail).isTrue();
            assertThat(matchesPhone).isFalse();
        }
    }

    @Nested
    @DisplayName("Message Serialization")
    class MessageSerialization {

        @Test
        @DisplayName("Should serialize to JSON for API response")
        void shouldSerializeToJsonForApiResponse() {
            // Given
            message.setSubject("Test Subject");
            message.setBody("Test Body");
            message.setSender("sender@test.de");

            // When
            String json = message.toJson();

            // Then
            assertThat(json).contains("\"subject\":\"Test Subject\"");
            assertThat(json).contains("\"body\":\"Test Body\"");
            assertThat(json).contains("\"sender\":\"sender@test.de\"");
            assertThat(json).doesNotContain("password");
            assertThat(json).doesNotContain("secret");
        }

        @Test
        @DisplayName("Should implement equals and hashCode")
        void shouldImplementEqualsAndHashCode() {
            // Given
            Message msg1 = new Message();
            msg1.setId("123");
            Message msg2 = new Message();
            msg2.setId("123");
            Message msg3 = new Message();
            msg3.setId("456");

            // Then
            assertThat(msg1).isEqualTo(msg2);
            assertThat(msg1.hashCode()).isEqualTo(msg2.hashCode());
            assertThat(msg1).isNotEqualTo(msg3);
        }
    }
}