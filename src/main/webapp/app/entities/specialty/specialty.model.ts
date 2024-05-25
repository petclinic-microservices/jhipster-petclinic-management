import { IVet } from 'app/entities/vet/vet.model';

export interface ISpecialty {
  id: number;
  name?: string | null;
  vets?: Pick<IVet, 'id' | 'firstName'>[] | null;
}

export type NewSpecialty = Omit<ISpecialty, 'id'> & { id: null };
