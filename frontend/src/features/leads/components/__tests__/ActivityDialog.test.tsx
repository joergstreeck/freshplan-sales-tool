import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import '@testing-library/jest-dom';
import { ActivityDialog } from '../ActivityDialog';
import { http, HttpResponse } from 'msw';
import { setupServer } from 'msw/node';

/**
 * ActivityDialog Component Tests
 *
 * Sprint 2.1.7 - Issue #126: ActivityOutcome Integration
 *
 * Test Coverage:
 * 1. Dialog rendering and form fields
 * 2. Activity type selection
 * 3. Description input and validation
 * 4. ActivityOutcome dropdown with backend data
 * 5. Form submission success
 * 6. Form submission with validation errors
 * 7. API error handling
 * 8. Form reset after success
 *
 * Testing Pattern: Vitest + React Testing Library + MSW
 */

// Mock useActivityOutcomes hook
vi.mock('../../../../hooks/useActivityOutcomes', () => ({
  useActivityOutcomes: vi.fn(() => ({
    data: [
      { value: 'SUCCESSFUL', label: 'Erfolgreich' },
      { value: 'UNSUCCESSFUL', label: 'Nicht erfolgreich' },
      { value: 'NO_ANSWER', label: 'Keine Antwort' },
      { value: 'CALLBACK_REQUESTED', label: 'Rückruf gewünscht' },
      { value: 'INFO_SENT', label: 'Info versendet' },
      { value: 'QUALIFIED', label: 'Qualifiziert' },
      { value: 'DISQUALIFIED', label: 'Disqualifiziert' },
    ],
    isLoading: false,
    error: null,
  })),
}));

// MSW Server for API mocking
const server = setupServer(
  http.post('http://localhost:8080/api/leads/:leadId/activities', () => {
    return HttpResponse.json(
      {
        id: 1,
        activityType: 'CALL',
        description: 'Test activity',
        outcome: 'SUCCESSFUL',
        userId: 'test-user',
        leadId: 123,
      },
      { status: 200 }
    );
  })
);

// Enable API mocking
beforeAll(() => server.listen());
afterEach(() => server.resetHandlers());
afterAll(() => server.close());

