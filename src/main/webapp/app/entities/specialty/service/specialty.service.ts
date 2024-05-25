import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { SearchWithPagination } from 'app/core/request/request.model';
import { ISpecialty, NewSpecialty } from '../specialty.model';

export type PartialUpdateSpecialty = Partial<ISpecialty> & Pick<ISpecialty, 'id'>;

export type EntityResponseType = HttpResponse<ISpecialty>;
export type EntityArrayResponseType = HttpResponse<ISpecialty[]>;

@Injectable({ providedIn: 'root' })
export class SpecialtyService {
  protected http = inject(HttpClient);
  protected applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/specialties');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/specialties/_search');

  create(specialty: NewSpecialty): Observable<EntityResponseType> {
    return this.http.post<ISpecialty>(this.resourceUrl, specialty, { observe: 'response' });
  }

  update(specialty: ISpecialty): Observable<EntityResponseType> {
    return this.http.put<ISpecialty>(`${this.resourceUrl}/${this.getSpecialtyIdentifier(specialty)}`, specialty, { observe: 'response' });
  }

  partialUpdate(specialty: PartialUpdateSpecialty): Observable<EntityResponseType> {
    return this.http.patch<ISpecialty>(`${this.resourceUrl}/${this.getSpecialtyIdentifier(specialty)}`, specialty, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<ISpecialty>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<ISpecialty[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: SearchWithPagination): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<ISpecialty[]>(this.resourceSearchUrl, { params: options, observe: 'response' })
      .pipe(catchError(() => scheduled([new HttpResponse<ISpecialty[]>()], asapScheduler)));
  }

  getSpecialtyIdentifier(specialty: Pick<ISpecialty, 'id'>): number {
    return specialty.id;
  }

  compareSpecialty(o1: Pick<ISpecialty, 'id'> | null, o2: Pick<ISpecialty, 'id'> | null): boolean {
    return o1 && o2 ? this.getSpecialtyIdentifier(o1) === this.getSpecialtyIdentifier(o2) : o1 === o2;
  }

  addSpecialtyToCollectionIfMissing<Type extends Pick<ISpecialty, 'id'>>(
    specialtyCollection: Type[],
    ...specialtiesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const specialties: Type[] = specialtiesToCheck.filter(isPresent);
    if (specialties.length > 0) {
      const specialtyCollectionIdentifiers = specialtyCollection.map(specialtyItem => this.getSpecialtyIdentifier(specialtyItem));
      const specialtiesToAdd = specialties.filter(specialtyItem => {
        const specialtyIdentifier = this.getSpecialtyIdentifier(specialtyItem);
        if (specialtyCollectionIdentifiers.includes(specialtyIdentifier)) {
          return false;
        }
        specialtyCollectionIdentifiers.push(specialtyIdentifier);
        return true;
      });
      return [...specialtiesToAdd, ...specialtyCollection];
    }
    return specialtyCollection;
  }
}
