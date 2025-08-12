/**
 * Auto Save API Hook
 *
 * Enhanced auto-save hook with API integration.
 * Handles network failures gracefully and provides detailed status.
 *
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/04-api-integration.md
 * @see /Users/joergstreeck/freshplan-sales-tool/docs/features/FC-005-CUSTOMER-MANAGEMENT/03-FRONTEND/02-state-management.md
 */

import { useEffect, useRef, useState, useCallback } from 'react';
import { useCustomerOnboardingStore } from '../stores/customerOnboardingStore';
import { useUpdateCustomerDraft, useCreateCustomerDraft } from '../services';
import { toast } from 'react-toastify';

interface UseAutoSaveApiOptions {
  /** Debounce delay in milliseconds */
  delay?: number;
  /** Enable/disable auto-save */
  enabled?: boolean;
  /** Show toast notifications */
  showNotifications?: boolean;
  /** Callback on successful save */
  onSuccess?: () => void;
  /** Callback on save error */
  onError?: (error: Error) => void;
}

interface UseAutoSaveApiResult {
  /** Currently saving */
  isSaving: boolean;
  /** Last save timestamp */
  lastSaved: Date | null;
  /** Last save error */
  lastError: Error | null;
  /** Save status */
  status: 'idle' | 'saving' | 'success' | 'error';
  /** Manually trigger save */
  save: () => Promise<void>;
  /** Retry failed save */
  retry: () => Promise<void>;
  /** Clear error state */
  clearError: () => void;
}

/**
 * Auto-save hook with full API integration
 *
 * Features:
 * - Automatic draft creation if needed
 * - Debounced saving on changes
 * - Network error handling with retry
 * - Detailed status reporting
 * - Optional toast notifications
 */
export const useAutoSaveApi = (options: UseAutoSaveApiOptions = {}): UseAutoSaveApiResult => {
  const { delay = 2000, enabled = true, showNotifications = false, onSuccess, onError } = options;

  const {
    isDirty,
    draftId,
    customerData,
    locations,
    setDraftId,
    setLastSaved,
    setSaving,
    setDirty,
  } = useCustomerOnboardingStore();

  const [status, setStatus] = useState<'idle' | 'saving' | 'success' | 'error'>('idle');
  const [lastError, setLastError] = useState<Error | null>(null);
  const [lastSavedDate, setLastSavedDate] = useState<Date | null>(null);

  const timeoutRef = useRef<NodeJS.Timeout>();
  const saveCountRef = useRef(0);

  // API Mutations
  const createDraftMutation = useCreateCustomerDraft({
    onSuccess: data => {
      setDraftId(data.id);
      if (showNotifications) {
        toast.success('Entwurf erstellt');
      }
    },
    onError: error => {
      setLastError(error as Error);
      if (showNotifications) {
        toast.error('Entwurf konnte nicht erstellt werden');
      }
    },
  });

  const updateDraftMutation = useUpdateCustomerDraft({
    onSuccess: () => {
      const now = new Date();
      setLastSaved(now);
      setLastSavedDate(now);
      setStatus('success');
      setDirty(false);
      saveCountRef.current += 1;

      if (showNotifications && saveCountRef.current % 5 === 0) {
        toast.success('Automatisch gespeichert', { autoClose: 1000 });
      }

      onSuccess?.();
    },
    onError: error => {
      setLastError(error as Error);
      setStatus('error');

      if (showNotifications) {
        toast.error('Speichern fehlgeschlagen - wird erneut versucht');
      }

      onError?.(error as Error);
    },
  });

  /**
   * Save implementation
   */
  const performSave = useCallback(async () => {
    try {
      setStatus('saving');
      setSaving(true);
      setLastError(null);

      // Prepare save data
      const saveData = {
        fieldValues: {
          ...customerData,
          // Include location count for quick reference
          _locationCount: locations.length,
          _lastModified: new Date().toISOString(),
        },
      };

      if (!draftId) {
        // Create new draft first
        const draft = await createDraftMutation.mutateAsync();
        // Then update with data
        await updateDraftMutation.mutateAsync({
          draftId: draft.id,
          data: saveData,
        });
      } else {
        // Update existing draft
        await updateDraftMutation.mutateAsync({
          draftId,
          data: saveData,
        });
      }
    } catch (_error) { void _error;
      throw error;
    } finally {
      setSaving(false);
    }
  }, [draftId, customerData, locations, createDraftMutation, updateDraftMutation, setSaving]);

  /**
   * Manual save trigger
   */
  const save = useCallback(async () => {
    // Clear any pending auto-save
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }

    await performSave();
  }, [performSave]);

  /**
   * Retry failed save
   */
  const retry = useCallback(async () => {
    if (status === 'error' && lastError) {
      await performSave();
    }
  }, [status, lastError, performSave]);

  /**
   * Clear error state
   */
  const clearError = useCallback(() => {
    setLastError(null);
    setStatus('idle');
  }, []);

  /**
   * Auto-save effect
   */
  useEffect(() => {
    if (!enabled || !isDirty) {
      setStatus('idle');
      return;
    }

    // Clear existing timeout
    if (timeoutRef.current) {
      clearTimeout(timeoutRef.current);
    }

    // Set new timeout
    timeoutRef.current = setTimeout(() => {
      performSave().catch(() => {
        // Error already handled in mutation
      });
    }, delay);

    // Cleanup
    return () => {
      if (timeoutRef.current) {
        clearTimeout(timeoutRef.current);
      }
    };
  }, [isDirty, enabled, delay, performSave]);

  /**
   * Save on unmount if dirty
   */
  useEffect(() => {
    return () => {
      if (isDirty && enabled) {
        // Attempt synchronous save on unmount
        performSave().catch(() => {
          // Error already handled in mutation
        });
      }
    };
  }, [isDirty, enabled, performSave]);

  return {
    isSaving: createDraftMutation.isLoading || updateDraftMutation.isLoading,
    lastSaved: lastSavedDate,
    lastError,
    status,
    save,
    retry,
    clearError,
  };
};
