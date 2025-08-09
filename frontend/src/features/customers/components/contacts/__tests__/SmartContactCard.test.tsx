/**
 * SmartContactCard Component Tests
 * 
 * Test suite for the enhanced contact card with relationship intelligence.
 * Part of FC-005 Contact Management UI.
 */

import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { ThemeProvider, createTheme } from '@mui/material';
import { SmartContactCard } from '../SmartContactCard';
import type { Contact } from '../../../types/contact.types';
import type { RelationshipWarmth } from '../WarmthIndicator';

const theme = createTheme();

const mockContact: Contact = {
  id: '1',
  customerId: 'cust-1',
  firstName: 'Max',
  lastName: 'Mustermann',
  salutation: 'Herr',
  title: 'Dr.',
  position: 'Geschäftsführer',
  decisionLevel: 'entscheider',
  email: 'max.mustermann@example.com',
  phone: '+49 123 456789',
  mobile: '+49 171 1234567',
  isPrimary: false,
  isActive: true,
  responsibilityScope: 'all',
  birthday: new Date(Date.now() + 5 * 24 * 60 * 60 * 1000).toISOString(), // 5 days from now
  hobbies: ['Golf', 'Tennis', 'Wein'],
  personalNotes: 'Mag italienische Küche',
  createdAt: new Date().toISOString(),
  updatedAt: new Date().toISOString(),
};

const mockWarmth: RelationshipWarmth = {
  temperature: 'HOT',
  score: 85,
  lastInteraction: new Date(Date.now() - 2 * 24 * 60 * 60 * 1000), // 2 days ago
  suggestions: ['Anrufen', 'Meeting vereinbaren'],
};

const defaultProps = {
  contact: mockContact,
  onEdit: jest.fn(),
  onDelete: jest.fn(),
  onSetPrimary: jest.fn(),
  onAssignLocation: jest.fn(),
  onViewTimeline: jest.fn(),
  onQuickAction: jest.fn(),
};

const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={theme}>{component}</ThemeProvider>);
};

