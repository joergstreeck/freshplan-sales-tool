// Typed API client with RFC7807 error handling
export class ApiError extends Error {
  status: number; detail?: string; errors?: Array<{field:string; message:string}>;
  constructor(status:number, message:string, detail?:string, errors?:Array<{field:string; message:string}>) {
    super(message); this.status=status; this.detail=detail; this.errors=errors;
  }
}
async function parseProblem(r: Response) {
  try { return await r.json(); } catch { return {}; }
}
export async function getJSON<T>(url: string): Promise<T> {
  const res = await fetch(url);
  if (!res.ok) {
    const prob = await parseProblem(res);
    throw new ApiError(res.status, prob.title || 'Request failed', prob.detail, prob.errors);
  }
  return res.json() as Promise<T>;
}
export async function postJSON<TReq, TRes>(url: string, body: TReq): Promise<TRes> {
  const res = await fetch(url, { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body) });
  if (!res.ok) {
    const prob = await parseProblem(res);
    throw new ApiError(res.status, prob.title || 'Request failed', prob.detail, prob.errors);
  }
  return res.json() as Promise<TRes>;
}
