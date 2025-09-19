export type Scope = { tenantId?:string|null; territory?:string|null; accountId?:string|null; contactRole?:'CHEF'|'BUYER'|null; contactId?:string|null; userId?:string|null };
export type EffectiveSettings = { blob: Record<string,any>; etag: string; computedAt: string };