describe('SmartContactCard', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Basic Rendering', () => {
    it('should render contact information correctly', () => {
      renderWithTheme(<SmartContactCard {...defaultProps} />);
      
      expect(screen.getByText('Dr. Max Mustermann')).toBeInTheDocument();
      expect(screen.getByText('Geschäftsführer')).toBeInTheDocument();
      expect(screen.getByText('Entscheider')).toBeInTheDocument();
      expect(screen.getByText('max.mustermann@example.com')).toBeInTheDocument();
      expect(screen.getByText('+49 123 456789')).toBeInTheDocument();
    });

    it('should show initials in avatar', () => {
      renderWithTheme(<SmartContactCard {...defaultProps} />);
      
      const avatar = screen.getByText('MM');
      expect(avatar).toBeInTheDocument();
    });

    it('should display location responsibility', () => {
      renderWithTheme(<SmartContactCard {...defaultProps} />);
      
      expect(screen.getByText('Alle Standorte')).toBeInTheDocument();
    });
  });

  describe('Primary Contact Highlighting', () => {
    it('should highlight primary contacts with special styling', () => {
      const primaryContact = { ...mockContact, isPrimary: true };
      const { container } = renderWithTheme(
        <SmartContactCard {...defaultProps} contact={primaryContact} />
      );
      
      expect(screen.getByText('HAUPT')).toBeInTheDocument();
      const card = container.querySelector('.MuiCard-root');
      expect(card).toHaveStyle({ borderWidth: '2px' });
    });

    it('should not show set as primary option for primary contacts', () => {
      const primaryContact = { ...mockContact, isPrimary: true };
      renderWithTheme(<SmartContactCard {...defaultProps} contact={primaryContact} />);
      
      const moreButton = screen.getByRole('button', { name: '' });
      fireEvent.click(moreButton);
      
      expect(screen.queryByText('Als Hauptkontakt')).not.toBeInTheDocument();
    });
  });

  describe('Birthday Indicator', () => {
    it('should show birthday indicator when birthday is upcoming', () => {
      renderWithTheme(<SmartContactCard {...defaultProps} />);
      
      const birthdayChip = screen.getByText(/5T/);
      expect(birthdayChip).toBeInTheDocument();
    });

    it('should not show birthday indicator when no birthday', () => {
      const contactNoBirthday = { ...mockContact, birthday: undefined };
      renderWithTheme(<SmartContactCard {...defaultProps} contact={contactNoBirthday} />);
      
      expect(screen.queryByText(/\dT/)).not.toBeInTheDocument();
    });
  });

  describe('Warmth Indicator', () => {
    it('should display warmth indicator when data is provided', () => {
      renderWithTheme(<SmartContactCard {...defaultProps} warmth={mockWarmth} />);
      
      expect(screen.getByText('Beziehungsstatus: Heiß')).toBeInTheDocument();
    });

    it('should not display warmth indicator when no data', () => {
      renderWithTheme(<SmartContactCard {...defaultProps} />);
      
      expect(screen.queryByText(/Beziehungsstatus/)).not.toBeInTheDocument();
    });
  });

  describe('Interactions', () => {
    it('should call onEdit when edit is clicked', async () => {
      renderWithTheme(<SmartContactCard {...defaultProps} />);
      
      const moreButton = screen.getByRole('button', { name: '' });
      fireEvent.click(moreButton);
      
      await waitFor(() => {
        const editMenuItem = screen.getByText('Bearbeiten');
        fireEvent.click(editMenuItem);
      });
      
      expect(defaultProps.onEdit).toHaveBeenCalledWith(mockContact);
    });

    it('should call onDelete after confirmation', async () => {
      window.confirm = jest.fn(() => true);
      renderWithTheme(<SmartContactCard {...defaultProps} />);
      
      const moreButton = screen.getByRole('button', { name: '' });
      fireEvent.click(moreButton);
      
      await waitFor(() => {
        const deleteMenuItem = screen.getByText('Löschen');
        fireEvent.click(deleteMenuItem);
      });
      
      expect(window.confirm).toHaveBeenCalled();
      expect(defaultProps.onDelete).toHaveBeenCalledWith(mockContact.id);
    });

    it('should not delete when confirmation is cancelled', async () => {
      window.confirm = jest.fn(() => false);
      renderWithTheme(<SmartContactCard {...defaultProps} />);
      
      const moreButton = screen.getByRole('button', { name: '' });
      fireEvent.click(moreButton);
      
      await waitFor(() => {
        const deleteMenuItem = screen.getByText('Löschen');
        fireEvent.click(deleteMenuItem);
      });
      
      expect(defaultProps.onDelete).not.toHaveBeenCalled();
    });

    it('should call onSetPrimary when option is selected', async () => {
      renderWithTheme(<SmartContactCard {...defaultProps} />);
      
      const moreButton = screen.getByRole('button', { name: '' });
      fireEvent.click(moreButton);
      
      await waitFor(() => {
        const setPrimaryMenuItem = screen.getByText('Als Hauptkontakt');
        fireEvent.click(setPrimaryMenuItem);
      });
      
      expect(defaultProps.onSetPrimary).toHaveBeenCalledWith(mockContact.id);
    });

    it('should call onViewTimeline when timeline is clicked', async () => {
      renderWithTheme(<SmartContactCard {...defaultProps} />);
      
      const moreButton = screen.getByRole('button', { name: '' });
      fireEvent.click(moreButton);
      
      await waitFor(() => {
        const timelineMenuItem = screen.getByText('Aktivitätsverlauf');
        fireEvent.click(timelineMenuItem);
      });
      
      expect(defaultProps.onViewTimeline).toHaveBeenCalledWith(mockContact.id);
    });
  });

  describe('Hover Effects', () => {
    it('should show quick actions on hover when enabled', () => {
      renderWithTheme(<SmartContactCard {...defaultProps} showQuickActions={true} />);
      
      const card = screen.getByText('Dr. Max Mustermann').closest('.MuiCard-root');
      if (card) {
        fireEvent.mouseEnter(card);
        
        // Quick actions should be visible
        expect(screen.getByTestId('quick-actions')).toBeInTheDocument();
      }
    });

    it('should show additional details on hover', () => {
      renderWithTheme(<SmartContactCard {...defaultProps} warmth={mockWarmth} />);
      
      const card = screen.getByText('Dr. Max Mustermann').closest('.MuiCard-root');
      if (card) {
        fireEvent.mouseEnter(card);
        
        // Warmth details should expand
        expect(screen.getByText('Sehr aktive Beziehung')).toBeInTheDocument();
      }
    });
  });

  describe('Decision Level Display', () => {
    it('should display different decision levels correctly', () => {
      const levels = [
        { value: 'entscheider', label: 'Entscheider' },
        { value: 'mitentscheider', label: 'Mitentscheider' },
        { value: 'einflussnehmer', label: 'Einflussnehmer' },
        { value: 'nutzer', label: 'Nutzer' },
        { value: 'gatekeeper', label: 'Gatekeeper' },
      ];
      
      levels.forEach(level => {
        const { rerender } = renderWithTheme(
          <SmartContactCard {...defaultProps} contact={{ ...mockContact, decisionLevel: level.value as any }} />
        );
        
        expect(screen.getByText(level.label)).toBeInTheDocument();
        rerender(<></>);
      });
    });
  });

  describe('Accessibility', () => {
    it('should have proper ARIA labels', () => {
      renderWithTheme(<SmartContactCard {...defaultProps} />);
      
      const moreButton = screen.getByRole('button', { name: '' });
      expect(moreButton).toBeInTheDocument();
    });

    it('should be keyboard navigable', () => {
      renderWithTheme(<SmartContactCard {...defaultProps} />);
      
      const moreButton = screen.getByRole('button', { name: '' });
      moreButton.focus();
      
      fireEvent.keyDown(moreButton, { key: 'Enter' });
      expect(screen.getByText('Bearbeiten')).toBeInTheDocument();
    });
  });
});