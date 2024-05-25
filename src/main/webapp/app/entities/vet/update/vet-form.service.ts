import { Injectable } from '@angular/core';
import { FormGroup, FormControl, Validators } from '@angular/forms';

import { IVet, NewVet } from '../vet.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IVet for edit and NewVetFormGroupInput for create.
 */
type VetFormGroupInput = IVet | PartialWithRequiredKeyOf<NewVet>;

type VetFormDefaults = Pick<NewVet, 'id' | 'specialties'>;

type VetFormGroupContent = {
  id: FormControl<IVet['id'] | NewVet['id']>;
  firstName: FormControl<IVet['firstName']>;
  lastName: FormControl<IVet['lastName']>;
  specialties: FormControl<IVet['specialties']>;
};

export type VetFormGroup = FormGroup<VetFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class VetFormService {
  createVetFormGroup(vet: VetFormGroupInput = { id: null }): VetFormGroup {
    const vetRawValue = {
      ...this.getFormDefaults(),
      ...vet,
    };
    return new FormGroup<VetFormGroupContent>({
      id: new FormControl(
        { value: vetRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      firstName: new FormControl(vetRawValue.firstName, {
        validators: [Validators.maxLength(30)],
      }),
      lastName: new FormControl(vetRawValue.lastName, {
        validators: [Validators.maxLength(30)],
      }),
      specialties: new FormControl(vetRawValue.specialties ?? []),
    });
  }

  getVet(form: VetFormGroup): IVet | NewVet {
    return form.getRawValue() as IVet | NewVet;
  }

  resetForm(form: VetFormGroup, vet: VetFormGroupInput): void {
    const vetRawValue = { ...this.getFormDefaults(), ...vet };
    form.reset(
      {
        ...vetRawValue,
        id: { value: vetRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): VetFormDefaults {
    return {
      id: null,
      specialties: [],
    };
  }
}
