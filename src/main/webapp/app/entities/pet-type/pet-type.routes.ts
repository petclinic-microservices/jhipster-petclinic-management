import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { PetTypeComponent } from './list/pet-type.component';
import { PetTypeDetailComponent } from './detail/pet-type-detail.component';
import { PetTypeUpdateComponent } from './update/pet-type-update.component';
import PetTypeResolve from './route/pet-type-routing-resolve.service';

const petTypeRoute: Routes = [
  {
    path: '',
    component: PetTypeComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: PetTypeDetailComponent,
    resolve: {
      petType: PetTypeResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: PetTypeUpdateComponent,
    resolve: {
      petType: PetTypeResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: PetTypeUpdateComponent,
    resolve: {
      petType: PetTypeResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default petTypeRoute;
