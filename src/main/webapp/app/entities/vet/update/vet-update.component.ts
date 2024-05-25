import { Component, inject, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ISpecialty } from 'app/entities/specialty/specialty.model';
import { SpecialtyService } from 'app/entities/specialty/service/specialty.service';
import { IVet } from '../vet.model';
import { VetService } from '../service/vet.service';
import { VetFormService, VetFormGroup } from './vet-form.service';

@Component({
  standalone: true,
  selector: 'jhi-vet-update',
  templateUrl: './vet-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class VetUpdateComponent implements OnInit {
  isSaving = false;
  vet: IVet | null = null;

  specialtiesSharedCollection: ISpecialty[] = [];

  protected vetService = inject(VetService);
  protected vetFormService = inject(VetFormService);
  protected specialtyService = inject(SpecialtyService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: VetFormGroup = this.vetFormService.createVetFormGroup();

  compareSpecialty = (o1: ISpecialty | null, o2: ISpecialty | null): boolean => this.specialtyService.compareSpecialty(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ vet }) => {
      this.vet = vet;
      if (vet) {
        this.updateForm(vet);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const vet = this.vetFormService.getVet(this.editForm);
    if (vet.id !== null) {
      this.subscribeToSaveResponse(this.vetService.update(vet));
    } else {
      this.subscribeToSaveResponse(this.vetService.create(vet));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IVet>>): void {
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

  protected updateForm(vet: IVet): void {
    this.vet = vet;
    this.vetFormService.resetForm(this.editForm, vet);

    this.specialtiesSharedCollection = this.specialtyService.addSpecialtyToCollectionIfMissing<ISpecialty>(
      this.specialtiesSharedCollection,
      ...(vet.specialties ?? []),
    );
  }

  protected loadRelationshipsOptions(): void {
    this.specialtyService
      .query()
      .pipe(map((res: HttpResponse<ISpecialty[]>) => res.body ?? []))
      .pipe(
        map((specialties: ISpecialty[]) =>
          this.specialtyService.addSpecialtyToCollectionIfMissing<ISpecialty>(specialties, ...(this.vet?.specialties ?? [])),
        ),
      )
      .subscribe((specialties: ISpecialty[]) => (this.specialtiesSharedCollection = specialties));
  }
}
