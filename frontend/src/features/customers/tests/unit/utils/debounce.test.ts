/**
 * FC-005 Debounce Utility Tests
 *
 * Tests für Performance-Optimierung bei User Input.
 * Wichtig für Auto-Save und Search-Features.
 */

import { describe, it, expect, vi, beforeEach } from 'vitest';
import { debounce } from '../../../utils/debounce';

describe('Debounce Utility', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('should delay function execution', () => {
    const mockFn = vi.fn();
    const debouncedFn = debounce(mockFn, 100);

    debouncedFn('arg1', 'arg2');

    // Function should not be called immediately
    expect(mockFn).not.toHaveBeenCalled();

    // Advance timers by 100ms
    vi.advanceTimersByTime(100);

    // Now function should be called
    expect(mockFn).toHaveBeenCalledWith('arg1', 'arg2');
  });

  it('should cancel previous timer when called multiple times', () => {
    const mockFn = vi.fn();
    const debouncedFn = debounce(mockFn, 100);

    debouncedFn('first');
    vi.advanceTimersByTime(50);

    debouncedFn('second');
    vi.advanceTimersByTime(50);

    debouncedFn('third');
    vi.advanceTimersByTime(100);

    // Only the last call should execute
    expect(mockFn).toHaveBeenCalledTimes(1);
    expect(mockFn).toHaveBeenCalledWith('third');
  });

  it('should handle rapid successive calls (Auto-Save scenario)', () => {
    const mockSave = vi.fn();
    const debouncedSave = debounce(mockSave, 500);

    // Simulate rapid user input
    debouncedSave({ field: 'companyName', value: 'T' });
    vi.advanceTimersByTime(100);

    debouncedSave({ field: 'companyName', value: 'Te' });
    vi.advanceTimersByTime(100);

    debouncedSave({ field: 'companyName', value: 'Tes' });
    vi.advanceTimersByTime(100);

    debouncedSave({ field: 'companyName', value: 'Test' });
    vi.advanceTimersByTime(100);

    debouncedSave({ field: 'companyName', value: 'Test GmbH' });

    // No saves yet
    expect(mockSave).not.toHaveBeenCalled();

    // Complete the delay
    vi.advanceTimersByTime(500);

    // Only final value should be saved
    expect(mockSave).toHaveBeenCalledTimes(1);
    expect(mockSave).toHaveBeenCalledWith({ field: 'companyName', value: 'Test GmbH' });
  });

  it('should work with async functions', async () => {
    const mockAsyncFn = vi.fn().mockResolvedValue('result');
    const debouncedAsyncFn = debounce(mockAsyncFn, 100);

    debouncedAsyncFn('test');
    vi.advanceTimersByTime(100);

    expect(mockAsyncFn).toHaveBeenCalledWith('test');
  });

  it('should preserve function context and arguments', () => {
    const mockFn = vi.fn();
    const debouncedFn = debounce(mockFn, 100);

    const complexArgs = {
      customerData: {
        companyName: 'Test GmbH',
        industry: 'hotel',
        locations: [{ name: 'Hauptstandort' }, { name: 'Filiale 1' }],
      },
      metadata: {
        timestamp: Date.now(),
        user: 'test-user',
      },
    };

    debouncedFn(complexArgs, 'secondArg', 123);
    vi.advanceTimersByTime(100);

    expect(mockFn).toHaveBeenCalledWith(complexArgs, 'secondArg', 123);
  });

  it('should handle different delay times', () => {
    const mockFn = vi.fn();
    const shortDebounce = debounce(mockFn, 50);
    const longDebounce = debounce(mockFn, 200);

    shortDebounce('short');
    longDebounce('long');

    // Advance by short delay
    vi.advanceTimersByTime(50);
    expect(mockFn).toHaveBeenCalledWith('short');

    // Advance by remaining time for long delay
    vi.advanceTimersByTime(150);
    expect(mockFn).toHaveBeenCalledWith('long');

    expect(mockFn).toHaveBeenCalledTimes(2);
  });

  it('should not crash with no arguments', () => {
    const mockFn = vi.fn();
    const debouncedFn = debounce(mockFn, 100);

    expect(() => {
      debouncedFn();
      vi.advanceTimersByTime(100);
    }).not.toThrow();

    expect(mockFn).toHaveBeenCalledWith();
  });

  it('should work with TypeScript generic types', () => {
    // Test that TypeScript types are preserved
    const numberFunction = (x: number, y: number): number => x + y;
    const debouncedNumber = debounce(numberFunction, 100);

    const stringFunction = (s: string): string => s.toUpperCase();
    const debouncedString = debounce(stringFunction, 100);

    // These should compile without TypeScript errors
    debouncedNumber(1, 2);
    debouncedString('test');

    // This ensures the tests compile correctly
    expect(typeof debouncedNumber).toBe('function');
    expect(typeof debouncedString).toBe('function');
  });

  describe('Real-world Customer Onboarding scenarios', () => {
    it('should handle field validation debouncing', () => {
      const mockValidate = vi.fn();
      const debouncedValidate = debounce(mockValidate, 300);

      // User types email address character by character
      const emailInputs = [
        'test',
        'test@',
        'test@ex',
        'test@exa',
        'test@exam',
        'test@exampl',
        'test@example',
        'test@example.',
        'test@example.c',
        'test@example.co',
        'test@example.com',
      ];

      emailInputs.forEach((input, index) => {
        debouncedValidate('contactEmail', input);
        if (index < emailInputs.length - 1) {
          vi.advanceTimersByTime(50); // User continues typing
        }
      });

      // Complete validation delay
      vi.advanceTimersByTime(300);

      // Validation should only run once with final value
      expect(mockValidate).toHaveBeenCalledTimes(1);
      expect(mockValidate).toHaveBeenCalledWith('contactEmail', 'test@example.com');
    });

    it('should handle auto-save with draft persistence', () => {
      const mockSaveDraft = vi.fn().mockResolvedValue({ draftId: 'draft-123' });
      const debouncedSaveDraft = debounce(mockSaveDraft, 1000);

      // User makes multiple quick changes
      debouncedSaveDraft({
        customerData: { companyName: 'Test' },
        timestamp: Date.now(),
      });

      vi.advanceTimersByTime(200);

      debouncedSaveDraft({
        customerData: {
          companyName: 'Test GmbH',
          industry: 'hotel',
        },
        timestamp: Date.now(),
      });

      vi.advanceTimersByTime(200);

      debouncedSaveDraft({
        customerData: {
          companyName: 'Test Hotel GmbH',
          industry: 'hotel',
          chainCustomer: 'ja',
        },
        timestamp: Date.now(),
      });

      // Complete auto-save delay
      vi.advanceTimersByTime(1000);

      // Only final state should be saved
      expect(mockSaveDraft).toHaveBeenCalledTimes(1);
      expect(mockSaveDraft).toHaveBeenCalledWith(
        expect.objectContaining({
          customerData: {
            companyName: 'Test Hotel GmbH',
            industry: 'hotel',
            chainCustomer: 'ja',
          },
        })
      );
    });
  });
});
