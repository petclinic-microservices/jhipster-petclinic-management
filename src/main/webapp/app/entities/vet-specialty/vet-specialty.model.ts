export interface IVetSpecialty {
  id: number;
}

export type NewVetSpecialty = Omit<IVetSpecialty, 'id'> & { id: null };
