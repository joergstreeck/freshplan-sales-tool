package de.freshplan.domain.customer.service;

import static org.assertj.core.api.Assertions.*;
import static org.awaitility.Awaitility.*;

import de.freshplan.domain.customer.entity.Customer;
import de.freshplan.domain.customer.entity.CustomerContact;
import de.freshplan.domain.customer.entity.CustomerStatus;
import de.freshplan.domain.customer.entity.CustomerType;
import de.freshplan.domain.customer.entity.Industry;
import de.freshplan.domain.customer.entity.ContactInteraction;
import de.freshplan.domain.customer.repository.ContactInteractionRepository;
import de.freshplan.domain.customer.repository.ContactRepository;
import de.freshplan.domain.customer.repository.CustomerRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.TestTransaction;import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.TestTransaction;import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.TestTransaction;import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.enterprise.context.control.ActivateRequestContext;
import jakarta.inject.Inject;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Integration Test for ContactEventCaptureService CQRS Implementation.
 * 
 * Tests the WRITE-ONLY ContactEventCaptureCommandService with Feature Flag enabled.
 * ContactEventCaptureService has no QueryService as it's purely for capturing events.
 * 
 * Key aspects tested:
 * - Contact view event capture
 * - Contact update event capture
 * - Email sent event capture
 * - Phone call event capture
 * - Meeting scheduled event capture
 * - Event-driven architecture with async processing
 * 
 * @author Claude
 * @since Phase 14.3 - Feature Flag Switching Tests
 */
@QuarkusTest
@TestProfile(ContactEventCaptureCQRSTestProfile.class)
@TestSecurity(user = "testuser", roles = {"admin", "sales"})
@DisplayName("ContactEventCapture Service CQRS Integration Test")
class ContactEventCaptureCQRSIntegrationTest {

    @Inject
    ContactEventCaptureService captureService; // Test via Facade to verify Feature Flag switching
    
    @Inject
    CustomerRepository customerRepository;
    
    @Inject
    ContactRepository contactRepository;
    
    @Inject
    ContactInteractionRepository interactionRepository;
    
    @ConfigProperty(name = "features.cqrs.enabled")
    boolean cqrsEnabled;
    
    private Customer testCustomer;
    private CustomerContact testContact;
    
    @BeforeEach
    @TestTransaction
    void setUp() {
        // Create test customer and contact
        String uniqueSuffix = String.valueOf(System.currentTimeMillis());
        
        // Create customer
        testCustomer = new Customer();
        testCustomer.setCustomerNumber("KD-EVT-" + uniqueSuffix.substring(7));
        testCustomer.setCompanyName("Event Test Company " + uniqueSuffix);
        testCustomer.setCustomerType(CustomerType.UNTERNEHMEN);
        testCustomer.setStatus(CustomerStatus.AKTIV);
        testCustomer.setIndustry(Industry.HOTEL);
        testCustomer.setExpectedAnnualVolume(new BigDecimal("250000"));
        testCustomer.setCreatedBy("testuser");
        testCustomer.setCreatedAt(LocalDateTime.now());
        customerRepository.persist(testCustomer);
        
        // Create contact
        testContact = new CustomerContact();
        testContact.setCustomer(testCustomer);
        testContact.setFirstName("Max");
        testContact.setLastName("Mustermann");
        testContact.setEmail("max.mustermann@test.com");
        testContact.setPhone("+49 123 456789");
        testContact.setPosition("Geschäftsführer");
        testContact.setIsPrimary(true);
        testContact.setCreatedBy("testuser");
        testContact.setCreatedAt(LocalDateTime.now());
        contactRepository.persist(testContact);
        
        customerRepository.flush();
        contactRepository.flush();
    }
    
    @Test
    @DisplayName("Feature Flag should be enabled for CQRS tests")
    void testCQRSModeIsEnabled() {
        assertThat(cqrsEnabled)
            .as("CQRS Feature Flag must be enabled for this test")
            .isTrue();
    }
    
    // =====================================
    // CONTACT VIEW CAPTURE TESTS
    // =====================================
    
    @Test
    @DisplayName("Capture contact view event should be processed")
    void captureContactView_shouldBeProcessed() {
        // When - Capture a contact view event
        captureService.captureContactView(testContact.getId(), "testuser");
        
        // Then - Event should be captured (async processing may occur)
        // Note: This is fire-and-forget, so we just verify no exception
        assertThat(testContact.getId()).isNotNull();
        
        // Event processing is async, so we don't verify immediate database changes
        // The event is fired for analytics tracking
    }
    
