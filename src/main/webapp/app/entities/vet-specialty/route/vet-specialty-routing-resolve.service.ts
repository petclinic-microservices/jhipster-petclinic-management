import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { of, EMPTY, Observable } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IVetSpecialty } from '../vet-specialty.model';
import { VetSpecialtyService } from '../service/vet-specialty.service';

const vetSpecialtyResolve = (route: ActivatedRouteSnapshot): Observable<null | IVetSpecialty> => {
  const id = route.params['id'];
  if (id) {
    return inject(VetSpecialtyService)
      .find(id)
      .pipe(
        mergeMap((vetSpecialty: HttpResponse<IVetSpecialty>) => {
          if (vetSpecialty.body) {
            return of(vetSpecialty.body);
          } else {
            inject(Router).navigate(['404']);
            return EMPTY;
          }
        }),
      );
  }
  return of(null);
};

export default vetSpecialtyResolve;
