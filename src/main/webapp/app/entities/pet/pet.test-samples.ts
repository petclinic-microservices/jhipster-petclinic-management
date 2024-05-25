import dayjs from 'dayjs/esm';

import { IPet, NewPet } from './pet.model';

export const sampleWithRequiredData: IPet = {
  id: 21150,
};

export const sampleWithPartialData: IPet = {
  id: 30498,
};

export const sampleWithFullData: IPet = {
  id: 17204,
  name: 'er toreador',
  birthDate: dayjs('2024-05-24'),
};

export const sampleWithNewData: NewPet = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
