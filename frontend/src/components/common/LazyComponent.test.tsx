import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { LazyComponent } from './LazyComponent';
import React from 'react';

// Mock react-intersection-observer with control over inView state
let mockInView = false;
const mockRef = vi.fn();

vi.mock('react-intersection-observer', () => ({
  useInView: vi.fn(() => ({
    ref: mockRef,
    inView: mockInView,
    entry: undefined
  }))
}));

// Mock IntersectionObserver for fallback
class MockIntersectionObserver {
  callback: IntersectionObserverCallback;
  options?: IntersectionObserverInit;
  elements: Set<Element> = new Set();

  constructor(callback: IntersectionObserverCallback, options?: IntersectionObserverInit) {
    this.callback = callback;
    this.options = options;
    mockObserverInstances.add(this);
  }

  observe(element: Element) {
    this.elements.add(element);
  }

  unobserve(element: Element) {
    this.elements.delete(element);
  }

  disconnect() {
    this.elements.clear();
  }

  trigger(entries: Partial<IntersectionObserverEntry>[]) {
    this.callback(entries as IntersectionObserverEntry[], this as any);
  }
}

const mockObserverInstances = new Set<MockIntersectionObserver>();

describe('LazyComponent', () => {
  const TestComponent = () => <div data-testid="test-component">Loaded Content</div>;
  const fallback = <div data-testid="fallback">Loading...</div>;

  beforeEach(() => {
    mockObserverInstances.clear();
    global.IntersectionObserver = MockIntersectionObserver as any;
    mockInView = false; // Reset to not in view by default
  });

  afterEach(() => {
    vi.clearAllMocks();
    mockInView = false;
  });

  describe('Basic Functionality', () => {
    it('should render fallback initially', () => {
      render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      expect(screen.getByTestId('fallback')).toBeInTheDocument();
      expect(screen.queryByTestId('test-component')).not.toBeInTheDocument();
    });

    it('should render component when intersecting', async () => {
      // Set mock to in view
      mockInView = true;
      
      render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      await waitFor(() => {
        expect(screen.getByTestId('test-component')).toBeInTheDocument();
      });
    });

    it('should not render component when not intersecting', () => {
      render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      // Trigger non-intersection
      const observer = Array.from(mockObserverInstances)[0];
      observer.trigger([{ isIntersecting: false }]);

      expect(screen.getByTestId('fallback')).toBeInTheDocument();
      expect(screen.queryByTestId('test-component')).not.toBeInTheDocument();
    });
  });

  describe('Threshold Configuration', () => {
    it('should use custom threshold', () => {
      render(
        <LazyComponent fallback={fallback} threshold={0.5}>
          <TestComponent />
        </LazyComponent>
      );

      const observer = Array.from(mockObserverInstances)[0];
      expect(observer.options?.threshold).toBe(0.5);
    });

    it('should use default threshold when not provided', () => {
      render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      const observer = Array.from(mockObserverInstances)[0];
      expect(observer.options?.threshold).toBe(0.1);
    });
  });

  describe('Root Margin Configuration', () => {
    it('should use custom rootMargin', () => {
      render(
        <LazyComponent fallback={fallback} rootMargin="200px">
          <TestComponent />
        </LazyComponent>
      );

      const observer = Array.from(mockObserverInstances)[0];
      expect(observer.options?.rootMargin).toBe('200px');
    });

    it('should use default rootMargin when not provided', () => {
      render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      const observer = Array.from(mockObserverInstances)[0];
      expect(observer.options?.rootMargin).toBe('100px');
    });
  });

  describe('Multiple Components', () => {
    it('should handle multiple lazy components independently', async () => {
      const TestComponent1 = () => <div data-testid="component-1">Component 1</div>;
      const TestComponent2 = () => <div data-testid="component-2">Component 2</div>;

      render(
        <>
          <LazyComponent fallback={<div>Loading 1...</div>}>
            <TestComponent1 />
          </LazyComponent>
          <LazyComponent fallback={<div>Loading 2...</div>}>
            <TestComponent2 />
          </LazyComponent>
        </>
      );

      const observers = Array.from(mockObserverInstances);
      expect(observers).toHaveLength(2);

      // Trigger first component
      observers[0].trigger([{ isIntersecting: true }]);

      await waitFor(() => {
        expect(screen.getByTestId('component-1')).toBeInTheDocument();
        expect(screen.queryByTestId('component-2')).not.toBeInTheDocument();
      });

      // Trigger second component
      observers[1].trigger([{ isIntersecting: true }]);

      await waitFor(() => {
        expect(screen.getByTestId('component-1')).toBeInTheDocument();
        expect(screen.getByTestId('component-2')).toBeInTheDocument();
      });
    });
  });

  describe('Cleanup', () => {
    it('should disconnect observer on unmount', () => {
      const { unmount } = render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      const observer = Array.from(mockObserverInstances)[0];
      const disconnectSpy = vi.spyOn(observer, 'disconnect');

      unmount();

      expect(disconnectSpy).toHaveBeenCalled();
    });

    it('should unobserve element when already loaded', async () => {
      render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      const observer = Array.from(mockObserverInstances)[0];
      const unobserveSpy = vi.spyOn(observer, 'unobserve');

      // Trigger intersection to load component
      observer.trigger([{ isIntersecting: true }]);

      await waitFor(() => {
        expect(screen.getByTestId('test-component')).toBeInTheDocument();
        expect(unobserveSpy).toHaveBeenCalled();
      });
    });
  });

  describe('Edge Cases', () => {
    it('should handle missing fallback prop', () => {
      render(
        <LazyComponent>
          <TestComponent />
        </LazyComponent>
      );

      // Should render empty div as default fallback
      const container = screen.getByTestId('lazy-container');
      expect(container).toBeInTheDocument();
      expect(container).toBeEmptyDOMElement();
    });

    it('should handle null children', () => {
      render(
        <LazyComponent fallback={fallback}>
          {null}
        </LazyComponent>
      );

      expect(screen.getByTestId('fallback')).toBeInTheDocument();
    });

    it('should handle component that throws error', () => {
      const ErrorComponent = () => {
        throw new Error('Test error');
      };

      // Suppress error output
      const consoleError = console.error;
      console.error = vi.fn();

      expect(() => {
        render(
          <LazyComponent fallback={fallback}>
            <ErrorComponent />
          </LazyComponent>
        );
      }).not.toThrow();

      console.error = consoleError;
    });
  });

  describe('Performance', () => {
    it('should only create one observer per component', () => {
      const { rerender } = render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      const initialCount = mockObserverInstances.size;

      // Rerender with same props
      rerender(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      expect(mockObserverInstances.size).toBe(initialCount);
    });

    it('should not re-observe after loading', async () => {
      render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      const observer = Array.from(mockObserverInstances)[0];
      const observeSpy = vi.spyOn(observer, 'observe');

      // Trigger intersection
      observer.trigger([{ isIntersecting: true }]);

      await waitFor(() => {
        expect(screen.getByTestId('test-component')).toBeInTheDocument();
      });

      // Clear spy call count
      observeSpy.mockClear();

      // Trigger intersection again
      observer.trigger([{ isIntersecting: true }]);

      // Should not observe again
      expect(observeSpy).not.toHaveBeenCalled();
    });
  });

  describe('Accessibility', () => {
    it('should maintain focus when component loads', async () => {
      render(
        <LazyComponent fallback={<button data-testid="fallback-button">Loading</button>}>
          <button data-testid="loaded-button">Loaded</button>
        </LazyComponent>
      );

      const fallbackButton = screen.getByTestId('fallback-button');
      fallbackButton.focus();
      expect(document.activeElement).toBe(fallbackButton);

      // Trigger intersection
      const observer = Array.from(mockObserverInstances)[0];
      observer.trigger([{ isIntersecting: true }]);

      await waitFor(() => {
        const loadedButton = screen.getByTestId('loaded-button');
        expect(loadedButton).toBeInTheDocument();
      });
    });

    it('should have proper container attributes', () => {
      render(
        <LazyComponent fallback={fallback} className="custom-class">
          <TestComponent />
        </LazyComponent>
      );

      const container = screen.getByTestId('lazy-container');
      expect(container).toHaveClass('custom-class');
    });
  });
});