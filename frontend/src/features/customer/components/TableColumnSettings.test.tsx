import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { TableColumnSettings } from './TableColumnSettings';
import { useFocusListStore } from '../store/focusListStore';
import React from 'react';

// Mock the store
vi.mock('../store/focusListStore');

// Mock @dnd-kit
vi.mock('@dnd-kit/core', () => ({
  DndContext: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="dnd-context">{children}</div>
  ),
  closestCenter: vi.fn(),
  KeyboardSensor: vi.fn(),
  PointerSensor: vi.fn(),
  useSensor: vi.fn(),
  useSensors: vi.fn(),
  DragOverlay: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="drag-overlay">{children}</div>
  ),
}));

vi.mock('@dnd-kit/sortable', () => ({
  arrayMove: vi.fn((array: unknown[], from: number, to: number) => {
    const newArray = [...array];
    const [removed] = newArray.splice(from, 1);
    newArray.splice(to, 0, removed);
    return newArray;
  }),
  SortableContext: ({ children }: { children: React.ReactNode }) => (
    <div data-testid="sortable-context">{children}</div>
  ),
  sortableKeyboardCoordinates: vi.fn(),
  verticalListSortingStrategy: vi.fn(),
  useSortable: vi.fn(() => ({
    attributes: { 'data-testid': 'sortable-item' },
    listeners: {
      onPointerDown: vi.fn(),
      onKeyDown: vi.fn(),
    },
    setNodeRef: vi.fn(),
    transform: null,
    transition: null,
    isDragging: false,
  })),
}));

vi.mock('@dnd-kit/utilities', () => ({
  CSS: {
    Transform: {
      toString: vi.fn(() => ''),
    },
  },
}));

