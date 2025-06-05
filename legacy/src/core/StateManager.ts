/**
 * StateManager - Now uses Zustand under the hood
 * Maintains backward compatibility with legacy code
 */

import StoreAdapter from '../store/StoreAdapter';

// Re-export the adapter as StateManager for backward compatibility
export default StoreAdapter;

// Export types for backward compatibility
export type { StateChangeEvent } from './StateManagerLegacy';
export type { DeepPartial } from '../store/index';