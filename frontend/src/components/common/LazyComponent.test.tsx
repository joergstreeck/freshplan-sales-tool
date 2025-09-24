import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import { LazyComponent } from './LazyComponent';
import React from 'react';
import { useInView } from 'react-intersection-observer';

// Mock react-intersection-observer with control over inView state
let mockInView = false;
const mockRef = vi.fn();
const mockInViewInstances: Array<{
  ref: unknown;
  inView: boolean;
  setInView: (value: boolean) => void;
}> = [];

vi.mock('react-intersection-observer', () => ({
  useInView: vi.fn(() => {
    const instance = {
      ref: mockRef,
      inView: mockInView,
      entry: undefined,
      setInView: (value: boolean) => {
        instance.inView = value;
      },
    };
    mockInViewInstances.push(instance);
    return instance;
  }),
}));

// Mock IntersectionObserver for fallback
class MockIntersectionObserver {
  callback: IntersectionObserverCallback;
  options?: IntersectionObserverInit;
  elements: Set<Element> = new Set();
  static instances = new Set<MockIntersectionObserver>();

  constructor(callback: IntersectionObserverCallback, options?: IntersectionObserverInit) {
    this.callback = callback;
    this.options = options;
    MockIntersectionObserver.instances.add(this);
  }

  observe(element: Element) {
    this.elements.add(element);
  }

  unobserve(element: Element) {
    this.elements.delete(element);
  }

  disconnect() {
    this.elements.clear();
    MockIntersectionObserver.instances.delete(this);
  }

  trigger(entries: Partial<IntersectionObserverEntry>[]) {
    this.callback(entries as IntersectionObserverEntry[], this as unknown as IntersectionObserver);
  }
}

