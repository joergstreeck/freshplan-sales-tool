/**
 * Auto Save Hook
 *
 * Automatically saves draft changes after a debounce period.
 * Shows save status to the user.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/02-state-management.md
 */

import { useEffect, useRef } from 'react';
import { useCustomerOnboardingStore } from '../stores/customerOnboardingStore';

interface UseAutoSaveOptions {
  /** Debounce delay in milliseconds */
  delay?: number;
  /** Enable/disable auto-save */
  enabled?: boolean;
}

interface UseAutoSaveResult {
  /** Currently saving */
  isSaving: boolean;
  /** Last save timestamp */
  lastSaved: Date | null;
  /** Manually trigger save */
  save: () => Promise<void>;
}

/**
 * Auto-save hook for customer drafts
 *
 * Watches for changes and automatically saves after a delay.
 * Prevents data loss during the onboarding process.
 */
export const useAutoSave = (options: UseAutoSaveOptions = {}): UseAutoSaveResult => {
  const { delay = 2000, enabled = true } = options;

  const { isDirty, isSaving, lastSaved, saveAsDraft, draftId } = useCustomerOnboardingStore();

  const timeoutRef = useRef<NodeJS.Timeout>();

  useEffect(() => {
    if (!enabled || !isDirty) return;

    // Clear existing timeout
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }

    // Set new timeout
    timeoutRef.current = setTimeout(async () => {
      try {
        await saveAsDraft();
      } catch (_error) {
        void _error;        // Ignore save errors silently
      }
    }, delay);

    // Cleanup
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, [isDirty, enabled, delay, saveAsDraft, draftId]);

  return {
    isSaving,
    lastSaved,
    save: saveAsDraft,
  };
};
