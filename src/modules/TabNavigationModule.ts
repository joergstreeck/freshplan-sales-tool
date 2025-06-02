/**
 * Tab Navigation Module
 * Handles tab switching and navigation state
 */

import Module from '../core/Module';
import { useStore } from '../store';
import type { TabName } from '../types';
import { TAB_EVENTS } from '../constants/events';

// Re-export TAB_EVENTS for backward compatibility
export { TAB_EVENTS };

export default class TabNavigationModule extends Module {
  private legacyMode: boolean = false;
  private tabButtons: Map<TabName, HTMLElement> = new Map();
  private tabPanels: Map<TabName, HTMLElement> = new Map();
  private progressBar: HTMLElement | null = null;

  constructor() {
    super('tabs');
    
    // Check if we're in legacy mode (called from legacy-script.ts)
    this.legacyMode = !!(window as any).__LEGACY_SCRIPT_ACTIVE;
  }

  async setup(): Promise<void> {
    // Cache DOM elements
    this.cacheElements();
    
    // Set initial tab from state
    const currentTab = useStore.getState().ui.currentTab;
    this.activateTab(currentTab);
    
    // Update progress
    this.updateProgress();
  }

  bindEvents(): void {
    // Tab button clicks
    this.on(document, 'click', '.nav-tab', (e: Event) => {
      const button = (e.target as Element).closest('.nav-tab') as HTMLElement;
      const tab = button?.dataset.tab as TabName;
      
      if (tab && this.isValidTab(tab)) {
        this.switchTab(tab);
      }
    });

    // Keyboard navigation
    this.on(document, 'keydown', '.nav-tabs', (e: Event) => {
      const keyEvent = e as KeyboardEvent;
      if (keyEvent.key === 'ArrowLeft' || keyEvent.key === 'ArrowRight') {
        e.preventDefault();
        this.handleKeyboardNavigation(keyEvent.key);
      }
    });

    // Handle browser back/forward
    this.on(window, 'popstate', () => {
      const tab = this.getTabFromURL();
      if (tab && this.isValidTab(tab)) {
        this.switchTab(tab, false); // Don't push to history
      }
    });
    
    // Listen for legacy tab switch events (Phase 3.2.1)
    if (this.legacyMode) {
      this.on(window as any, TAB_EVENTS.SWITCH, (e: CustomEvent) => {
        const { tab, source } = e.detail;
        
        // Only handle if from legacy source and valid tab
        if (source === 'legacy' && tab && this.isValidTab(tab)) {
          // Prevent recursive events
          if (!e.defaultPrevented) {
            e.preventDefault();
            this.switchTab(tab);
          }
        }
      });
    }
  }

  subscribeToState(): void {
    // React to tab changes from other sources
    useStore.subscribe(
      (state) => state.ui.currentTab,
      (tab) => {
        this.activateTab(tab);
        this.updateProgress();
      }
    );

    // React to customer data changes for progress
    useStore.subscribe(
      (state) => state.customer.data,
      () => this.updateProgress()
    );

    // React to profile generation
    useStore.subscribe(
      (state) => state.profile.data,
      () => this.updateProgress()
    );
  }

  /**
   * Cache DOM elements
   */
  private cacheElements(): void {
    // Tab buttons
    this.dom.$$('.nav-tab').forEach(button => {
      const tab = (button as HTMLElement).dataset.tab as TabName;
      if (tab) {
        this.tabButtons.set(tab, button as HTMLElement);
      }
    });

    // Tab panels
    this.dom.$$('.tab-panel').forEach(panel => {
      const tab = panel.id as TabName;
      if (tab) {
        this.tabPanels.set(tab, panel as HTMLElement);
      }
    });

    // Progress bar
    this.progressBar = this.dom.$('.progress-bar');
  }

  /**
   * Switch to a tab
   */
  private switchTab(tab: TabName, updateHistory: boolean = true): void {
    const currentTab = useStore.getState().ui.currentTab;
    
    if (tab === currentTab) return;
    
    // Emit before switch event (can be cancelled)
    const beforeSwitchEvent = new CustomEvent(TAB_EVENTS.BEFORE_SWITCH, {
      detail: { tab, previousTab: currentTab },
      cancelable: true
    });
    
    // Emit on module
    this.emit(TAB_EVENTS.BEFORE_SWITCH, { tab, previousTab: currentTab });
    
    // Also emit on window for legacy compatibility
    if (this.legacyMode || (window as any).__LEGACY_LISTENERS) {
      window.dispatchEvent(beforeSwitchEvent);
      
      // Check if event was cancelled
      if (beforeSwitchEvent.defaultPrevented) {
        console.log(`[TabNavigationModule] Tab switch to '${tab}' was cancelled`);
        return;
      }
    }
    
    // Update state
    useStore.getState().setCurrentTab(tab);
    
    // Update URL
    if (updateHistory) {
      const url = new URL(window.location.href);
      url.searchParams.set('tab', tab);
      window.history.pushState({ tab }, '', url);
    }
    
    // Emit events for both new and legacy systems
    this.emit('switched', tab); // Original event
    this.emit(TAB_EVENTS.SWITCHED, { tab, previousTab: currentTab }); // New standardized event
    
    // Also emit on window for legacy compatibility
    if (this.legacyMode || (window as any).__LEGACY_LISTENERS) {
      window.dispatchEvent(new CustomEvent(TAB_EVENTS.SWITCHED, {
        detail: { tab, previousTab: currentTab }
      }));
    }
    
    // Analytics
    this.trackTabSwitch(tab);
  }

