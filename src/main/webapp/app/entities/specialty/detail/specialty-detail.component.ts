import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe } from 'app/shared/date';
import { ISpecialty } from '../specialty.model';

@Component({
  standalone: true,
  selector: 'jhi-specialty-detail',
  templateUrl: './specialty-detail.component.html',
  imports: [SharedModule, RouterModule, DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe],
})
export class SpecialtyDetailComponent {
  specialty = input<ISpecialty | null>(null);

  previousState(): void {
    window.history.back();
  }
}
