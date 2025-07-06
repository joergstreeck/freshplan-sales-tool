/**
 * Tests für ActionCenterColumn Komponente
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import { ActionCenterColumn } from './ActionCenterColumn';
import { useCockpitStore } from '../../../store/cockpitStore';
import type { Customer } from '../types';

// Mock für Store
const mockSelectCustomer = vi.fn();
const mockSetActiveProcess = vi.fn();

const mockCustomer: Customer = {
  id: '123',
  companyName: 'Test GmbH',
  status: 'active'
};

vi.mock('../../../store/cockpitStore', () => ({
  useCockpitStore: vi.fn(() => ({
    selectedCustomer: null,
    activeProcess: null,
    setActiveProcess: mockSetActiveProcess,
    selectCustomer: mockSelectCustomer
  }))
}));

describe('ActionCenterColumn', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  describe('Ohne ausgewählten Kunden', () => {
    it('sollte Empty State anzeigen', () => {
      render(<ActionCenterColumn />);
      
      expect(screen.getByText('Aktions-Center')).toBeInTheDocument();
      expect(screen.getByText('Kein Kunde ausgewählt')).toBeInTheDocument();
      expect(screen.getByText(/Wählen Sie einen Kunden/)).toBeInTheDocument();
    });

    it('sollte das richtige Empty Icon anzeigen', () => {
      render(<ActionCenterColumn />);
      
      expect(screen.getByText('👈')).toBeInTheDocument();
    });
  });

  describe('Mit ausgewähltem Kunden', () => {
    beforeEach(() => {
      (useCockpitStore as ReturnType<typeof vi.fn>).mockReturnValue({
        selectedCustomer: mockCustomer,
        activeProcess: null,
        setActiveProcess: mockSetActiveProcess,
        selectCustomer: mockSelectCustomer
      });
    });

    it('sollte Kundeninformationen anzeigen', () => {
      render(<ActionCenterColumn />);
      
      expect(screen.getByText('Test GmbH')).toBeInTheDocument();
      expect(screen.getByText('active')).toBeInTheDocument();
    });

    it('sollte Close Button anzeigen', () => {
      render(<ActionCenterColumn />);
      
      const closeButton = screen.getByTitle('Kunde schließen');
      expect(closeButton).toBeInTheDocument();
    });

    it('sollte Kunde schließen bei Close Button Klick', () => {
      render(<ActionCenterColumn />);
      
      const closeButton = screen.getByTitle('Kunde schließen');
      fireEvent.click(closeButton);
      
      expect(mockSelectCustomer).toHaveBeenCalledWith(null);
    });

    it('sollte verfügbare Prozesse anzeigen', () => {
      render(<ActionCenterColumn />);
      
      expect(screen.getByText('Verfügbare Prozesse')).toBeInTheDocument();
      expect(screen.getByText('Neukunden-Akquise')).toBeInTheDocument();
      expect(screen.getByText('Angebot erstellen')).toBeInTheDocument();
      expect(screen.getByText('Nachfassen')).toBeInTheDocument();
      expect(screen.getByText('Vertragsverlängerung')).toBeInTheDocument();
    });

    it('sollte Prozess-Icons anzeigen', () => {
      render(<ActionCenterColumn />);
      
      const processSection = screen.getByText('Verfügbare Prozesse').parentElement;
      
      expect(processSection?.querySelector('.process-icon')?.textContent).toBe('🎯');
      expect(screen.getByText('📄')).toBeInTheDocument();
      expect(screen.getAllByText('📞')[0]).toBeInTheDocument(); // Erstes Vorkommen ist das Prozess-Icon
      expect(screen.getByText('🔄')).toBeInTheDocument();
    });

    it('sollte Prozess aktivieren bei Klick', () => {
      render(<ActionCenterColumn />);
      
      const processButton = screen.getByText('Neukunden-Akquise').closest('button');
      fireEvent.click(processButton!);
      
      expect(mockSetActiveProcess).toHaveBeenCalledWith('new-customer');
    });

    it('sollte Schnellaktionen anzeigen', () => {
      render(<ActionCenterColumn />);
      
      expect(screen.getByText('Schnellaktionen')).toBeInTheDocument();
      expect(screen.getByText('Anrufen')).toBeInTheDocument();
      expect(screen.getByText('E-Mail')).toBeInTheDocument();
      expect(screen.getByText('Notiz')).toBeInTheDocument();
    });

    it('sollte Timeline Preview anzeigen', () => {
      render(<ActionCenterColumn />);
      
      expect(screen.getByText('Letzte Aktivitäten')).toBeInTheDocument();
      expect(screen.getByText('E-Mail gesendet: Angebot Q1-2025')).toBeInTheDocument();
      expect(screen.getByText('Telefonat: Budget-Besprechung')).toBeInTheDocument();
    });

    it('sollte CSS-Klasse für Kundenstatus anwenden', () => {
      render(<ActionCenterColumn />);
      
      const statusElement = screen.getByText('active');
      expect(statusElement).toHaveClass('customer-status', 'status-active');
    });
  });

  describe('Mit aktivem Prozess', () => {
    beforeEach(() => {
      (useCockpitStore as ReturnType<typeof vi.fn>).mockReturnValue({
        selectedCustomer: mockCustomer,
        activeProcess: 'new-customer',
        setActiveProcess: mockSetActiveProcess,
        selectCustomer: mockSelectCustomer
      });
    });

    it('sollte aktiven Prozess anzeigen', () => {
      render(<ActionCenterColumn />);
      
      expect(screen.getByText('Neukunden-Akquise', { selector: '.process-title' })).toBeInTheDocument();
      expect(screen.getByText('Prozess beenden')).toBeInTheDocument();
    });

    it('sollte Prozess-Placeholder anzeigen', () => {
      render(<ActionCenterColumn />);
      
      expect(screen.getByText(/Hier werden die geführten Schritte/)).toBeInTheDocument();
      expect(screen.getByText(/Die detaillierte Prozess-Implementation/)).toBeInTheDocument();
    });

    it('sollte aktive Prozess-Karte hervorheben', () => {
      render(<ActionCenterColumn />);
      
      const processSection = screen.getByText('Verfügbare Prozesse').parentElement;
      const activeProcess = processSection?.querySelector('.process-card.active');
      expect(activeProcess).toBeInTheDocument();
      expect(activeProcess?.textContent).toContain('Neukunden-Akquise');
    });

    it('sollte Prozess beenden können', () => {
      render(<ActionCenterColumn />);
      
      const endButton = screen.getByText('Prozess beenden');
      fireEvent.click(endButton);
      
      expect(mockSetActiveProcess).toHaveBeenCalledWith(null);
    });
  });

  describe('Mit verschiedenen Kundenstatus', () => {
    it('sollte inactive Status korrekt anzeigen', () => {
      const inactiveCustomer = { ...mockCustomer, status: 'inactive' as const };
      (useCockpitStore as ReturnType<typeof vi.fn>).mockReturnValue({
        selectedCustomer: inactiveCustomer,
        activeProcess: null,
        setActiveProcess: mockSetActiveProcess,
        selectCustomer: mockSelectCustomer
      });

      render(<ActionCenterColumn />);
      
      const statusElement = screen.getByText('inactive');
      expect(statusElement).toHaveClass('status-inactive');
    });

    it('sollte prospect Status korrekt anzeigen', () => {
      const prospectCustomer = { ...mockCustomer, status: 'prospect' as const };
      (useCockpitStore as ReturnType<typeof vi.fn>).mockReturnValue({
        selectedCustomer: prospectCustomer,
        activeProcess: null,
        setActiveProcess: mockSetActiveProcess,
        selectCustomer: mockSelectCustomer
      });

      render(<ActionCenterColumn />);
      
      const statusElement = screen.getByText('prospect');
      expect(statusElement).toHaveClass('status-prospect');
    });
  });

  describe('Accessibility', () => {
    it('sollte alle Buttons mit title Attributen haben', () => {
      (useCockpitStore as ReturnType<typeof vi.fn>).mockReturnValue({
        selectedCustomer: mockCustomer,
        activeProcess: null,
        setActiveProcess: mockSetActiveProcess,
        selectCustomer: mockSelectCustomer
      });

      render(<ActionCenterColumn />);
      
      const closeButton = screen.getByTitle('Kunde schließen');
      expect(closeButton).toBeInTheDocument();
    });

    it('sollte semantisch korrekte Überschriften verwenden', () => {
      (useCockpitStore as ReturnType<typeof vi.fn>).mockReturnValue({
        selectedCustomer: mockCustomer,
        activeProcess: null,
        setActiveProcess: mockSetActiveProcess,
        selectCustomer: mockSelectCustomer
      });

      render(<ActionCenterColumn />);
      
      // h2 für Hauptüberschrift
      expect(screen.getByRole('heading', { level: 2, name: 'Aktions-Center' })).toBeInTheDocument();
      
      // h3 für Unterüberschriften
      expect(screen.getByRole('heading', { level: 3, name: 'Test GmbH' })).toBeInTheDocument();
      expect(screen.getByRole('heading', { level: 3, name: 'Verfügbare Prozesse' })).toBeInTheDocument();
    });
  });
});