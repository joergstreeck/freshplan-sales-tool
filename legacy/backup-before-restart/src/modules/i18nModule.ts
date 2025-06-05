/**
 * Internationalization Module - TypeScript version
 * Handles language switching and translations
 */

import Module from '../core/Module';
import { useStore } from '../store';
import { translations } from '../utils/translations';
import type { Language, TranslationParams } from '../types';

interface I18nState {
  currentLanguage: Language;
  availableLanguages: Language[];
  autoUpdate: boolean;
}

export default class i18nModule extends Module {
  private updateInterval: NodeJS.Timeout | null = null;
  private observedElements: WeakSet<Element> = new WeakSet();
  private mutationObserver: MutationObserver | null = null;

  constructor() {
    super('i18n');
  }

  async setup(): Promise<void> {
    // Initialize with saved language or default
    const savedLanguage = (localStorage.getItem('freshplan_language') || 'de') as Language;
    this.setModuleState<I18nState>({
      currentLanguage: savedLanguage,
      availableLanguages: ['de', 'en'],
      autoUpdate: true
    });

    // Update language selector if exists
    const selector = this.dom.$('#languageSelect') as HTMLSelectElement;
    if (selector) {
      selector.value = savedLanguage;
    }

    // Initial translation
    this.translateAll();

    // Setup auto-update if enabled
    if (this.getModuleState<I18nState>().autoUpdate) {
      this.startAutoUpdate();
    }
  }

  bindEvents(): void {
    // Language selector
    this.on('#languageSelect', 'change', (e: Event) => {
      const select = e.target as HTMLSelectElement;
      this.changeLanguage(select.value as Language);
    });

    // Observe DOM mutations for new elements
    this.setupMutationObserver();
  }

  subscribeToState(): void {
    // React to language changes
    useStore.subscribe(
      (state) => state.app.language,
      (language) => {
        this.translateAll();
        this.emit('changed', { language });
        this.events.emit('app:language:changed', language);
      }
    );
  }

  /**
   * Change current language
   */
  changeLanguage(lang: Language): void {
    if (!this.getModuleState<I18nState>().availableLanguages.includes(lang)) {
      console.warn(`Language '${lang}' not available`);
      return;
    }

    this.setModuleState('currentLanguage', lang);
    localStorage.setItem('freshplan_language', lang);
    
    // Update global state
    useStore.getState().setLanguage(lang);
  }

  /**
   * Get translation for key
   */
  getTranslation(key: string, params: TranslationParams = {}): string {
    const lang = this.getModuleState<I18nState>().currentLanguage;
    
    // Navigate through translation object
    const keys = key.split('.');
    let value: any = translations[lang];
    
    for (const k of keys) {
      if (value && typeof value === 'object' && k in value) {
        value = value[k];
      } else {
        // Fallback to German if key not found
        value = this.getFallbackTranslation(key);
        break;
      }
    }

    // If still not found, return key
    if (typeof value !== 'string') {
      console.warn(`Translation not found: ${key}`);
      return key;
    }

    // Replace parameters
    return this.interpolate(value, params);
  }

  /**
   * Get fallback translation (German)
   */
  private getFallbackTranslation(key: string): string {
    const keys = key.split('.');
    let value: any = translations.de;
    
    for (const k of keys) {
      if (value && typeof value === 'object' && k in value) {
        value = value[k];
      } else {
        return key;
      }
    }
    
    return typeof value === 'string' ? value : key;
  }

  /**
   * Interpolate parameters in translation string
   */
  private interpolate(str: string, params: TranslationParams): string {
    return str.replace(/\{(\w+)\}/g, (match, key) => {
      return params.hasOwnProperty(key) ? String(params[key]) : match;
    });
  }

  /**
   * Translate all elements in document
   */
  translateAll(): void {
    const elements = this.dom.$$('[data-i18n]');
    
    elements.forEach(element => {
      this.translateElement(element as HTMLElement);
    });

    // Update document title
    document.title = this.getTranslation('app.title');
  }

