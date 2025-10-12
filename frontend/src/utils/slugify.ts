/**
 * Slugify Utility - URL-freundliche Slugs generieren
 *
 * Konvertiert Text in URL-sichere Slugs:
 * - "Müller GmbH" → "mueller-gmbh"
 * - "Café & Restaurant" → "cafe-restaurant"
 * - "123 Test Inc." → "123-test-inc"
 */
export function slugify(text: string): string {
  return (
    text
      .toString()
      .toLowerCase()
      .trim()
      // Umlaute ersetzen
      .replace(/ä/g, 'ae')
      .replace(/ö/g, 'oe')
      .replace(/ü/g, 'ue')
      .replace(/ß/g, 'ss')
      // Sonderzeichen entfernen/ersetzen
      .replace(/[^\w\s-]/g, '') // Alles außer Buchstaben, Zahlen, Leerzeichen, Bindestriche
      .replace(/\s+/g, '-') // Leerzeichen → Bindestrich
      .replace(/--+/g, '-') // Mehrfache Bindestriche → einzelner Bindestrich
      .replace(/^-+/, '') // Führende Bindestriche entfernen
      .replace(/-+$/, '')
  ); // Trailing Bindestriche entfernen
}

/**
 * Lead-URL mit Slug + ID generieren
 *
 * @param companyName - Firmenname
 * @param leadId - Lead-ID
 * @returns URL-Pfad z.B. "/lead-generation/leads/mueller-gmbh-123"
 */
export function generateLeadUrl(companyName: string, leadId: number): string {
  const slug = slugify(companyName);
  return `/lead-generation/leads/${slug}-${leadId}`;
}

/**
 * Lead-ID aus Slug-URL extrahieren
 *
 * @param slugParam - URL-Parameter z.B. "mueller-gmbh-123"
 * @returns Lead-ID als String z.B. "123"
 */
export function extractLeadIdFromSlug(slugParam: string): string {
  const lastDashIndex = slugParam.lastIndexOf('-');
  if (lastDashIndex === -1) {
    // Fallback: Ganzer String ist die ID (für alte URLs)
    return slugParam;
  }
  return slugParam.substring(lastDashIndex + 1);
}
