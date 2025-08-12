/**
 * Offline Queue Service
 *
 * Manages queued actions when offline for later execution.
 * Part of FC-005 Contact Management UI - Mobile Actions.
 *
 * @see /docs/features/FC-005-CUSTOMER-MANAGEMENT/Step3/MOBILE_CONTACT_ACTIONS.md
 */

import type { Contact } from '../../types/contact.types';
import type { QuickAction, QueuedAction } from '../../types/mobileActions.types';

class OfflineQueueService {
  private readonly STORAGE_KEY = 'freshplan_offline_actions';
  private readonly MAX_RETRIES = 3;
  private readonly RETRY_DELAY = 5000; // 5 seconds
  private isProcessing = false;

  constructor() {
    // Auto-process queue when coming online
    window.addEventListener('online', () => {
      this.processQueue();
    });

    // Check for queued actions on startup
    if (navigator.onLine) {
      setTimeout(() => this.processQueue(), 2000);
    }
  }

  /**
   * Queue an action for later execution
   */
  async queueAction(action: QuickAction, contact: Contact): Promise<void> {
    const queue = this.getQueue();

    const queuedAction: QueuedAction = {
      id: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      action,
      contactId: contact.id,
      contactData: {
        id: contact.id,
        firstName: contact.firstName,
        lastName: contact.lastName,
        email: contact.email,
        phone: contact.phone,
        mobile: contact.mobile,
      },
      timestamp: new Date(),
      retryCount: 0,
      maxRetries: this.MAX_RETRIES,
    };

    queue.push(queuedAction);
    this.saveQueue(queue);

    // Show notification
    this.notifyQueued(action, contact);

    // Try to register for background sync if available
    if ('serviceWorker' in navigator && 'sync' in self.registration) {
      try {
        await (self.registration as unknown).sync.register('sync-contact-actions');
      } catch (error) {}
    }
  }

  /**
   * Process all queued actions
   */
  async processQueue(): Promise<void> {
    if (!navigator.onLine || this.isProcessing) {
      return;
    }

    this.isProcessing = true;
    const queue = this.getQueue();
    const pending = [...queue];

    for (const item of pending) {
      try {
        // Attempt to execute action
        await this.executeQueuedAction(item);

        // Remove from queue on success
        this.removeFromQueue(item.id);
        this.notifySuccess(item);
      } catch (_error) { void _error;
        item.retryCount++;

        if (item.retryCount >= item.maxRetries) {
          // Max retries reached, remove and notify
          this.removeFromQueue(item.id);
          this.notifyFailed(item);
        } else { void 0;
          // Update retry count and try again later
          this.updateQueueItem(item);
          setTimeout(() => this.processQueue(), this.RETRY_DELAY);
        }
      }
    }

    this.isProcessing = false;
  }

  /**
   * Get queued actions
   */
  getQueue(): QueuedAction[] {
    try {
      const stored = localStorage.getItem(this.STORAGE_KEY);
      if (!stored) return [];

      const queue = JSON.parse(stored);
      // Convert date strings back to Date objects
      return queue.map((item: unknown) => ({
        ...item,
        timestamp: new Date(item.timestamp),
      }));
    } catch (_error) { void _error;
      return [];
    }
  }

  /**
   * Get queue size
   */
  getQueueSize(): number {
    return this.getQueue().length;
  }

  /**
   * Clear the queue
   */
  clearQueue(): void {
    localStorage.removeItem(this.STORAGE_KEY);
  }

  /**
   * Save queue to storage
   */
  private saveQueue(queue: QueuedAction[]): void {
    try {
      localStorage.setItem(this.STORAGE_KEY, JSON.stringify(queue));
    } catch (error) {}
  }

  /**
   * Remove item from queue
   */
  private removeFromQueue(id: string): void {
    const queue = this.getQueue();
    const filtered = queue.filter(item => item.id !== id);
    this.saveQueue(filtered);
  }

  /**
   * Update queue item
   */
  private updateQueueItem(item: QueuedAction): void {
    const queue = this.getQueue();
    const index = queue.findIndex(q => q.id === item.id);
    if (index !== -1) {
      queue[index] = item;
      this.saveQueue(queue);
    }
  }

  /**
   * Execute a queued action
   */
  private async executeQueuedAction(item: QueuedAction): Promise<void> {
    // Dynamically import to avoid circular dependency
    const { actionExecutionService } = await import('./ActionExecutionService');

    // Reconstruct minimal contact object
    const contact: Partial<Contact> = {
      id: item.contactId,
      ...item.contactData,
      customerId: '', // Required field, but not used for actions
      firstName: item.contactData.firstName || '',
      lastName: item.contactData.lastName || '',
      isPrimary: false,
      isActive: true,
      responsibilityScope: 'all',
      createdAt: '',
      updatedAt: '',
    };

    const result = await actionExecutionService.executeAction(item.action, contact as Contact);

    if (!result.success) {
      throw new Error(result.message || 'Action execution failed');
    }
  }

  /**
   * Notify user that action was queued
   */
  private notifyQueued(action: QuickAction, contact: Contact): void {
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification('Aktion gespeichert', {
        body: `${action.label} für ${contact.firstName} ${contact.lastName} wird ausgeführt, sobald Sie online sind`,
        icon: '/icon-192.png',
        tag: 'offline-queue',
      });
    }
  }

  /**
   * Notify user of successful execution
   */
  private notifySuccess(item: QueuedAction): void {
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification('Aktion erfolgreich', {
        body: `${item.action.label} wurde erfolgreich ausgeführt`,
        icon: '/icon-192.png',
        tag: 'offline-success',
      });
    }
  }

  /**
   * Notify user of failed action
   */
  private notifyFailed(item: QueuedAction): void {
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification('Aktion fehlgeschlagen', {
        body: `${item.action.label} konnte nicht ausgeführt werden`,
        icon: '/icon-192.png',
        tag: 'offline-failed',
      });
    }
  }

  /**
   * Request notification permission
   */
  async requestNotificationPermission(): Promise<void> {
    if ('Notification' in window && Notification.permission === 'default') {
      await Notification.requestPermission();
    }
  }
}

// Singleton export
export const offlineQueueService = new OfflineQueueService();
