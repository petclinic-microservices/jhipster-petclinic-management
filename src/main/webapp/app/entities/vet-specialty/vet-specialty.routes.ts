import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { VetSpecialtyComponent } from './list/vet-specialty.component';
import { VetSpecialtyDetailComponent } from './detail/vet-specialty-detail.component';
import { VetSpecialtyUpdateComponent } from './update/vet-specialty-update.component';
import VetSpecialtyResolve from './route/vet-specialty-routing-resolve.service';

const vetSpecialtyRoute: Routes = [
  {
    path: '',
    component: VetSpecialtyComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: VetSpecialtyDetailComponent,
    resolve: {
      vetSpecialty: VetSpecialtyResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: VetSpecialtyUpdateComponent,
    resolve: {
      vetSpecialty: VetSpecialtyResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default vetSpecialtyRoute;
