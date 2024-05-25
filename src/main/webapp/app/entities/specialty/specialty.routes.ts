import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { SpecialtyComponent } from './list/specialty.component';
import { SpecialtyDetailComponent } from './detail/specialty-detail.component';
import { SpecialtyUpdateComponent } from './update/specialty-update.component';
import SpecialtyResolve from './route/specialty-routing-resolve.service';

const specialtyRoute: Routes = [
  {
    path: '',
    component: SpecialtyComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: SpecialtyDetailComponent,
    resolve: {
      specialty: SpecialtyResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: SpecialtyUpdateComponent,
    resolve: {
      specialty: SpecialtyResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: SpecialtyUpdateComponent,
    resolve: {
      specialty: SpecialtyResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default specialtyRoute;
