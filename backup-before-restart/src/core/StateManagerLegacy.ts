/**
 * Legacy StateManager types for backward compatibility
 */

import type { AppState } from '../types';

export type StateChangeEvent<T = any> = {
  path: string;
  oldValue: T;
  newValue: T;
  state: AppState;
};