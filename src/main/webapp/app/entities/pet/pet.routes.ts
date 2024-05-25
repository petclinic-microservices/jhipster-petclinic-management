import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { PetComponent } from './list/pet.component';
import { PetDetailComponent } from './detail/pet-detail.component';
import { PetUpdateComponent } from './update/pet-update.component';
import PetResolve from './route/pet-routing-resolve.service';

const petRoute: Routes = [
  {
    path: '',
    component: PetComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: PetDetailComponent,
    resolve: {
      pet: PetResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: PetUpdateComponent,
    resolve: {
      pet: PetResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: PetUpdateComponent,
    resolve: {
      pet: PetResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default petRoute;
