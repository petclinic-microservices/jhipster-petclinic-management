import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { ISpecialty } from '../specialty.model';
import { SpecialtyService } from '../service/specialty.service';

const specialtyResolve = (route: ActivatedRouteSnapshot): Observable<null | ISpecialty> => {
  const id = route.params['id'];
  if (id) {
    return inject(SpecialtyService)
      .find(id)
      .pipe(
        mergeMap((specialty: HttpResponse<ISpecialty>) => {
          if (specialty.body) {
            return of(specialty.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default specialtyResolve;
