import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IPetType } from '../pet-type.model';
import { PetTypeService } from '../service/pet-type.service';

@Component({
  standalone: true,
  templateUrl: './pet-type-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class PetTypeDeleteDialogComponent {
  petType?: IPetType;

  protected petTypeService = inject(PetTypeService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.petTypeService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
