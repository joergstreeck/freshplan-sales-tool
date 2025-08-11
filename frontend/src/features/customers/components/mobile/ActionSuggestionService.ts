/**
 * Action Suggestion Service
 * 
 * Intelligent action suggestions based on contact context and relationship data.
 * Part of FC-005 Contact Management UI - Mobile Actions.
 * 
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/MOBILE_CONTACT_ACTIONS.md
 */

import React from 'react';
import {
  Phone as PhoneIcon,
  Email as EmailIcon,
  WhatsApp as WhatsAppIcon,
  Cake as CakeIcon,
  Warning as WarningIcon,
  Event as EventIcon,
  TrendingUp as TrendingUpIcon,
  Sms as SmsIcon,
  Note as NoteIcon,
} from '@mui/icons-material';

import type { Contact } from '../../types/contact.types';
import type {
  QuickAction,
  SwipeActions,
  ContactIntelligence,
} from '../../types/mobileActions.types';
import { DEFAULT_SWIPE_ACTIONS, ACTION_COLORS } from '../../types/mobileActions.types';

export class ActionSuggestionService {
  /**
   * Generate context-based action suggestions
   * Prioritized by: Urgency → Warmth → Availability → History
   */
  getSuggestedActions(
    contact: Contact,
    intelligence?: ContactIntelligence
  ): QuickAction[] {
    const actions: QuickAction[] = [];

    // 1. URGENT: Birthday coming up
    if (this.isBirthdayUpcoming(contact.birthday)) {
      const daysUntil = this.getDaysUntilBirthday(contact.birthday);
      actions.push({
        id: 'birthday-call',
        type: 'call',
        label: `🎂 Geburtstag in ${daysUntil} Tagen`,
        icon: React.createElement(CakeIcon),
        color: '#E91E63',
        primary: true,
        urgency: 'high',
        enabled: !!contact.phone || !!contact.mobile,
      });
    }

    // 2. URGENT: Relationship cooling down
    if (intelligence?.freshnessLevel === 'critical') {
      actions.push({
        id: 'urgent-reconnect',
        type: 'call',
        label: '🔥 Dringend kontaktieren',
        icon: React.createElement(WarningIcon),
        color: '#FF5722',
        primary: true,
        urgency: 'high',
        enabled: !!contact.phone || !!contact.mobile,
      });
    }

    // 3. OPPORTUNITY: Warmth improving
    if (intelligence?.trendDirection === 'improving') {
      actions.push({
        id: 'momentum-follow',
        type: 'meeting',
        label: '📈 Momentum nutzen',
        icon: React.createElement(TrendingUpIcon),
        color: '#4CAF50',
        primary: true,
        urgency: 'medium',
        enabled: true,
      });
    }

    // 4. STANDARD: Based on available contact data
    if (contact.phone || contact.mobile) {
      const _phoneNumber = contact.mobile || contact.phone;
      actions.push({
        id: 'call-standard',
        type: 'call',
        label: 'Anrufen',
        icon: React.createElement(PhoneIcon),
        color: ACTION_COLORS.call,
        contextual: true,
        enabled: true,
      });

      // WhatsApp for mobile numbers
      if (contact.mobile) {
        actions.push({
          id: 'whatsapp',
          type: 'whatsapp',
          label: 'WhatsApp',
          icon: React.createElement(WhatsAppIcon),
          color: ACTION_COLORS.whatsapp,
          contextual: true,
          enabled: true,
        });

        actions.push({
          id: 'sms',
          type: 'sms',
          label: 'SMS',
          icon: React.createElement(SmsIcon),
          color: ACTION_COLORS.sms,
          contextual: true,
          enabled: true,
        });
      }
    }

    if (contact.email) {
      actions.push({
        id: 'email',
        type: 'email',
        label: 'E-Mail',
        icon: React.createElement(EmailIcon),
        color: ACTION_COLORS.email,
        contextual: true,
        enabled: true,
      });
    }

    // 5. ALWAYS AVAILABLE: Universal actions
    actions.push({
      id: 'schedule',
      type: 'calendar',
      label: 'Termin planen',
      icon: React.createElement(EventIcon),
      color: ACTION_COLORS.calendar,
      enabled: true,
    });

    actions.push({
      id: 'note',
      type: 'note',
      label: 'Notiz hinzufügen',
      icon: React.createElement(NoteIcon),
      color: ACTION_COLORS.note,
      enabled: true,
    });

    // Sort by priority
    return this.prioritizeActions(actions, contact, intelligence);
  }

