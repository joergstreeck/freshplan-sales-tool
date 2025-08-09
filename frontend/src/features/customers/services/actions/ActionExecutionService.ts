/**
 * Action Execution Service
 * 
 * Handles execution of contact actions with offline support.
 * Part of FC-005 Contact Management UI - Mobile Actions.
 * 
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/MOBILE_CONTACT_ACTIONS.md
 */

import type { Contact } from '../../types/contact.types';
import type { QuickAction, ActionResult } from '../../types/mobileActions.types';
import { offlineQueueService } from './OfflineQueueService';
import { actionSuggestionService } from '../../components/mobile/ActionSuggestionService';

export class ActionExecutionService {
  /**
   * Execute a contact action
   */
  async executeAction(
    action: QuickAction,
    contact: Contact
  ): Promise<ActionResult> {
    const startTime = Date.now();
    
    try {
      // Track action for analytics
      this.trackAction(action, contact);

      // Execute based on action type
      switch (action.type) {
        case 'call':
          await this.initiateCall(contact);
          break;
        case 'email':
          await this.composeEmail(contact);
          break;
        case 'whatsapp':
          await this.openWhatsApp(contact);
          break;
        case 'sms':
          await this.composeSMS(contact);
          break;
        case 'calendar':
          await this.scheduleAppointment(contact);
          break;
        case 'note':
          await this.addQuickNote(contact);
          break;
        case 'meeting':
          await this.scheduleMeeting(contact);
          break;
        default:
          throw new Error(`Unknown action type: ${action.type}`);
      }

      // Record successful interaction
      await this.recordInteraction(contact.id, action.type, 'successful');

      return {
        success: true,
        actionId: action.id,
        timestamp: new Date(),
        message: `${action.label} erfolgreich ausgeführt`,
      };
    } catch (error) {
      // Queue for retry if offline
      if (!navigator.onLine) {
        await offlineQueueService.queueAction(action, contact);
        return {
          success: false,
          actionId: action.id,
          timestamp: new Date(),
          error: error as Error,
          requiresRetry: true,
          message: 'Aktion wird ausgeführt, sobald Sie online sind',
        };
      }

      return {
        success: false,
        actionId: action.id,
        timestamp: new Date(),
        error: error as Error,
        message: `Fehler: ${(error as Error).message}`,
      };
    }
  }

  /**
   * Initiate a phone call
   */
  private async initiateCall(contact: Contact): Promise<void> {
    const phoneNumber = contact.mobile || contact.phone;
    if (!phoneNumber) {
      throw new Error('Keine Telefonnummer verfügbar');
    }

    // Clean phone number
    const cleanNumber = phoneNumber.replace(/\s/g, '');

    if (actionSuggestionService.isMobileDevice()) {
      // Mobile: Direct call
      window.location.href = `tel:${cleanNumber}`;
    } else {
      // Desktop: Show call dialog or copy to clipboard
      await this.showCallDialog(contact, cleanNumber);
    }
  }

  /**
   * Compose an email
   */
  private async composeEmail(contact: Contact): Promise<void> {
    if (!contact.email) {
      throw new Error('Keine E-Mail-Adresse verfügbar');
    }

    const subject = this.generateEmailSubject(contact);
    const body = this.generateEmailBody(contact);

    const mailtoLink = `mailto:${contact.email}?subject=${encodeURIComponent(
      subject
    )}&body=${encodeURIComponent(body)}`;

    window.location.href = mailtoLink;
  }

  /**
   * Open WhatsApp chat
   */
  private async openWhatsApp(contact: Contact): Promise<void> {
    const phoneNumber = contact.mobile?.replace(/\D/g, '');
    if (!phoneNumber) {
      throw new Error('Keine Mobilnummer für WhatsApp verfügbar');
    }

    // Remove leading zeros and add country code if missing
    let formattedNumber = phoneNumber;
    if (formattedNumber.startsWith('0')) {
      formattedNumber = '49' + formattedNumber.substring(1); // Germany
    }

    const message = this.generateWhatsAppMessage(contact);
    const whatsappUrl = `https://wa.me/${formattedNumber}?text=${encodeURIComponent(
      message
    )}`;

    window.open(whatsappUrl, '_blank');
  }

