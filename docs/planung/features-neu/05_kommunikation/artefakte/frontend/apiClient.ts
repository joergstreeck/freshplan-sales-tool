export class ApiError extends Error{ constructor(public status:number, message:string, public detail?:string){super(message);}}
async function parseProblem(r:Response){ try{return await r.json();}catch{return{};} }
export async function getJSON<T>(url:string):Promise<T>{ const r=await fetch(url); if(!r.ok){const p=await parseProblem(r); throw new ApiError(r.status,p.title||'Request failed',p.detail);} return r.json(); }
export async function postJSON<TReq,TRes>(url:string, body:TReq, headers:Record<string,string>={}):Promise<TRes>{
  const r=await fetch(url,{method:'POST',headers:{'Content-Type':'application/json',...headers},body:JSON.stringify(body)});
  if(!r.ok){const p=await parseProblem(r); throw new ApiError(r.status,p.title||'Request failed',p.detail);} return r.json();
}