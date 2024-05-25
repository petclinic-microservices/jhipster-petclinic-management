import { IVetSpecialty, NewVetSpecialty } from './vet-specialty.model';

export const sampleWithRequiredData: IVetSpecialty = {
  id: 3673,
};

export const sampleWithPartialData: IVetSpecialty = {
  id: 5943,
};

export const sampleWithFullData: IVetSpecialty = {
  id: 5355,
};

export const sampleWithNewData: NewVetSpecialty = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
