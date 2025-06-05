/**
 * Test Setup with Mocks
 */

import { vi } from 'vitest';
import DOMHelperMock from './DOMHelper.mock';

// Mock DOMHelper globally
vi.mock('@/core/DOMHelper', () => ({
  default: DOMHelperMock
}));

vi.mock('../core/DOMHelper', () => ({
  default: DOMHelperMock
}));

// Mock window.jspdf
Object.assign(global.window, {
  jspdf: {
    jsPDF: vi.fn(() => ({
      text: vi.fn(),
      setFontSize: vi.fn(),
      setTextColor: vi.fn(),
      setDrawColor: vi.fn(),
      setLineWidth: vi.fn(),
      line: vi.fn(),
      rect: vi.fn(),
      setFillColor: vi.fn(),
      addPage: vi.fn(),
      save: vi.fn(),
      output: vi.fn(() => 'mock-pdf-data')
    }))
  }
});

// Mock document.body for FreshPlanApp error handling
if (!document.body) {
  document.body = document.createElement('body');
}

// Mock appendChild to avoid DOM errors
const originalAppendChild = document.body.appendChild;
document.body.appendChild = vi.fn((node) => {
  // For error divs created by FreshPlanApp, return the node without actually appending
  if (node && typeof node === 'object' && 'innerHTML' in node) {
    return node;
  }
  // For real DOM nodes, use original
  try {
    return originalAppendChild.call(document.body, node);
  } catch {
    return node;
  }
}) as any;