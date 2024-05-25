import { Component, inject, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IVetSpecialty } from '../vet-specialty.model';
import { VetSpecialtyService } from '../service/vet-specialty.service';
import { VetSpecialtyFormService, VetSpecialtyFormGroup } from './vet-specialty-form.service';

@Component({
  standalone: true,
  selector: 'jhi-vet-specialty-update',
  templateUrl: './vet-specialty-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class VetSpecialtyUpdateComponent implements OnInit {
  isSaving = false;
  vetSpecialty: IVetSpecialty | null = null;

  protected vetSpecialtyService = inject(VetSpecialtyService);
  protected vetSpecialtyFormService = inject(VetSpecialtyFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: VetSpecialtyFormGroup = this.vetSpecialtyFormService.createVetSpecialtyFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ vetSpecialty }) => {
      this.vetSpecialty = vetSpecialty;
      if (vetSpecialty) {
        this.updateForm(vetSpecialty);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const vetSpecialty = this.vetSpecialtyFormService.getVetSpecialty(this.editForm);
    this.subscribeToSaveResponse(this.vetSpecialtyService.create(vetSpecialty));
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IVetSpecialty>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(vetSpecialty: IVetSpecialty): void {
    this.vetSpecialty = vetSpecialty;
    this.vetSpecialtyFormService.resetForm(this.editForm, vetSpecialty);
  }
}
