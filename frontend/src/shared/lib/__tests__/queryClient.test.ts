/**
 * QueryClient Tests
 *
 * Tests for React Query client configuration.
 * Validates cache settings, retry logic, and refetch behavior.
 */

import { describe, it, expect, beforeEach } from 'vitest';
import { QueryClient } from '@tanstack/react-query';
import { queryClient } from '../queryClient';

describe('QueryClient Configuration', () => {
  beforeEach(() => {
    queryClient.clear();
  });

  it('should be an instance of QueryClient', () => {
    expect(queryClient).toBeInstanceOf(QueryClient);
  });

  describe('Query Default Options', () => {
    it('should have staleTime set to 5 minutes (300000ms)', () => {
      const options = queryClient.getDefaultOptions();
      expect(options.queries?.staleTime).toBe(5 * 60 * 1000);
      expect(options.queries?.staleTime).toBe(300000);
    });

    it('should have gcTime set to 10 minutes (600000ms)', () => {
      const options = queryClient.getDefaultOptions();
      expect(options.queries?.gcTime).toBe(10 * 60 * 1000);
      expect(options.queries?.gcTime).toBe(600000);
    });

    it('should retry failed queries 3 times', () => {
      const options = queryClient.getDefaultOptions();
      expect(options.queries?.retry).toBe(3);
    });

    it('should have exponential retry delay with max 30s', () => {
      const options = queryClient.getDefaultOptions();
      const retryDelay = options.queries?.retryDelay;

      expect(typeof retryDelay).toBe('function');

      if (typeof retryDelay === 'function') {
        // First retry: 1000 * 2^0 = 1000ms
        expect(retryDelay(0)).toBe(1000);
        // Second retry: 1000 * 2^1 = 2000ms
        expect(retryDelay(1)).toBe(2000);
        // Third retry: 1000 * 2^2 = 4000ms
        expect(retryDelay(2)).toBe(4000);
        // Fourth retry: 1000 * 2^3 = 8000ms
        expect(retryDelay(3)).toBe(8000);
        // Fifth retry: 1000 * 2^4 = 16000ms
        expect(retryDelay(4)).toBe(16000);
        // Sixth retry: 1000 * 2^5 = 32000ms → capped at 30000ms
        expect(retryDelay(5)).toBe(30000);
        // Tenth retry: should still be capped at 30000ms
        expect(retryDelay(10)).toBe(30000);
      }
    });

    it('should refetch on window focus by default', () => {
      const options = queryClient.getDefaultOptions();
      expect(options.queries?.refetchOnWindowFocus).toBe(true);
    });

    it('should refetch on reconnect by default', () => {
      const options = queryClient.getDefaultOptions();
      expect(options.queries?.refetchOnReconnect).toBe(true);
    });
  });

  describe('Mutation Default Options', () => {
    it('should retry failed mutations once', () => {
      const options = queryClient.getDefaultOptions();
      expect(options.mutations?.retry).toBe(1);
    });
  });

  describe('QueryClient Instance Behavior', () => {
    it('should start with empty cache', () => {
      const cache = queryClient.getQueryCache();
      expect(cache.getAll()).toHaveLength(0);
    });

    it('should allow clearing the cache', () => {
      queryClient.setQueryData(['test-key'], { data: 'test' });
      expect(queryClient.getQueryCache().getAll()).toHaveLength(1);

      queryClient.clear();
      expect(queryClient.getQueryCache().getAll()).toHaveLength(0);
    });

    it('should allow setting and getting query data', () => {
      const testData = { id: 1, name: 'Test' };
      queryClient.setQueryData(['user', 1], testData);

      const retrievedData = queryClient.getQueryData(['user', 1]);
      expect(retrievedData).toEqual(testData);
    });

    it('should invalidate queries by key', () => {
      queryClient.setQueryData(['user', 1], { id: 1, name: 'Test' });

      const query = queryClient.getQueryCache().find({ queryKey: ['user', 1] });
      expect(query?.state.isInvalidated).toBe(false);

      queryClient.invalidateQueries({ queryKey: ['user', 1] });

      const invalidatedQuery = queryClient.getQueryCache().find({ queryKey: ['user', 1] });
      expect(invalidatedQuery?.state.isInvalidated).toBe(true);
    });
  });

  describe('Configuration Values Validation', () => {
    it('should have staleTime less than gcTime', () => {
      const options = queryClient.getDefaultOptions();
      const staleTime = options.queries?.staleTime as number;
      const gcTime = options.queries?.gcTime as number;

      expect(staleTime).toBeLessThan(gcTime);
    });

    it('should have query retry count higher than mutation retry count', () => {
      const options = queryClient.getDefaultOptions();
      const queryRetry = options.queries?.retry as number;
      const mutationRetry = options.mutations?.retry as number;

      expect(queryRetry).toBeGreaterThan(mutationRetry);
    });

    it('should have all time values as positive numbers', () => {
      const options = queryClient.getDefaultOptions();

      expect(options.queries?.staleTime).toBeGreaterThan(0);
      expect(options.queries?.gcTime).toBeGreaterThan(0);
    });

    it('should have all retry values as positive numbers', () => {
      const options = queryClient.getDefaultOptions();

      expect(options.queries?.retry).toBeGreaterThan(0);
      expect(options.mutations?.retry).toBeGreaterThan(0);
    });
  });
});
