/**
 * Tests für den Cockpit Zustand Store
 */

import { describe, it, expect, beforeEach } from 'vitest';
import { renderHook, act } from '@testing-library/react';
import { useCockpitStore } from './cockpitStore';

describe('cockpitStore', () => {
  beforeEach(() => {
    // Reset store vor jedem Test
    const { setState } = useCockpitStore;
    setState({
      activeColumn: 'focus-list',
      viewMode: 'list',
      selectedCustomer: null,
      showTriageInbox: false,
      priorityTasksCount: 0,
      filterTags: [],
      searchQuery: '',
      isMobileMenuOpen: false,
      isCompactMode: false,
      activeProcess: null,
    });
  });

  describe('Column Navigation', () => {
    it('sollte die aktive Spalte setzen', () => {
      const { result } = renderHook(() => useCockpitStore());

      expect(result.current.activeColumn).toBe('focus-list');

      act(() => {
        result.current.setActiveColumn('my-day');
      });

      expect(result.current.activeColumn).toBe('my-day');
    });

    it('sollte zwischen allen drei Spalten wechseln können', () => {
      const { result } = renderHook(() => useCockpitStore());

      const columns = ['my-day', 'focus-list', 'action-center'] as const;

      columns.forEach(column => {
        act(() => {
          result.current.setActiveColumn(column);
        });
        expect(result.current.activeColumn).toBe(column);
      });
    });
  });

  describe('View Mode', () => {
    it('sollte den View Mode ändern', () => {
      const { result } = renderHook(() => useCockpitStore());

      expect(result.current.viewMode).toBe('list');

      act(() => {
        result.current.setViewMode('cards');
      });

      expect(result.current.viewMode).toBe('cards');

      act(() => {
        result.current.setViewMode('kanban');
      });

      expect(result.current.viewMode).toBe('kanban');
    });
  });

  describe('Customer Selection', () => {
    it('sollte einen Kunden auswählen', () => {
      const { result } = renderHook(() => useCockpitStore());

      const customer = {
        id: '123',
        companyName: 'Test GmbH',
        status: 'active' as const,
      };

      act(() => {
        result.current.selectCustomer(customer);
      });

      expect(result.current.selectedCustomer).toEqual(customer);
      expect(result.current.activeColumn).toBe('action-center');
    });

    it('sollte Kunden abwählen', () => {
      const { result } = renderHook(() => useCockpitStore());

      const customer = {
        id: '123',
        companyName: 'Test GmbH',
        status: 'active' as const,
      };

      act(() => {
        result.current.selectCustomer(customer);
      });

      expect(result.current.selectedCustomer).toEqual(customer);

      act(() => {
        result.current.selectCustomer(null);
      });

      expect(result.current.selectedCustomer).toBeNull();
    });
  });

  describe('Filter Management', () => {
    it('sollte Filter Tags hinzufügen', () => {
      const { result } = renderHook(() => useCockpitStore());

      act(() => {
        result.current.addFilterTag('status:active');
      });

      expect(result.current.filterTags).toContain('status:active');

      act(() => {
        result.current.addFilterTag('priority:high');
      });

      expect(result.current.filterTags).toHaveLength(2);
      expect(result.current.filterTags).toContain('priority:high');
    });

    it('sollte keine doppelten Filter Tags hinzufügen', () => {
      const { result } = renderHook(() => useCockpitStore());

      act(() => {
        result.current.addFilterTag('status:active');
        result.current.addFilterTag('status:active');
      });

      expect(result.current.filterTags).toHaveLength(1);
    });

    it('sollte Filter Tags entfernen', () => {
      const { result } = renderHook(() => useCockpitStore());

      act(() => {
        result.current.addFilterTag('status:active');
        result.current.addFilterTag('priority:high');
      });

      expect(result.current.filterTags).toHaveLength(2);

      act(() => {
        result.current.removeFilterTag('status:active');
      });

      expect(result.current.filterTags).toHaveLength(1);
      expect(result.current.filterTags).not.toContain('status:active');
    });

    it('sollte alle Filter löschen', () => {
      const { result } = renderHook(() => useCockpitStore());

      act(() => {
        result.current.addFilterTag('status:active');
        result.current.addFilterTag('priority:high');
        result.current.clearFilterTags();
      });

      expect(result.current.filterTags).toHaveLength(0);
    });

    it('sollte Filter Tags als Array setzen', () => {
      const { result } = renderHook(() => useCockpitStore());

      const newTags = ['aktiv', 'neu', 'lead'];

      act(() => {
        result.current.setFilterTags(newTags);
      });

      expect(result.current.filterTags).toEqual(newTags);
      expect(result.current.filterTags).toHaveLength(3);
    });
  });

  describe('Search', () => {
    it('sollte Search Query setzen', () => {
      const { result } = renderHook(() => useCockpitStore());

      act(() => {
        result.current.setSearchQuery('Test GmbH');
      });

      expect(result.current.searchQuery).toBe('Test GmbH');
    });
  });

  describe('UI States', () => {
    it('sollte Triage Inbox togglen', () => {
      const { result } = renderHook(() => useCockpitStore());

      expect(result.current.showTriageInbox).toBe(false);

      act(() => {
        result.current.toggleTriageInbox();
      });

      expect(result.current.showTriageInbox).toBe(true);

      act(() => {
        result.current.toggleTriageInbox();
      });

      expect(result.current.showTriageInbox).toBe(false);
    });

    it('sollte Mobile Menu togglen', () => {
      const { result } = renderHook(() => useCockpitStore());

      expect(result.current.isMobileMenuOpen).toBe(false);

      act(() => {
        result.current.toggleMobileMenu();
      });

      expect(result.current.isMobileMenuOpen).toBe(true);
    });

    it('sollte Compact Mode togglen', () => {
      const { result } = renderHook(() => useCockpitStore());

      expect(result.current.isCompactMode).toBe(false);

      act(() => {
        result.current.toggleCompactMode();
      });

      expect(result.current.isCompactMode).toBe(true);
    });
  });

  describe('Process Management', () => {
    it('sollte aktiven Prozess setzen', () => {
      const { result } = renderHook(() => useCockpitStore());

      act(() => {
        result.current.setActiveProcess('new-customer');
      });

      expect(result.current.activeProcess).toBe('new-customer');

      act(() => {
        result.current.setActiveProcess(null);
      });

      expect(result.current.activeProcess).toBeNull();
    });
  });

  describe('Priority Tasks', () => {
    it('sollte Priority Tasks Count setzen', () => {
      const { result } = renderHook(() => useCockpitStore());

      act(() => {
        result.current.setPriorityTasksCount(5);
      });

      expect(result.current.priorityTasksCount).toBe(5);
    });
  });
});
