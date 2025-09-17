import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { renderHook } from '@testing-library/react';
import { useKeyboardNavigation } from '../useKeyboardNavigation';

// Mock react-router-dom
const mockNavigate = vi.fn();
vi.mock('react-router-dom', () => ({
  useNavigate: () => mockNavigate,
}));

// Mock navigation store
const mockToggleSidebar = vi.fn();
const mockExpandedMenus = new Set<string>();
const mockSetExpandedMenus = vi.fn();

vi.mock('@/store/navigationStore', () => ({
  useNavigationStore: () => ({
    toggleSidebar: mockToggleSidebar,
    expandedMenus: mockExpandedMenus,
    setExpandedMenus: mockSetExpandedMenus,
  }),
}));

describe('useKeyboardNavigation', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  afterEach(() => {
    // Remove all event listeners
    const events = ['keydown'];
    events.forEach(event => {
      window.removeEventListener(event, () => {});
    });
  });

  it('should toggle sidebar with Ctrl+B', () => {
    renderHook(() => useKeyboardNavigation());

    const event = new KeyboardEvent('keydown', {
      key: 'b',
      ctrlKey: true,
    });
    window.dispatchEvent(event);

    expect(mockToggleSidebar).toHaveBeenCalled();
  });

  it('should navigate to dashboard with Alt+H', () => {
    renderHook(() => useKeyboardNavigation());

    const event = new KeyboardEvent('keydown', {
      key: 'h',
      altKey: true,
    });
    window.dispatchEvent(event);

    expect(mockNavigate).toHaveBeenCalledWith('/cockpit');
  });

  it('should navigate to customers with Alt+K', () => {
    renderHook(() => useKeyboardNavigation());

    const event = new KeyboardEvent('keydown', {
      key: 'k',
      altKey: true,
    });
    window.dispatchEvent(event);

    expect(mockNavigate).toHaveBeenCalledWith('/customers');
  });

  it('should navigate to orders with Alt+B', () => {
    renderHook(() => useKeyboardNavigation());

    const event = new KeyboardEvent('keydown', {
      key: 'b',
      altKey: true,
    });
    window.dispatchEvent(event);

    expect(mockNavigate).toHaveBeenCalledWith('/orders');
  });

  it('should navigate to calculator with Alt+R', () => {
    renderHook(() => useKeyboardNavigation());

    const event = new KeyboardEvent('keydown', {
      key: 'r',
      altKey: true,
    });
    window.dispatchEvent(event);

    expect(mockNavigate).toHaveBeenCalledWith('/calculator');
  });

  it('should not trigger when typing in input field', () => {
    renderHook(() => useKeyboardNavigation());

    const input = document.createElement('input');
    document.body.appendChild(input);
    input.focus();

    const event = new KeyboardEvent('keydown', {
      key: 'b',
      ctrlKey: true,
      bubbles: true,
    });
    input.dispatchEvent(event);

    expect(mockToggleSidebar).not.toHaveBeenCalled();

    document.body.removeChild(input);
  });

  it('should not trigger when typing in textarea', () => {
    renderHook(() => useKeyboardNavigation());

    const textarea = document.createElement('textarea');
    document.body.appendChild(textarea);
    textarea.focus();

    const event = new KeyboardEvent('keydown', {
      key: 'h',
      altKey: true,
      bubbles: true,
    });
    textarea.dispatchEvent(event);

    expect(mockNavigate).not.toHaveBeenCalled();

    document.body.removeChild(textarea);
  });

  it('should not trigger when contentEditable is active', () => {
    renderHook(() => useKeyboardNavigation());

    const div = document.createElement('div');
    div.contentEditable = 'true';
    document.body.appendChild(div);
    div.focus();

    const event = new KeyboardEvent('keydown', {
      key: 'k',
      altKey: true,
      bubbles: true,
    });
    div.dispatchEvent(event);

    expect(mockNavigate).not.toHaveBeenCalled();

    document.body.removeChild(div);
  });

  it('should prevent default behavior for handled shortcuts', () => {
    renderHook(() => useKeyboardNavigation());

    const event = new KeyboardEvent('keydown', {
      key: 'b',
      ctrlKey: true,
      cancelable: true,
    });

    const preventDefaultSpy = vi.spyOn(event, 'preventDefault');
    window.dispatchEvent(event);

    expect(preventDefaultSpy).toHaveBeenCalled();
  });

  it('should not handle unregistered shortcuts', () => {
    renderHook(() => useKeyboardNavigation());

    const event = new KeyboardEvent('keydown', {
      key: 'x',
      ctrlKey: true,
    });
    window.dispatchEvent(event);

    expect(mockToggleSidebar).not.toHaveBeenCalled();
    expect(mockNavigate).not.toHaveBeenCalled();
  });

  it('should cleanup event listeners on unmount', () => {
    const { unmount } = renderHook(() => useKeyboardNavigation());

    unmount();

    const event = new KeyboardEvent('keydown', {
      key: 'b',
      ctrlKey: true,
    });
    window.dispatchEvent(event);

    // Should not trigger after unmount
    expect(mockToggleSidebar).not.toHaveBeenCalled();
  });
});