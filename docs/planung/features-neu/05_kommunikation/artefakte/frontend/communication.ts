export type Channel = 'EMAIL'|'PHONE'|'MEETING'|'NOTE';
export interface ThreadItem { id:string; customerId:string; subject:string; territory:string; channel:Channel; participants:string[]; lastMessageAt:string; unreadCount:number; etag:string; }
export interface ThreadPage { items: ThreadItem[]; nextCursor?: string|null; }
