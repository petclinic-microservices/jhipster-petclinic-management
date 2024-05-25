import { Component, inject, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IPetType } from 'app/entities/pet-type/pet-type.model';
import { PetTypeService } from 'app/entities/pet-type/service/pet-type.service';
import { IOwner } from 'app/entities/owner/owner.model';
import { OwnerService } from 'app/entities/owner/service/owner.service';
import { PetService } from '../service/pet.service';
import { IPet } from '../pet.model';
import { PetFormService, PetFormGroup } from './pet-form.service';

@Component({
  standalone: true,
  selector: 'jhi-pet-update',
  templateUrl: './pet-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class PetUpdateComponent implements OnInit {
  isSaving = false;
  pet: IPet | null = null;

  petTypesSharedCollection: IPetType[] = [];
  ownersSharedCollection: IOwner[] = [];

  protected petService = inject(PetService);
  protected petFormService = inject(PetFormService);
  protected petTypeService = inject(PetTypeService);
  protected ownerService = inject(OwnerService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: PetFormGroup = this.petFormService.createPetFormGroup();

  comparePetType = (o1: IPetType | null, o2: IPetType | null): boolean => this.petTypeService.comparePetType(o1, o2);

  compareOwner = (o1: IOwner | null, o2: IOwner | null): boolean => this.ownerService.compareOwner(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ pet }) => {
      this.pet = pet;
      if (pet) {
        this.updateForm(pet);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const pet = this.petFormService.getPet(this.editForm);
    if (pet.id !== null) {
      this.subscribeToSaveResponse(this.petService.update(pet));
    } else {
      this.subscribeToSaveResponse(this.petService.create(pet));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IPet>>): void {
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

  protected updateForm(pet: IPet): void {
    this.pet = pet;
    this.petFormService.resetForm(this.editForm, pet);

    this.petTypesSharedCollection = this.petTypeService.addPetTypeToCollectionIfMissing<IPetType>(this.petTypesSharedCollection, pet.type);
    this.ownersSharedCollection = this.ownerService.addOwnerToCollectionIfMissing<IOwner>(this.ownersSharedCollection, pet.owner);
  }

  protected loadRelationshipsOptions(): void {
    this.petTypeService
      .query()
      .pipe(map((res: HttpResponse<IPetType[]>) => res.body ?? []))
      .pipe(map((petTypes: IPetType[]) => this.petTypeService.addPetTypeToCollectionIfMissing<IPetType>(petTypes, this.pet?.type)))
      .subscribe((petTypes: IPetType[]) => (this.petTypesSharedCollection = petTypes));

    this.ownerService
      .query()
      .pipe(map((res: HttpResponse<IOwner[]>) => res.body ?? []))
      .pipe(map((owners: IOwner[]) => this.ownerService.addOwnerToCollectionIfMissing<IOwner>(owners, this.pet?.owner)))
      .subscribe((owners: IOwner[]) => (this.ownersSharedCollection = owners));
  }
}
