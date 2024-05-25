import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpResponse } from '@angular/common/http';
import { map, Observable, asapScheduler, scheduled } from 'rxjs';

import { catchError } from 'rxjs/operators';

import dayjs from 'dayjs/esm';

import { isPresent } from 'app/core/util/operators';
import { DATE_FORMAT } from 'app/config/input.constants';
import { ApplicationConfigService } from 'app/core/config/application-config.service';
import { createRequestOption } from 'app/core/request/request-util';
import { SearchWithPagination } from 'app/core/request/request.model';
import { IVisit, NewVisit } from '../visit.model';

export type PartialUpdateVisit = Partial<IVisit> & Pick<IVisit, 'id'>;

type RestOf<T extends IVisit | NewVisit> = Omit<T, 'visitDate'> & {
  visitDate?: string | null;
};

export type RestVisit = RestOf<IVisit>;

export type NewRestVisit = RestOf<NewVisit>;

export type PartialUpdateRestVisit = RestOf<PartialUpdateVisit>;

export type EntityResponseType = HttpResponse<IVisit>;
export type EntityArrayResponseType = HttpResponse<IVisit[]>;

@Injectable({ providedIn: 'root' })
export class VisitService {
  protected http = inject(HttpClient);
  protected applicationConfigService = inject(ApplicationConfigService);

  protected resourceUrl = this.applicationConfigService.getEndpointFor('api/visits');
  protected resourceSearchUrl = this.applicationConfigService.getEndpointFor('api/visits/_search');

  create(visit: NewVisit): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(visit);
    return this.http.post<RestVisit>(this.resourceUrl, copy, { observe: 'response' }).pipe(map(res => this.convertResponseFromServer(res)));
  }

  update(visit: IVisit): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(visit);
    return this.http
      .put<RestVisit>(`${this.resourceUrl}/${this.getVisitIdentifier(visit)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  partialUpdate(visit: PartialUpdateVisit): Observable<EntityResponseType> {
    const copy = this.convertDateFromClient(visit);
    return this.http
      .patch<RestVisit>(`${this.resourceUrl}/${this.getVisitIdentifier(visit)}`, copy, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  find(id: number): Observable<EntityResponseType> {
    return this.http
      .get<RestVisit>(`${this.resourceUrl}/${id}`, { observe: 'response' })
      .pipe(map(res => this.convertResponseFromServer(res)));
  }

  query(req?: any): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http
      .get<RestVisit[]>(this.resourceUrl, { params: options, observe: 'response' })
      .pipe(map(res => this.convertResponseArrayFromServer(res)));
  }

  delete(id: number): Observable<HttpResponse<{}>> {
    return this.http.delete(`${this.resourceUrl}/${id}`, { observe: 'response' });
  }

  search(req: SearchWithPagination): Observable<EntityArrayResponseType> {
    const options = createRequestOption(req);
    return this.http.get<RestVisit[]>(this.resourceSearchUrl, { params: options, observe: 'response' }).pipe(
      map(res => this.convertResponseArrayFromServer(res)),

      catchError(() => scheduled([new HttpResponse<IVisit[]>()], asapScheduler)),
    );
  }

  getVisitIdentifier(visit: Pick<IVisit, 'id'>): number {
    return visit.id;
  }

  compareVisit(o1: Pick<IVisit, 'id'> | null, o2: Pick<IVisit, 'id'> | null): boolean {
    return o1 && o2 ? this.getVisitIdentifier(o1) === this.getVisitIdentifier(o2) : o1 === o2;
  }

  addVisitToCollectionIfMissing<Type extends Pick<IVisit, 'id'>>(
    visitCollection: Type[],
    ...visitsToCheck: (Type | null | undefined)[]
  ): Type[] {
    const visits: Type[] = visitsToCheck.filter(isPresent);
    if (visits.length > 0) {
      const visitCollectionIdentifiers = visitCollection.map(visitItem => this.getVisitIdentifier(visitItem));
      const visitsToAdd = visits.filter(visitItem => {
        const visitIdentifier = this.getVisitIdentifier(visitItem);
        if (visitCollectionIdentifiers.includes(visitIdentifier)) {
          return false;
        }
        visitCollectionIdentifiers.push(visitIdentifier);
        return true;
      });
      return [...visitsToAdd, ...visitCollection];
    }
    return visitCollection;
  }

  protected convertDateFromClient<T extends IVisit | NewVisit | PartialUpdateVisit>(visit: T): RestOf<T> {
    return {
      ...visit,
      visitDate: visit.visitDate?.format(DATE_FORMAT) ?? null,
    };
  }

  protected convertDateFromServer(restVisit: RestVisit): IVisit {
    return {
      ...restVisit,
      visitDate: restVisit.visitDate ? dayjs(restVisit.visitDate) : undefined,
    };
  }

  protected convertResponseFromServer(res: HttpResponse<RestVisit>): HttpResponse<IVisit> {
    return res.clone({
      body: res.body ? this.convertDateFromServer(res.body) : null,
    });
  }

  protected convertResponseArrayFromServer(res: HttpResponse<RestVisit[]>): HttpResponse<IVisit[]> {
    return res.clone({
      body: res.body ? res.body.map(item => this.convertDateFromServer(item)) : null,
    });
  }
}
