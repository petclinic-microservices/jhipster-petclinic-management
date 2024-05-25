import { IOwner, NewOwner } from './owner.model';

export const sampleWithRequiredData: IOwner = {
  id: 10664,
};

export const sampleWithPartialData: IOwner = {
  id: 8977,
  address: 'if because frayed',
  city: 'Melanyfort',
};

export const sampleWithFullData: IOwner = {
  id: 4202,
  firstName: 'Dan',
  lastName: 'Jenkins',
  address: 'spotted',
  city: 'Goldnerfield',
  telephone: '1-406-653-3397 x7536',
};

export const sampleWithNewData: NewOwner = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
