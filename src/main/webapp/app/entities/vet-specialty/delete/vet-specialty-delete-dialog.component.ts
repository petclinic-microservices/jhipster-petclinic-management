import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IVetSpecialty } from '../vet-specialty.model';
import { VetSpecialtyService } from '../service/vet-specialty.service';

@Component({
  standalone: true,
  templateUrl: './vet-specialty-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class VetSpecialtyDeleteDialogComponent {
  vetSpecialty?: IVetSpecialty;

  protected vetSpecialtyService = inject(VetSpecialtyService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.vetSpecialtyService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
