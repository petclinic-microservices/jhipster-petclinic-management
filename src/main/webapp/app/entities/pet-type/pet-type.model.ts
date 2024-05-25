export interface IPetType {
  id: number;
  name?: string | null;
}

export type NewPetType = Omit<IPetType, 'id'> & { id: null };
