import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { ASC } from 'app/config/navigation.constants';
import { VisitComponent } from './list/visit.component';
import { VisitDetailComponent } from './detail/visit-detail.component';
import { VisitUpdateComponent } from './update/visit-update.component';
import VisitResolve from './route/visit-routing-resolve.service';

const visitRoute: Routes = [
  {
    path: '',
    component: VisitComponent,
    data: {
      defaultSort: 'id,' + ASC,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    component: VisitDetailComponent,
    resolve: {
      visit: VisitResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    component: VisitUpdateComponent,
    resolve: {
      visit: VisitResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    component: VisitUpdateComponent,
    resolve: {
      visit: VisitResolve,
    },
    canActivate: [UserRouteAccessService],
  },
];

export default visitRoute;