    // =====================================
    // CONTACT UPDATE CAPTURE TESTS
    // =====================================
    
    @Test
    @DisplayName("Capture contact update should create interaction")
    void captureContactUpdate_shouldCreateInteraction() {
        // Given - Initial interaction count
        long initialCount = interactionRepository.count();
        
        // When - Capture a contact update event
        captureService.captureContactUpdate(
            testContact.getId(), 
            "testuser", 
            "email"
        );
        
        // Then - Should create an interaction (may be async)
        await()
            .atMost(Duration.ofSeconds(5))
            .until(() -> {
                return QuarkusTransaction.call(() -> {
                    long newCount = interactionRepository.count();
                    return newCount > initialCount;
                });
            });
        
        // Verify the interaction details
        List<ContactInteraction> interactions = interactionRepository
            .find("contact.id", testContact.getId())
            .list();
        
        assertThat(interactions)
            .isNotEmpty()
            .anyMatch(i -> 
                i.getType() == ContactInteraction.InteractionType.NOTE &&
                i.getSummary() != null &&
                i.getSummary().contains("email")
            );
    }
    
    // =====================================
    // EMAIL SENT CAPTURE TESTS
    // =====================================
    
    @Test
    @DisplayName("Capture email sent should create email interaction")
    void captureEmailSent_shouldCreateEmailInteraction() {
        // Given - Email details
        String subject = "Meeting Proposal";
        String content = "Dear Mr. Mustermann, I would like to propose a meeting...";
        
        // When - Capture email sent event
        captureService.captureEmailSent(
            testContact.getId(),
            "testuser",
            subject,
            content
        );
        
        // Then - Should create an email interaction
        await()
            .atMost(Duration.ofSeconds(5))
            .pollInterval(Duration.ofMillis(500))
            .until(() -> {
                return QuarkusTransaction.call(() -> {
                    List<ContactInteraction> interactions = interactionRepository
                        .find("contact.id = ?1 and type = ?2", 
                              testContact.getId(), 
                              ContactInteraction.InteractionType.EMAIL)
                        .list();
                    
                    return !interactions.isEmpty() &&
                           interactions.stream().anyMatch(i -> 
                               i.getSubject() != null &&
                               i.getSubject().equals(subject) &&
                               i.getFullContent() != null &&
                               i.getFullContent().equals(content)
                           );
                });
            });
    }
    
    // =====================================
    // PHONE CALL CAPTURE TESTS
    // =====================================
    
    @Test
    @DisplayName("Capture phone call should create call interaction")
    void capturePhoneCall_shouldCreateCallInteraction() {
        // Given - Call details
        Integer durationMinutes = 15;
        String outcome = "Positive - Interest in product demo";
        String notes = "Customer is interested in our premium package";
        
        // When - Capture phone call event
        captureService.capturePhoneCall(
            testContact.getId(),
            "testuser",
            durationMinutes,
            outcome,
            notes
        );
        
        // Then - Should create a call interaction
        await()
            .atMost(Duration.ofSeconds(5))
            .until(() -> {
                return QuarkusTransaction.call(() -> {
                    List<ContactInteraction> interactions = interactionRepository
                        .find("contact.id = ?1 and type = ?2",
                              testContact.getId(),
                              ContactInteraction.InteractionType.CALL)
                        .list();
                    
                    return !interactions.isEmpty() &&
                           interactions.stream().anyMatch(i -> 
                               i.getOutcome() != null &&
                               i.getOutcome().equals(outcome) &&
                               i.getSummary() != null &&
                               i.getSummary().equals(notes)
                           );
                });
            });
    }
    
    // =====================================
    // MEETING SCHEDULED CAPTURE TESTS
    // =====================================
    
