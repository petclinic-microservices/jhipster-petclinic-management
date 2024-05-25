export interface IOwner {
  id: number;
  firstName?: string | null;
  lastName?: string | null;
  address?: string | null;
  city?: string | null;
  telephone?: string | null;
}

export type NewOwner = Omit<IOwner, 'id'> & { id: null };