  /**
   * Activate tab UI
   */
  private activateTab(tab: TabName): void {
    // Update buttons
    this.tabButtons.forEach((button, tabName) => {
      const isActive = tabName === tab;
      this.dom.toggleClass(button, 'active', isActive);
      button.setAttribute('aria-selected', String(isActive));
    });

    // Update panels
    this.tabPanels.forEach((panel, tabName) => {
      const isActive = tabName === tab;
      this.dom.toggleClass(panel, 'active', isActive);
      panel.setAttribute('aria-hidden', String(!isActive));
    });

    // Focus management
    const activeButton = this.tabButtons.get(tab);
    if (activeButton && document.activeElement?.classList.contains('nav-tab')) {
      activeButton.focus();
    }
  }

  /**
   * Handle keyboard navigation
   */
  private handleKeyboardNavigation(key: string): void {
    const tabs = Array.from(this.tabButtons.keys());
    const currentTab = useStore.getState().ui.currentTab;
    const currentIndex = tabs.indexOf(currentTab);
    
    let newIndex: number;
    if (key === 'ArrowLeft') {
      newIndex = currentIndex > 0 ? currentIndex - 1 : tabs.length - 1;
    } else {
      newIndex = currentIndex < tabs.length - 1 ? currentIndex + 1 : 0;
    }
    
    const newTab = tabs[newIndex];
    if (newTab) {
      this.switchTab(newTab);
    }
  }

  /**
   * Update progress bar
   */
  private updateProgress(): void {
    if (!this.progressBar) return;
    
    const progress = this.calculateProgress();
    
    this.progressBar.style.width = `${progress}%`;
    this.progressBar.setAttribute('aria-valuenow', String(progress));
    
    // Update color based on progress
    this.dom.removeClass(this.progressBar, 'low', 'medium', 'high', 'complete');
    
    if (progress === 100) {
      this.dom.addClass(this.progressBar, 'complete');
    } else if (progress >= 75) {
      this.dom.addClass(this.progressBar, 'high');
    } else if (progress >= 50) {
      this.dom.addClass(this.progressBar, 'medium');
    } else {
      this.dom.addClass(this.progressBar, 'low');
    }
    
    // Emit progress update event
    this.emit(TAB_EVENTS.PROGRESS_UPDATE, { progress, steps: this.getProgressSteps() });
    
    // Also emit on window for legacy compatibility
    if (this.legacyMode || (window as any).__LEGACY_LISTENERS) {
      window.dispatchEvent(new CustomEvent(TAB_EVENTS.PROGRESS_UPDATE, {
        detail: { progress, steps: this.getProgressSteps() }
      }));
    }
  }

  /**
   * Calculate overall progress
   */
  private calculateProgress(): number {
    const state = useStore.getState();
    let steps = 0;
    let completed = 0;
    
    // Step 1: Calculator used
    steps++;
    if (state.calculator.calculation !== null) {
      completed++;
    }
    
    // Step 2: Customer data entered
    steps++;
    if (state.customer.data && state.customer.data.companyName) {
      completed++;
    }
    
    // Step 3: Customer data valid
    steps++;
    if (state.customer.isValid && !state.customer.isDirty) {
      completed++;
    }
    
    // Step 4: Profile generated
    steps++;
    if (state.profile.data !== null) {
      completed++;
    }
    
    // Step 5: Settings configured
    steps++;
    if (state.settings.salesperson.name && state.settings.salesperson.email) {
      completed++;
    }
    
    return Math.round((completed / steps) * 100);
  }

  /**
   * Get tab from URL
   */
  private getTabFromURL(): TabName | null {
    const params = new URLSearchParams(window.location.search);
    const tab = params.get('tab') as TabName;
    return this.isValidTab(tab) ? tab : null;
  }

  /**
   * Check if tab is valid
   */
  private isValidTab(tab: any): tab is TabName {
    const validTabs: TabName[] = ['demonstrator', 'customer', 'profile', 'offer', 'settings'];
    return validTabs.includes(tab);
  }

  /**
   * Track tab switch for analytics
   */
  private trackTabSwitch(tab: TabName): void {
    // In a real app, this would send to analytics
    console.log(`Analytics: Tab switched to ${tab}`);
    
    // Could integrate with Google Analytics, Mixpanel, etc.
    if ((window as any).gtag) {
      (window as any).gtag('event', 'tab_switch', {
        tab_name: tab,
        timestamp: Date.now()
      });
    }
  }

