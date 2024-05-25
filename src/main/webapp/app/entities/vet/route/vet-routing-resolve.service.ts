import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IVet } from '../vet.model';
import { VetService } from '../service/vet.service';

const vetResolve = (route: ActivatedRouteSnapshot): Observable<null | IVet> => {
  const id = route.params['id'];
  if (id) {
    return inject(VetService)
      .find(id)
      .pipe(
        mergeMap((vet: HttpResponse<IVet>) => {
          if (vet.body) {
            return of(vet.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default vetResolve;
