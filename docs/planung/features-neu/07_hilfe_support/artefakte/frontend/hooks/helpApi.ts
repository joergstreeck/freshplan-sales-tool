export type Persona = 'CHEF'|'BUYER'|'GF'|'REP';

export type HelpArticle = {
  id: string; slug?: string; module: '01'|'02'|'03'|'04'|'05'|'06'|'07';
  kind: 'FAQ'|'HowTo'|'Playbook'|'Video';
  title: string; summary?: string; locale: string; territory?: string; persona?: Persona;
  keywords?: string[];
  cta?: { type: 'NONE'|'LINK'|'GUIDED_FOLLOWUP'|'GUIDED_ROI'; href?: string };
};

export type Suggestion = { article: HelpArticle; confidence: number };

export type FollowUpPlanRequest = {
  accountId: string;
  contactRoles?: Persona[];
  followupOffsets?: string[]; // e.g., 'P3D','P7D'
  note?: string;
};
export type FollowUpPlanResponse = { createdActivities: { id: string; dueDate: string; kind: string }[] };

export type RoiQuickCheckRequest = {
  accountId: string;
  hoursSavedPerDay: number;
  hourlyRate: number;
  workingDaysPerMonth?: number;
  wasteReductionPerMonth?: number;
};
export type RoiQuickCheckResponse = { paybackMonths: number; monthlySavings: number; usedCalculator: 'INTERNAL'|'EXTERNAL' };

export async function getMenu(params: { module?: HelpArticle['module']; persona?: Persona; territory?: string; limit?: number }): Promise<HelpArticle[]>{
  const qs = new URLSearchParams();
  if (params.module) qs.set('module', params.module);
  if (params.persona) qs.set('persona', params.persona);
  if (params.territory) qs.set('territory', params.territory);
  if (params.limit) qs.set('limit', String(params.limit));
  const r = await fetch(`/api/help/menu?${qs.toString()}`);
  if (!r.ok) throw new Error('menu failed');
  const j = await r.json();
  return j.items as HelpArticle[];
}

export async function getSuggest(params: { context: string; module?: HelpArticle['module']; top?: number; sessionMinutes?: number; sessionId?: string }): Promise<{ items: Suggestion[], budgetLeft?: number }>{
  const qs = new URLSearchParams();
  qs.set('context', params.context);
  if (params.module) qs.set('module', params.module);
  if (params.top) qs.set('top', String(params.top));
  if (typeof params.sessionMinutes === 'number') qs.set('sessionMinutes', String(params.sessionMinutes));
  const r = await fetch(`/api/help/suggest?${qs.toString()}`, { headers: params.sessionId ? { 'X-Session-Id': params.sessionId } : {} });
  if (!r.ok) throw new Error('suggest failed');
  return { items: (await r.json()).items as Suggestion[], budgetLeft: Number(r.headers.get('X-Nudge-Budget-Left')||'0') };
}

export async function postFollowUp(body: FollowUpPlanRequest): Promise<FollowUpPlanResponse>{
  const r = await fetch('/api/help/guided/follow-up', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body) });
  if (!r.ok) throw new Error('follow-up failed');
  return r.json() as Promise<FollowUpPlanResponse>;
}

export async function postRoiQuick(body: RoiQuickCheckRequest): Promise<RoiQuickCheckResponse>{
  const r = await fetch('/api/help/guided/roi-check', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body) });
  if (!r.ok) throw new Error('roi failed');
  return r.json() as Promise<RoiQuickCheckResponse>;
}
