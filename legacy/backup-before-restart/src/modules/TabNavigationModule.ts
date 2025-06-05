/**
 * Tab Navigation Module
 * Handles tab switching and navigation state
 */

import Module from '../core/Module';
import { useStore } from '../store';
import type { TabName } from '../types';

export default class TabNavigationModule extends Module {
  private tabButtons: Map<TabName, HTMLElement> = new Map();
  private tabPanels: Map<TabName, HTMLElement> = new Map();
  private progressBar: HTMLElement | null = null;

  constructor() {
    super('tabs');
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
    
    // Update state
    useStore.getState().setCurrentTab(tab);
    
    // Update URL
    if (updateHistory) {
      const url = new URL(window.location.href);
      url.searchParams.set('tab', tab);
      window.history.pushState({ tab }, '', url);
    }
    
    // Emit event
    this.emit('switched', tab);
    
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
      this.switchTab(tab);
    }
  }

  getProgress(): number {
    return this.calculateProgress();
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
   * Cleanup module resources
   */
  cleanup(): void {
    // Module cleanup is handled by base class
  }
}