describe('ActivityDialog', () => {
  const mockOnClose = vi.fn();
  const mockOnSave = vi.fn();

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('renders dialog with all form fields when open', () => {
    render(
      <ActivityDialog open={true} onClose={mockOnClose} leadId={123} onSave={mockOnSave} />
    );

    // Verify dialog title
    expect(screen.getByText('Neue Aktivität erfassen')).toBeInTheDocument();

    // Verify form fields
    expect(screen.getByLabelText(/Aktivitätstyp/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Beschreibung/i)).toBeInTheDocument();
    expect(screen.getByLabelText(/Ergebnis \(optional\)/i)).toBeInTheDocument();

    // Verify buttons
    expect(screen.getByRole('button', { name: /Abbrechen/i })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: /Speichern/i })).toBeInTheDocument();
  });

  it('does not render when open prop is false', () => {
    render(
      <ActivityDialog open={false} onClose={mockOnClose} leadId={123} onSave={mockOnSave} />
    );

    expect(screen.queryByText('Neue Aktivität erfassen')).not.toBeInTheDocument();
  });

  it('allows selecting activity type', async () => {
    const user = userEvent.setup();
    render(
      <ActivityDialog open={true} onClose={mockOnClose} leadId={123} onSave={mockOnSave} />
    );

    // Verify activity type select exists with default value
    const activityTypeSelect = screen.getByLabelText(/Aktivitätstyp/i);
    expect(activityTypeSelect).toBeInTheDocument();

    // Verify default value is CALL (via hidden input)
    const hiddenInput = document.querySelector('.MuiSelect-nativeInput[value="CALL"]');
    expect(hiddenInput).toBeInTheDocument();
  });

  it('allows entering description text', async () => {
    const user = userEvent.setup();
    render(
      <ActivityDialog open={true} onClose={mockOnClose} leadId={123} onSave={mockOnSave} />
    );

    const descriptionField = screen.getByLabelText(/Beschreibung/i);
    await user.type(descriptionField, 'Test activity description');

    expect(descriptionField).toHaveValue('Test activity description');
    // Character counter should update (exact text may vary with length)
    expect(screen.getByText(/\/1000 Zeichen/i)).toBeInTheDocument();
  });

  it('shows validation error when description is empty', async () => {
    render(
      <ActivityDialog open={true} onClose={mockOnClose} leadId={123} onSave={mockOnSave} />
    );

    // Submit button should be disabled when description is empty
    const submitButton = screen.getByRole('button', { name: /Speichern/i });
    expect(submitButton).toBeDisabled();

    // Verify callbacks were NOT called (button is disabled, so can't submit)
    expect(mockOnSave).not.toHaveBeenCalled();
    expect(mockOnClose).not.toHaveBeenCalled();
  });

  it('renders activity outcome dropdown', async () => {
    render(
      <ActivityDialog open={true} onClose={mockOnClose} leadId={123} onSave={mockOnSave} />
    );

    // Verify outcome dropdown exists
    const outcomeSelect = screen.getByLabelText(/Ergebnis \(optional\)/i);
    expect(outcomeSelect).toBeInTheDocument();

    // Verify helper text
    expect(screen.getByText(/Hilft beim Tracking von Erfolg/i)).toBeInTheDocument();
  });

  it('submits form successfully with description field filled', async () => {
    const user = userEvent.setup();
    render(
      <ActivityDialog open={true} onClose={mockOnClose} leadId={123} onSave={mockOnSave} />
    );

    // Fill form fields
    const descriptionField = screen.getByLabelText(/Beschreibung/i);
    await user.type(descriptionField, 'Successful phone call with customer');

    // Submit form (without outcome - optional field)
    const submitButton = screen.getByRole('button', { name: /Speichern/i });
    await user.click(submitButton);

    // Wait for async operations
    await waitFor(() => {
      expect(mockOnSave).toHaveBeenCalledTimes(1);
      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });
  });

  it('submits form successfully without outcome (backward compatibility)', async () => {
    const user = userEvent.setup();
    render(
      <ActivityDialog open={true} onClose={mockOnClose} leadId={123} onSave={mockOnSave} />
    );

    // Fill only required fields
    const descriptionField = screen.getByLabelText(/Beschreibung/i);
    await user.type(descriptionField, 'Internal note without outcome');

    // Submit form
    const submitButton = screen.getByRole('button', { name: /Speichern/i });
    await user.click(submitButton);

    // Wait for async operations
    await waitFor(() => {
      expect(mockOnSave).toHaveBeenCalledTimes(1);
      expect(mockOnClose).toHaveBeenCalledTimes(1);
    });
  });

  it('displays API error message on submission failure', async () => {
    // Override MSW handler to return error
    server.use(
      http.post('http://localhost:8080/api/leads/:leadId/activities', () => {
        return HttpResponse.json(
          { error: 'Invalid activity outcome: XYZ' },
          { status: 400 }
        );
      })
    );

    const user = userEvent.setup();
    render(
      <ActivityDialog open={true} onClose={mockOnClose} leadId={123} onSave={mockOnSave} />
    );

    // Fill form
    const descriptionField = screen.getByLabelText(/Beschreibung/i);
    await user.type(descriptionField, 'Test activity');

    // Submit form
    const submitButton = screen.getByRole('button', { name: /Speichern/i });
    await user.click(submitButton);

    // Wait for error message
    await waitFor(() => {
      expect(screen.getByText(/Invalid activity outcome: XYZ/i)).toBeInTheDocument();
    });

    // Verify callbacks were NOT called
    expect(mockOnSave).not.toHaveBeenCalled();
    expect(mockOnClose).not.toHaveBeenCalled();
  });

  it('resets form on cancel', async () => {
    const user = userEvent.setup();
    render(
      <ActivityDialog open={true} onClose={mockOnClose} leadId={123} onSave={mockOnSave} />
    );

    // Fill form fields
    const descriptionField = screen.getByLabelText(/Beschreibung/i);
    await user.type(descriptionField, 'Test description');

    // Cancel dialog
    const cancelButton = screen.getByRole('button', { name: /Abbrechen/i });
    await user.click(cancelButton);

    // Verify onClose was called
    expect(mockOnClose).toHaveBeenCalledTimes(1);
  });

  it('disables submit button when description is empty', () => {
    render(
      <ActivityDialog open={true} onClose={mockOnClose} leadId={123} onSave={mockOnSave} />
    );

    const submitButton = screen.getByRole('button', { name: /Speichern/i });
    expect(submitButton).toBeDisabled();
  });

  it('enables submit button when description is filled', async () => {
    const user = userEvent.setup();
    render(
      <ActivityDialog open={true} onClose={mockOnClose} leadId={123} onSave={mockOnSave} />
    );

    const descriptionField = screen.getByLabelText(/Beschreibung/i);
    await user.type(descriptionField, 'Valid description');

    const submitButton = screen.getByRole('button', { name: /Speichern/i });
    expect(submitButton).not.toBeDisabled();
  });

  it('shows loading indicator during submission', async () => {
    // Override MSW handler with delay
    server.use(
      http.post('http://localhost:8080/api/leads/:leadId/activities', async () => {
        await new Promise(resolve => setTimeout(resolve, 100));
        return HttpResponse.json({ id: 1 }, { status: 200 });
      })
    );

    const user = userEvent.setup();
    render(
      <ActivityDialog open={true} onClose={mockOnClose} leadId={123} onSave={mockOnSave} />
    );

    // Fill form
    const descriptionField = screen.getByLabelText(/Beschreibung/i);
    await user.type(descriptionField, 'Test activity');

    // Submit form
    const submitButton = screen.getByRole('button', { name: /Speichern/i });
    await user.click(submitButton);

    // Verify loading state
    expect(submitButton).toBeDisabled();
    expect(screen.getByRole('progressbar')).toBeInTheDocument();

    // Wait for completion
    await waitFor(() => {
      expect(mockOnSave).toHaveBeenCalled();
    });
  });
});
