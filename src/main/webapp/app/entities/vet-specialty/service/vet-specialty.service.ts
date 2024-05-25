import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { Search } from 'app/core/request/request.model';
import { IVetSpecialty, NewVetSpecialty } from '../vet-specialty.model';

export type EntityResponseType = HttpResponse<IVetSpecialty>;
export type EntityArrayResponseType = HttpResponse<IVetSpecialty[]>;

@Injectable({ providedIn: 'root' })
export class VetSpecialtyService {
  protected http = inject(HttpClient);
  protected applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/vet-specialties');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/vet-specialties/_search');

  create(vetSpecialty: NewVetSpecialty): Observable<EntityResponseType> {
    return this.http.post<IVetSpecialty>(this.resourceUrl, vetSpecialty, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IVetSpecialty>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IVetSpecialty[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: Search): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IVetSpecialty[]>(this.resourceSearchUrl, { params: options, observe: 'response' })
      .pipe(catchError(() => scheduled([new HttpResponse<IVetSpecialty[]>()], asapScheduler)));
  }

  getVetSpecialtyIdentifier(vetSpecialty: Pick<IVetSpecialty, 'id'>): number {
    return vetSpecialty.id;
  }

  compareVetSpecialty(o1: Pick<IVetSpecialty, 'id'> | null, o2: Pick<IVetSpecialty, 'id'> | null): boolean {
    return o1 && o2 ? this.getVetSpecialtyIdentifier(o1) === this.getVetSpecialtyIdentifier(o2) : o1 === o2;
  }

  addVetSpecialtyToCollectionIfMissing<Type extends Pick<IVetSpecialty, 'id'>>(
    vetSpecialtyCollection: Type[],
    ...vetSpecialtiesToCheck: (Type | null | undefined)[]
  ): Type[] {
    const vetSpecialties: Type[] = vetSpecialtiesToCheck.filter(isPresent);
    if (vetSpecialties.length > 0) {
      const vetSpecialtyCollectionIdentifiers = vetSpecialtyCollection.map(vetSpecialtyItem =>
        this.getVetSpecialtyIdentifier(vetSpecialtyItem),
      );
      const vetSpecialtiesToAdd = vetSpecialties.filter(vetSpecialtyItem => {
        const vetSpecialtyIdentifier = this.getVetSpecialtyIdentifier(vetSpecialtyItem);
        if (vetSpecialtyCollectionIdentifiers.includes(vetSpecialtyIdentifier)) {
          return false;
        }
        vetSpecialtyCollectionIdentifiers.push(vetSpecialtyIdentifier);
        return true;
      });
      return [...vetSpecialtiesToAdd, ...vetSpecialtyCollection];
    }
    return vetSpecialtyCollection;
  }
}
