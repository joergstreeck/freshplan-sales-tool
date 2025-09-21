import { useState, useCallback } from 'react';
import { helpApi } from './helpApi';

interface OperationsGuidance {
  guidance: CARResponse;
  confidence: number;
  metadata: {
    category: string;
    timestamp: string;
    user_persona: string;
  };
}

interface ConfidenceCheck {
  confidence: number;
  tier: 'high' | 'medium' | 'low' | 'insufficient';
  has_guidance: boolean;
  category: string;
}

interface OperationsTemplate {
  title: string;
  query: string;
  description: string;
  confidence: number;
}

/**
 * Hook für Operations-Guidance im CAR-Strategy Help-System
 * Integriert User-Lead-Protection Runbook in die Help-Experience
 */
export const useOperationsGuidance = () => {
  const [guidance, setGuidance] = useState<OperationsGuidance | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const getGuidance = useCallback(async (query: string): Promise<OperationsGuidance | null> => {
    if (!query?.trim()) {
      setError('Query is required');
      return null;
    }

    setIsLoading(true);
    setError(null);

    try {
      const response = await helpApi.post('/operations/user-lead-guidance', { query });

      if (response.ok) {
        const data = await response.json() as OperationsGuidance;
        setGuidance(data);
        return data;
      } else if (response.status === 404) {
        // No guidance available for this query
        setGuidance(null);
        return null;
      } else {
        throw new Error(`HTTP ${response.status}: ${await response.text()}`);
      }
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Unknown error occurred';
      setError(errorMessage);
      console.error('Operations guidance error:', err);
      return null;
    } finally {
      setIsLoading(false);
    }
  }, []);

  const checkConfidence = useCallback(async (query: string): Promise<ConfidenceCheck | null> => {
    if (!query?.trim()) {
      return null;
    }

    try {
      const response = await helpApi.get(`/operations/confidence-check?query=${encodeURIComponent(query)}`);

      if (response.ok) {
        return await response.json() as ConfidenceCheck;
      }
      return null;
    } catch (err) {
      console.error('Confidence check error:', err);
      return null;
    }
  }, []);

  const getTemplates = useCallback(async (): Promise<Record<string, OperationsTemplate> | null> => {
    try {
      const response = await helpApi.get('/operations/templates');

      if (response.ok) {
        const data = await response.json();
        return data.templates as Record<string, OperationsTemplate>;
      }
      return null;
    } catch (err) {
      console.error('Templates fetch error:', err);
      return null;
    }
  }, []);

  const executeOperationsAction = useCallback(async (action: string) => {
    // Placeholder für Operations-Action-Execution
    // Hier würden SQL-Commands oder API-Calls ausgeführt
    console.log('Executing operations action:', action);

    // In der realen Implementation würde hier ein API-Call erfolgen
    // der die entsprechende SQL-Query ausführt oder die Operation triggert

    return {
      success: true,
      message: `Action executed: ${action}`,
      timestamp: new Date().toISOString()
    };
  }, []);

  const copyToClipboard = useCallback(async (content: string) => {
    try {
      await navigator.clipboard.writeText(content);
      return true;
    } catch (err) {
      console.error('Clipboard copy failed:', err);
      // Fallback for older browsers
      try {
        const textArea = document.createElement('textarea');
        textArea.value = content;
        textArea.style.position = 'fixed';
        textArea.style.opacity = '0';
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
        return true;
      } catch (fallbackErr) {
        console.error('Fallback clipboard copy failed:', fallbackErr);
        return false;
      }
    }
  }, []);

  return {
    // State
    guidance,
    isLoading,
    error,

    // Actions
    getGuidance,
    checkConfidence,
    getTemplates,
    executeOperationsAction,
    copyToClipboard,

    // Helpers
    clearError: () => setError(null),
    clearGuidance: () => setGuidance(null)
  };
};

export default useOperationsGuidance;