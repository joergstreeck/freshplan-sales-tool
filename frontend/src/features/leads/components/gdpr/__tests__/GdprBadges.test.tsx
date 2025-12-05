/**
 * GDPR Badge Components Tests - gemäß testing_guide.md
 * Sprint 2.1.8 - Phase 2: GDPR Frontend-Komponenten
 *
 * Fokus laut Guide:
 * 1. Business-Logic-Tests (PFLICHT) - DSGVO-Artikel korrekt angezeigt
 * 2. DTO-Completeness-Tests (EMPFOHLEN) - Alle Felder im Tooltip
 * 3. Edge-Case-Tests (WICHTIG) - NULL-Werte, compact mode
 *
 * @since 2025-12-05
 */

import { describe, it, expect } from 'vitest';
import { screen, render } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { ThemeProvider } from '@mui/material/styles';
import freshfoodzTheme from '@/theme/freshfoodz';
import { GdprDeletedBadge } from '../GdprDeletedBadge';
import { ContactBlockedBadge } from '../ContactBlockedBadge';

// =============================================================================
// Render Helper
// =============================================================================

const renderWithTheme = (component: React.ReactElement) => {
  return render(<ThemeProvider theme={freshfoodzTheme}>{component}</ThemeProvider>);
};

// =============================================================================
// TESTS: GdprDeletedBadge - Art. 17 (Löschrecht)
// =============================================================================

describe('GdprDeletedBadge - Art. 17 Löschrecht', () => {
  describe('Business Logic', () => {
    /**
     * BUSINESS RULE: Badge muss DSGVO-Artikel 17 korrekt referenzieren
     */
    it('zeigt DSGVO-anonymisiert Label', () => {
      renderWithTheme(<GdprDeletedBadge />);

      expect(screen.getByText('DSGVO-anonymisiert')).toBeInTheDocument();
    });

    it('zeigt Art. 17 Referenz im Tooltip', async () => {
      const user = userEvent.setup();

      renderWithTheme(
        <GdprDeletedBadge gdprDeletedAt="2025-12-01T10:30:00" gdprDeletedBy="admin@freshfoodz.de" />
      );

      // Hover über Badge um Tooltip zu sehen
      await user.hover(screen.getByText('DSGVO-anonymisiert'));

      // Tooltip sollte Art. 17 erwähnen
      expect(await screen.findByText(/Art\. 17/)).toBeInTheDocument();
    });
  });

  describe('DTO Completeness', () => {
    /**
     * WICHTIG: Alle übergebenen Felder müssen im Tooltip erscheinen
     */
    it('zeigt alle Felder im Tooltip', async () => {
      const user = userEvent.setup();

      renderWithTheme(
        <GdprDeletedBadge
          gdprDeletedAt="2025-12-01T10:30:00"
          gdprDeletedBy="admin@freshfoodz.de"
          gdprDeletionReason="Kundenanfrage gem. Art. 17"
        />
      );

      await user.hover(screen.getByText('DSGVO-anonymisiert'));

      // Datum (formatiert)
      expect(await screen.findByText(/01\.12\.2025/)).toBeInTheDocument();
      // Von
      expect(screen.getByText(/admin@freshfoodz\.de/)).toBeInTheDocument();
      // Grund
      expect(screen.getByText(/Kundenanfrage gem\. Art\. 17/)).toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('zeigt "Unbekannt" wenn gdprDeletedAt fehlt', async () => {
      const user = userEvent.setup();

      renderWithTheme(<GdprDeletedBadge />);

      await user.hover(screen.getByText('DSGVO-anonymisiert'));

      expect(await screen.findByText(/Unbekannt/)).toBeInTheDocument();
    });

    it('zeigt compact mode nur als Icon', () => {
      renderWithTheme(<GdprDeletedBadge compact={true} />);

      // Kein Text-Label im compact mode
      expect(screen.queryByText('DSGVO-anonymisiert')).not.toBeInTheDocument();
      // Aber Icon sollte existieren
      expect(screen.getByTestId('DeleteForeverIcon')).toBeInTheDocument();
    });

    it('versteckt gdprDeletedBy wenn nicht übergeben', async () => {
      const user = userEvent.setup();

      renderWithTheme(<GdprDeletedBadge gdprDeletedAt="2025-12-01T10:30:00" />);

      await user.hover(screen.getByText('DSGVO-anonymisiert'));

      // "Von:" sollte nicht erscheinen
      expect(screen.queryByText(/Von:/)).not.toBeInTheDocument();
    });
  });
});

// =============================================================================
// TESTS: ContactBlockedBadge - Art. 7.3 (Widerruf)
// =============================================================================

describe('ContactBlockedBadge - Art. 7.3 Widerruf', () => {
  describe('Business Logic', () => {
    /**
     * BUSINESS RULE: Badge muss DSGVO-Artikel 7.3 korrekt referenzieren
     */
    it('zeigt Kontakt gesperrt Label', () => {
      renderWithTheme(<ContactBlockedBadge />);

      expect(screen.getByText('Kontakt gesperrt')).toBeInTheDocument();
    });

    it('zeigt Art. 7.3 Referenz im Tooltip', async () => {
      const user = userEvent.setup();

      renderWithTheme(
        <ContactBlockedBadge
          consentRevokedAt="2025-12-01T10:30:00"
          consentRevokedBy="sales@freshfoodz.de"
        />
      );

      await user.hover(screen.getByText('Kontakt gesperrt'));

      expect(await screen.findByText(/Art\. 7\.3/)).toBeInTheDocument();
    });
  });

  describe('DTO Completeness', () => {
    it('zeigt alle Felder im Tooltip', async () => {
      const user = userEvent.setup();

      renderWithTheme(
        <ContactBlockedBadge
          consentRevokedAt="2025-12-02T14:15:00"
          consentRevokedBy="manager@freshfoodz.de"
        />
      );

      await user.hover(screen.getByText('Kontakt gesperrt'));

      // Datum (formatiert)
      expect(await screen.findByText(/02\.12\.2025/)).toBeInTheDocument();
      // Von
      expect(screen.getByText(/manager@freshfoodz\.de/)).toBeInTheDocument();
    });
  });

  describe('Edge Cases', () => {
    it('zeigt "Unbekannt" wenn consentRevokedAt fehlt', async () => {
      const user = userEvent.setup();

      renderWithTheme(<ContactBlockedBadge />);

      await user.hover(screen.getByText('Kontakt gesperrt'));

      expect(await screen.findByText(/Unbekannt/)).toBeInTheDocument();
    });

    it('zeigt compact mode nur als Icon', () => {
      renderWithTheme(<ContactBlockedBadge compact={true} />);

      // Kein Text-Label im compact mode
      expect(screen.queryByText('Kontakt gesperrt')).not.toBeInTheDocument();
      // Aber Icon sollte existieren
      expect(screen.getByTestId('BlockIcon')).toBeInTheDocument();
    });

    it('versteckt consentRevokedBy wenn nicht übergeben', async () => {
      const user = userEvent.setup();

      renderWithTheme(<ContactBlockedBadge consentRevokedAt="2025-12-01T10:30:00" />);

      await user.hover(screen.getByText('Kontakt gesperrt'));

      // "Von:" sollte nicht erscheinen
      expect(screen.queryByText(/Von:/)).not.toBeInTheDocument();
    });
  });
});
