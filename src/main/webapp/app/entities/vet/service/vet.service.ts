import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import { isPresent } from 'app/core/util/operators';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { SearchWithPagination } from 'app/core/request/request.model';
import { IVet, NewVet } from '../vet.model';

export type PartialUpdateVet = Partial<IVet> & Pick<IVet, 'id'>;

export type EntityResponseType = HttpResponse<IVet>;
export type EntityArrayResponseType = HttpResponse<IVet[]>;

@Injectable({ providedIn: 'root' })
export class VetService {
  protected http = inject(HttpClient);
  protected applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/vets');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/vets/_search');

  create(vet: NewVet): Observable<EntityResponseType> {
    return this.http.post<IVet>(this.resourceUrl, vet, { observe: 'response' });
  }

  update(vet: IVet): Observable<EntityResponseType> {
    return this.http.put<IVet>(`${this.resourceUrl}/${this.getVetIdentifier(vet)}`, vet, { observe: 'response' });
  }

  partialUpdate(vet: PartialUpdateVet): Observable<EntityResponseType> {
    return this.http.patch<IVet>(`${this.resourceUrl}/${this.getVetIdentifier(vet)}`, vet, { observe: 'response' });
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http.get<IVet>(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<IVet[]>(this.resourceUrl, { params: options, observe: 'response' });
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: SearchWithPagination): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<IVet[]>(this.resourceSearchUrl, { params: options, observe: 'response' })
      .pipe(catchError(() => scheduled([new HttpResponse<IVet[]>()], asapScheduler)));
  }

  getVetIdentifier(vet: Pick<IVet, 'id'>): number {
    return vet.id;
  }

  compareVet(o1: Pick<IVet, 'id'> | null, o2: Pick<IVet, 'id'> | null): boolean {
    return o1 && o2 ? this.getVetIdentifier(o1) === this.getVetIdentifier(o2) : o1 === o2;
  }

  addVetToCollectionIfMissing<Type extends Pick<IVet, 'id'>>(vetCollection: Type[], ...vetsToCheck: (Type | null | undefined)[]): Type[] {
    const vets: Type[] = vetsToCheck.filter(isPresent);
    if (vets.length > 0) {
      const vetCollectionIdentifiers = vetCollection.map(vetItem => this.getVetIdentifier(vetItem));
      const vetsToAdd = vets.filter(vetItem => {
        const vetIdentifier = this.getVetIdentifier(vetItem);
        if (vetCollectionIdentifiers.includes(vetIdentifier)) {
          return false;
        }
        vetCollectionIdentifiers.push(vetIdentifier);
        return true;
      });
      return [...vetsToAdd, ...vetCollection];
    }
    return vetCollection;
  }
}
