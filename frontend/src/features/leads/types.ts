export type Lead = {
  id: string;
  name: string;
  email?: string | null;
  createdAt?: string;
};

export type Problem = {
  type?: string;
  title?: string;
  detail?: string;
  status?: number;
  errors?: Record<string, string[]>;
};
