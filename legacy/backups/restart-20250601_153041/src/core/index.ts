/**
 * Core module exports
 */

export { default as EventBus } from './EventBus';
export { default as DOMHelper } from './DOMHelper';
export { default as Module } from './Module';
export { default as StateManager } from './StateManager';
export type { StateChangeEvent } from './StateManagerLegacy';
export type { DeepPartial } from '../store/index';
export type { ModuleState, CleanupFn, ModuleLifecycle } from './Module';