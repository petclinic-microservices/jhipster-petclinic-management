import { ISpecialty, NewSpecialty } from './specialty.model';

export const sampleWithRequiredData: ISpecialty = {
  id: 1390,
};

export const sampleWithPartialData: ISpecialty = {
  id: 20875,
};

export const sampleWithFullData: ISpecialty = {
  id: 7267,
  name: 'punctually',
};

export const sampleWithNewData: NewSpecialty = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
