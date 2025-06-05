/**
 * EventBus Tests
 */

import { describe, it, expect, beforeEach, vi } from 'vitest';
import EventBus from '../EventBus';

describe('EventBus', () => {
  beforeEach(() => {
    // Clear all events before each test
    EventBus.clear();
  });

  describe('Basic Events', () => {
    it('should emit and receive events', () => {
      const callback = vi.fn();
      
      EventBus.on('test:event', callback);
      EventBus.emit('test:event', { data: 'test' });
      
      expect(callback).toHaveBeenCalledWith({ data: 'test' });
      expect(callback).toHaveBeenCalledTimes(1);
    });

    it('should handle multiple subscribers', () => {
      const callback1 = vi.fn();
      const callback2 = vi.fn();
      
      EventBus.on('test:event', callback1);
      EventBus.on('test:event', callback2);
      EventBus.emit('test:event', 'data');
      
      expect(callback1).toHaveBeenCalledWith('data');
      expect(callback2).toHaveBeenCalledWith('data');
    });

    it('should unsubscribe correctly', () => {
      const callback = vi.fn();
      
      const unsubscribe = EventBus.on('test:event', callback);
      EventBus.emit('test:event', 'first');
      
      unsubscribe();
      EventBus.emit('test:event', 'second');
      
      expect(callback).toHaveBeenCalledTimes(1);
      expect(callback).toHaveBeenCalledWith('first');
    });

    it('should handle once subscriptions', () => {
      const callback = vi.fn();
      
      EventBus.once('test:once', callback);
      EventBus.emit('test:once', 'first');
      EventBus.emit('test:once', 'second');
      
      expect(callback).toHaveBeenCalledTimes(1);
      expect(callback).toHaveBeenCalledWith('first');
    });
  });

  describe('Wildcard Events', () => {
    it('should handle wildcard subscriptions', () => {
      const callback = vi.fn();
      
      EventBus.on('test:*', callback);
      EventBus.emit('test:one', 'data1');
      EventBus.emit('test:two', 'data2');
      EventBus.emit('other:event', 'data3');
      
      expect(callback).toHaveBeenCalledTimes(2);
      expect(callback).toHaveBeenCalledWith('data1');
      expect(callback).toHaveBeenCalledWith('data2');
    });

    it('should handle global wildcard', () => {
      const callback = vi.fn();
      
      EventBus.on('*', callback);
      EventBus.emit('any:event', 'data1');
      EventBus.emit('other:event', 'data2');
      
      expect(callback).toHaveBeenCalledTimes(2);
    });
  });

  describe('Context Binding', () => {
    it('should call handler with correct context', () => {
      const context = { name: 'test' };
      const callback = vi.fn(function(this: any) {
        return this.name;
      });
      
      EventBus.on('test:context', callback, context);
      EventBus.emit('test:context');
      
      expect(callback).toHaveBeenCalled();
      expect(callback.mock.results[0].value).toBe('test');
    });
  });

  describe('Error Handling', () => {
    it('should catch and emit errors from handlers', () => {
      const errorCallback = vi.fn();
      const errorHandler = vi.fn(() => {
        throw new Error('Handler error');
      });
      
      EventBus.on('error', errorCallback);
      EventBus.on('test:error', errorHandler);
      
      // Should not throw
      expect(() => EventBus.emit('test:error')).not.toThrow();
      
      // Should emit error event
      expect(errorCallback).toHaveBeenCalled();
      expect(errorCallback.mock.calls[0][0]).toMatchObject({
        type: 'handler-error',
        error: expect.any(Error)
      });
    });
  });

  describe('Async Events', () => {
    it('should handle async emit', async () => {
      const callback = vi.fn(async (data) => {
        await new Promise(resolve => setTimeout(resolve, 10));
        return data;
      });
      
      EventBus.on('test:async', callback);
      await EventBus.emitAsync('test:async', 'data');
      
      expect(callback).toHaveBeenCalledWith('data');
    });

    it('should handle async handler errors', async () => {
      const errorCallback = vi.fn();
      const asyncHandler = vi.fn(async () => {
        throw new Error('Async error');
      });
      
      EventBus.on('error', errorCallback);
      EventBus.on('test:async:error', asyncHandler);
      
      await EventBus.emitAsync('test:async:error');
      
      expect(errorCallback).toHaveBeenCalled();
      expect(errorCallback.mock.calls[0][0]).toMatchObject({
        type: 'async-handler-error',
        error: expect.any(Error)
      });
    });
  });

  describe('Event Management', () => {
    it('should remove all handlers for an event', () => {
      const callback1 = vi.fn();
      const callback2 = vi.fn();
      
      EventBus.on('test:remove', callback1);
      EventBus.on('test:remove', callback2);
      
      EventBus.off('test:remove');
      EventBus.emit('test:remove');
      
      expect(callback1).not.toHaveBeenCalled();
      expect(callback2).not.toHaveBeenCalled();
    });

    it('should clear all events', () => {
      const callback1 = vi.fn();
      const callback2 = vi.fn();
      
      EventBus.on('event1', callback1);
      EventBus.on('event2', callback2);
      
      EventBus.clear();
      
      EventBus.emit('event1');
      EventBus.emit('event2');
      
      expect(callback1).not.toHaveBeenCalled();
      expect(callback2).not.toHaveBeenCalled();
    });

    it('should track registered events', () => {
      EventBus.on('test:one', () => {});
      EventBus.on('test:two', () => {});
      EventBus.on('test:*', () => {});
      
      const events = EventBus.getRegisteredEvents();
      
      expect(events).toContain('test:one');
      expect(events).toContain('test:two');
      expect(events).toContain('test:*');
    });

    it('should check if event has listeners', () => {
      EventBus.on('test:check', () => {});
      
      expect(EventBus.hasListeners('test:check')).toBe(true);
      expect(EventBus.hasListeners('test:none')).toBe(false);
    });

    it('should count listeners correctly', () => {
      EventBus.on('test:count', () => {});
      EventBus.on('test:count', () => {});
      EventBus.on('test:*', () => {});
      
      expect(EventBus.listenerCount('test:count')).toBe(3); // 2 direct + 1 wildcard
      expect(EventBus.listenerCount('test:other')).toBe(1); // 1 wildcard
    });
  });

  describe('Event History', () => {
    it('should track event history', () => {
      EventBus.emit('test:history1', 'data1');
      EventBus.emit('test:history2', 'data2');
      
      const history = EventBus.getHistory();
      
      expect(history).toHaveLength(2);
      expect(history[0]).toMatchObject({
        type: 'test:history1',
        payload: 'data1',
        timestamp: expect.any(Number)
      });
      expect(history[1]).toMatchObject({
        type: 'test:history2',
        payload: 'data2',
        timestamp: expect.any(Number)
      });
    });
  });

  describe('Debug Mode', () => {
    it('should toggle debug mode', () => {
      const consoleSpy = vi.spyOn(console, 'log').mockImplementation(() => {});
      
      EventBus.setDebug(true);
      EventBus.emit('test:debug', 'data');
      
      expect(consoleSpy).toHaveBeenCalledWith('[EventBus] Emitting: test:debug', 'data');
      
      EventBus.setDebug(false);
      consoleSpy.mockRestore();
    });
  });
});