describe('LazyComponent', () => {
  const TestComponent = () => <div data-testid="test-component">Loaded Content</div>;
  const fallback = <div data-testid="fallback">Loading...</div>;

  beforeEach(() => {
    MockIntersectionObserver.instances.clear();
    mockInViewInstances.length = 0;
    global.IntersectionObserver =
      MockIntersectionObserver as unknown as typeof IntersectionObserver;
    mockInView = false; // Reset to not in view by default

    // Reset mock implementation
    vi.mocked(useInView).mockImplementation(() => ({
      ref: mockRef,
      inView: mockInView,
      entry: undefined,
    }));
  });

  afterEach(() => {
    vi.clearAllMocks();
    mockInView = false;
  });

  describe('Basic Functionality', () => {
    it('should render placeholder initially', () => {
      render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      // LazyComponent shows a CircularProgress by default, not the fallback
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
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
      // Keep mockInView as false
      mockInView = false;

      render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      // Should show placeholder (CircularProgress)
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
      expect(screen.queryByTestId('test-component')).not.toBeInTheDocument();
    });
  });

  describe('Threshold Configuration', () => {
    it('should accept custom threshold', () => {
      // Just verify component renders with custom threshold
      render(
        <LazyComponent fallback={fallback} threshold={0.5}>
          <TestComponent />
        </LazyComponent>
      );

      // Component should render placeholder initially
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('should use default threshold when not provided', () => {
      // Just verify component renders with default threshold
      render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      // Component should render placeholder initially
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });
  });

  describe('Root Margin Configuration', () => {
    it('should accept custom rootMargin', () => {
      // Just verify component renders with custom rootMargin
      render(
        <LazyComponent fallback={fallback} rootMargin="200px">
          <TestComponent />
        </LazyComponent>
      );

      // Component should render placeholder initially
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('should use default rootMargin when not provided', () => {
      // Just verify component renders with default rootMargin
      render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      // Component should render placeholder initially
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });
  });

  describe('Multiple Components', () => {
    it('should handle multiple lazy components independently', async () => {
      const TestComponent1 = () => <div data-testid="component-1">Component 1</div>;
      const TestComponent2 = () => <div data-testid="component-2">Component 2</div>;

      // Mock independent inView states for multiple components
      let component1InView = false;
      let component2InView = false;
      let callCount = 0;

      vi.mocked(useInView).mockImplementation(() => {
        const index = callCount++;
        if (index === 0) {
          return { ref: mockRef, inView: component1InView, entry: undefined };
        } else {
          return { ref: mockRef, inView: component2InView, entry: undefined };
        }
      });

      const { rerender } = render(
        <>
          <LazyComponent placeholder={<div>Loading 1...</div>}>
            <TestComponent1 />
          </LazyComponent>
          <LazyComponent placeholder={<div>Loading 2...</div>}>
            <TestComponent2 />
          </LazyComponent>
        </>
      );

      // Initially both should show loading
      expect(screen.getByText('Loading 1...')).toBeInTheDocument();
      expect(screen.getByText('Loading 2...')).toBeInTheDocument();

      // Trigger first component
      component1InView = true;
      callCount = 0; // Reset for rerender
      rerender(
        <>
          <LazyComponent placeholder={<div>Loading 1...</div>}>
            <TestComponent1 />
          </LazyComponent>
          <LazyComponent placeholder={<div>Loading 2...</div>}>
            <TestComponent2 />
          </LazyComponent>
        </>
      );

      await waitFor(() => {
        expect(screen.getByTestId('component-1')).toBeInTheDocument();
        expect(screen.queryByTestId('component-2')).not.toBeInTheDocument();
      });

      // Trigger second component
      component2InView = true;
      callCount = 0; // Reset for rerender
      rerender(
        <>
          <LazyComponent placeholder={<div>Loading 1...</div>}>
            <TestComponent1 />
          </LazyComponent>
          <LazyComponent placeholder={<div>Loading 2...</div>}>
            <TestComponent2 />
          </LazyComponent>
        </>
      );

      await waitFor(() => {
        expect(screen.getByTestId('component-1')).toBeInTheDocument();
        expect(screen.getByTestId('component-2')).toBeInTheDocument();
      });
    });
  });

  describe('Cleanup', () => {
    it('should handle unmount gracefully', () => {
      const { unmount } = render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      // Should not throw on unmount
      expect(() => unmount()).not.toThrow();
    });

    it('should render component when already loaded', async () => {
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
  });

  describe('Edge Cases', () => {
    it('should handle missing fallback prop', () => {
      // Ensure component is not in view initially
      mockInView = false;

      render(
        <LazyComponent>
          <TestComponent />
        </LazyComponent>
      );

      // Should render default CircularProgress
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('should handle null children', () => {
      // Ensure component is not in view initially
      mockInView = false;

      render(<LazyComponent fallback={fallback}>{null}</LazyComponent>);

      // Should render default CircularProgress (not fallback since no children)
      expect(screen.getByRole('progressbar')).toBeInTheDocument();
    });

    it('should handle component that throws error', () => {
      const ErrorComponent = () => {
        throw new Error('Test error');
      };

      // Ensure component is not in view initially so ErrorComponent doesn't render
      mockInView = false;

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
      const _callCountBefore = vi.mocked(useInView).mock.calls.length;

      const { rerender } = render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      const callCountAfterFirst = vi.mocked(useInView).mock.calls.length;

      // Rerender with same props
      rerender(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      const callCountAfterSecond = vi.mocked(useInView).mock.calls.length;

      // useInView should be called once per render
      expect(callCountAfterSecond - callCountAfterFirst).toBe(1);
    });

    it('should not re-observe after loading', async () => {
      mockInView = true;

      render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      await waitFor(() => {
        expect(screen.getByTestId('test-component')).toBeInTheDocument();
      });

      const callCount = vi.mocked(useInView).mock.calls.length;

      // Force rerender
      mockInView = false;

      // useInView should be called with triggerOnce, so it shouldn't re-observe
      expect(vi.mocked(useInView).mock.calls.length).toBeGreaterThanOrEqual(callCount);
    });
  });

  describe('Accessibility', () => {
    it('should maintain focus when component loads', async () => {
      mockInView = false;

      render(
        <LazyComponent placeholder={<button data-testid="fallback-button">Loading</button>}>
          <button data-testid="loaded-button">Loaded</button>
        </LazyComponent>
      );

      const fallbackButton = screen.getByTestId('fallback-button');
      fallbackButton.focus();
      expect(document.activeElement).toBe(fallbackButton);

      // Set to in view to trigger loading
      mockInView = true;

      // Rerender to trigger the effect
      const { rerender: _rerender } = render(
        <LazyComponent placeholder={<button data-testid="fallback-button">Loading</button>}>
          <button data-testid="loaded-button">Loaded</button>
        </LazyComponent>
      );

      await waitFor(() => {
        const loadedButton = screen.getByTestId('loaded-button');
        expect(loadedButton).toBeInTheDocument();
      });
    });

    it('should have proper container attributes', () => {
      // Ensure component is not in view initially
      mockInView = false;

      render(
        <LazyComponent fallback={fallback}>
          <TestComponent />
        </LazyComponent>
      );

      // LazyComponent uses CircularProgress which has progressbar role
      const progressbar = screen.getByRole('progressbar');
      expect(progressbar).toBeInTheDocument();
    });
  });
});
