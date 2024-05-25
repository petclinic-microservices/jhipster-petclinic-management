import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { OwnerComponent } from './list/owner.component';
import { OwnerDetailComponent } from './detail/owner-detail.component';
import { OwnerUpdateComponent } from './update/owner-update.component';
import OwnerResolve from './route/owner-routing-resolve.service';

const ownerRoute: Routes = [
  {
    path: '',
    component: OwnerComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: OwnerDetailComponent,
    resolve: {
      owner: OwnerResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: OwnerUpdateComponent,
    resolve: {
      owner: OwnerResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: OwnerUpdateComponent,
    resolve: {
      owner: OwnerResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default ownerRoute;
