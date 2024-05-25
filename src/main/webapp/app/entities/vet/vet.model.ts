import { ISpecialty } from 'app/entities/specialty/specialty.model';

export interface IVet {
  id: number;
  firstName?: string | null;
  lastName?: string | null;
  specialties?: Pick<ISpecialty, 'id' | 'name'>[] | null;
}

export type NewVet = Omit<IVet, 'id'> & { id: null };
