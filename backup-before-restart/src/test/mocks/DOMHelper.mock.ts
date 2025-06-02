/**
 * DOMHelper Mock for Testing
 */

import { vi } from 'vitest';

export const createDOMHelperMock = () => {
  const mockElement = {
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    classList: {
      add: vi.fn(),
      remove: vi.fn(),
      toggle: vi.fn(),
      contains: vi.fn(() => false)
    },
    style: {
      display: '',
      setProperty: vi.fn()
    },
    innerHTML: '',
    textContent: '',
    value: '',
    checked: false,
    dataset: {},
    querySelector: vi.fn(),
    querySelectorAll: vi.fn(() => []),
    contains: vi.fn(() => true),
    closest: vi.fn(),
    appendChild: vi.fn(),
    removeChild: vi.fn(),
    children: [],
    id: '',
    className: '',
    getAttribute: vi.fn(),
    setAttribute: vi.fn(),
    removeAttribute: vi.fn()
  } as any;

  const domHelperMock: any = {
    $: vi.fn((selector) => {
      if (typeof selector === 'string') {
        return mockElement;
      }
      return selector || mockElement;
    }),
    $$: vi.fn(() => [mockElement]),
    on: vi.fn(() => {
      // Return unsubscribe function
      return vi.fn();
    }),
    off: vi.fn(),
    addClass: vi.fn(),
    removeClass: vi.fn(),
    toggleClass: vi.fn(),
    hasClass: vi.fn(() => false),
    show: vi.fn(),
    hide: vi.fn(),
    toggle: vi.fn(),
    attr: vi.fn(),
    data: vi.fn(),
    html: vi.fn(),
    text: vi.fn(),
    val: vi.fn(),
    create: vi.fn(() => mockElement),
    ready: vi.fn((callback) => callback()),
    debounce: vi.fn((fn: any) => fn),
    throttle: vi.fn((fn: any) => fn),
    getInstance: vi.fn((): any => domHelperMock)
  };

  return domHelperMock;
};

// Default mock
const DOMHelperMock = createDOMHelperMock();

export default DOMHelperMock;