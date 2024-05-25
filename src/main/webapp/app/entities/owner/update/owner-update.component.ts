import { Component, inject, OnInit } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IOwner } from '../owner.model';
import { OwnerService } from '../service/owner.service';
import { OwnerFormService, OwnerFormGroup } from './owner-form.service';

@Component({
  standalone: true,
  selector: 'jhi-owner-update',
  templateUrl: './owner-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class OwnerUpdateComponent implements OnInit {
  isSaving = false;
  owner: IOwner | null = null;

  protected ownerService = inject(OwnerService);
  protected ownerFormService = inject(OwnerFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: OwnerFormGroup = this.ownerFormService.createOwnerFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ owner }) => {
      this.owner = owner;
      if (owner) {
        this.updateForm(owner);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const owner = this.ownerFormService.getOwner(this.editForm);
    if (owner.id !== null) {
      this.subscribeToSaveResponse(this.ownerService.update(owner));
    } else {
      this.subscribeToSaveResponse(this.ownerService.create(owner));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IOwner>>): void {
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

  protected updateForm(owner: IOwner): void {
    this.owner = owner;
    this.ownerFormService.resetForm(this.editForm, owner);
  }
}
