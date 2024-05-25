import { IPetType, NewPetType } from './pet-type.model';

export const sampleWithRequiredData: IPetType = {
  id: 23005,
};

export const sampleWithPartialData: IPetType = {
  id: 20008,
  name: 'along whenever',
};

export const sampleWithFullData: IPetType = {
  id: 23655,
  name: 'afore speedily memorise',
};

export const sampleWithNewData: NewPetType = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