  /**
   * Translate single element
   */
  private translateElement(element: HTMLElement): void {
    const key = element.getAttribute('data-i18n');
    if (!key) return;

    // Get parameters if any
    const params: TranslationParams = {};
    const paramAttrs = Array.from(element.attributes).filter(attr => 
      attr.name.startsWith('data-i18n-param-')
    );
    
    paramAttrs.forEach(attr => {
      const paramName = attr.name.replace('data-i18n-param-', '');
      params[paramName] = attr.value;
    });

    const translation = this.getTranslation(key, params);
    
    // Apply translation based on element type
    if (element instanceof HTMLInputElement || element instanceof HTMLTextAreaElement) {
      if (element.type === 'button' || element.type === 'submit') {
        element.value = translation;
      } else {
        element.placeholder = translation;
      }
    } else if (element instanceof HTMLOptionElement) {
      element.textContent = translation;
    } else {
      // Check if element has only text content
      const hasOnlyText = Array.from(element.childNodes).every(node => 
        node.nodeType === Node.TEXT_NODE
      );
      
      if (hasOnlyText) {
        element.textContent = translation;
      } else {
        // Update only the first text node
        const textNode = Array.from(element.childNodes).find(node => 
          node.nodeType === Node.TEXT_NODE && node.textContent?.trim()
        );
        if (textNode) {
          textNode.textContent = translation;
        }
      }
    }

    // Mark as observed
    this.observedElements.add(element);
  }

  /**
   * Setup mutation observer for dynamic content
   */
  private setupMutationObserver(): void {
    const observer = new MutationObserver((mutations) => {
      mutations.forEach(mutation => {
        // Check added nodes
        mutation.addedNodes.forEach(node => {
          if (node.nodeType === Node.ELEMENT_NODE) {
            const element = node as Element;
            // Translate the node if it has data-i18n
            if (element.hasAttribute && element.hasAttribute('data-i18n')) {
              this.translateElement(element as HTMLElement);
            }
            
            // Translate children
            if (element.querySelectorAll) {
              const children = element.querySelectorAll('[data-i18n]');
              children.forEach(child => this.translateElement(child as HTMLElement));
            }
          }
        });

        // Check for attribute changes
        if (mutation.type === 'attributes' && mutation.attributeName === 'data-i18n') {
          this.translateElement(mutation.target as HTMLElement);
        }
      });
    });

    observer.observe(document.body, {
      childList: true,
      subtree: true,
      attributes: true,
      attributeFilter: ['data-i18n']
    });

    // Store observer for cleanup
    this.mutationObserver = observer;
  }

  /**
   * Start auto-update interval
   */
  private startAutoUpdate(): void {
    // Update every 5 seconds for dynamic content
    this.updateInterval = this.setInterval(() => {
      this.translateAll();
    }, 5000);
  }

  /**
   * Stop auto-update interval
   */
  private stopAutoUpdate(): void {
    if (this.updateInterval) {
      clearInterval(this.updateInterval);
      this.updateInterval = null;
    }
  }

  /**
   * Get current language
   */
  getCurrentLanguage(): Language {
    return this.getModuleState<I18nState>().currentLanguage;
  }

  /**
   * Check if language is RTL
   */
  isRTL(lang?: Language): boolean {
    lang = lang || this.getCurrentLanguage();
    return ['ar', 'he', 'fa', 'ur'].includes(lang);
  }


  /**
   * Public API
   */
  
  t(key: string, params?: TranslationParams): string {
    return this.getTranslation(key, params);
  }

  setLanguage(lang: Language): void {
    this.changeLanguage(lang);
  }

  getLanguage(): Language {
    return this.getCurrentLanguage();
  }

  getAvailableLanguages(): Language[] {
    return this.getModuleState<I18nState>().availableLanguages;
  }

  isLanguageAvailable(lang: string): lang is Language {
    return this.getAvailableLanguages().includes(lang as Language);
  }

  /**
   * Cleanup module resources
   */
  cleanup(): void {
    this.stopAutoUpdate();
    
    if (this.mutationObserver) {
      this.mutationObserver.disconnect();
    }
    
    this.observedElements = new WeakSet();
    // Module cleanup is handled by base class
  }
}