import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe } from 'app/shared/date';
import { IVisit } from '../visit.model';

@Component({
  standalone: true,
  selector: 'jhi-visit-detail',
  templateUrl: './visit-detail.component.html',
  imports: [SharedModule, RouterModule, DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe],
})
export class VisitDetailComponent {
  visit = input<IVisit | null>(null);

  previousState(): void {
    window.history.back();
  }
}
