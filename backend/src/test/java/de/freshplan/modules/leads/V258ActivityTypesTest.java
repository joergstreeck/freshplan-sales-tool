package de.freshplan.modules.leads;

import de.freshplan.modules.leads.domain.ActivityType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * V258 Migration Test: Validates new Activity-Types are available in Enum
 * Sprint 2.1.5 - Activity-Types Progress-Mapping
 */
class V258ActivityTypesTest {

    @Test
    void shouldHave13ActivityTypes() {
        ActivityType[] types = ActivityType.values();
        // 13 Types aus ACTIVITY_TYPES_PROGRESS_MAPPING.md + Legacy System Activities
        assertTrue(types.length >= 13, "Should have at least 13 activity types");
    }

    @Test
    void shouldHaveNewSystemActivities() {
        // Neue System-Activities aus Sprint 2.1.5
        assertNotNull(ActivityType.valueOf("FIRST_CONTACT_DOCUMENTED"));
        assertNotNull(ActivityType.valueOf("EMAIL_RECEIVED"));
        assertNotNull(ActivityType.valueOf("LEAD_ASSIGNED"));
    }

    @Test
    void firstContactDocumented_shouldNotResetTimer() {
        // FIRST_CONTACT_DOCUMENTED startet Schutz explizit via Service, NICHT via V257 Trigger
        ActivityType firstContact = ActivityType.FIRST_CONTACT_DOCUMENTED;
        assertFalse(firstContact.resetsTimer(), 
            "FIRST_CONTACT_DOCUMENTED should not reset timer (countsAsProgress=false)");
        assertTrue(firstContact.isMeaningfulContact(), 
            "FIRST_CONTACT_DOCUMENTED is a meaningful contact for protection start");
    }

    @Test
    void emailReceived_shouldNotResetTimer() {
        ActivityType emailReceived = ActivityType.EMAIL_RECEIVED;
        assertFalse(emailReceived.resetsTimer(), 
            "EMAIL_RECEIVED should not reset timer (countsAsProgress=false)");
        assertFalse(emailReceived.isMeaningfulContact(), 
            "EMAIL_RECEIVED is not a meaningful contact");
    }

    @Test
    void leadAssigned_shouldNotResetTimer() {
        ActivityType leadAssigned = ActivityType.LEAD_ASSIGNED;
        assertFalse(leadAssigned.resetsTimer(), 
            "LEAD_ASSIGNED should not reset timer (countsAsProgress=false)");
        assertFalse(leadAssigned.isMeaningfulContact(), 
            "LEAD_ASSIGNED is not a meaningful contact");
    }

    @Test
    void progressActivities_shouldResetTimer() {
        // 5 Progress Activities (countsAsProgress=true)
        assertTrue(ActivityType.QUALIFIED_CALL.resetsTimer());
        assertTrue(ActivityType.MEETING.resetsTimer());
        assertTrue(ActivityType.DEMO.resetsTimer());
        assertTrue(ActivityType.ROI_PRESENTATION.resetsTimer());
        assertTrue(ActivityType.SAMPLE_SENT.resetsTimer());
    }

    @Test
    void nonProgressActivities_shouldNotResetTimer() {
        // 5 Non-Progress Activities (countsAsProgress=false)
        assertFalse(ActivityType.NOTE.resetsTimer());
        assertFalse(ActivityType.FOLLOW_UP.resetsTimer());
        assertFalse(ActivityType.EMAIL.resetsTimer());
        assertFalse(ActivityType.CALL.resetsTimer());
        assertFalse(ActivityType.SAMPLE_FEEDBACK.resetsTimer());
    }
}
