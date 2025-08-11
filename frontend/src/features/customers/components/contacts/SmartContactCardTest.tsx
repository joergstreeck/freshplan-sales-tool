/**
 * Test Component for SmartContactCard with MiniAuditTimeline
 *
 * This is a test page to verify the integration of the Mini Audit Timeline
 * into the SmartContactCard component.
 */

import React from 'react';
import { Box, Container, Typography, Grid } from '@mui/material';
import { SmartContactCard } from './SmartContactCard';
import type { Contact } from '../../types/contact.types';
import type { RelationshipWarmth } from './WarmthIndicator';

// Mock data for testing
const mockContact: Contact = {
  id: 'contact-123',
  customerId: 'customer-456',
  firstName: 'Max',
  lastName: 'Mustermann',
  salutation: 'Herr',
  title: 'Dr.',
  position: 'Geschäftsführer',
  email: 'max.mustermann@example.com',
  phone: '+49 123 456789',
  mobile: '+49 170 1234567',
  isPrimary: true,
  decisionLevel: 'entscheider',
  department: 'Geschäftsleitung',
  birthday: '1980-05-15',
  hobbies: ['Golf', 'Segeln', 'Lesen'],
  personalNotes: 'Mag italienisches Essen',
  responsibilityScope: 'all',
  assignedLocationIds: [],
  contactFrequency: 'weekly',
  preferredContactMethod: 'email',
  createdAt: new Date('2024-01-01'),
  updatedAt: new Date('2024-12-01'),
  createdBy: 'user-1',
  updatedBy: 'user-2',
};

const mockWarmth: RelationshipWarmth = {
  temperature: 'WARM',
  lastContact: new Date('2024-11-15'),
  daysSinceContact: 25,
  nextActionDate: new Date('2024-12-20'),
  interactionCount: 12,
  trend: 'stable',
};

export function SmartContactCardTest() {
  const handleEdit = (contact: Contact) => {
  };

  const handleDelete = (contactId: string) => {
  };

  const handleSetPrimary = (contactId: string) => {
  };

  const handleViewTimeline = (contactId: string) => {
  };

  const handleQuickAction = (action: string, contactId: string) => {
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Typography variant="h4" gutterBottom>
        SmartContactCard mit Mini Audit Timeline Test
      </Typography>

      <Typography variant="body1" color="text.secondary" paragraph>
        Diese Seite testet die Integration der Mini Audit Timeline in die SmartContactCard. Die
        Timeline sollte als Accordion am unteren Rand der Karte erscheinen.
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6} lg={4}>
          <Box>
            <Typography variant="h6" gutterBottom>
              Mit Audit Trail (für Admin/Manager/Auditor)
            </Typography>
            <SmartContactCard
              contact={mockContact}
              warmth={mockWarmth}
              onEdit={handleEdit}
              onDelete={handleDelete}
              onSetPrimary={handleSetPrimary}
              onViewTimeline={handleViewTimeline}
              onQuickAction={handleQuickAction}
              showAuditTrail={true}
              customerId="customer-456"
            />
          </Box>
        </Grid>

        <Grid item xs={12} md={6} lg={4}>
          <Box>
            <Typography variant="h6" gutterBottom>
              Ohne Audit Trail
            </Typography>
            <SmartContactCard
              contact={{ ...mockContact, id: 'contact-789', isPrimary: false }}
              warmth={{ ...mockWarmth, temperature: 'COLD' }}
              onEdit={handleEdit}
              onDelete={handleDelete}
              onSetPrimary={handleSetPrimary}
              onViewTimeline={handleViewTimeline}
              onQuickAction={handleQuickAction}
              showAuditTrail={false}
              customerId="customer-456"
            />
          </Box>
        </Grid>
      </Grid>

      <Box sx={{ mt: 4, p: 2, bgcolor: 'grey.100', borderRadius: 1 }}>
        <Typography variant="h6" gutterBottom>
          Feature-Beschreibung:
        </Typography>
        <Typography variant="body2" paragraph>
          <strong>✅ Implementiert:</strong>
        </Typography>
        <ul>
          <li>MiniAuditTimeline als Accordion am unteren Rand der SmartContactCard</li>
          <li>Collapsed: Zeigt "Zuletzt geändert vor X Tagen von Y"</li>
          <li>Expanded: Zeigt die letzten 5 Änderungen mit Icons und Farbcodierung</li>
          <li>Role-based Visibility: Nur für Admin, Manager und Auditor sichtbar</li>
          <li>Performance-optimiert mit React Query Caching (5 Minuten)</li>
          <li>"Vollständige Historie anzeigen" Link bei mehr als 5 Einträgen</li>
        </ul>

        <Typography variant="body2" paragraph sx={{ mt: 2 }}>
          <strong>🎨 Design-Highlights:</strong>
        </Typography>
        <ul>
          <li>Subtile Integration ohne die Karte zu überladen</li>
          <li>Progressive Disclosure: Details nur bei Bedarf</li>
          <li>Konsistente Farbcodierung: Grün (Create), Blau (Update), Rot (Delete)</li>
          <li>Responsive: Passt sich der Kartenbreite an</li>
        </ul>
      </Box>
    </Container>
  );
}
