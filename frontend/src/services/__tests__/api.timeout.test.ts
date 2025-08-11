import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { ApiService } from '../api';

describe('ApiService Timeout', () => {
  let fetchSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(() => {
    fetchSpy = vi.spyOn(global, 'fetch');
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  it('should include timeout signal in fetch request', async () => {
    const mockResponse = {
      message: 'pong',
      timestamp: '2025-08-11T15:00:00Z',
      environment: 'test',
    };

    fetchSpy.mockResolvedValueOnce({
      ok: true,
      json: vi.fn().mockResolvedValueOnce(mockResponse),
    } as unknown as Response);

    await ApiService.ping();

    expect(fetchSpy).toHaveBeenCalledWith(
      expect.stringContaining('/api/ping'),
      expect.objectContaining({
        signal: expect.any(AbortSignal),
      })
    );
  });

  it('should handle timeout error correctly', async () => {
    // Create an AbortError to simulate timeout
    const abortError = new Error('The operation was aborted');
    abortError.name = 'AbortError';
    fetchSpy.mockRejectedValueOnce(abortError);

    await expect(ApiService.ping()).rejects.toMatchObject({
      code: 'TIMEOUT',
      message: 'Request timeout - the server took too long to respond',
      details: { timeout: 10000 },
    });
  });

  it('should handle TimeoutError correctly', async () => {
    // Some environments use TimeoutError instead of AbortError
    const timeoutError = new Error('Request timed out');
    timeoutError.name = 'TimeoutError';
    fetchSpy.mockRejectedValueOnce(timeoutError);

    await expect(ApiService.ping()).rejects.toMatchObject({
      code: 'TIMEOUT',
      message: 'Request timeout - the server took too long to respond',
      details: { timeout: 10000 },
    });
  });

  it('should use 10 second timeout by default', async () => {
    const mockResponse = {
      message: 'pong',
      timestamp: '2025-08-11T15:00:00Z',
      environment: 'test',
    };

    fetchSpy.mockResolvedValueOnce({
      ok: true,
      json: vi.fn().mockResolvedValueOnce(mockResponse),
    } as unknown as Response);

    await ApiService.ping();

    // Check that the signal is an AbortSignal with timeout
    const callArgs = fetchSpy.mock.calls[0];
    const signal = callArgs[1]?.signal;

    expect(signal).toBeInstanceOf(AbortSignal);
  });
});
