import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe } from 'app/shared/date';
import { IVet } from '../vet.model';

@Component({
  standalone: true,
  selector: 'jhi-vet-detail',
  templateUrl: './vet-detail.component.html',
  imports: [SharedModule, RouterModule, DurationPipe, FormatMediumDatetimePipe, FormatMediumDatePipe],
})
export class VetDetailComponent {
  vet = input<IVet | null>(null);

  previousState(): void {
    window.history.back();
  }
}