  /**
   * Public API
   */
  
  getCurrentTab(): TabName {
    return useStore.getState().ui.currentTab;
  }

  navigateTo(tab: TabName): void {
    if (this.isValidTab(tab)) {
      // Check if tab is accessible
      if (!this.isTabAccessible(tab)) {
        // Emit access denied event
        const reason = this.getAccessDeniedReason(tab);
        this.emit(TAB_EVENTS.ACCESS_DENIED, { tab, reason });
        
        // Also emit on window for legacy compatibility
        if (this.legacyMode || (window as any).__LEGACY_LISTENERS) {
          window.dispatchEvent(new CustomEvent(TAB_EVENTS.ACCESS_DENIED, {
            detail: { tab, reason }
          }));
        }
        
        console.warn(`[TabNavigationModule] Access denied to tab '${tab}': ${reason}`);
        return;
      }
      
      this.switchTab(tab);
    }
  }
  
  // Backward compatible methods with deprecation warnings
  
  /**
   * @deprecated Use navigateTo() instead
   */
  showTab(tabId: string): void {
    console.warn('[TabNavigationModule] showTab() is deprecated. Use navigateTo() instead.');
    
    // Convert legacy tab IDs if needed
    const mappedTab = this.mapLegacyTabId(tabId);
    if (mappedTab && this.isValidTab(mappedTab)) {
      this.navigateTo(mappedTab);
    }
  }
  
  /**
   * @deprecated Use getCurrentTab() instead
   */
  getActiveTab(): string {
    console.warn('[TabNavigationModule] getActiveTab() is deprecated. Use getCurrentTab() instead.');
    return this.getCurrentTab();
  }
  
  /**
   * @deprecated Use module event system instead
   */
  onTabChange(callback: (tab: string) => void): () => void {
    console.warn('[TabNavigationModule] onTabChange() is deprecated. Use module event system instead.');
    
    // Create wrapper for legacy callback
    const handler = (event: CustomEvent) => {
      callback(event.detail.tab);
    };
    
    // Listen to both module events and window events
    this.on(window as any, TAB_EVENTS.SWITCHED, handler);
    
    // Return unsubscribe function
    return () => {
      window.removeEventListener(TAB_EVENTS.SWITCHED, handler as any);
    };
  }
  
  /**
   * Maps legacy tab IDs to current TabName types
   */
  private mapLegacyTabId(tabId: string): TabName | null {
    const mapping: Record<string, TabName> = {
      'demonstrator': 'demonstrator',
      'calculator': 'demonstrator', // Legacy alias
      'customer': 'customer',
      'profile': 'profile',
      'offer': 'offer',
      'settings': 'settings'
    };
    
    return mapping[tabId] || null;
  }

  getProgress(): number {
    return this.calculateProgress();
  }
  
  /**
   * Get detailed progress steps information
   */
  getProgressSteps(): Record<string, boolean> {
    const state = useStore.getState();
    return {
      calculatorUsed: state.calculator.calculation !== null,
      customerDataEntered: !!(state.customer.data && state.customer.data.companyName),
      customerDataValid: state.customer.isValid && !state.customer.isDirty,
      profileGenerated: state.profile.data !== null,
      settingsConfigured: !!(state.settings.salesperson.name && state.settings.salesperson.email)
    };
  }

  isTabAccessible(tab: TabName): boolean {
    // Business logic for tab accessibility
    const state = useStore.getState();
    
    switch (tab) {
      case 'demonstrator':
        return true; // Always accessible
        
      case 'customer':
        return true; // Always accessible
        
      case 'profile':
        // Requires customer data
        return !!(state.customer.data && state.customer.data.companyName);
        
      case 'offer':
        // Requires profile
        return state.profile.data !== null;
        
      case 'settings':
        return true; // Always accessible
        
      default:
        return false;
    }
  }
  
  /**
   * Get reason why tab access is denied
   */
  private getAccessDeniedReason(tab: TabName): string {
    const state = useStore.getState();
    
    switch (tab) {
      case 'profile':
        if (!state.customer.data || !state.customer.data.companyName) {
          return 'Kundendaten müssen zuerst eingegeben werden';
        }
        break;
        
      case 'offer':
        if (state.profile.data === null) {
          return 'Kundenprofil muss zuerst generiert werden';
        }
        break;
    }
    
    return 'Zugriff verweigert';
  }

  /**
   * Enable legacy mode for backward compatibility
   */
  enableLegacyMode(): void {
    this.legacyMode = true;
    (window as any).__LEGACY_LISTENERS = true;
  }
  
  /**
   * Cleanup module resources
   */
  cleanup(): void {
    // Module cleanup is handled by base class
    delete (window as any).__LEGACY_LISTENERS;
  }
}