import { Component, inject, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IVet } from 'app/entities/vet/vet.model';
import { VetService } from 'app/entities/vet/service/vet.service';
import { ISpecialty } from '../specialty.model';
import { SpecialtyService } from '../service/specialty.service';
import { SpecialtyFormService, SpecialtyFormGroup } from './specialty-form.service';

@Component({
  standalone: true,
  selector: 'jhi-specialty-update',
  templateUrl: './specialty-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class SpecialtyUpdateComponent implements OnInit {
  isSaving = false;
  specialty: ISpecialty | null = null;

  vetsSharedCollection: IVet[] = [];

  protected specialtyService = inject(SpecialtyService);
  protected specialtyFormService = inject(SpecialtyFormService);
  protected vetService = inject(VetService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: SpecialtyFormGroup = this.specialtyFormService.createSpecialtyFormGroup();

  compareVet = (o1: IVet | null, o2: IVet | null): boolean => this.vetService.compareVet(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ specialty }) => {
      this.specialty = specialty;
      if (specialty) {
        this.updateForm(specialty);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const specialty = this.specialtyFormService.getSpecialty(this.editForm);
    if (specialty.id !== null) {
      this.subscribeToSaveResponse(this.specialtyService.update(specialty));
    } else {
      this.subscribeToSaveResponse(this.specialtyService.create(specialty));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ISpecialty>>): void {
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

  protected updateForm(specialty: ISpecialty): void {
    this.specialty = specialty;
    this.specialtyFormService.resetForm(this.editForm, specialty);

    this.vetsSharedCollection = this.vetService.addVetToCollectionIfMissing<IVet>(this.vetsSharedCollection, ...(specialty.vets ?? []));
  }

  protected loadRelationshipsOptions(): void {
    this.vetService
      .query()
      .pipe(map((res: HttpResponse<IVet[]>) => res.body ?? []))
      .pipe(map((vets: IVet[]) => this.vetService.addVetToCollectionIfMissing<IVet>(vets, ...(this.specialty?.vets ?? []))))
      .subscribe((vets: IVet[]) => (this.vetsSharedCollection = vets));
  }
}