  /**
   * Prioritize actions based on context
   */
  private prioritizeActions(
    actions: QuickAction[],
    contact: Contact,
    intelligence?: ContactIntelligence
  ): QuickAction[] {
    return actions.sort((a, b) => {
      // Urgency first
      const urgencyOrder = { high: 0, medium: 1, low: 2, undefined: 3 };
      const urgencyDiff =
        urgencyOrder[a.urgency || 'undefined'] -
        urgencyOrder[b.urgency || 'undefined'];
      if (urgencyDiff !== 0) return urgencyDiff;

      // Primary actions second
      if (a.primary && !b.primary) return -1;
      if (!a.primary && b.primary) return 1;

      // Preferred communication channel
      if (intelligence?.preferredChannel) {
        const preferred = intelligence.preferredChannel;
        if (a.type === preferred) return -1;
        if (b.type === preferred) return 1;
      }

      // Enabled actions before disabled
      if (a.enabled && !b.enabled) return -1;
      if (!a.enabled && b.enabled) return 1;

      return 0;
    });
  }

  /**
   * Generate optimal swipe actions based on context
   */
  getSwipeActions(
    contact: Contact,
    intelligence?: ContactIntelligence
  ): SwipeActions {
    const suggestions = this.getSuggestedActions(contact, intelligence);
    
    // Find best actions for left/right swipe
    const callAction = suggestions.find(
      (a) => a.type === 'call' && a.enabled
    );
    const emailAction = suggestions.find(
      (a) => a.type === 'email' && a.enabled
    );
    const whatsappAction = suggestions.find(
      (a) => a.type === 'whatsapp' && a.enabled
    );

    // Right swipe = Primary action (usually call)
    let rightAction: QuickAction | null = null;
    if (callAction && callAction.urgency === 'high') {
      rightAction = callAction;
    } else if (
      whatsappAction &&
      intelligence?.preferredChannel === 'whatsapp'
    ) {
      rightAction = whatsappAction;
    } else if (callAction) {
      rightAction = callAction;
    }

    // Left swipe = Secondary action (usually email)
    let leftAction: QuickAction | null = emailAction || whatsappAction || null;

    // Fallback to defaults if no suitable actions
    if (!rightAction && (contact.phone || contact.mobile)) {
      rightAction = {
        ...DEFAULT_SWIPE_ACTIONS.right!,
        icon: React.createElement(PhoneIcon),
        enabled: true,
      };
    }

    if (!leftAction && contact.email) {
      leftAction = {
        ...DEFAULT_SWIPE_ACTIONS.left!,
        icon: React.createElement(EmailIcon),
        enabled: true,
      };
    }

    return {
      left: leftAction,
      right: rightAction,
    };
  }

  /**
   * Check if birthday is upcoming (within 14 days)
   */
  private isBirthdayUpcoming(birthday?: string): boolean {
    if (!birthday) return false;

    const today = new Date();
    const birthdayDate = new Date(birthday);
    birthdayDate.setFullYear(today.getFullYear());

    // If birthday already passed this year, check next year
    if (birthdayDate < today) {
      birthdayDate.setFullYear(today.getFullYear() + 1);
    }

    const daysUntil = Math.ceil(
      (birthdayDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24)
    );

    return daysUntil >= 0 && daysUntil <= 14;
  }

  /**
   * Get days until birthday
   */
  private getDaysUntilBirthday(birthday?: string): number {
    if (!birthday) return -1;

    const today = new Date();
    const birthdayDate = new Date(birthday);
    birthdayDate.setFullYear(today.getFullYear());

    if (birthdayDate < today) {
      birthdayDate.setFullYear(today.getFullYear() + 1);
    }

    return Math.ceil(
      (birthdayDate.getTime() - today.getTime()) / (1000 * 60 * 60 * 24)
    );
  }

  /**
   * Get time-based greeting
   */
  getTimeBasedGreeting(): string {
    const hour = new Date().getHours();
    if (hour < 12) return 'Guten Morgen';
    if (hour < 17) return 'Guten Tag';
    return 'Guten Abend';
  }

  /**
   * Check if device is mobile
   */
  isMobileDevice(): boolean {
    return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(
      navigator.userAgent
    );
  }

  /**
   * Get device capabilities
   */
  getDeviceCapabilities() {
    return {
      hasPhone: this.isMobileDevice() || navigator.userAgent.includes('Mac'),
      hasSMS: this.isMobileDevice(),
      hasEmail: true,
      hasWhatsApp: true,
      hasCalendar: true,
      hasHaptic: 'vibrate' in navigator,
      isOnline: navigator.onLine,
      isMobile: this.isMobileDevice(),
    };
  }
}

// Singleton export
export const actionSuggestionService = new ActionSuggestionService();