  /**
   * Compose SMS message
   */
  private async composeSMS(contact: Contact): Promise<void> {
    const phoneNumber = contact.mobile;
    if (!phoneNumber) {
      throw new Error('Keine Mobilnummer für SMS verfügbar');
    }

    const message = this.generateSMSMessage(contact);
    const smsLink = `sms:${phoneNumber}?body=${encodeURIComponent(message)}`;

    window.location.href = smsLink;
  }

  /**
   * Schedule an appointment
   */
  private async scheduleAppointment(contact: Contact): Promise<void> {
    // Generate calendar event
    const event = {
      title: `Termin mit ${contact.firstName} ${contact.lastName}`,
      location: contact.position || '',
      details: `Termin mit ${this.getContactFullName(contact)}`,
    };

    // Use calendar API or open default calendar
    if (actionSuggestionService.isMobileDevice()) {
      // Mobile: Try to open calendar app
      const calendarUrl = this.generateCalendarUrl(event);
      window.open(calendarUrl, '_blank');
    } else {
      // Desktop: Download ICS file
      this.downloadICSFile(event, contact);
    }
  }

  /**
   * Add a quick note
   */
  private async addQuickNote(contact: Contact): Promise<void> {
    // This would typically open a modal or navigate to a note-taking interface
    // For now, we'll just log it
    console.log('Add note for contact:', contact.id);
    
    // In a real implementation, this would:
    // 1. Open a note dialog
    // 2. Save the note to the backend
    // 3. Update the contact's activity timeline
  }

  /**
   * Schedule a meeting
   */
  private async scheduleMeeting(contact: Contact): Promise<void> {
    // Similar to appointment but with meeting-specific logic
    await this.scheduleAppointment(contact);
  }

  /**
   * Generate email subject based on context
   */
  private generateEmailSubject(contact: Contact): string {
    const greeting = actionSuggestionService.getTimeBasedGreeting();
    
    // Check for special occasions
    if (this.isBirthdayToday(contact.birthday)) {
      return 'Herzlichen Glückwunsch zum Geburtstag! 🎂';
    }

    return `${greeting} - Kontaktaufnahme`;
  }

  /**
   * Generate email body template
   */
  private generateEmailBody(contact: Contact): string {
    const greeting = actionSuggestionService.getTimeBasedGreeting();
    const salutation = contact.salutation || '';
    const title = contact.title || '';

    return `${greeting} ${salutation} ${title} ${contact.lastName},

ich hoffe, es geht Ihnen gut!

[Ihr Anliegen hier]

Mit freundlichen Grüßen
${this.getCurrentUserName()}
FreshPlan Team

--
📱 Gesendet von FreshPlan Mobile`;
  }

  /**
   * Generate WhatsApp message template
   */
  private generateWhatsAppMessage(contact: Contact): string {
    const greeting = actionSuggestionService.getTimeBasedGreeting();
    return `${greeting} ${contact.firstName}, kurze Frage: Haben Sie gerade 5 Minuten Zeit? 😊`;
  }

  /**
   * Generate SMS message template
   */
  private generateSMSMessage(contact: Contact): string {
    return `Hallo ${contact.firstName}, kurze Info von FreshPlan:`;
  }

  /**
   * Generate calendar URL
   */
  private generateCalendarUrl(event: any): string {
    const startDate = new Date();
    startDate.setHours(startDate.getHours() + 1);
    const endDate = new Date(startDate);
    endDate.setHours(endDate.getHours() + 1);

    const params = new URLSearchParams({
      action: 'TEMPLATE',
      text: event.title,
      dates: `${this.formatDateForCalendar(startDate)}/${this.formatDateForCalendar(
        endDate
      )}`,
      location: event.location,
      details: event.details,
    });

    return `https://calendar.google.com/calendar/render?${params.toString()}`;
  }

