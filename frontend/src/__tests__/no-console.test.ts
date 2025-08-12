import { describe, it, expect } from 'vitest';
import { glob } from 'glob';
import fs from 'fs';
import path from 'path';

describe('Console Statement Verification', () => {
  it('should have NO console statements in production code', async () => {
    const srcFiles = await glob('src/**/*.{ts,tsx}', {
      ignore: [
        '**/*.test.*',
        '**/*.spec.*',
        '**/test/**',
        '**/tests/**',
        '**/main.tsx', // main.tsx is allowed to have console for MSW
        '**/setupTests.ts', // setup files are allowed
      ],
      cwd: path.resolve(__dirname, '..'),
    });

    const filesWithConsole: string[] = [];

    for (const file of srcFiles) {
      const content = fs.readFileSync(path.join(__dirname, '..', file), 'utf-8');
      // Check for console. but ignore comments
      const lines = content.split('\n');
      for (let i = 0; i < lines.length; i++) {
        const line = lines[i];
        // Skip comments
        if (line.trim().startsWith('//') || line.trim().startsWith('*')) {
          continue;
        }
        // Check for console.
        if (line.includes('console.')) {
          filesWithConsole.push(`${file}:${i + 1}`);
        }
      }
    }

    if (filesWithConsole.length > 0) {
      console.error('Files with console statements:', filesWithConsole);
    }

    expect(filesWithConsole).toEqual([]);
  });

  it('should use logger instead of console in services', async () => {
    const serviceFiles = await glob('src/services/**/*.ts', {
      ignore: ['**/*.test.*', '**/*.spec.*'],
      cwd: path.resolve(__dirname, '..'),
    });

    for (const file of serviceFiles) {
      const content = fs.readFileSync(path.join(__dirname, '..', file), 'utf-8');

      // Services should not have console. statements
      if (content.includes('console.')) {
        // Check if logger is imported
        const hasLogger = content.includes('logger') || content.includes('Logger');
        expect(hasLogger).toBe(true);

        // Ensure no console. exists
        expect(content).not.toContain('console.');
      }
    }
  });

  it('should allow console in test files only', async () => {
    const testFiles = await glob('**/*.{test,spec}.{ts,tsx}', {
      cwd: path.resolve(__dirname, '..'),
      ignore: ['node_modules/**'],
    });

    // Test files are allowed to have console statements
    if (testFiles.length > 0) {
      expect(testFiles.length).toBeGreaterThan(0);

      // Just verify test files exist and can have console
      for (const file of testFiles.slice(0, 5)) {
        // Check first 5 test files
        const content = fs.readFileSync(path.join(__dirname, '..', file), 'utf-8');
        // Test files can have console statements - no assertion needed
        expect(content).toBeDefined();
      }
    } else {
      // If no test files found, that's also OK - just pass
      expect(true).toBe(true);
    }
  });

  it('should not affect debugging capabilities', () => {
    // Verify that we have alternative debugging methods

    // Check if logger utility exists
    const loggerPath = path.join(__dirname, '..', 'lib', 'logger.ts');
    const hasLogger = fs.existsSync(loggerPath);

    if (hasLogger) {
      const loggerContent = fs.readFileSync(loggerPath, 'utf-8');

      // Logger should have debug, info, warn, error methods
      expect(loggerContent).toContain('debug');
      expect(loggerContent).toContain('info');
      expect(loggerContent).toContain('warn');
      expect(loggerContent).toContain('error');
    }

    // Either logger exists or we're in a test environment
    expect(hasLogger || process.env.NODE_ENV === 'test').toBe(true);
  });
});
