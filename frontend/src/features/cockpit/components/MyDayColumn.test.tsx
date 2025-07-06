/**
 * Tests für MyDayColumn Komponente
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MyDayColumn } from './MyDayColumn';
import { useCockpitStore } from '../../../store/cockpitStore';

// Mock für die Store hooks
const mockToggleTriageInbox = vi.fn();
const mockSetPriorityTasksCount = vi.fn();

vi.mock('../../../store/cockpitStore', () => ({
  useCockpitStore: vi.fn(() => ({
    showTriageInbox: false,
    toggleTriageInbox: mockToggleTriageInbox,
    setPriorityTasksCount: mockSetPriorityTasksCount,
  })),
}));

// Mock für die Daten
vi.mock('../data/mockData', () => ({
  mockTasks: [
    {
      id: '1',
      title: 'Test Aufgabe 1',
      type: 'call',
      customerName: 'Test GmbH',
      priority: 'high',
      completed: false,
    },
    {
      id: '2',
      title: 'Test Aufgabe 2',
      type: 'email',
      customerName: 'Test AG',
      priority: 'medium',
      completed: false,
    },
  ],
  mockTriageItems: [
    {
      id: 't1',
      from: 'test@example.com',
      subject: 'Test Anfrage',
      content: 'Test Inhalt',
      receivedAt: new Date('2025-01-06T10:00:00'),
      type: 'email',
      processed: false,
    },
  ],
}));

describe('MyDayColumn', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('sollte Loading State anzeigen', () => {
    render(<MyDayColumn />);

    expect(screen.getByText('', { selector: '.loading-spinner' })).toBeInTheDocument();
  });

  it('sollte nach dem Laden die Komponente anzeigen', async () => {
    render(<MyDayColumn />);

    await waitFor(() => {
      expect(screen.getByText('Mein Tag')).toBeInTheDocument();
    });
  });

  it('sollte KI-Empfehlung anzeigen', async () => {
    render(<MyDayColumn />);

    await waitFor(() => {
      expect(screen.getByText('Nächste beste Aktion')).toBeInTheDocument();
      expect(screen.getByText(/Müller GmbH/)).toBeInTheDocument();
    });
  });

  it('sollte Prioritäts-Aufgaben anzeigen', async () => {
    render(<MyDayColumn />);

    await waitFor(() => {
      expect(screen.getByText('Prioritäts-Aufgaben (2)')).toBeInTheDocument();
      expect(screen.getByText('Test Aufgabe 1')).toBeInTheDocument();
      expect(screen.getByText('Test Aufgabe 2')).toBeInTheDocument();
    });
  });

  it('sollte die richtigen Icons für Aufgabentypen anzeigen', async () => {
    render(<MyDayColumn />);

    await waitFor(() => {
      const taskIcons = screen.getAllByText((content, element) => {
        return element?.classList.contains('task-icon') || false;
      });

      expect(taskIcons[0]).toHaveTextContent('📞'); // call
      expect(taskIcons[1]).toHaveTextContent('✉️'); // email
    });
  });

  it('sollte Priority Tasks Count setzen', async () => {
    render(<MyDayColumn />);

    await waitFor(() => {
      expect(mockSetPriorityTasksCount).toHaveBeenCalledWith(1); // 1 high priority task
    });
  });

  it('sollte Triage-Inbox toggle funktionieren', async () => {
    render(<MyDayColumn />);

    await waitFor(() => {
      const toggleButton = screen.getByRole('button', {
        name: /triage-inbox anzeigen/i,
      });

      fireEvent.click(toggleButton);

      expect(mockToggleTriageInbox).toHaveBeenCalled();
    });
  });

  it('sollte Triage-Inbox Items anzeigen wenn offen', async () => {
    // Mock showTriageInbox = true
    (useCockpitStore as ReturnType<typeof vi.fn>).mockReturnValue({
      showTriageInbox: true,
      toggleTriageInbox: mockToggleTriageInbox,
      setPriorityTasksCount: mockSetPriorityTasksCount,
    });

    render(<MyDayColumn />);

    await waitFor(() => {
      expect(screen.getByText('Test Anfrage')).toBeInTheDocument();
      expect(screen.getByText('test@example.com')).toBeInTheDocument();
      expect(screen.getByText('Test Inhalt')).toBeInTheDocument();
    });
  });

  it('sollte Aktualisieren Button haben', async () => {
    render(<MyDayColumn />);

    await waitFor(() => {
      const refreshButton = screen.getByRole('button', {
        name: /aufgaben aktualisieren/i,
      });

      expect(refreshButton).toBeInTheDocument();
    });
  });

  it('sollte Triage Action Buttons anzeigen', async () => {
    (useCockpitStore as ReturnType<typeof vi.fn>).mockReturnValue({
      showTriageInbox: true,
      toggleTriageInbox: mockToggleTriageInbox,
      setPriorityTasksCount: mockSetPriorityTasksCount,
    });

    render(<MyDayColumn />);

    await waitFor(() => {
      expect(screen.getByText('Zuordnen')).toBeInTheDocument();
      expect(screen.getByText('Als Lead')).toBeInTheDocument();
    });
  });

  it('sollte korrekte CSS-Klassen für Prioritäten verwenden', async () => {
    render(<MyDayColumn />);

    await waitFor(() => {
      const highPriorityTask = screen.getByText('Test Aufgabe 1').closest('.task-item');
      const mediumPriorityTask = screen.getByText('Test Aufgabe 2').closest('.task-item');

      expect(highPriorityTask).toHaveClass('priority-high');
      expect(mediumPriorityTask).toHaveClass('priority-medium');
    });
  });
});