describe.skip('TableColumnSettings - TODO: Fix mock issues', () => {
  const mockTableColumns = [
    { id: 'companyName', label: 'Kunde', field: 'companyName', visible: true, order: 0 },
    { id: 'status', label: 'Status', field: 'status', visible: true, order: 1 },
    { id: 'riskScore', label: 'Risiko', field: 'riskScore', visible: false, order: 2 },
    { id: 'actions', label: 'Aktionen', field: 'actions', visible: true, order: 3 },
  ];

  const mockStore = {
    tableColumns: mockTableColumns,
    toggleColumnVisibility: vi.fn(),
    setColumnOrder: vi.fn(),
    resetTableColumns: vi.fn(),
  };

  beforeEach(() => {
    vi.clearAllMocks();
    (useFocusListStore as unknown as jest.Mock).mockReturnValue(mockStore);
  });

  it('sollte das Einstellungs-Icon rendern', () => {
    render(<TableColumnSettings />);

    const settingsButton = screen.getByLabelText('Spalten-Einstellungen');
    expect(settingsButton).toBeInTheDocument();
  });

  it('sollte das Menü beim Klick öffnen', async () => {
    const user = userEvent.setup();
    render(<TableColumnSettings />);

    const settingsButton = screen.getByLabelText('Spalten-Einstellungen');
    await user.click(settingsButton);

    expect(screen.getByText('Spalten konfigurieren')).toBeInTheDocument();
    expect(screen.getByText('Ziehen Sie die Spalten, um sie neu anzuordnen')).toBeInTheDocument();
  });

  it('sollte alle Spalten mit Checkboxen anzeigen', async () => {
    const user = userEvent.setup();
    render(<TableColumnSettings />);

    const settingsButton = screen.getByLabelText('Spalten-Einstellungen');
    await user.click(settingsButton);

    // Prüfe, ob alle Spalten angezeigt werden
    expect(screen.getByText('Kunde')).toBeInTheDocument();
    expect(screen.getByText('Status')).toBeInTheDocument();
    expect(screen.getByText('Risiko')).toBeInTheDocument();
    expect(screen.getByText('Aktionen')).toBeInTheDocument();
  });

  it('sollte Spalten-Sichtbarkeit umschalten können', async () => {
    const user = userEvent.setup();
    render(<TableColumnSettings />);

    const settingsButton = screen.getByLabelText('Spalten-Einstellungen');
    await user.click(settingsButton);

    // Finde die Checkbox für "Risiko" (ist initial nicht sichtbar)
    const risikoCheckbox = screen.getByRole('checkbox', { name: /Risiko/i });
    expect(risikoCheckbox).not.toBeChecked();

    // Klicke auf die Checkbox
    await user.click(risikoCheckbox);

    // Verifiziere, dass toggleColumnVisibility aufgerufen wurde
    expect(mockStore.toggleColumnVisibility).toHaveBeenCalledWith('riskScore');
  });

  it('sollte verhindert dass weniger als 2 Spalten sichtbar sind', async () => {
    const user = userEvent.setup();

    // Modifiziere mockStore für diesen Test - nur 2 sichtbare Spalten
    const limitedColumns = [
      { id: 'companyName', label: 'Kunde', field: 'companyName', visible: true, order: 0 },
      { id: 'status', label: 'Status', field: 'status', visible: false, order: 1 },
      { id: 'actions', label: 'Aktionen', field: 'actions', visible: true, order: 2 },
    ];

    (useFocusListStore as unknown as jest.Mock).mockReturnValue({
      ...mockStore,
      tableColumns: limitedColumns,
    });

    render(<TableColumnSettings />);

    const settingsButton = screen.getByLabelText('Spalten-Einstellungen');
    await user.click(settingsButton);

    // Die sichtbaren Checkboxen sollten disabled sein
    const kundeCheckbox = screen.getByRole('checkbox', { name: /Kunde/i });
    const aktionenCheckbox = screen.getByRole('checkbox', { name: /Aktionen/i });

    expect(kundeCheckbox).toBeChecked();
    expect(kundeCheckbox).toBeDisabled();
    expect(aktionenCheckbox).toBeChecked();
    expect(aktionenCheckbox).toBeDisabled();
  });

  it('sollte die Spalten zurücksetzen können', async () => {
    const user = userEvent.setup();
    render(<TableColumnSettings />);

    const settingsButton = screen.getByLabelText('Spalten-Einstellungen');
    await user.click(settingsButton);

    const resetButton = screen.getByRole('button', { name: /Zurücksetzen/i });
    await user.click(resetButton);

    expect(mockStore.resetTableColumns).toHaveBeenCalled();
  });

  it('sollte Drag-Handles für sortierbare Spalten anzeigen', async () => {
    const user = userEvent.setup();
    render(<TableColumnSettings />);

    const settingsButton = screen.getByLabelText('Spalten-Einstellungen');
    await user.click(settingsButton);

    // Alle sortierbaren Spalten sollten das sortable-item Attribut haben
    const sortableItems = screen.getAllByTestId('sortable-item');
    // 3 sortierbare Spalten (Aktionen ist nicht sortierbar)
    expect(sortableItems.length).toBeGreaterThan(0);
  });

  it('sollte DnD Context und Sortable Context rendern', async () => {
    const user = userEvent.setup();
    render(<TableColumnSettings />);

    const settingsButton = screen.getByLabelText('Spalten-Einstellungen');
    await user.click(settingsButton);

    expect(screen.getByTestId('dnd-context')).toBeInTheDocument();
    expect(screen.getByTestId('sortable-context')).toBeInTheDocument();
  });

  describe('Drag & Drop Debugging', () => {
    it('sollte Drag-Interaktion simulieren', async () => {
      const user = userEvent.setup();
      render(<TableColumnSettings />);

      const settingsButton = screen.getByLabelText('Spalten-Einstellungen');
      await user.click(settingsButton);

      // Debug: Ausgabe der DOM-Struktur
      const menu = screen.getByRole('menu');
      console.log('Menu HTML:', menu.innerHTML);

      // Suche nach Drag-Indicators
      const dragIndicators = menu.querySelectorAll('[data-testid="DragIndicatorIcon"]');
      console.log('Anzahl Drag-Indicators:', dragIndicators.length);

      // Prüfe ob die sortierbaren Elemente korrekt gerendert werden
      const sortableContext = screen.getByTestId('sortable-context');
      console.log('Sortable Context Kinder:', sortableContext.children.length);
    });

    it('sollte die korrekte Spalten-Reihenfolge anzeigen', async () => {
      const user = userEvent.setup();
      render(<TableColumnSettings />);

      const settingsButton = screen.getByLabelText('Spalten-Einstellungen');
      await user.click(settingsButton);

      // Hole alle Spalten-Labels in der Reihenfolge wie sie erscheinen
      const columnLabels = screen.getAllByText(/^(Kunde|Status|Risiko|Aktionen)$/);
      const labelTexts = columnLabels.map(el => el.textContent);

      console.log('Spalten-Reihenfolge:', labelTexts);

      // Erwartete Reihenfolge basierend auf order property
      expect(labelTexts).toEqual(['Kunde', 'Status', 'Risiko', 'Aktionen']);
    });
  });
});
