/**
 * Lead Import API Client - Sprint 2.1.8 Phase 2
 *
 * Self-Service Lead-Import API für den 4-Schritt Import-Wizard.
 *
 * Endpoints:
 * - POST /api/leads/import/upload - Datei hochladen
 * - POST /api/leads/import/{uploadId}/preview - Vorschau erstellen
 * - POST /api/leads/import/{uploadId}/execute - Import ausführen
 * - GET /api/leads/import/quota - Quota-Info abrufen
 *
 * @module leadImportApi
 * @since Sprint 2.1.8
 */

const BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080';

function authHeaders(): HeadersInit {
  // Dev Mode: Use dev-auth-user from sessionStorage (bypass Keycloak)
  const devUser = sessionStorage.getItem('dev-auth-user');
  if (devUser) {
    const user = JSON.parse(devUser);
    const mockToken = `dev.${user.id}.${user.username}`;
    return { Authorization: `Bearer ${mockToken}` };
  }

  // Production: JWT aus localStorage (Keycloak)
  const token = localStorage.getItem('token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

// ============================================================================
// Types
// ============================================================================

/** Schritt 1: Upload Response */
export interface ImportUploadResponse {
  uploadId: string;
  columns: string[];
  rowCount: number;
  autoMapping: Record<string, string>;
  fileName: string;
  fileSize: number;
}

/** Schritt 2: Mapping Request */
export interface ImportMappingRequest {
  mapping: Record<string, string>;
}

/** Schritt 3: Preview Response */
export interface ImportPreviewResponse {
  uploadId: string;
  validation: ValidationSummary;
  previewRows: PreviewRow[];
  errors: ValidationError[];
  duplicates: DuplicateMatch[];
  quotaCheck: QuotaCheck;
}

export interface ValidationSummary {
  totalRows: number;
  validRows: number;
  errorRows: number;
  duplicateRows: number;
}

export interface PreviewRow {
  row: number;
  data: Record<string, string>;
  status: 'VALID' | 'ERROR' | 'DUPLICATE';
  matchId?: number;
}

export interface ValidationError {
  row: number;
  column: string;
  message: string;
  value: string;
}

export interface DuplicateMatch {
  row: number;
  existingLeadId: number;
  existingCompanyName: string;
  type: 'HARD_COLLISION' | 'SOFT_COLLISION';
  similarity: number;
}

export interface QuotaCheck {
  approved: boolean;
  message: string;
  currentOpenLeads: number;
  maxOpenLeads: number;
  remainingCapacity: number;
}

/** Schritt 4: Execute Request */
export interface ImportExecuteRequest {
  mapping: Record<string, string>;
  duplicateAction: 'SKIP' | 'CREATE';
  source?: string;
  ignoreErrors: boolean;
}

/** Schritt 4: Execute Response */
export interface ImportExecuteResponse {
  success: boolean;
  importId: string | null;
  imported: number;
  skipped: number;
  errors: number;
  status: 'COMPLETED' | 'PENDING_APPROVAL' | 'FAILED';
  message: string;
}

/** Quota Info */
export interface QuotaInfo {
  currentOpenLeads: number;
  maxOpenLeads: number;
  importsToday: number;
  maxImportsPerDay: number;
  maxLeadsPerImport: number;
  remainingCapacity: number;
  canImport: boolean;
}

/** RFC7807 Problem Details */
export interface Problem {
  title?: string;
  status?: number;
  detail?: string;
  type?: string;
}

// ============================================================================
// API Functions
// ============================================================================

/**
 * Wandelt eine Response in ein Problem um (RFC7807 Error Handling)
 */
async function toProblem(res: Response): Promise<Problem> {
  try {
    return await res.json();
  } catch {
    return { title: res.statusText, status: res.status };
  }
}

/**
 * Schritt 1: Datei hochladen
 *
 * Lädt eine CSV/Excel-Datei hoch und gibt Spalten + Auto-Mapping zurück.
 *
 * @param file Die hochzuladende Datei
 * @returns Upload-Response mit uploadId, Spalten und Auto-Mapping
 */
export async function uploadFile(file: File): Promise<ImportUploadResponse> {
  const formData = new FormData();
  formData.append('file', file);

  const res = await fetch(`${BASE}/api/leads/import/upload`, {
    method: 'POST',
    headers: {
      ...authHeaders(),
      // Content-Type wird automatisch gesetzt für FormData
    },
    body: formData,
    credentials: 'include',
  });

  if (!res.ok) throw await toProblem(res);
  return res.json();
}

/**
 * Schritt 2/3: Vorschau erstellen
 *
 * Erstellt eine Vorschau mit Validierung und Duplikat-Check.
 *
 * @param uploadId Upload-ID aus Schritt 1
 * @param mapping Spalten-Zuordnung
 * @returns Preview-Response mit Validierung und Duplikaten
 */
export async function createPreview(
  uploadId: string,
  mapping: Record<string, string>
): Promise<ImportPreviewResponse> {
  const res = await fetch(`${BASE}/api/leads/import/${uploadId}/preview`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...authHeaders(),
    },
    body: JSON.stringify({ mapping }),
    credentials: 'include',
  });

  if (!res.ok) throw await toProblem(res);
  return res.json();
}

/**
 * Schritt 4: Import ausführen
 *
 * Führt den Import mit den gewählten Optionen aus.
 *
 * @param uploadId Upload-ID aus Schritt 1
 * @param request Import-Request mit Mapping und Optionen
 * @returns Import-Ergebnis
 */
export async function executeImport(
  uploadId: string,
  request: ImportExecuteRequest
): Promise<ImportExecuteResponse> {
  const res = await fetch(`${BASE}/api/leads/import/${uploadId}/execute`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
      ...authHeaders(),
    },
    body: JSON.stringify(request),
    credentials: 'include',
  });

  if (!res.ok) throw await toProblem(res);
  return res.json();
}

/**
 * Quota-Info abrufen
 *
 * Gibt die aktuellen Import-Limits und Kapazitäten zurück.
 *
 * @returns Quota-Informationen
 */
export async function getQuotaInfo(): Promise<QuotaInfo> {
  const res = await fetch(`${BASE}/api/leads/import/quota`, {
    method: 'GET',
    headers: {
      Accept: 'application/json',
      ...authHeaders(),
    },
    credentials: 'include',
  });

  if (!res.ok) throw await toProblem(res);
  return res.json();
}

// ============================================================================
// Lead Fields Configuration
// ============================================================================

/**
 * Verfügbare Lead-Felder für das Mapping
 */
export const LEAD_FIELDS = [
  { key: 'companyName', label: 'Firmenname', required: true },
  { key: 'email', label: 'E-Mail', required: false },
  { key: 'phone', label: 'Telefon', required: false },
  { key: 'city', label: 'Stadt', required: false },
  { key: 'postalCode', label: 'PLZ', required: false },
  { key: 'street', label: 'Straße', required: false },
  { key: 'businessType', label: 'Branche', required: false },
  { key: 'contactPerson', label: 'Ansprechpartner', required: false },
  { key: 'contactPosition', label: 'Position', required: false },
  { key: 'website', label: 'Website', required: false },
  { key: 'notes', label: 'Notizen', required: false },
  // Sprint 2.1.8: Historical Lead Import
  { key: 'originalCreatedAt', label: 'Erstelldatum', required: false },
] as const;

export type LeadFieldKey = (typeof LEAD_FIELDS)[number]['key'];
