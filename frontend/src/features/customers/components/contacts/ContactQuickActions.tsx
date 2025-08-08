/**
 * ContactQuickActions Component
 *
 * Mobile-optimized quick actions for contacts.
 * Implements swipe gestures and one-tap actions.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/sprint2/step3/MOBILE_CONTACT_ACTIONS.md
 */

import React from 'react';
import {
  Box,
  SpeedDial,
  SpeedDialAction,
  SpeedDialIcon,
  IconButton,
  Stack,
  Tooltip,
} from '@mui/material';
import {
  Phone as PhoneIcon,
  Email as EmailIcon,
  Message as MessageIcon,
  CalendarToday as CalendarIcon,
  Edit as EditIcon,
  ContentCopy as CopyIcon,
  WhatsApp as WhatsAppIcon,
} from '@mui/icons-material';

import type { Contact } from '../../types/contact.types';
import { getContactDisplayName } from '../../types/contact.types';

interface ContactQuickActionsProps {
  contact: Contact;
  variant?: 'inline' | 'speeddial';
  onEdit?: (contact: Contact) => void;
}

/**
 * Quick Actions for Mobile Contact Interaction
 *
 * Provides fast access to common contact actions
 * optimized for mobile usage.
 */
export const ContactQuickActions: React.FC<ContactQuickActionsProps> = ({
  contact,
  variant = 'inline',
  onEdit,
}) => {
  // Handle phone call
  const handleCall = (number: string) => {
    window.location.href = `tel:${number}`;
  };

  // Handle email
  const handleEmail = () => {
    if (contact.email) {
      window.location.href = `mailto:${contact.email}`;
    }
  };

  // Handle WhatsApp
  const handleWhatsApp = (number: string) => {
    // Remove non-numeric characters and add country code if needed
    const cleanNumber = number.replace(/\D/g, '');
    const whatsappNumber = cleanNumber.startsWith('49')
      ? cleanNumber
      : `49${cleanNumber.replace(/^0/, '')}`;
    window.open(`https://wa.me/${whatsappNumber}`, '_blank');
  };

  // Handle SMS
  const handleSMS = (number: string) => {
    window.location.href = `sms:${number}`;
  };

  // Copy to clipboard
  const handleCopy = (text: string, type: string) => {
    navigator.clipboard.writeText(text).then(() => {
      // Could show a toast here
      console.log(`${type} copied to clipboard`);
    });
  };

  // Create calendar event
  const handleCalendarEvent = () => {
    const subject = `Meeting mit ${getContactDisplayName(contact)}`;
    const details = `Kontakt: ${getContactDisplayName(contact)}${contact.position ? `\nPosition: ${contact.position}` : ''}${contact.email ? `\nEmail: ${contact.email}` : ''}${contact.phone ? `\nTelefon: ${contact.phone}` : ''}`;

    // Create calendar URL (works with most calendar apps)
    const calendarUrl = `data:text/calendar;charset=utf8,BEGIN:VCALENDAR
VERSION:2.0
BEGIN:VEVENT
SUMMARY:${subject}
DESCRIPTION:${details}
END:VEVENT
END:VCALENDAR`;

    const link = document.createElement('a');
    link.href = calendarUrl;
    link.download = `meeting-${contact.firstName}-${contact.lastName}.ics`;
    link.click();
  };

  const actions = [
    ...(contact.phone
      ? [
          {
            icon: <PhoneIcon />,
            name: 'Anrufen (Festnetz)',
            onClick: () => handleCall(contact.phone!),
          },
        ]
      : []),
    ...(contact.mobile
      ? [
          {
            icon: <PhoneIcon />,
            name: 'Anrufen (Mobil)',
            onClick: () => handleCall(contact.mobile!),
          },
          {
            icon: <WhatsAppIcon />,
            name: 'WhatsApp',
            onClick: () => handleWhatsApp(contact.mobile!),
          },
          {
            icon: <MessageIcon />,
            name: 'SMS',
            onClick: () => handleSMS(contact.mobile!),
          },
        ]
      : []),
    ...(contact.email
      ? [
          {
            icon: <EmailIcon />,
            name: 'E-Mail',
            onClick: handleEmail,
          },
        ]
      : []),
    {
      icon: <CalendarIcon />,
      name: 'Termin erstellen',
      onClick: handleCalendarEvent,
    },
    ...(onEdit
      ? [
          {
            icon: <EditIcon />,
            name: 'Bearbeiten',
            onClick: () => onEdit(contact),
          },
        ]
      : []),
  ];

  if (variant === 'speeddial') {
    return (
      <SpeedDial
        ariaLabel="Kontakt-Aktionen"
        sx={{ position: 'absolute', bottom: 16, right: 16 }}
        icon={<SpeedDialIcon />}
      >
        {actions.map(action => (
          <SpeedDialAction
            key={action.name}
            icon={action.icon}
            tooltipTitle={action.name}
            onClick={action.onClick}
          />
        ))}
      </SpeedDial>
    );
  }

  // Inline variant
  return (
    <Box>
      <Stack direction="row" spacing={1} justifyContent="center" flexWrap="wrap">
        {contact.mobile && (
          <>
            <Tooltip title="Mobil anrufen">
              <IconButton size="small" color="primary" onClick={() => handleCall(contact.mobile!)}>
                <PhoneIcon />
              </IconButton>
            </Tooltip>
            <Tooltip title="WhatsApp">
              <IconButton
                size="small"
                color="success"
                onClick={() => handleWhatsApp(contact.mobile!)}
              >
                <WhatsAppIcon />
              </IconButton>
            </Tooltip>
          </>
        )}

        {contact.phone && (
          <Tooltip title="Festnetz anrufen">
            <IconButton size="small" color="primary" onClick={() => handleCall(contact.phone!)}>
              <PhoneIcon />
            </IconButton>
          </Tooltip>
        )}

        {contact.email && (
          <Tooltip title="E-Mail senden">
            <IconButton size="small" color="primary" onClick={handleEmail}>
              <EmailIcon />
            </IconButton>
          </Tooltip>
        )}

        <Tooltip title="Termin erstellen">
          <IconButton size="small" onClick={handleCalendarEvent}>
            <CalendarIcon />
          </IconButton>
        </Tooltip>

        {/* Copy Actions */}
        {contact.email && (
          <Tooltip title="E-Mail kopieren">
            <IconButton size="small" onClick={() => handleCopy(contact.email!, 'E-Mail')}>
              <CopyIcon />
            </IconButton>
          </Tooltip>
        )}
      </Stack>
    </Box>
  );
};
