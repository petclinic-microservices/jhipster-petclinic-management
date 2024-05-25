import { IVet, NewVet } from './vet.model';

export const sampleWithRequiredData: IVet = {
  id: 7532,
};

export const sampleWithPartialData: IVet = {
  id: 25567,
  lastName: 'Kreiger',
};

export const sampleWithFullData: IVet = {
  id: 13482,
  firstName: 'Willow',
  lastName: 'Friesen',
};

export const sampleWithNewData: NewVet = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
