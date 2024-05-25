import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IPetType } from '../pet-type.model';
import { PetTypeService } from '../service/pet-type.service';

const petTypeResolve = (route: ActivatedRouteSnapshot): Observable<null | IPetType> => {
  const id = route.params['id'];
  if (id) {
    return inject(PetTypeService)
      .find(id)
      .pipe(
        mergeMap((petType: HttpResponse<IPetType>) => {
          if (petType.body) {
            return of(petType.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default petTypeResolve;
