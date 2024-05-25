import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { ISpecialty } from '../specialty.model';
import { SpecialtyService } from '../service/specialty.service';

@Component({
  standalone: true,
  templateUrl: './specialty-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class SpecialtyDeleteDialogComponent {
  specialty?: ISpecialty;

  protected specialtyService = inject(SpecialtyService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.specialtyService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