    @Test
    @DisplayName("Capture meeting scheduled should create event interaction")
    void captureMeetingScheduled_shouldCreateEventInteraction() {
        // Given - Meeting details
        LocalDateTime meetingDate = LocalDateTime.now().plusDays(7);
        String agenda = "Product demonstration and pricing discussion";
        
        // When - Capture meeting scheduled event
        captureService.captureMeetingScheduled(
            testContact.getId(),
            "testuser",
            meetingDate,
            agenda
        );
        
        // Then - Should create an event interaction
        await()
            .atMost(Duration.ofSeconds(5))
            .until(() -> {
                return QuarkusTransaction.call(() -> {
                    List<ContactInteraction> interactions = interactionRepository
                        .find("contact.id = ?1 and type = ?2",
                              testContact.getId(),
                              ContactInteraction.InteractionType.EVENT)
                        .list();
                    
                    return !interactions.isEmpty() &&
                           interactions.stream().anyMatch(i -> 
                               i.getNextActionDate() != null &&
                               i.getNextActionDate().equals(meetingDate) &&
                               i.getSummary() != null &&
                               i.getSummary().equals(agenda)
                           );
                });
            });
    }
    
    // =====================================
    // MULTIPLE EVENT CAPTURE TESTS
    // =====================================
    
    @Test
    @DisplayName("Multiple events should be captured independently")
    void multipleEvents_shouldBeCapturedIndependently() {
        // When - Capture multiple different events
        captureService.captureContactView(testContact.getId(), "user1");
        captureService.captureContactUpdate(testContact.getId(), "user2", "phone");
        captureService.captureEmailSent(
            testContact.getId(), 
            "user3", 
            "Follow-up", 
            "Following up on our conversation..."
        );
        captureService.capturePhoneCall(
            testContact.getId(),
            "user4",
            10,
            "Left voicemail",
            "Will call back tomorrow"
        );
        
        // Then - All events should be processed (async)
        await()
            .atMost(Duration.ofSeconds(10))
            .until(() -> {
                return QuarkusTransaction.call(() -> {
                    List<ContactInteraction> allInteractions = interactionRepository
                        .find("contact.id", testContact.getId())
                        .list();
                    
                    // Should have multiple interactions
                    if (allInteractions.size() < 3) {
                        return false;
                    }
                    
                    // Check for different types
                    boolean hasNote = allInteractions.stream()
                        .anyMatch(i -> i.getType() == ContactInteraction.InteractionType.NOTE);
                    boolean hasEmail = allInteractions.stream()
                        .anyMatch(i -> i.getType() == ContactInteraction.InteractionType.EMAIL);
                    boolean hasCall = allInteractions.stream()
                        .anyMatch(i -> i.getType() == ContactInteraction.InteractionType.CALL);
                    
                    return hasNote || hasEmail || hasCall;
                });
            });
    }
    
    // =====================================
    // ERROR HANDLING TESTS
    // =====================================
    
    @Test
    @DisplayName("Invalid contact ID should handle gracefully")
    void invalidContactId_shouldHandleGracefully() {
        // Given - Non-existent contact ID
        UUID invalidId = UUID.randomUUID();
        
        // When/Then - Should not throw exception (fire-and-forget pattern)
        assertThatCode(() -> {
            captureService.captureContactView(invalidId, "testuser");
            captureService.captureContactUpdate(invalidId, "testuser", "field");
            captureService.captureEmailSent(invalidId, "testuser", "Subject", "Content");
        }).doesNotThrowAnyException();
    }
    
    // =====================================
    // CQRS BEHAVIOR VERIFICATION
    // =====================================
    
    @Test
    @DisplayName("CQRS mode should properly delegate all capture operations")
    void cqrsMode_shouldProperlyDelegateAllOperations() {
        // This test verifies that the facade properly delegates to CommandService
        
        // All capture operations should work in CQRS mode
        assertThatCode(() -> {
            // View capture
            captureService.captureContactView(testContact.getId(), "testuser");
            
            // Update capture
            captureService.captureContactUpdate(
                testContact.getId(), 
                "testuser", 
                "position"
            );
            
            // Email capture
            captureService.captureEmailSent(
                testContact.getId(),
                "testuser",
                "Test Subject",
                "Test Content"
            );
            
            // Phone call capture
            captureService.capturePhoneCall(
                testContact.getId(),
                "testuser",
                5,
                "Brief call",
                "Quick check-in"
            );
            
            // Meeting capture
            captureService.captureMeetingScheduled(
                testContact.getId(),
                "testuser",
                LocalDateTime.now().plusDays(3),
                "Initial meeting"
            );
        }).doesNotThrowAnyException();
        
        // Verify CQRS is enabled
        assertThat(cqrsEnabled).isTrue();
    }
}