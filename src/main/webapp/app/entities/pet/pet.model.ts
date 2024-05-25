import dayjs from 'dayjs/esm';
import { IPetType } from 'app/entities/pet-type/pet-type.model';
import { IOwner } from 'app/entities/owner/owner.model';

export interface IPet {
  id: number;
  name?: string | null;
  birthDate?: dayjs.Dayjs | null;
  type?: Pick<IPetType, 'id' | 'name'> | null;
  owner?: Pick<IOwner, 'id' | 'lastName'> | null;
}

export type NewPet = Omit<IPet, 'id'> & { id: null };
