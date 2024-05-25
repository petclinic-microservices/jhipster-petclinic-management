import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IVisit } from '../visit.model';
import { VisitService } from '../service/visit.service';

const visitResolve = (route: ActivatedRouteSnapshot): Observable<null | IVisit> => {
  const id = route.params['id'];
  if (id) {
    return inject(VisitService)
      .find(id)
      .pipe(
        mergeMap((visit: HttpResponse<IVisit>) => {
          if (visit.body) {
            return of(visit.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default visitResolve;
