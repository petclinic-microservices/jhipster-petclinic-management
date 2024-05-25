import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { VetComponent } from './list/vet.component';
import { VetDetailComponent } from './detail/vet-detail.component';
import { VetUpdateComponent } from './update/vet-update.component';
import VetResolve from './route/vet-routing-resolve.service';

const vetRoute: Routes = [
  {
    path: '',
    component: VetComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: VetDetailComponent,
    resolve: {
      vet: VetResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: VetUpdateComponent,
    resolve: {
      vet: VetResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: VetUpdateComponent,
    resolve: {
      vet: VetResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default vetRoute;
