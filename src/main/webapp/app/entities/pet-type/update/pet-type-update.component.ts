import { Component, inject, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IPetType } from '../pet-type.model';
import { PetTypeService } from '../service/pet-type.service';
import { PetTypeFormService, PetTypeFormGroup } from './pet-type-form.service';

@Component({
  standalone: true,
  selector: 'jhi-pet-type-update',
  templateUrl: './pet-type-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class PetTypeUpdateComponent implements OnInit {
  isSaving = false;
  petType: IPetType | null = null;

  protected petTypeService = inject(PetTypeService);
  protected petTypeFormService = inject(PetTypeFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: PetTypeFormGroup = this.petTypeFormService.createPetTypeFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ petType }) => {
      this.petType = petType;
      if (petType) {
        this.updateForm(petType);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const petType = this.petTypeFormService.getPetType(this.editForm);
    if (petType.id !== null) {
      this.subscribeToSaveResponse(this.petTypeService.update(petType));
    } else {
      this.subscribeToSaveResponse(this.petTypeService.create(petType));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPetType>>): void {
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

  protected updateForm(petType: IPetType): void {
    this.petType = petType;
    this.petTypeFormService.resetForm(this.editForm, petType);
  }
}
