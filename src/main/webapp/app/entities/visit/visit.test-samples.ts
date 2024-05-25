import dayjs from 'dayjs/esm';

import { IVisit, NewVisit } from './visit.model';

export const sampleWithRequiredData: IVisit = {
  id: 23973,
};

export const sampleWithPartialData: IVisit = {
  id: 32396,
  visitDate: dayjs('2024-05-25'),
  description: 'within ew',
};

export const sampleWithFullData: IVisit = {
  id: 29199,
  visitDate: dayjs('2024-05-24'),
  description: 'untried',
};

export const sampleWithNewData: NewVisit = {
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