  /**
   * Format date for calendar URL
   */
  private formatDateForCalendar(date: Date): string {
    return date.toISOString().replace(/[-:]/g, '').replace(/\.\d{3}/, '');
  }

  /**
   * Download ICS file for calendar
   */
  private downloadICSFile(event: any, contact: Contact): void {
    const startDate = new Date();
    startDate.setHours(startDate.getHours() + 1);
    const endDate = new Date(startDate);
    endDate.setHours(endDate.getHours() + 1);

    const icsContent = `BEGIN:VCALENDAR
VERSION:2.0
PRODID:-//FreshPlan//Contact Management//EN
BEGIN:VEVENT
UID:${Date.now()}@freshplan.de
DTSTAMP:${this.formatDateForICS(new Date())}
DTSTART:${this.formatDateForICS(startDate)}
DTEND:${this.formatDateForICS(endDate)}
SUMMARY:${event.title}
LOCATION:${event.location}
DESCRIPTION:${event.details}
END:VEVENT
END:VCALENDAR`;

    const blob = new Blob([icsContent], { type: 'text/calendar' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = `termin-${contact.lastName}.ics`;
    link.click();
    URL.revokeObjectURL(url);
  }

  /**
   * Format date for ICS file
   */
  private formatDateForICS(date: Date): string {
    return date.toISOString().replace(/[-:]/g, '').replace(/\.\d{3}/, '');
  }

  /**
   * Show call dialog for desktop
   */
  private async showCallDialog(contact: Contact, phoneNumber: string): Promise<void> {
    // Copy number to clipboard
    try {
      await navigator.clipboard.writeText(phoneNumber);
      alert(`Telefonnummer ${phoneNumber} wurde in die Zwischenablage kopiert`);
    } catch (error) {
      console.error('Could not copy phone number:', error);
      alert(`Telefonnummer: ${phoneNumber}`);
    }
  }

  /**
   * Record interaction in the system
   */
  private async recordInteraction(
    contactId: string,
    type: string,
    outcome: string
  ): Promise<void> {
    // This would typically call an API to record the interaction
    // For now, we'll just store it in localStorage
    const interactions = JSON.parse(
      localStorage.getItem('contact_interactions') || '[]'
    );
    
    interactions.push({
      contactId,
      type,
      outcome,
      timestamp: new Date().toISOString(),
      device: actionSuggestionService.isMobileDevice() ? 'mobile' : 'desktop',
    });

    localStorage.setItem('contact_interactions', JSON.stringify(interactions));
  }

  /**
   * Track action for analytics
   */
  private trackAction(action: QuickAction, contact: Contact): void {
    // Analytics tracking would go here
    console.log('Track action:', {
      actionType: action.type,
      contactId: contact.id,
      urgency: action.urgency,
      device: actionSuggestionService.isMobileDevice() ? 'mobile' : 'desktop',
    });
  }

  /**
   * Get current user name
   */
  private getCurrentUserName(): string {
    // This would typically come from auth context
    return 'Ihr Vertriebsteam';
  }

  /**
   * Check if today is birthday
   */
  private isBirthdayToday(birthday?: string): boolean {
    if (!birthday) return false;
    const today = new Date();
    const bday = new Date(birthday);
    return (
      today.getDate() === bday.getDate() && today.getMonth() === bday.getMonth()
    );
  }

  /**
   * Get full contact name
   */
  private getContactFullName(contact: Contact): string {
    const parts = [];
    if (contact.salutation) parts.push(contact.salutation);
    if (contact.title) parts.push(contact.title);
    parts.push(contact.firstName);
    parts.push(contact.lastName);
    return parts.join(' ');
  }
}

// Singleton export
export const actionExecutionService = new ActionExecutionService();